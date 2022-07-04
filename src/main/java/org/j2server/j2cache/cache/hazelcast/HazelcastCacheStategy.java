package org.j2server.j2cache.cache.hazelcast;

import org.j2server.j2cache.cache.ICache;
import org.j2server.j2cache.cache.ICacheStrategy;
import org.j2server.j2cache.utils.PropsUtils;

@SuppressWarnings("rawtypes")
public class HazelcastCacheStategy implements ICacheStrategy{

	@Override
	public ICache createCache(String name, Class<?> keyClass,
			Class<?> valueClass) {
		return createCache(name, keyClass, valueClass, PropsUtils.getCacheMaxSize(), PropsUtils.getCacheMaxSize());
	}

	@Override
	public void destroyCache(ICache cache) {
		cache.clear();
	}

	@Override
	public ICache createCache(String name, Class<?> keyClass,
			Class<?> valueClass, long maxSize, long maxLifetime) {
		return new HazelcastCache(name, maxSize, maxLifetime);
	}

}
