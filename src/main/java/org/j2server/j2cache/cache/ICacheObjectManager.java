package org.j2server.j2cache.cache;

import java.util.Collection;

public interface ICacheObjectManager {
	public CacheObject put(String cahceName, CacheObject value);
	
	public CacheObject get(String cahceName);
	
	public CacheObject remove(String cahceName);
	
	public Collection<CacheObject> getAllCaches();

}
