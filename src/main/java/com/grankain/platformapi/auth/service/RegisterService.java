package com.grankain.platformapi.auth.service;

import java.util.Objects;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.grankain.platformapi.auth.domain.Account;
import com.grankain.platformapi.auth.domain.login.AccessCounterFailure;
import com.grankain.platformapi.auth.domain.vo.Email;
import com.grankain.platformapi.auth.domain.vo.Password;
import com.grankain.platformapi.auth.domain.vo.Username;
import com.grankain.platformapi.auth.dto.request.RegisterRequest;
import com.grankain.platformapi.auth.dto.response.RegisterResponse;
import com.grankain.platformapi.auth.exceptions.AccountAlreadyExistsException;
import com.grankain.platformapi.auth.repository.AccountRepository;

/**
 * Service responsável pela lógica de registro de novas contas.
 * Aqui residem as regras de negócio do domínio de autenticação.
 */
@Service
public class RegisterService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccessCounterFailure accessCounterFailure;

    // Construtor para injeção de dependências gerenciadas pelo Spring IoC Container.
    public RegisterService(AccountRepository accountRepository, PasswordEncoder passwordEncoder, AccessCounterFailure accessCounterFailure) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.accessCounterFailure = accessCounterFailure;
    }

    /**
     * Executa o fluxo de registro de um novo usuário.
     *
     * @param register DTO com os dados da requisição.
     * @return DTO com os dados da conta criada para retorno à API.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public RegisterResponse authRegister(RegisterRequest register, String ipUser) {

        // Encapsulamento em Value Objects: Garante que os dados sejam válidos por design.
        Username username = new Username(register.user());
        Email email = new Email(register.email());
        Password password = new Password(register.password());

        // Regra de Negócio: Não permite duplicidade de e-mail ou nome de usuário.
        boolean isValidEmail = accountRepository.existsByEmailOrUser(email, username);
        if (isValidEmail) {
            // Lança exceção de conflito se os dados já existirem no banco.
            throw new AccountAlreadyExistsException("Usuário ou e-mail já em uso.");
        }

        // Criptografia: Gera o Hash da senha utilizando o algoritmo configurado (ex: Argon2/BCrypt).
        // Nunca salve senhas em texto puro!
        String hash = passwordEncoder.encode(password.value());

        // Criação da Entidade de Domínio.
        Account user = Account.forRegistration(
                register.name(),
                register.lastname(),
                email,
                register.birthday(),
                username,
                hash
        );

        // Garante que não é nulo (se for, ele lança o erro na hora com a mensagem)
        Objects.requireNonNull(user, "The Account object cannot be null when attempting to save.");

        // Persistência: Salva a nova conta no banco de dados através do JPA.
        accountRepository.save(user);

        // Sucesso no cadastro: reseta o contador de falhas do IP
        accessCounterFailure.resetIpCounter(ipUser);

        // Mapeamento: Converte a entidade de volta para um DTO de resposta (Response).
        return new RegisterResponse(user.getUser().toString(), user.getEmail().toString());
    }
}


