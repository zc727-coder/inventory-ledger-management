package cn.sycoder.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "GoodsListParam", description = "库存列表查询参数")
public class GoodsListParam {

    @ApiModelProperty("页码")
    private Integer page;

    @ApiModelProperty("每页条数")
    private Integer limit;

    @ApiModelProperty("产品名称")
    private String productName;

    @ApiModelProperty("产品编号")
    private String productCode;

    @ApiModelProperty("库房位置")
    private String location;

    @ApiModelProperty("台账开始日期，格式yyyy-MM-dd")
    private String startDate;

    @ApiModelProperty("台账结束日期，格式yyyy-MM-dd")
    private String endDate;
}
