package org.coffee.domain.dtos;

public class RockwellWriteRequest extends RockwellReadRequest {
    private Object value;

    // Getters and Setters
    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}