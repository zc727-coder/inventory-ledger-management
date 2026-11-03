package cn.sycoder.exception;

import cn.sycoder.resp.Resp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AuthException.class)
    public Resp<Object> handleAuthException(AuthException e) {
        return Resp.fail(resolveMessage(e, "认证失败，请重新登录"));
    }

    @ExceptionHandler(BusinessException.class)
    public Resp<Object> handleBusinessException(BusinessException e) {
        return Resp.fail(resolveMessage(e, "业务处理失败"));
    }

    @ExceptionHandler(DataAccessException.class)
    public Resp<Object> handleDataAccessException(DataAccessException e) {
        log.error("Data access exception", e);
        return Resp.fail(resolveMessage(e, "数据访问异常，请稍后再试"));
    }

    @ExceptionHandler(SystemException.class)
    public Resp<Object> handleSystemException(SystemException e) {
        log.error("System exception", e);
        return Resp.fail(resolveMessage(e, "系统异常，请联系管理员"));
    }

    @ExceptionHandler(Exception.class)
    public Resp<Object> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return Resp.fail(resolveMessage(e, "系统异常，请联系管理员"));
    }

    private String resolveMessage(Throwable e, String defaultMessage) {
        if (e == null || e.getMessage() == null || e.getMessage().trim().isEmpty()) {
            return defaultMessage;
        }
        return e.getMessage().trim();
    }
}
