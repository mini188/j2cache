package org.j2server.j2cache;

import com.alibaba.fastjson.JSON;
import org.j2server.j2cache.cache.CacheManager;
import org.j2server.j2cache.cache.ICache;
import org.j2server.j2cache.cache.redis.RedisCacheStategy;
import org.j2server.j2cache.entites.DataClass;
import org.j2server.j2cache.entites.GenericDataClass;
import org.j2server.j2cache.entites.GenericKeyClass;
import org.j2server.j2cache.entites.KeyClass;
import org.j2server.j2cache.utils.PropsUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class TestTongRDSCache {
	public TestTongRDSCache() {
	}

	@Before
	public void setUp() {
		//jedis pool
		PropsUtils.setCacheStrategyClass(RedisCacheStategy.class.getName());
		PropsUtils.setRedisHost("10.18.1.209");
		PropsUtils.setRedisPort(6379);
		PropsUtils.setRedisKeyPrefix("j2cache");
		PropsUtils.setRedisPassword("hundsun@1");

		//sentinels pool
//		PropsUtils.setCacheStrategyClass(RedisCacheStategy.class.getName());
//		PropsUtils.setPoolModule(PropsUtils.JEDIS_SENTINEL_POOL);
//		PropsUtils.setRedisHost("10.20.28.1:26380,10.20.28.2:26380,10.20.28.3:26380");
//		PropsUtils.setRedisMasterName("mymaster");
//		PropsUtils.setRedisPassword("default@1");
	}

	@After
	public void tearDown() {
	}
	
	@Test
	public void testCacheObject() {
		ICache<String, DataClass> cache = CacheManager.getOrCreateCache("tongRDSCacheObject", String.class, DataClass.class);
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
	public void testGenericTypeCacheObject() {
		ICache<String, GenericDataClass<DataClass>> cache = CacheManager.getOrCreateCache("genericTypeCacheObject",
				String.class,
				GenericDataClass.class);
		try {
			GenericDataClass<DataClass> obj = buildGenericData();
			String key = "objectKey";
			cache.put(key, obj);

			GenericDataClass<DataClass> cacheObj = cache.get(key);
			Assert.assertNotNull(cacheObj);
			Assert.assertEquals(obj.getName(), cacheObj.getName());
			Assert.assertEquals(obj.getList().size(), cacheObj.getList().size());
			Assert.assertEquals(obj.getList().get(0).getStrValue(), cacheObj.getList().get(0).getStrValue());
		} finally {
			CacheManager.destroyCache(cache.getName());
		}
	}

	private GenericDataClass<DataClass> buildGenericData() {
		List<DataClass> lists = new ArrayList<>();
		DataClass data1 = new DataClass();
		data1.setName("data-1");
		data1.setStrValue("test str");
		data1.setValue(100L);
		lists.add(data1);

		DataClass data2 = new DataClass();
		data2.setName("data-1");
		data2.setStrValue("test str");
		data2.setValue(100L);
		lists.add(data2);


		GenericDataClass<DataClass> obj = new GenericDataClass<>();
		obj.setName("generic data object");
		obj.setList(lists);
		return obj;
	}

	@Test
	public void testListCacheObject() {
		ICache<String, List<DataClass>> cache = CacheManager.getOrCreateCache("listCacheObject",
				String.class,
				List.class);
		try {
			String key = "list-object-Key";

			List<DataClass> lists = new ArrayList<>();
			DataClass data1 = new DataClass();
			data1.setName("data-1");
			data1.setStrValue("test str");
			data1.setValue(100L);
			lists.add(data1);

			DataClass data2 = new DataClass();
			data2.setName("data-1");
			data2.setStrValue("test str");
			data2.setValue(100L);
			lists.add(data2);

			System.out.println(JSON.toJSONString(lists));
			cache.put(key, lists);

			List<DataClass> cacheObj = cache.get(key);
			Assert.assertNotNull(cacheObj);
			Assert.assertEquals(lists.size(), cacheObj.size());
			Assert.assertEquals(lists.get(0).getValue(), cacheObj.get(0).getValue());
		} finally {
			CacheManager.destroyCache(cache.getName());
		}
	}

	@Test
	public void testReadAndWrite() {
		ICache<String, DataClass> cache = CacheManager.getOrCreateCache("tongRDSCache", String.class, DataClass.class);
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
		ICache<String, String> cache = CacheManager.getOrCreateCache("tongRDSCacheExpire",
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
	public void testValues() {
		ICache<Object, String> cache = CacheManager.getOrCreateCache("tongRDSCacheValues", Object.class, String.class);
		int size = 5;
		for (int i = 0; i < size; i++) {
			cache.put("testKey" + i, "testValue" + i);
		}

		try {
			Collection<String> values = cache.values();
			Assert.assertEquals(size, values.size());
			int randomIdx = new Random().nextInt(size);
			Assert.assertTrue(values.contains("testValue"+randomIdx));
		} finally {
			CacheManager.destroyCache(cache.getName());
		}
	}
	
	@Test
	public void testObjectKeySet() {
		ICache<KeyClass, String> cache = CacheManager.getOrCreateCache("tongRDSCacheObjectKeySet", KeyClass.class, String.class);
		int size = 5;
		for (int i = 0; i < size; i++) {
			cache.put(new KeyClass("testKey" + i), "testValue" + i);
		}
		
		try {
			Set<KeyClass> keySets = cache.keySet();
			Assert.assertEquals(size, keySets == null ? 0 : keySets.size());
			int randomIdx = new Random().nextInt(size);
			String randomKey = "testKey"+randomIdx;
			Assert.assertTrue(keySets.stream().anyMatch(e -> randomKey.equals(e.getKeyName())));
		} finally {
			CacheManager.destroyCache(cache.getName());
		}
	}

	@Test
	public void testGenericKeySet() {
		ICache<GenericKeyClass<KeyClass>, String> cache = CacheManager.getOrCreateCache("tongRDSCacheGenericKeySet", GenericKeyClass.class, String.class);
		int size = 5;

		List<KeyClass> lists = new ArrayList<>();
		KeyClass key1 = new KeyClass();
		key1.setKeyName("generic-key-1");
		lists.add(key1);

		KeyClass key2 = new KeyClass();
		key2.setKeyName("generic-key-2");
		lists.add(key2);

		for (int i = 0; i < size; i++) {
			GenericKeyClass<KeyClass> keyObjs = new GenericKeyClass<>();
			keyObjs.setName("genericKey" + i);
			keyObjs.setList(lists);

			cache.put(keyObjs, "testValue" + i);
		}

		try {
			Set<GenericKeyClass<KeyClass>> keySets = cache.keySet();
			Assert.assertEquals(size, keySets == null ? 0 : keySets.size());
			int randomIdx = new Random().nextInt(size);
			String randomKey = "genericKey"+randomIdx;
			Assert.assertTrue(keySets.stream().anyMatch(e -> randomKey.equals(e.getName())));
		} finally {
			CacheManager.destroyCache(cache.getName());
		}
	}
	
	@Test
	public void testStringKeySet() {
		ICache<String, String> cache = CacheManager.getOrCreateCache("tongRDSCacheStringKeySet", String.class, String.class);
		int size = 5;
		for (int i = 0; i < size; i++) {
			cache.put("testKey" + i, "testValue" + i);
		}
		
		try {
			Set<String> keySets = cache.keySet();
			Assert.assertEquals(size, keySets == null ? 0 : keySets.size());
			int randomIdx = new Random().nextInt(size);
			String randomKey = "testKey"+randomIdx;
			Assert.assertTrue(keySets.stream().anyMatch(randomKey::equals));
		} finally {
			CacheManager.destroyCache(cache.getName());
		}
	}
	
	@Test
	public void testClear() {
		ICache<String, String> cache = CacheManager.getOrCreateCache("tongRDSCacheClear", String.class, String.class);
		int cnt = 100;
		for (int i = 0; i < cnt; i++) {
			cache.put(Integer.toString(i), "value"+i);
		}
		
		CacheManager.destroyCache(cache.getName());
		Assert.assertEquals(0, cache.size());
	}
}
