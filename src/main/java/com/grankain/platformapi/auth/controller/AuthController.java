package com.grankain.platformapi.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grankain.platformapi.auth.dto.request.LoginRequest;
import com.grankain.platformapi.auth.dto.request.RegisterRequest;
import com.grankain.platformapi.auth.dto.response.LoginResponse;
import com.grankain.platformapi.auth.dto.response.RegisterResponse;
import com.grankain.platformapi.auth.service.LoginService;
import com.grankain.platformapi.auth.service.RegisterService;
import com.grankain.platformapi.infra.util.IpUtil;

import jakarta.validation.Valid;

/**
 * Controller responsável pelas operações de autenticação e registro de
 * usuários.
 * Atua como o ponto de entrada (Entry Point) para as requisições HTTP
 * relacionadas ao domínio de Auth.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    // Injeção de dependência via construtor (Boa prática: facilita testes e garante
    // imutabilidade).
    private final RegisterService registerService;
    private final LoginService loginService;

    public AuthController(RegisterService registerService, LoginService loginService) {
        this.registerService = registerService;
        this.loginService = loginService;
    }

    /**
     * Endpoint para registro de novos usuários.
     *
     * @param register DTO contendo os dados do novo usuário. Validado pelo Jakarta
     *                 Validation (@Valid).
     * @return ResponseEntity com o DTO de resposta e status HTTP 201 (Created).
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> authRegister(@Valid @RequestBody RegisterRequest register) {

        String ipUser = IpUtil.getClientIp();

        return ResponseEntity.status(HttpStatus.CREATED).body(registerService.authRegister(register, ipUser));
    }

    /**
     * Endpoint para Login de usuários.
     *
     * @param login DTO contendo os dados do novo usuário. Validado pelo Jakarta
     *              Validation (@Valid).
     * @return ResponseEntity com o DTO de resposta e status HTTP 201 (Created).
     */

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authLogin(@Valid @RequestBody LoginRequest login) {

        String ipUser = IpUtil.getClientIp();

        return ResponseEntity.ok(loginService.authLogin(login, ipUser));
    }
}
