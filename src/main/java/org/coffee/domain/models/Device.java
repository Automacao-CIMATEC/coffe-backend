package org.coffee.domain.models;

import java.time.LocalDateTime;

public class Device {

    // Objeto de transferencia de dados (DTO)
    // Usado para logica de negocio e API
    // Pode conter metodos auxiliares e validacoes

    private Long id;
    private String deviceName;
    private String deviceType;
    private String ipAddress;
    private Integer port;
    private String protocol;
    private String status;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Construtor vazio
    public Device() {
    }

    // Construtor completo
    public Device(Long id, String deviceName, String deviceType, String ipAddress,
                  Integer port, String protocol, String status, Boolean isActive,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.ipAddress = ipAddress;
        this.port = port;
        this.protocol = protocol;
        this.status = status;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Construtor simplificado
    public Device(String deviceName, String deviceType, String protocol) {
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.protocol = protocol;
        this.isActive = true;
        this.status = "offline";
    }

    // Metodos de logica de negocio

    /**
     * Verifica se o dispositivo esta online
     * @return true se status for "online"
     */
    public boolean isOnline() {
        return "online".equalsIgnoreCase(this.status);
    }

    /**
     * Verifica se o dispositivo esta disponivel para uso
     * @return true se esta ativo e online
     */
    public boolean isAvailable() {
        return this.isActive != null &&
                this.isActive &&
                isOnline();
    }

    /**
     * Retorna a URL completa de conexao
     * @return URL no formato protocolo://ip:porta
     */
    public String getConnectionUrl() {
        if (this.protocol == null || this.ipAddress == null) {
            return "";
        }

        String url = this.protocol.toLowerCase() + "://" + this.ipAddress;

        if (this.port != null) {
            url += ":" + this.port;
        }

        return url;
    }

    /**
     * Verifica se as configuracoes de rede sao validas
     * @return true se IP e porta estao configurados
     */
    public boolean hasValidNetworkConfig() {
        return this.ipAddress != null &&
                !this.ipAddress.isEmpty() &&
                this.port != null &&
                this.port > 0 &&
                this.port <= 65535;
    }

    /**
     * Verifica se o protocolo e suportado
     * @return true se o protocolo e OPC-UA, Modbus ou MQTT
     */
    public boolean hasSupportedProtocol() {
        if (this.protocol == null) return false;

        String protocolUpper = this.protocol.toUpperCase();
        return protocolUpper.equals("OPC-UA") ||
                protocolUpper.equals("MODBUS") ||
                protocolUpper.equals("MQTT");
    }

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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
                ", deviceName='" + deviceName + '\'' +
                ", deviceType='" + deviceType + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", port=" + port +
                ", protocol='" + protocol + '\'' +
                ", status='" + status + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}