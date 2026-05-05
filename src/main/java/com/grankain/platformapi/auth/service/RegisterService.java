package com.grankain.platformapi.auth.service;

import com.grankain.platformapi.auth.dto.RequestRegister;
import com.grankain.platformapi.auth.dto.ResponseRegister;
import com.grankain.platformapi.auth.entity.Account;
import com.grankain.platformapi.auth.enums.AccountStatus;
import com.grankain.platformapi.auth.repository.AccountRepository;
import com.grankain.platformapi.auth.valueObjects.Email;
import com.grankain.platformapi.auth.valueObjects.EncodedPassword;
import com.grankain.platformapi.auth.valueObjects.Password;
import com.grankain.platformapi.auth.valueObjects.Username;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
    public ResponseRegister authRegister(RequestRegister register) {
        // Encapsulamento em Value Objects: Garante que os dados sejam válidos por design.
        Username username = new Username(register.user());
        Email email = new Email(register.email());
        Password password = new Password(register.password());

        // Regra de Negócio: Não permite duplicidade de e-mail ou nome de usuário.
        boolean isValidEmail = accountRepository.existsByEmailOrUser(email, username);
        if (isValidEmail) {
            // Lança exceção de conflito se os dados já existirem no banco.
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Dados inválidos: usuário ou e-mail já em uso.");
        }

        // Criptografia: Gera o Hash da senha utilizando o algoritmo configurado (ex: Argon2/BCrypt).
        // Nunca salve senhas em texto puro!
        String hash = passwordEncoder.encode(password.value());
        EncodedPassword encoded = new EncodedPassword(hash);

        // Criação da Entidade de Domínio.
        Account user = new Account(
                register.name(),
                register.lastname(),
                email,
                register.birthday(),
                username,
                AccountStatus.PENDING,
                encoded
        );

        // Persistência: Salva a nova conta no banco de dados através do JPA.
        accountRepository.save(user);
        
        // Mapeamento: Converte a entidade de volta para um DTO de resposta (Response).
        return new ResponseRegister(user);
    }
}


