package com.grankain.platformapi.auth.service;

import com.grankain.platformapi.auth.domain.UserAccess;
import com.grankain.platformapi.auth.dto.response.Register;
import com.grankain.platformapi.auth.domain.Account;
import com.grankain.platformapi.auth.domain.AccountStatus;
import com.grankain.platformapi.auth.exceptions.AccountAlreadyExistsException;
import com.grankain.platformapi.auth.repository.AccountRepository;
import com.grankain.platformapi.auth.domain.vo.Email;
import com.grankain.platformapi.auth.domain.vo.Password;
import com.grankain.platformapi.auth.domain.vo.Username;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service responsável pela lógica de registro de novas contas.
 * Aqui residem as regras de negócio do domínio de autenticação.
 */
@Service
public class RegisterService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    // Construtor para injeção de dependências gerenciadas pelo Spring IoC Container.
    public RegisterService(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Executa o fluxo de registro de um novo usuário.
     *
     * @param register DTO com os dados da requisição.
     * @return DTO com os dados da conta criada para retorno à API.
     */
    public Register authRegister(com.grankain.platformapi.auth.dto.request.Register register) {
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
        Account user = new Account(
                register.name(),
                register.lastname(),
                email,
                register.birthday(),
                username,
                AccountStatus.PENDING,
                hash,
                UserAccess.USER
        );

        // Persistência: Salva a nova conta no banco de dados através do JPA.
        accountRepository.save(user);

        // Mapeamento: Converte a entidade de volta para um DTO de resposta (Response).
        return new Register(user);
    }
}


