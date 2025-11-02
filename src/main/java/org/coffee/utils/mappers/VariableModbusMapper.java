package org.coffee.utils.mappers;

import org.coffee.domain.models.VariableModbus;
import org.coffee.domain.models.database.VariableModbusTable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class VariableModbusMapper {

    // Converte VariableModbusTable (entidade do banco) para VariableModbus (DTO)
    public VariableModbus toDTO(VariableModbusTable table) {
        if (table == null) {
            return null;
        }

        return new VariableModbus(
                table.getId(),
                table.getName(),
                table.getDataType(),
                table.getUnit(),
                table.getDescription(),
                table.getDevice() != null ? table.getDevice().getId() : null,
                table.getAddress(),
                table.getPort(),
                table.getUnitId(),
                table.getRegisterType(),
                table.getCreatedAt(),
                table.getUpdatedAt()
        );
    }

    // Converte VariableModbus (DTO) para VariableModbusTable (entidade do banco)
    // Nota: Este metodo nao configura o relacionamento Device
    // Isso deve ser feito no Service usando o repository apropriado
    public VariableModbusTable toTable(VariableModbus dto) {
        if (dto == null) {
            return null;
        }

        VariableModbusTable table = new VariableModbusTable();
        table.setId(dto.getId());
        table.setName(dto.getName());
        table.setDataType(dto.getDataType());
        table.setUnit(dto.getUnit());
        table.setDescription(dto.getDescription());
        table.setAddress(dto.getAddress());
        table.setPort(dto.getPort());
        table.setUnitId(dto.getUnitId());
        table.setRegisterType(dto.getRegisterType());
        table.setCreatedAt(dto.getCreatedAt());
        table.setUpdatedAt(dto.getUpdatedAt());

        return table;
    }

    // Converte uma lista de VariableModbusTable para lista de VariableModbus
    public List<VariableModbus> toDTOList(List<VariableModbusTable> tables) {
        if (tables == null) {
            return null;
        }

        return tables.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Converte uma lista de VariableModbus para lista de VariableModbusTable
    public List<VariableModbusTable> toTableList(List<VariableModbus> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .map(this::toTable)
                .collect(Collectors.toList());
    }

    // Atualiza uma entidade existente com dados do DTO
    public void updateTableFromDTO(VariableModbusTable table, VariableModbus dto) {
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
        if (dto.getAddress() != null) {
            table.setAddress(dto.getAddress());
        }
        if (dto.getPort() != null) {
            table.setPort(dto.getPort());
        }
        if (dto.getUnitId() != null) {
            table.setUnitId(dto.getUnitId());
        }
        if (dto.getRegisterType() != null) {
            table.setRegisterType(dto.getRegisterType());
        }
        // createdAt e updatedAt sao gerenciados pelo @PreUpdate
    }
}