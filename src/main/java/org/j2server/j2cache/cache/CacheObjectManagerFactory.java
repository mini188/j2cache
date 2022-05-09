package org.j2server.j2cache.cache;

import org.apache.commons.lang.StringUtils;

public class CacheObjectManagerFactory {
	public static ICacheObjectManager createCacheObjectManager(String cacheObjectManagerClass)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		if (StringUtils.isNotEmpty(cacheObjectManagerClass)) {
			return (ICacheObjectManager) Class.forName(cacheObjectManagerClass).newInstance();
		}

		return new DefaultCacheObjectManager();
	}
}
