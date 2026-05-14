package org.coffee.repository;

import org.coffee.domain.models.database.PlcSiemensS7ConfigTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlcSiemensS7ConfigRepository extends JpaRepository<PlcSiemensS7ConfigTable, Long> {

    // Busca a configuracao S7 de um PLC especifico (1:1)
    Optional<PlcSiemensS7ConfigTable> findByPlcId(Long plcId);

    // Verifica se um PLC ja possui configuracao S7
    boolean existsByPlcId(Long plcId);
}
