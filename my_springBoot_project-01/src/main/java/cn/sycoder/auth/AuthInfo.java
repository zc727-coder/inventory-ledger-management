package cn.sycoder.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private String userName;
    private String userRealName;
    private Long roleId;
    private Long deptId;
    private String isLock;
    private String token;
    private Long loginTime;
    private Long expireTime;
}
