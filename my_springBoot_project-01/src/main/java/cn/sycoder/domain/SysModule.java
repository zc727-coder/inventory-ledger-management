package cn.sycoder.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 菜单表
 * </p>
 *
 * @author sy
 * @since 2026-04-02
 */
@Getter
@Setter
  @TableName("sys_module")
public class SysModule implements Serializable {

    private static final long serialVersionUID = 1L;

      /**
     * 菜单ID
     */
        @TableId(value = "module_id", type = IdType.AUTO)
      private Long moduleId;

      /**
     * 父级ID（0=顶级）
     */
      private Long parentId;

      /**
     * 菜单名称
     */
      private String moduleName;

      /**
     * 菜单代码
     */
      private String moduleNo;

      /**
     * 菜单层级
     */
      private Integer moduleLevel;

      /**
     * 菜单URL
     */
      private String moduleUrl;

      /**
     * 菜单图标
     */
      private String moduleIcon;

      /**
     * 排序
     */
      private Integer moduleOrder;

      /**
     * 系统编码
     */
      private String systemNo;

      /**
     * 是否叶子节点
     */
      private Integer isLeaf;

      /**
     * 完整路径
     */
      private String fullIndex;

      /**
     * 备注
     */
      private String moduleNotes;

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
      private Integer deleted;


}
