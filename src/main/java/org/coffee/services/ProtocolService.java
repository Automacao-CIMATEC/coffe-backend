package org.coffee.services;

import org.coffee.utils.mappers.ProtocolMapper;
import org.coffee.domain.models.Protocol;
import org.coffee.domain.models.database.ProtocolTable;
import org.coffee.repository.ProtocolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProtocolService {

    private final ProtocolRepository protocolRepository;
    private final ProtocolMapper protocolMapper;

    @Autowired
    public ProtocolService(ProtocolRepository protocolRepository, ProtocolMapper protocolMapper) {
        this.protocolRepository = protocolRepository;
        this.protocolMapper = protocolMapper;
    }

    // Cria um novo protocolo
    public Protocol createProtocol(Protocol protocol) {
        // Validacoes de negocio
        if (!protocol.hasValidName()) {
            throw new RuntimeException("Nome do protocolo invalido. Deve ter pelo menos 3 caracteres");
        }

        // Verifica se o nome ja existe
        if (protocolRepository.existsByName(protocol.getName())) {
            throw new RuntimeException("Protocolo com este nome ja existe no sistema");
        }

        // Converte DTO para entidade
        ProtocolTable table = protocolMapper.toTable(protocol);

        // Salva no banco
        ProtocolTable savedTable = protocolRepository.save(table);

        // Converte de volta para DTO
        return protocolMapper.toDTO(savedTable);
    }

    // Atualiza um protocolo existente
    public Protocol updateProtocol(Long id, Protocol protocol) {
        // Busca a entidade existente
        ProtocolTable existingTable = protocolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Protocolo nao encontrado"));

        // Validacoes de negocio
        if (protocol.getName() != null && protocol.getName().trim().length() < 3) {
            throw new RuntimeException("Nome invalido. Deve ter pelo menos 3 caracteres");
        }

        // Verifica se o nome esta sendo alterado e se ja existe
        if (protocol.getName() != null && !existingTable.getName().equals(protocol.getName())) {
            if (protocolRepository.existsByName(protocol.getName())) {
                throw new RuntimeException("Nome ja esta em uso por outro protocolo");
            }
        }

        // Atualiza a entidade com dados do DTO
        protocolMapper.updateTableFromDTO(existingTable, protocol);

        // Salva as alteracoes
        ProtocolTable updatedTable = protocolRepository.save(existingTable);

        // Retorna como DTO
        return protocolMapper.toDTO(updatedTable);
    }

    // Remove um protocolo
    public void deleteProtocol(Long id) {
        if (!protocolRepository.existsById(id)) {
            throw new RuntimeException("Protocolo nao encontrado");
        }
        protocolRepository.deleteById(id);
    }

    // Busca um protocolo por ID
    @Transactional(readOnly = true)
    public Optional<Protocol> getProtocolById(Long id) {
        return protocolRepository.findById(id)
                .map(protocolMapper::toDTO);
    }

    // Busca um protocolo por nome
    @Transactional(readOnly = true)
    public Optional<Protocol> getProtocolByName(String name) {
        return protocolRepository.findByName(name)
                .map(protocolMapper::toDTO);
    }

    // Retorna todos os protocolos
    @Transactional(readOnly = true)
    public List<Protocol> getAllProtocols() {
        List<ProtocolTable> tables = protocolRepository.findAll();
        return protocolMapper.toDTOList(tables);
    }
}