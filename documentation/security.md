# Segurança

A segurança é uma prioridade central neste projeto, implementada através do **Spring Security** e práticas recomendadas de criptografia.

## Autenticação e Autorização

- **Spring Security**: Configurado em `SecurityConfig.java`.
- **Estratégia Stateless**: A API é configurada como stateless (`SessionCreationPolicy.STATELESS`), ideal para escalabilidade e uso com JWT.
- **HTTP Basic**: Atualmente utilizado para testes e desenvolvimento inicial. Planejado para migração para **JWT (JSON Web Token)**.
- **CORS (Cross-Origin Resource Sharing)**: Configuração robusta para permitir chamadas apenas de origens autorizadas (lidas de variáveis de ambiente).

## Proteção de Dados

### 1. Senhas
- **Algoritmo**: Utiliza **Argon2id** via `Argon2PasswordEncoder`. 
- **Por que Argon2?**: É o vencedor do Password Hashing Competition e oferece proteção superior contra ataques de GPU e força bruta devido à sua configuração de memória e paralelismo.
- **Complexidade**: Validada no Value Object `Password` e na anotação `@ValidPassword` (Exige: 1 maiúscula, 1 número, 1 caractere especial, min 8 caracteres).

### 2. PII (Personally Identifiable Information)
- **Mascaramento**: Dados como e-mail são mascarados em respostas de sucesso para evitar vazamento de informações (ex: `co*****@exemplo.com`).
- **Hardening de Exceções**: O `GlobalExceptionHandler` filtra stacktraces e detalhes técnicos em ambiente de produção, retornando apenas o estritamente necessário para o cliente.

## Validação de Entrada

- **Jakarta Validation**: Uso extensivo de `@Valid`, `@NotBlank`, `@Email`, etc.
- **Anotações Customizadas**:
    - `@ValidUser`: Valida formato de nome de usuário.
    - `@ValidPassword`: Valida complexidade de senha.
- **Sanitização**: Lógica de domínio nas entidades limpa e formata strings (ex: Capitalização de nomes).

## reCAPTCHA
- O fluxo de registro (`RequestRegister`) inclui um campo `recaptchaToken`, preparado para validação de integridade no backend para evitar ataques de bots.
