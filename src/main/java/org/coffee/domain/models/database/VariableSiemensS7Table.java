package org.coffee.domain.models.database;

import jakarta.persistence.*;

@Entity
@Table(name = "variable_siemens_s7")
public class VariableSiemensS7Table extends VariableTable {

    // Representa uma variavel especifica para protocolo Siemens S7
    // Herda campos comuns de VariableTable e adiciona campos especificos do Siemens S7

    @Column(name = "db_number", nullable = false)
    private Integer dbNumber;

    @Column(name = "byte_offset", nullable = false)
    private Integer offset;

    @Column(name = "bit_offset", nullable = false)
    private Integer bitOffset;

    // Construtor padrao obrigatorio para JPA
    public VariableSiemensS7Table() {
        super();
    }

    // Construtor auxiliar
    public VariableSiemensS7Table(String name, String dataType, DeviceTable device,
                                  Integer dbNumber, Integer offset, Integer bitOffset) {
        super(name, dataType, device);
        this.dbNumber = dbNumber;
        this.offset = offset;
        this.bitOffset = bitOffset;
    }

    // Getters e Setters

    public Integer getDbNumber() {
        return dbNumber;
    }

    public void setDbNumber(Integer dbNumber) {
        this.dbNumber = dbNumber;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getBitOffset() {
        return bitOffset;
    }

    public void setBitOffset(Integer bitOffset) {
        this.bitOffset = bitOffset;
    }
}