package org.coffee.domain.models;

import java.time.LocalDateTime;

public class VariableMqtt {

    // Objeto de transferencia de dados (DTO) para variaveis MQTT
    // Usado para logica de negocio e API

    private Long id;
    private String name;
    private String dataType;
    private String unit;
    private String description;
    private Long deviceId;
    private String topic;
    private Integer qos;
    private String clientId;
    private Boolean retained;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Construtor vazio
    public VariableMqtt() {
    }

    // Construtor completo
    public VariableMqtt(Long id, String name, String dataType, String unit, String description,
                        Long deviceId, String topic, Integer qos, String clientId, Boolean retained,
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.dataType = dataType;
        this.unit = unit;
        this.description = description;
        this.deviceId = deviceId;
        this.topic = topic;
        this.qos = qos;
        this.clientId = clientId;
        this.retained = retained;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Construtor sem ID (para criacao)
    public VariableMqtt(String name, String dataType, Long deviceId, String topic, Integer qos) {
        this.name = name;
        this.dataType = dataType;
        this.deviceId = deviceId;
        this.topic = topic;
        this.qos = qos;
        this.retained = false;
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
     * Verifica se o topico e valido
     * @return true se topico nao e nulo ou vazio
     */
    public boolean hasValidTopic() {
        return this.topic != null && !this.topic.trim().isEmpty();
    }

    /**
     * Verifica se o QoS e valido
     * @return true se QoS esta entre 0 e 2
     */
    public boolean hasValidQos() {
        return this.qos != null &&
                this.qos >= 0 &&
                this.qos <= 2;
    }

    /**
     * Verifica se a mensagem e retida
     * @return true se retained e true
     */
    public boolean isRetained() {
        return this.retained != null && this.retained;
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

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Integer getQos() {
        return qos;
    }

    public void setQos(Integer qos) {
        this.qos = qos;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Boolean getRetained() {
        return retained;
    }

    public void setRetained(Boolean retained) {
        this.retained = retained;
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
        return "VariableMqtt{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dataType='" + dataType + '\'' +
                ", unit='" + unit + '\'' +
                ", deviceId=" + deviceId +
                ", topic='" + topic + '\'' +
                ", qos=" + qos +
                ", clientId='" + clientId + '\'' +
                ", retained=" + retained +
                '}';
    }
}