package org.coffee.domain.models;

import java.time.LocalDateTime;

public class Device {

    // Objeto de transferencia de dados (DTO)
    // Usado para logica de negocio e API

    private Long id;
    private String name;
    private String manufacturer;
    private String description;
    private Long plcId;
    private Long protocolId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Construtor vazio
    public Device() {
    }

    // Construtor completo
    public Device(Long id, String name, String manufacturer, String description,
                  Long plcId, Long protocolId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.manufacturer = manufacturer;
        this.description = description;
        this.plcId = plcId;
        this.protocolId = protocolId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Construtor sem ID (para criacao)
    public Device(String name, Long plcId, Long protocolId) {
        this.name = name;
        this.plcId = plcId;
        this.protocolId = protocolId;
    }

    // Metodos de logica de negocio

    /**
     * Verifica se o nome e valido
     * @return true se o nome tem pelo menos 3 caracteres
     */
    public boolean hasValidName() {
        return this.name != null &&
                this.name.trim().length() >= 3;
    }

    /**
     * Verifica se o dispositivo tem PLC associado
     * @return true se plcId nao e nulo
     */
    public boolean hasPlc() {
        return this.plcId != null;
    }

    /**
     * Verifica se o dispositivo tem protocolo associado
     * @return true se protocolId nao e nulo
     */
    public boolean hasProtocol() {
        return this.protocolId != null;
    }

    /**
     * Verifica se o dispositivo esta completamente configurado
     * @return true se tem nome, PLC e protocolo
     */
    public boolean isFullyConfigured() {
        return hasValidName() && hasPlc() && hasProtocol();
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

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getPlcId() {
        return plcId;
    }

    public void setPlcId(Long plcId) {
        this.plcId = plcId;
    }

    public Long getProtocolId() {
        return protocolId;
    }

    public void setProtocolId(Long protocolId) {
        this.protocolId = protocolId;
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
        return "Device{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", description='" + description + '\'' +
                ", plcId=" + plcId +
                ", protocolId=" + protocolId +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}