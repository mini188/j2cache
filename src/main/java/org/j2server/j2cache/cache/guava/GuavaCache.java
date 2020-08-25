package org.j2server.j2cache.cache.guava;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.j2server.j2cache.cache.ICache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class GuavaCache<K, V> implements ICache<K, V> {
	protected LoadingCache<K, V> map;
	private String name;
	private long maxCacheSize;
	private long maxLifetime;
	private int cacheSize = 0;

	public GuavaCache(String name, long maxSize, long maxLifetime) {
		this.name = name;
		this.maxCacheSize = maxSize;
		this.maxLifetime = maxLifetime;
		CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();

		if (this.maxLifetime > 0) {
			builder.expireAfterWrite(maxLifetime, TimeUnit.MILLISECONDS);
		}
		if (this.maxCacheSize > 0) {
			builder.maximumSize(this.maxCacheSize);
		}

		map = builder.build(new CacheLoader<K, V>() {
			@Override
			public V load(K key) throws Exception {
				return null;
			}
		});
	}

	@Override
	public int size() {
		Long size = new Long(map.size());
		return size.intValue();
	}

	@Override
	public boolean isEmpty() {
		return map.asMap().isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return map.asMap().containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return map.asMap().containsValue(value);
	}

	@Override
	public V get(Object key) {
		return map.getIfPresent(key);
	}

	@Override
	public V put(K key, V value) {
		V returnValue = map.getIfPresent(key);
		map.put(key, value);
		return returnValue;
	}

	@Override
	public V remove(Object key) {
		V returnValue = map.getIfPresent(key);
		map.invalidate(key);
		return returnValue;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		map.putAll(m);
	}

	@Override
	public void clear() {
		map.invalidateAll();
	}

	@Override
	public Set<K> keySet() {
		return map.asMap().keySet();
	}

	@Override
	public Collection<V> values() {
		return map.asMap().values();
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return map.asMap().entrySet();
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
}
