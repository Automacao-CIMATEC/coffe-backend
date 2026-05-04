package org.coffee.domain.models.database;

import jakarta.persistence.*;

@Entity
@Table(name = "variable_ethernet_ip")
public class VariableEthernetIpTable extends VariableTable {

    // Representa uma variavel especifica para protocolo Ethernet/IP
    // Herda campos comuns de VariableTable e adiciona campos especificos do Ethernet/IP

    @Column(name = "tag_name", nullable = false, length = 100)
    private String tagName;

    // Construtor padrao obrigatorio para JPA
    public VariableEthernetIpTable() {
        super();
    }

    // Construtor auxiliar
    public VariableEthernetIpTable(String name, String dataType, DeviceTable device, String tagName) {
        super(name, dataType, device);
        this.tagName = tagName;
    }

    // Getters e Setters

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
}