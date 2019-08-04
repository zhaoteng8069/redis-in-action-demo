package com.ssnow.demo.redis02;

import com.ssnow.demo.redis02.service.CacheCartService;
import com.ssnow.demo.redis02.service.CacheLoginService;
import com.ssnow.demo.redis02.service.CachePageService;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.util.Map;
import java.util.UUID;

/**
 * @ClassName test
 * @Desc TODO
 * @Author zhaoteng
 * @Date 2019/8/4 12:00
 * @Version 1.0
 **/
public class ServiceTest {

    private Jedis conn;

    @Before
    public void init() {
        conn = new Jedis("39.105.156.77");
        conn.auth("211314Pijiu");
        conn.select(15);
    }

    @Test
    public void testCachePage() {
        CacheLoginService cacheLoginService = new CacheLoginService();
        CachePageService cachePageService = new CachePageService();
        System.out.println("\n----- testCacheRequest -----");
        String token = UUID.randomUUID().toString();

        CachePageService.Callback callback = new CachePageService.Callback(){
            public String call(String request){
                return "content for " + request;
            }
        };

        cacheLoginService.updateToken(conn, token, "username", "itemX");
        String url = "http://test.com/?item=itemX";
        System.out.println("We are going to cache a simple request against " + url);
        String result = cachePageService.cacheRequest(conn, url, callback);
        System.out.println("We got initial content:\n" + result);
        System.out.println();

        assert result != null;

        System.out.println("To test that we've cached the request, we'll pass a bad callback");
        String result2 = cachePageService.cacheRequest(conn, url, null);
        System.out.println("We ended up getting the same response!\n" + result2);

        assert result.equals(result2);

        assert !cachePageService.canCache(conn, "http://test.com/");
        assert !cachePageService.canCache(conn, "http://test.com/?item=itemX&_=1234536");
    }

    @Test
    public void testCacheCar() throws InterruptedException {
        CacheLoginService cacheLoginService = new CacheLoginService();
        CacheCartService cartService = new CacheCartService();
        System.out.println("\n----- testShopppingCartCookies -----");
        String token = UUID.randomUUID().toString();

        System.out.println("We'll refresh our session...");
        cacheLoginService.updateToken(conn, token, "username", "itemX");
        System.out.println("And add an item to the shopping cart");
        cartService.addToCart(conn, token, "itemY", 3);
        Map<String,String> r = conn.hgetAll("cart:" + token);
        System.out.println("Our shopping cart currently has:");
        for (Map.Entry<String,String> entry : r.entrySet()){
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }
        System.out.println();

        assert r.size() >= 1;

        System.out.println("Let's clean out our sessions and carts");
        CacheLoginService.CleanSessionThread thread =cacheLoginService.new CleanSessionThread(0);
        thread.start();
        Thread.sleep(1000);
        thread.quit();
        Thread.sleep(2000);
        if (thread.isAlive()){
            throw new RuntimeException("The clean sessions thread is still alive?!?");
        }

        r = conn.hgetAll("cart:" + token);
        System.out.println("Our shopping cart now contains:");
        for (Map.Entry<String,String> entry : r.entrySet()){
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }
        assert r.size() == 0;
    }

    @Test
    public void testCacheCookie() throws InterruptedException {
        CacheLoginService cacheLoginService = new CacheLoginService();
        System.out.println("\n----- testLoginCookies -----");
        String token = UUID.randomUUID().toString();

        cacheLoginService.updateToken(conn, token, "zhangsanfeng", "itemX");
        System.out.println("We just logged-in/updated token: " + token);
        System.out.println("For user: 'zhangsanfeng'");
        System.out.println();

        System.out.println("What username do we get when we look-up that token?");
        String r = cacheLoginService.checkToken(conn, token);
        System.out.println(r);
        System.out.println();
        assert r != null;

        System.out.println("Let's drop the maximum number of cookies to 0 to clean them out");
        System.out.println("We will start a thread to do the cleaning, while we stop it later");

        CacheLoginService.CleanSessionThread thread =cacheLoginService.new CleanSessionThread(0);
        thread.start();
        Thread.sleep(1000);
        thread.quit();
        Thread.sleep(2000);
        if (thread.isAlive()){
            throw new RuntimeException("The clean sessions thread is still alive?!?");
        }

        long s = conn.hlen("login:");
        System.out.println("The current number of sessions still available is: " + s);
        assert s == 0;
    }
}
