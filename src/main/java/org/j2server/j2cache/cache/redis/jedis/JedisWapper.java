package org.j2server.j2cache.cache.redis.jedis;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.j2server.j2cache.utils.PropsUtils;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.Protocol;
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
	
	public JedisWapper() {
		this(PropsUtils.getRedisHost(), PropsUtils.getRedisPort());
	}

	public JedisWapper(String host, Integer port) {
		this(host,port, Protocol.DEFAULT_TIMEOUT);
	}
	
	public JedisWapper(String host, Integer port, Integer timeOut) {
		this(host, port, timeOut, null);
	}
	
	public JedisWapper(String host, Integer port, String pwd) {
		this(host, port, Protocol.DEFAULT_TIMEOUT, pwd);
	}
	
	public JedisWapper(String host, Integer port, Integer timeOut,String pwd) {
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		if (StringUtils.isNotEmpty(pwd)) {
			pool = new JedisPool(poolConfig, host, port, timeOut, pwd);
		} else {
			pool = new JedisPool(poolConfig, host, port, timeOut);
		}
	}
	
	public JedisWapper(Set<HostAndPort> hostAndPorts, String masterName) {
		this(hostAndPorts, null, masterName);
	}
	
	public JedisWapper(Set<HostAndPort> hostAndPorts, String pwd, String masterName) {
		Set<String> sentinels = new HashSet<String>();
		for (HostAndPort hap: hostAndPorts) {
			sentinels.add(hap.toString());
		}
		if (StringUtils.isNotEmpty(pwd)) {
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
