package com.ssnow.demo.redis02.service;

import com.ssnow.demo.redis02.constant.CartConstant;
import com.ssnow.demo.redis02.constant.LoginConstant;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @ClassName CacheLoginService
 * @Desc TODO
 * @Author LoginService
 * @Date 2019/8/4 8:59
 * @Version 1.0
 * @Desc
 *  使用Redis构建WEB应用
 *      1. 登陆和缓存Cookie
 **/
public class CacheLoginService {

    /**
     * 检查token
     */
    public String checkToken(Jedis conn, String token) {
        return conn.hget(LoginConstant.LOGIN_KEY, token);
    }

    /**
     * 更新token
     * @param item 商品信息
     */
    public void updateToken(Jedis conn, String token, String user, String item) {
        long timestamp = System.currentTimeMillis() / 1000;
        conn.hset(LoginConstant.LOGIN_KEY, token, user);
        conn.zadd(LoginConstant.RECENT_KEY, timestamp, token);
        if (item != null) {
            conn.zadd(LoginConstant.VIEWED_KEY_PREFIX + token, timestamp, item);
            // 移除有序集合中 0 - -26，-26表示下标倒着数，保留25个元素
            conn.zremrangeByRank(LoginConstant.VIEWED_KEY_PREFIX + token, 0, -26);
            conn.zincrby(LoginConstant.VIEWED_KEY_PREFIX, -1, item);
        }
    }

    /**
     * 清理Session的线程类
     */
    public class CleanSessionThread extends Thread {

        private Jedis conn;
        private int limit;
        private boolean quit;

        /**
         * 清理数量超过limit的cookie信息
         * @param limit
         */
        public CleanSessionThread(int limit) {
            this.conn = new Jedis("39.105.156.77");
            this.conn.auth("211314Pijiu");
            this.conn.select(15);
            this.limit = limit;
        }
        public void quit(){
            quit = true;
        }

        @Override
        public void run() {
            while (!quit) {
                // 统计cookie的数量，小于limit，沉睡一秒
                long size = conn.zcard(LoginConstant.RECENT_KEY);
                if (size <= limit) {
                    try {
                        sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    continue;
                }

                long endIndex = Math.min(size - limit, 100);
                // 获取最早登录的endIndex个cookie
                Set<String> tokenSet = conn.zrange(LoginConstant.RECENT_KEY, 0, endIndex-1);
                String[] tokens = tokenSet.toArray(new String[tokenSet.size()]);

                List<String> sessionKeys = new ArrayList<>();
                for (String token : tokens) {
                    sessionKeys.add(LoginConstant.VIEWED_KEY_PREFIX + token);
                    // 第二章，购物车追加
                    sessionKeys.add(CartConstant.CART_KEY_PREFIX + token);
                }
                // 删除商品的有序集合
                conn.del(sessionKeys.toArray(new String[sessionKeys.size()]));
                // 删除对应的登录记录
                conn.zrem(LoginConstant.RECENT_KEY, tokens);
                // 删除对应的用户信息
                conn.hdel(LoginConstant.LOGIN_KEY, tokens);

            }
        }
    }


}
