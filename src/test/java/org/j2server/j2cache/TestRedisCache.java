package org.j2server.j2cache;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import org.j2server.j2cache.cache.CacheManager;
import org.j2server.j2cache.cache.ICache;
import org.j2server.j2cache.cache.redis.RedisCacheStategy;
import org.j2server.j2cache.entites.DataClass;
import org.j2server.j2cache.utils.PropsUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.fastjson.JSON;

public class TestRedisCache {
	private static final String ASSRT_EXPRIE_MSG = "get exprie cache after [%d] ms";
	
	public TestRedisCache() {
	}

	@Before
	public void setUp() throws Exception {
		PropsUtils.setCacheStrategyClass(RedisCacheStategy.class.getName());
		PropsUtils.setRedisHost("10.220.125.211");
		PropsUtils.setRedisPort(6379);
		PropsUtils.setRedisPassword("j2cache@1");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
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
		System.out.println(String.format("每毫秒写入:%.2f条。", cnt / diff));
		System.out.println(String.format("每秒写入:%.2f条。", cnt / diff * 1000));
		System.out.println(String.format("每秒写入：%.2f mb", size / 1024 / 1024 / diff * 1000));

		System.out.println("开始测试读取缓存" + cache.getName());
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

		CacheManager.destroyCache("redisCache");
	}

	@Test
	public void testExprie() {
		long expire = 3000L;
		ICache<String, String> cache = CacheManager.getOrCreateCache("redisCacheExprie", 
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
				Assert.assertEquals(String.format(ASSRT_EXPRIE_MSG, waitTime),
						waitTime >= expire ? null : actual, v);
			}
		} finally {
			CacheManager.destroyCache("redisCacheExprie");
		}
	}

	@Test
	public void testValues() {
		ICache<Object, String> cache = CacheManager.getOrCreateCache("redisCacheValues", Object.class, String.class);
		int size = 5;
		for (int i = 0; i < size; i++) {
			cache.put("testKey" + i, "testValue" + i);
		}

		try {
			Collection<String> values = cache.values();
			Assert.assertEquals(size, values == null ? 0 : values.size());
			int randomIdx = new Random().nextInt(size);
			Assert.assertTrue(values.contains("testValue"+randomIdx));
		} finally {
			CacheManager.destroyCache("redisCacheValues");
		}
	}
}
