package org.coffee.domain.models.database;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "protocol")
public class ProtocolTable {

    // Representa os tipos de protocolos disponiveis no sistema
    // Modbus, OPC-UA, MQTT, etc

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relacionamento bidirecional com Device
    @OneToMany(mappedBy = "protocol", cascade = CascadeType.ALL)
    private Set<DeviceTable> devices = new HashSet<>();

    // Construtor padrao obrigatorio para JPA
    public ProtocolTable() {
    }

    // Construtor auxiliar
    public ProtocolTable(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Metodos de lifecycle do JPA
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
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

    public Set<DeviceTable> getDevices() {
        return devices;
    }

    public void setDevices(Set<DeviceTable> devices) {
        this.devices = devices;
    }
}