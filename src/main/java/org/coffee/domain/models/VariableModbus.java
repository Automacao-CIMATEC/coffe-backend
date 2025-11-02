package org.coffee.domain.models;

import java.time.LocalDateTime;

public class VariableModbus {

    // Objeto de transferencia de dados (DTO) para variaveis Modbus
    // Usado para logica de negocio e API

    private Long id;
    private String name;
    private String dataType;
    private String unit;
    private String description;
    private Long deviceId;
    private Integer address;
    private Integer port;
    private Integer unitId;
    private String registerType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Construtor vazio
    public VariableModbus() {
    }

    // Construtor completo
    public VariableModbus(Long id, String name, String dataType, String unit, String description,
                          Long deviceId, Integer address, Integer port, Integer unitId, String registerType,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.dataType = dataType;
        this.unit = unit;
        this.description = description;
        this.deviceId = deviceId;
        this.address = address;
        this.port = port;
        this.unitId = unitId;
        this.registerType = registerType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Construtor sem ID (para criacao)
    public VariableModbus(String name, String dataType, Long deviceId, Integer address, Integer port, Integer unitId, String registerType) {
        this.name = name;
        this.dataType = dataType;
        this.deviceId = deviceId;
        this.address = address;
        this.port = port;
        this.unitId = unitId;
        this.registerType = registerType;
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
     * Verifica se o endereco e valido
     * @return true se o endereco e maior ou igual a zero
     */
    public boolean hasValidAddress() {
        return this.address != null && this.address >= 0;
    }

    /**
     * Verifica se a porta e valida
     * @return true se a porta esta entre 1 e 65535
     */
    public boolean hasValidPort() {
        return this.port != null &&
                this.port > 0 &&
                this.port <= 65535;
    }

    /**
     * Verifica se o tipo de registrador e valido
     * @return true se e um tipo conhecido
     */
    public boolean hasValidRegisterType() {
        if (this.registerType == null) return false;
        String type = this.registerType.toUpperCase();
        return type.equals("HOLDING") ||
                type.equals("INPUT") ||
                type.equals("COIL") ||
                type.equals("DISCRETE");
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

    public Integer getAddress() {
        return address;
    }

    public void setAddress(Integer address) {
        this.address = address;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getUnitId() {
        return unitId;
    }

    public void setUnitId(Integer unitId) {
        this.unitId = unitId;
    }

    public String getRegisterType() {
        return registerType;
    }

    public void setRegisterType(String registerType) {
        this.registerType = registerType;
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
        return "VariableModbus{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dataType='" + dataType + '\'' +
                ", unit='" + unit + '\'' +
                ", deviceId=" + deviceId +
                ", address=" + address +
                ", port=" + port +
                ", unitId=" + unitId +
                ", registerType='" + registerType + '\'' +
                '}';
    }
}