package org.coffee.domain.models.database;

import jakarta.persistence.*;

@Entity
@Table(name = "variable_opcua")
public class VariableOpcUaTable extends VariableTable {

    // Representa uma variavel especifica para protocolo OPC-UA
    // Herda campos comuns de VariableTable e adiciona campos especificos do OPC-UA

    @Column(name = "node_id", nullable = false, length = 255)
    private String nodeId;

    @Column(name = "namespace_index", nullable = false)
    private Integer namespaceIndex;

    @Column(name = "node_id_prefix", length = 100)
    private String nodeIdPrefix;

    // Construtor padrao obrigatorio para JPA
    public VariableOpcUaTable() {
        super();
    }

    // Construtor auxiliar
    public VariableOpcUaTable(String name, String dataType, DeviceTable device,
                              String nodeId, Integer namespaceIndex, String nodeIdPrefix) {
        super(name, dataType, device);
        this.nodeId = nodeId;
        this.namespaceIndex = namespaceIndex;
        this.nodeIdPrefix = nodeIdPrefix;
    }

    // Getters e Setters

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Integer getNamespaceIndex() {
        return namespaceIndex;
    }

    public void setNamespaceIndex(Integer namespaceIndex) {
        this.namespaceIndex = namespaceIndex;
    }

    public String getNodeIdPrefix() {
        return nodeIdPrefix;
    }

    public void setNodeIdPrefix(String nodeIdPrefix) {
        this.nodeIdPrefix = nodeIdPrefix;
    }
}