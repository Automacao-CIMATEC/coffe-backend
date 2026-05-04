package org.coffee.domain.models;

import java.time.LocalDateTime;

/**
 * Classe de modelo que representa um PLC (Programmable Logic Controller).
 * Objeto de transferência de dados (DTO) usado para lógica de negócio e API.
 */
public class Plc {

    /** Identificador único do PLC */
    private Long id;

    /** Nome do PLC */
    private String name;

    /** Endereço IP do PLC */
    private String ip;

    /** Fabricante do PLC */
    private String manufacturer;

    /** Modelo do PLC */
    private String model;

    /** Descrição do PLC */
    private String description;

    /** Data e hora de criação do registro */
    private LocalDateTime createdAt;

    /** Data e hora da última atualização do registro */
    private LocalDateTime updatedAt;

    /**
     * Construtor padrão sem parâmetros.
     */
    public Plc() {
    }

    /**
     * Construtor completo com todos os parâmetros.
     *
     * @param id Identificador único do PLC
     * @param name Nome do PLC
     * @param ip Endereço IP do PLC
     * @param manufacturer Fabricante do PLC
     * @param model Modelo do PLC
     * @param description Descrição do PLC
     * @param createdAt Data e hora de criação
     * @param updatedAt Data e hora da última atualização
     */
    public Plc(Long id, String name, String ip, String manufacturer,
               String model, String description, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.ip = ip;
        this.manufacturer = manufacturer;
        this.model = model;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Construtor simplificado para criação de novo PLC.
     * Não inclui ID, que será gerado pelo banco de dados.
     *
     * @param name Nome do PLC
     * @param ip Endereço IP do PLC
     */
    public Plc(String name, String ip) {
        this.name = name;
        this.ip = ip;
    }

    /**
     * Valida se o nome do PLC está correto.
     * Verifica se o nome não é nulo e possui pelo menos 3 caracteres.
     *
     * @return true se o nome for válido, false caso contrário
     */
    public boolean hasValidName() {
        return this.name != null &&
                this.name.trim().length() >= 3;
    }

    /**
     * Valida se o endereço IP está em formato correto.
     * Realiza validação básica verificando se contém 4 octetos separados por ponto.
     *
     * @return true se o IP for válido, false caso contrário
     */
    public boolean hasValidIp() {
        if (this.ip == null || this.ip.trim().isEmpty()) {
            return false;
        }
        String[] parts = this.ip.split("\\.");
        return parts.length == 4;
    }

    /**
     * Retorna o identificador único do PLC.
     *
     * @return ID do PLC
     */
    public Long getId() {
        return id;
    }

    /**
     * Define o identificador único do PLC.
     *
     * @param id Novo ID do PLC
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Retorna o nome do PLC.
     *
     * @return Nome do PLC
     */
    public String getName() {
        return name;
    }

    /**
     * Define o nome do PLC.
     *
     * @param name Novo nome do PLC
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retorna o endereço IP do PLC.
     *
     * @return Endereço IP do PLC
     */
    public String getIp() {
        return ip;
    }

    /**
     * Define o endereço IP do PLC.
     *
     * @param ip Novo endereço IP do PLC
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * Retorna o fabricante do PLC.
     *
     * @return Fabricante do PLC
     */
    public String getManufacturer() {
        return manufacturer;
    }

    /**
     * Define o fabricante do PLC.
     *
     * @param manufacturer Novo fabricante do PLC
     */
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    /**
     * Retorna o modelo do PLC.
     *
     * @return Modelo do PLC
     */
    public String getModel() {
        return model;
    }

    /**
     * Define o modelo do PLC.
     *
     * @param model Novo modelo do PLC
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * Retorna a descrição do PLC.
     *
     * @return Descrição do PLC
     */
    public String getDescription() {
        return description;
    }

    /**
     * Define a descrição do PLC.
     *
     * @param description Nova descrição do PLC
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
     * Retorna uma representação textual do objeto PLC.
     *
     * @return String contendo todos os atributos do PLC
     */
    @Override
    public String toString() {
        return "Plc{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", ip='" + ip + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", model='" + model + '\'' +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}