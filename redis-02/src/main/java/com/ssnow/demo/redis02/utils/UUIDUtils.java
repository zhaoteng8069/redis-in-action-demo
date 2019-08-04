package com.ssnow.demo.redis02.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Set;

/**
 * @ClassName UUIDUtils
 * @Desc TODO
 * @Author zhaoteng
 * @Date 2019/8/4 10:42
 * @Version 1.0
 **/
public class UUIDUtils {

    public static void main(String[] args) throws MalformedURLException {
        String urlStr = "http://www.baidu.com?a=1&b=2&c=3";
        URL url = new URL(urlStr);
        HashMap<String,String> params = new HashMap<String,String>();
        String query = url.getQuery();
        String[] split = query.split("&");
        if (url.getQuery() != null){
            for (String param : split){
                String[] pair = param.split("=", 2);
                params.put(pair[0], pair.length == 2 ? pair[1] : null);
            }
        }
        Set<String> strings = params.keySet();
        for (String key : strings) {
            System.out.println(params.get(key));
        }
    }

}
