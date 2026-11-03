package cn.sycoder.aspect;

import cn.sycoder.domain.SysLoginLog;
import cn.sycoder.param.LoginParam;
import cn.sycoder.resp.Resp;
import cn.sycoder.response.LoginResponse;
import cn.sycoder.service.SysLoginLogMonthlyService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Aspect
@Order(5)
@Component
public class LoginLogAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginLogAspect.class);

    @Autowired
    private SysLoginLogMonthlyService sysLoginLogMonthlyService;

    @Around("execution(* cn.sycoder.controller.SysUserController.login(..))")
    public Object aroundLogin(ProceedingJoinPoint joinPoint) throws Throwable {
        LoginParam loginParam = findLoginParam(joinPoint.getArgs());
        String username = loginParam != null ? loginParam.getUsername() : null;

        try {
            Object result = joinPoint.proceed();
            Long userId = resolveUserId(result);
            recordLogin(userId, username, "SUCCESS", "登录成功");
            return result;
        } catch (Throwable e) {
            recordLogin(null, username, "FAIL", e.getMessage());
            throw e;
        }
    }

    private void recordLogin(Long userId, String username, String status, String message) {
        try {
            HttpServletRequest request = currentRequest();
            LocalDateTime now = LocalDateTime.now();
            SysLoginLog log = new SysLoginLog();
            log.setUserId(userId);
            log.setUserName(trimToLength(username, 100));
            log.setLoginStatus(status);
            log.setLoginIp(trimToLength(resolveIp(request), 100));
            log.setUserAgent(trimToLength(request != null ? request.getHeader("User-Agent") : null, 500));
            log.setMessage(trimToLength(message, 255));
            log.setLoginTime(now);
            log.setCreateTime(now);
            log.setDeleted(0);
            sysLoginLogMonthlyService.save(log);
        } catch (Exception e) {
            LOGGER.warn("login log save failed: {}", e.getMessage());
        }
    }

    private LoginParam findLoginParam(Object[] args) {
        if (args == null) {
            return null;
        }
        for (Object arg : args) {
            if (arg instanceof LoginParam) {
                return (LoginParam) arg;
            }
        }
        return null;
    }

    private Long resolveUserId(Object result) {
        if (!(result instanceof Resp)) {
            return null;
        }
        Object data = ((Resp<?>) result).getData();
        if (data instanceof LoginResponse) {
            return ((LoginResponse) data).getUserId();
        }
        return null;
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private String resolveIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String ip = firstHeader(request, "X-Forwarded-For");
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        if (ip == null) {
            ip = firstHeader(request, "X-Real-IP");
        }
        return ip != null ? ip : request.getRemoteAddr();
    }

    private String firstHeader(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        if (value == null || value.trim().isEmpty() || "unknown".equalsIgnoreCase(value.trim())) {
            return null;
        }
        return value.trim();
    }

    private String trimToLength(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String text = value.trim();
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }
}
