package org.j2server.j2cache.serializer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SerializerManager {
    private static final SerializerManager me = new SerializerManager();

    private Map<String, ISerializer> serializerMap;

    private SerializerManager(){
        serializerMap = new ConcurrentHashMap<>();
        serializerMap.put(SerializerType.FASTJSON.getTypeName(), new FastjsonSerializer());
    }

    public static SerializerManager me() {
        return me;
    }

    public ISerializer getSerializer(String serializerName) {
        return serializerMap.get(serializerName);
    }
}
