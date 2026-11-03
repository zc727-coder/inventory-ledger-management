package cn.sycoder.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 部门表
 * </p>
 *
 * @author sy
 * @since 2026-04-01
 */
@Getter
@Setter
  @TableName("sys_dept")
public class SysDept implements Serializable {

    private static final long serialVersionUID = 1L;

      /**
     * 部门ID
     */
        @TableId(value = "dept_id", type = IdType.AUTO)
      private Long deptId;

      /**
     * 上级部门ID（0=顶级部门）
     */
      private Long parentId;

      /**
     * 部门名称
     */
      private String deptName;

      /**
     * 部门代码
     */
      private String deptNo;

      /**
     * 创建人ID
     */
      private Long createBy;

      /**
     * 修改人ID
     */
      private Long updateBy;

      /**
     * 创建时间
     */
      @JsonProperty("addTime")
      private LocalDateTime createTime;

      /**
     * 修改时间
     */
      @JsonProperty("editTime")
      private LocalDateTime updateTime;

      /**
     * 逻辑删除 0未删 1已删
     */
      @TableLogic
      private Integer deleted;


}
