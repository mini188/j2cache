package org.j2server.j2cache;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import org.j2server.j2cache.cache.CacheManager;
import org.j2server.j2cache.cache.ICache;
import org.j2server.j2cache.entites.DataClass;
import org.j2server.j2cache.entites.DataClassNormal;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.fastjson.JSON;

public class TestJvmCache {

	public TestJvmCache() {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testCacheObject() {
		ICache<String, DataClass> cache = CacheManager.getOrCreateCache("jvmCache-Object", String.class, DataClass.class);
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
	public void testWriteAndRead() {
		ICache<String, DataClass> cache = CacheManager.getOrCreateCache("jvmCache-testWriteAndRead", String.class, DataClass.class);
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
		ICache<String, String> cache = CacheManager.getOrCreateCache("jvmCache-testExprie", String.class, DataClass.class, 0, expire);
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
	public void testDataClassNormal() {
		ICache<String, DataClassNormal> cache = CacheManager.getOrCreateCache("jvmCache-testDataClassNormal", String.class, DataClassNormal.class, 0, 2000l);
		
		DataClassNormal data = new DataClassNormal();
		data.setName("a");
		data.setStrValue("bb");
		data.setValue(100l);
		cache.put("a", data);
		
		Assert.assertNotNull(cache.get("a"));		
		try {
			Thread.sleep(1900);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Assert.assertNotNull(cache.get("a"));	
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Assert.assertNull(cache.get("a"));
	}
	
	@Test
	public void testValues() {
		ICache<Object, String> cache = CacheManager.getOrCreateCache("jvmCache-testCacheValues", Object.class, String.class);
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
	
	//@Test
	public void testThreadSafe() {
		ICache<Integer, DataClassNormal> cache = CacheManager.getOrCreateCache("jvmCache-testThreadSafe", Integer.class, DataClassNormal.class, 0, 5000l);
		
		System.out.println("put caches.");
		for(int i=0;i< 10; i++) {
			DataClassNormal data = new DataClassNormal();
			data.setName("a"+i);
			data.setStrValue("bb");
			data.setValue(100l);
			cache.put(i, data);
		}
        
        Thread thread2 = new Thread(){
            public void run() {
            	Random rand =new Random();
            	for (;;) {
	            	Integer randKey = rand.nextInt(10);
	            	DataClassNormal data = cache.get(randKey);
	            	if (data == null) {
	                	System.err.println(String.format("get cache[key=%d, value=null]", randKey));
	            	} else {
	            		System.out.println(String.format("get cache[key=%d, value=%s]", randKey, data.getName()));
	            	}
	            	
	            	try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						return;
					}
            	}
            };
        };
        thread2.start();
        
        try {
            System.out.println("wait 10 seconds.");
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Thread thread1 = new Thread(){
            public void run() {
            	for(int n=0; n < 10; n++) {
            		System.out.println("put caches again. count:" + n);
	        		for(int i=0; i<10; i++) {
	        			DataClassNormal data = new DataClassNormal();
		    			data.setName("a"+i);
		    			data.setStrValue("bb");
		    			data.setValue(100l);
		    			cache.put(i, data);
		    			
		    			DataClassNormal nData = cache.get(i);
		    			System.err.println("new put:"+ (nData == null ? " - ": nData.getName()));
		    		}
	            	try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
					}
            	}
            };
        };
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
	public void testClear() {
		ICache<String, String> cache = CacheManager.getOrCreateCache("jvmCache-cacheClear", String.class, String.class);
		Integer cnt = 100;
		for (int i = 0; i < cnt; i++) {
			cache.put(Integer.toString(i), "value"+i);
		}
		
		CacheManager.destroyCache(cache.getName());
		Assert.assertTrue(0 == cache.size());
	}
}
