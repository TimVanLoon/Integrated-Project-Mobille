package com.example.keiichi.project_mobile.POJO;


public class SingleValueLegacyExtendedProperty {

    private String id;
    private String value;

    public SingleValueLegacyExtendedProperty(String id, String value) {
        this.id = id;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
