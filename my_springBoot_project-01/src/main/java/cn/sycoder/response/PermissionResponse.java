package cn.sycoder.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(value = "PermissionResponse", description = "权限列表返回结果")
public class PermissionResponse {

    @ApiModelProperty("权限ID")
    private Long permissionId;

    @ApiModelProperty("权限名称")
    private String permissionName;

    @ApiModelProperty("权限标识")
    private String permission;

    @ApiModelProperty("备注")
    private String permissionNotes;

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

    @ApiModelProperty("当前角色是否已勾选该权限")
    private Boolean checked = false;
}
