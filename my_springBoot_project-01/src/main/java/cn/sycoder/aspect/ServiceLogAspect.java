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

@Aspect
@Component
public class ServiceLogAspect {

    private static final Logger DB_LOG = LoggerFactory.getLogger("db-log");

    @Pointcut("execution(* cn.sycoder.service..*.*(..))")
    public void servicePointCut() {
    }

    @Around("servicePointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        String methodName = joinPoint.getSignature().getDeclaringTypeName()
                + "." + joinPoint.getSignature().getName();
        String methodParams = LogMaskUtils.maskArgs(joinPoint.getArgs());
        String currentUser = buildCurrentUser();

        try {
            Object result = joinPoint.proceed();
            long cost = System.currentTimeMillis() - start;

            DB_LOG.info(
                    "db success | method={} | user={} | params={} | cost={}ms",
                    methodName, currentUser, methodParams, cost
            );
            return result;
        } catch (Throwable e) {
            long cost = System.currentTimeMillis() - start;

            DB_LOG.error(
                    "db error | method={} | user={} | params={} | cost={}ms | error={}",
                    methodName, currentUser, methodParams, cost, e.getMessage()
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
