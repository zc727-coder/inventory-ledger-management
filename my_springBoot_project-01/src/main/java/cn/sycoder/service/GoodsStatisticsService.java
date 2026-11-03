package cn.sycoder.service;

import cn.sycoder.exception.BusinessException;
import cn.sycoder.param.GoodsStatisticsParam;
import cn.sycoder.response.ChartItemResponse;
import cn.sycoder.response.GoodsStatisticsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GoodsStatisticsService {

    private static final int TOP_LIMIT = 5;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_KEY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter DAY_KEY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private ProductRecordMonthlyService productRecordMonthlyService;

    @Autowired
    private SysLoginLogMonthlyService sysLoginLogMonthlyService;

    public GoodsStatisticsResponse statistics(GoodsStatisticsParam param) {
        DateRange dateRange = resolveDateRange(param);

        GoodsStatisticsResponse response = new GoodsStatisticsResponse();
        response.setScope(dateRange.scope);
        response.setStartDate(dateRange.startDate.format(DATE_FORMATTER));
        response.setEndDate(dateRange.endDate.format(DATE_FORMATTER));
        response.setLoginUserTop5(sysLoginLogMonthlyService.topLoginUsers(dateRange.startTime, dateRange.endTime, TOP_LIMIT));
        response.setStockOutProductTop5(productRecordMonthlyService.topProducts("OUT", dateRange.startTime, dateRange.endTime, TOP_LIMIT));
        response.setStockInQuantity(fillPeriods(
                productRecordMonthlyService.quantityByPeriod("IN", dateRange.startTime, dateRange.endTime, dateRange.groupByMonth),
                dateRange
        ));
        response.setStockOutQuantity(fillPeriods(
                productRecordMonthlyService.quantityByPeriod("OUT", dateRange.startTime, dateRange.endTime, dateRange.groupByMonth),
                dateRange
        ));
        return response;
    }

    private DateRange resolveDateRange(GoodsStatisticsParam param) {
        String scope = trim(param != null ? param.getScope() : null);
        if (!"year".equals(scope)) {
            scope = "month";
        }

        LocalDate now = LocalDate.now();
        int year = param != null && param.getYear() != null ? param.getYear() : now.getYear();
        if (year < 2000 || year > 2100) {
            throw new BusinessException("统计年份不合法");
        }

        if ("year".equals(scope)) {
            LocalDate startDate = LocalDate.of(year, 1, 1);
            LocalDate endDate = LocalDate.of(year, 12, 31);
            return new DateRange(scope, startDate, endDate, true);
        }

        int month = param != null && param.getMonth() != null ? param.getMonth() : now.getMonthValue();
        if (month < 1 || month > 12) {
            throw new BusinessException("统计月份不合法");
        }
        YearMonth yearMonth = YearMonth.of(year, month);
        return new DateRange(scope, yearMonth.atDay(1), yearMonth.atEndOfMonth(), false);
    }

    private List<ChartItemResponse> fillPeriods(List<ChartItemResponse> source, DateRange dateRange) {
        Map<String, Long> valueMap = new HashMap<String, Long>();
        if (source != null) {
            for (ChartItemResponse item : source) {
                if (item != null && item.getName() != null) {
                    valueMap.put(item.getName(), item.getValue() == null ? 0L : item.getValue());
                }
            }
        }

        List<ChartItemResponse> result = new ArrayList<ChartItemResponse>();
        if (dateRange.groupByMonth) {
            YearMonth month = YearMonth.from(dateRange.startDate);
            YearMonth endMonth = YearMonth.from(dateRange.endDate);
            while (!month.isAfter(endMonth)) {
                String key = month.format(MONTH_KEY_FORMATTER);
                result.add(buildItem(month.getMonthValue() + "月", valueMap.get(key)));
                month = month.plusMonths(1);
            }
            return result;
        }

        LocalDate day = dateRange.startDate;
        while (!day.isAfter(dateRange.endDate)) {
            String key = day.format(DAY_KEY_FORMATTER);
            result.add(buildItem(day.getDayOfMonth() + "日", valueMap.get(key)));
            day = day.plusDays(1);
        }
        return result;
    }

    private ChartItemResponse buildItem(String name, Long value) {
        ChartItemResponse item = new ChartItemResponse();
        item.setName(name);
        item.setValue(value == null ? 0L : value);
        return item;
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String text = value.trim();
        return text.isEmpty() ? null : text;
    }

    private static class DateRange {
        private final String scope;
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final LocalDateTime startTime;
        private final LocalDateTime endTime;
        private final boolean groupByMonth;

        private DateRange(String scope, LocalDate startDate, LocalDate endDate, boolean groupByMonth) {
            this.scope = scope;
            this.startDate = startDate;
            this.endDate = endDate;
            this.startTime = startDate.atStartOfDay();
            this.endTime = LocalDateTime.of(endDate, LocalTime.MAX);
            this.groupByMonth = groupByMonth;
        }
    }
}
