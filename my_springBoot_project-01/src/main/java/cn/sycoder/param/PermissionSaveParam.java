package cn.sycoder.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "PermissionSaveParam", description = "权限新增/编辑参数")
public class PermissionSaveParam {

    @ApiModelProperty("权限ID")
    private Long permissionId;

    @ApiModelProperty("权限名称")
    private String permissionName;

    @ApiModelProperty("权限标识")
    private String permission;

    @ApiModelProperty("备注")
    private String permissionNotes;
}
