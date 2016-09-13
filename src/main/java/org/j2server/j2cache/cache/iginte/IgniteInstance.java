package org.j2server.j2cache.cache.iginte;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;

public class IgniteInstance {
	private static IgniteInstance _instance = new IgniteInstance();
	private Ignite ignite;
	
	private IgniteInstance() {
	}
	
	public static IgniteInstance getInstance() {
		return _instance;
	}
	
	public synchronized Ignite getIgnite() {
		if (ignite == null) {
			ignite = Ignition.start();
		}
		
		return ignite;
	}
}
