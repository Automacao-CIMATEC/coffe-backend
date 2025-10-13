package org.coffee.repository;

import org.coffee.domain.models.database.UserTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserTable, Long> {

    /**
     * Busca um usuário pelo email
     * @param email email do usuário
     * @return Optional contendo o usuário se encontrado
     */
    Optional<UserTable> findByEmail(String email);

    /**
     * Verifica se existe um usuário com o email informado
     * @param email email a verificar
     * @return true se existe, false caso contrário
     */
    boolean existsByEmail(String email);
}