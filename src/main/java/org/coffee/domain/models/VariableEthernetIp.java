package org.coffee.domain.models;

import java.time.LocalDateTime;

public class VariableEthernetIp {

    // Objeto de transferencia de dados (DTO) para variaveis Ethernet/IP
    // Usado para logica de negocio e API

    private Long id;
    private String name;
    private String dataType;
    private String unit;
    private String description;
    private Long deviceId;
    private String tagName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Construtor vazio
    public VariableEthernetIp() {
    }

    // Construtor completo
    public VariableEthernetIp(Long id, String name, String dataType, String unit, String description,
                              Long deviceId, String tagName,
                              LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.dataType = dataType;
        this.unit = unit;
        this.description = description;
        this.deviceId = deviceId;
        this.tagName = tagName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Construtor sem ID (para criacao)
    public VariableEthernetIp(String name, String dataType, Long deviceId, String tagName) {
        this.name = name;
        this.dataType = dataType;
        this.deviceId = deviceId;
        this.tagName = tagName;
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
     * Verifica se o nome da tag e valido
     * @return true se o nome da tag tem pelo menos 1 caractere
     */
    public boolean hasValidTagName() {
        return this.tagName != null &&
                this.tagName.trim().length() >= 1;
    }

    /**
     * Verifica se o tipo de dados e valido para Ethernet/IP
     * @return true se e um tipo conhecido
     */
    public boolean hasValidDataType() {
        if (this.dataType == null) return false;
        String type = this.dataType.toUpperCase();
        // FLOAT e sinonimo de REAL — ambos sao aceitos para compatibilidade com clientes
        return type.equals("BOOL") ||
                type.equals("BOOLEAN") ||
                type.equals("SINT") ||
                type.equals("INT") ||
                type.equals("DINT") ||
                type.equals("REAL") ||
                type.equals("FLOAT") ||
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

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
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
        return "VariableEthernetIp{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dataType='" + dataType + '\'' +
                ", unit='" + unit + '\'' +
                ", deviceId=" + deviceId +
                ", tagName='" + tagName + '\'' +
                '}';
    }
}