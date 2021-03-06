package org.j2server.j2cache;

import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.j2server.j2cache.cache.CacheManager;
import org.j2server.j2cache.cache.ICache;
import org.j2server.j2cache.cache.guava.GuavaCacheStategy;
import org.j2server.j2cache.entites.DataClass;
import org.j2server.j2cache.entites.DataClassNormal;
import org.j2server.j2cache.utils.PropsUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestGuavaCache {

	public TestGuavaCache() {
	}

	@Before
	public void setUp() throws Exception {
		PropsUtils.setCacheStrategyClass(GuavaCacheStategy.class.getName());
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testWriteAndRead() {
		ICache<String, DataClass> cache = CacheManager.getOrCreateCache("guavaCache-testWriteAndRead", String.class, DataClass.class);
		
		Integer cnt = 5000000;
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
		assertTrue(String.format("缓存写入异常，预期的size=%d, 实际的size=%d", cnt, cache.size()), cache.size() == cnt);
		
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
	public void testExprie() {
		ICache<String, String> cache = CacheManager.getOrCreateCache("guavaCache-testExprie", String.class, DataClass.class, 0, 2000l);
		
		cache.put("a", "bb");		
		assertTrue("测试读取缓存，读取的缓存值并不是预期值[bb]。", "bb".equals(cache.get("a")));
		try {
			Thread.sleep(1900);
		} catch (InterruptedException e) {
		}
		assertTrue("测试缓存有效期，读取到的缓存值并不是预期值[bb]。", "bb".equals(cache.get("a")));
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		assertTrue("测试读取过期缓存，读取的缓存值并不是预期的[null]。", cache.get("a") == null);
		
		cache.put("a", "new value");
		assertTrue("测试重新写入过期缓存，读取的缓存值不是预期数据[bb].", "new value".equals(cache.get("a")));
	}
	
	//@Test
	public void testThreadSafe() {
		ICache<Integer, DataClassNormal> cache = CacheManager.getOrCreateCache("guavaCache-testThreadSafe", Integer.class, DataClassNormal.class, 0, 5000l);
		
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
	public void testMapProps() {
		long maxSize = 100l;
		ICache<String, DataClass> cache = CacheManager.getOrCreateCache("guavaCache-testMapProps", String.class, DataClass.class, maxSize, 0l);
		
		Integer cnt = 500;
		for (int i = 0; i < cnt; i++) {
			DataClass dc = new DataClass();
			dc.setName(Integer.toString(i));
			dc.setValue(i);
			dc.setStrValue("asdfadsfasfda");
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
		assertTrue(String.format("测试maxSize参数.设置的 [maxSize=%d] 但实际读取的条数为[actual size：%d]", maxSize, readSize), maxSize == readSize);		
		assertTrue(String.format("测试GuavaCache的size方法失败。cache.size=%d，不是预期的条数%d", cache.size(), maxSize), cache.size() == maxSize);
		assertTrue(String.format("测试GuavaCache的isEmpty方法。 isEmpty=%b,不是预期的false", cache.isEmpty()),  false == cache.isEmpty());
	}
	
	@Test
	public void testRemoveCache() {
		ICache<String, String> cache = CacheManager.getOrCreateCache("guavaCache-testMapProps", String.class, DataClass.class);
		
		String singleRemove = "single-remove";
		cache.put(singleRemove, "single-remove-value");
		cache.remove(singleRemove);
		assertTrue("单个缓存remove测试异常，预期应该返回[null]。", null == cache.get(singleRemove));
		
		
		Integer cnt = 500;
		for (int i = 0; i < cnt; i++) {
			cache.put(Integer.toString(i), "value");
		}		
		assertTrue(String.format("批量缓存remove测试数据初始化异常，预期缓存数量=%d，实际数量=%d。", cnt, cache.size()), cache.size() == cnt);
		
		for (int i = 0; i < cnt; i++) {
			cache.remove(Integer.toString(i));
		}		
		assertTrue(String.format("单个缓存remove测试异常，预期应该返回[null]。", cache.isEmpty()),  true == cache.isEmpty());
	}
}
