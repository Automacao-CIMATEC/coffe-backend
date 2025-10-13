package org.coffee.services;

import org.coffee.utils.mappers.UserMapper;
import org.coffee.domain.models.User;
import org.coffee.domain.models.database.UserTable;
import org.coffee.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Autowired
    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    /**
     * Cria um novo usuario
     * @param user DTO com dados do usuario
     * @return DTO do usuario criado
     */
    public User createUser(User user) {
        // Validacoes de negocio
        if (!user.hasValidName()) {
            throw new RuntimeException("Nome do usuario invalido. Deve ter pelo menos 3 caracteres");
        }

        if (!user.hasValidEmail()) {
            throw new RuntimeException("Email invalido");
        }

        // Verifica se o email ja existe
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email ja cadastrado no sistema");
        }

        // Converte DTO para entidade
        UserTable table = userMapper.toTable(user);

        // Salva no banco
        UserTable savedTable = userRepository.save(table);

        // Converte de volta para DTO
        return userMapper.toDTO(savedTable);
    }

    /**
     * Atualiza um usuario existente
     * @param id identificador do usuario
     * @param user DTO com dados atualizados
     * @return DTO do usuario atualizado
     */
    public User updateUser(Long id, User user) {
        // Busca a entidade existente
        UserTable existingTable = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));

        // Validacoes de negocio
        if (user.getName() != null && user.getName().trim().length() < 3) {
            throw new RuntimeException("Nome invalido. Deve ter pelo menos 3 caracteres");
        }

        // Verifica se o email esta sendo alterado e se ja existe
        if (user.getEmail() != null && !existingTable.getEmail().equals(user.getEmail())) {
            if (!user.hasValidEmail()) {
                throw new RuntimeException("Email invalido");
            }
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new RuntimeException("Email ja esta em uso por outro usuario");
            }
        }

        // Atualiza a entidade com dados do DTO
        userMapper.updateTableFromDTO(existingTable, user);

        // Salva as alteracoes
        UserTable updatedTable = userRepository.save(existingTable);

        // Retorna como DTO
        return userMapper.toDTO(updatedTable);
    }

    /**
     * Remove um usuario
     * @param id identificador do usuario
     */
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Usuario nao encontrado");
        }
        userRepository.deleteById(id);
    }

    /**
     * Busca um usuario por ID
     * @param id identificador do usuario
     * @return Optional contendo o DTO se encontrado
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDTO);
    }

    /**
     * Busca um usuario por email
     * @param email email do usuario
     * @return Optional contendo o DTO se encontrado
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toDTO);
    }

    /**
     * Retorna todos os usuarios
     * @return lista de DTOs
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        List<UserTable> tables = userRepository.findAll();
        return userMapper.toDTOList(tables);
    }

    /**
     * Exemplo: metodo que processa usuarios em memoria
     * Busca usuarios e realiza operacoes sem tocar no banco
     * @return lista de usuarios com emails validos
     */
    @Transactional(readOnly = true)
    public List<User> getUsersWithValidEmails() {
        // Busca todas as entidades do banco
        List<UserTable> tables = userRepository.findAll();

        // Converte para DTOs (objetos em memoria)
        List<User> users = userMapper.toDTOList(tables);

        // Processa em memoria sem tocar no banco
        return users.stream()
                .filter(User::hasValidEmail)
                .toList();
    }
}