package cn.sycoder.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "ModuleSaveParam", description = "菜单新增/编辑参数")
public class ModuleSaveParam {

    @ApiModelProperty("菜单ID（有值=编辑，无值=新增）")
    private Long moduleId;

    @ApiModelProperty("父级ID")
    private Long parentId;

    @ApiModelProperty("菜单名称")
    private String moduleName;

    @ApiModelProperty("菜单图标")
    private String moduleIcon;

    @ApiModelProperty("菜单URL")
    private String moduleUrl;

    @ApiModelProperty("排序")
    private Integer moduleOrder;

    @ApiModelProperty("系统编码")
    private String systemNo;

    @ApiModelProperty("菜单层级")
    private Integer moduleLevel;

    @ApiModelProperty("是否叶子节点")
    private Integer isLeaf;

    @ApiModelProperty("完整路径")
    private String fullIndex;

    @ApiModelProperty("备注")
    private String moduleNotes;
}
