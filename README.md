# j2cache
在项目中经常会用到内存作为缓存，以提供高性能。本项目主要是针对缓存提供一种可扩展的思路，并且统一缓存的访问接口。项目的目的主要是key-value类型的内存数据缓存存储。

## 为此项目中实现的存储方案：
- jvm虚拟机内存
- redis
- iginte
- hazelcast (add 2016-10-13)

## 本地环境中写入1,000,000个对象的性能数据：

- jvmCache

测试写入缓存jvmCache  
总共耗时：2655  
每毫秒写入:376条  
每秒写入:376000条  

测试读取缓存jvmCache  
读取总共耗时：135  
每毫秒读取:7407条  
每秒读取:7407000条  

- hazelcastCache

开始测试写入缓存hazelcastCache  
总共耗时：15027  
每毫秒写入:66条  
每秒写入:66000条  

开始测试读取缓存hazelcastCache  
读取总共耗时：11422  
每毫秒读取:87条  
每秒读取:87000条  

- igniteCache

开始测试写入缓存igniteCache  
总共耗时：9603  
每毫秒写入:104条  
每秒写入:104000条  

开始测试读取缓存igniteCache  
读取总共耗时：2168  
每毫秒读取:461条  
每秒读取:461000条  

- redisCache

开始测试写入缓存redisCache  
总共耗时：40276ms  
每毫秒写入:24条  
每秒写入:24000条  

开始测试读取缓存redisCache  
读取总共耗时：37537  
每毫秒读取:26条  
每秒读取:26000条  


## 测试机器环境：

OS:Windows7 64位

CPU：I5-4210U @ 1.70GHZ 2.40GHZ

RAM:8G

> Redis用的是Redis on Windows Redis-x64-3.2.100

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