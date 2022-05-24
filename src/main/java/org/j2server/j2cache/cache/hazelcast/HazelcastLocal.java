package org.j2server.j2cache.cache.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class HazelcastLocal {
	private static HazelcastLocal _instance = new HazelcastLocal();
	private HazelcastInstance hazelcast;

	private HazelcastLocal() {
	}

	public static HazelcastLocal getInstance() {
		return _instance;
	}

	public synchronized HazelcastInstance getHazelcast(String mapName, int timeToLiveSeconds) {
		if (hazelcast == null) {
			Config config = new Config();
			config.setInstanceName("j2cache");

			hazelcast = Hazelcast.newHazelcastInstance(config);
		} 

		MapConfig mapConfig = hazelcast.getConfig().getMapConfig(mapName);
		if (timeToLiveSeconds > 0) {
			mapConfig.setTimeToLiveSeconds(timeToLiveSeconds);
			mapConfig.setEvictionPolicy(EvictionPolicy.RANDOM);
		}

		return hazelcast;
	}

}
