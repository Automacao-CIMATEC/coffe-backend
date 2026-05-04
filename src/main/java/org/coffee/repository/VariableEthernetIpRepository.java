package org.coffee.repository;

import org.coffee.domain.models.database.VariableEthernetIpTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VariableEthernetIpRepository extends JpaRepository<VariableEthernetIpTable, Long> {
    // Busca variaveis por dispositivo
    List<VariableEthernetIpTable> findByDeviceId(Long deviceId);

    // Busca variavel por nome da tag
    Optional<VariableEthernetIpTable> findByTagName(String tagName);

    // Busca variaveis por dispositivo e nome da tag
    Optional<VariableEthernetIpTable> findByDeviceIdAndTagName(Long deviceId, String tagName);

    // Busca variaveis por tipo de dados
    List<VariableEthernetIpTable> findByDataType(String dataType);

    // Busca variaveis por dispositivo e tipo de dados
    List<VariableEthernetIpTable> findByDeviceIdAndDataType(Long deviceId, String dataType);
}