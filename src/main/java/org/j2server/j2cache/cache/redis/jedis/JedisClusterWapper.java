package org.j2server.j2cache.cache.redis.jedis;

import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import redis.clients.jedis.Connection;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.Protocol;

/**
 * JedisCluster包装类。支持redis集群方式 </br>
 * 
 * 仅实现了用项目用到的JedisCluster方法.
 * 
 * @author xiexb
 *
 */
public class JedisClusterWapper implements IJedisWapper {
	protected JedisCluster jedisCluster;
	
	public JedisClusterWapper(Set<HostAndPort> haps) {
		this(haps, Protocol.DEFAULT_TIMEOUT, null, null);
	}
	
	public JedisClusterWapper(Set<HostAndPort> haps, String password) {
		this(haps, Protocol.DEFAULT_TIMEOUT, null, password);
	}
	
	public JedisClusterWapper(Set<HostAndPort> haps, Integer timeout, Integer maxAttempts, String password) {
		GenericObjectPoolConfig<Connection> poolConfig = new GenericObjectPoolConfig<>();
		jedisCluster = newJedisCluster(haps, timeout, maxAttempts, password, poolConfig);
	}

	public static JedisCluster newJedisCluster(Set<HostAndPort> haps, Integer timeout, Integer maxAttempts,
			String password, final GenericObjectPoolConfig<Connection> poolConfig) {
		JedisCluster jedisCluster;
		if (timeout != null && maxAttempts != null && password != null && poolConfig != null) {
	        jedisCluster = new JedisCluster(haps, timeout, timeout, maxAttempts, password, poolConfig);
		} else if (timeout != null && maxAttempts != null) {
			jedisCluster = new JedisCluster(haps, timeout, maxAttempts);
		} else if (timeout != null) {
			jedisCluster = new JedisCluster(haps, timeout);
		} else {
			jedisCluster = new JedisCluster(haps);
		}
		return jedisCluster;
	}

	public boolean exists(final byte[] key) {
		return jedisCluster.exists(key);
	}

	public byte[] get(final byte[] key) {
		return jedisCluster.get(key);
	}

	public String set(final byte[] key, final byte[] value) {
		return jedisCluster.set(key, value);
	}

	public String setex(final byte[] key, final long seconds, final byte[] value) {
		return jedisCluster.setex(key, seconds, value);
	}

	public long del(final byte[] key) {
		return jedisCluster.del(key);
	}

	public long unlink(final byte[] key) {
		return jedisCluster.unlink(key);
	}

	public long unlink(final byte[]... keys) {
		return jedisCluster.unlink(keys);
	}

	public Set<byte[]> keys(final byte[] pattern) {
		return jedisCluster.keys(pattern);
	}
}
