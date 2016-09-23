package org.j2server.j2cache;

import redis.clients.jedis.Jedis;

public class redis {
	public static void main(String[] args) {
		String value = null;
		Integer cnt = 100000;
		Jedis jedis = new Jedis("localhost",6379);
		
		
		long startTime=System.currentTimeMillis();   //获取开始时间
		
		System.out.println("开始写入测试");  
		for(int i=0;i<cnt;i++){
			jedis.set("foo"+i, "bar"+i);
		}		
		long endTime=System.currentTimeMillis(); //获取结束时间
		System.out.println("程序运行时间： "+(endTime-startTime)+"ms");  
		System.out.println("每毫秒写入:"+cnt/(endTime-startTime)+"条。");
		System.out.println("每秒写入:"+(cnt/(endTime-startTime))*1000+"条。");
		
		/*jedis.lpush("foo", "bar1");
		jedis.lpush("foo", "bar2");
		jedis.lpush("foo", "bar3");*/
		System.out.println("开始读取测试");  
		startTime=System.currentTimeMillis();   //获取开始时间
		for(int i=0;i<cnt;i++){
			value = jedis.get("foo"+i);
			//System.out.println("value:"+value);
		}		
		endTime=System.currentTimeMillis(); //获取结束时间
		System.out.println("程序运行时间： "+(endTime-startTime)+"ms");  
		System.out.println("每毫秒读取:"+cnt/(endTime-startTime)+"条。");
		System.out.println("每秒读取:"+(cnt/(endTime-startTime))*1000+"条。");

		value = jedis.get("foo");
		System.out.println("value:"+value);
	}
}
