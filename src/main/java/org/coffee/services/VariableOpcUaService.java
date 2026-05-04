package org.coffee.services;

import org.coffee.utils.mappers.VariableOpcUaMapper;
import org.coffee.domain.models.VariableOpcUa;
import org.coffee.domain.models.database.VariableOpcUaTable;
import org.coffee.domain.models.database.DeviceTable;
import org.coffee.repository.VariableOpcUaRepository;
import org.coffee.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VariableOpcUaService {

    private final VariableOpcUaRepository variableOpcUaRepository;
    private final DeviceRepository deviceRepository;
    private final VariableOpcUaMapper variableOpcUaMapper;

    @Autowired
    public VariableOpcUaService(VariableOpcUaRepository variableOpcUaRepository,
                                DeviceRepository deviceRepository,
                                VariableOpcUaMapper variableOpcUaMapper) {
        this.variableOpcUaRepository = variableOpcUaRepository;
        this.deviceRepository = deviceRepository;
        this.variableOpcUaMapper = variableOpcUaMapper;
    }

    // Cria uma nova variavel OPC-UA
    public VariableOpcUa createVariableOpcUa(VariableOpcUa variable) {
        // Validacoes de negocio
        if (!variable.hasValidName()) {
            throw new RuntimeException("Nome da variavel invalido. Deve ter pelo menos 2 caracteres");
        }

        if (!variable.hasValidNodeId()) {
            throw new RuntimeException("NodeId invalido");
        }

        if (!variable.hasValidPort()) {
            throw new RuntimeException("Porta invalida. Deve estar entre 1 e 65535");
        }

        if (!variable.hasValidNamespaceIndex()) {
            throw new RuntimeException("Namespace index invalido");
        }

        if (variable.getDeviceId() == null) {
            throw new RuntimeException("Variavel deve ter um dispositivo associado");
        }

        // Busca o Device
        DeviceTable deviceTable = deviceRepository.findById(variable.getDeviceId())
                .orElseThrow(() -> new RuntimeException("Dispositivo nao encontrado"));

        // Converte DTO para entidade
        VariableOpcUaTable table = variableOpcUaMapper.toTable(variable);
        table.setDevice(deviceTable);

        // Salva no banco
        VariableOpcUaTable savedTable = variableOpcUaRepository.save(table);

        // Converte de volta para DTO
        return variableOpcUaMapper.toDTO(savedTable);
    }

    // Atualiza uma variavel OPC-UA existente
    public VariableOpcUa updateVariableOpcUa(Long id, VariableOpcUa variable) {
        // Busca a entidade existente
        VariableOpcUaTable existingTable = variableOpcUaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Variavel nao encontrada"));

        // Validacoes de negocio
        if (variable.getName() != null && variable.getName().trim().length() < 2) {
            throw new RuntimeException("Nome invalido. Deve ter pelo menos 2 caracteres");
        }

        if (variable.getNodeId() != null && variable.getNodeId().trim().isEmpty()) {
            throw new RuntimeException("NodeId invalido");
        }

        if (variable.getPort() != null && !variable.hasValidPort()) {
            throw new RuntimeException("Porta invalida. Deve estar entre 1 e 65535");
        }

        if (variable.getNamespaceIndex() != null && variable.getNamespaceIndex() < 0) {
            throw new RuntimeException("Namespace index invalido");
        }

        // Atualiza Device se fornecido
        if (variable.getDeviceId() != null) {
            DeviceTable deviceTable = deviceRepository.findById(variable.getDeviceId())
                    .orElseThrow(() -> new RuntimeException("Dispositivo nao encontrado"));
            existingTable.setDevice(deviceTable);
        }

        // Atualiza a entidade com dados do DTO
        variableOpcUaMapper.updateTableFromDTO(existingTable, variable);

        // Salva as alteracoes
        VariableOpcUaTable updatedTable = variableOpcUaRepository.save(existingTable);

        // Retorna como DTO
        return variableOpcUaMapper.toDTO(updatedTable);
    }

    // Remove uma variavel OPC-UA
    public void deleteVariableOpcUa(Long id) {
        if (!variableOpcUaRepository.existsById(id)) {
            throw new RuntimeException("Variavel nao encontrada");
        }
        variableOpcUaRepository.deleteById(id);
    }

    // Busca uma variavel OPC-UA por ID
    @Transactional(readOnly = true)
    public Optional<VariableOpcUa> getVariableOpcUaById(Long id) {
        return variableOpcUaRepository.findById(id)
                .map(variableOpcUaMapper::toDTO);
    }

    // Busca variaveis OPC-UA por dispositivo
    @Transactional(readOnly = true)
    public List<VariableOpcUa> getVariableOpcUaByDeviceId(Long deviceId) {
        List<VariableOpcUaTable> tables = variableOpcUaRepository.findByDeviceId(deviceId);
        return variableOpcUaMapper.toDTOList(tables);
    }

    // Busca variaveis OPC-UA por namespaceIndex
    @Transactional(readOnly = true)
    public List<VariableOpcUa> getVariableOpcUaByNamespaceIndex(Integer namespaceIndex) {
        List<VariableOpcUaTable> tables = variableOpcUaRepository.findByNamespaceIndex(namespaceIndex);
        return variableOpcUaMapper.toDTOList(tables);
    }

    // Retorna todas as variaveis OPC-UA
    @Transactional(readOnly = true)
    public List<VariableOpcUa> getAllVariablesOpcUa() {
        List<VariableOpcUaTable> tables = variableOpcUaRepository.findAll();
        return variableOpcUaMapper.toDTOList(tables);
    }
}