package org.j2server.j2cache.utils;

import org.j2server.j2cache.cache.jvm.DefaultCacheStategy;




/**
 * 在不使用spring的情况下，属性文件加载。应用中可以仿此类，写一个PropsUtils
 * 工具类，需要更改的是类中PropertiesLoader的构造函数参数。
 */
public class PropsUtils {
	private static String cacheStrategyClass = DefaultCacheStategy.class.getName();
	private static String redisHost= "127.0.0.1";

	private static Integer redisPort = 6379;
	private static String redisPwd = "";

	/**
	 * 获取获取的最大长度
	 * @return
	 */
	public static long getCacheMaxSize() {
		return 0l;
	}
	
	public static long getCacheMaxLifeTime() {
		return 0l;
	}
	
	public static String getCacheStrategyClass() {
		return cacheStrategyClass;
	}
	
	public static void setCacheStrategyClass(String stategy) {
		cacheStrategyClass = stategy;
	}
	
	public static String getRedisHost() {
		return redisHost;
	}
	
	public static int getRedisPort() {
		return redisPort;
	}
	
	public static String getRedisPassword() {
		return redisPwd;
	}

	public static void setRedisPassword(String redisPwd) {
		PropsUtils.redisPwd = redisPwd;
	}

	public static void setRedisHost(String redisHost) {
		PropsUtils.redisHost = redisHost;
	}

	public static void setRedisPort(Integer redisPort) {
		PropsUtils.redisPort = redisPort;
	}
}
