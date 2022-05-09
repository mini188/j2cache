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
	
	public CacheObject put(String cahceName, CacheObject value) {
		return cacheObjectManager.put(cahceName, value);
	}

	public CacheObject get(String cahceName) {
		return cacheObjectManager.get(cahceName);
	}

	public CacheObject remove(String cahceName) {
		return cacheObjectManager.remove(cahceName);
	}

	public Collection<CacheObject> getAllCaches() {
		return cacheObjectManager.getAllCaches();
	}

}
