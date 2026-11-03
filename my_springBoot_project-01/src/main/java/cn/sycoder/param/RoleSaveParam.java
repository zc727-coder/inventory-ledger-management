package cn.sycoder.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "RoleSaveParam", description = "角色新增/编辑参数")
public class RoleSaveParam {

    @ApiModelProperty("角色ID（有值=编辑，无值=新增）")
    private Long roleId;

    @ApiModelProperty("系统编码")
    private String systemNo;

    @ApiModelProperty("角色代码")
    private String roleNo;

    @ApiModelProperty("角色名称")
    private String roleName;
}