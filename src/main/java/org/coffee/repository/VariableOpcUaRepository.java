package org.coffee.repository;

import org.coffee.domain.models.database.VariableOpcUaTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VariableOpcUaRepository extends JpaRepository<VariableOpcUaTable, Long> {

    // Busca variaveis por dispositivo
    List<VariableOpcUaTable> findByDeviceId(Long deviceId);

    // Busca variavel por nodeId e namespaceIndex
    Optional<VariableOpcUaTable> findByNodeIdAndNamespaceIndex(String nodeId, Integer namespaceIndex);

    // Busca variaveis por namespaceIndex
    List<VariableOpcUaTable> findByNamespaceIndex(Integer namespaceIndex);

    // Busca variaveis por dispositivo e namespaceIndex
    List<VariableOpcUaTable> findByDeviceIdAndNamespaceIndex(Long deviceId, Integer namespaceIndex);
}