package org.j2server.j2cache.cache.iginte;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;

import org.apache.ignite.cache.CachePeekMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.j2server.j2cache.cache.ICache;

@SuppressWarnings("unchecked")
public class IgniteCache<K, V> implements ICache<K, V> {
	private String name;
	private long maxCacheSize;
	private long maxLifetime;
	private int cacheSize = 0;
	private org.apache.ignite.IgniteCache<K, V> map;
	
	public IgniteCache(String name, long maxSize, long maxLifetime) {
		this.name = name;
		this.maxCacheSize = maxSize;
		this.maxLifetime = maxLifetime;
		CacheConfiguration<K, V> config = new CacheConfiguration<K, V>();
		config.setName(name);
		if (maxLifetime > 0) {
			config.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.MILLISECONDS, maxLifetime)));
		}
	    map = IgniteInstance.getInstance().getIgnite().getOrCreateCache(config);
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.size(CachePeekMode.PRIMARY) == 0;
	}

	@Override
	public boolean containsKey(Object key) {
		return map.containsKey((K)key);
	}

	@Override
	public boolean containsValue(Object value) {
		return false;
	}

	@Override
	public V get(Object key) {
		return map.get((K) key);
	}

	@Override
	public V put(K key, V value) {
		map.put(key, value);
		return value;
	}

	@Override
	public V remove(Object key) {
		V v = map.get((K) key);
		map.remove((K) key);
		return v;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		map.putAll(m);
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public Set<K> keySet() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<V> values() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;		
	}

	@Override
	public long getMaxCacheSize() {
		return this.maxCacheSize;
	}

	@Override
	public void setMaxCacheSize(int maxSize) {
		maxCacheSize = maxSize;
	}

	@Override
	public long getMaxLifetime() {
		return this.maxLifetime;
	}

	@Override
	public void setMaxLifetime(long maxLifetime) {
		this.maxLifetime = maxLifetime;
	}

	@Override
	public int getCacheSize() {
		return this.cacheSize;
	}

	/**
	 * 用于
	 * @author xiexb
	 *
	 * @param <K>
	 * @param <V>
	 */
	static class EntryWapper<K, V> implements java.util.Map.Entry<K, V> {
		private K k;
		private V v;
		
		@Override
		public K getKey() {
			return k;
		}

		@Override
		public V getValue() {
			return v;
		}

		@Override
		public V setValue(V value) {
			v = value;
			return v;
		}
		
		public K setKey(K value) {
			k = value;
			return k;
		}
	}
}
