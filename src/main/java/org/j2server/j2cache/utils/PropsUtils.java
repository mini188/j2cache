package org.j2server.j2cache.utils;

import org.j2server.j2cache.cache.DefaultCacheObjectManager;
import org.j2server.j2cache.cache.jvm.DefaultCacheStategy;
import org.j2server.j2cache.cache.redis.jedis.JedisPoolConfig;




/**
 * 在不使用spring的情况下，属性文件加载。应用中可以仿此类，写一个PropsUtils
 * 工具类，需要更改的是类中PropertiesLoader的构造函数参数。
 */
public class PropsUtils {
	private static String cacheStrategyClass = DefaultCacheStategy.class.getName();
	private static String cacheObjectManagerClass = DefaultCacheObjectManager.class.getName();
	
    public static final String JEDIS_POOL = "jedisPool";
    public static final String JEDIS_SENTINEL_POOL = "jedisSentinelPool";
    public static final String JEDIS_CLUSTER = "jedisCluster";
    
    /**
     * 默认为jedis.pool，可以通过设置为sentinel.pool
     */
    private static String poolModule = JEDIS_POOL;
	private static String redisHost= "127.0.0.1";
	private static Integer redisPort = 6379;
	private static String redisPwd;
	private static String redisMasterName = "";
	private static Integer redisTimeOut;
	private static Integer redisMaxAttempts;
    /**
     * 	缓存 主建前缀
     */
    private static String redisKeyPrefix = "";
	private static JedisPoolConfig jedisPoolConfig;

	public static String getRedisMasterName() {
		return redisMasterName;
	}

	public static void setRedisMasterName(String redisMasterName) {
		PropsUtils.redisMasterName = redisMasterName;
	}

	/**
	 * 获取获取的最大长度
	 * @return cache max size
	 */
	public static long getCacheMaxSize() {
		return 0L;
	}
	
	public static long getCacheMaxLifeTime() {
		return 0L;
	}
	
	public static String getCacheStrategyClass() {
		return cacheStrategyClass;
	}
	
	public static void setCacheStrategyClass(String stately) {
		cacheStrategyClass = stately;
	}
	
	public static String getRedisHost() {
		return redisHost;
	}
	
	public static Integer getRedisPort() {
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

	public static String getCacheObjectManagerClass() {
		return cacheObjectManagerClass;
	}

	public static void setCacheObjectManagerClass(String cacheObjectManagerClass) {
		PropsUtils.cacheObjectManagerClass = cacheObjectManagerClass;
	}

	public static JedisPoolConfig getJedisPoolConfig() {
		return jedisPoolConfig;
	}

	public static void setJedisPoolConfig(JedisPoolConfig jedisPoolConfig) {
		PropsUtils.jedisPoolConfig = jedisPoolConfig;
	}

	public static String getPoolModule() {
		return poolModule;
	}

	public static void setPoolModule(String poolModule) {
		PropsUtils.poolModule = poolModule;
	}

	public static Integer getRedisTimeOut() {
		return redisTimeOut;
	}

	public static void setRedisTimeOut(Integer redisTimeOut) {
		PropsUtils.redisTimeOut = redisTimeOut;
	}

	public static Integer getRedisMaxAttempts() {
		return redisMaxAttempts;
	}

	public static void setRedisMaxAttempts(Integer redisMaxAttempts) {
		PropsUtils.redisMaxAttempts = redisMaxAttempts;
	}

	public static String getRedisKeyPrefix() {
		return redisKeyPrefix;
	}

	public static void setRedisKeyPrefix(String redisKeyPrefix) {
		PropsUtils.redisKeyPrefix = redisKeyPrefix;
	}
}
