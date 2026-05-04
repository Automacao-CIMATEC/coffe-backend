package org.coffee.services;

import org.coffee.utils.mappers.VariableModbusMapper;
import org.coffee.domain.models.VariableModbus;
import org.coffee.domain.models.database.VariableModbusTable;
import org.coffee.domain.models.database.DeviceTable;
import org.coffee.repository.VariableModbusRepository;
import org.coffee.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VariableModbusService {

    private final VariableModbusRepository variableModbusRepository;
    private final DeviceRepository deviceRepository;
    private final VariableModbusMapper variableModbusMapper;

    @Autowired
    public VariableModbusService(VariableModbusRepository variableModbusRepository,
                                 DeviceRepository deviceRepository,
                                 VariableModbusMapper variableModbusMapper) {
        this.variableModbusRepository = variableModbusRepository;
        this.deviceRepository = deviceRepository;
        this.variableModbusMapper = variableModbusMapper;
    }

    // Cria uma nova variavel Modbus
    public VariableModbus createVariableModbus(VariableModbus variable) {
        // Validacoes de negocio
        if (!variable.hasValidName()) {
            throw new RuntimeException("Nome da variavel invalido. Deve ter pelo menos 2 caracteres");
        }

        if (!variable.hasValidAddress()) {
            throw new RuntimeException("Endereco invalido");
        }

        if (!variable.hasValidPort()) {
            throw new RuntimeException("Porta invalida. Deve estar entre 1 e 65535");
        }

        if (!variable.hasValidRegisterType()) {
            throw new RuntimeException("Tipo de registrador invalido. Use: HOLDING, INPUT, COIL ou DISCRETE");
        }

        if (variable.getDeviceId() == null) {
            throw new RuntimeException("Variavel deve ter um dispositivo associado");
        }

        // Busca o Device
        DeviceTable deviceTable = deviceRepository.findById(variable.getDeviceId())
                .orElseThrow(() -> new RuntimeException("Dispositivo nao encontrado"));

        // Converte DTO para entidade
        VariableModbusTable table = variableModbusMapper.toTable(variable);
        table.setDevice(deviceTable);

        // Salva no banco
        VariableModbusTable savedTable = variableModbusRepository.save(table);

        // Converte de volta para DTO
        return variableModbusMapper.toDTO(savedTable);
    }

    // Atualiza uma variavel Modbus existente
    public VariableModbus updateVariableModbus(Long id, VariableModbus variable) {
        // Busca a entidade existente
        VariableModbusTable existingTable = variableModbusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Variavel nao encontrada"));

        // Validacoes de negocio
        if (variable.getName() != null && variable.getName().trim().length() < 2) {
            throw new RuntimeException("Nome invalido. Deve ter pelo menos 2 caracteres");
        }

        if (variable.getAddress() != null && variable.getAddress() < 0) {
            throw new RuntimeException("Endereco invalido");
        }

        if (variable.getPort() != null && !variable.hasValidPort()) {
            throw new RuntimeException("Porta invalida. Deve estar entre 1 e 65535");
        }

        if (variable.getRegisterType() != null && !variable.hasValidRegisterType()) {
            throw new RuntimeException("Tipo de registrador invalido");
        }

        // Atualiza Device se fornecido
        if (variable.getDeviceId() != null) {
            DeviceTable deviceTable = deviceRepository.findById(variable.getDeviceId())
                    .orElseThrow(() -> new RuntimeException("Dispositivo nao encontrado"));
            existingTable.setDevice(deviceTable);
        }

        // Atualiza a entidade com dados do DTO
        variableModbusMapper.updateTableFromDTO(existingTable, variable);

        // Salva as alteracoes
        VariableModbusTable updatedTable = variableModbusRepository.save(existingTable);

        // Retorna como DTO
        return variableModbusMapper.toDTO(updatedTable);
    }

    // Remove uma variavel Modbus
    public void deleteVariableModbus(Long id) {
        if (!variableModbusRepository.existsById(id)) {
            throw new RuntimeException("Variavel nao encontrada");
        }
        variableModbusRepository.deleteById(id);
    }

    // Busca uma variavel Modbus por ID
    @Transactional(readOnly = true)
    public Optional<VariableModbus> getVariableModbusById(Long id) {
        return variableModbusRepository.findById(id)
                .map(variableModbusMapper::toDTO);
    }

    // Busca variaveis Modbus por dispositivo
    @Transactional(readOnly = true)
    public List<VariableModbus> getVariableModbusByDeviceId(Long deviceId) {
        List<VariableModbusTable> tables = variableModbusRepository.findByDeviceId(deviceId);
        return variableModbusMapper.toDTOList(tables);
    }

    // Busca variaveis Modbus por tipo de registrador
    @Transactional(readOnly = true)
    public List<VariableModbus> getVariableModbusByRegisterType(String registerType) {
        List<VariableModbusTable> tables = variableModbusRepository.findByRegisterType(registerType);
        return variableModbusMapper.toDTOList(tables);
    }

    // Retorna todas as variaveis Modbus
    @Transactional(readOnly = true)
    public List<VariableModbus> getAllVariablesModbus() {
        List<VariableModbusTable> tables = variableModbusRepository.findAll();
        return variableModbusMapper.toDTOList(tables);
    }
}