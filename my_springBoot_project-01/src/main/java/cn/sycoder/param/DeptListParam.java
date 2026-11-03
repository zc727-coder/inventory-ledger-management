package cn.sycoder.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@ApiModel(value = "DeptListParam", description = "部门列表查询参数")
public class DeptListParam {

    @ApiModelProperty("页码")
    private Integer page;

    @ApiModelProperty("每页条数")
    private Integer limit;

    @ApiModelProperty("部门名称（搜索）")
    private String deptName;

    @ApiModelProperty("部门代码（搜索）")
    private String deptNo;
}
