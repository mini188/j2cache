package org.j2server.j2cache.cache.jvm;

import org.j2server.j2cache.cache.ICache;
import org.j2server.j2cache.cache.ICacheStrategy;
import org.j2server.j2cache.utils.PropsUtils;

@SuppressWarnings("rawtypes")
public class DefaultCacheStategy implements ICacheStrategy {

	@Override
	public ICache createCache(String name, Class<?> keyClass, Class<?> valueCalss) {
		return new DefaultCache(name
				, PropsUtils.getCacheMaxSize()
				, PropsUtils.getCacheMaxLifeTime());
	}

	@Override
	public void destroyCache(ICache cache) {
		cache.clear();
	}

}
