package cn.sycoder.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 角色菜单关联表
 * </p>
 *
 * @author sy
 * @since 2026-04-02
 */
@Getter
@Setter
  @TableName("sys_role_module")
public class SysRoleModule implements Serializable {

    private static final long serialVersionUID = 1L;

      /**
     * 主键ID
     */
        @TableId(value = "id", type = IdType.AUTO)
      private Long id;

      /**
     * 角色ID
     */
      private Long roleId;

      /**
     * 菜单ID
     */
      private Long moduleId;

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
      private LocalDateTime createTime;

      /**
     * 修改时间
     */
      private LocalDateTime updateTime;

      /**
     * 逻辑删除 0-未删 1-已删
     */
      @TableLogic
      private Integer deleted;


}
