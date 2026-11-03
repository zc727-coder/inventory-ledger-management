package cn.sycoder.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "UserPwdParam", description = "重置密码参数")
public class UserPwdParam {

    @ApiModelProperty("用户ID")
    private Long userId;
}
