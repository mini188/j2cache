package org.j2server.j2cache.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.util.IOUtils;

public class FastjsonSerializer implements ISerializer{
    private final static ParserConfig autoTypeSupportConfig = new ParserConfig();
    static {
        autoTypeSupportConfig.setAutoTypeSupport(true);
    }

    public byte[] serialize(Object obj) {
        if (obj == null) {
            return null;
        }

        return JSON.toJSONBytes(obj
                , SerializerFeature.WriteClassName
                , SerializerFeature.SkipTransientField
                , SerializerFeature.IgnoreErrorGetter
        );
    }

    public Object deserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        return JSON.parse(new String(bytes, IOUtils.UTF8), autoTypeSupportConfig);
    }

    @Override
    public <V> V deserialize(byte[] bytes, Class<V> clazz) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        return JSON.parseObject(new String(bytes, IOUtils.UTF8), clazz,autoTypeSupportConfig);
    }
}
