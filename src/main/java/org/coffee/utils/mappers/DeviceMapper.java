package org.coffee.utils.mappers;

import org.coffee.domain.models.Device;
import org.coffee.domain.models.database.DeviceTable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DeviceMapper {

    // Converte DeviceTable (entidade do banco) para Device (DTO)
    public Device toDTO(DeviceTable table) {
        if (table == null) {
            return null;
        }

        return new Device(
                table.getId(),
                table.getName(),
                table.getManufacturer(),
                table.getDescription(),
                table.getPlc() != null ? table.getPlc().getId() : null,
                table.getProtocol() != null ? table.getProtocol().getId() : null,
                table.getCreatedAt(),
                table.getUpdatedAt()
        );
    }

    // Converte Device (DTO) para DeviceTable (entidade do banco)
    // Nota: Este metodo nao configura os relacionamentos PLC e Protocol
    // Isso deve ser feito no Service usando os repositories apropriados
    public DeviceTable toTable(Device dto) {
        if (dto == null) {
            return null;
        }

        DeviceTable table = new DeviceTable();
        table.setId(dto.getId());
        table.setName(dto.getName());
        table.setManufacturer(dto.getManufacturer());
        table.setDescription(dto.getDescription());
        table.setCreatedAt(dto.getCreatedAt());
        table.setUpdatedAt(dto.getUpdatedAt());

        return table;
    }

    // Converte uma lista de DeviceTable para lista de Device
    public List<Device> toDTOList(List<DeviceTable> tables) {
        if (tables == null) {
            return null;
        }

        return tables.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Converte uma lista de Device para lista de DeviceTable
    public List<DeviceTable> toTableList(List<Device> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .map(this::toTable)
                .collect(Collectors.toList());
    }

    // Atualiza uma entidade existente com dados do DTO
    // Nota: Nao atualiza os relacionamentos PLC e Protocol
    // Isso deve ser feito no Service se necessario
    public void updateTableFromDTO(DeviceTable table, Device dto) {
        if (table == null || dto == null) {
            return;
        }

        // Nao atualiza o ID
        if (dto.getName() != null) {
            table.setName(dto.getName());
        }
        if (dto.getManufacturer() != null) {
            table.setManufacturer(dto.getManufacturer());
        }
        if (dto.getDescription() != null) {
            table.setDescription(dto.getDescription());
        }
        // createdAt e updatedAt sao gerenciados pelo @PreUpdate
    }
}