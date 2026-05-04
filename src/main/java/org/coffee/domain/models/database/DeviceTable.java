package org.coffee.domain.models.database;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidade JPA que representa um dispositivo monitorado no sistema.
 * Cada dispositivo possui um PLC e um protocolo associado.
 */
@Entity
@Table(name = "device")
public class DeviceTable {

    /** Identificador único do dispositivo */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nome do dispositivo */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /** Fabricante do dispositivo */
    @Column(name = "manufacturer", length = 100)
    private String manufacturer;

    /** Descrição do dispositivo */
    @Column(name = "description", length = 255)
    private String description;

    /** Data e hora de criação do registro */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Data e hora da última atualização do registro */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Relacionamento Many-to-One com PLC.
     * Um dispositivo pertence a apenas um PLC.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plc_id", nullable = false)
    private PlcTable plc;

    /**
     * Relacionamento Many-to-One com Protocol.
     * Um dispositivo utiliza apenas um protocolo.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "protocol_id", nullable = false)
    private ProtocolTable protocol;

    /**
     * Relacionamento One-to-Many com Variable.
     * Um dispositivo pode ter múltiplas variáveis.
     */
    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<VariableTable> variables = new HashSet<>();

    /**
     * Construtor padrão obrigatório para JPA.
     */
    public DeviceTable() {
    }

    /**
     * Construtor auxiliar com parâmetros essenciais.
     *
     * @param name Nome do dispositivo
     * @param plc PLC ao qual o dispositivo está associado
     * @param protocol Protocolo utilizado pelo dispositivo
     */
    public DeviceTable(String name, PlcTable plc, ProtocolTable protocol) {
        this.name = name;
        this.plc = plc;
        this.protocol = protocol;
    }

    /**
     * Método de lifecycle JPA executado antes de persistir a entidade.
     * Define automaticamente as datas de criação e atualização.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Método de lifecycle JPA executado antes de atualizar a entidade.
     * Atualiza automaticamente a data de modificação.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Adiciona uma variável ao dispositivo mantendo o relacionamento bidirecional.
     *
     * @param variable Variável a ser adicionada
     */
    public void addVariable(VariableTable variable) {
        variables.add(variable);
        variable.setDevice(this);
    }

    /**
     * Remove uma variável do dispositivo mantendo o relacionamento bidirecional.
     *
     * @param variable Variável a ser removida
     */
    public void removeVariable(VariableTable variable) {
        variables.remove(variable);
        variable.setDevice(null);
    }

    /**
     * Retorna o identificador único do dispositivo.
     *
     * @return ID do dispositivo
     */
    public Long getId() {
        return id;
    }

    /**
     * Define o identificador único do dispositivo.
     *
     * @param id Novo ID do dispositivo
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Retorna o nome do dispositivo.
     *
     * @return Nome do dispositivo
     */
    public String getName() {
        return name;
    }

    /**
     * Define o nome do dispositivo.
     *
     * @param name Novo nome do dispositivo
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retorna o fabricante do dispositivo.
     *
     * @return Fabricante do dispositivo
     */
    public String getManufacturer() {
        return manufacturer;
    }

    /**
     * Define o fabricante do dispositivo.
     *
     * @param manufacturer Novo fabricante do dispositivo
     */
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    /**
     * Retorna a descrição do dispositivo.
     *
     * @return Descrição do dispositivo
     */
    public String getDescription() {
        return description;
    }

    /**
     * Define a descrição do dispositivo.
     *
     * @param description Nova descrição do dispositivo
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Retorna a data e hora de criação do registro.
     *
     * @return Data e hora de criação
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Define a data e hora de criação do registro.
     *
     * @param createdAt Nova data e hora de criação
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Retorna a data e hora da última atualização do registro.
     *
     * @return Data e hora da última atualização
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Define a data e hora da última atualização do registro.
     *
     * @param updatedAt Nova data e hora de atualização
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Retorna o PLC associado ao dispositivo.
     *
     * @return PLC associado
     */
    public PlcTable getPlc() {
        return plc;
    }

    /**
     * Define o PLC associado ao dispositivo.
     *
     * @param plc Novo PLC a ser associado
     */
    public void setPlc(PlcTable plc) {
        this.plc = plc;
    }

    /**
     * Retorna o protocolo utilizado pelo dispositivo.
     *
     * @return Protocolo utilizado
     */
    public ProtocolTable getProtocol() {
        return protocol;
    }

    /**
     * Define o protocolo utilizado pelo dispositivo.
     *
     * @param protocol Novo protocolo a ser utilizado
     */
    public void setProtocol(ProtocolTable protocol) {
        this.protocol = protocol;
    }

    /**
     * Retorna o conjunto de variáveis associadas ao dispositivo.
     *
     * @return Conjunto de variáveis
     */
    public Set<VariableTable> getVariables() {
        return variables;
    }

    /**
     * Define o conjunto de variáveis associadas ao dispositivo.
     *
     * @param variables Novo conjunto de variáveis
     */
    public void setVariables(Set<VariableTable> variables) {
        this.variables = variables;
    }
}