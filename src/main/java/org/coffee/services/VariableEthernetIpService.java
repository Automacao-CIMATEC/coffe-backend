package org.coffee.services;

import org.coffee.utils.mappers.VariableEthernetIpMapper;
import org.coffee.domain.models.VariableEthernetIp;
import org.coffee.domain.models.database.VariableEthernetIpTable;
import org.coffee.domain.models.database.DeviceTable;
import org.coffee.repository.VariableEthernetIpRepository;
import org.coffee.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VariableEthernetIpService {

    private final VariableEthernetIpRepository variableEthernetIpRepository;
    private final DeviceRepository deviceRepository;
    private final VariableEthernetIpMapper variableEthernetIpMapper;

    @Autowired
    public VariableEthernetIpService(VariableEthernetIpRepository variableEthernetIpRepository,
                                     DeviceRepository deviceRepository,
                                     VariableEthernetIpMapper variableEthernetIpMapper) {
        this.variableEthernetIpRepository = variableEthernetIpRepository;
        this.deviceRepository = deviceRepository;
        this.variableEthernetIpMapper = variableEthernetIpMapper;
    }

    // Cria uma nova variavel Ethernet/IP
    public VariableEthernetIp createVariableEthernetIp(VariableEthernetIp variable) {
        // Validacoes de negocio
        if (!variable.hasValidName()) {
            throw new RuntimeException("Nome da variavel invalido. Deve ter pelo menos 2 caracteres");
        }

        if (!variable.hasValidTagName()) {
            throw new RuntimeException("Nome da tag invalido. Deve ter pelo menos 1 caractere");
        }

        if (!variable.hasValidDataType()) {
            throw new RuntimeException("Tipo de dados invalido. Use: BOOL, SINT, INT, DINT, REAL, FLOAT ou STRING");
        }

        if (variable.getDeviceId() == null) {
            throw new RuntimeException("Variavel deve ter um dispositivo associado");
        }

        // Busca o Device
        DeviceTable deviceTable = deviceRepository.findById(variable.getDeviceId())
                .orElseThrow(() -> new RuntimeException("Dispositivo nao encontrado"));

        // Verifica se ja existe uma variavel com o mesmo nome de tag no dispositivo
        Optional<VariableEthernetIpTable> existingVariable =
                variableEthernetIpRepository.findByDeviceIdAndTagName(variable.getDeviceId(), variable.getTagName());

        if (existingVariable.isPresent()) {
            throw new RuntimeException("Ja existe uma variavel com este nome de tag no dispositivo");
        }

        // Converte DTO para entidade
        VariableEthernetIpTable table = variableEthernetIpMapper.toTable(variable);
        table.setDevice(deviceTable);

        // Salva no banco
        VariableEthernetIpTable savedTable = variableEthernetIpRepository.save(table);

        // Converte de volta para DTO
        return variableEthernetIpMapper.toDTO(savedTable);
    }

    // Atualiza uma variavel Ethernet/IP existente
    public VariableEthernetIp updateVariableEthernetIp(Long id, VariableEthernetIp variable) {
        // Busca a entidade existente
        VariableEthernetIpTable existingTable = variableEthernetIpRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Variavel nao encontrada"));

        // Validacoes de negocio
        if (variable.getName() != null && variable.getName().trim().length() < 2) {
            throw new RuntimeException("Nome invalido. Deve ter pelo menos 2 caracteres");
        }

        if (variable.getTagName() != null && variable.getTagName().trim().length() < 1) {
            throw new RuntimeException("Nome da tag invalido. Deve ter pelo menos 1 caractere");
        }

        if (variable.getDataType() != null && !variable.hasValidDataType()) {
            throw new RuntimeException("Tipo de dados invalido");
        }

        // Atualiza Device se fornecido
        if (variable.getDeviceId() != null) {
            DeviceTable deviceTable = deviceRepository.findById(variable.getDeviceId())
                    .orElseThrow(() -> new RuntimeException("Dispositivo nao encontrado"));
            existingTable.setDevice(deviceTable);
        }

        // Verifica duplicacao de tag name se estiver sendo atualizado
        if (variable.getTagName() != null && !variable.getTagName().equals(existingTable.getTagName())) {
            Long deviceId = existingTable.getDevice().getId();
            Optional<VariableEthernetIpTable> duplicateVariable =
                    variableEthernetIpRepository.findByDeviceIdAndTagName(deviceId, variable.getTagName());

            if (duplicateVariable.isPresent()) {
                throw new RuntimeException("Ja existe uma variavel com este nome de tag no dispositivo");
            }
        }

        // Atualiza a entidade com dados do DTO
        variableEthernetIpMapper.updateTableFromDTO(existingTable, variable);

        // Salva as alteracoes
        VariableEthernetIpTable updatedTable = variableEthernetIpRepository.save(existingTable);

        // Retorna como DTO
        return variableEthernetIpMapper.toDTO(updatedTable);
    }

    // Remove uma variavel Ethernet/IP
    public void deleteVariableEthernetIp(Long id) {
        if (!variableEthernetIpRepository.existsById(id)) {
            throw new RuntimeException("Variavel nao encontrada");
        }
        variableEthernetIpRepository.deleteById(id);
    }

    // Busca uma variavel Ethernet/IP por ID
    @Transactional(readOnly = true)
    public Optional<VariableEthernetIp> getVariableEthernetIpById(Long id) {
        return variableEthernetIpRepository.findById(id)
                .map(variableEthernetIpMapper::toDTO);
    }

    // Busca variaveis Ethernet/IP por dispositivo
    @Transactional(readOnly = true)
    public List<VariableEthernetIp> getVariableEthernetIpByDeviceId(Long deviceId) {
        List<VariableEthernetIpTable> tables = variableEthernetIpRepository.findByDeviceId(deviceId);
        return variableEthernetIpMapper.toDTOList(tables);
    }

    // Busca variaveis Ethernet/IP por tipo de dados
    @Transactional(readOnly = true)
    public List<VariableEthernetIp> getVariableEthernetIpByDataType(String dataType) {
        List<VariableEthernetIpTable> tables = variableEthernetIpRepository.findByDataType(dataType);
        return variableEthernetIpMapper.toDTOList(tables);
    }

    // Retorna todas as variaveis Ethernet/IP
    @Transactional(readOnly = true)
    public List<VariableEthernetIp> getAllVariablesEthernetIp() {
        List<VariableEthernetIpTable> tables = variableEthernetIpRepository.findAll();
        return variableEthernetIpMapper.toDTOList(tables);
    }

    // Busca variavel por nome da tag
    @Transactional(readOnly = true)
    public Optional<VariableEthernetIp> getVariableEthernetIpByTagName(String tagName) {
        return variableEthernetIpRepository.findByTagName(tagName)
                .map(variableEthernetIpMapper::toDTO);
    }
}