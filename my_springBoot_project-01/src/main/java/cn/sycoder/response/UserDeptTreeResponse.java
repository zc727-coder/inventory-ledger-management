package cn.sycoder.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "UserDeptTreeResponse", description = "用户部门树节点返回结果")
public class UserDeptTreeResponse {

    @ApiModelProperty("节点ID")
    private Long id;

    @ApiModelProperty("父节点ID")
    private Long pId;

    @ApiModelProperty("节点名称")
    private String name;

    @ApiModelProperty("是否展开")
    private Boolean open = true;

    @ApiModelProperty("是否选中")
    private Boolean checked = false;
}
