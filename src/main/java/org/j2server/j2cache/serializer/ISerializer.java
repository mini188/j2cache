package org.j2server.j2cache.serializer;

/**
 * 序列化
 */
public interface ISerializer {
    byte[] serialize(Object obj);
    Object deserialize(byte[] bytes);
    <V> V deserialize(byte[] bytes, Class<V> clazz);
}
