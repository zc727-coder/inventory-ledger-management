package cn.sycoder.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "GoodsStockParam", description = "库存入库/出库参数")
public class GoodsStockParam {

    @ApiModelProperty("产品ID")
    private Long productId;

    @ApiModelProperty("操作数量")
    private Integer quantity;

    @ApiModelProperty("业务说明/出库用途")
    private String bizReason;

    @ApiModelProperty("备注")
    private String remark;
}
