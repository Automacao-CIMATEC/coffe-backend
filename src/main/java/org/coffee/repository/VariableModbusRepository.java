package org.coffee.repository;

import org.coffee.domain.models.database.VariableModbusTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VariableModbusRepository extends JpaRepository<VariableModbusTable, Long> {
    // Busca variaveis por dispositivo
    List<VariableModbusTable> findByDeviceId(Long deviceId);
    // Busca variaveis por endereco e unitId
    List<VariableModbusTable> findByAddressAndUnitId(Integer address, Integer unitId);
    // Busca variaveis por tipo de registrador
    List<VariableModbusTable> findByRegisterType(String registerType);
    // Busca variaveis por dispositivo e tipo de registrador
    List<VariableModbusTable> findByDeviceIdAndRegisterType(Long deviceId, String registerType);
}