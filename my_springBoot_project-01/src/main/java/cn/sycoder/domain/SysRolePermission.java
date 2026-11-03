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
 * 角色权限关联表
 * </p>
 *
 * @author sy
 * @since 2026-04-07
 */
@Getter
@Setter
  @TableName("sys_role_permission")
public class SysRolePermission implements Serializable {

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
     * 权限ID
     */
      private Long permissionId;

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
     * 逻辑删除 0未删 1已删
     */
      @TableLogic
      private Integer deleted;


}
