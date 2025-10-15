package org.coffee.domain.models;

import java.time.LocalDateTime;

public class Plc {

    // Objeto de transferencia de dados (DTO)
    // Usado para logica de negocio e API

    private Long id;
    private String name;
    private String ip;
    private Integer port;
    private String manufacturer;
    private String model;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Construtor vazio
    public Plc() {
    }

    // Construtor completo
    public Plc(Long id, String name, String ip, Integer port, String manufacturer,
               String model, String description, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.ip = ip;
        this.port = port;
        this.manufacturer = manufacturer;
        this.model = model;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Construtor sem ID (para criacao)
    public Plc(String name, String ip, Integer port) {
        this.name = name;
        this.ip = ip;
        this.port = port;
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
     * Verifica se o IP e valido (validacao simples)
     * @return true se o IP parece valido
     */
    public boolean hasValidIp() {
        if (this.ip == null || this.ip.trim().isEmpty()) {
            return false;
        }
        String[] parts = this.ip.split("\\.");
        return parts.length == 4;
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
     * Retorna a conexao formatada
     * @return string no formato ip:porta
     */
    public String getConnectionString() {
        return this.ip + ":" + this.port;
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

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
        return "Plc{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", manufacturer='" + manufacturer + '\'' +
                ", model='" + model + '\'' +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}