package cn.sycoder.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(value = "RoleResponse", description = "角色列表返回结果")
public class RoleResponse {

    @ApiModelProperty("角色ID")
    private Long roleId;

    @ApiModelProperty("系统编码")
    private String systemNo;

    @ApiModelProperty("角色代码")
    private String roleNo;

    @ApiModelProperty("角色名称")
    private String roleName;

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