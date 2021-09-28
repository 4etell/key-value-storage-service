package com.foretell.kvpstorageservice.data;

public class StoringData {
    private Object value;
    private long stampTtl;

    public StoringData() {}


    public StoringData(Object value, long stampTtl) {
        this.value = value;
        this.stampTtl = stampTtl;
    }

    public Object getValue() {
        return value;
    }

    public long getStampTtl() {
        return stampTtl;
    }
}