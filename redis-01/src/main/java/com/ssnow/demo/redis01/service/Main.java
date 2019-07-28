package com.ssnow.demo.redis01.service;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;

/**
 * @ClassName Main
 * @Desc TODO
 * @Author zhaoteng
 * @Date 2019/7/28 18:19
 * @Version 1.0
 **/
@Slf4j
public class Main {

    private void run(A a, B b, C c) {

        Jedis conn = new Jedis("39.105.156.77", 6379);
        conn.auth("211314Pijiu");
        conn.select(15);
        // 发布文章
        String articleId = a.postArticle(
                conn, "xiaoZhao", "hello redis", "http://www.baidu.com");
        log.info("### 我们发布了一篇新的文章,ID: {}", articleId);
        log.info("### 内容如下：####:");
        Map<String, String> articleData = conn.hgetAll("article:" + articleId);
        for (Map.Entry<String, String> entry : articleData.entrySet()) {
            log.info("{} : {}", entry.getKey(), entry.getValue());
        }

        System.out.println();
        // 文章投票
        a.articleVote(conn, "热心网友1号", "article:" + articleId);
        String votes = conn.hget("article:" + articleId, "votes");
        log.info("### 文章已经被投过票了，现在的票数是：{}", votes);
        assert Integer.parseInt(votes) > 1;

        log.info("### 现在看一下文章的分数排名：");
        List<Map<String, String>> articles = b.getArticles(conn, 1, "score:");
        printArticles(articles);
        assert articles.size() >= 1;

        c.addGroups(conn, articleId, new String[]{"xinXinGroup"});
        log.info("### 我们添加了文章到一个组中，让我们看一下这个组中所有的文章: ###");
        articles = c.getGroupArticles(conn, "xinXinGroup", 1, "score:");
        printArticles(articles);
        assert articles.size() >= 1;
    }

    private void printArticles(List<Map<String,String>> articles){
        for (Map<String,String> article : articles){
            log.info("  ===id: {} ===", article.get("id"));
            for (Map.Entry<String,String> entry : article.entrySet()){
                if (entry.getKey().equals("id")){
                    continue;
                }
                log.info(" ==={} : {}===   ", entry.getKey(), entry.getValue());
            }
        }
    }

    public static void main(String[] args) {
        new Main().run(new A(), new B(), new C());
    }
}
