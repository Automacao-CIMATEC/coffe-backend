package org.coffee.domain.models;

import java.time.LocalDateTime;

public class VariableOpcUa {

    // Objeto de transferencia de dados (DTO) para variaveis OPC-UA
    // Usado para logica de negocio e API

    private Long id;
    private String name;
    private String dataType;
    private String unit;
    private String description;
    private Long deviceId;
    private String nodeId;
    private Integer namespaceIndex;
    private String nodeIdPrefix;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Construtor vazio
    public VariableOpcUa() {
    }

    // Construtor completo
    public VariableOpcUa(Long id, String name, String dataType, String unit, String description,
                         Long deviceId, String nodeId, Integer namespaceIndex, String nodeIdPrefix,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.dataType = dataType;
        this.unit = unit;
        this.description = description;
        this.deviceId = deviceId;
        this.nodeId = nodeId;
        this.namespaceIndex = namespaceIndex;
        this.nodeIdPrefix = nodeIdPrefix;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Construtor sem ID (para criacao)
    public VariableOpcUa(String name, String dataType, Long deviceId, String nodeId, Integer namespaceIndex) {
        this.name = name;
        this.dataType = dataType;
        this.deviceId = deviceId;
        this.nodeId = nodeId;
        this.namespaceIndex = namespaceIndex;
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
     * Verifica se o nodeId e valido
     * @return true se nodeId nao e nulo ou vazio
     */
    public boolean hasValidNodeId() {
        return this.nodeId != null && !this.nodeId.trim().isEmpty();
    }

    /**
     * Verifica se o namespace index e valido
     * @return true se e maior ou igual a zero
     */
    public boolean hasValidNamespaceIndex() {
        return this.namespaceIndex != null && this.namespaceIndex >= 0;
    }

    /**
     * Retorna o nodeId completo com prefixo
     * @return nodeId formatado
     */
    public String getFullNodeId() {
        String prefix = (this.nodeIdPrefix != null && !this.nodeIdPrefix.isEmpty())
                ? this.nodeIdPrefix : "";
        return prefix + this.nodeId;
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

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Integer getNamespaceIndex() {
        return namespaceIndex;
    }

    public void setNamespaceIndex(Integer namespaceIndex) {
        this.namespaceIndex = namespaceIndex;
    }

    public String getNodeIdPrefix() {
        return nodeIdPrefix;
    }

    public void setNodeIdPrefix(String nodeIdPrefix) {
        this.nodeIdPrefix = nodeIdPrefix;
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
        return "VariableOpcUa{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dataType='" + dataType + '\'' +
                ", unit='" + unit + '\'' +
                ", deviceId=" + deviceId +
                ", nodeId='" + nodeId + '\'' +
                ", namespaceIndex=" + namespaceIndex +
                ", nodeIdPrefix='" + nodeIdPrefix + '\'' +
                '}';
    }
}