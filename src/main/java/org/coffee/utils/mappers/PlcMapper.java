package org.coffee.utils.mappers;

import org.coffee.domain.models.Plc;
import org.coffee.domain.models.database.PlcTable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PlcMapper {

    // Converte PlcTable (entidade do banco) para Plc (DTO)
    public Plc toDTO(PlcTable table) {
        if (table == null) {
            return null;
        }

        return new Plc(
                table.getId(),
                table.getName(),
                table.getIp(),
                table.getPort(),
                table.getManufacturer(),
                table.getModel(),
                table.getDescription(),
                table.getCreatedAt(),
                table.getUpdatedAt()
        );
    }

    // Converte Plc (DTO) para PlcTable (entidade do banco)
    public PlcTable toTable(Plc dto) {
        if (dto == null) {
            return null;
        }

        PlcTable table = new PlcTable();
        table.setId(dto.getId());
        table.setName(dto.getName());
        table.setIp(dto.getIp());
        table.setPort(dto.getPort());
        table.setManufacturer(dto.getManufacturer());
        table.setModel(dto.getModel());
        table.setDescription(dto.getDescription());
        table.setCreatedAt(dto.getCreatedAt());
        table.setUpdatedAt(dto.getUpdatedAt());

        return table;
    }

    // Converte uma lista de PlcTable para lista de Plc
    public List<Plc> toDTOList(List<PlcTable> tables) {
        if (tables == null) {
            return null;
        }

        return tables.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Converte uma lista de Plc para lista de PlcTable
    public List<PlcTable> toTableList(List<Plc> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .map(this::toTable)
                .collect(Collectors.toList());
    }

    // Atualiza uma entidade existente com dados do DTO
    public void updateTableFromDTO(PlcTable table, Plc dto) {
        if (table == null || dto == null) {
            return;
        }

        // Nao atualiza o ID
        if (dto.getName() != null) {
            table.setName(dto.getName());
        }
        if (dto.getIp() != null) {
            table.setIp(dto.getIp());
        }
        if (dto.getPort() != null) {
            table.setPort(dto.getPort());
        }
        if (dto.getManufacturer() != null) {
            table.setManufacturer(dto.getManufacturer());
        }
        if (dto.getModel() != null) {
            table.setModel(dto.getModel());
        }
        if (dto.getDescription() != null) {
            table.setDescription(dto.getDescription());
        }
        // createdAt e updatedAt sao gerenciados pelo @PreUpdate
    }
}