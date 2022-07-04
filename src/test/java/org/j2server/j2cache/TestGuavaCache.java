package org.j2server.j2cache;

import java.util.Random;

import org.j2server.j2cache.cache.CacheManager;
import org.j2server.j2cache.cache.ICache;
import org.j2server.j2cache.cache.guava.GuavaCacheStategy;
import org.j2server.j2cache.entites.DataClass;
import org.j2server.j2cache.entites.DataClassNormal;
import org.j2server.j2cache.utils.PropsUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestGuavaCache {

	public TestGuavaCache() {
	}

	@Before
	public void setUp() {
		PropsUtils.setCacheStrategyClass(GuavaCacheStategy.class.getName());
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testWriteAndRead() {
		ICache<String, DataClass> cache = CacheManager.getOrCreateCache("guavaCache-testWriteAndRead", String.class, DataClass.class);
		
		int cnt = 5000000;
		System.out.println("开始测试写入缓存" + cache.getName());
		long begin = System.currentTimeMillis();
		for (int i = 0; i < cnt; i++) {
			DataClass dc = new DataClass();
			dc.setName(Integer.toString(i));
			dc.setValue(i);
			dc.setStrValue("test write and read.");
			cache.put(Integer.toString(i), dc);
		}
		long end = System.currentTimeMillis();
		Assert.assertEquals(String.format("缓存写入异常，预期的size=%d, 实际的size=%d", cnt, cache.size()), cache.size(), cnt);
		
		System.out.println("总共耗时：" + (end - begin));
		System.out.println("每毫秒写入:"+cnt/(end - begin)+"条。");  
        System.out.println("每秒写入:"+(cnt/(end - begin))*1000+"条。"); 
        
        System.out.println("开始测试读取缓存" + cache.getName());
        begin = System.currentTimeMillis();
		for (int i = 0; i < cnt; i++) {
			cache.get(Integer.toString(i));
		}
		end = System.currentTimeMillis();
		System.out.println("读取总共耗时：" + (end - begin));
		System.out.println("每毫秒读取:"+cnt/(end - begin)+"条。");  
        System.out.println("每秒读取:"+(cnt/(end - begin))*1000+"条。");       
	}
	
	@Test
	public void testExpire() {
		long expire = 3000L;
		ICache<String, String> cache = CacheManager.getOrCreateCache("guavaCache-testExpire", String.class, DataClass.class, 0, expire);
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
	public void testCacheObject() {
		ICache<String, DataClass> cache = CacheManager.getOrCreateCache("guavaCacheObject", String.class, DataClass.class);
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
	
	//@Test
	public void testThreadSafe() {
		ICache<Integer, DataClassNormal> cache = CacheManager.getOrCreateCache("guavaCache-testThreadSafe", Integer.class, DataClassNormal.class, 0, 5000L);
		
		System.out.println("put caches.");
		for(int i=0;i< 10; i++) {
			DataClassNormal data = new DataClassNormal();
			data.setName("a"+i);
			data.setStrValue("bb");
			data.setValue(100L);
			assert cache != null;
			cache.put(i, data);
		}
        
        Thread thread2 = new Thread(() -> {
			Random rand =new Random();
			for (;;) {
				Integer randKey = rand.nextInt(10);
				DataClassNormal data = cache.get(randKey);
				if (data == null) {
					System.err.printf("get cache[key=%d, value=null]%n", randKey);
				} else {
					System.out.printf("get cache[key=%d, value=%s]%n", randKey, data.getName());
				}

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					return;
				}
			}
		});
        thread2.start();
        
        try {
            System.out.println("wait 10 seconds.");
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Thread thread1 = new Thread(() -> {
			for(int n=0; n < 10; n++) {
				System.out.println("put caches again. count:" + n);
				for(int i=0; i<10; i++) {
					DataClassNormal data = new DataClassNormal();
					data.setName("a"+i);
					data.setStrValue("bb");
					data.setValue(100L);
					cache.put(i, data);

					DataClassNormal nData = cache.get(i);
					System.err.println("new put:"+ (nData == null ? " - ": nData.getName()));
				}
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
				}
			}
		});
        System.out.println("new thread to put caches again.");
        thread1.start();
        
        int cnt = 0;
        for(;;) {
            try {
    			Thread.sleep(1000);
    			if (cnt == 20) {
    				return;
    			}
    			cnt++;
    		} catch (InterruptedException e) {
    			e.printStackTrace();
    		}        	
        }
	}
	
	@Test
	public void testMapProps() {
		long maxSize = 100L;
		ICache<String, DataClass> cache = CacheManager.getOrCreateCache("guavaCache-testMapProps", String.class, DataClass.class, maxSize, 0L);
		
		int cnt = 500;
		for (int i = 0; i < cnt; i++) {
			DataClass dc = new DataClass();
			dc.setName(Integer.toString(i));
			dc.setValue(i);
			dc.setStrValue("test map prop");
			assert cache != null;
			cache.put(Integer.toString(i), dc);
		}
		
		long readSize = 0;
		for (int i = 0; i < cnt; i++) {
			DataClass dc = cache.get(Integer.toString(i));
			if (dc == null) {
				continue;
			}
			readSize++;
		}
		Assert.assertEquals(String.format("测试maxSize参数.设置的 [maxSize=%d] 但实际读取的条数为[actual size：%d]", maxSize, readSize), maxSize, readSize);
		Assert.assertEquals(String.format("测试GuavaCache的size方法失败。cache.size=%d，不是预期的条数%d", cache.size(), maxSize), cache.size(), maxSize);
		Assert.assertFalse(String.format("测试GuavaCache的isEmpty方法。 isEmpty=%b,不是预期的false", cache.isEmpty()), cache.isEmpty());
	}
	
	@Test
	public void testRemoveCache() {
		ICache<String, String> cache = CacheManager.getOrCreateCache("guavaCache-testRemoveCache", String.class, DataClass.class);
		
		String singleRemove = "single-remove";
		cache.put(singleRemove, "single-remove-value");
		cache.remove(singleRemove);
		Assert.assertNull("单个缓存remove测试异常，预期应该返回[null]。", cache.get(singleRemove));
		
		
		int cnt = 500;
		for (int i = 0; i < cnt; i++) {
			cache.put(Integer.toString(i), "value");
		}
		Assert.assertEquals(String.format("批量缓存remove测试数据初始化异常，预期缓存数量=%d，实际数量=%d。", cnt, cache.size()), cache.size(), cnt);
		
		for (int i = 0; i < cnt; i++) {
			cache.remove(Integer.toString(i));
		}
		Assert.assertTrue(String.format("单个缓存remove测试异常，预期应该返回[null]。", cache.isEmpty()), cache.isEmpty());
	}
	
	@Test
	public void testClear() {
		ICache<String, String> cache = CacheManager.getOrCreateCache("guavaCache-cacheClear", String.class, String.class);
		int cnt = 100;
		for (int i = 0; i < cnt; i++) {
			cache.put(Integer.toString(i), "value"+i);
		}
		
		CacheManager.destroyCache(cache.getName());
		Assert.assertEquals(0, cache.size());
	}
}
