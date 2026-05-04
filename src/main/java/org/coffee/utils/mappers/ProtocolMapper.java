package org.coffee.utils.mappers;

import org.coffee.domain.models.Protocol;
import org.coffee.domain.models.database.ProtocolTable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProtocolMapper {

    // Converte ProtocolTable (entidade do banco) para Protocol (DTO)
    public Protocol toDTO(ProtocolTable table) {
        if (table == null) {
            return null;
        }

        return new Protocol(
                table.getId(),
                table.getName(),
                table.getDescription(),
                table.getCreatedAt(),
                table.getUpdatedAt()
        );
    }

    // Converte Protocol (DTO) para ProtocolTable (entidade do banco)
    public ProtocolTable toTable(Protocol dto) {
        if (dto == null) {
            return null;
        }

        ProtocolTable table = new ProtocolTable();
        table.setId(dto.getId());
        table.setName(dto.getName());
        table.setDescription(dto.getDescription());
        table.setCreatedAt(dto.getCreatedAt());
        table.setUpdatedAt(dto.getUpdatedAt());

        return table;
    }

    // Converte uma lista de ProtocolTable para lista de Protocol
    public List<Protocol> toDTOList(List<ProtocolTable> tables) {
        if (tables == null) {
            return null;
        }

        return tables.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Converte uma lista de Protocol para lista de ProtocolTable
    public List<ProtocolTable> toTableList(List<Protocol> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .map(this::toTable)
                .collect(Collectors.toList());
    }

    // Atualiza uma entidade existente com dados do DTO
    public void updateTableFromDTO(ProtocolTable table, Protocol dto) {
        if (table == null || dto == null) {
            return;
        }

        // Nao atualiza o ID
        if (dto.getName() != null) {
            table.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            table.setDescription(dto.getDescription());
        }
        // createdAt e updatedAt sao gerenciados pelo @PreUpdate
    }
}