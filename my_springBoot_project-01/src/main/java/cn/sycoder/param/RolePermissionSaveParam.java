package cn.sycoder.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "RolePermissionSaveParam", description = "角色权限保存参数")
public class RolePermissionSaveParam {

    @ApiModelProperty("角色ID")
    private Long roleId;

    @ApiModelProperty("权限ID集合，逗号分隔")
    private String permissionIds;
}
