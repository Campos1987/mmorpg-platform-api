package com.grankain.platformapi.auth.repository;

import com.grankain.platformapi.auth.entity.Account;
import com.grankain.platformapi.auth.valueObjects.Email;
import com.grankain.platformapi.auth.valueObjects.Username;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositório para a entidade Account.
 * O Spring Data JPA cria automaticamente a implementação desta interface em tempo de execução.
 */
public interface AccountRepository extends JpaRepository<Account, UUID> {

    /**
     * Verifica se já existe uma conta cadastrada com o e-mail OU nome de usuário informados.
     * Utilizado para validação de unicidade durante o registro.
     */
    Boolean existsByEmailOrUser(Email email, Username user);

    /**
     * Busca uma conta através do e-mail ou nome de usuário.
     * Retorna um Optional, o que obriga o chamador a tratar o caso onde o usuário não é encontrado.
     */
    Optional<Account> findAccountByEmailOrUser(Email email, Username user);
}

