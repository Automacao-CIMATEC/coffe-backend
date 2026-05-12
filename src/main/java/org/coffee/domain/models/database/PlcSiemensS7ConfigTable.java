package org.coffee.domain.models.database;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Configuracao especifica do protocolo Siemens S7 para um PLC.
 *
 * Relacionamento 1:1 (opcional) com PlcTable: um PLC tem no maximo
 * uma configuracao S7, e PLCs que nao falam S7 simplesmente nao
 * possuem registro nessa tabela.
 *
 * Esses parametros (rack e slot) sao exigidos pela connection string
 * da PLC4X para o protocolo S7:
 *   s7://{ip}?remote-rack={rack}&remote-slot={slot}
 *
 * Valores tipicos:
 *   - S7-1200 / S7-1500: rack=0, slot=1
 *   - S7-300:            rack=0, slot=2
 *   - S7-400:            rack=0, slot=3
 */
@Entity
@Table(name = "plc_siemens_s7_config")
public class PlcSiemensS7ConfigTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relacionamento 1:1 com PlcTable.
    // Lado dono do relacionamento - aqui que mora a FK plc_id.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plc_id", nullable = false, unique = true)
    private PlcTable plc;

    @Column(name = "rack", nullable = false)
    private Integer rack;

    @Column(name = "slot", nullable = false)
    private Integer slot;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Construtor padrao obrigatorio para JPA
    public PlcSiemensS7ConfigTable() {
    }

    public PlcSiemensS7ConfigTable(PlcTable plc, Integer rack, Integer slot) {
        this.plc = plc;
        this.rack = rack;
        this.slot = slot;
    }

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

    public PlcTable getPlc() {
        return plc;
    }

    public void setPlc(PlcTable plc) {
        this.plc = plc;
    }

    public Integer getRack() {
        return rack;
    }

    public void setRack(Integer rack) {
        this.rack = rack;
    }

    public Integer getSlot() {
        return slot;
    }

    public void setSlot(Integer slot) {
        this.slot = slot;
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
}
