package org.j2server.j2cache.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.j2server.j2cache.cache.jvm.DefaultCacheStategy;
import org.j2server.j2cache.utils.PropsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("rawtypes")
public class CacheManager {
	private final static Logger	logger = LoggerFactory.getLogger(CacheManager.class);
	private static Map<String, ICache> caches = new ConcurrentHashMap<>();
	private static ICacheStrategy cacheStrategy = new DefaultCacheStategy(); 
	private static String cacheStrategyClass;
	
	static {
		String cacheStrategyClassSetting = PropsUtils.getCacheStrategyClass();
		//加载缓存策略
		if (StringUtils.isNotEmpty(cacheStrategyClassSetting) &&  cacheStrategyClassSetting.equals(cacheStrategyClass) == false) {
			cacheStrategyClass = cacheStrategyClassSetting;
			try {
				cacheStrategy = (ICacheStrategy) Class.forName(cacheStrategyClass).newInstance();
			} catch (InstantiationException | IllegalAccessException
					| ClassNotFoundException e) {
				e.printStackTrace();
				logger.error("ChanageCache error", e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static synchronized <T extends ICache> T  getOrCreateCache(String cacheName, Class<?> keyClass, Class<?> valueCalss) {
	   T cache = (T) caches.get(cacheName);
        if (cache != null) {
            return cache;
        }
        cache = (T) cacheStrategy.createCache(cacheName, keyClass, valueCalss);
        caches.put(cacheName, cache);
		return cache;
	}
	
	
	public static synchronized <T extends ICache> T  getOrCreateCache(String cacheName, Class<?> keyClass, Class<?> valueCalss, long maxSize, long maxLifetime) {
		T cache = (T) caches.get(cacheName);
	    if (cache != null) {
	        return cache;
	    }
	    
	    cache = (T) cacheStrategy.createCache(cacheName, keyClass, valueCalss, maxSize, maxLifetime);
	    caches.put(cacheName, cache);
		return cache;
	}


	public static synchronized void destroyCache(String cacheName) {
		ICache cache = caches.remove(cacheName);
		if (cache != null) {
			cache.clear();
		}
	}
	
	public static ICache[] getAllCaches() {
		List<ICache> values = new ArrayList<ICache>();
		for (ICache item: caches.values()) {
			values.add(item);
		}
			
		return values.toArray(new ICache[values.size()]);
	}
}
