package cn.sycoder.service;

import cn.sycoder.domain.ProductRecord;
import cn.sycoder.exception.SystemException;
import cn.sycoder.mapper.ProductRecordMapper;
import cn.sycoder.param.GoodsListParam;
import cn.sycoder.response.ChartItemResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class ProductRecordMonthlyService {

    private static final String TABLE_PREFIX = "product_record_";
    private static final Pattern TABLE_NAME_PATTERN = Pattern.compile("^product_record_\\d{4}_\\d{2}$");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy_MM");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private ProductRecordMapper productRecordMapper;

    public void save(ProductRecord record) {
        if (record == null) {
            return;
        }
        LocalDateTime recordDate = record.getRecordDate() == null ? LocalDateTime.now() : record.getRecordDate();
        record.setRecordDate(recordDate);
        String tableName = buildTableName(YearMonth.from(recordDate));
        ensureTable(tableName);
        if (productRecordMapper.insertMonthly(tableName, record) <= 0) {
            throw new SystemException("库存台账月表保存失败");
        }
    }

    public List<ProductRecord> listForExport(GoodsListParam param) {
        DateRange dateRange = resolveDateRange(param);
        List<String> existingTableNames = existingTableNames(dateRange.startTime, dateRange.endTime);
        if (existingTableNames.isEmpty()) {
            return Collections.emptyList();
        }
        return productRecordMapper.selectMonthlyRecords(
                existingTableNames,
                trim(param != null ? param.getProductName() : null),
                trim(param != null ? param.getProductCode() : null),
                trim(param != null ? param.getLocation() : null),
                dateRange.startTime,
                dateRange.endTime
        );
    }

    public List<ChartItemResponse> topProducts(String bizType,
                                               LocalDateTime startTime,
                                               LocalDateTime endTime,
                                               int limit) {
        List<String> existingTableNames = existingTableNames(startTime, endTime);
        if (existingTableNames.isEmpty()) {
            return Collections.emptyList();
        }
        return productRecordMapper.selectTopProducts(existingTableNames, bizType, startTime, endTime, limit);
    }

    public List<ChartItemResponse> quantityByPeriod(String bizType,
                                                    LocalDateTime startTime,
                                                    LocalDateTime endTime,
                                                    boolean groupByMonth) {
        List<String> existingTableNames = existingTableNames(startTime, endTime);
        if (existingTableNames.isEmpty()) {
            return Collections.emptyList();
        }
        return productRecordMapper.selectQuantityByPeriod(existingTableNames, bizType, startTime, endTime, groupByMonth);
    }

    private DateRange resolveDateRange(GoodsListParam param) {
        LocalDate startDate = parseDate(param != null ? param.getStartDate() : null);
        LocalDate endDate = parseDate(param != null ? param.getEndDate() : null);

        if (startDate == null && endDate == null) {
            YearMonth currentMonth = YearMonth.now();
            startDate = currentMonth.atDay(1);
            endDate = currentMonth.atEndOfMonth();
        } else if (startDate == null) {
            startDate = YearMonth.from(endDate).atDay(1);
        } else if (endDate == null) {
            endDate = YearMonth.from(startDate).atEndOfMonth();
        }

        if (endDate.isBefore(startDate)) {
            LocalDate temp = startDate;
            startDate = endDate;
            endDate = temp;
        }
        return new DateRange(startDate.atStartOfDay(), LocalDateTime.of(endDate, LocalTime.MAX));
    }

    private List<String> buildTableNames(LocalDateTime startTime, LocalDateTime endTime) {
        YearMonth startMonth = YearMonth.from(startTime);
        YearMonth endMonth = YearMonth.from(endTime);
        List<String> tableNames = new ArrayList<String>();
        while (!startMonth.isAfter(endMonth)) {
            tableNames.add(buildTableName(startMonth));
            startMonth = startMonth.plusMonths(1);
        }
        return tableNames;
    }

    private List<String> existingTableNames(LocalDateTime startTime, LocalDateTime endTime) {
        List<String> tableNames = buildTableNames(startTime, endTime);
        List<String> existingTableNames = new ArrayList<String>();
        for (String tableName : tableNames) {
            validateTableName(tableName);
            Integer count = productRecordMapper.tableExists(tableName);
            if (count != null && count > 0) {
                existingTableNames.add(tableName);
            }
        }
        return existingTableNames;
    }

    private String buildTableName(YearMonth yearMonth) {
        String tableName = TABLE_PREFIX + yearMonth.format(MONTH_FORMATTER);
        validateTableName(tableName);
        return tableName;
    }

    private void ensureTable(String tableName) {
        validateTableName(tableName);
        Integer count = productRecordMapper.tableExists(tableName);
        if (count == null || count == 0) {
            productRecordMapper.createMonthlyTable(tableName);
        }
    }

    private void validateTableName(String tableName) {
        if (tableName == null || !TABLE_NAME_PATTERN.matcher(tableName).matches()) {
            throw new SystemException("库存台账月表名称不合法");
        }
    }

    private LocalDate parseDate(String value) {
        String text = trim(value);
        if (text == null) {
            return null;
        }
        try {
            return LocalDate.parse(text, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toLocalDate();
            } catch (DateTimeParseException ignored) {
                throw new SystemException("台账日期格式不合法，请使用yyyy-MM-dd");
            }
        }
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String text = value.trim();
        return text.isEmpty() ? null : text;
    }

    private static class DateRange {
        private final LocalDateTime startTime;
        private final LocalDateTime endTime;

        private DateRange(LocalDateTime startTime, LocalDateTime endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }
}
