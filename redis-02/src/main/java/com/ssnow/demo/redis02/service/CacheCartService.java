package com.ssnow.demo.redis02.service;

import com.ssnow.demo.redis02.constant.CartConstant;
import redis.clients.jedis.Jedis;

/**
 * @ClassName CacheCartService
 * @Desc 使用Redis构建WEB应用
 *         2. 缓存购物车
 * @Author zhaoteng
 * @Date 2019/8/4 10:19
 * @Version 1.0
 **/
public class CacheCartService {

    /**
     * 添加购物车
     * @desc
     *  如果商品数量小于等于0 删除购物车中相关商品信息
     *  大于0，覆盖
     */
    public void addToCart(Jedis conn, String session, String item, int count) {
        if (count <= 0) {
            conn.hdel(CartConstant.CART_KEY_PREFIX + session, item);
        } else {
            conn.hset(CartConstant.CART_KEY_PREFIX + session, item, String.valueOf(conn));
        }
    }

}
