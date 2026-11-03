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
 * 角色表
 * </p>
 *
 * @author sy
 * @since 2026-04-02
 */
@Getter
@Setter
  @TableName("sys_role")
public class SysRole implements Serializable {

    private static final long serialVersionUID = 1L;

      /**
     * 角色ID
     */
        @TableId(value = "role_id", type = IdType.AUTO)
      private Long roleId;

      /**
     * 系统编码
     */
      private String systemNo;

      /**
     * 角色代码
     */
      private String roleNo;

      /**
     * 角色名称
     */
      private String roleName;

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
