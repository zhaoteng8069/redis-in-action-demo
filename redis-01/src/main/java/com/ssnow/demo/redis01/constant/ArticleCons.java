package com.ssnow.demo.redis01.constant;

/**
 * @ClassName ArticleCons
 * @Desc 常量定义
 * @Author zhaoteng
 * @Date 2019/7/28 17:02
 * @Version 1.0
 **/
public class ArticleCons {
    /** 一周的秒数 */
    public static final int ONE_WEEK_IN_SECONDS = 7 * 86400;

    /**每票得分*/
    public static final int SCORE_VOTE = 423;

    /**用于文章id自增的key*/
    public static final String ARTICLE_INCR = "article:";

    /**分布时间有序集合*/
    public static final String Z_TIME = "time:";

    /**评分有序集合*/
    public static final String Z_SCORE = "score:";

    /**分页参数*/
    public static final int ARTICLES_PER_PAGE = 25;

}
