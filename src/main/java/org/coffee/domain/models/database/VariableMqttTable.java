package org.coffee.domain.models.database;

import jakarta.persistence.*;

@Entity
@Table(name = "variable_mqtt")
public class VariableMqttTable extends VariableTable {

    // Representa uma variavel especifica para protocolo MQTT
    // Herda campos comuns de VariableTable e adiciona campos especificos do MQTT

    @Column(name = "topic", nullable = false, length = 255)
    private String topic;

    @Column(name = "port", nullable = false)
    private Integer port;

    @Column(name = "qos", nullable = false)
    private Integer qos;

    @Column(name = "client_id", length = 100)
    private String clientId;

    @Column(name = "retained")
    private Boolean retained;

    // Construtor padrao obrigatorio para JPA
    public VariableMqttTable() {
        super();
    }

    // Construtor auxiliar
    public VariableMqttTable(String name, String dataType, DeviceTable device,
                             String topic, Integer port, Integer qos, String clientId) {
        super(name, dataType, device);
        this.topic = topic;
        this.port = port;
        this.qos = qos;
        this.clientId = clientId;
        this.retained = false;
    }

    // Getters e Setters

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
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
}