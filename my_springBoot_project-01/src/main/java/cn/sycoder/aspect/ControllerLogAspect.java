package cn.sycoder.aspect;

import cn.sycoder.context.UserContext;
import cn.sycoder.domain.SysUser;
import cn.sycoder.utils.LogMaskUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class ControllerLogAspect {

    private static final Logger REQUEST_LOG = LoggerFactory.getLogger("request-log");

    @Pointcut("execution(* cn.sycoder.controller..*.*(..))")
    public void controllerPointCut() {
    }

    @Around("controllerPointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        String requestUrl = request != null ? request.getRequestURI() : "";
        String requestType = request != null ? request.getMethod() : "";
        String requestMethod = joinPoint.getSignature().getDeclaringTypeName()
                + "." + joinPoint.getSignature().getName();
        String methodParams = LogMaskUtils.maskArgs(joinPoint.getArgs());
        String currentUser = buildCurrentUser();

        try {
            Object result = joinPoint.proceed();
            long cost = System.currentTimeMillis() - start;

            REQUEST_LOG.info(
                    "request success | url={} | type={} | method={} | user={} | params={} | cost={}ms",
                    requestUrl, requestType, requestMethod, currentUser, methodParams, cost
            );
            return result;
        } catch (Throwable e) {
            long cost = System.currentTimeMillis() - start;

            REQUEST_LOG.error(
                    "request error | url={} | type={} | method={} | user={} | params={} | cost={}ms | error={}",
                    requestUrl, requestType, requestMethod, currentUser, methodParams, cost, e.getMessage()
            );
            throw e;
        }
    }

    private String buildCurrentUser() {
        SysUser user = UserContext.get();
        if (user == null) {
            return "anonymous";
        }
        return "userId=" + user.getUserId() + ",userName=" + user.getUserName();
    }
}
