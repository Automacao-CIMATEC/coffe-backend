package org.coffee.services;

import org.coffee.utils.mappers.VariableMqttMapper;
import org.coffee.domain.models.VariableMqtt;
import org.coffee.domain.models.database.VariableMqttTable;
import org.coffee.domain.models.database.DeviceTable;
import org.coffee.repository.VariableMqttRepository;
import org.coffee.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VariableMqttService {

    private final VariableMqttRepository variableMqttRepository;
    private final DeviceRepository deviceRepository;
    private final VariableMqttMapper variableMqttMapper;

    @Autowired
    public VariableMqttService(VariableMqttRepository variableMqttRepository,
                               DeviceRepository deviceRepository,
                               VariableMqttMapper variableMqttMapper) {
        this.variableMqttRepository = variableMqttRepository;
        this.deviceRepository = deviceRepository;
        this.variableMqttMapper = variableMqttMapper;
    }

    // Cria uma nova variavel MQTT
    public VariableMqtt createVariableMqtt(VariableMqtt variable) {
        // Validacoes de negocio
        if (!variable.hasValidName()) {
            throw new RuntimeException("Nome da variavel invalido. Deve ter pelo menos 2 caracteres");
        }

        if (!variable.hasValidBrokerIp()) {
            throw new RuntimeException("Broker IP invalido");
        }

        if (!variable.hasValidTopic()) {
            throw new RuntimeException("Topico invalido");
        }

        if (!variable.hasValidPort()) {
            throw new RuntimeException("Porta invalida. Deve estar entre 1 e 65535");
        }

        if (!variable.hasValidQos()) {
            throw new RuntimeException("QoS invalido. Deve estar entre 0 e 2");
        }

        if (variable.getDeviceId() == null) {
            throw new RuntimeException("Variavel deve ter um dispositivo associado");
        }

        // Busca o Device
        DeviceTable deviceTable = deviceRepository.findById(variable.getDeviceId())
                .orElseThrow(() -> new RuntimeException("Dispositivo nao encontrado"));

        // Converte DTO para entidade
        VariableMqttTable table = variableMqttMapper.toTable(variable);
        table.setDevice(deviceTable);

        // Salva no banco
        VariableMqttTable savedTable = variableMqttRepository.save(table);

        // Converte de volta para DTO
        return variableMqttMapper.toDTO(savedTable);
    }

    // Atualiza uma variavel MQTT existente
    public VariableMqtt updateVariableMqtt(Long id, VariableMqtt variable) {
        // Busca a entidade existente
        VariableMqttTable existingTable = variableMqttRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Variavel nao encontrada"));

        // Validacoes de negocio
        if (variable.getName() != null && variable.getName().trim().length() < 2) {
            throw new RuntimeException("Nome invalido. Deve ter pelo menos 2 caracteres");
        }

        if (variable.getBrokerIp() != null && variable.getBrokerIp().trim().isEmpty()) {
            throw new RuntimeException("Broker IP invalido");
        }

        if (variable.getTopic() != null && variable.getTopic().trim().isEmpty()) {
            throw new RuntimeException("Topico invalido");
        }

        if (variable.getPort() != null && !variable.hasValidPort()) {
            throw new RuntimeException("Porta invalida. Deve estar entre 1 e 65535");
        }

        if (variable.getQos() != null && !variable.hasValidQos()) {
            throw new RuntimeException("QoS invalido. Deve estar entre 0 e 2");
        }

        // Atualiza Device se fornecido
        if (variable.getDeviceId() != null) {
            DeviceTable deviceTable = deviceRepository.findById(variable.getDeviceId())
                    .orElseThrow(() -> new RuntimeException("Dispositivo nao encontrado"));
            existingTable.setDevice(deviceTable);
        }

        // Atualiza a entidade com dados do DTO
        variableMqttMapper.updateTableFromDTO(existingTable, variable);

        // Salva as alteracoes
        VariableMqttTable updatedTable = variableMqttRepository.save(existingTable);

        // Retorna como DTO
        return variableMqttMapper.toDTO(updatedTable);
    }

    // Remove uma variavel MQTT
    public void deleteVariableMqtt(Long id) {
        if (!variableMqttRepository.existsById(id)) {
            throw new RuntimeException("Variavel nao encontrada");
        }
        variableMqttRepository.deleteById(id);
    }

    // Busca uma variavel MQTT por ID
    @Transactional(readOnly = true)
    public Optional<VariableMqtt> getVariableMqttById(Long id) {
        return variableMqttRepository.findById(id)
                .map(variableMqttMapper::toDTO);
    }

    // Busca variaveis MQTT por dispositivo
    @Transactional(readOnly = true)
    public List<VariableMqtt> getVariableMqttByDeviceId(Long deviceId) {
        List<VariableMqttTable> tables = variableMqttRepository.findByDeviceId(deviceId);
        return variableMqttMapper.toDTOList(tables);
    }

    // Busca variavel MQTT por topico
    @Transactional(readOnly = true)
    public Optional<VariableMqtt> getVariableMqttByTopic(String topic) {
        return variableMqttRepository.findByTopic(topic)
                .map(variableMqttMapper::toDTO);
    }

    // Busca variaveis MQTT por QoS
    @Transactional(readOnly = true)
    public List<VariableMqtt> getVariableMqttByQos(Integer qos) {
        List<VariableMqttTable> tables = variableMqttRepository.findByQos(qos);
        return variableMqttMapper.toDTOList(tables);
    }

    // Retorna todas as variaveis MQTT
    @Transactional(readOnly = true)
    public List<VariableMqtt> getAllVariablesMqtt() {
        List<VariableMqttTable> tables = variableMqttRepository.findAll();
        return variableMqttMapper.toDTOList(tables);
    }
}