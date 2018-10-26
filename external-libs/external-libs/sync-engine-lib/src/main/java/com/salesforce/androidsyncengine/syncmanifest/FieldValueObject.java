package com.salesforce.androidsyncengine.syncmanifest;

import java.io.Serializable;

/**
 * Created by bduggirala on 11/16/15.
 */
public class FieldValueObject implements Serializable {

    private static final long serialVersionUID = 2L;

    private String field;
    private String fieldValue;

    public FieldValueObject(String field, String fieldValue) {
        this.field = field;
        this.fieldValue = fieldValue;
    }

    public String getField() {
        return field;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    @Override
    public String toString() {
        return field + ":" + fieldValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FieldValueObject that = (FieldValueObject) o;

        if (field != null ? !field.equals(that.field) : that.field != null) return false;
        return !(fieldValue != null ? !fieldValue.equals(that.fieldValue) : that.fieldValue != null);

    }

    @Override
    public int hashCode() {
        int result = field != null ? field.hashCode() : 0;
        result = 31 * result + (fieldValue != null ? fieldValue.hashCode() : 0);
        return result;
    }
}
