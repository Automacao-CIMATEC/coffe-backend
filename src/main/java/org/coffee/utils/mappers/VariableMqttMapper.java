package org.coffee.utils.mappers;

import org.coffee.domain.models.VariableMqtt;
import org.coffee.domain.models.database.VariableMqttTable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class VariableMqttMapper {

    // Converte VariableMqttTable (entidade do banco) para VariableMqtt (DTO)
    public VariableMqtt toDTO(VariableMqttTable table) {
        if (table == null) {
            return null;
        }

        return new VariableMqtt(
                table.getId(),
                table.getName(),
                table.getDataType(),
                table.getUnit(),
                table.getDescription(),
                table.getDevice() != null ? table.getDevice().getId() : null,
                table.getTopic(),
                table.getQos(),
                table.getClientId(),
                table.getRetained(),
                table.getCreatedAt(),
                table.getUpdatedAt()
        );
    }

    // Converte VariableMqtt (DTO) para VariableMqttTable (entidade do banco)
    // Nota: Este metodo nao configura o relacionamento Device
    // Isso deve ser feito no Service usando o repository apropriado
    public VariableMqttTable toTable(VariableMqtt dto) {
        if (dto == null) {
            return null;
        }

        VariableMqttTable table = new VariableMqttTable();
        table.setId(dto.getId());
        table.setName(dto.getName());
        table.setDataType(dto.getDataType());
        table.setUnit(dto.getUnit());
        table.setDescription(dto.getDescription());
        table.setTopic(dto.getTopic());
        table.setQos(dto.getQos());
        table.setClientId(dto.getClientId());
        table.setRetained(dto.getRetained());
        table.setCreatedAt(dto.getCreatedAt());
        table.setUpdatedAt(dto.getUpdatedAt());

        return table;
    }

    // Converte uma lista de VariableMqttTable para lista de VariableMqtt
    public List<VariableMqtt> toDTOList(List<VariableMqttTable> tables) {
        if (tables == null) {
            return null;
        }

        return tables.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Converte uma lista de VariableMqtt para lista de VariableMqttTable
    public List<VariableMqttTable> toTableList(List<VariableMqtt> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .map(this::toTable)
                .collect(Collectors.toList());
    }

    // Atualiza uma entidade existente com dados do DTO
    public void updateTableFromDTO(VariableMqttTable table, VariableMqtt dto) {
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
        if (dto.getTopic() != null) {
            table.setTopic(dto.getTopic());
        }
        if (dto.getQos() != null) {
            table.setQos(dto.getQos());
        }
        if (dto.getClientId() != null) {
            table.setClientId(dto.getClientId());
        }
        if (dto.getRetained() != null) {
            table.setRetained(dto.getRetained());
        }
        // createdAt e updatedAt sao gerenciados pelo @PreUpdate
    }
}