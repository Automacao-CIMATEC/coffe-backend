package org.coffee.configs;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "org.coffee.repository")
@EntityScan(basePackages = "org.coffee.domain.models.database")
public class JpaConfig {

    /**
     * Configuração do JPA para o Spring Boot
     *
     * @EnableJpaRepositories: habilita os repositórios JPA no pacote especificado
     * @EntityScan: escaneia as entidades JPA no pacote especificado
     * @EnableTransactionManagement: habilita o gerenciamento de transações
     *
     * IMPORTANTE: Todas as classes anotadas com @Entity no pacote
     * org.coffee.domain.models.database serão automaticamente detectadas
     * e suas tabelas serão criadas no banco de dados
     */
}