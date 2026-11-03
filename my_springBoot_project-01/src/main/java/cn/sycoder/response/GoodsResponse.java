package cn.sycoder.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(value = "GoodsResponse", description = "库存列表返回结果")
public class GoodsResponse {

    @ApiModelProperty("产品ID")
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

    @ApiModelProperty("历史入库总量")
    private Integer inventorySum;

    @ApiModelProperty("当前库存")
    private Integer inventory;

    @ApiModelProperty("历史出库总量")
    private Integer inventoryRemove;

    @ApiModelProperty("告警值")
    private Integer alarmValue;

    @ApiModelProperty("是否告警")
    private Integer alarm;

    @ApiModelProperty("图片地址")
    private String img;

    @ApiModelProperty("创建人")
    private String addUser;

    @ApiModelProperty("修改人")
    private String editUser;

    @ApiModelProperty("创建时间")
    @JsonProperty("addTime")
    private LocalDateTime createTime;

    @ApiModelProperty("修改时间")
    @JsonProperty("editTime")
    private LocalDateTime updateTime;
}
