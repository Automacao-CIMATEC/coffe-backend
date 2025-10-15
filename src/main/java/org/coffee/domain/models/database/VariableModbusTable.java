package org.coffee.domain.models.database;

import jakarta.persistence.*;

@Entity
@Table(name = "variable_modbus")
public class VariableModbusTable extends VariableTable {

    // Representa uma variavel especifica para protocolo Modbus
    // Herda campos comuns de VariableTable e adiciona campos especificos do Modbus

    @Column(name = "address", nullable = false)
    private Integer address;

    @Column(name = "unit_id", nullable = false)
    private Integer unitId;

    @Column(name = "register_type", nullable = false, length = 50)
    private String registerType;

    // Construtor padrao obrigatorio para JPA
    public VariableModbusTable() {
        super();
    }

    // Construtor auxiliar
    public VariableModbusTable(String name, String dataType, DeviceTable device,
                               Integer address, Integer unitId, String registerType) {
        super(name, dataType, device);
        this.address = address;
        this.unitId = unitId;
        this.registerType = registerType;
    }

    // Getters e Setters

    public Integer getAddress() {
        return address;
    }

    public void setAddress(Integer address) {
        this.address = address;
    }

    public Integer getUnitId() {
        return unitId;
    }

    public void setUnitId(Integer unitId) {
        this.unitId = unitId;
    }

    public String getRegisterType() {
        return registerType;
    }

    public void setRegisterType(String registerType) {
        this.registerType = registerType;
    }
}