package org.j2server.j2cache.cache;

/**
 * 缓存管理对象，用于存放缓存及缓存策略对象
 * @author xiexb
 *
 */
public class CacheObject {
	private ICache<?, ?> cache;
	private ICacheStrategy strategy;
	
	CacheObject(ICacheStrategy strategy, ICache<?, ?> cache) {
		this.setStrategy(strategy);
		this.setCache(cache);
	}

	public ICache<?, ?> getCache() {
		return cache;
	}

	public void setCache(ICache<?, ?> cache) {
		this.cache = cache;
	}

	public ICacheStrategy getStrategy() {
		return strategy;
	}

	public void setStrategy(ICacheStrategy strategy) {
		this.strategy = strategy;
	}
}
