package org.j2server.j2cache;

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
	public void test() {
		ICache<String, DataClass> cache = CacheManager.getOrCreateCache("jvmCache", String.class, DataClass.class);
		
		Integer cnt = 1000000;
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
		ICache<String, String> cache = CacheManager.getOrCreateCache("jvmCache", String.class, DataClass.class, 0, 2000l);
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
            // TODO: handle exception
        }
		System.out.println("try set cache again:" + cache.get("a"));
	}
	
	@Test
	public void testDataClassNormal() {
		ICache<String, DataClassNormal> cache = CacheManager.getOrCreateCache("jvmCache", String.class, DataClassNormal.class, 0, 2000l);
		
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
}
