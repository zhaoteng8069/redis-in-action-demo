package com.ssnow.demo.redis01.service;

import com.ssnow.demo.redis01.constant.ArticleCons;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @ClassName B
 * @Desc 获取最新的文章、分数最高的文章
 * @Author zhaoteng
 * @Date 2019/7/28 17:01
 * @Version 1.0
 **/
public class B {

    /**
     * 在指定ZSet中获取指定范围的文章（按照分值从大到小排序）
     * @param conn 连接
     * @param page 第几页
     * @param order 指定ZSet
     * @return 文章集合
     */
    public List<Map<String, String>> getArticles(Jedis conn, int page, String order) {
        // 计算分页起点  终点
        int start = (page - 1) * ArticleCons.ARTICLES_PER_PAGE;
        int end = start + ArticleCons.ARTICLES_PER_PAGE - 1;
        // 按照分值“从大到小”排序，默认是“从小到大”
        Set<String> ids = conn.zrevrange(order, start, end);
        List<Map<String, String>> articles = new ArrayList<Map<String, String>>();
        for (String id : ids) {
            Map<String, String> articleData = conn.hgetAll(id);
            articleData.put("id", id);
            articles.add(articleData);
        }
        return articles;
    }

}
