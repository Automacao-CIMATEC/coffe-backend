package org.coffee.services;

import org.coffee.utils.mappers.DeviceMapper;
import org.coffee.domain.models.Device;
import org.coffee.domain.models.database.DeviceTable;
import org.coffee.domain.models.database.PlcTable;
import org.coffee.domain.models.database.ProtocolTable;
import org.coffee.repository.DeviceRepository;
import org.coffee.repository.PlcRepository;
import org.coffee.repository.ProtocolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final PlcRepository plcRepository;
    private final ProtocolRepository protocolRepository;
    private final DeviceMapper deviceMapper;

    @Autowired
    public DeviceService(DeviceRepository deviceRepository, PlcRepository plcRepository,
                         ProtocolRepository protocolRepository, DeviceMapper deviceMapper) {
        this.deviceRepository = deviceRepository;
        this.plcRepository = plcRepository;
        this.protocolRepository = protocolRepository;
        this.deviceMapper = deviceMapper;
    }

    // Cria um novo dispositivo
    public Device createDevice(Device device) {
        // Validacoes de negocio
        if (!device.hasValidName()) {
            throw new RuntimeException("Nome do dispositivo invalido. Deve ter pelo menos 3 caracteres");
        }

        if (!device.hasPlc()) {
            throw new RuntimeException("Dispositivo deve ter um PLC associado");
        }

        if (!device.hasProtocol()) {
            throw new RuntimeException("Dispositivo deve ter um protocolo associado");
        }

        // Verifica se o nome ja existe
        if (deviceRepository.existsByName(device.getName())) {
            throw new RuntimeException("Dispositivo com este nome ja existe no sistema");
        }

        // Busca o PLC
        PlcTable plcTable = plcRepository.findById(device.getPlcId())
                .orElseThrow(() -> new RuntimeException("PLC nao encontrado"));

        // Busca o Protocol
        ProtocolTable protocolTable = protocolRepository.findById(device.getProtocolId())
                .orElseThrow(() -> new RuntimeException("Protocolo nao encontrado"));

        // Converte DTO para entidade
        DeviceTable table = deviceMapper.toTable(device);
        table.setPlc(plcTable);
        table.setProtocol(protocolTable);

        // Salva no banco
        DeviceTable savedTable = deviceRepository.save(table);

        // Converte de volta para DTO
        return deviceMapper.toDTO(savedTable);
    }

    // Atualiza um dispositivo existente
    public Device updateDevice(Long id, Device device) {
        // Busca a entidade existente
        DeviceTable existingTable = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dispositivo nao encontrado"));

        // Validacoes de negocio
        if (device.getName() != null && device.getName().trim().length() < 3) {
            throw new RuntimeException("Nome invalido. Deve ter pelo menos 3 caracteres");
        }

        // Verifica se o nome esta sendo alterado e se ja existe
        if (device.getName() != null && !existingTable.getName().equals(device.getName())) {
            if (deviceRepository.existsByName(device.getName())) {
                throw new RuntimeException("Nome ja esta em uso por outro dispositivo");
            }
        }

        // Atualiza PLC se fornecido
        if (device.getPlcId() != null) {
            PlcTable plcTable = plcRepository.findById(device.getPlcId())
                    .orElseThrow(() -> new RuntimeException("PLC nao encontrado"));
            existingTable.setPlc(plcTable);
        }

        // Atualiza Protocol se fornecido
        if (device.getProtocolId() != null) {
            ProtocolTable protocolTable = protocolRepository.findById(device.getProtocolId())
                    .orElseThrow(() -> new RuntimeException("Protocolo nao encontrado"));
            existingTable.setProtocol(protocolTable);
        }

        // Atualiza a entidade com dados do DTO
        deviceMapper.updateTableFromDTO(existingTable, device);

        // Salva as alteracoes
        DeviceTable updatedTable = deviceRepository.save(existingTable);

        // Retorna como DTO
        return deviceMapper.toDTO(updatedTable);
    }

    // Remove um dispositivo
    public void deleteDevice(Long id) {
        if (!deviceRepository.existsById(id)) {
            throw new RuntimeException("Dispositivo nao encontrado");
        }
        deviceRepository.deleteById(id);
    }

    // Busca um dispositivo por ID
    @Transactional(readOnly = true)
    public Optional<Device> getDeviceById(Long id) {
        return deviceRepository.findById(id)
                .map(deviceMapper::toDTO);
    }

    // Busca um dispositivo por nome
    @Transactional(readOnly = true)
    public Optional<Device> getDeviceByName(String name) {
        return deviceRepository.findByName(name)
                .map(deviceMapper::toDTO);
    }

    // Busca dispositivos por PLC
    @Transactional(readOnly = true)
    public List<Device> getDevicesByPlcId(Long plcId) {
        List<DeviceTable> tables = deviceRepository.findByPlcId(plcId);
        return deviceMapper.toDTOList(tables);
    }

    // Busca dispositivos por protocolo
    @Transactional(readOnly = true)
    public List<Device> getDevicesByProtocolId(Long protocolId) {
        List<DeviceTable> tables = deviceRepository.findByProtocolId(protocolId);
        return deviceMapper.toDTOList(tables);
    }

    // Busca dispositivos por fabricante
    @Transactional(readOnly = true)
    public List<Device> getDevicesByManufacturer(String manufacturer) {
        List<DeviceTable> tables = deviceRepository.findByManufacturer(manufacturer);
        return deviceMapper.toDTOList(tables);
    }

    // Retorna todos os dispositivos
    @Transactional(readOnly = true)
    public List<Device> getAllDevices() {
        List<DeviceTable> tables = deviceRepository.findAll();
        return deviceMapper.toDTOList(tables);
    }
}