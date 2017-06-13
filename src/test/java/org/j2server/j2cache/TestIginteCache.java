package org.j2server.j2cache;

import org.j2server.j2cache.cache.CacheManager;
import org.j2server.j2cache.cache.ICache;
import org.j2server.j2cache.cache.iginte.IgniteCacheStategy;
import org.j2server.j2cache.cache.redis.RedisCacheStategy;
import org.j2server.j2cache.entites.DataClass;
import org.j2server.j2cache.utils.PropsUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestIginteCache {

	public TestIginteCache() {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		PropsUtils.setCacheStrategyClass(IgniteCacheStategy.class.getName());
		ICache<String, DataClass> cache = CacheManager.getOrCreateCache("igniteCache", String.class, DataClass.class);
		
		Integer cnt = 1000000;
		System.out.println("开始测试写入缓存" + cache.getName());
		long begin = System.currentTimeMillis();
		for (int i = 0; i < cnt; i++) {
			DataClass dc = new DataClass();
			dc.setName(Integer.toString(i));
			dc.setValue(i);
			dc.setStrValue("asdfadsfasfda");
			cache.put(Integer.toString(i), dc);
		}
		long end = System.currentTimeMillis();		
		System.out.println("总共耗时：" + (end - begin));
		System.out.println("每毫秒写入:"+cnt/(end - begin)+"条。");  
        System.out.println("每秒写入:"+(cnt/(end - begin))*1000+"条。"); 
        
        System.out.println("开始测试读取缓存" + cache.getName());
        begin = System.currentTimeMillis();
		for (int i = 0; i < cnt; i++) {
			DataClass dc = cache.get(Integer.toString(i));
		}
		end = System.currentTimeMillis();		
		System.out.println("读取总共耗时：" + (end - begin));
		System.out.println("每毫秒读取:"+cnt/(end - begin)+"条。");  
        System.out.println("每秒读取:"+(cnt/(end - begin))*1000+"条。");     
	}
	
	@Test
	public void testExprie() {
		PropsUtils.setCacheStrategyClass(IgniteCacheStategy.class.getName());
		ICache<String, String> cache = CacheManager.getOrCreateCache("igniteCache", String.class, String.class, 0, 3000l);
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
	}
	
}
