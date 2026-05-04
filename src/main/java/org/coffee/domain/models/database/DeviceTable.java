package org.coffee.domain.models.database;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "device")
public class DeviceTable {

    // Representa um dispositivo monitorado no sistema
    // Cada dispositivo possui um PLC e um protocolo associado

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "manufacturer", length = 100)
    private String manufacturer;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relacionamento Many-to-One com PLC
    // Um dispositivo pertence a apenas um PLC
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plc_id", nullable = false)
    private PlcTable plc;

    // Relacionamento Many-to-One com Protocol
    // Um dispositivo utiliza apenas um protocolo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "protocol_id", nullable = false)
    private ProtocolTable protocol;

    // Relacionamento One-to-Many com Variable
    // Um dispositivo pode ter multiplas variaveis
    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<VariableTable> variables = new HashSet<>();

    // Construtor padrao obrigatorio para JPA
    public DeviceTable() {
    }

    // Construtor auxiliar
    public DeviceTable(String name, PlcTable plc, ProtocolTable protocol) {
        this.name = name;
        this.plc = plc;
        this.protocol = protocol;
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

    // Metodos auxiliares para gerenciar relacionamento bidirecional
    public void addVariable(VariableTable variable) {
        variables.add(variable);
        variable.setDevice(this);
    }

    public void removeVariable(VariableTable variable) {
        variables.remove(variable);
        variable.setDevice(null);
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

    public PlcTable getPlc() {
        return plc;
    }

    public void setPlc(PlcTable plc) {
        this.plc = plc;
    }

    public ProtocolTable getProtocol() {
        return protocol;
    }

    public void setProtocol(ProtocolTable protocol) {
        this.protocol = protocol;
    }

    public Set<VariableTable> getVariables() {
        return variables;
    }

    public void setVariables(Set<VariableTable> variables) {
        this.variables = variables;
    }
}