package com.grankain.platformapi.auth.controller;

import com.grankain.platformapi.auth.dto.response.Login;
import com.grankain.platformapi.auth.dto.response.Register;
import com.grankain.platformapi.auth.service.LoginService;
import com.grankain.platformapi.auth.service.RegisterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller responsável pelas operações de autenticação e registro de usuários.
 * Atua como o ponto de entrada (Entry Point) para as requisições HTTP relacionadas ao domínio de Auth.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    // Injeção de dependência via construtor (Boa prática: facilita testes e garante imutabilidade).
    private final RegisterService registerService;
    private final LoginService loginService;

    // TODO: O parâmetro LoginService deve ser declarado como um campo da classe ou removido se não for usado.
    public AuthController(RegisterService registerService, LoginService loginService) {
        this.registerService = registerService;
        this.loginService = loginService;
    }

    /**
     * Endpoint para registro de novos usuários.
     *
     * @param register DTO contendo os dados do novo usuário. Validado pelo Jakarta Validation (@Valid).
     * @return ResponseEntity com o DTO de resposta e status HTTP 201 (Created).
     */
    @PostMapping("/register")
    public ResponseEntity<Register> authRegister(@Valid @RequestBody com.grankain.platformapi.auth.dto.request.Register register) {

        return ResponseEntity.status(HttpStatus.CREATED).body(registerService.authRegister(register));
    }

    /**
     * Endpoint para Login de usuários.
     *
     * @param register DTO contendo os dados do novo usuário. Validado pelo Jakarta Validation (@Valid).
     * @return ResponseEntity com o DTO de resposta e status HTTP 201 (Created).
     */

    @PostMapping("/login")
    public ResponseEntity<Login> authLogin(@Valid @RequestBody com.grankain.platformapi.auth.dto.request.Login login) {

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(loginService.authLogin(login));
    }
}

