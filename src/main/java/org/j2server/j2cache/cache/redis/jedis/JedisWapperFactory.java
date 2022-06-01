package org.j2server.j2cache.cache.redis.jedis;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.j2server.j2cache.utils.PropsUtils;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Protocol;

public class JedisWapperFactory {
	public static IJedisWapper createJedisWapper() {
		IJedisWapper wapper;
		switch (PropsUtils.getPoolModule()) {
		case PropsUtils.JEDIS_POOL:
			wapper = createJedisPool();
			break;
		case PropsUtils.JEDIS_CLUSTER:
			wapper = createJedisClustor();
			break;
		case PropsUtils.JEDIS_SENTINEL_POOL:
			wapper = createJedisSentinelPool();
			break;
		default:
			wapper = createJedisPool();
			break;
		}

		return wapper;
	}

	private static IJedisWapper createJedisPool() {
		return new JedisWapper(PropsUtils.getRedisHost(), PropsUtils.getRedisPort(), PropsUtils.getRedisTimeOut(),
				PropsUtils.getRedisPassword(), JedisPoolConfig.buildPoolConfig(PropsUtils.getJedisPoolConfig()));
	}

	private static IJedisWapper createJedisClustor() {
		return new JedisClusterWapper(getHostAndPorts(PropsUtils.getRedisHost()), PropsUtils.getRedisTimeOut(),
				PropsUtils.getRedisMaxAttempts(), PropsUtils.getRedisPassword(),
				JedisPoolConfig.buildPoolConfig(PropsUtils.getJedisPoolConfig()));
	}

	private static IJedisWapper createJedisSentinelPool() {
		Set<HostAndPort> hAps = getHostAndPorts(PropsUtils.getRedisHost());
		return new JedisWapper(hAps, PropsUtils.getRedisPassword(), PropsUtils.getRedisMasterName(),
				JedisPoolConfig.buildPoolConfig(PropsUtils.getJedisPoolConfig()));
	}

	private static Set<HostAndPort> getHostAndPorts(String hostAndPorts) {
		Set<HostAndPort> hostAndPortSet = new HashSet<>();
		String[] hostAndPortStrings = hostAndPorts.split(",");
		for (String hostAndPortString : hostAndPortStrings) {
			if (StringUtils.isBlank(hostAndPortString))
				continue;
			String[] hostAndPort = hostAndPortString.split(":");

			String host = hostAndPort[0];
			int port = hostAndPort.length > 1 ? Integer.parseInt(hostAndPort[1]) : Protocol.DEFAULT_PORT;

			hostAndPortSet.add(new HostAndPort(host, port));
		}
		return hostAndPortSet;
	}
}
