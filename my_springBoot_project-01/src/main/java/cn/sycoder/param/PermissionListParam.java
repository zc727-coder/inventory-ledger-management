package cn.sycoder.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "PermissionListParam", description = "权限列表查询参数")
public class PermissionListParam {

    @ApiModelProperty("页码")
    private Integer page;

    @ApiModelProperty("每页条数")
    private Integer limit;

    @ApiModelProperty("权限名称")
    private String permissionName;

    @ApiModelProperty("权限标识")
    private String permission;

    @ApiModelProperty("角色ID")
    private Long roleId;
}
