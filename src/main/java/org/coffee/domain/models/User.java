package org.coffee.domain.models;

import java.time.LocalDateTime;

public class User {

    // Objeto de transferencia de dados (DTO)
    // Usado para logica de negocio e API
    // Pode conter metodos auxiliares e validacoes

    private Long id;
    private String name;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Construtor vazio
    public User() {
    }

    // Construtor completo
    public User(Long id, String name, String email, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Construtor sem ID (para criacao)
    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // Metodos de logica de negocio

    /**
     * Verifica se o email e valido
     * @return true se o email contem @ e ponto
     */
    public boolean hasValidEmail() {
        return this.email != null &&
                this.email.contains("@") &&
                this.email.contains(".");
    }

    /**
     * Verifica se o nome e valido
     * @return true se o nome tem pelo menos 3 caracteres
     */
    public boolean hasValidName() {
        return this.name != null &&
                this.name.trim().length() >= 3;
    }

    /**
     * Retorna o nome formatado para exibicao
     * @return nome em maiusculas
     */
    public String getDisplayName() {
        return this.name != null ? this.name.toUpperCase() : "";
    }

    /**
     * Extrai o dominio do email
     * @return dominio do email ou string vazia
     */
    public String getEmailDomain() {
        if (this.email == null || !this.email.contains("@")) {
            return "";
        }
        return this.email.substring(this.email.indexOf("@") + 1);
    }

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}