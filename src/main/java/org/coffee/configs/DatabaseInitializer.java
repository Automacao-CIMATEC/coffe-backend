package org.coffee.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

@Configuration
public class DatabaseInitializer {

    @Value("${spring.datasource.url}")
    private String dataSourceUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    /**
     * Bean que inicializa o banco de dados antes de qualquer outra configuracao
     * Este bean deve ser criado antes do DataSource do Spring
     */
    @Bean(name = "databaseInitializerBean")
    public Boolean initializeDatabase() {
        try {
            // Extrai o nome do banco da URL
            String databaseName = extractDatabaseName(dataSourceUrl);

            // URL base do PostgreSQL sem o nome do banco
            String postgresUrl = dataSourceUrl.substring(0, dataSourceUrl.lastIndexOf("/") + 1) + "postgres";

            // Conecta ao banco postgres padrao
            try (Connection conn = DriverManager.getConnection(postgresUrl, username, password)) {
                // Verifica se o banco ja existe
                if (!databaseExists(conn, databaseName)) {
                    // Cria o banco de dados
                    createDatabase(conn, databaseName);
                    System.out.println("==> Banco de dados '" + databaseName + "' criado com sucesso");
                } else {
                    System.out.println("==> Banco de dados '" + databaseName + "' ja existe");
                }
            }

            System.out.println("==> Inicializacao do banco de dados concluida");
            System.out.println("==> Spring JPA ira criar/atualizar as tabelas automaticamente");

            return true;

        } catch (Exception e) {
            System.err.println("Erro ao inicializar banco de dados: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Falha na inicializacao do banco de dados", e);
        }
    }

    /**
     * Extrai o nome do banco da URL de conexao
     * @param url URL completa do banco
     * @return nome do banco de dados
     */
    private String extractDatabaseName(String url) {
        // jdbc:postgresql://localhost:5432/nome_banco
        String[] parts = url.split("/");
        String dbNameWithParams = parts[parts.length - 1];
        // Remove parametros se existirem
        return dbNameWithParams.split("\\?")[0];
    }

    /**
     * Verifica se o banco de dados existe
     * @param conn conexao com o PostgreSQL
     * @param databaseName nome do banco a verificar
     * @return true se existe, false caso contrario
     */
    private boolean databaseExists(Connection conn, String databaseName) throws Exception {
        String query = "SELECT 1 FROM pg_database WHERE datname = '" + databaseName + "'";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            return rs.next();
        }
    }

    /**
     * Cria o banco de dados
     * @param conn conexao com o PostgreSQL
     * @param databaseName nome do banco a criar
     */
    private void createDatabase(Connection conn, String databaseName) throws Exception {
        String createDbQuery = "CREATE DATABASE " + databaseName;
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createDbQuery);
        }
    }
}