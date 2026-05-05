package com.grankain.platformapi.auth.controller;

import com.grankain.platformapi.auth.dto.RequestRegister;
import com.grankain.platformapi.auth.dto.ResponseRegister;
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

    // TODO: O parâmetro LoginService deve ser declarado como um campo da classe ou removido se não for usado.
    public AuthController(RegisterService registerService) {
        this.registerService = registerService;
    }

    /**
     * Endpoint para registro de novos usuários.
     *
     * @param register DTO contendo os dados do novo usuário. Validado pelo Jakarta Validation (@Valid).
     * @return ResponseEntity com o DTO de resposta e status HTTP 201 (Created).
     */
    @PostMapping("/register")
    public ResponseEntity<ResponseRegister> authRegister(@Valid @RequestBody RequestRegister register) {
        // Delega o processamento da regra de negócio para a camada de Service.
        return ResponseEntity.status(HttpStatus.CREATED).body(registerService.authRegister(register));
    }
}

