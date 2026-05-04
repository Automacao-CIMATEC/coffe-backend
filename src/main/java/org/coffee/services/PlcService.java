package org.coffee.services;

import org.coffee.utils.mappers.PlcMapper;
import org.coffee.domain.models.Plc;
import org.coffee.domain.models.database.PlcTable;
import org.coffee.repository.PlcRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PlcService {

    private final PlcRepository plcRepository;
    private final PlcMapper plcMapper;

    @Autowired
    public PlcService(PlcRepository plcRepository, PlcMapper plcMapper) {
        this.plcRepository = plcRepository;
        this.plcMapper = plcMapper;
    }

    // Cria um novo PLC
    public Plc createPlc(Plc plc) {
        // Validacoes de negocio
        if (!plc.hasValidName()) {
            throw new RuntimeException("Nome do PLC invalido. Deve ter pelo menos 3 caracteres");
        }

        if (!plc.hasValidIp()) {
            throw new RuntimeException("IP invalido");
        }

        // Verifica se o nome ja existe
        if (plcRepository.existsByName(plc.getName())) {
            throw new RuntimeException("PLC com este nome ja existe no sistema");
        }

        // Converte DTO para entidade
        PlcTable table = plcMapper.toTable(plc);

        // Salva no banco
        PlcTable savedTable = plcRepository.save(table);

        // Converte de volta para DTO
        return plcMapper.toDTO(savedTable);
    }

    // Atualiza um PLC existente
    public Plc updatePlc(Long id, Plc plc) {
        // Busca a entidade existente
        PlcTable existingTable = plcRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PLC nao encontrado"));

        // Validacoes de negocio
        if (plc.getName() != null && plc.getName().trim().length() < 3) {
            throw new RuntimeException("Nome invalido. Deve ter pelo menos 3 caracteres");
        }

        if (plc.getIp() != null && !plc.hasValidIp()) {
            throw new RuntimeException("IP invalido");
        }

        // Verifica se o nome esta sendo alterado e se ja existe
        if (plc.getName() != null && !existingTable.getName().equals(plc.getName())) {
            if (plcRepository.existsByName(plc.getName())) {
                throw new RuntimeException("Nome ja esta em uso por outro PLC");
            }
        }

        // Atualiza a entidade com dados do DTO
        plcMapper.updateTableFromDTO(existingTable, plc);

        // Salva as alteracoes
        PlcTable updatedTable = plcRepository.save(existingTable);

        // Retorna como DTO
        return plcMapper.toDTO(updatedTable);
    }

    // Remove um PLC
    public void deletePlc(Long id) {
        if (!plcRepository.existsById(id)) {
            throw new RuntimeException("PLC nao encontrado");
        }
        plcRepository.deleteById(id);
    }

    // Busca um PLC por ID
    @Transactional(readOnly = true)
    public Optional<Plc> getPlcById(Long id) {
        return plcRepository.findById(id)
                .map(plcMapper::toDTO);
    }

    // Busca um PLC por nome
    @Transactional(readOnly = true)
    public Optional<Plc> getPlcByName(String name) {
        return plcRepository.findByName(name)
                .map(plcMapper::toDTO);
    }

    // Busca PLCs por fabricante
    @Transactional(readOnly = true)
    public List<Plc> getPlcsByManufacturer(String manufacturer) {
        List<PlcTable> tables = plcRepository.findByManufacturer(manufacturer);
        return plcMapper.toDTOList(tables);
    }

    // Busca PLCs por IP
    @Transactional(readOnly = true)
    public List<Plc> getPlcsByIp(String ip) {
        List<PlcTable> tables = plcRepository.findByIp(ip);
        return plcMapper.toDTOList(tables);
    }

    // Retorna todos os PLCs
    @Transactional(readOnly = true)
    public List<Plc> getAllPlcs() {
        List<PlcTable> tables = plcRepository.findAll();
        return plcMapper.toDTOList(tables);
    }
}