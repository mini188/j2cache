package org.j2server.j2cache;

import java.util.Arrays;

import org.j2server.j2cache.cache.CacheManager;
import org.j2server.j2cache.cache.ICache;
import org.j2server.j2cache.cache.iginte.IgniteCacheStategy;
import org.j2server.j2cache.entites.DataClass;
import org.j2server.j2cache.utils.PropsUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.fastjson.JSON;

public class TestIginteCache {

	public TestIginteCache() {
	}

	@Before
	public void setUp() throws Exception {
		PropsUtils.setCacheStrategyClass(IgniteCacheStategy.class.getName());
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testCacheObject() {
		ICache<String, DataClass> cache = CacheManager.getOrCreateCache("igniteCacheObject", String.class, DataClass.class);
		try {
			String key = "objectKey";
			DataClass obj = new DataClass();
			obj.setName("data-1");
			obj.setStrValue("test str");
			obj.setValue(100l);
			cache.put(key, obj);
			
			DataClass cacheObj = cache.get(key);
			Assert.assertNotNull(cacheObj);
			Assert.assertTrue(obj.getName().equals(cacheObj.getName()));
			Assert.assertTrue(obj.getStrValue().equals(cacheObj.getStrValue()));
			Assert.assertTrue(obj.getValue() == cacheObj.getValue());
		} finally {
			CacheManager.destroyCache(cache.getName());
		}
	}
	
	@Test
	public void testReadAndWrite() {
		ICache<String, DataClass> cache = CacheManager.getOrCreateCache("igniteCacheObject", String.class, DataClass.class);
		try {
			Integer cnt = 10010;
			System.out.println("开始缓存写入测试" + cache.getName());
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
			System.out.println(String.format("每毫秒写入:%.2f条。", cnt / diff));
			System.out.println(String.format("每秒写入:%.2f条。", cnt / diff * 1000));
			System.out.println(String.format("每秒写入：%.2f mb", size / 1024 / 1024 / diff * 1000));

			System.out.println("开始缓存读取测试" + cache.getName());
			begin = System.currentTimeMillis();
			for (int i = 0; i < cnt; i++) {
				size += JSON.toJSONString(cache.get(Integer.toString(i))).length();
			}
			end = System.currentTimeMillis();
			diff = end - begin;
			System.out.println("读取总共耗时：" + diff);
			System.out.println(String.format("每毫秒读取:%.2f条。", cnt / diff));
			System.out.println(String.format("每秒读取:%.2f条。", cnt / diff * 1000));
			System.out.println(String.format("每秒读取：%.2f mb", size / 1024 / 1024 / diff * 1000));
		} finally {
			CacheManager.destroyCache(cache.getName());
		}
	}

	@Test
	public void testExprie() {
		long expire = 3000L;
		ICache<String, String> cache = CacheManager.getOrCreateCache("igniteCacheExpire", 
				String.class, String.class, 0, expire);
		String key = "testKey";
		String actual = "testValue";
		cache.put(key, actual);
		try {
			long waitTime = 0;
			for (int i = 0; i < 5; i++) {
				waitTime += 1000;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				String v = cache.get(key);
				Assert.assertEquals(String.format("get expire cache after [%d] ms", waitTime),
						waitTime >= expire ? null : actual, v);
			}
		} finally {
			CacheManager.destroyCache(cache.getName());
		}
	}
	
	@Test
	public void testClear() {
		ICache<String, String> cache = CacheManager.getOrCreateCache("igniteCacheClear", String.class, String.class);
		Integer cnt = 100;
		for (int i = 0; i < cnt; i++) {
			cache.put(Integer.toString(i), "value"+i);
		}
		
		CacheManager.destroyCache(cache.getName());
		Assert.assertTrue(0 == cache.size());
	}
	
}
