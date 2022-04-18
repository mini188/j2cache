package org.j2server.j2cache.cache.redis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.j2server.j2cache.cache.ICache;
import org.j2server.j2cache.utils.PropsUtils;

import com.alibaba.fastjson.JSON;

import redis.clients.jedis.Jedis;

public class RedisCache<K, V> implements ICache<K, V> {
	private Class<?> keyClass;
	private Class<?> valueClass;
	private String name;
	private int cacheSize = 0;
	private long maxCacheSize;
	private long maxLifetime;
	private String keyPrefix;
	
	private Jedis jedis;

    /**
     * 
     * @param name
     * @param keyPrefix
     * @param maxSize
     * @param maxLifetime
     * @param keyClass
     * @param valueCalss
     * @param this.jedis
     */
    public RedisCache(String name, String keyPrefix, long maxSize, long maxLifetime, Class<K> keyClass,
                      Class<V> valueCalss) {
        this.name = name;
        this.maxCacheSize = maxSize;
        this.maxLifetime = maxLifetime;
        this.jedis = new Jedis(PropsUtils.getRedisHost(), PropsUtils.getRedisPort());
	    if (StringUtils.isNotEmpty(PropsUtils.getRedisPassword())) {
	    	this.jedis.auth(PropsUtils.getRedisPassword());
	    }
        this.keyClass = keyClass;
        this.valueClass = valueCalss;
        this.keyPrefix = keyPrefix;
    }

	private String getCacheKey(Object key) {
		String cacheKey = "";
		
		if (StringUtils.isNotEmpty(this.keyPrefix)) {
			cacheKey += this.keyPrefix;
		}
		
		if (StringUtils.isNotEmpty(cacheKey)) {
			cacheKey += ":";
		}
		
		if (StringUtils.isNotEmpty(this.name)) {
			cacheKey += this.name;
		}
		
		if (StringUtils.isNotEmpty(cacheKey)) {
			cacheKey += ":";
		}
		
		if (key instanceof String) {
			cacheKey += ((String) key);
		} else {
			//为了兼容Map接口里的Object入参使用JSON序列化key
			//JSON串的特殊字符处理掉
			cacheKey += JSON.toJSONString(key).replaceAll("\\{", "")
					.replaceAll("}", "")
					.replaceAll("\"", "")
					.replaceAll(",", "")
					.replaceAll(":", "_")
					.trim();
		}

		return cacheKey;
	}
	
	private Integer getExpire() {
		if (this.maxLifetime <= 0) {
			return -1;
		}
		return (int) (this.maxLifetime / 1000);
	}
	
    /**
     * 获取map的大小
     */
    @Override
    public int size() {
        return getAllKesByCacheName().size();
    }

    /**
     * 判断map是否为空
     */
    @Override
    public boolean isEmpty() {
        return size() <= 0;
    }

    /**
     * 判断map是否包含key键
     */
    @Override
    public boolean containsKey(Object key) {
        return this.jedis.exists(getCacheKey(key));
    }

    /**
     * 判断map是否包含value值
     */
    @Override
    public boolean containsValue(Object value) {
    	throw new RuntimeException("not support containsValue in redis cache.");
    }

    /**
     * 根据key获取map的值
     */
    @SuppressWarnings("unchecked")
	@Override
    public V get(Object key) {
    	String valueJson = jedis.get(getCacheKey(key)); 
		if (valueJson == null) {
			return null;
		}
		return (V) JSON.parseObject(valueJson, valueClass);
    }

    /**
     * 根据key设置map的值
     */
    @Override
    public V put(K key, V value) {
    	Integer expire = getExpire();
    	if (expire > 0) {
    		this.jedis.setex(getCacheKey(key), expire, JSON.toJSONString(value));
    	} else {
    		this.jedis.set(getCacheKey(key), JSON.toJSONString(value));
		}
        return value;
    }

    /**
     * 根据key移除map的值
     */
    @Override
    public V remove(Object key) {
    	String cacheKey = getCacheKey(key);
    	synchronized (cacheKey.intern()) {
        	V v = get(cacheKey);
            this.jedis.del(cacheKey);
            return v;
		}
    }

    /**
     * 追加Map对象到当前Map集合中
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        if (null != m) {
            for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * 清空map里所有的值
     */
    @Override
    public void clear() {
        Set<String> keySet = getAllKesByCacheName();
        String[] strs = Arrays.asList(keySet.toArray()).toArray(new String[0]);
        if (null != keySet && keySet.size() > 0) {
            this.jedis.del(strs);
        }
    }

    @Override
    public Set<K> keySet() {
    	throw new UnsupportedOperationException();
    }

    /**
     * 获取map里所有的key的值
     */
    @SuppressWarnings("unchecked")
	@Override
    public synchronized Collection<V> values() {
        Collection<V> list = new ArrayList<V>();
        Set<String> keys = getAllKesByCacheName();
        for (String key : keys) {
        	String val = this.jedis.get(key);
            list.add((V) JSON.parseObject(val, valueClass));
        }
        return list;
    }

	private Set<String> getAllKesByCacheName() {
		return this.jedis.keys(String.format("%s:*", name));
	}

    public Set<Map.Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
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
