package org.j2server.j2cache.cache;

@SuppressWarnings("rawtypes")
public interface ICacheStrategy {
   ICache createCache(String name, Class<?> keyClass, Class<?> valueCalss);
   ICache createCache(String name, Class<?> keyClass, Class<?> valueCalss, long maxSize, long maxLifetime);
   void destroyCache(ICache cache);
}
