package org.j2server.j2cache;

import java.util.Arrays;

import org.j2server.j2cache.cache.CacheManager;
import org.j2server.j2cache.cache.ICache;
import org.j2server.j2cache.cache.hazelcast.HazelcastCacheStategy;
import org.j2server.j2cache.entites.DataClass;
import org.j2server.j2cache.utils.PropsUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.fastjson.JSON;

public class TestHazelcastCache {
	public TestHazelcastCache() {
	}

	@Before
	public void setUp() {
		PropsUtils.setCacheStrategyClass(HazelcastCacheStategy.class.getName());
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testCacheObject() {
		ICache<String, DataClass> cache = CacheManager.getOrCreateCache("hazelcastCacheObject", String.class, DataClass.class);
		try {
			String key = "objectKey";
			DataClass obj = new DataClass();
			obj.setName("data-1");
			obj.setStrValue("test str");
			obj.setValue(100L);
			cache.put(key, obj);
			
			DataClass cacheObj = cache.get(key);
			Assert.assertNotNull(cacheObj);
			Assert.assertEquals(obj.getName(), cacheObj.getName());
			Assert.assertEquals(obj.getStrValue(), cacheObj.getStrValue());
			Assert.assertEquals(obj.getValue(), cacheObj.getValue());
		} finally {
			CacheManager.destroyCache(cache.getName());
		}
	}
	
	@Test
	public void testReadAndWrite() {
		ICache<String, DataClass> cache = CacheManager.getOrCreateCache("hazelcastCacheObject", String.class, DataClass.class);
		try {
			int cnt = 10010;
			System.out.println("开始缓存写入测试" + cache.getName());
			long begin = System.currentTimeMillis();
			long size = 0L;
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
			System.out.printf("每毫秒写入:%.2f条。%n", cnt / diff);
			System.out.printf("每秒写入:%.2f条。%n", cnt / diff * 1000);
			System.out.printf("每秒写入：%.2f mb%n", size / 1024 / 1024 / diff * 1000);

			System.out.println("开始缓存读取测试" + cache.getName());
			begin = System.currentTimeMillis();
			for (int i = 0; i < cnt; i++) {
				size += JSON.toJSONString(cache.get(Integer.toString(i))).length();
			}
			end = System.currentTimeMillis();
			diff = end - begin;
			System.out.println("读取总共耗时：" + diff);
			System.out.printf("每毫秒读取:%.2f条。%n", cnt / diff);
			System.out.printf("每秒读取:%.2f条。%n", cnt / diff * 1000);
			System.out.printf("每秒读取：%.2f mb%n", size / 1024 / 1024 / diff * 1000);
		} finally {
			CacheManager.destroyCache(cache.getName());
		}
	}

	@Test
	public void testExpire() {
		long expire = 3000L;
		ICache<String, String> cache = CacheManager.getOrCreateCache("hazelcastCacheExpire", 
				String.class, String.class, 0, expire);
		String key = "testKey";
		String actual = "testValue";
		assert cache != null;
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
		ICache<String, String> cache = CacheManager.getOrCreateCache("hazelcastCacheClear", String.class, String.class);
		int cnt = 100;
		for (int i = 0; i < cnt; i++) {
			cache.put(Integer.toString(i), "value"+i);
		}
		Assert.assertEquals(100, cache.size());
		CacheManager.destroyCache(cache.getName());
		Assert.assertEquals(0, cache.size());
	}
	
}
