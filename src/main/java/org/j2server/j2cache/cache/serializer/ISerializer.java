package org.j2server.j2cache.cache.serializer;

/**
 * 序列化
 */
public interface ISerializer {
    byte[] serialize(Object obj);
    Object deserialize(byte[] bytes);
}
