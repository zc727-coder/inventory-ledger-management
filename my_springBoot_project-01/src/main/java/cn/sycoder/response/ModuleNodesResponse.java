package cn.sycoder.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "ModuleNodesResponse", description = "父级菜单下拉返回")
public class ModuleNodesResponse {

    @ApiModelProperty("菜单ID")
    private Long id;

    @ApiModelProperty("菜单名称")
    private String name;
}
