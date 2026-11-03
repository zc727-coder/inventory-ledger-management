package cn.sycoder.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(value = "ModuleResponse", description = "菜单列表返回结果")
public class ModuleResponse {

    @ApiModelProperty("菜单ID")
    private Long id;

    @ApiModelProperty("父级ID")
    private Long pId;

    @ApiModelProperty("菜单名称")
    private String name;

    @ApiModelProperty("是否展开")
    private Boolean open = true;

    @ApiModelProperty("是否选中")
    private Boolean checked = false;

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

    @ApiModelProperty("菜单URL")
    private String moduleUrl;

    @ApiModelProperty("菜单图标")
    private String moduleIcon;

    @ApiModelProperty("排序")
    private Integer moduleOrder;
}
