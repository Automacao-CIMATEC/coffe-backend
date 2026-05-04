package org.coffee.utils.mappers;

import org.coffee.domain.models.VariableEthernetIp;
import org.coffee.domain.models.database.VariableEthernetIpTable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class VariableEthernetIpMapper {

    // Converte VariableEthernetIpTable (entidade do banco) para VariableEthernetIp (DTO)
    public VariableEthernetIp toDTO(VariableEthernetIpTable table) {
        if (table == null) {
            return null;
        }

        return new VariableEthernetIp(
                table.getId(),
                table.getName(),
                table.getDataType(),
                table.getUnit(),
                table.getDescription(),
                table.getDevice() != null ? table.getDevice().getId() : null,
                table.getTagName(),
                table.getCreatedAt(),
                table.getUpdatedAt()
        );
    }

    // Converte VariableEthernetIp (DTO) para VariableEthernetIpTable (entidade do banco)
    // Nota: Este metodo nao configura o relacionamento Device
    // Isso deve ser feito no Service usando o repository apropriado
    public VariableEthernetIpTable toTable(VariableEthernetIp dto) {
        if (dto == null) {
            return null;
        }

        VariableEthernetIpTable table = new VariableEthernetIpTable();
        table.setId(dto.getId());
        table.setName(dto.getName());
        table.setDataType(dto.getDataType());
        table.setUnit(dto.getUnit());
        table.setDescription(dto.getDescription());
        table.setTagName(dto.getTagName());
        table.setCreatedAt(dto.getCreatedAt());
        table.setUpdatedAt(dto.getUpdatedAt());

        return table;
    }

    // Converte uma lista de VariableEthernetIpTable para lista de VariableEthernetIp
    public List<VariableEthernetIp> toDTOList(List<VariableEthernetIpTable> tables) {
        if (tables == null) {
            return null;
        }

        return tables.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Converte uma lista de VariableEthernetIp para lista de VariableEthernetIpTable
    public List<VariableEthernetIpTable> toTableList(List<VariableEthernetIp> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .map(this::toTable)
                .collect(Collectors.toList());
    }

    // Atualiza uma entidade existente com dados do DTO
    public void updateTableFromDTO(VariableEthernetIpTable table, VariableEthernetIp dto) {
        if (table == null || dto == null) {
            return;
        }

        // Nao atualiza o ID
        if (dto.getName() != null) {
            table.setName(dto.getName());
        }
        if (dto.getDataType() != null) {
            table.setDataType(dto.getDataType());
        }
        if (dto.getUnit() != null) {
            table.setUnit(dto.getUnit());
        }
        if (dto.getDescription() != null) {
            table.setDescription(dto.getDescription());
        }
        if (dto.getTagName() != null) {
            table.setTagName(dto.getTagName());
        }
        // createdAt e updatedAt sao gerenciados pelo @PreUpdate
    }
}