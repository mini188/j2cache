package org.j2server.j2cache.cache.redis.jedis;

import java.util.Set;

public interface IJedisWapper {
	public boolean exists(final byte[] key);
	
	public byte[] get(final byte[] key);
	
	public String set(final byte[] key, final byte[] value);
	
	public String setex(final byte[] key, final long seconds, final byte[] value);
	
	public long del(final byte[] key);
	
	public long unlink(final byte[] key);
	
	public long unlink(final byte[]... keys);
	
	public Set<byte[]> keys(final byte[] pattern);
}
