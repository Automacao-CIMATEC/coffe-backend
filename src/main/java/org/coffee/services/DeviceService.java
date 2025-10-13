package org.coffee.services;

import org.coffee.utils.mappers.DeviceMapper;
import org.coffee.domain.models.Device;
import org.coffee.domain.models.database.DeviceTable;
import org.coffee.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceMapper deviceMapper;

    @Autowired
    public DeviceService(DeviceRepository deviceRepository, DeviceMapper deviceMapper) {
        this.deviceRepository = deviceRepository;
        this.deviceMapper = deviceMapper;
    }

    /**
     * Cria um novo dispositivo
     * @param device DTO com dados do dispositivo
     * @return DTO do dispositivo criado
     */
    public Device createDevice(Device device) {
        // Validacoes de negocio
        if (device.getDeviceName() == null || device.getDeviceName().trim().isEmpty()) {
            throw new RuntimeException("Nome do dispositivo e obrigatorio");
        }

        if (!device.hasSupportedProtocol()) {
            throw new RuntimeException("Protocolo nao suportado. Use OPC-UA, Modbus ou MQTT");
        }

        // Converte DTO para entidade
        DeviceTable table = deviceMapper.toTable(device);

        // Salva no banco
        DeviceTable savedTable = deviceRepository.save(table);

        // Converte de volta para DTO
        return deviceMapper.toDTO(savedTable);
    }

    /**
     * Atualiza um dispositivo existente
     * @param id identificador do dispositivo
     * @param device DTO com dados atualizados
     * @return DTO do dispositivo atualizado
     */
    public Device updateDevice(Long id, Device device) {
        // Busca a entidade existente
        DeviceTable existingTable = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dispositivo nao encontrado"));

        // Validacoes de negocio
        if (device.getProtocol() != null) {
            Device tempDevice = new Device();
            tempDevice.setProtocol(device.getProtocol());
            if (!tempDevice.hasSupportedProtocol()) {
                throw new RuntimeException("Protocolo nao suportado");
            }
        }

        // Atualiza a entidade com dados do DTO
        deviceMapper.updateTableFromDTO(existingTable, device);

        // Salva as alteracoes
        DeviceTable updatedTable = deviceRepository.save(existingTable);

        // Retorna como DTO
        return deviceMapper.toDTO(updatedTable);
    }

    /**
     * Remove um dispositivo
     * @param id identificador do dispositivo
     */
    public void deleteDevice(Long id) {
        if (!deviceRepository.existsById(id)) {
            throw new RuntimeException("Dispositivo nao encontrado");
        }
        deviceRepository.deleteById(id);
    }

    /**
     * Busca um dispositivo por ID
     * @param id identificador do dispositivo
     * @return Optional contendo o DTO se encontrado
     */
    @Transactional(readOnly = true)
    public Optional<Device> getDeviceById(Long id) {
        return deviceRepository.findById(id)
                .map(deviceMapper::toDTO);
    }

    /**
     * Busca um dispositivo por nome
     * @param deviceName nome do dispositivo
     * @return Optional contendo o DTO se encontrado
     */
    @Transactional(readOnly = true)
    public Optional<Device> getDeviceByName(String deviceName) {
        return deviceRepository.findByDeviceName(deviceName)
                .map(deviceMapper::toDTO);
    }

    /**
     * Retorna todos os dispositivos
     * @return lista de DTOs
     */
    @Transactional(readOnly = true)
    public List<Device> getAllDevices() {
        List<DeviceTable> tables = deviceRepository.findAll();
        return deviceMapper.toDTOList(tables);
    }

    /**
     * Busca dispositivos por tipo
     * @param deviceType tipo do dispositivo
     * @return lista de DTOs
     */
    @Transactional(readOnly = true)
    public List<Device> getDevicesByType(String deviceType) {
        List<DeviceTable> tables = deviceRepository.findByDeviceType(deviceType);
        return deviceMapper.toDTOList(tables);
    }

    /**
     * Busca dispositivos por protocolo
     * @param protocol protocolo de comunicacao
     * @return lista de DTOs
     */
    @Transactional(readOnly = true)
    public List<Device> getDevicesByProtocol(String protocol) {
        List<DeviceTable> tables = deviceRepository.findByProtocol(protocol);
        return deviceMapper.toDTOList(tables);
    }

    /**
     * Busca dispositivos ativos
     * @return lista de DTOs de dispositivos ativos
     */
    @Transactional(readOnly = true)
    public List<Device> getActiveDevices() {
        List<DeviceTable> tables = deviceRepository.findByIsActive(true);
        return deviceMapper.toDTOList(tables);
    }

    /**
     * Busca dispositivos online
     * @return lista de DTOs de dispositivos online
     */
    @Transactional(readOnly = true)
    public List<Device> getOnlineDevices() {
        List<DeviceTable> tables = deviceRepository.findByStatus("online");
        return deviceMapper.toDTOList(tables);
    }

    /**
     * Exemplo: metodo que processa dispositivos em memoria
     * Busca dispositivos e realiza operacoes sem tocar no banco
     * @return lista de dispositivos disponiveis para uso
     */
    @Transactional(readOnly = true)
    public List<Device> getAvailableDevices() {
        // Busca todas as entidades do banco
        List<DeviceTable> tables = deviceRepository.findAll();

        // Converte para DTOs (objetos em memoria)
        List<Device> devices = deviceMapper.toDTOList(tables);

        // Processa em memoria sem tocar no banco
        // Usa metodos de logica de negocio do DTO
        return devices.stream()
                .filter(Device::isAvailable)
                .toList();
    }
}