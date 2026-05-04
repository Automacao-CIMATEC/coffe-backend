package org.coffee.repository;

import org.coffee.domain.models.database.PlcTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlcRepository extends JpaRepository<PlcTable, Long> {

    // Busca um PLC pelo nome
    Optional<PlcTable> findByName(String name);

    // Busca PLCs por fabricante
    List<PlcTable> findByManufacturer(String manufacturer);

    // Busca PLCs por IP
    List<PlcTable> findByIp(String ip);

    // Verifica se existe um PLC com o nome informado
    boolean existsByName(String name);
}