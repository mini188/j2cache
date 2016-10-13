package org.j2server.j2cache;

import org.j2server.j2cache.cache.CacheManager;
import org.j2server.j2cache.cache.ICache;
import org.j2server.j2cache.cache.hazelcast.HazelcastCacheStategy;
import org.j2server.j2cache.entites.DataClass;
import org.j2server.j2cache.utils.PropsUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestHazelcastCache {
	public TestHazelcastCache() {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void test() {
		PropsUtils.setCacheStrategyClass(HazelcastCacheStategy.class.getName());
		ICache<String, DataClass> hazelcasthCache = CacheManager.getOrCreateCache("hazelcasthCache", String.class, DataClass.class);
		
		System.out.println("开始测试写入缓存" + hazelcasthCache.getName());
		long begin = System.currentTimeMillis();
		for (int i = 0; i < 1000000; i++) {
			DataClass dc = new DataClass();
			dc.setName(Integer.toString(i));
			dc.setValue(i);
			dc.setStrValue("asdfadsfasfda");
			hazelcasthCache.put(Integer.toString(i), dc);
		}
		long end = System.currentTimeMillis();		
		System.out.println("总共耗时：" + (end - begin));
		System.out.println("每毫秒写入:"+1000000/(end - begin)+"条。");  
        System.out.println("每秒写入:"+(1000000/(end - begin))*1000+"条。"); 
	}
}
