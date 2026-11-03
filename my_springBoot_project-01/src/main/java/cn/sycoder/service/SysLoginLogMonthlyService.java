package cn.sycoder.service;

import cn.sycoder.domain.SysLoginLog;
import cn.sycoder.exception.SystemException;
import cn.sycoder.mapper.SysLoginLogMapper;
import cn.sycoder.response.ChartItemResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class SysLoginLogMonthlyService {

    private static final String TABLE_PREFIX = "sys_login_log_";
    private static final Pattern TABLE_NAME_PATTERN = Pattern.compile("^sys_login_log_\\d{4}_\\d{2}$");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy_MM");

    @Autowired
    private SysLoginLogMapper sysLoginLogMapper;

    public void save(SysLoginLog log) {
        if (log == null) {
            return;
        }
        LocalDateTime loginTime = log.getLoginTime() == null ? LocalDateTime.now() : log.getLoginTime();
        log.setLoginTime(loginTime);
        if (log.getCreateTime() == null) {
            log.setCreateTime(LocalDateTime.now());
        }
        if (log.getDeleted() == null) {
            log.setDeleted(0);
        }

        String tableName = buildTableName(YearMonth.from(loginTime));
        ensureTable(tableName);
        if (sysLoginLogMapper.insertMonthly(tableName, log) <= 0) {
            throw new SystemException("登录日志月表保存失败");
        }
    }

    public List<ChartItemResponse> topLoginUsers(LocalDateTime startTime, LocalDateTime endTime, int limit) {
        List<String> existingTableNames = existingTableNames(startTime, endTime);
        if (existingTableNames.isEmpty()) {
            return Collections.emptyList();
        }
        return sysLoginLogMapper.selectTopLoginUsers(existingTableNames, startTime, endTime, limit);
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
            Integer count = sysLoginLogMapper.tableExists(tableName);
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
        Integer count = sysLoginLogMapper.tableExists(tableName);
        if (count == null || count == 0) {
            sysLoginLogMapper.createMonthlyTable(tableName);
        }
    }

    private void validateTableName(String tableName) {
        if (tableName == null || !TABLE_NAME_PATTERN.matcher(tableName).matches()) {
            throw new SystemException("登录日志月表名称不合法");
        }
    }
}
