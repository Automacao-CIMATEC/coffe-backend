package org.coffee.repository;

import org.coffee.domain.models.database.DeviceTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<DeviceTable, Long> {

    /**
     * Busca um dispositivo pelo nome
     * @param deviceName nome do dispositivo
     * @return Optional contendo o dispositivo se encontrado
     */
    Optional<DeviceTable> findByDeviceName(String deviceName);

    /**
     * Busca dispositivos por tipo
     * @param deviceType tipo do dispositivo
     * @return lista de dispositivos do tipo especificado
     */
    List<DeviceTable> findByDeviceType(String deviceType);

    /**
     * Busca dispositivos por protocolo
     * @param protocol protocolo de comunicação
     * @return lista de dispositivos com o protocolo especificado
     */
    List<DeviceTable> findByProtocol(String protocol);

    /**
     * Busca dispositivos ativos
     * @param isActive status de ativação
     * @return lista de dispositivos ativos/inativos
     */
    List<DeviceTable> findByIsActive(Boolean isActive);

    /**
     * Busca dispositivos por status
     * @param status status do dispositivo (online/offline)
     * @return lista de dispositivos com o status especificado
     */
    List<DeviceTable> findByStatus(String status);
}