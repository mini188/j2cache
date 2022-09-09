package org.j2server.j2cache.cache;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.NullArgumentException;
import org.j2server.j2cache.utils.PropsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheManager {
	private final static Logger	logger = LoggerFactory.getLogger(CacheManager.class);
	private static final CacheObjectHolder caches = new CacheObjectHolder();

	/**
	 * 获取或创建缓存
	 * @param cacheName  缓存名
	 * @param keyClass   键的类型
	 * @param valueClass 值的类型
	 * @return 缓存对象
	 */
	public static <T extends ICache<?,?>> T  getOrCreateCache(String cacheName, Class<?> keyClass, Class<?> valueClass) {
		return getOrCreateCache(cacheName, keyClass, valueClass, 0L, 0L);
	}


	/**
	 * 获取或创建缓存
	 * @param cacheName  缓存名
	 * @param keyClass   键的类型
	 * @param valueClass 值的类型
	 * @param maxLifetime 过期时间
	 * @return 缓存对象
	 */
	public static <T extends ICache<?,?>> T  getOrCreateCache(String cacheName, Class<?> keyClass, Class<?> valueClass, long maxLifetime) {
		return getOrCreateCache(cacheName, keyClass, valueClass, 0L, maxLifetime);
	}
	
	/**
	 * 获取或创建缓存
	 * @param cacheName  缓存名
	 * @param keyClass   键的类型
	 * @param valueClass 值的类型
	 * @return 缓存对象
	 */
	public static <T extends ICache<?,?>> T  getOrCreateCache(String cacheName, Class<?> keyClass, Class<?> valueClass, long maxSize, long maxLifetime) {
		try {
			return getOrCreateCache(PropsUtils.getCacheStrategyClass(), cacheName, keyClass, valueClass, maxSize, maxLifetime);
		} catch (Exception e) {
			logger.error("create cache error.", e);
			return null;
		}
	}
	
	/**
	 * 可以指定缓存提供者的方式获取或创建缓存
	 * @param stately    缓存策略
	 * @param cacheName  缓存名
	 * @param keyClass   键的类类型
	 * @param valueClass 值的类类型
	 * @return 缓存对象
	 * @throws Exception 如果stately为空时会抛出异常
	 */
	public static <T extends ICache<?,?>> T  getOrCreateCache(
			String stately, String cacheName, Class<?> keyClass, Class<?> valueClass) throws Exception {
		return getOrCreateCache(stately, cacheName, keyClass, valueClass, 0L, 0L);
	}
	
	/**
	 * 可以指定缓存提供者的方式获取或创建缓存
	 * @param stately    缓存策略类
	 * @param cacheName  缓存名
	 * @param keyClass   键的类类型
	 * @param valueClass 值的类类型
	 * @param maxSize    最大缓存大小(0表示不限制)
	 * @return 缓存对象
	 * @throws Exception 如果stately为空时会抛出异常
	 */
	@SuppressWarnings("unchecked")
	public static <T extends ICache<?,?>> T  getOrCreateCache(
			String stately, String cacheName, Class<?> keyClass, Class<?> valueClass,
			long maxSize, long maxLifetime) throws Exception {
		if (stately == null) {
			throw new NullArgumentException("stately");
		}
		
		CacheObject cacheObj = caches.get(cacheName);
	    if (cacheObj != null) {
	        return (T) cacheObj.getCache();
	    }

		synchronized (cacheName.intern()) {
			cacheObj = caches.get(cacheName);
			if (cacheObj != null) {
				return (T) cacheObj.getCache();
			}

			ICacheStrategy strategy = (ICacheStrategy) Class.forName(stately).newInstance();
			T cache = (T) strategy.createCache(cacheName, keyClass, valueClass, maxSize, maxLifetime);
			caches.put(cacheName, new CacheObject(strategy, cache));
			return cache;
		}
	}


	public static void destroyCache(String cacheName) {
		CacheObject cacheObj = caches.remove(cacheName);
		if (cacheObj != null) {
			cacheObj.getStrategy().destroyCache(cacheObj.getCache());
		}		
	}
	
	public static ICache<?,?>[] getAllCaches() {
		List<ICache<?,?>> values = new ArrayList<>();
		for (CacheObject item: caches.getAllCaches()) {
			values.add(item.getCache());
		}
			
		return values.toArray(new ICache<?, ?>[0]);
	}
}
