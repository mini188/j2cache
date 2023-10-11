package org.j2server.j2cache.cache.redis;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.j2server.j2cache.cache.ICache;
import org.j2server.j2cache.serializer.ISerializer;
import org.j2server.j2cache.serializer.SerializerManager;
import org.j2server.j2cache.cache.redis.jedis.IJedisWapper;
import org.j2server.j2cache.cache.redis.jedis.JedisWapperFactory;
import org.j2server.j2cache.serializer.SerializerType;

import java.util.*;

public class RedisCache<K, V> implements ICache<K, V> {
	private Class<K> keyClass;
	private Class<V> valueClass;
	private String name;
	private int cacheSize = 0;
	private long maxCacheSize;
	private long maxLifetime;
	private String keyPrefix;
	
	private IJedisWapper jedis;
    private ISerializer serializer;

    /**
     * 
     * @param name
     * @param keyPrefix
     * @param maxSize
     * @param maxLifetime
     * @param keyClass
     * @param valueClass
     */
    public RedisCache(String name, String keyPrefix, long maxSize, long maxLifetime, Class<K> keyClass,
                      Class<V> valueClass) {
        this.name = name;
        this.maxCacheSize = maxSize;
        this.maxLifetime = maxLifetime;
    	this.jedis = JedisWapperFactory.createJedisWapper();
        this.keyClass = keyClass;
        this.valueClass = valueClass;
        this.keyPrefix = keyPrefix;

        this.serializer = SerializerManager.me().getSerializer(SerializerType.FASTJSON.getTypeName());

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
        return this.jedis.exists(buildCacheKey(key));
    }

    /**
     * 判断map是否包含value值
     */
    @Override
    public boolean containsValue(Object value) {
    	throw new UnsupportedOperationException();
    }

    /**
     * 	根据key获取map的值
     */
	@Override
    public V get(Object key) {
    	byte[] valueJson = jedis.get(buildCacheKey(key)); 
		if (valueJson == null) {
			return null;
		}
		return this.serializer.deserialize(valueJson, valueClass);
    }

    /**
     * 	写入K-V
     */
    @Override
    public V put(K key, V value) {
    	Integer expire = getExpire();
    	if (expire > 0) {
    		this.jedis.setex(buildCacheKey(key), expire,  this.serializer.serialize(value));
    	} else {
    		this.jedis.set(buildCacheKey(key),  this.serializer.serialize(value));
		}
        return value;
    }

    /**
     * 	根据key移除值
     */
    @Override
    public V remove(Object key) {
    	byte[] cacheKey = buildCacheKey(key);
    	synchronized (cacheKey) {
        	V v = get(cacheKey);
            this.jedis.del(cacheKey);
            return v;
		}
    }

    /**
     * 	批量加入对象
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
     * 	清空K-V
     */
    @Override
    public void clear() {
    	Set<byte[]> keys = getAllKesByCacheName();
    	int batchSize = keys.size() > 1000 ? 1000: keys.size();
		byte[][] dels = new byte[batchSize][];
		int i = 0;
    	for (byte[] k: keys) {
    		if (i >= batchSize) {
        		jedis.unlink(dels);
        		i = 0;
    		}
    		
    		dels[i] = k;
    		i++;
    	}
    	
    	if (i > 0) {
    		jedis.unlink(dels);
    	}
    }

    /**
     *	获取全部key，慎重使用！
     */
    @Override
    public Set<K> keySet() {
    	Set<K> result = new HashSet<K>();
        Set<byte[]> keys = getAllKesByCacheName();
        for (byte[] key : keys) {
        	K realKey = restoreRealKey(key);
        	result.add(realKey);
        }
        return result;
    }

    /**
     *	 获取所有的key的值，慎重使用！
     */
	@Override
    public synchronized Collection<V> values() {
        Collection<V> list = new ArrayList<V>();
        Set<byte[]> keys = getAllKesByCacheName();
        for (byte[] key : keys) {
        	byte[] val = this.jedis.get(key);
            list.add(JSON.parseObject(val, valueClass));
        }
        return list;
    }

	private Set<byte[]> getAllKesByCacheName() {
        String keyPrefix = "";

        if (StringUtils.isNotEmpty(this.keyPrefix)) {
            keyPrefix += this.keyPrefix;
        }

        if (StringUtils.isNotEmpty(keyPrefix)) {
            keyPrefix += ":";
        }

		return this.jedis.keys(String.format("%s%s:*", keyPrefix, name).getBytes());
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

	private byte[] buildCacheKey(Object key) {
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
		
		cacheKey += JSON.toJSONString(key);
		return cacheKey.getBytes();
	}
	
	private K restoreRealKey(byte[] cacheKey) {
		if (cacheKey == null || cacheKey.length <= 0) {
			return null;
		}
		
		String cacheKeyStr = new String(cacheKey);
		
		String cachePrefix = "";
		if (StringUtils.isNotEmpty(this.keyPrefix)) {
			cachePrefix += this.keyPrefix;
		}
		
		if (StringUtils.isNotEmpty(cachePrefix)) {
			cachePrefix += ":";
		}
		
		if (StringUtils.isNotEmpty(this.name)) {
			cachePrefix += this.name;
		}
		
		if (StringUtils.isNotEmpty(cachePrefix)) {
			cachePrefix += ":";
		}
		
		return JSON.parseObject(cacheKeyStr.replace(cachePrefix, ""), keyClass);
	}
	
	private Integer getExpire() {
		if (this.maxLifetime <= 0) {
			return -1;
		}
		return (int) (this.maxLifetime / 1000);
	}
}
