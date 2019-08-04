package com.ssnow.demo.redis02.service;

import com.ssnow.demo.redis02.constant.LoginConstant;
import com.ssnow.demo.redis02.constant.PageConstant;
import redis.clients.jedis.Jedis;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;


/**
 * @ClassName CachePageService
 * @Desc 3. 缓存页面
 * @Author zhaoteng
 * @Date 2019/8/4 10:36
 * @Version 1.0
 **/
public class CachePageService {

    /**
     * 缓存页面
     */
    public String cacheRequest(Jedis conn, String request, Callback callback) {
        if (!canCache(conn, request)) {
            return callback != null ? callback.call(request) : null;
        }
        String pageKey = PageConstant.PAGE_KEY_PREFIX + hashRequest(request);
        String content = conn.get(pageKey);
        if (content == null && callback != null) {
            content = callback.call(request);
            conn.setex(pageKey, 300, content);
        }
        return content;
    }


    public interface Callback {
        public String call(String request);
    }

    public boolean canCache(Jedis conn, String request) {
        try {
            URL url = new URL(request);
            Map<String, String> params = new HashMap<>();
            if (url.getQuery() != null) {
                for (String param : url.getQuery().split("&")) {
                    String[] pair = param.split("=", 2);
                    params.put(pair[0], pair.length == 2 ? pair[1] : null);
                }
            }
            // 不存在item的value或者 没有_ 的key
            String itemId = extractItemId(params);
            if (itemId == null || isDynamic(params)) {
                return false;
            }
            Long rank = conn.zrank(LoginConstant.VIEWED_KEY_PREFIX, itemId);
            return rank != null && rank < 10000;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public String extractItemId(Map<String,String> params) {
        return params.get("item");
    }

    public boolean isDynamic(Map<String, String> params) {
        return params.containsKey("_");
    }

    public String hashRequest(String request) {
        return String.valueOf(request.hashCode());
    }
}
