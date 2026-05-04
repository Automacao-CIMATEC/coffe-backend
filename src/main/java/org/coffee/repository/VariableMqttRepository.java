package org.coffee.repository;

import org.coffee.domain.models.database.VariableMqttTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VariableMqttRepository extends JpaRepository<VariableMqttTable, Long> {

    // Busca variaveis por dispositivo
    List<VariableMqttTable> findByDeviceId(Long deviceId);

    // Busca variavel por topico
    Optional<VariableMqttTable> findByTopic(String topic);

    // Busca variaveis por QoS
    List<VariableMqttTable> findByQos(Integer qos);

    // Busca variaveis retidas
    List<VariableMqttTable> findByRetained(Boolean retained);

    // Busca variaveis por dispositivo e QoS
    List<VariableMqttTable> findByDeviceIdAndQos(Long deviceId, Integer qos);
}