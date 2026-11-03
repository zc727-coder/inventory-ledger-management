package cn.sycoder.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "GoodsSaveParam", description = "库存新增/编辑参数")
public class GoodsSaveParam {

    @ApiModelProperty("产品ID，有值编辑，无值新增")
    private Long productId;

    @ApiModelProperty("产品名称")
    private String productName;

    @ApiModelProperty("产品编号")
    private String productCode;

    @ApiModelProperty("型号")
    private String productType;

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("库房位置")
    private String location;

    @ApiModelProperty("当前库存")
    private Integer inventory;

    @ApiModelProperty("告警值")
    private Integer alarmValue;

    @ApiModelProperty("图片地址")
    private String img;

    @ApiModelProperty("产品图片地址，多个用英文逗号分隔")
    private String imgUrls;
}
