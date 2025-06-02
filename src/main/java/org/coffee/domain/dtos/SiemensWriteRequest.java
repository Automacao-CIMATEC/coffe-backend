package org.coffee.domain.dtos;

public class SiemensWriteRequest extends SiemensReadRequest {
    private Object value;

    // Getters and Setters
    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}