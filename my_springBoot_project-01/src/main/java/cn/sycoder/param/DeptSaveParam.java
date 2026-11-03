package cn.sycoder.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "DeptSaveParam", description = "部门新增/编辑参数")
public class DeptSaveParam {

    @ApiModelProperty("部门ID（有值=编辑，无值=新增）")
    private Long deptId;

    @ApiModelProperty("部门名称")
    private String deptName;

    @ApiModelProperty("部门代码")
    private String deptNo;
}
