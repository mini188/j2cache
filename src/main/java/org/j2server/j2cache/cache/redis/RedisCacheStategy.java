package org.j2server.j2cache.cache.redis;

import org.j2server.j2cache.cache.ICache;
import org.j2server.j2cache.cache.ICacheStrategy;
import org.j2server.j2cache.utils.PropsUtils;

@SuppressWarnings("rawtypes")
public class RedisCacheStategy implements ICacheStrategy {

	@SuppressWarnings("unchecked")
	@Override
	public ICache createCache(String name, Class<?> keyClass, Class<?> valueCalss) {
		return new RedisCache(name
				, PropsUtils.getCacheMaxSize()
				, PropsUtils.getCacheMaxLifeTime()
				, keyClass
				, valueCalss);
	}

	@Override
	public void destroyCache(ICache cache) {
		cache.clear();
	}

}
