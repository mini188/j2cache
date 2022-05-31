package org.j2server.j2cache.cache.redis.jedis;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.j2server.j2cache.utils.PropsUtils;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Protocol;

public class JedisWapperFactory {
    public static final String JEDIS_POOL = "jedisPool";
    public static final String JEDIS_SENTINEL_POOL = "jedisSentinelPool";
    public static final String JEDIS_CLUSTER = "jedisCluster";
    
	public static IJedisWapper createJedisWapper() {
		IJedisWapper wapper;
		switch (PropsUtils.getJedisType()) {
		case JedisWapperFactory.JEDIS_POOL:
			wapper = createJedisPool();
			break;
		case JedisWapperFactory.JEDIS_CLUSTER:
			wapper = createJedisClustor();
			break;
		case JedisWapperFactory.JEDIS_SENTINEL_POOL:
			wapper = createJedisSentinelPool();
			break;
		default:
			wapper = createJedisPool();
			break;
		}
		
		return wapper;
	}
	
	private static IJedisWapper createJedisPool() {
	    if (StringUtils.isNotEmpty(PropsUtils.getRedisPassword())) {
	    	return new JedisWapper(PropsUtils.getRedisHost(), PropsUtils.getRedisPort(), PropsUtils.getRedisPassword());
	    } else {
	    	return new JedisWapper(PropsUtils.getRedisHost(), PropsUtils.getRedisPort());
	    }
	}
	
	private static IJedisWapper createJedisClustor() {
	    if (StringUtils.isNotEmpty(PropsUtils.getRedisPassword())) {
	    	return new JedisClusterWapper(getHostAndPorts(PropsUtils.getRedisHost()), PropsUtils.getRedisPassword());
	    } else {
	    	return new JedisClusterWapper(getHostAndPorts(PropsUtils.getRedisHost()));
	    }
	}
	
	private static IJedisWapper createJedisSentinelPool() {
		Set<HostAndPort> hAps = getHostAndPorts(PropsUtils.getRedisHost());
        if (StringUtils.isNotEmpty(PropsUtils.getRedisPassword())) {
        	return new JedisWapper(hAps, PropsUtils.getRedisPassword(), PropsUtils.getRedisMasterName());
        } else {
        	return new JedisWapper(hAps, PropsUtils.getRedisMasterName());
        }
	}
	
	private static Set<HostAndPort> getHostAndPorts(String hostAndPorts) {
        Set<HostAndPort> hostAndPortSet = new HashSet<>();
        String[] hostAndPortStrings = hostAndPorts.split(",");
        for (String hostAndPortString : hostAndPortStrings) {
            if (StringUtils.isBlank(hostAndPortString)) continue;
            String[] hostAndPort = hostAndPortString.split(":");

            String host = hostAndPort[0];
            int port = hostAndPort.length > 1 ? Integer.parseInt(hostAndPort[1]) : Protocol.DEFAULT_PORT;

            hostAndPortSet.add(new HostAndPort(host, port));
        }
        return hostAndPortSet;
    }
}
