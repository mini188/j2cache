package org.j2server.j2cache;

import java.util.Random;

import org.j2server.j2cache.cache.CacheManager;
import org.j2server.j2cache.cache.ICache;
import org.j2server.j2cache.entites.DataClass;
import org.j2server.j2cache.entites.DataClassNormal;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
	public void testWriteAndRead() {
		ICache<String, DataClass> cache = CacheManager.getOrCreateCache("jvmCache-testWriteAndRead", String.class, DataClass.class);
		
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
		ICache<String, String> cache = CacheManager.getOrCreateCache("jvmCache-testExprie", String.class, DataClass.class, 0, 2000l);
		cache.put("a", "bb");
		
		System.out.println("try get cache:" + cache.get("a"));
		
		try {
			Thread.sleep(1900);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("try get exprie cache1:" + cache.get("a"));	
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("try get exprie cache2:" + cache.get("a"));
		try {
		    cache.put("a", "bb");
        } catch (Exception e) {
        }
		System.out.println("try set cache again:" + cache.get("a"));
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
	public void testThreadSafe() {
		ICache<Integer, DataClassNormal> cache = CacheManager.getOrCreateCache("testThreadSafe", Integer.class, DataClassNormal.class, 0, 5000l);
		
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
}
