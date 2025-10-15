package org.coffee.utils.mappers;

import org.coffee.domain.models.VariableOpcUa;
import org.coffee.domain.models.database.VariableOpcUaTable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class VariableOpcUaMapper {

    // Converte VariableOpcUaTable (entidade do banco) para VariableOpcUa (DTO)
    public VariableOpcUa toDTO(VariableOpcUaTable table) {
        if (table == null) {
            return null;
        }

        return new VariableOpcUa(
                table.getId(),
                table.getName(),
                table.getDataType(),
                table.getUnit(),
                table.getDescription(),
                table.getDevice() != null ? table.getDevice().getId() : null,
                table.getNodeId(),
                table.getNamespaceIndex(),
                table.getNodeIdPrefix(),
                table.getCreatedAt(),
                table.getUpdatedAt()
        );
    }

    // Converte VariableOpcUa (DTO) para VariableOpcUaTable (entidade do banco)
    // Nota: Este metodo nao configura o relacionamento Device
    // Isso deve ser feito no Service usando o repository apropriado
    public VariableOpcUaTable toTable(VariableOpcUa dto) {
        if (dto == null) {
            return null;
        }

        VariableOpcUaTable table = new VariableOpcUaTable();
        table.setId(dto.getId());
        table.setName(dto.getName());
        table.setDataType(dto.getDataType());
        table.setUnit(dto.getUnit());
        table.setDescription(dto.getDescription());
        table.setNodeId(dto.getNodeId());
        table.setNamespaceIndex(dto.getNamespaceIndex());
        table.setNodeIdPrefix(dto.getNodeIdPrefix());
        table.setCreatedAt(dto.getCreatedAt());
        table.setUpdatedAt(dto.getUpdatedAt());

        return table;
    }

    // Converte uma lista de VariableOpcUaTable para lista de VariableOpcUa
    public List<VariableOpcUa> toDTOList(List<VariableOpcUaTable> tables) {
        if (tables == null) {
            return null;
        }

        return tables.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Converte uma lista de VariableOpcUa para lista de VariableOpcUaTable
    public List<VariableOpcUaTable> toTableList(List<VariableOpcUa> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .map(this::toTable)
                .collect(Collectors.toList());
    }

    // Atualiza uma entidade existente com dados do DTO
    public void updateTableFromDTO(VariableOpcUaTable table, VariableOpcUa dto) {
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
        if (dto.getNodeId() != null) {
            table.setNodeId(dto.getNodeId());
        }
        if (dto.getNamespaceIndex() != null) {
            table.setNamespaceIndex(dto.getNamespaceIndex());
        }
        if (dto.getNodeIdPrefix() != null) {
            table.setNodeIdPrefix(dto.getNodeIdPrefix());
        }
        // createdAt e updatedAt sao gerenciados pelo @PreUpdate
    }
}