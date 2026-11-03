package cn.sycoder.interceptor;

import cn.sycoder.auth.AuthInfo;
import cn.sycoder.context.UserContext;
import cn.sycoder.domain.SysUser;
import cn.sycoder.exception.AuthException;
import cn.sycoder.exception.DataAccessException;
import cn.sycoder.service.RedisSessionService;
import cn.sycoder.utils.JwtUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    private final RedisSessionService redisSessionService;

    public LoginInterceptor(RedisSessionService redisSessionService) {
        this.redisSessionService = redisSessionService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String token = request.getHeader("token");

        // Keep compatibility with requests that still pass token in query params.
        if (token == null || token.trim().isEmpty()) {
            token = request.getParameter("token");
        }

        if (token == null || token.trim().isEmpty()) {
            throw new AuthException("token不能为空，请先登录");
        }

        if (!JwtUtils.validateToken(token)) {
            throw new AuthException("token无效或已过期，请重新登录");
        }

        AuthInfo authInfo;
        try {
            authInfo = redisSessionService.getAuthInfo(token);
        } catch (Exception e) {
            throw new DataAccessException("Redis会话读取失败", e);
        }
        if (authInfo == null) {
            throw new AuthException("登录状态已失效，请重新登录");
        }
        if ("Y".equals(authInfo.getIsLock())) {
            throw new AuthException("用户已被锁定，请联系管理员");
        }

        try {
            redisSessionService.refreshAuthInfo(token);
        } catch (Exception e) {
            throw new DataAccessException("Redis会话刷新失败", e);
        }

        SysUser user = new SysUser();
        user.setUserId(authInfo.getUserId());
        user.setUserName(authInfo.getUserName());
        user.setUserRealName(authInfo.getUserRealName());
        user.setRoleId(authInfo.getRoleId());
        user.setDeptId(authInfo.getDeptId());
        user.setIsLock(authInfo.getIsLock());
        UserContext.set(user);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }
}
