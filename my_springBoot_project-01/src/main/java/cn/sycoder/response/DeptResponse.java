package cn.sycoder.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(value = "DeptResponse", description = "部门列表返回结果")
public class DeptResponse {

    @ApiModelProperty("部门ID")
    private Long deptId;

    @ApiModelProperty("部门名称")
    private String deptName;

    @ApiModelProperty("部门代码")
    private String deptNo;

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
