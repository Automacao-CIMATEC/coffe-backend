package org.coffee.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    /**
     * Configura o bean RestTemplate para comunicacao HTTP com APIs externas
     * Utilizado pelo EthernetIpOperationController para comunicar com a API Ethernet/IP
     *
     * @param builder construtor fornecido pelo Spring Boot
     * @return instancia configurada do RestTemplate
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        // Configura timeouts para evitar requisicoes travadas
        return builder
                .setConnectTimeout(Duration.ofSeconds(5))  // Timeout de conexao: 5 segundos
                .setReadTimeout(Duration.ofSeconds(10))    // Timeout de leitura: 10 segundos
                .build();
    }
}