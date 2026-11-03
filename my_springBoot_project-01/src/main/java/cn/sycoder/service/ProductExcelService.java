package cn.sycoder.service;

import cn.sycoder.context.UserContext;
import cn.sycoder.domain.Product;
import cn.sycoder.domain.ProductRecord;
import cn.sycoder.domain.SysUser;
import cn.sycoder.exception.BusinessException;
import cn.sycoder.exception.SystemException;
import cn.sycoder.excel.ProductRecordExcelRow;
import cn.sycoder.param.GoodsListParam;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductExcelService {

    private static final String TEMPLATE_PATH = "excel/product_record_template.xlsx";
    private static final DateTimeFormatter EXPORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/M/d");
    private static final DateTimeFormatter EXPORT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/M/d HH:mm:ss");
    private static final DateTimeFormatter[] DATE_PATTERNS = new DateTimeFormatter[]{
            DateTimeFormatter.ofPattern("yyyy/M/d"),
            DateTimeFormatter.ofPattern("yyyy-M-d"),
            DateTimeFormatter.ofPattern("yyyy/M/d H:m:s"),
            DateTimeFormatter.ofPattern("yyyy-M-d H:m:s"),
            DateTimeFormatter.ofPattern("yyyy/M/d H:m"),
            DateTimeFormatter.ofPattern("yyyy-M-d H:m"),
            DateTimeFormatter.ofPattern("yyyy/M/d HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy年M月d日"),
            DateTimeFormatter.ofPattern("yyyy年M月d日 H时m分"),
            DateTimeFormatter.ofPattern("yyyy年M月d日 H时m分s秒")
    };

    @Autowired
    private IProductService productService;

    @Autowired
    private ProductRecordMonthlyService productRecordMonthlyService;

    @Transactional(rollbackFor = Exception.class)
    public void importExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xls") && !fileName.endsWith(".xlsx"))) {
            throw new BusinessException("请上传Excel文件");
        }

        List<ProductRecordExcelRow> rows;
        try (InputStream inputStream = file.getInputStream()) {
            rows = EasyExcel.read(inputStream)
                    .head(ProductRecordExcelRow.class)
                    .sheet(0)
                    .headRowNumber(5)
                    .doReadSync();
        } catch (IOException e) {
            throw new SystemException("读取Excel文件失败", e);
        } catch (Exception e) {
            throw new BusinessException("Excel解析失败，请检查台账模板和内容");
        }

        if (rows == null || rows.isEmpty()) {
            throw new BusinessException("Excel中没有可导入的数据");
        }

        SysUser currentUser = UserContext.get();
        Long currentUserId = currentUser != null ? currentUser.getUserId() : null;
        String currentUserName = resolveCurrentUserName(currentUser);

        int successCount = 0;
        for (int i = 0; i < rows.size(); i++) {
            ProductRecordExcelRow row = rows.get(i);
            if (isBlankRow(row)) {
                continue;
            }
            processImportRow(row, i + 6, currentUserId, currentUserName);
            successCount++;
        }

        if (successCount == 0) {
            throw new BusinessException("Excel中没有有效数据");
        }
    }

    public void exportExcel(HttpServletResponse response, GoodsListParam param) {
        List<ProductRecordExcelRow> rows = buildExportRows(param);

        InputStream templateStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(TEMPLATE_PATH);
        if (templateStream == null) {
            throw new SystemException("台账模板不存在，请检查资源文件");
        }

        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            String fileName = URLEncoder.encode("库存台账", StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

            ExcelWriter writer = EasyExcel.write(response.getOutputStream())
                    .withTemplate(templateStream)
                    .autoCloseStream(false)
                    .build();
            WriteSheet writeSheet = EasyExcel.writerSheet(0).build();
            FillConfig fillConfig = FillConfig.builder().forceNewRow(true).build();
            writer.fill(rows, fillConfig, writeSheet);
            writer.finish();
        } catch (IOException e) {
            throw new SystemException("Excel导出失败", e);
        } finally {
            try {
                templateStream.close();
            } catch (IOException ignored) {
            }
        }
    }

    public void createManualSnapshotRecord(Product product, Integer inventoryValue, String remark) {
        if (product == null || product.getProductId() == null) {
            return;
        }

        int balance = inventoryValue == null ? 0 : inventoryValue;
        LocalDateTime now = LocalDateTime.now();
        SysUser currentUser = UserContext.get();
        String currentUserName = resolveCurrentUserName(currentUser);
        Long currentUserId = currentUser != null ? currentUser.getUserId() : null;
        String[] locationParts = splitLocation(product.getLocation());

        ProductRecord record = new ProductRecord();
        record.setProductId(product.getProductId());
        record.setRecordDate(now);
        record.setWarehouseName(locationParts[0]);
        record.setLocationName(locationParts[1]);
        record.setProductCode(product.getProductCode());
        record.setProductName(product.getProductName());
        record.setProductType(product.getProductType());
        record.setBizType("CHECK");
        record.setBizReason("手工维护库存");
        record.setInQuantity(0);
        record.setOutQuantity(0);
        record.setBalanceQuantity(balance);
        record.setUnit(product.getUnit());
        record.setOperatorName(currentUserName);
        record.setRemark(remark);
        record.setSourceType("MANUAL");
        record.setCreateBy(currentUserId);
        record.setUpdateBy(currentUserId);
        record.setCreateTime(now);
        record.setUpdateTime(now);
        record.setDeleted(0);

        productRecordMonthlyService.save(record);
    }

    private void processImportRow(ProductRecordExcelRow row, int rowNo, Long currentUserId, String currentUserName) {
        String productCode = trim(row.getProductCode());
        if (isBlank(productCode)) {
            throw new BusinessException("第" + rowNo + "行产品编号不能为空");
        }

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getProductCode, productCode);
        Product product = productService.getOne(wrapper);

        int inQuantity = parseInteger(row.getInQuantity(), 0, "第" + rowNo + "行入库数量格式错误");
        int outQuantity = parseInteger(row.getOutQuantity(), 0, "第" + rowNo + "行出库数量格式错误");
        Integer balanceQuantity = parseNullableInteger(row.getBalanceQuantity(), "第" + rowNo + "行余量格式错误");

        if (product == null) {
            String productName = trim(row.getProductName());
            if (isBlank(productName)) {
                throw new BusinessException("第" + rowNo + "行产品名称不能为空");
            }

            LocalDateTime now = LocalDateTime.now();
            product = new Product();
            product.setProductName(productName);
            product.setProductCode(productCode);
            product.setProductType(trim(row.getProductType()));
            product.setUnit(trim(row.getUnit()));
            product.setLocation(joinLocation(row.getWarehouseName(), row.getLocationName()));
            product.setInventorySum(inQuantity);
            product.setInventoryRemove(outQuantity);
            product.setInventory(balanceQuantity != null ? balanceQuantity : Math.max(0, inQuantity - outQuantity));
            product.setAlarmValue(0);
            product.setAlarm(calcAlarm(product.getInventory(), product.getAlarmValue()));
            product.setCreateBy(currentUserId);
            product.setUpdateBy(currentUserId);
            product.setCreateTime(now);
            product.setUpdateTime(now);
            product.setDeleted(0);
            if (!productService.save(product)) {
                throw new SystemException("第" + rowNo + "行库存商品保存失败");
            }
        } else {
            int currentInventory = defaultInt(product.getInventory());
            product.setProductName(firstNonBlank(row.getProductName(), product.getProductName()));
            product.setProductType(firstNonBlank(row.getProductType(), product.getProductType()));
            product.setUnit(firstNonBlank(row.getUnit(), product.getUnit()));

            String location = joinLocation(row.getWarehouseName(), row.getLocationName());
            if (!isBlank(location)) {
                product.setLocation(location);
            }

            product.setInventorySum(defaultInt(product.getInventorySum()) + inQuantity);
            product.setInventoryRemove(defaultInt(product.getInventoryRemove()) + outQuantity);
            int computedBalance = currentInventory + inQuantity - outQuantity;
            product.setInventory(balanceQuantity != null ? balanceQuantity : computedBalance);
            if (product.getInventory() != null && product.getInventory() < 0) {
                throw new BusinessException("第" + rowNo + "行库存余量不能小于0");
            }
            product.setAlarm(calcAlarm(product.getInventory(), product.getAlarmValue()));
            product.setUpdateBy(currentUserId);
            product.setUpdateTime(LocalDateTime.now());
            if (!productService.updateById(product)) {
                throw new SystemException("第" + rowNo + "行库存商品更新失败");
            }
        }

        ProductRecord record = new ProductRecord();
        LocalDateTime now = LocalDateTime.now();
        record.setProductId(product.getProductId());
        record.setRecordDate(parseRecordDate(row.getRecordDate()));
        record.setWarehouseName(trim(row.getWarehouseName()));
        record.setLocationName(trim(row.getLocationName()));
        record.setProductCode(product.getProductCode());
        record.setProductName(firstNonBlank(row.getProductName(), product.getProductName()));
        record.setProductType(firstNonBlank(row.getProductType(), product.getProductType()));
        record.setBizType(resolveBizType(inQuantity, outQuantity));
        record.setBizReason(trim(row.getBizReason()));
        record.setInQuantity(inQuantity);
        record.setOutQuantity(outQuantity);
        record.setBalanceQuantity(product.getInventory());
        record.setUnit(firstNonBlank(row.getUnit(), product.getUnit()));
        record.setOperatorName(firstNonBlank(row.getOperatorName(), currentUserName));
        record.setRemark(trim(row.getRemark()));
        record.setSourceType("IMPORT");
        record.setCreateBy(currentUserId);
        record.setUpdateBy(currentUserId);
        record.setCreateTime(now);
        record.setUpdateTime(now);
        record.setDeleted(0);

        productRecordMonthlyService.save(record);
    }

    private List<ProductRecordExcelRow> buildExportRows(GoodsListParam param) {
        List<ProductRecord> recordList = productRecordMonthlyService.listForExport(param);
        List<ProductRecordExcelRow> rows = new ArrayList<>();
        int seq = 1;
        for (ProductRecord record : recordList) {
            rows.add(toExcelRow(record, seq++));
        }

        LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
        productWrapper.like(hasText(param != null ? param.getProductName() : null), Product::getProductName, trim(param != null ? param.getProductName() : null))
                .like(hasText(param != null ? param.getProductCode() : null), Product::getProductCode, trim(param != null ? param.getProductCode() : null))
                .like(hasText(param != null ? param.getLocation() : null), Product::getLocation, trim(param != null ? param.getLocation() : null))
                .orderByDesc(Product::getUpdateTime)
                .orderByDesc(Product::getProductId);

        List<Product> productList = productService.list(productWrapper);
        Set<String> recordedCodes = recordList.stream()
                .map(ProductRecord::getProductCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));

        for (Product product : productList) {
            if (isBlank(product.getProductCode()) || recordedCodes.contains(product.getProductCode())) {
                continue;
            }
            rows.add(toSnapshotRow(product, seq++));
        }

        if (rows.isEmpty()) {
            rows.add(new ProductRecordExcelRow());
        }
        return rows;
    }

    private ProductRecordExcelRow toExcelRow(ProductRecord record, int seq) {
        ProductRecordExcelRow row = new ProductRecordExcelRow();
        row.setSeqNo(String.valueOf(seq));
        row.setRecordDate(formatDate(record.getRecordDate()));
        row.setWarehouseName(defaultString(record.getWarehouseName()));
        row.setLocationName(defaultString(record.getLocationName()));
        row.setProductCode(defaultString(record.getProductCode()));
        row.setProductName(defaultString(record.getProductName()));
        row.setProductType(defaultString(record.getProductType()));
        row.setBizReason(defaultString(record.getBizReason()));
        row.setInQuantity(String.valueOf(defaultInt(record.getInQuantity())));
        row.setOutQuantity(String.valueOf(defaultInt(record.getOutQuantity())));
        row.setBalanceQuantity(String.valueOf(defaultInt(record.getBalanceQuantity())));
        row.setUnit(defaultString(record.getUnit()));
        row.setOperatorName(defaultString(record.getOperatorName()));
        row.setRemark(defaultString(record.getRemark()));
        return row;
    }

    private ProductRecordExcelRow toSnapshotRow(Product product, int seq) {
        ProductRecordExcelRow row = new ProductRecordExcelRow();
        String[] locationParts = splitLocation(product.getLocation());
        row.setSeqNo(String.valueOf(seq));
        row.setRecordDate(formatDate(product.getUpdateTime() != null ? product.getUpdateTime() : product.getCreateTime()));
        row.setWarehouseName(locationParts[0]);
        row.setLocationName(locationParts[1]);
        row.setProductCode(defaultString(product.getProductCode()));
        row.setProductName(defaultString(product.getProductName()));
        row.setProductType(defaultString(product.getProductType()));
        row.setBizReason("");
        row.setInQuantity("0");
        row.setOutQuantity("0");
        row.setBalanceQuantity(String.valueOf(defaultInt(product.getInventory())));
        row.setUnit(defaultString(product.getUnit()));
        row.setOperatorName("");
        row.setRemark("当前库存快照");
        return row;
    }

    private String resolveBizType(int inQuantity, int outQuantity) {
        if (inQuantity > 0 && outQuantity == 0) {
            return "IN";
        }
        if (outQuantity > 0 && inQuantity == 0) {
            return "OUT";
        }
        return "CHECK";
    }

    private int calcAlarm(Integer inventory, Integer alarmValue) {
        int currentInventory = defaultInt(inventory);
        int currentAlarmValue = defaultInt(alarmValue);
        return currentInventory < currentAlarmValue ? 1 : 0;
    }

    private LocalDateTime parseRecordDate(String raw) {
        String value = trim(raw);
        if (isBlank(value)) {
            return LocalDate.now().atStartOfDay();
        }

        if (value.matches("^\\d+(\\.\\d+)?$")) {
            try {
                BigDecimal serial = new BigDecimal(value);
                LocalDate baseDate = LocalDate.of(1899, 12, 30).plusDays(serial.intValue());
                return LocalDateTime.of(baseDate, LocalTime.MIN);
            } catch (Exception ignored) {
            }
        }

        for (DateTimeFormatter formatter : DATE_PATTERNS) {
            try {
                return LocalDateTime.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
            }
            try {
                return LocalDate.parse(value, formatter).atStartOfDay();
            } catch (DateTimeParseException ignored) {
            }
        }

        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDate.parse(value).atStartOfDay();
        } catch (DateTimeParseException ignored) {
        }

        throw new BusinessException("日期格式无法识别: " + value);
    }

    private Integer parseNullableInteger(String raw, String errorMessage) {
        String value = trim(raw);
        if (isBlank(value)) {
            return null;
        }
        try {
            return new BigDecimal(value).intValue();
        } catch (Exception e) {
            throw new BusinessException(errorMessage);
        }
    }

    private int parseInteger(String raw, int defaultValue, String errorMessage) {
        Integer value = parseNullableInteger(raw, errorMessage);
        return value == null ? defaultValue : value;
    }

    private boolean isBlankRow(ProductRecordExcelRow row) {
        return isBlank(row.getRecordDate())
                && isBlank(row.getWarehouseName())
                && isBlank(row.getLocationName())
                && isBlank(row.getProductCode())
                && isBlank(row.getProductName())
                && isBlank(row.getProductType())
                && isBlank(row.getBizReason())
                && isBlank(row.getInQuantity())
                && isBlank(row.getOutQuantity())
                && isBlank(row.getBalanceQuantity())
                && isBlank(row.getUnit())
                && isBlank(row.getOperatorName())
                && isBlank(row.getRemark());
    }

    private String[] splitLocation(String location) {
        String[] result = new String[]{"", ""};
        if (isBlank(location)) {
            return result;
        }

        String[] parts = location.split("[,，]");
        result[0] = trim(parts[0]);
        if (parts.length > 1) {
            result[1] = trim(String.join("，", Arrays.copyOfRange(parts, 1, parts.length)));
        }
        return result;
    }

    private String joinLocation(String warehouseName, String locationName) {
        String warehouse = trim(warehouseName);
        String location = trim(locationName);
        if (!isBlank(warehouse) && !isBlank(location)) {
            return warehouse + "，" + location;
        }
        if (!isBlank(warehouse)) {
            return warehouse;
        }
        return defaultString(location);
    }

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        if (dateTime.toLocalTime().equals(LocalTime.MIN)) {
            return dateTime.format(EXPORT_DATE_FORMATTER);
        }
        return dateTime.format(EXPORT_DATE_TIME_FORMATTER);
    }

    private String resolveCurrentUserName(SysUser currentUser) {
        if (currentUser == null) {
            return "系统";
        }
        if (!isBlank(currentUser.getUserRealName())) {
            return currentUser.getUserRealName().trim();
        }
        if (!isBlank(currentUser.getUserName())) {
            return currentUser.getUserName().trim();
        }
        return "系统";
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean hasText(String value) {
        return !isBlank(value);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String firstNonBlank(String candidate, String fallback) {
        return isBlank(candidate) ? fallback : candidate.trim();
    }
}
