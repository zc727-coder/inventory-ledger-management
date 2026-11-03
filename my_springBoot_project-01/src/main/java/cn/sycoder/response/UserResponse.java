package cn.sycoder.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(value = "UserResponse", description = "用户列表返回结果")
public class UserResponse {

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("系统编号")
    private String systemNo;

    @ApiModelProperty("用户名")
    private String userName;

    @ApiModelProperty("真实姓名")
    private String userRealName;

    @ApiModelProperty("性别")
    private String userSex;

    @ApiModelProperty("手机号")
    private String userMobile;

    @ApiModelProperty("邮箱")
    private String userEmail;

    @ApiModelProperty("部门ID")
    private Long deptId;

    @ApiModelProperty("部门名称")
    private String deptName;

    @ApiModelProperty("角色ID")
    private Long roleId;

    @ApiModelProperty("角色名称")
    private String roleName;

    @ApiModelProperty("是否锁定")
    private String isLock;

    @ApiModelProperty("备注")
    private String remark;

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
