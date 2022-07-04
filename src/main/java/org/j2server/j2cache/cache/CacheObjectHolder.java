package org.j2server.j2cache.cache;

import java.util.Collection;

import org.j2server.j2cache.utils.PropsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheObjectHolder {
	private final static Logger	logger = LoggerFactory.getLogger(CacheObjectHolder.class);
	private ICacheObjectManager cacheObjectManager;
	
	public CacheObjectHolder() {
		try {
			String cacheObjectManagerClass = PropsUtils.getCacheObjectManagerClass();
			cacheObjectManager = CacheObjectManagerFactory.createCacheObjectManager(cacheObjectManagerClass);
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			logger.error("create cache object manager instance error, please check your settings.", e);
		}
	}
	
	public CacheObject put(String cacheName, CacheObject value) {
		return cacheObjectManager.put(cacheName, value);
	}

	public CacheObject get(String cacheName) {
		return cacheObjectManager.get(cacheName);
	}

	public CacheObject remove(String cacheName) {
		return cacheObjectManager.remove(cacheName);
	}

	public Collection<CacheObject> getAllCaches() {
		return cacheObjectManager.getAllCaches();
	}

}
