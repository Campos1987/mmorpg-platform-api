package com.grankain.platformapi.infra.exception;

import com.grankain.platformapi.auth.exceptions.AccountAlreadyExistsException;
import com.grankain.platformapi.infra.exception.dto.ApiErrorResponse;
import com.grankain.platformapi.infra.exception.dto.ApiTraceItem;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

/**
 * Utiliza @ControllerAdvice para interceptar exceções em todos os @Controllers.
 * Estende ResponseEntityExceptionHandler para herdar tratamentos padrão do
 * Spring MVC.
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // Flag que determina o nível de exposição de dados com base no perfil ativo.
    private final boolean isDev;

    /**
     * Construtor que injeta as configurações de ambiente.
     * Define 'isDev' como true apenas se o perfil "dev" estiver ativo no
     * application.properties/yml.
     */
    public GlobalExceptionHandler(Environment environment) {
        this.isDev = environment.acceptsProfiles(Profiles.of("dev"));
    }

    /**
     * Intercepta a exceção de domínio quando um usuário ou e-mail já estão em uso.
     * Retorna HTTP 409 (Conflict).
     */
    @ExceptionHandler(AccountAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleAccountAlreadyExistsException(
            AccountAlreadyExistsException ex,
            HttpServletRequest request) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                ex,
                request);
    }

    /**
     * Intercepta erros de autenticação do Spring Security (ex: senha errada ou
     * usuário bloqueado).
     * Retorna HTTP 401 (Unauthorized) com a mensagem específica do motivo da falha.
     *
     * @param ex      A exceção de credenciais inválidas capturada.
     * @param request Dados da requisição original.
     * @return Resposta padronizada conforme o DTO ApiErrorResponse.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentialsException(
            BadCredentialsException ex,
            HttpServletRequest request) {
        // Define o status fixo como 401 (Unauthorized), padrão para falhas de
        // login/autenticação
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        // Recupera o texto passado no 'throw new BadCredentialsException("mensagem")',
        // ou usa a descrição padrão do HTTP 401 caso nenhuma mensagem tenha sido
        // informada.
        String message = ex.getMessage() != null
                ? ex.getMessage()
                : status.getReasonPhrase();

        // Monta e retorna o JSON estruturado respeitando as regras do ambiente (Dev vs
        // Prod)
        return buildResponse(status, message, ex, request);
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ApiErrorResponse> handleDateTimeParseException(
            DateTimeParseException ex,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        // Mensagem clara e segura para o cliente
        String message = "Invalid date. Please ensure you submit an actual date in YYYY-MM-DD format.";

        return buildResponse(status, message, ex, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;

        // Mensagem genérica segura (fallback)
        String message = "Data conflict: The resource already exists or violates a database rule.";

        // Extrai a causa raiz para inspecionar a mensagem original do banco de dados
        Throwable rootCause = ex.getRootCause();

        if (rootCause != null && rootCause.getMessage() != null) {
            String dbMessage = rootCause.getMessage().toLowerCase();

            // Mapeamento amigável baseado no nome da constraint ou coluna
            // (Ajuste "email" e "username" para bater com o nome exato das suas
            // colunas/constraints no banco)
            if (dbMessage.contains("email")) {
                message = "Conflict: This email address is already registered.";
            } else if (dbMessage.contains("username")) {
                message = "Conflict: This username is already in use.";
            }
        }

        return buildResponse(status, message, ex, request);
    }

    /**
     * Intercepta erros do tipo {@link ResponseStatusException}.
     * Comumente lançada programaticamente via: throw new
     * ResponseStatusException(HttpStatus.NOT_FOUND, "msg").
     *
     * @param ex      Exceção capturada pelo Spring.
     * @param request Dados da requisição original.
     * @return Resposta padronizada conforme o DTO ApiErrorResponse.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> statusException(
            ResponseStatusException ex,
            HttpServletRequest request) {
        // Resolve o código HTTP (ex: 404, 500) para garantir que seja um status válido
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());

        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        // Prioriza a mensagem customizada da exceção; caso ausente, usa o nome padrão
        // do status
        String message = ex.getReason() != null
                ? ex.getReason()
                : status.getReasonPhrase();

        return buildResponse(status, message, ex, request);
    }

    /**
     * Intercepta erros {@link HttpClientErrorException}, geralmente disparados por
     * clientes
     * Rest (como o RestTemplate) ao receberem respostas de erro (4xx) de serviços
     * externos.
     */
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ApiErrorResponse> httpClientErrorException(
            HttpClientErrorException ex,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());

        if (status == null) {
            status = HttpStatus.BAD_REQUEST;
        }

        String message = ex.getMessage() != null
                ? ex.getMessage()
                : "Erro na requisição originada pelo cliente.";

        return buildResponse(status, message, ex, request);
    }

    /**
     * Sobrescreve o tratamento padrão para erros de validação (@Valid).
     * Este método é chamado quando um DTO falha nas anotações como @NotBlank, @Size
     * ou @ValidPassword.
     */
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        // Captura a primeira mensagem de erro de validação encontrada
        // Se quiser listar todos os erros de todos os campos, teria que adaptar o seu
        // ApiErrorResponse
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        // Convertemos o WebRequest para HttpServletRequest para usar no seu
        // buildResponse
        HttpServletRequest servletRequest = ((org.springframework.web.context.request.ServletWebRequest) request)
                .getRequest();

        // Usamos o seu método padrão da classe para gerar o JSON de erro
        ApiErrorResponse errorBody = buildResponse(
                HttpStatus.BAD_REQUEST,
                errorMessage,
                ex,
                servletRequest).getBody();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody);
    }

    /**
     * Fábrica de respostas que aplica a política de segurança da aplicação.
     * <p>
     * Lógica de Ambiente:
     * - DEV: Retorna detalhes técnicos exaustivos (Stacktrace, URI, Timestamp).
     * - PROD: Retorna apenas o nome do erro técnico para evitar Information
     * Exposure.
     */
    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            Throwable ex,
            HttpServletRequest request) {
        ApiErrorResponse error;

        if (isDev) {
            // Em ambiente de desenvolvimento, provê rastro completo para depuração rápida
            error = new ApiErrorResponse(
                    Instant.now(),
                    status.value(),
                    status.name(),
                    message,
                    buildFilteredTrace(ex),
                    request.getRequestURI());
        } else {
            // Em produção, os campos são omitidos (null) para segurança (Hardening)
            // Apenas o 'error' (ex: "NOT_FOUND") é enviado.
            error = new ApiErrorResponse(
                    null,
                    null,
                    status.name(),
                    null,
                    null,
                    null);
        }

        return ResponseEntity.status(status).body(error);
    }

    /**
     * Processa o StackTrace da exceção para extrair apenas informações relevantes.
     * Filtra o rastro para remover chamadas internas de bibliotecas e focar na
     * lógica de negócio.
     *
     * @param ex A exceção original.
     * @return Lista de {@link ApiTraceItem} limitada aos primeiros 8 frames do
     *         projeto.
     */
    private List<ApiTraceItem> buildFilteredTrace(Throwable ex) {
        return Arrays.stream(ex.getStackTrace())
                // Filtra classes pertencentes ao domínio da aplicação
                .filter(item -> item.getClassName().startsWith("com.grankain.platformapi"))
                // Ignora frames desta classe de tratamento para limpar o topo do rastro
                .filter(item -> !item.getClassName().contains("GlobalExceptionHandler"))
                // Limita a profundidade para manter a resposta leve
                .limit(8)
                .map(item -> new ApiTraceItem(
                        item.getClassName(),
                        item.getFileName(),
                        item.getLineNumber(),
                        item.getMethodName()))
                .toList();
    }
}