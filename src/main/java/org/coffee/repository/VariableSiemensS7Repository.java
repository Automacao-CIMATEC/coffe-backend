package org.coffee.repository;

import org.coffee.domain.models.database.VariableSiemensS7Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VariableSiemensS7Repository extends JpaRepository<VariableSiemensS7Table, Long> {
    // Busca variaveis por dispositivo
    List<VariableSiemensS7Table> findByDeviceId(Long deviceId);

    // Busca variaveis por numero da DB
    List<VariableSiemensS7Table> findByDbNumber(Integer dbNumber);

    // Busca variaveis por dispositivo e numero da DB
    List<VariableSiemensS7Table> findByDeviceIdAndDbNumber(Long deviceId, Integer dbNumber);

    // Busca variavel por dispositivo, DB number, offset e bit offset (identificacao unica)
    Optional<VariableSiemensS7Table> findByDeviceIdAndDbNumberAndOffsetAndBitOffset(
            Long deviceId, Integer dbNumber, Integer offset, Integer bitOffset);

    // Busca variaveis por tipo de dados
    List<VariableSiemensS7Table> findByDataType(String dataType);

    // Busca variaveis por dispositivo e tipo de dados
    List<VariableSiemensS7Table> findByDeviceIdAndDataType(Long deviceId, String dataType);
}