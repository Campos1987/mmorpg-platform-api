package com.grankain.platformapi.user.repository;

import com.grankain.platformapi.user.domain.PlatformUser;
import com.grankain.platformapi.user.domain.vo.Email;
import com.grankain.platformapi.user.domain.vo.Username;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositório para a entidade PlatformUser.
 * <p>
 * O Spring Data JPA cria automaticamente a implementação desta interface em
 * tempo de execução, gerenciada pelo EntityManager do banco 'db-web' (WebDatabase).
 */
public interface PlatformUserRepository extends JpaRepository<PlatformUser, UUID> {

    /**
     * Verifica se já existe uma conta cadastrada com o e-mail OU nome de usuário informados.
     * Utilizado para validação de unicidade durante o registro.
     */
    Boolean existsByEmailOrUser(Email email, Username user);

    /**
     * Busca um usuário através do e-mail.
     * Retorna um Optional, obrigando o chamador a tratar o caso de usuário não encontrado.
     */
    Optional<PlatformUser> findByEmail(Email email);

    /**
     * Busca um usuário através do nome de usuário (login).
     * Retorna um Optional, obrigando o chamador a tratar o caso de usuário não encontrado.
     */
    Optional<PlatformUser> findByUser(Username user);
}
