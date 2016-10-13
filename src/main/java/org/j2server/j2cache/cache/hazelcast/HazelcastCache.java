package org.j2server.j2cache.cache.hazelcast;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.j2server.j2cache.cache.ICache;

import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.map.impl.operation.MapPartitionDestroyTask;

public class HazelcastCache<K, V> implements ICache<K, V>{
	private String name;
	private long maxCacheSize;
	private long maxLifetime;
	private int cacheSize = 0;
	protected IMap map;
	private static HazelcastInstance hazelcast = null;
	
	public HazelcastCache(String name, long maxSize, long maxLifetime) {
		this.name = name;
		this.maxCacheSize = maxSize;
		this.maxLifetime = maxLifetime;
        //ClassLoader loader = new ClusterClassLoader();
        //Thread.currentThread().setContextClassLoader(loader);
		
        Config config = new ClasspathXmlConfig("org/j2server/j2cache/cache/hazelcast/hazelcast-cache-config.xml");
        config.setInstanceName("j2cache");
        //config.setClassLoader(loader);
		hazelcast = Hazelcast.getOrCreateHazelcastInstance(config);
		
		map = hazelcast.getMap(name);
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
		return (V)map.get(key);
	}

	@Override
	public V put(K key, V value) {
		return (V) map.put(key,  value);
	}

	@Override
	public V remove(Object key) {
		return (V) map.remove(key);
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
		//this.name = name;
	}

	@Override
	public long getMaxCacheSize() {
		return this.maxCacheSize;
	}

	@Override
	public void setMaxCacheSize(int maxSize) {
		this.maxCacheSize = maxSize;
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

}
