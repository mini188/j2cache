package org.j2server.j2cache.cache;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultCacheObjectManager implements ICacheObjectManager {
	private Map<String, CacheObject> cacheObjects = new ConcurrentHashMap<>();

	@Override
	public CacheObject put(String cahceName, CacheObject value) {
		return cacheObjects.put(cahceName, value);
	}

	@Override
	public CacheObject get(String cahceName) {
		return cacheObjects.get(cahceName);
	}

	@Override
	public CacheObject remove(String cahceName) {
		return cacheObjects.remove(cahceName);
	}

	@Override
	public Collection<CacheObject> getAllCaches() {
		return cacheObjects.values();
	}

}
