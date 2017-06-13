package org.j2server.j2cache.cache.jvm;

import org.j2server.j2cache.cache.ICache;
import org.j2server.j2cache.cache.ICacheStrategy;
import org.j2server.j2cache.utils.PropsUtils;


@SuppressWarnings("rawtypes")
public class DefaultCacheStategy implements ICacheStrategy {

	@Override
	public ICache createCache(String name, Class<?> keyClass,
			Class<?> valueCalss) {
		return createCache(name, keyClass, valueCalss,
				PropsUtils.getCacheMaxSize(),
				PropsUtils.getCacheMaxLifeTime());
	}

	@Override
	public void destroyCache(ICache cache) {
		cache.clear();
	}

	@Override
	public ICache createCache(String name, Class<?> keyClass,
			Class<?> valueCalss, long maxSize, long maxLifetime) {
		return new DefaultCache(name, maxSize, maxLifetime);
	}

}