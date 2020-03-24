package org.j2server.j2cache.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;
import org.j2server.j2cache.cache.jvm.DefaultCacheStategy;
import org.j2server.j2cache.utils.PropsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("rawtypes")
public class CacheManager {
	private final static Logger	logger = LoggerFactory.getLogger(CacheManager.class);
	private static Map<String, CacheObject> caches = new ConcurrentHashMap<>();
	private static ICacheStrategy cacheStrategy = new DefaultCacheStategy(); 
	private static String cacheStrategyClass;
	
	
	private static void InitCacheStrategy() {
		String cacheStrategyClassSetting = PropsUtils.getCacheStrategyClass();
		//加载缓存策略
		if (StringUtils.isNotEmpty(cacheStrategyClassSetting) &&  cacheStrategyClassSetting.equals(cacheStrategyClass) == false) {
			cacheStrategyClass = cacheStrategyClassSetting;
			try {
				cacheStrategy = (ICacheStrategy) Class.forName(cacheStrategyClass).newInstance();
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
		T cache = (T) caches.get(cacheName);
	    if (cache != null) {
	        return cache;
	    }
        
	    if (cacheStrategy == null) {
	    	InitCacheStrategy();
	    }
	    
	    cache = (T) cacheStrategy.createCache(cacheName, keyClass, valueCalss, maxSize, maxLifetime);
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
		for (CacheObject item: caches.values()) {
			values.add(item.getCache());
		}
			
		return values.toArray(new ICache<?,?>[values.size()]);
	}
	
	/**
	 * 缓存管理对象，用于存放缓存及缓存策略对象
	 * @author xiexb
	 *
	 */
	static class CacheObject {
		private ICache cache;
		private ICacheStrategy strategy;
		
		CacheObject(ICacheStrategy strategy, ICache cache) {
			this.setStrategy(strategy);
			this.setCache(cache);
		}

		public ICache getCache() {
			return cache;
		}

		public void setCache(ICache cache) {
			this.cache = cache;
		}

		public ICacheStrategy getStrategy() {
			return strategy;
		}

		public void setStrategy(ICacheStrategy strategy) {
			this.strategy = strategy;
		}
	}
}
