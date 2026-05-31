package com.grankain.platformapi.auth.service;

import java.util.Objects;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.grankain.platformapi.user.domain.PlatformUser;
import com.grankain.platformapi.user.domain.vo.Email;
import com.grankain.platformapi.user.domain.vo.Password;
import com.grankain.platformapi.user.domain.vo.Username;
import com.grankain.platformapi.user.dto.request.RegisterRequest;
import com.grankain.platformapi.user.dto.response.RegisterResponse;
import com.grankain.platformapi.user.exceptions.UserAlreadyExistsException;
import com.grankain.platformapi.user.repository.PlatformUserRepository;
import com.grankain.platformapi.auth.domain.login.AccessCounterFailure;

/**
 * Service responsável pela lógica de registro de novas contas na plataforma.
 * Aqui residem as regras de negócio do domínio de autenticação para criação de usuários.
 */
@Service
public class RegisterService {

    private final PlatformUserRepository platformUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccessCounterFailure accessCounterFailure;

    public RegisterService(PlatformUserRepository platformUserRepository, PasswordEncoder passwordEncoder,
            AccessCounterFailure accessCounterFailure) {
        this.platformUserRepository = platformUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.accessCounterFailure = accessCounterFailure;
    }

    /**
     * Executa o fluxo de registro de um novo usuário na plataforma.
     *
     * @param register DTO com os dados da requisição.
     * @param ipUser   IP do cliente para controle de tentativas.
     * @return DTO com os dados da conta criada para retorno à API.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public RegisterResponse authRegister(RegisterRequest register, String ipUser) {

        // Encapsulamento em Value Objects: garante que os dados sejam válidos por design.
        Username username = new Username(register.user());
        Email email = new Email(register.email());
        Password password = new Password(register.password());

        // Regra de Negócio: não permite duplicidade de e-mail ou nome de usuário.
        boolean alreadyExists = platformUserRepository.existsByEmailOrUser(email, username);
        if (alreadyExists) {
            throw new UserAlreadyExistsException("Usuário ou e-mail já em uso.");
        }

        // Criptografia: gera o Hash da senha utilizando Argon2id.
        String hash = passwordEncoder.encode(password.value());

        // Criação da entidade de domínio via factory method.
        PlatformUser user = PlatformUser.newUser(
                register.name(),
                register.lastname(),
                email,
                username,
                hash);

        Objects.requireNonNull(user, "The PlatformUser object cannot be null when attempting to save.");

        // Persistência: salva a nova conta no banco de dados através do JPA.
        platformUserRepository.save(user);

        // Sucesso no cadastro: reseta o contador de falhas do IP.
        accessCounterFailure.resetIpCounter(ipUser);

        // Mapeamento: converte a entidade de volta para um DTO de resposta.
        return new RegisterResponse(user.getUser().toString(), user.getEmail().toString());
    }
}
