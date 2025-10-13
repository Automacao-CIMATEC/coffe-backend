package org.coffee.utils.mappers;

import org.coffee.domain.models.Device;
import org.coffee.domain.models.database.DeviceTable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DeviceMapper {

    /**
     * Converte DeviceTable (entidade do banco) para Device (DTO)
     * @param table entidade do banco de dados
     * @return objeto DTO para uso em logica de negocio
     */
    public Device toDTO(DeviceTable table) {
        if (table == null) {
            return null;
        }

        return new Device(
                table.getId(),
                table.getDeviceName(),
                table.getDeviceType(),
                table.getIpAddress(),
                table.getPort(),
                table.getProtocol(),
                table.getStatus(),
                table.getIsActive(),
                table.getCreatedAt(),
                table.getUpdatedAt()
        );
    }

    /**
     * Converte Device (DTO) para DeviceTable (entidade do banco)
     * @param dto objeto de negocio
     * @return entidade para persistencia no banco
     */
    public DeviceTable toTable(Device dto) {
        if (dto == null) {
            return null;
        }

        DeviceTable table = new DeviceTable();
        table.setId(dto.getId());
        table.setDeviceName(dto.getDeviceName());
        table.setDeviceType(dto.getDeviceType());
        table.setIpAddress(dto.getIpAddress());
        table.setPort(dto.getPort());
        table.setProtocol(dto.getProtocol());
        table.setStatus(dto.getStatus());
        table.setIsActive(dto.getIsActive());
        table.setCreatedAt(dto.getCreatedAt());
        table.setUpdatedAt(dto.getUpdatedAt());

        return table;
    }

    /**
     * Converte uma lista de DeviceTable para lista de Device
     * @param tables lista de entidades do banco
     * @return lista de DTOs
     */
    public List<Device> toDTOList(List<DeviceTable> tables) {
        if (tables == null) {
            return null;
        }

        return tables.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Converte uma lista de Device para lista de DeviceTable
     * @param dtos lista de DTOs
     * @return lista de entidades para o banco
     */
    public List<DeviceTable> toTableList(List<Device> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .map(this::toTable)
                .collect(Collectors.toList());
    }

    /**
     * Atualiza uma entidade existente com dados do DTO
     * Util para operacoes de update onde o ID ja existe
     * @param table entidade existente no banco
     * @param dto DTO com novos dados
     */
    public void updateTableFromDTO(DeviceTable table, Device dto) {
        if (table == null || dto == null) {
            return;
        }

        // Nao atualiza o ID
        if (dto.getDeviceName() != null) {
            table.setDeviceName(dto.getDeviceName());
        }
        if (dto.getDeviceType() != null) {
            table.setDeviceType(dto.getDeviceType());
        }
        if (dto.getIpAddress() != null) {
            table.setIpAddress(dto.getIpAddress());
        }
        if (dto.getPort() != null) {
            table.setPort(dto.getPort());
        }
        if (dto.getProtocol() != null) {
            table.setProtocol(dto.getProtocol());
        }
        if (dto.getStatus() != null) {
            table.setStatus(dto.getStatus());
        }
        if (dto.getIsActive() != null) {
            table.setIsActive(dto.getIsActive());
        }
        // createdAt e updatedAt sao gerenciados pelo @PreUpdate
    }
}