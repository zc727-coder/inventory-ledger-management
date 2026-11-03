package cn.sycoder.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "UserSaveParam", description = "用户新增/编辑参数")
public class UserSaveParam {

    @ApiModelProperty("用户ID，新增时不传，编辑时传")
    private Long userId;

    @ApiModelProperty("用户名")
    private String userName;

    @ApiModelProperty("真实姓名")
    private String userRealName;

    @ApiModelProperty("角色ID")
    private Long roleId;

    @ApiModelProperty("部门ID")
    private Long deptId;

    @ApiModelProperty("手机号")
    private String userMobile;

    @ApiModelProperty("邮箱")
    private String userEmail;

    @ApiModelProperty("性别")
    private String userSex;
}
