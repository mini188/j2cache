package org.j2server.j2cache.cache.redis.jedis;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import redis.clients.jedis.*;
import redis.clients.jedis.util.Pool;

/**
 * Jedis包装类 ，支持JedisPool和JedisSentinelPool</br>
 * 
 * 仅实现了用项目用到的jedis方法.
 * @author xiexb
 *
 */
public class JedisWapper implements IJedisWapper{
	private Pool<Jedis> pool;
	
	public JedisWapper(String host, Integer port, Integer timeOut, String pwd, GenericObjectPoolConfig<Jedis> poolConfig) {
		int timeout = timeOut == null ? Protocol.DEFAULT_TIMEOUT : timeOut;
		if (StringUtils.isNotEmpty(pwd) && poolConfig != null) {
			pool = new JedisPool(poolConfig, host, port, timeout, pwd);
		} else if (poolConfig != null) {
			pool = new JedisPool(poolConfig, host, port);
		} else {
			pool = new JedisPool(host, port);
		}
	}
	
	public JedisWapper(Set<HostAndPort> hostAndPorts, String pwd, String masterName, final GenericObjectPoolConfig<Jedis> poolConfig) {
		Set<String> sentinels = new HashSet<String>();
		for (HostAndPort hap: hostAndPorts) {
			sentinels.add(hap.toString());
		}
		
		if (poolConfig != null && StringUtils.isNotEmpty(pwd)) {
			pool = new JedisSentinelPool(masterName, sentinels, poolConfig, pwd);
		} else if (StringUtils.isNotEmpty(pwd)) {
			pool = new JedisSentinelPool(masterName, sentinels, pwd);
		} else {
			pool = new JedisSentinelPool(masterName, sentinels);
		}
	}
	
	public boolean exists(final byte[] key) {
		Jedis jedis = getJedis();
		try {
			return jedis.exists(key);
		} finally {
			closeResource(jedis);
		}
	}
	
	public byte[] get(final byte[] key) {
		Jedis jedis = getJedis();
		try {
			return jedis.get(key);
		} finally {
			closeResource(jedis);
		}
	}
	
	public String set(final byte[] key, final byte[] value) {
		Jedis jedis = getJedis();
		try {
			return jedis.set(key, value);
		} finally {
			closeResource(jedis);
		}
	}
	
	public String setex(final byte[] key, final long seconds, final byte[] value) {
		Jedis jedis = getJedis();
		try {
			return jedis.setex(key, seconds, value);
		} finally {
			closeResource(jedis);
		}
	}
	
	public long del(final byte[] key) {
		Jedis jedis = getJedis();
		try {
			return jedis.del(key);
		} finally {
			closeResource(jedis);
		}
	}
	
	public long unlink(final byte[] key) {
		Jedis jedis = getJedis();
		try {
			return jedis.unlink(key);
		} finally {
			closeResource(jedis);
		}
	}
	
	public long unlink(final byte[]... keys) {
		Jedis jedis = getJedis();
		try {
			return jedis.unlink(keys);
		} finally {
			closeResource(jedis);
		}
	}
	
	public Set<byte[]> keys(final byte[] pattern) {
		Jedis jedis = getJedis();
		try {
			return jedis.keys(pattern);
		} finally {
			closeResource(jedis);
		}
	}
	
    public Jedis getJedis() {
        return pool.getResource();
    }

    public void closeResource(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

}
