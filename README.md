# j2cache
在系统中经常会用缓存以提供更好的性能，缓存主要的数据结构也只是K-V。对于一些早期产品使用haspmap作为缓存，随着发展需要分布式部署，那么就会遇到分布式缓存的问题，自然就要使用redis/memcached之类的产品，但每一种产品都有自己一套的接口或者SDK，开发人员使用的时候需要专门改写代码才能使用。使用j2cache就可以简单的实现这种切换，j2cache实现了Map接口可以更友好的切换旧代码，这对于开发人员来说是透明的。

## 主要特点
1. 针对K-V缓存提供一种扩展的思路，并且统一缓存的访问接口，开发人员可以在系统中灵活的指定缓存的产品，也可以新增其他的缓存产品。

2. 创建缓存时指定缓存的策略，从而实现多种策略同时在代码中并存，比如开发人员可以在仅需要本地内存时指定本地缓存策略，如果分布式缓存时指定相应策略即可。

3. 可以设置缓存有效期，解决hashmap之类无法通过有效期淘汰缓存的问题

## 为此项目中实现的存储方案：
- jvm(ConcurrentHashMap)
- redis
- iginte
- hazelcast (add 2016-10-13)
- guava localcache(add 2020-08-25)

> 每种缓存策略都对应一种KV软件产品/组件，其中jvm/guava都是本地缓存策略，redis/iginte/hazelcast是分布式缓存。


## 未来想法
- CacheManager支持分布式
-  支持指标统计

## 使用方法示例：
```
public static void main(String[] args) {
	try {
		ICache<String, String> cache = CacheManager.getOrCreateCache(DefaultCacheStategy.class.getName(), "jvmCache", String.class, String.class);
		cache.put("key1", new Date().toString());
		
		String v = cache.get("key1");
		System.err.println("cache value: " +v);
	} catch (Exception e) {
		e.printStackTrace();
	}
}
```

> 另外可以参考src/test/java 下的测试代码