package org.j2server.j2cache.cache.iginte;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
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
		config.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.MILLISECONDS, maxLifetime)));
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
		Set<K> keySet = new HashSet<K>();
		Iterator<javax.cache.Cache.Entry<K, V>> it = map.iterator();
		while (it.hasNext()) {
			keySet.add(it.next().getKey());
		}
		return keySet;
	}

	@Override
	public Collection<V> values() {
		Collection<V> vals = new ArrayList<V>();
		Iterator<javax.cache.Cache.Entry<K, V>> it = map.iterator();
		while (it.hasNext()) {
			vals.add(it.next().getValue());
		}
		return vals;
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		Set<Entry<K, V>> entrySet = new HashSet<Entry<K,V>>();
		Iterator<javax.cache.Cache.Entry<K, V>> it = map.iterator();
		while (it.hasNext()) {
			javax.cache.Cache.Entry<K, V> e =it.next();
			EntryWapper<K, V> wapper = new EntryWapper<K, V>();
			wapper.setKey(e.getKey());
			wapper.setValue(e.getValue());
			entrySet.add(wapper);
		}
		return entrySet;
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
