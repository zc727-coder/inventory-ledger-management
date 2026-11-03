package cn.sycoder.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "GoodsAlarmParam", description = "库存告警设置参数")
public class GoodsAlarmParam {

    @ApiModelProperty("产品ID")
    private Long productId;

    @ApiModelProperty("告警值")
    private Integer alarmValue;
}
