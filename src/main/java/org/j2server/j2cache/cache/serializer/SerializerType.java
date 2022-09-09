package org.j2server.j2cache.cache.serializer;

public enum SerializerType {
    FASTJSON("fastjson");

    private String typeName;

    SerializerType(String typeName){
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}
