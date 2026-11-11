package com.recruitment.context;

/**
 * 当前用户上下文（第06步课件 6.4）
 * 拦截器把 userId 放入当前请求线程的临时空间（ThreadLocal），
 * 后面的 Controller、Service 随时可以读取，请求结束后再清除。
 *
 * 必须 clear()：服务器线程会重复使用，如果不清理，
 * 线程下一次处理别人的请求时，可能还保留上一次请求的 userId。
 */
public class UserContext {

    // 每个请求线程都有自己独立的一份 Long 数据
    private static final ThreadLocal<Long> userIdHolder = new ThreadLocal<>();

    // 拦截器验证成功后调用
    public static void setCurrentUserId(Long userId) {
        userIdHolder.set(userId);
    }

    // Controller 或 Service 需要当前用户时调用
    public static Long getCurrentUserId() {
        return userIdHolder.get();
    }

    // 请求结束后调用，防止线程复用时残留上一次请求的数据
    public static void clear() {
        userIdHolder.remove();
    }
}
