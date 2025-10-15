package org.coffee.domain.models.database;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "variable")
@Inheritance(strategy = InheritanceType.JOINED)
public class VariableTable {

    // Representa uma variavel generica de um dispositivo
    // Classes especificas herdam desta para adicionar campos especificos do protocolo

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "data_type", nullable = false, length = 50)
    private String dataType;

    @Column(name = "unit", length = 50)
    private String unit;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relacionamento Many-to-One com Device
    // Multiplas variaveis pertencem a um dispositivo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private DeviceTable device;

    // Construtor padrao obrigatorio para JPA
    public VariableTable() {
    }

    // Construtor auxiliar
    public VariableTable(String name, String dataType, DeviceTable device) {
        this.name = name;
        this.dataType = dataType;
        this.device = device;
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
        this.updatedAt = LocalDateTime.now();
    }

    public DeviceTable getDevice() {
        return device;
    }

    public void setDevice(DeviceTable device) {
        this.device = device;
    }
}