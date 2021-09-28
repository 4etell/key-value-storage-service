package com.foretell.kvpstorageservice.dto.req;

public class StorageDtoReq {
    private String key;
    private Object value;
    private Long ttlInMs;

    public StorageDtoReq() {

    }

    public StorageDtoReq(String key, Object value, Long ttlInMs) {
        this.key = key;
        this.value = value;
        this.ttlInMs = ttlInMs;
    }

    public StorageDtoReq(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public Long getTtlInMs() {
        return ttlInMs;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setTtlInMs(Long ttlInMs) {
        this.ttlInMs = ttlInMs;
    }
}