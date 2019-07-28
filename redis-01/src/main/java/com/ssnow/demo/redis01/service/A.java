package com.ssnow.demo.redis01.service;

import com.ssnow.demo.redis01.constant.ArticleCons;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName A
 * @Desc 文章的评分以及发布功能
 * @Author zhaoteng
 * @Date 2019/7/28 15:49
 * @Version 1.0
 * @DESC
 *  第一个功能：
 *      文章投票
 *          1. 保存文章信息
 *          2. 对文章进行投票
 *              投票规则：
 *                  1） 每个人对每个文章最多投一次。
 *                  2） 发布之后的一个周是投票时间
 *          3. 对文章进行评分
 *              评分算法：
 *                  票数 * 423 + 文章发布时间
 *      Redis数据结构设计：
 *          PS:变量用{}表示
 *          1. 文章存储，Hash散列，   名称：article:{文章ID}，包含的key：title、link、poster、time、votes（票数）
 *          2. 发布时间存储，ZSet有序集合，名称：time: ， key的名称规则：article:{文章ID}
 *          3. 文章评分存储，ZSet有序集合，名称：score:， key的名称规则：同上
 *          4. 投票人存储，Set，名称：voted:{文章ID}，key的名称规则，user:{用户ID}
 *
 *
 **/
@Slf4j
public class A {

    /**
     * 发布文章
     * @param conn redis连接
     * @param user 发布用户
     * @param title 文章标题
     * @param link 文章连接
     * @return 文章id
     * @逻辑
     *   创建文章ID
     *   在hash中存储文章信息
     *   将发布者添加到以投票用户里，并设置一个周的过期时间
     *   将文章初始评分 和 发布时间存储到ZSet
     */
    public String postArticle(Jedis conn, String user, String title, String link) {
        String articleId = String.valueOf(conn.incr(ArticleCons.ARTICLE_INCR));
        // 名称封装
        String article = ArticleCons.ARTICLE_INCR + articleId;
        String voted = "voted:" + articleId;
        // 发布时间
        long now = System.currentTimeMillis() / 1000;
        // 封装文章的散列信息
        Map<String, String> map = new HashMap<>();
        map.put("title", title);
        map.put("link", link);
        map.put("user", user);
        map.put("now", String.valueOf(now));
        map.put("votes", "1");
        // 保存文章
        conn.hmset(article, map);
        // 将用户添加到已投票数据
        conn.sadd(voted, user);
        // 设置过期时间
        conn.expire(voted, ArticleCons.ONE_WEEK_IN_SECONDS);
        // 保存文章初始评分 和 发布时间
        conn.zadd(ArticleCons.Z_SCORE, now + ArticleCons.SCORE_VOTE, article);
        conn.zadd(ArticleCons.Z_TIME, now, article);

        return articleId;
    }

    /**
     * 文章投票
     * @param conn redis连接
     * @param user 用户
     * @param article 文章
     * @DESC
     *  从基数上讲，要正确的实现投票功能，需要将方法中所有的操作放在一个事务中执行，
     *  不过关于事务之后会说，现在可以不考虑
     */
    public void articleVote(Jedis conn, String user, String article) {
        // 1. 判断投票时间
        // 当前时间（s） - 一周秒数 = 当前时间对应最晚的发布时间
        // 如果发布时间在这个时间之前，就不允许点赞了
        long cutoff = (System.currentTimeMillis() / 1000) - ArticleCons.ONE_WEEK_IN_SECONDS;
        if (conn.zscore(ArticleCons.Z_TIME, article) < cutoff) {
            log.error("文章发布时间超过一周，投票失败！");
            return;
        }
        // 2. 判断投票的用户
        // 每个用户对每个文章只能投票一次
        // conn.sismember()||根据sadd的返回值
        String articleId = article.substring(article.indexOf(":") + 1);
        if (!conn.sismember("voted:" + articleId, user)) {
        // if (conn.sadd("voted:" + articleId, user) == 1) {
            // 票数增加
            conn.hincrBy(article, "votes", 1);
            // 评分增加
            conn.zincrby(ArticleCons.Z_SCORE, ArticleCons.SCORE_VOTE, article);
        }
    }

}
