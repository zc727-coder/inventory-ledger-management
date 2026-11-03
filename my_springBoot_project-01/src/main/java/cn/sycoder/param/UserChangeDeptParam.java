package cn.sycoder.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "UserChangeDeptParam", description = "批量修改用户部门参数")
public class UserChangeDeptParam {

    @ApiModelProperty("用户ID列表，逗号分隔")
    private String userIds;

    @ApiModelProperty("部门ID")
    private Long deptId;

    @ApiModelProperty("部门名称")
    private String deptName;
}
