package org.coffee.domain.models;

import java.time.LocalDateTime;

public class VariableSiemensS7 {

    // Objeto de transferencia de dados (DTO) para variaveis Siemens S7
    // Usado para logica de negocio e API

    private Long id;
    private String name;
    private String dataType;
    private String unit;
    private String description;
    private Long deviceId;
    private Integer dbNumber;
    private Integer offset;
    private Integer bitOffset;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Construtor vazio
    public VariableSiemensS7() {
    }

    // Construtor completo
    public VariableSiemensS7(Long id, String name, String dataType, String unit, String description,
                             Long deviceId, Integer dbNumber, Integer offset, Integer bitOffset,
                             LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.dataType = dataType;
        this.unit = unit;
        this.description = description;
        this.deviceId = deviceId;
        this.dbNumber = dbNumber;
        this.offset = offset;
        this.bitOffset = bitOffset;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Construtor sem ID (para criacao)
    public VariableSiemensS7(String name, String dataType, Long deviceId, Integer dbNumber, Integer offset, Integer bitOffset) {
        this.name = name;
        this.dataType = dataType;
        this.deviceId = deviceId;
        this.dbNumber = dbNumber;
        this.offset = offset;
        this.bitOffset = bitOffset;
    }

    // Metodos de logica de negocio

    /**
     * Verifica se o nome e valido
     * @return true se o nome tem pelo menos 2 caracteres
     */
    public boolean hasValidName() {
        return this.name != null &&
                this.name.trim().length() >= 2;
    }

    /**
     * Verifica se o numero da DB e valido
     * @return true se o numero da DB e maior ou igual a zero
     */
    public boolean hasValidDbNumber() {
        return this.dbNumber != null && this.dbNumber >= 0;
    }

    /**
     * Verifica se o offset e valido
     * @return true se o offset e maior ou igual a zero
     */
    public boolean hasValidOffset() {
        return this.offset != null && this.offset >= 0;
    }

    /**
     * Verifica se o bit offset e valido
     * @return true se o bit offset esta entre 0 e 7
     */
    public boolean hasValidBitOffset() {
        return this.bitOffset != null &&
                this.bitOffset >= 0 &&
                this.bitOffset <= 7;
    }

    /**
     * Verifica se o tipo de dados e valido para Siemens S7
     * @return true se e um tipo conhecido
     */
    public boolean hasValidDataType() {
        if (this.dataType == null) return false;
        String type = this.dataType.toUpperCase();
        return type.equals("BOOL") ||
                type.equals("BOOLEAN") ||
                type.equals("BYTE") ||
                type.equals("WORD") ||
                type.equals("DWORD") ||
                type.equals("INT") ||
                type.equals("DINT") ||
                type.equals("REAL") ||
                type.equals("STRING");
    }

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getDbNumber() {
        return dbNumber;
    }

    public void setDbNumber(Integer dbNumber) {
        this.dbNumber = dbNumber;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getBitOffset() {
        return bitOffset;
    }

    public void setBitOffset(Integer bitOffset) {
        this.bitOffset = bitOffset;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "VariableSiemensS7{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dataType='" + dataType + '\'' +
                ", unit='" + unit + '\'' +
                ", deviceId=" + deviceId +
                ", dbNumber=" + dbNumber +
                ", offset=" + offset +
                ", bitOffset=" + bitOffset +
                '}';
    }
}