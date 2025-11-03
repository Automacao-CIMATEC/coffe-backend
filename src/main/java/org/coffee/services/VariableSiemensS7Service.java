package org.coffee.services;

import org.coffee.utils.mappers.VariableSiemensS7Mapper;
import org.coffee.domain.models.VariableSiemensS7;
import org.coffee.domain.models.database.VariableSiemensS7Table;
import org.coffee.domain.models.database.DeviceTable;
import org.coffee.repository.VariableSiemensS7Repository;
import org.coffee.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VariableSiemensS7Service {

    private final VariableSiemensS7Repository variableSiemensS7Repository;
    private final DeviceRepository deviceRepository;
    private final VariableSiemensS7Mapper variableSiemensS7Mapper;

    @Autowired
    public VariableSiemensS7Service(VariableSiemensS7Repository variableSiemensS7Repository,
                                    DeviceRepository deviceRepository,
                                    VariableSiemensS7Mapper variableSiemensS7Mapper) {
        this.variableSiemensS7Repository = variableSiemensS7Repository;
        this.deviceRepository = deviceRepository;
        this.variableSiemensS7Mapper = variableSiemensS7Mapper;
    }

    // Cria uma nova variavel Siemens S7
    public VariableSiemensS7 createVariableSiemensS7(VariableSiemensS7 variable) {
        // Validacoes de negocio
        if (!variable.hasValidName()) {
            throw new RuntimeException("Nome da variavel invalido. Deve ter pelo menos 2 caracteres");
        }

        if (!variable.hasValidDbNumber()) {
            throw new RuntimeException("Numero da DB invalido. Deve ser maior ou igual a zero");
        }

        if (!variable.hasValidOffset()) {
            throw new RuntimeException("Offset invalido. Deve ser maior ou igual a zero");
        }

        if (!variable.hasValidBitOffset()) {
            throw new RuntimeException("Bit offset invalido. Deve estar entre 0 e 7");
        }

        if (!variable.hasValidDataType()) {
            throw new RuntimeException("Tipo de dados invalido. Use: BOOL, BYTE, WORD, DWORD, INT, DINT, REAL ou STRING");
        }

        if (variable.getDeviceId() == null) {
            throw new RuntimeException("Variavel deve ter um dispositivo associado");
        }

        // Busca o Device
        DeviceTable deviceTable = deviceRepository.findById(variable.getDeviceId())
                .orElseThrow(() -> new RuntimeException("Dispositivo nao encontrado"));

        // Verifica se ja existe uma variavel com o mesmo endereco no dispositivo
        Optional<VariableSiemensS7Table> existingVariable =
                variableSiemensS7Repository.findByDeviceIdAndDbNumberAndOffsetAndBitOffset(
                        variable.getDeviceId(),
                        variable.getDbNumber(),
                        variable.getOffset(),
                        variable.getBitOffset());

        if (existingVariable.isPresent()) {
            throw new RuntimeException("Ja existe uma variavel com este endereco (DB, offset, bit offset) no dispositivo");
        }

        // Converte DTO para entidade
        VariableSiemensS7Table table = variableSiemensS7Mapper.toTable(variable);
        table.setDevice(deviceTable);

        // Salva no banco
        VariableSiemensS7Table savedTable = variableSiemensS7Repository.save(table);

        // Converte de volta para DTO
        return variableSiemensS7Mapper.toDTO(savedTable);
    }

    // Atualiza uma variavel Siemens S7 existente
    public VariableSiemensS7 updateVariableSiemensS7(Long id, VariableSiemensS7 variable) {
        // Busca a entidade existente
        VariableSiemensS7Table existingTable = variableSiemensS7Repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Variavel nao encontrada"));

        // Validacoes de negocio
        if (variable.getName() != null && variable.getName().trim().length() < 2) {
            throw new RuntimeException("Nome invalido. Deve ter pelo menos 2 caracteres");
        }

        if (variable.getDbNumber() != null && variable.getDbNumber() < 0) {
            throw new RuntimeException("Numero da DB invalido");
        }

        if (variable.getOffset() != null && variable.getOffset() < 0) {
            throw new RuntimeException("Offset invalido");
        }

        if (variable.getBitOffset() != null && !variable.hasValidBitOffset()) {
            throw new RuntimeException("Bit offset invalido. Deve estar entre 0 e 7");
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

        // Verifica duplicacao de endereco se algum dos campos de endereco estiver sendo atualizado
        if (variable.getDbNumber() != null || variable.getOffset() != null || variable.getBitOffset() != null) {
            Long deviceId = existingTable.getDevice().getId();
            Integer dbNumber = variable.getDbNumber() != null ? variable.getDbNumber() : existingTable.getDbNumber();
            Integer offset = variable.getOffset() != null ? variable.getOffset() : existingTable.getOffset();
            Integer bitOffset = variable.getBitOffset() != null ? variable.getBitOffset() : existingTable.getBitOffset();

            Optional<VariableSiemensS7Table> duplicateVariable =
                    variableSiemensS7Repository.findByDeviceIdAndDbNumberAndOffsetAndBitOffset(
                            deviceId, dbNumber, offset, bitOffset);

            if (duplicateVariable.isPresent() && !duplicateVariable.get().getId().equals(id)) {
                throw new RuntimeException("Ja existe uma variavel com este endereco no dispositivo");
            }
        }

        // Atualiza a entidade com dados do DTO
        variableSiemensS7Mapper.updateTableFromDTO(existingTable, variable);

        // Salva as alteracoes
        VariableSiemensS7Table updatedTable = variableSiemensS7Repository.save(existingTable);

        // Retorna como DTO
        return variableSiemensS7Mapper.toDTO(updatedTable);
    }

    // Remove uma variavel Siemens S7
    public void deleteVariableSiemensS7(Long id) {
        if (!variableSiemensS7Repository.existsById(id)) {
            throw new RuntimeException("Variavel nao encontrada");
        }
        variableSiemensS7Repository.deleteById(id);
    }

    // Busca uma variavel Siemens S7 por ID
    @Transactional(readOnly = true)
    public Optional<VariableSiemensS7> getVariableSiemensS7ById(Long id) {
        return variableSiemensS7Repository.findById(id)
                .map(variableSiemensS7Mapper::toDTO);
    }

    // Busca variaveis Siemens S7 por dispositivo
    @Transactional(readOnly = true)
    public List<VariableSiemensS7> getVariableSiemensS7ByDeviceId(Long deviceId) {
        List<VariableSiemensS7Table> tables = variableSiemensS7Repository.findByDeviceId(deviceId);
        return variableSiemensS7Mapper.toDTOList(tables);
    }

    // Busca variaveis Siemens S7 por tipo de dados
    @Transactional(readOnly = true)
    public List<VariableSiemensS7> getVariableSiemensS7ByDataType(String dataType) {
        List<VariableSiemensS7Table> tables = variableSiemensS7Repository.findByDataType(dataType);
        return variableSiemensS7Mapper.toDTOList(tables);
    }

    // Busca variaveis Siemens S7 por numero da DB
    @Transactional(readOnly = true)
    public List<VariableSiemensS7> getVariableSiemensS7ByDbNumber(Integer dbNumber) {
        List<VariableSiemensS7Table> tables = variableSiemensS7Repository.findByDbNumber(dbNumber);
        return variableSiemensS7Mapper.toDTOList(tables);
    }

    // Retorna todas as variaveis Siemens S7
    @Transactional(readOnly = true)
    public List<VariableSiemensS7> getAllVariablesSiemensS7() {
        List<VariableSiemensS7Table> tables = variableSiemensS7Repository.findAll();
        return variableSiemensS7Mapper.toDTOList(tables);
    }
}