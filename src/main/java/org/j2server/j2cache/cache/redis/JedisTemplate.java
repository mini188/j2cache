package org.j2server.j2cache.cache.redis;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.j2server.j2cache.utils.PropsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;

import com.alibaba.fastjson.JSON;


/**
 * JedisTemplate提供了一个template方法，负责对Jedis连接的获取与归还。<br>
 * JedisAction&ltT&gt和 JedisNoResultAction&ltT&gt两种回调接口，适用于有无返回值两种情况。<br>
 * 1、最常用函数的封装, 如get/set/zadd等。<br>
 * 2、直接进行对象查询和缓存的封装。
 */
public class JedisTemplate {
	private static Logger logger = LoggerFactory.getLogger(JedisTemplate.class);
	private static JedisTemplate instance;
	private JedisSentinelPool jedisPool;
	//private  sentinelPool;
	private String encoding = "UTF-8";
	
	public static synchronized JedisTemplate getInstance() {
		instance = new JedisTemplate();
		return instance;
	}
	
	private JedisTemplate() {
    	Set<String> sentinels = new HashSet<String>();
    	String redisHost = PropsUtils.getRedisHost();
    	int redisPort = PropsUtils.getRedisPort();
    	String redisPassword = PropsUtils.getRedisPassword();
	    sentinels.add(new HostAndPort(redisHost, redisPort).toString());
	    jedisPool = new JedisSentinelPool("mymaster",sentinels,redisPassword);
	}

	/**
	 * 返回Jedis
	 */
	public Jedis getResource(){
		return jedisPool.getResource();
	}
	
	/**
	 * 执行有返回结果的action。没有封装的方法，请使用这个方法调用。
	 */
	public <T> T execute(JedisAction<T> jedisAction) throws JedisException {
		Jedis jedis = null;
		boolean broken = false;
		try {
			jedis = jedisPool.getResource();
			return jedisAction.action(jedis);
		} catch (JedisConnectionException e) {
			logger.error("Redis connection lost.", e);
			broken = true;
			throw e;
		} finally {
			closeResource(jedis, broken);
		}
	}

	/**
	 * 执行无返回结果的action。没有封装的方法，请使用这个方法调用。
	 */
	public void execute(JedisNoResultAction jedisAction) throws JedisException {
		Jedis jedis = null;
		boolean broken = false;
		try {
			jedis = jedisPool.getResource();
			jedisAction.action(jedis);
		} catch (JedisConnectionException e) {
			logger.error("Redis connection lost.", e);
			broken = true;
			throw e;
		} finally {
			closeResource(jedis, broken);
		}
	}

	/**
	 * 根据连接是否已中断的标志，分别调用returnBrokenResource或returnResource。
	 */
	public void closeResource(Jedis jedis, boolean connectionBroken) {
		if (jedis != null) {
			try {
				if (connectionBroken) {
					//jedisPool.returnBrokenResource(jedis);
					jedis.close();
				} else {
					//jedisPool.returnResource(jedis);
					jedis.close();
				}
			} catch (Exception e) {
				logger.error("Error happen when return jedis to pool, try to close it directly.", e);
				closeJedis(jedis);
			}
		}
	}
	
	/**
	 * 退出然后关闭Jedis连接。
	 */
	public static void closeJedis(Jedis jedis) {
		if (jedis.isConnected()) {
			try {
				try {
					jedis.quit();
				} catch (Exception e) {
				}
				jedis.disconnect();
			} catch (Exception e) {

			}
		}
	}
	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	
	public void setJedisPool(JedisSentinelPool jedisPool) {
		this.jedisPool = jedisPool;
	}
	
	/**
	 * 获取内部的pool做进一步的动作。
	 */
	public JedisSentinelPool getJedisPool() {
		return jedisPool;
	}

	/**
	 * 有返回结果的回调接口定义。
	 */
	public interface JedisAction<T> {
		T action(Jedis jedis);
	}

	/**
	 * 无返回结果的回调接口定义。
	 */
	public interface JedisNoResultAction {
		void action(Jedis jedis);
	}


	// ////////////// keys ///////////////////////////
	/**
	 * 按模糊匹配获取所以的keys
	 */
	public Set<String> getAllKeys(final String key) {
		return execute(new JedisAction<Set<String>>() {

			@Override
			public Set<String> action(Jedis jedis) {
				return jedis.keys(key);
			}
		});
	}
	
	/**
	 * 删除key, 如果key存在返回true, 否则返回false。
	 */
	public boolean del(final String key) {
		return execute(new JedisAction<Boolean>() {

			@Override
			public Boolean action(Jedis jedis) {
				return jedis.del(key) == 1 ? true : false;
			}
		});
	}

	/**
	 * 批量删除keys，如果key存在返回true, 否则返回false。
	 */
	public boolean del(final String... keys) {
		return execute(new JedisAction<Boolean>() {

			@Override
			public Boolean action(Jedis jedis) {
				return jedis.del(keys) == 1 ? true : false;
			}
		});
	}
	
	/**
	 * 删除当前DB中所有的键。该操作不会失败。
	 */
	public void flushDB() {
		execute(new JedisNoResultAction() {

			@Override
			public void action(Jedis jedis) {
				jedis.flushDB();
			}
		});
	}
	
	/**
	 * 获取默认DB信息。
	 */
	public String info() {
		return execute(new JedisAction<String>() {

			@Override
			public String action(Jedis jedis) {
				return jedis.info();
			}
		});
	}
	
	/**
	 * 获取指定DB的统计信息。
	 * @param select DB号
	 * @return 统计信息
	 */
	public String info(final String select) {
		return execute(new JedisAction<String>() {

			@Override
			public String action(Jedis jedis) {
				return jedis.info(select);
			}
		});
	}
	
	/**
	 * 后台flush数据到磁盘。立即返回OK。可以使用LASTSAVE命令检查是否成功。
	 * @return OK
	 */
	public String bgsave() {
		return execute(new JedisAction<String>() {

			@Override
			public String action(Jedis jedis) {
				return jedis.bgsave();
			}
		});
	}
	
	/**
	 * 返回最后一次成功保存数据到磁盘的UNIX时间戳。
	 * @return 成功保存数据日期
	 */
	public Date lastsave() {
		return execute(new JedisAction<Date>() {

			@Override
			public Date action(Jedis jedis) {
				long timestamp = jedis.lastsave();
				return new Date(timestamp);
			}
		});
	}
	
	/**
	 * 设置key的过期时间。
	 * @param key 键
	 * @param seconds 多长时间过期，单位秒
	 * @return 1成功设置，0过期时间被更新或者key不存在
	 */
	public Long expire(final String key, final int seconds) {
		return execute(new JedisAction<Long>() {
			
			@Override
			public Long action(Jedis jedis) {
				return jedis.expire(key, seconds);
			}
		});
	}
	
	/**
	 * 设置key在unixTime时间点过期。
	 * @param key 键
	 * @param unixTime 未来的某一时间
	 * @return 1成功设置，0过期时间被更新或者key不存在
	 */
	public Long expireAt(final String key, final long unixTime) {
		return execute(new JedisAction<Long>() {
			
			@Override
			public Long action(Jedis jedis) {
				return jedis.expireAt(key, unixTime);
			}
		});
	}
	
	/**
	 * 测试指定的key是否存在。
	 * @param key 键
	 * @return true是，false否
	 */
	public boolean exist(final String key) {
		return execute(new JedisAction<Boolean>() {

			@Override
			public Boolean action(Jedis jedis) {
				return jedis.exists(key);
			}
		});
	}
	
	/**
	 * 移动一个key到另一个DB。
	 * @param key 键
	 * @param dbIndex DB编号
	 * @return true成功，false失败
	 */
	public boolean move(final String key, final int dbIndex) {
		return execute(new JedisAction<Boolean>() {

			@Override
			public Boolean action(Jedis jedis) {
				return jedis.move(key, dbIndex) == 1 ? true : false;
			}
		});
	}
	
	/**
	 * 移除key的过期时间
	 * @param key 键
	 * @return true成功，false失败
	 */
	public boolean persist(final String key) {
		return execute(new JedisAction<Boolean>() {

			@Override
			public Boolean action(Jedis jedis) {
				return jedis.persist(key) == 1 ? true : false;
			}
		});
	}
	
	/**
	 * 获取key的有效秒数
	 * @param key 键
	 * @return 秒数
	 */
	public Long ttl(final String key) {
		return execute(new JedisAction<Long>() {

			@Override
			public Long action(Jedis jedis) {
				return jedis.ttl(key);
			}
		});
	}
	
	/**
	 * 获取key的有效毫秒数
	 * @param key 键
	 * @return 毫秒数
	 */
	public Long pttl(final String key) {
		return execute(new JedisAction<Long>() {

			@Override
			public Long action(Jedis jedis) {
				return jedis.pttl(key);
			}
		});
	}
	
	/**
	 * 重命名一个key，新的key必须是不存在的。
	 * @param oldKey 旧key
	 * @param newKey 新key
	 */
	public void renamenx(final String oldKey, final String newKey) {
		execute(new JedisNoResultAction() {
			
			@Override
			public void action(Jedis jedis) {
				jedis.renamenx(oldKey, newKey);
			}
		});
	}
	
	/**
	 * 获取key的类型，可能是"none", "string", "list", "set".不存在返回none
	 * @param key 键
	 */
	public String type(final String key) {
		return execute(new JedisAction<String>() {

			@Override
			public String action(Jedis jedis) {
				return jedis.type(key);
			}
		});
	}
	
	// ////////////// 关于String ///////////////////////////
	
	/**
	 * 追加一个值value到key上
	 * @param key 键
	 * @param value 值
	 * @return 操作后，总的字符长度
	 */
	public Long append(final String key, final String value) {
		return execute(new JedisAction<Long>() {

			@Override
			public Long action(Jedis jedis) {
				return jedis.append(key, value);
			}
		});
	}
	
	/**
	 * 追加一个值value到key上
	 * @param key 键
	 * @param value 值
	 * @return 操作后，总的字符长度
	 */
	public Long append(final byte[] key, final byte[] value) {
		return execute(new JedisAction<Long>() {

			@Override
			public Long action(Jedis jedis) {
				return jedis.append(key, value);
			}
		});
	}
	
	/**
	 * 如果key不存在, 返回null.
	 */
	public String get(final String key) {
		return execute(new JedisAction<String>() {

			@Override
			public String action(Jedis jedis) {
				return jedis.get(key);
			}
		});
	}
	
	/**
	 * 如果key不存在, 返回null.
	 */
	public byte[] get(final byte[] key) {
		return execute(new JedisAction<byte[]>() {

			@Override
			public byte[] action(Jedis jedis) {
				return jedis.get(key);
			}
		});
	}
	/**
	 * 获取所有key的值
	 * @param keys 多个key
	 * @return 所有key值
	 */
	public List<String> mget(final String... keys) {
		return execute(new JedisAction<List<String>>() {

			@Override
			public List<String> action(Jedis jedis) {
				return jedis.mget(keys);
			}
		});
	}
	
	/**
	 * 获取所有key的值
	 * @param keys 多个key
	 * @return 所有key值
	 */
	public List<byte[]> mget(final byte[]... keys) {
		return execute(new JedisAction<List<byte[]>>() {

			@Override
			public List<byte[]> action(Jedis jedis) {
				return jedis.mget(keys);
			}
		});
	}
	
	/**
	 * 如果key不存在, 返回0.
	 */
	public Long getAsLong(final String key) {
		String result = get(key);
		return result != null ? Long.valueOf(result) : 0;
	}

	/**
	 * 如果key不存在, 返回0.
	 */
	public Integer getAsInt(final String key) {
		String result = get(key);
		return result != null ? Integer.valueOf(result) : 0;
	}

	/**
	 * 设置key值为value，value最大为1GB。
	 * @param key 键
	 * @param value 值
	 */
	public void set(final String key, final String value) {
		execute(new JedisNoResultAction() {

			@Override
			public void action(Jedis jedis) {
				jedis.set(key, value);
			}
		});
	}
	
	/**
	 * 向缓存中设置对象
	 * @param key 
	 * @param value
	 */
    public void set(final String key,final Object value){
	  execute(new JedisNoResultAction() {
			@Override
			public void action(Jedis jedis) {
				 String objectJson = JSON.toJSONString(value);
			     jedis.set(key, objectJson);
			}
		});
  }
    
    /**
     * 根据key 获取对象
     * @param <T>
     * @param key
     * @return
     */
    public <T> T get(final String key, final Class<T> clazz){
    	 return execute(new JedisAction<T>() {
 			@Override
 			public  T action(Jedis jedis) {
 				String value = jedis.get(key);
 				if(null == value){
					return null;
				}
 		         return JSON.parseObject(value, clazz);
 			}
 		});
    }
    
	/**
	 * 设置key值为value，seconds秒后过期。
	 * @param key 键
	 * @param value 值
	 * @param seconds 过期时间，单位秒
	 */
	public void setex(final String key, final String value, final int seconds) {
		execute(new JedisNoResultAction() {

			@Override
			public void action(Jedis jedis) {
				jedis.setex(key, seconds, value);
			}
		});
	}
	
	/**
	 * 设置key值为value，seconds秒后过期。
	 * @param key 键
	 * @param value 值
	 * @param seconds 过期时间，单位秒
	 */
	public void setex(final String key, final Object value, final int seconds) {
		execute(new JedisNoResultAction() {

			@Override
			public void action(Jedis jedis) {
				 String objectJson = JSON.toJSONString(value);
			     jedis.setex(key, seconds,objectJson);
			}
		});
	}

	/**
	 * 如果key还不存在则进行设置，返回true，否则返回false.
	 */
	public boolean setnx(final String key, final String value) {
		return execute(new JedisAction<Boolean>() {

			@Override
			public Boolean action(Jedis jedis) {
				return jedis.setnx(key, value) == 1 ? true : false;
			}
		});
	}
	
	/**
	 * 批量设置多对键值对，只有当所有的键都不存在时，才成功。
	 * @param keysvalues 多对键值对，即String[]，每一个元素中间用空格分开
	 * @return true成功，false失败
	 */
	public boolean msetnx(final String... keysvalues) {
		if ((keysvalues.length % 2) != 0) {
			throw new RuntimeException("msetnx，键值对不完整。键值对总和是奇数。");
		}
		return execute(new JedisAction<Boolean>() {

			@Override
			public Boolean action(Jedis jedis) {
				return jedis.msetnx(keysvalues) == 1 ? true : false;
			}
		});
	}
	
	/**
	 * 将key对应的值递增1
	 * @param key 键
	 * @return 递增后的值
	 */
	public long incr(final String key) {
		return execute(new JedisAction<Long>() {

			@Override
			public Long action(Jedis jedis) {
				return jedis.incr(key);
			}
		});
	}

	/**
	 * 将key对应的值递增1
	 * @param key 键
	 * @param val 要递增的数
	 * @return 递增后的值
	 */
	public long incrBy(final String key, final long val) {
		return execute(new JedisAction<Long>() {

			@Override
			public Long action(Jedis jedis) {
				return jedis.incrBy(key, val);
			}
		});
	}
	
	/**
	 * 将key对应的值递增val
	 * @param key 键
	 * @param val 要递增的数
	 * @return 递增后的值
	 */
	public double incrByFloat(final String key, final double val) {
		return execute(new JedisAction<Double>() {

			@Override
			public Double action(Jedis jedis) {
				return jedis.incrByFloat(key, val);
			}
		});
	}
	
	/**
	 * 将key对应的值递减1
	 * @param key 键
	 * @return 递减后的值
	 */
	public long decr(final String key) {
		return execute(new JedisAction<Long>() {

			@Override
			public Long action(Jedis jedis) {
				return jedis.decr(key);
			}
		});
	}
	
	/**
	 * 将key对应的值递减val
	 * @param key 键
	 * @param val 要递减的数
	 * @return 递减后的值
	 */
	public long decrBy(final String key, final long val) {
		return execute(new JedisAction<Long>() {

			@Override
			public Long action(Jedis jedis) {
				return jedis.decrBy(key, val);
			}
		});
	}

	/**
	 * 设置key值为value，并返回原来的值。
	 * @param key 键
	 * @param value 值
	 * @return key对应的原来的值
	 */
	public String getSet(final String key, final String value) {
		return execute(new JedisAction<String>() {

			@Override
			public String action(Jedis jedis) {
				return jedis.getSet(key, value);
			}
		});
	}
	
	/**
	 * 设置key值为value，并且millis后过期。
	 * @param key 键
	 * @param seconds 过期时间，秒数
	 * @param value 值
	 */
	public void psetex(final String key, final int seconds, final String value) {
		execute(new JedisNoResultAction() {

			@Override
			public void action(Jedis jedis) {
				jedis.psetex(key, seconds * 1000L, value);
			}
		});
	}
	
	/**
	 * 获取指定key值的长度
	 * @param key 键
	 * @return key值长度
	 */
	public Long strlen(final String key) {
		return execute(new JedisAction<Long>() {

			@Override
			public Long action(Jedis jedis) {
				return jedis.strlen(key);
			}
		});
	}
	
	/**
	 * 获取key对应的字符串的子串
	 * @param key 键
	 * @param startOffset 开始偏移量
	 * @param endOffset 结束偏移量
	 * @return 子串
	 */
	public String getrange(final String key, final int startOffset, final int endOffset) {
		return execute(new JedisAction<String>() {

			@Override
			public String action(Jedis jedis) {
				return jedis.getrange(key, startOffset, endOffset);
			}
		});
	}
	
	// ////////////// List ///////////////////////////
	
	/**
	 * 从队列的头部（左边）弹出一个元素
	 * @param key 键
	 * @return 队列头第一个元素
	 */
	public String lpop(final String key) {
		return execute(new JedisAction<String>() {
			
			@Override
			public String action(Jedis jedis) {
				return jedis.lpop(key);
			}
		});
	}
	
	/**
	 * 从队列的尾部（右边）弹出一个元素
	 * @param key 键
	 * @return 队列尾第一个元素
	 */
	public String rpop(final String key) {
		return execute(new JedisAction<String>() {
			
			@Override
			public String action(Jedis jedis) {
				return jedis.rpop(key);
			}
		});
	}

	// since 3.0 
//	/**
//	 * 从队列的头部（左边）弹出一个元素，阻塞直到有可用元素。队列头第一个元素
//	 * @param key 键
//	 * @return 是一个2元素数组，包含[key, value]
//	 */
//	public List<String> blpop(final String key) {
//		return execute(new JedisAction<List<String>>() {
//			
//			@Override
//			public List<String> action(Jedis jedis) {
//				return jedis.blpop(key);
//			}
//		});
//	}
//	
//	/**
//	 * 从队列的头部（左边）弹出一个元素，阻塞直到有可用元素。
//	 * @param key 键，可以多个
//	 * @return 是一个2元素数组，包含[key, value]
//	 */
//	public List<String> blpop(final String... key) {
//		return execute(new JedisAction<List<String>>() {
//			
//			@Override
//			public List<String> action(Jedis jedis) {
//				return jedis.blpop(key);
//			}
//		});
//	}
	
	/**
	 * 从队列的头部（左边）弹出一个元素，阻塞直到有可用元素，超时返回nil。多个队列，弹出第一个非空队列，头元素。
	 * @param timeout 超时时间，单位秒
	 * @param key 键，可以多个
	 * @return 队列头第一个元素，是一个2元素数组，包含[key, value]
	 */
	public List<String> blpop(final int timeout, final String... key) {
		return execute(new JedisAction<List<String>>() {
			
			@Override
			public List<String> action(Jedis jedis) {
				return jedis.blpop(timeout, key);
			}
		});
	}
	
//	/**
//	 * 从队列的尾部（右边）弹出一个元素，阻塞直到有可用元素
//	 * @param key 键
//	 * @return 队列尾第一个元素，是一个2元素数组，包含[key, value]
//	 */
//	public List<String> brpop(final String key) {
//		return execute(new JedisAction<List<String>>() {
//			
//			@Override
//			public List<String> action(Jedis jedis) {
//				return jedis.brpop(key);
//			}
//		});
//	}
	
	/**
	 * 从队列的尾部（右边）弹出一个元素，阻塞直到有可用元素，多个队列，弹出第一个非空队列，尾元素。
	 * @param key 键，可以多个
	 * @return 队列尾第一个元素，是一个2元素数组，包含[key, value]
	 */
	public List<String> brpop(final String... key) {
		return execute(new JedisAction<List<String>>() {
			
			@Override
			public List<String> action(Jedis jedis) {
				return jedis.brpop(key);
			}
		});
	}
	
	/**
	 * 从队列的尾部（右边）弹出一个元素，阻塞直到有可用元素，超时后返回nil。多个队列，弹出第一个非空队列，尾元素。
	 * @param timeout 超时时间，单位秒
	 * @param key 键，可以多个
	 * @return 队列尾第一个元素，是一个2元素数组，包含[key, value]
	 */
	public List<String> brpop(final int timeout, final String... key) {
		return execute(new JedisAction<List<String>>() {
			
			@Override
			public List<String> action(Jedis jedis) {
				return jedis.brpop(timeout, key);
			}
		});
	}
	
	/**
	 * 添加String值到List的头部（左边），List不存在将创建，key类型不是list时抛出异常。
	 * @param key 键
	 * @param value 值，可多个,即String[]
	 * @return 操作后List长度
	 */
	public Long lpush(final String key, final String... value) {
		return execute(new JedisAction<Long>() {

			@Override
			public Long action(Jedis jedis) {
				return jedis.lpush(key, value);
			}
		});
	}

	/**
	 * 添加String值到List的尾部（右边），List不存在将创建，key类型不是list时抛出异常。
	 * @param key 键
	 * @param value 值，可多个,即String[]
	 * @return 操作后List长度
	 */
	public Long rpush(final String key, final String... value) {
		return execute(new JedisAction<Long>() {

			@Override
			public Long action(Jedis jedis) {
				return jedis.rpush(key, value);
			}
		});
	}
	
	/**
	 * 删除列表中的最后（右边）一个元素，将其追加到另一个列表的头部（左边）
	 * @param srckey 源key
	 * @param dstkey 目标key
	 * @return 源key最后一个元素，为空时返回nil
	 */
	public String rpoplpush(final String srckey, final String dstkey) {
		return execute(new JedisAction<String>() {

			@Override
			public String action(Jedis jedis) {
				return jedis.rpoplpush(srckey, dstkey);
			}
		});
	}
	
	/**
	 * 删除列表中的最后（右边）一个元素，将其追加到另一个列表的头部（左边），阻塞直到有可用元素，超时返回nil
	 * @param srckey 源key
	 * @param dstkey 目标key
	 * @param timeout 超时时间，单位秒
	 * @return 源key最后一个元素，为空时返回nil
	 */
	public String brpoplpush(final String srckey, final String dstkey, final int timeout) {
		return execute(new JedisAction<String>() {

			@Override
			public String action(Jedis jedis) {
				return jedis.brpoplpush(srckey, dstkey, timeout);
			}
		});
	}
	
	/**
	 * 获取列表指定索引处的值
	 * @param key 键
	 * @param index 索引
	 * @return 该索引处的值
	 */
	public String lindex(final String key, final long index) {
		return execute(new JedisAction<String>() {

			@Override
			public String action(Jedis jedis) {
				return jedis.lindex(key, index);
			}
		});
	}
	
	/**
	 * 设置列表指定索引处的值
	 * @param key 键
	 * @param index 索引
	 * @param value 值
	 * @return
	 */
	public String lset(final String key, final long index, final String value) {
		return execute(new JedisAction<String>() {

			@Override
			public String action(Jedis jedis) {
				return jedis.lset(key, index, value);
			}
		});
	}
	
	/**
	 * 获取列表从start到end索引处的元素，索引从0开始，-1指最后一个，-2倒数第二个，以此类推。<br>
	 * 子列表包含start和end位置处的元素。
	 * @param key 键
	 * @param start 开始索引
	 * @param end 结束索引
	 * @return 子列表
	 */
	public List<String> lrange(final String key, final long start, final long end) {
		return execute(new JedisAction<List<String>>() {

			@Override
			public List<String> action(Jedis jedis) {
				return jedis.lrange(key, start, end);
			}
		});
	}
	
	/**
	 * 修剪列表，只有在start到end直接的元素会被保留。索引从0开始，-1指最后一个，-2倒数第二个，以此类推。
	 * @param key 键
	 * @param start 开始索引
	 * @param end 结束索引
	 * @return 
	 */
	public String ltrim(final String key, final long start, final long end) {
		return execute(new JedisAction<String>() {

			@Override
			public String action(Jedis jedis) {
				return jedis.ltrim(key, start, end);
			}
		});
	}
	
	/**
	 * 返回List长度, key不存在时返回0，key类型不是list时抛出异常.
	 */
	public long llen(final String key) {
		return execute(new JedisAction<Long>() {

			@Override
			public Long action(Jedis jedis) {
				return jedis.llen(key);
			}
		});
	}

	/**
	 * 删除List中的第一个等于value的元素，value不存在或key不存在时返回0.
	 */
	public boolean lremOne(final String key, final String value) {
		return execute(new JedisAction<Boolean>() {
			@Override
			public Boolean action(Jedis jedis) {
				Long count = jedis.lrem(key, 1, value);
				return (count == 1);
			}
		});
	}

	/**
	 * 删除List中的所有等于value的元素，value不存在或key不存在时返回0.
	 */
	public boolean lremAll(final String key, final String value) {
		return execute(new JedisAction<Boolean>() {
			@Override
			public Boolean action(Jedis jedis) {
				Long count = jedis.lrem(key, 0, value);
				return (count > 0);
			}
		});
	}

	//*********************** hash *****************************//
	
	/**
	 * 设置hash中某个字段的值，如果hash不存在，将新建。
	 * @param key 键
	 * @param field 字段
	 * @param value 值
	 * @return 字段存在，则更新，返回0；不存在新增，返回1
	 */
	public Long hset(final String key, final String field, final String value) {
		return execute(new JedisAction<Long>() {

			@Override
			public Long action(Jedis jedis) {
				return jedis.hset(key, field, value);
			}
		});
	}
	
	/**
	 * 设置hash中某个字段的值，如果hash不存在，将新建。
	 * @param key 键
	 * @param field 字段
	 * @param value 值
	 * @return 字段存在，则更新，返回0；不存在新增，返回1
	 */
	public <T> T hset(final String key, final String field, final T value,final Class<T> clazz) {
		return execute(new JedisAction<T>() {

			@Override
			public T action(Jedis jedis) {
				String objectJson = JSON.toJSONString(value); 
				jedis.hset(key, field, objectJson);
				return null;
			}
		});
	}
	
	/**
	 * 设置hash中某个字段的值。
	 * @param key 键
	 * @param field 字段
	 * @param value 值
	 * @return 字段存在，直接返回0；不存在则新增，返回1
	 */
	public Long hsetnx(final String key, final String field, final String value) {
		return execute(new JedisAction<Long>() {

			@Override
			public Long action(Jedis jedis) {
				return jedis.hsetnx(key, field, value);
			}
		});
	}
	
	/**
	 * 批量设置hash字段值。如果hash不存在将新建。如果字段存在，则更新字段值。
	 * @param key 键
	 * @param hash Map
	 * @return OK
	 */
	public String hmset(final String key, final Map<String, String> hash) {
		return execute(new JedisAction<String>() {

			@Override
			public String action(Jedis jedis) {
				return jedis.hmset(key, hash);
			}
		});
	}
	
	/**
	 * 批量设置hash字段值。如果hash不存在将新建。如果字段存在，则更新字段值。
	 * @param key 键
	 * @param hash Map
	 * @return OK
	 */
	public String hmset(final byte[] key, final Map<byte[], byte[]> hash) {
		return execute(new JedisAction<String>() {

			@Override
			public String action(Jedis jedis) {
				return jedis.hmset(key, hash);
			}
		});
	}
	
	/**
	 * 获取hash字段值。没有或者字段不存在，返回nil。
	 * @param key 键
	 * @param field 字段
	 * @return 字段值
	 */
	public String hget(final String key, final String field) {
		return execute(new JedisAction<String>() {

			@Override
			public String action(Jedis jedis) {
				return jedis.hget(key, field);
			}
		});
	}
	
	/**
	 * 获取hash字段值,并解析成响应的Class,没有或者字段不存在，返回null。
	 * @param key 键
	 * @param field 字段
	 * @return 字段值
	 */
	public <T> T hget(final String key, final String field,final Class<T> clazz) {
		return execute(new JedisAction<T>() {

			@Override
			public T action(Jedis jedis) {
				String value= jedis.hget(key, field);
				if(null == value){
					return null;
				}
				return JSON.parseObject(value, clazz);
			}
		});
	}
	/**
	 * 获取hash中多个字段的值
	 * @param key 键
	 * @param fields 多个字段
	 * @return 多个字段对应的值
	 */
	public List<String> hmget(final String key, final String... fields) {
		return execute(new JedisAction<List<String>>() {

			@Override
			public List<String> action(Jedis jedis) {
				return jedis.hmget(key, fields);
			}
		});
	}
	
	/**
	 * 获取hash所有字段值。
	 * @param key 键
	 * @return Map，hash所有字段和值
	 */
	public Map<String, String> hgetAll(final String key) {
		return execute(new JedisAction<Map<String, String>>() {

			@Override
			public Map<String, String> action(Jedis jedis) {
				return jedis.hgetAll(key);
			}
		});
	}
	
	/**
	 * 获取hash所有字段值。
	 * @param <K>
	 * @param <V>
	 * @param key 键
	 * @return Map，hash所有字段和值
	 */
	public Set<Entry<String, String>> hgetAllEntry(final String key) {
		return execute(new JedisAction<Set<Entry<String, String>>>() {

			@Override
			public Set<Entry<String, String>> action(Jedis jedis) {
				return jedis.hgetAll(key).entrySet();
			}
		});
	}
	
	/**
	 * hash中某个字段是否存在
	 * @param key 键
	 * @param field 字段
	 * @return true存在，false不存在
	 */
	public boolean hexists(final String key, final String field) {
		return execute(new JedisAction<Boolean>() {

			@Override
			public Boolean action(Jedis jedis) {
				return jedis.hexists(key, field);
			}
		});
	}
	
	/**
	 * 获取hash里所有字段的数量
	 * @param key 键
	 * @return 所有字段的数量
	 */
	public Long hlen(final String key) {
		return execute(new JedisAction<Long>() {

			@Override
			public Long action(Jedis jedis) {
				return jedis.hlen(key);
			}
		});
	}
	
	/**
	 * 删除hash中多个字段
	 * @param key 键
	 * @param fields 字段
	 * @return 字段存在返回1，否则0
	 */
	public Long hdel(final String key, final String... fields) {
		return execute(new JedisAction<Long>() {

			@Override
			public Long action(Jedis jedis) {
				return jedis.hdel(key, fields);
			}
		});
	}
	
	/**
	 * 获取hash中所有字段
	 * @param key 键
	 * @return 所有字段
	 */
	public Set<String> hkeys(final String key) {
		return execute(new JedisAction<Set<String>>() {

			@Override
			public Set<String> action(Jedis jedis) {
				return jedis.hkeys(key);
			}
		});
	}
	
	/**
	 * 获取hash中所有值
	 * @param key 键
	 * @return 所有字段值
	 */
	public List<String> hvals(final String key) {
		return execute(new JedisAction<List<String>>() {

			@Override
			public List<String> action(Jedis jedis) {
				return jedis.hvals(key);
			}
		});
	}
	
	/**
	 * 将hash中指定字段值增加value。
	 * @param key 键
	 * @param field 字段
	 * @param value 要增加的值
	 * @return 字段增加后的值
	 */
	public Long hincrBy(final String key, final String field, final long value) {
		return execute(new JedisAction<Long>() {

			@Override
			public Long action(Jedis jedis) {
				return jedis.hincrBy(key, field, value);
			}
		});
	}
	
	/**
	 * 将hash中指定字段值增加value。
	 * @param key 键
	 * @param field 字段
	 * @param value 要增加的值
	 * @return 字段增加后的值
	 */
	public Double hincrByFloat(final String key, final String field, final double value) {
		return execute(new JedisAction<Double>() {

			@Override
			public Double action(Jedis jedis) {
				return jedis.hincrByFloat(key, field, value);
			}
		});
	}
	
	//************************* Set ********************************//
	
	/**
	 * 添加一个或多个元素到集合里，集合不存在则创建。
	 * @param key 键
	 * @param members 集合成员
	 * @return 0元素已存在，1添加成功
	 */
	public Long sadd(final String key, final String... members) {
		return execute(new JedisAction<Long>() {

			@Override
			public Long action(Jedis jedis) {
				return jedis.sadd(key, members);
			}
		});
	}
	
	/**
	 * 获取集合中元素数量
	 * @param key 键
	 * @return 元素数量
	 */
	public Long scard(final String key) {
		return execute(new JedisAction<Long>() {

			@Override
			public Long action(Jedis jedis) {
				return jedis.scard(key);
			}
		});
	}
	
	/**
	 * 删除集合中一个或多个元素。
	 * @param key 键
	 * @param members 元素
	 * @return 1成功删除，0元素不存在
	 */
	public Long srem(final String key, final String... members) {
		return execute(new JedisAction<Long>() {

			@Override
			public Long action(Jedis jedis) {
				return jedis.srem(key, members);
			}
		});
	}
	
	/**
	 * 获取集合中所有元素
	 * @param key 键
	 * @return 所有元素
	 */
	public Set<String> smembers(final String key) {
		return execute(new JedisAction<Set<String>>() {

			@Override
			public Set<String> action(Jedis jedis) {
				return jedis.smembers(key);
			}
		});
	}
	
	/**
	 * 确定元素是否是集合的成员。
	 * @param key 键
	 * @param member 成员
	 * @return true是，false否
	 */
	public boolean sismember(final String key, final String member) {
		return execute(new JedisAction<Boolean>() {

			@Override
			public Boolean action(Jedis jedis) {
				return jedis.sismember(key, member);
			}
		});
	}
	
	/**
	 * 返回所有集合的差集。不存在的集合被认为是空集。
	 * @param keys 所有集合键
	 * @return 差集的集合
	 */
	public Set<String> sdiff(final String... keys) {
		return execute(new JedisAction<Set<String>>() {

			@Override
			public Set<String> action(Jedis jedis) {
				return jedis.sdiff(keys);
			}
		});
	}
	
	/**
	 * 执行所有集合的差集，并存入目标集合。不存在的集合被认为是空集。
	 * @param dstkey 目标集合
	 * @param keys 所有集合键
	 * @return 结果集中的元素数量
	 */
	public Long sdiffstore(final String dstkey, final String... keys) {
		return execute(new JedisAction<Long>() {

			@Override
			public Long action(Jedis jedis) {
				return jedis.sdiffstore(dstkey, keys);
			}
		});
	}
	
	/**
	 * 返回所有集合的交集。不存在的集合被认为是空集。
	 * @param keys 所有集合键
	 * @return 交集的集合
	 */
	public Set<String> sinter(final String... keys) {
		return execute(new JedisAction<Set<String>>() {

			@Override
			public Set<String> action(Jedis jedis) {
				return jedis.sinter(keys);
			}
		});
	}
	
	/**
	 * 执行所有集合的交集，并存入目标集合。不存在的集合被认为是空集。
	 * @param dstkey 目标集合
	 * @param keys 所有集合键
	 * @return 结果集中的元素数量
	 */
	public Long sinterstore(final String dstkey, final String... keys) {
		return execute(new JedisAction<Long>() {

			@Override
			public Long action(Jedis jedis) {
				return jedis.sinterstore(dstkey, keys);
			}
		});
	}
	
	/**
	 * 返回所有集合的并集。不存在的集合被认为是空集。
	 * @param keys 所有集合键
	 * @return 并集的集合
	 */
	public Set<String> sunion(final String... keys) {
		return execute(new JedisAction<Set<String>>() {

			@Override
			public Set<String> action(Jedis jedis) {
				return jedis.sunion(keys);
			}
		});
	}
	
	/**
	 * 执行所有集合的并集，并存入目标集合。不存在的集合被认为是空集。
	 * @param dstkey 目标集合
	 * @param keys 所有集合键
	 * @return 结果集中的元素数量
	 */
	public Long sunionstore(final String dstkey, final String... keys) {
		return execute(new JedisAction<Long>() {

			@Override
			public Long action(Jedis jedis) {
				return jedis.sunionstore(dstkey, keys);
			}
		});
	}
	
	/**
	 * 将元素member从srckey集合移动到dstkey集合。
	 * @param srckey 源集合键
	 * @param dstkey 目标集合键
	 * @param member 成员
	 * @return true成功，false失败
	 */
	public boolean smove(final String srckey, final String dstkey, final String member) {
		return execute(new JedisAction<Boolean>() {

			@Override
			public Boolean action(Jedis jedis) {
				return jedis.smove(srckey, dstkey, member) == 1 ? true : false;
			}
		});
	}
	
	/**
	 * 随机弹出集合中的一个元素并删除。
	 * @param key 键
	 * @return 被弹出的元素
	 */
	public String spop(final String key) {
		return execute(new JedisAction<String>() {

			@Override
			public String action(Jedis jedis) {
				return jedis.spop(key);
			}
		});
	}
	
	/**
	 * 随机弹出集合中的一个元素。
	 * @param key 键
	 * @return 被弹出的元素
	 */
	public String srandmember(final String key) {
		return execute(new JedisAction<String>() {

			@Override
			public String action(Jedis jedis) {
				return jedis.srandmember(key);
			}
		});
	}
	
	/**
	 * 随机弹出集合中的count个元素。如果count大于集合大小，则弹出全部元素，如果count为负数，讲弹出count绝对值个元素，元素可能重复。
	 * @param key 键
	 * @param count 被弹出的元素数
	 * @return 被弹出的元素
	 */
	public List<String> srandmember(final String key, final int count) {
		return execute(new JedisAction<List<String>>() {

			@Override
			public List<String> action(Jedis jedis) {
				return jedis.srandmember(key, count);
			}
		});
	}
	
	
	//************************** Sorted Set *******************************//
	
	/**
	 * 加入Sorted set, 如果member在Set里已存在，只更新score并返回false,否则返回true.
	 */
	public boolean zadd(final String key, final String member, final double score) {
		return execute(new JedisAction<Boolean>() {

			@Override
			public Boolean action(Jedis jedis) {
				return jedis.zadd(key, score, member) == 1 ? true : false;
			}
		});
	}

	/**
	 * 返回Set大小, key不存在时返回0，key类型不是sorted set时抛出异常.
	 */
	public long zcard(final String key) {
		return execute(new JedisAction<Long>() {

			@Override
			public Long action(Jedis jedis) {
				return jedis.zcard(key);
			}
		});
	}
	
	/**
	 * 删除sorted set中的元素，成功删除返回true，key或member不存在返回false。
	 */
	public boolean zrem(final String key, final String member) {
		return execute(new JedisAction<Boolean>() {

			@Override
			public Boolean action(Jedis jedis) {
				return jedis.zrem(key, member) == 1 ? true : false;
			}
		});
	}

	
	
	//************************* 事务 ************************//
	
	/**
	 * 开始事务
	 */
	public Transaction multi() {
		return execute(new JedisAction<Transaction>() {

			@Override
			public Transaction action(Jedis jedis) {
				return jedis.multi();
			}
		});
	}
	
	/**
	 * 丢弃multi之后所有的命令。调用multi()后会返回Transaction，可直接调用。不需要调用此方法
	 */
	public String discard(final Transaction tx) {
		return tx.discard();
	}
	
	/**
	 * 执行multi之后所有的命令。调用multi()后会返回Transaction，可直接调用。不需要调用此方法
	 */
	public List<Object> exec(final Transaction tx) {
		return tx.exec();
	}
	
	/**
	 * 取消事务
	 */
	public String unwatch() {
		return execute(new JedisAction<String>() {

			@Override
			public String action(Jedis jedis) {
				return jedis.unwatch();
			}
		});
	}
	
	/**
	 * 锁定key直到执行了multi/exec命令
	 */
	public String watch(final String... keys) {
		return execute(new JedisAction<String>() {

			@Override
			public String action(Jedis jedis) {
				return jedis.watch(keys);
			}
		});
	} 
	
	//************************* 连接 ************************//
	
	/**
	 * 校验密码。因为 Redis高性能的特点，在很短时间内尝试猜测非常多个密码是有可能的，因此<br>
	 * 请确保使用的密码足够复杂和足够长，以免遭受密码猜测攻击。<br>
	 * 通过设置配置文件中 requirepass 项的值(使用命令 CONFIG SET requirepass password )，<br>
	 * 可以使用密码来保护 Redis服务器。如果开启了密码保护的话，在每次连接 Redis服务器之后，<br>
	 * 就要使用 AUTH命令解锁，解锁之后才能使用其他 Redis 命令。
	 * @param password 密码
	 * @return 验证成功返回OK，否则返回错误码
	 */
	public String auth(final String password) {
		return execute(new JedisAction<String>() {

			@Override
			public String action(Jedis jedis) {
				return jedis.auth(password);
			}
		});
	}
	
	/**
	 * ping服务器是否正常。常用于测试。
	 * @return ping通了返回PONG
	 */
	public String ping() {
		return execute(new JedisAction<String>() {

			@Override
			public String action(Jedis jedis) {
				return jedis.ping();
			}
		});
	}
	
	/**
	 * 切换到指定的数据库，数据库索引号 index从0 开始。
	 * 默认使用 0 号数据库。
	 * @param index 数据库索引号，从0开始
	 */
	public String select(final int index) {
		return execute(new JedisAction<String>() {

			@Override
			public String action(Jedis jedis) {
				return jedis.select(index);
			}
		});
	}
	
	/**
	 * 测试服务器，回显message字符。
	 * @param message 传入和回显的字符。
	 */
	public String echo(final String message) {
		return execute(new JedisAction<String>() {

			@Override
			public String action(Jedis jedis) {
				return jedis.echo(message);
			}
		});
	}
	
	/**
	 * 请求服务器关闭与当前客户端的连接。
	 * 一旦所有等待中的回复(如果有的话)顺利写入到客户端，连接就会被关闭
	 * @return OK
	 */
	public String quit() {
		return execute(new JedisAction<String>() {

			@Override
			public String action(Jedis jedis) {
				return jedis.quit();
			}
		});
	}
	
}
