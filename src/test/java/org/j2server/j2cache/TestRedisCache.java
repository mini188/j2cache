package org.j2server.j2cache;

import java.util.Arrays;

import org.j2server.j2cache.cache.CacheManager;
import org.j2server.j2cache.cache.ICache;
import org.j2server.j2cache.cache.redis.RedisCacheStategy;
import org.j2server.j2cache.entites.DataClass;
import org.j2server.j2cache.utils.PropsUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.fastjson.JSON;

public class TestRedisCache {

	public TestRedisCache() {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		PropsUtils.setCacheStrategyClass(RedisCacheStategy.class.getName());
		PropsUtils.setRedisHost("10.20.29.189");
		PropsUtils.setRedisPort(6379);
		PropsUtils.setRedisPassword("zrtredis");
		ICache<String, DataClass> cache = CacheManager.getOrCreateCache("redisCache", String.class, DataClass.class);
		
		Integer cnt = 10000;
		System.out.println("开始测试写入缓存" + cache.getName());
		long begin = System.currentTimeMillis();
		long size = 0l;
		char[] chars = new char[1024];
        Arrays.fill(chars, 'x');
        String body = new String(chars);
		for (int i = 0; i < cnt; i++) {
			DataClass dc = new DataClass();
			dc.setName(Integer.toString(i));
			dc.setValue(i);
			dc.setStrValue(body);
			cache.put(Integer.toString(i), dc);
			size += JSON.toJSONString(dc).length();
		}
		long end = System.currentTimeMillis();
		double diff = end - begin;
		System.out.println("总共耗时：" + diff);
		System.out.println(String.format("每毫秒写入:%.2f条。", cnt/diff));  
        System.out.println(String.format("每秒写入:%.2f条。", cnt/diff*1000));
        System.out.println(String.format("每秒写入：%.2f mb", size/1024/1024/diff*1000));
        
        System.out.println("开始测试读取缓存" + cache.getName());
        begin = System.currentTimeMillis();
		for (int i = 0; i < cnt; i++) {
			size += JSON.toJSONString(cache.get(Integer.toString(i))).length();
		}
		end = System.currentTimeMillis();	
		diff = end - begin;
		System.out.println("读取总共耗时：" + diff);
		System.out.println(String.format("每毫秒读取:%.2f条。", cnt/diff));  
        System.out.println(String.format("每秒读取:%.2f条。", cnt/diff*1000));  
        System.out.println(String.format("每秒读取：%.2f mb", size/1024/1024/diff*1000));
        
        CacheManager.destroyCache("redisCache");
	}
	
	@Test
	public void testExprie() {
		PropsUtils.setCacheStrategyClass(RedisCacheStategy.class.getName());
		ICache<String, String> cache = CacheManager.getOrCreateCache("redisCache", String.class, String.class, 0, 1000l);
		cache.put("a", "bb");
		
		System.out.println("try get cache:" + cache.get("a"));
		
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("try get exprie cache1:" + cache.get("a"));	
		try {
			Thread.sleep(900);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("try get exprie cache2:" + cache.get("a"));
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("try get exprie cache3:" + cache.get("a"));		
		try {
			Thread.sleep(1100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("try get exprie cache4:" + cache.get("a"));	
		
		CacheManager.destroyCache("redisCache");
	}

}
