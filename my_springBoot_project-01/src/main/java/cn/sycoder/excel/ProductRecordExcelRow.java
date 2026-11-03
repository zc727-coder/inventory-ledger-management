package cn.sycoder.excel;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
@ExcelIgnoreUnannotated
public class ProductRecordExcelRow {

    @ExcelProperty(index = 0)
    private String seqNo;

    @ExcelProperty(index = 1)
    private String recordDate;

    @ExcelProperty(index = 2)
    private String warehouseName;

    @ExcelProperty(index = 3)
    private String locationName;

    @ExcelProperty(index = 4)
    private String productCode;

    @ExcelProperty(index = 5)
    private String productName;

    @ExcelProperty(index = 6)
    private String productType;

    @ExcelProperty(index = 7)
    private String bizReason;

    @ExcelProperty(index = 8)
    private String inQuantity;

    @ExcelProperty(index = 9)
    private String outQuantity;

    @ExcelProperty(index = 10)
    private String balanceQuantity;

    @ExcelProperty(index = 11)
    private String unit;

    @ExcelProperty(index = 12)
    private String operatorName;

    @ExcelProperty(index = 13)
    private String remark;
}
