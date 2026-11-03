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
 * 系统用户表
 * </p>
 *
 * @author sy
 * @since 2026-03-26
 */
@Getter
@Setter
@TableName("sys_user")
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

      /**
     * 用户ID
     */
        @TableId(value = "user_id", type = IdType.AUTO)
      private Long userId;

      /**
     * 系统编号
     */
      private String systemNo;

      /**
     * 登录名
     */
      private String userName;

      /**
     * 密码
     */
      private String userPassword;

      /**
     * 真实姓名
     */
      private String userRealName;

      /**
     * 性别
     */
      private String userSex;

      /**
     * 手机号
     */
      private String userMobile;

      /**
     * 邮箱
     */
      private String userEmail;

      /**
     * 所属部门/公司ID
     */
      private Long deptId;

      /**
     * 角色ID
     */
      private Long roleId;

      /**
     * N正常 Y锁定
     */
      private String isLock;

      /**
     * 备注
     */
      private String remark;

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
     * 更新时间
     */
      @JsonProperty("editTime")
      private LocalDateTime updateTime;

      /**
     * 逻辑删除 0未删 1已删
     */
      @TableLogic
      private Integer deleted;


}
