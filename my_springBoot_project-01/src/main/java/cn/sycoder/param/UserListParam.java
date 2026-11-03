package cn.sycoder.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "UserListParam", description = "用户列表查询参数")
public class UserListParam {

    @ApiModelProperty("页码")
    private Integer page;

    @ApiModelProperty("每页条数")
    private Integer limit;

    @ApiModelProperty("部门ID")
    private Long deptId;

    @ApiModelProperty("用户名")
    private String userName;

    @ApiModelProperty("手机号")
    private String userMobile;

    @ApiModelProperty("是否锁定：N正常，Y锁定")
    private String isLock;
}
