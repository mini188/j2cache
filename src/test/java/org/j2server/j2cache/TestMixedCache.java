package org.j2server.j2cache;

import org.j2server.j2cache.cache.CacheManager;
import org.j2server.j2cache.cache.ICache;
import org.j2server.j2cache.cache.guava.GuavaCacheStategy;
import org.j2server.j2cache.cache.redis.RedisCacheStategy;
import org.j2server.j2cache.entites.DataClass;
import org.j2server.j2cache.utils.PropsUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestMixedCache {
	public TestMixedCache() {
	}

	@Before
	public void setUp(){
	}

	@After
	public void tearDown() {
	}
	
	@Test
	public void testCacheObject() {
		ICache<String, DataClass> jvmcache = CacheManager.getOrCreateCache("jvmCacheObject", String.class, DataClass.class);
		
		PropsUtils.setCacheStrategyClass(RedisCacheStategy.class.getName());
		PropsUtils.setRedisHost("127.0.0.1");
		PropsUtils.setRedisPort(6379);
		ICache<String, DataClass> rediscache = CacheManager.getOrCreateCache("redisCacheObject", String.class, DataClass.class);
		
		PropsUtils.setCacheStrategyClass(GuavaCacheStategy.class.getName());
		ICache<String, DataClass> guavacache = CacheManager.getOrCreateCache("guavaCacheObject", String.class, DataClass.class);

		
		cacheTest("jvmCacheObject", jvmcache);
		cacheTest("redisCacheObject", rediscache);
		cacheTest("guavaCacheObject", guavacache);
	}
	
	private void cacheTest(String cacheName, ICache<String, DataClass> cacheObject) {
		try {
			Assert.assertEquals(cacheName, cacheObject.getName());
			
			String key = "objectKey";
			DataClass obj = new DataClass();
			obj.setName("data-1");
			obj.setStrValue("test str");
			obj.setValue(100L);
			cacheObject.put(key, obj);
			
			DataClass cacheObj = cacheObject.get(key);
			Assert.assertNotNull(cacheObj);
			Assert.assertEquals(obj.getName(), cacheObj.getName());
			Assert.assertEquals(obj.getStrValue(), cacheObj.getStrValue());
			Assert.assertEquals(obj.getValue(), cacheObj.getValue());
		} finally {
			CacheManager.destroyCache(cacheObject.getName());
		}
	}

}
