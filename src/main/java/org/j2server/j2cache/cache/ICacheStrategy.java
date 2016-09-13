package org.j2server.j2cache.cache;

@SuppressWarnings("rawtypes")
public interface ICacheStrategy {
   ICache createCache(String name, Class<?> keyClass, Class<?> valueCalss);
   void destroyCache(ICache cache);
}
