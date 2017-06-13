package org.j2server.j2cache.cache.redis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.j2server.j2cache.cache.ICache;
import org.j2server.j2cache.utils.PropsUtils;

import redis.clients.jedis.Jedis;

import com.alibaba.fastjson.JSON;

@SuppressWarnings("unchecked")
public class RedisCache<K, V> implements ICache<K, V> {
	private Class<?> keyClass;
	private Class<?> valueClass;
	private String name;
	private int cacheSize = 0;
	private long maxCacheSize;
	private long maxLifetime;
	
	private Jedis jedis;
	
	public RedisCache(String name, long maxSize, long maxLifetime, Class<?> keyClass, Class<?> valueCalss) {
		this.name = name;
		this.maxCacheSize = maxSize;
		this.maxLifetime = maxLifetime;
	    this.keyClass = keyClass;
	    this.valueClass = valueCalss;
	    this.jedis = new Jedis(PropsUtils.getRedisHost(), PropsUtils.getRedisPort());
	    if (StringUtils.isNotEmpty(PropsUtils.getRedisPassword())) {
	    	this.jedis.auth(PropsUtils.getRedisPassword());
	    }	    
		if (maxLifetime > 0) {
			int secodes = (int) (maxLifetime / 1000);
			jedis.expire(name, secodes);
		}
	}
	
	/**
	 * 获取map的大小
	 */
	@Override
	public int size() {	
		return jedis.hgetAll(name).size();
	}
	
	/**
	 * 判断map是否为空
	 */
	@Override
	public boolean isEmpty() {
		return jedis.hgetAll(name).isEmpty();
	}
	
	/**
	 * 判断map是否包含key键
	 */
	@Override
	public boolean containsKey(Object key) {
		return jedis.hexists(name, JSON.toJSONString(key));
	}
	
	/**
	 * 判断map是否包含value值
	 */
	@Override
	public boolean containsValue(Object value) {
		return jedis.hvals(name).contains(value);
	}
	
	/**
	 * 根据key获取map的值
	 */
	@Override
	public V get(Object key) {
		String json = jedis.hget(name, JSON.toJSONString(key)); 
		return (V) JSON.parseObject(json, valueClass);
	}
	
	/**
	 * 根据key设置map的值
	 */
	@Override
	public V put(K key, V value) {
		jedis.hset(name, JSON.toJSONString(key), JSON.toJSONString(value));
		return value;
	}
	
	/**
	 * 根据key移除map的值
	 */
	@Override
	public V remove(Object key) {
		jedis.hdel(name, JSON.toJSONString(key));
		return null;
	}
	
	/**
	 * 追加Map对象到当前Map集合中
	 */
	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		if(null != m){
			for (java.util.Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {  
				jedis.hset(name, JSON.toJSONString(entry.getKey()), JSON.toJSONString(entry.getValue()));
			} 	
		}
		 
	}
	
	/**
	 * 清空map里所有的值
	 */
	@Override
	public void clear() {
		Set<String> keySet = jedis.hgetAll(name).keySet();
		String[] strs = Arrays.asList( keySet.toArray() ).toArray(new String[0]);
		if(null != keySet && keySet.size()>0){
			jedis.hdel(name,strs);
		}
	}

	@Override
	public Set<K> keySet() {
		Set<K> result = new HashSet<K>();
		Set<String> keys = jedis.hgetAll(name).keySet();
		for (String key: keys) {
			result.add((K)JSON.parseObject(key, keyClass));
		}
		return result;
	}
	
	/**
	 * 获取map里所有的key的值
	 */
	@Override
	public Collection<V> values() {
		Collection<V> list = new ArrayList<V>();
		Set<String> keysSet = jedis.hkeys(name);
		for (String string : keysSet) {
			list.add((V) JSON.parseObject(jedis.hget(name,string), valueClass));
		}
		return list;
	}

	public Set<java.util.Map.Entry<K, V>> entrySet() {
		Set<java.util.Map.Entry<String, String>> entry = jedis.hgetAll(name).entrySet();
		Set<java.util.Map.Entry<K, V>> e2 = new HashSet<java.util.Map.Entry<K, V>>();
		for (Entry<String, String> e : entry) {
			e2.add((java.util.Map.Entry<K, V>) e);
		}
		return e2;
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