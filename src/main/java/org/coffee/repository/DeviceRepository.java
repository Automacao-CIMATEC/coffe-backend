package org.coffee.repository;

import org.coffee.domain.models.database.DeviceTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<DeviceTable, Long> {

    // Busca um dispositivo pelo nome
    Optional<DeviceTable> findByName(String name);

    // Busca dispositivos por PLC
    List<DeviceTable> findByPlcId(Long plcId);

    // Busca dispositivos por protocolo
    List<DeviceTable> findByProtocolId(Long protocolId);

    // Busca dispositivos por fabricante
    List<DeviceTable> findByManufacturer(String manufacturer);

    // Verifica se existe um dispositivo com o nome informado
    boolean existsByName(String name);
}