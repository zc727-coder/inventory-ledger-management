package cn.sycoder.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(value = "ModuleGetResponse", description = "菜单详情返回结果")
public class ModuleGetResponse {

    @ApiModelProperty("菜单ID")
    private Long moduleId;

    @ApiModelProperty("父级ID")
    private Long parentId;

    @ApiModelProperty("菜单名称")
    private String moduleName;

    @ApiModelProperty("菜单URL")
    private String moduleUrl;

    @ApiModelProperty("菜单图标")
    private String moduleIcon;

    @ApiModelProperty("排序")
    private Integer moduleOrder;

    @ApiModelProperty("菜单层级")
    private Integer moduleLevel;

    @ApiModelProperty("系统编码")
    private String systemNo;

    @ApiModelProperty("是否叶子节点")
    private Integer isLeaf;

    @ApiModelProperty("完整路径")
    private String fullIndex;

    @ApiModelProperty("备注")
    private String moduleNotes;

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
