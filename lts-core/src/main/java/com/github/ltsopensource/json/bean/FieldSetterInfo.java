package com.github.ltsopensource.json.bean;

import java.lang.reflect.Field;

/**
 * @author Robert HG (254963746@qq.com) on 12/31/15.
 */
public class FieldSetterInfo {

    private String fieldName;

    private Field field;

    public FieldSetterInfo(String fieldName, Field field) {
        this.fieldName = fieldName;
        this.field = field;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }
}
