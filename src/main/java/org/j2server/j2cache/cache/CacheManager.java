package org.j2server.j2cache.cache;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;
import org.j2server.j2cache.utils.PropsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheManager {
	private final static Logger	logger = LoggerFactory.getLogger(CacheManager.class);
	private static CacheObjectHolder caches = new CacheObjectHolder();
	private static ICacheStrategy cacheStrategy; 
	
	private static void InitCacheStrategy() {
		String cacheStrategyClassSetting = PropsUtils.getCacheStrategyClass();
		//加载缓存策略
		if (StringUtils.isNotEmpty(cacheStrategyClassSetting)) {
			try {
				cacheStrategy = (ICacheStrategy) Class.forName(cacheStrategyClassSetting).newInstance();
			} catch (InstantiationException | IllegalAccessException
					| ClassNotFoundException e) {
				logger.error("ChanageCache error", e);
			}
		}
	}	

	public static synchronized <T extends ICache<?,?>> T  getOrCreateCache(String cacheName, Class<?> keyClass, Class<?> valueCalss) {
		return getOrCreateCache(cacheName, keyClass, valueCalss, 0l, 0l);
	}
	
	/**
	 * 获取或创建缓存
	 * @param cacheName  缓存名
	 * @param keyClass   键的类型
	 * @param valueCalss 值的类型
	 * @return 缓存对象
	 * @throws Exception 如果provider为空时会抛出异常
	 */
	@SuppressWarnings("unchecked")
	public static synchronized <T extends ICache<?,?>> T  getOrCreateCache(String cacheName, Class<?> keyClass, Class<?> valueCalss, long maxSize, long maxLifetime) {
		CacheObject cacheObj = caches.get(cacheName);
	    if (cacheObj != null) {
	        return (T) cacheObj.getCache();
	    }
        
	    if (cacheStrategy == null) {
	    	InitCacheStrategy();
	    }
	    
	    T cache = (T) cacheStrategy.createCache(cacheName, keyClass, valueCalss, maxSize, maxLifetime);
	    caches.put(cacheName, new CacheObject(cacheStrategy, cache));
		return cache;
	}
	
	/**
	 * 可以指定缓存提供者的方式获取或创建缓存
	 * @param stategy    缓存策略
	 * @param cacheName  缓存名
	 * @param keyClass   键的类类型
	 * @param valueCalss 值的类类型
	 * @return 缓存对象
	 * @throws Exception 如果stategy为空时会抛出异常
	 */
	public static synchronized <T extends ICache<?,?>> T  getOrCreateCache(
			String stategy, String cacheName, Class<?> keyClass, Class<?> valueCalss) throws Exception {
		return getOrCreateCache(stategy, cacheName, keyClass, valueCalss, 0l, 0l);
	}
	
	/**
	 * 可以指定缓存提供者的方式获取或创建缓存
	 * @param stategy    缓存策略类
	 * @param cacheName  缓存名
	 * @param keyClass   键的类类型
	 * @param valueCalss 值的类类型
	 * @param maxSize    最大缓存大小(0表示不限制)
	 * @param maxLieftime缓存过期时间(0表示不过期)
	 * @return 缓存对象
	 * @throws Exception 如果stategy为空时会抛出异常
	 */
	@SuppressWarnings("unchecked")
	public static synchronized <T extends ICache<?,?>> T  getOrCreateCache(
			String stategy, String cacheName, Class<?> keyClass, Class<?> valueCalss, 
			long maxSize, long maxLifetime) throws Exception {
		if (stategy == null) {
			throw new NullArgumentException("stategy");
		}
		
		CacheObject cacheObj = caches.get(cacheName);
	    if (cacheObj != null) {
	        return (T) cacheObj.getCache();
	    }
		ICacheStrategy starategy = (ICacheStrategy) Class.forName(stategy).newInstance();
	    T cache = (T) starategy.createCache(cacheName, keyClass, valueCalss, maxSize, maxLifetime);
	    caches.put(cacheName, new CacheObject(starategy, cache));
		return cache;
	}


	public static synchronized void destroyCache(String cacheName) {
		CacheObject cacheObj = caches.remove(cacheName);
		if (cacheObj != null) {
			cacheObj.getStrategy().destroyCache(cacheObj.getCache());
		}		
	}
	
	public static ICache<?,?>[] getAllCaches() {
		List<ICache<?,?>> values = new ArrayList<ICache<?,?>>();
		for (CacheObject item: caches.getAllCaches()) {
			values.add(item.getCache());
		}
			
		return values.toArray(new ICache<?,?>[values.size()]);
	}
}
