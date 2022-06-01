package org.j2server.j2cache;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import java.util.Set;

import org.j2server.j2cache.cache.CacheManager;
import org.j2server.j2cache.cache.ICache;
import org.j2server.j2cache.cache.redis.RedisCacheStategy;
import org.j2server.j2cache.entites.DataClass;
import org.j2server.j2cache.entites.KeyClass;
import org.j2server.j2cache.utils.PropsUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.fastjson.JSON;

public class TestRedisCache {
	public TestRedisCache() {
	}

	@Before
	public void setUp() throws Exception {
		//jedis pool
		PropsUtils.setCacheStrategyClass(RedisCacheStategy.class.getName());
		PropsUtils.setRedisHost("127.0.0.1");
		PropsUtils.setRedisPort(6379);

		//sentinels pool
//		PropsUtils.setCacheStrategyClass(RedisCacheStategy.class.getName());
//		PropsUtils.setPoolModule(PropsUtils.JEDIS_SENTINEL_POOL);
//		PropsUtils.setRedisHost("10.20.28.1:26380,10.20.28.2:26380,10.20.28.3:26380");
//		PropsUtils.setRedisMasterName("mymaster");
//		PropsUtils.setRedisPassword("default@1");
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testCacheObject() {
		ICache<String, DataClass> cache = CacheManager.getOrCreateCache("redisCacheObject", String.class, DataClass.class);
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
		ICache<String, DataClass> cache = CacheManager.getOrCreateCache("redisCache", String.class, DataClass.class);
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
		ICache<String, String> cache = CacheManager.getOrCreateCache("redisCacheExpire", 
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
			CacheManager.destroyCache(cache.getName());
		}
	}
	
	@Test
	public void testObjectKeySet() {
		ICache<KeyClass, String> cache = CacheManager.getOrCreateCache("redisCacheObjectKeySet", KeyClass.class, String.class);
		int size = 5;
		for (int i = 0; i < size; i++) {
			cache.put(new KeyClass("testKey" + i), "testValue" + i);
		}
		
		try {
			Set<KeyClass> keySets = cache.keySet();
			Assert.assertEquals(size, keySets == null ? 0 : keySets.size());
			int randomIdx = new Random().nextInt(size);
			String randomKey = "testKey"+randomIdx;
			Assert.assertTrue(keySets.stream().filter(e -> randomKey.equals(e.getKeyName())).findAny().isPresent());
		} finally {
			CacheManager.destroyCache(cache.getName());
		}
	}
	
	@Test
	public void testStringKeySet() {
		ICache<String, String> cache = CacheManager.getOrCreateCache("redisCacheStringKeySet", String.class, String.class);
		int size = 5;
		for (int i = 0; i < size; i++) {
			cache.put("testKey" + i, "testValue" + i);
		}
		
		try {
			Set<String> keySets = cache.keySet();
			Assert.assertEquals(size, keySets == null ? 0 : keySets.size());
			int randomIdx = new Random().nextInt(size);
			String randomKey = "testKey"+randomIdx;
			Assert.assertTrue(keySets.stream().filter(e -> randomKey.equals(e)).findAny().isPresent());
		} finally {
			CacheManager.destroyCache(cache.getName());
		}
	}
	
	@Test
	public void testClear() {
		ICache<String, String> cache = CacheManager.getOrCreateCache("redisCacheClear", String.class, String.class);
		Integer cnt = 100;
		for (int i = 0; i < cnt; i++) {
			cache.put(Integer.toString(i), "value"+i);
		}
		
		CacheManager.destroyCache(cache.getName());
		Assert.assertTrue(0 == cache.size());
	}
}
