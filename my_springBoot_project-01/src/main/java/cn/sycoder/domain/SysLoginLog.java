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
 * 用户登录行为日志模板表
 * </p>
 *
 * @author sy
 * @since 2026-04-13
 */
@Getter
@Setter
  @TableName("sys_login_log")
public class SysLoginLog implements Serializable {

    private static final long serialVersionUID = 1L;

      /**
     * 登录日志ID
     */
        @TableId(value = "log_id", type = IdType.AUTO)
      private Long logId;

      /**
     * 用户ID，登录失败时可为空
     */
      private Long userId;

      /**
     * 登录用户名
     */
      private String userName;

      /**
     * 登录状态：SUCCESS成功，FAIL失败
     */
      private String loginStatus;

      /**
     * 登录IP
     */
      private String loginIp;

      /**
     * 浏览器或客户端信息
     */
      private String userAgent;

      /**
     * 登录信息或失败原因
     */
      private String message;

      /**
     * 登录时间
     */
      private LocalDateTime loginTime;

      /**
     * 创建时间
     */
      private LocalDateTime createTime;

      /**
     * 逻辑删除：0未删除，1已删除
     */
      @TableLogic
      private Integer deleted;


}
