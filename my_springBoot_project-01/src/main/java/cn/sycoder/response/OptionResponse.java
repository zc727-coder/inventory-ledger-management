package cn.sycoder.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "OptionResponse", description = "下拉选项返回结果")
public class OptionResponse {

    @ApiModelProperty("选项ID")
    private Long id;

    @ApiModelProperty("选项名称")
    private String name;
}
