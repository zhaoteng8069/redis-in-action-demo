package com.ssnow.demo.redis02.constant;

/**
 * @ClassName LoginConstant
 * @Desc 登录相关的常量(key or key's prefix)
 * @Author zhaoteng
 * @Date 2019/8/4 10:07
 * @Version 1.0
 **/
public class LoginConstant {
    // 用户key
    public static final String LOGIN_KEY = "login:";
    // 登录key
    public static final String RECENT_KEY = "recent:";
    // 浏览记录key的前缀
    public static final String VIEWED_KEY_PREFIX = "viewed:";
}
