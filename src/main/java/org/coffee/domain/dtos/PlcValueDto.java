package org.coffee.domain.dtos;

import org.coffee.domain.enums.PlcDataType;

public class PlcValueDto {
    private PlcDataType type;
    private Object value;

    // Add a default constructor
    public PlcValueDto() {
    }

    // Constructor for error messages (if needed)
    public PlcValueDto(String errorMessage) {
        this.value = errorMessage;
    }

    // Getters and Setters
    public PlcDataType getType() {
        return type;
    }

    public void setType(PlcDataType type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}