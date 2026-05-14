package org.coffee.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuracao global de CORS (Cross-Origin Resource Sharing).
 *
 * Permite que o frontend realize requisicoes para a API (localhost:8080).
 * Sem esta configuracao, o browser bloqueia as requisicoes preflight (OPTIONS) com 403.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // Origens permitidas — localhost:3000 (producao) e localhost:5173 (Vite dev server)
                .allowedOrigins("http://localhost:3000", "http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                // Tempo em segundos que o browser pode cachear a resposta preflight
                .maxAge(3600);
    }
}