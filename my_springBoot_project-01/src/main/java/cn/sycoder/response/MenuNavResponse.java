package cn.sycoder.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "MenuNavResponse", description = "左侧菜单平铺返回结果")
public class MenuNavResponse {

    @ApiModelProperty("菜单ID")
    private Long moduleId;

    @ApiModelProperty("父级菜单ID")
    private Long parentId;

    @ApiModelProperty("菜单名称")
    private String moduleName;

    @ApiModelProperty("菜单URL")
    private String moduleUrl;

    @ApiModelProperty("菜单图标")
    private String moduleIcon;

    @ApiModelProperty("排序")
    private Integer moduleOrder;

    @ApiModelProperty("菜单层级")
    private Integer moduleLevel;
}
