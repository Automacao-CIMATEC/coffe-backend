package org.coffee.repository;

import org.coffee.domain.models.database.ProtocolTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProtocolRepository extends JpaRepository<ProtocolTable, Long> {

    // Busca um protocolo pelo nome
    Optional<ProtocolTable> findByName(String name);

    // Verifica se existe um protocolo com o nome informado
    boolean existsByName(String name);
}