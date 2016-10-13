package org.j2server.j2cache.cache.hazelcast;

import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class HazelcastLocal {
	private static HazelcastLocal _instance = new HazelcastLocal();
	private HazelcastInstance hazelcast;
	
	private HazelcastLocal() {}
	
	public static HazelcastLocal getInstance() {
		return _instance;
	}
	
	public synchronized HazelcastInstance getHazelcast() {
		if (hazelcast == null) {
			Config config = new ClasspathXmlConfig("org/j2server/j2cache/cache/hazelcast/hazelcast-cache-config.xml");
            config.setInstanceName("j2cache");
            hazelcast = Hazelcast.newHazelcastInstance(config);
		}
		
		return hazelcast;
	}
	
}
