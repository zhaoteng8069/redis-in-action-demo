package com.ssnow.demo.redis01.service;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ZParams;

import java.util.List;
import java.util.Map;

/**
 * @ClassName C
 * @Desc 对文章进行分组
 * @Author zhaoteng
 * @Date 2019/7/28 17:16
 * @Version 1.0
 **/
public class C {

    /**
     * 文章添加分组
     * @param conn 连接
     * @param articleId 文章id
     * @param toAdd 组id集合
     */
    public void addGroups(Jedis conn, String articleId, String[] toAdd) {
        String article = "article:" + articleId;
        for (String group : toAdd) {
            conn.sadd("group:" + group, article);
        }
    }

    /**
     * 获取指定分组的文章列表
     * @param conn 连接
     * @param group 组名称
     * @param page 页数
     * @param order 指定ZSet
     * @return
     */
    public List<Map<String, String>> getGroupArticles(Jedis conn, String group, int page, String order) {
        String key = order + group;//order:111
        if (!conn.exists(key)) {//查看key是否存在
            // 如果有交集，交集有scope，num相加，min取最小，max，取最大
            ZParams params = new ZParams().aggregate(ZParams.Aggregate.MAX);
            // 操作set和zset，聚合方法，聚合结果放在key中
            conn.zinterstore(key, params, "group:" + group, order);
            // 设置60秒有效时间
            conn.expire(key, 60);
        }
        B b = new B();
        return b.getArticles(conn, page, order);
    }

}
