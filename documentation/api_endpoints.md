# Endpoints da API

A API utiliza o prefixo base `/auth` para operações relacionadas a usuários.

## Autenticação

### 1. Registro de Usuário
`POST /auth/register`

Cria uma nova conta no sistema.

**Request Body:**
```json
{
  "user": "username123",
  "name": "João",
  "lastname": "Silva",
  "email": "joao@exemplo.com",
  "birthday": "1990-01-01",
  "password": "Senha@123",
  "recaptchaToken": "token-aqui"
}
```

**Regras de Validação:**
- `user`: 5-12 caracteres, apenas letras e números.
- `name/lastname`: 3-15 caracteres, apenas letras.
- `password`: 8-12 caracteres, 1 maiúscula, 1 número, 1 especial.
- `email`: Formato válido e único no sistema.

**Responses:**
- `201 Created`: Usuário criado com sucesso. Retorna username e email (mascarado).
- `400 Bad Request`: Dados inválidos (falha na validação do DTO).
- `409 Conflict`: Usuário ou E-mail já cadastrado.

---

## Padronização de Erros

Todas as exceções retornam um objeto padronizado conforme abaixo:

```json
{
  "timestamp": "2024-05-05T10:00:00Z",
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "Mensagem detalhada do erro",
  "trace": [],
  "path": "/auth/register"
}
```
*Nota: O campo `trace` é preenchido apenas em ambiente de desenvolvimento.*
