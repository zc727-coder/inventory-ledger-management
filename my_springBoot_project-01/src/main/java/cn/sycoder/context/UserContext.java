package cn.sycoder.context;



import cn.sycoder.domain.SysUser;

public class UserContext {

    private static final ThreadLocal<SysUser> THREAD_LOCAL = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(SysUser user) {
        THREAD_LOCAL.set(user);
    }

    public static SysUser get() {
        return THREAD_LOCAL.get();
    }

    public static void clear() {
        THREAD_LOCAL.remove();
    }
}

