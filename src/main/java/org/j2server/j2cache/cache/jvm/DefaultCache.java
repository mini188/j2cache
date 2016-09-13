package org.j2server.j2cache.cache.jvm;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.j2server.j2cache.cache.ICache;

public class DefaultCache<K, V> implements ICache<K, V> {
	protected Map<K, V> map;
	private String name;
	private long maxCacheSize;
	private long maxLifetime;
	private int cacheSize = 0;

	public DefaultCache(String name, long maxSize, long maxLifetime) {
		this.name = name;
		this.maxCacheSize = maxSize;
		this.maxLifetime = maxLifetime;
		map = new ConcurrentHashMap<K, V>(103);
	}
	
	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	@Override
	public V get(Object key) {
		return map.get(key);
	}

	@Override
	public V put(K key, V value) {
		return map.put(key, value);
	}

	@Override
	public V remove(Object key) {
		return map.remove(key);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		map.putAll(m);
	}

	@Override
	public void clear() {
		if (map != null) {
			map.clear();
		}
	}

	@Override
	public Set<K> keySet() {
		return map.keySet();
	}

	@Override
	public Collection<V> values() {
		return map.values();
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return map.entrySet();
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public long getMaxCacheSize() {
		return maxCacheSize;
	}

	@Override
	public void setMaxCacheSize(int maxSize) {
		this.maxCacheSize = maxSize;
	}

	@Override
	public long getMaxLifetime() {
		return maxLifetime;
	}

	@Override
	public void setMaxLifetime(long maxLifetime) {
		this.maxLifetime = maxLifetime;
	}

	@Override
	public int getCacheSize() {
		return cacheSize;
	}

	
	@SuppressWarnings("unused")
	private static class CacheWapper<V>  {
		 /**
         * Underlying object wrapped by the CacheObject.
         */
        public V object;

        /**
         * The size of the Cacheable object. The size of the Cacheable
         * object is only computed once when it is added to the cache. This makes
         * the assumption that once objects are added to cache, they are mostly
         * read-only and that their size does not change significantly over time.
         */
        public int size;		
		private long createTime;
	}
}
