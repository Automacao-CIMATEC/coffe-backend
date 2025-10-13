package org.coffee.utils.mappers;

import org.coffee.domain.models.User;
import org.coffee.domain.models.database.UserTable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    /**
     * Converte UserTable (entidade do banco) para User (DTO)
     * @param table entidade do banco de dados
     * @return objeto DTO para uso em logica de negocio
     */
    public User toDTO(UserTable table) {
        if (table == null) {
            return null;
        }

        return new User(
                table.getId(),
                table.getName(),
                table.getEmail(),
                table.getCreatedAt(),
                table.getUpdatedAt()
        );
    }

    /**
     * Converte User (DTO) para UserTable (entidade do banco)
     * @param dto objeto de negocio
     * @return entidade para persistencia no banco
     */
    public UserTable toTable(User dto) {
        if (dto == null) {
            return null;
        }

        UserTable table = new UserTable();
        table.setId(dto.getId());
        table.setName(dto.getName());
        table.setEmail(dto.getEmail());
        table.setCreatedAt(dto.getCreatedAt());
        table.setUpdatedAt(dto.getUpdatedAt());

        return table;
    }

    /**
     * Converte uma lista de UserTable para lista de User
     * @param tables lista de entidades do banco
     * @return lista de DTOs
     */
    public List<User> toDTOList(List<UserTable> tables) {
        if (tables == null) {
            return null;
        }

        return tables.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Converte uma lista de User para lista de UserTable
     * @param dtos lista de DTOs
     * @return lista de entidades para o banco
     */
    public List<UserTable> toTableList(List<User> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .map(this::toTable)
                .collect(Collectors.toList());
    }

    /**
     * Atualiza uma entidade existente com dados do DTO
     * Util para operacoes de update onde o ID ja existe
     * @param table entidade existente no banco
     * @param dto DTO com novos dados
     */
    public void updateTableFromDTO(UserTable table, User dto) {
        if (table == null || dto == null) {
            return;
        }

        // Nao atualiza o ID
        if (dto.getName() != null) {
            table.setName(dto.getName());
        }
        if (dto.getEmail() != null) {
            table.setEmail(dto.getEmail());
        }
        // createdAt e updatedAt sao gerenciados pelo @PreUpdate
    }
}