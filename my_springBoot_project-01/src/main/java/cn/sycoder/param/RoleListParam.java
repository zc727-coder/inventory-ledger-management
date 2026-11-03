package cn.sycoder.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@ApiModel(value = "RoleListParam", description = "角色列表查询参数")
public class RoleListParam {

    @ApiModelProperty("页码")
    private Integer page;

    @ApiModelProperty("每页条数")
    private Integer limit;

    @ApiModelProperty("角色名称（搜索）")
    private String roleName;

    @ApiModelProperty("角色代码（搜索）")
    private String roleNo;
}