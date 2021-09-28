package com.example.articledemo.domain;

public class EntityWithSmthInfo {

    private Object entity;
    private SmthInfo info;

    public EntityWithSmthInfo(Object entity, SmthInfo info) {
        this.entity = entity;
        this.info = info;
    }

    public Object getEntity() {
        return entity;
    }

    public void setEntity(FirstEntity entity) {
        this.entity = entity;
    }

    public SmthInfo getInfo() {
        return info;
    }

    public void setInfo(SmthInfo info) {
        this.info = info;
    }
}
