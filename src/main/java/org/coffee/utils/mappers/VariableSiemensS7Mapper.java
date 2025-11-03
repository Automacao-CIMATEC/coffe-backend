package org.coffee.utils.mappers;

import org.coffee.domain.models.VariableSiemensS7;
import org.coffee.domain.models.database.VariableSiemensS7Table;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class VariableSiemensS7Mapper {

    // Converte VariableSiemensS7Table (entidade do banco) para VariableSiemensS7 (DTO)
    public VariableSiemensS7 toDTO(VariableSiemensS7Table table) {
        if (table == null) {
            return null;
        }

        return new VariableSiemensS7(
                table.getId(),
                table.getName(),
                table.getDataType(),
                table.getUnit(),
                table.getDescription(),
                table.getDevice() != null ? table.getDevice().getId() : null,
                table.getDbNumber(),
                table.getOffset(),
                table.getBitOffset(),
                table.getCreatedAt(),
                table.getUpdatedAt()
        );
    }

    // Converte VariableSiemensS7 (DTO) para VariableSiemensS7Table (entidade do banco)
    // Nota: Este metodo nao configura o relacionamento Device
    // Isso deve ser feito no Service usando o repository apropriado
    public VariableSiemensS7Table toTable(VariableSiemensS7 dto) {
        if (dto == null) {
            return null;
        }

        VariableSiemensS7Table table = new VariableSiemensS7Table();
        table.setId(dto.getId());
        table.setName(dto.getName());
        table.setDataType(dto.getDataType());
        table.setUnit(dto.getUnit());
        table.setDescription(dto.getDescription());
        table.setDbNumber(dto.getDbNumber());
        table.setOffset(dto.getOffset());
        table.setBitOffset(dto.getBitOffset());
        table.setCreatedAt(dto.getCreatedAt());
        table.setUpdatedAt(dto.getUpdatedAt());

        return table;
    }

    // Converte uma lista de VariableSiemensS7Table para lista de VariableSiemensS7
    public List<VariableSiemensS7> toDTOList(List<VariableSiemensS7Table> tables) {
        if (tables == null) {
            return null;
        }

        return tables.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Converte uma lista de VariableSiemensS7 para lista de VariableSiemensS7Table
    public List<VariableSiemensS7Table> toTableList(List<VariableSiemensS7> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .map(this::toTable)
                .collect(Collectors.toList());
    }

    // Atualiza uma entidade existente com dados do DTO
    public void updateTableFromDTO(VariableSiemensS7Table table, VariableSiemensS7 dto) {
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
        if (dto.getDbNumber() != null) {
            table.setDbNumber(dto.getDbNumber());
        }
        if (dto.getOffset() != null) {
            table.setOffset(dto.getOffset());
        }
        if (dto.getBitOffset() != null) {
            table.setBitOffset(dto.getBitOffset());
        }
        // createdAt e updatedAt sao gerenciados pelo @PreUpdate
    }
}