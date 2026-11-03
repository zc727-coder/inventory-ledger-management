package cn.sycoder.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "UserDeptSaveParam", description = "用户部门权限保存参数")
public class UserDeptSaveParam {

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("部门ID JSON数组")
    private String deptIds;
}
