package com.grankain.platformapi.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuração de segurança da aplicação (Spring Security).
 * <p>
 * Objetivo geral:
 * - Definir quais endpoints são públicos e quais exigem autenticação.
 * - Configurar a aplicação como "stateless" (sem sessão), pensando em API REST com JWT.
 * - Configurar CORS para permitir chamadas do frontend no navegador.
 * <p>
 * Observação importante:
 * - No momento está usando HTTP Basic (temporário). Em produção, a ideia é substituir por JWT Bearer.
 */
@Configuration
@EnableMethodSecurity // Habilita anotações como @PreAuthorize / @PostAuthorize nos métodos (controllers/services).
public class SecurityConfig {

    @Value("${spring.application.cors-origins:}")
    private String originsEnv;

    /**
     * Define a "cadeia de filtros" do Spring Security.
     * <p>
     * A SecurityFilterChain é o coração do Spring Security: ela determina
     * o que acontece com cada requisição HTTP (autenticação, autorização, CORS, CSRF, etc).
     */
    @Bean
    SecurityFilterChain security(HttpSecurity http, Environment environment) throws Exception {
        boolean isProd = environment.acceptsProfiles(Profiles.of("prod"));
        http
                // Desativa o "request cache".
                // Em aplicações web com login via formulário, o Spring pode "salvar" a URL original para redirecionar após login.
                // Em API REST isso não faz sentido e pode causar comportamentos estranhos.
                .requestCache(RequestCacheConfigurer::disable)

                // Habilita CORS e diz ao Spring Security para usar o bean CorsConfigurationSource abaixo.
                // Sem isso, o navegador pode bloquear chamadas do frontend (erro de CORS), especialmente com Authorization header.
                .cors(cors -> {
                    // A configuração real de CORS está no método corsConfigurationSource().
                })

                // Desabilita CSRF.
                // CSRF é importante quando você autentica via cookies/sessão (navegador).
                // Para API stateless com JWT no header Authorization, normalmente CSRF pode ficar desabilitado.
                .csrf(AbstractHttpConfigurer::disable)

                // Configura a aplicação como STATELESS (sem sessão HTTP).
                // Isso significa:
                // - o servidor não guarda "estado" de login em memória/sessão.
                // - a autenticação (futuro JWT) deve vir em TODA requisição.
                .sessionManagement(sm -> sm.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS))

                // Define regras de autorização (quem pode acessar o quê).
                .authorizeHttpRequests(auth -> auth

                        // Libera todas as requisições OPTIONS em qualquer rota.
                        // Importante para CORS: o browser envia um "preflight" (OPTIONS) antes de alguns requests.
                        // Sem isso, o navegador pode bloquear mesmo que o endpoint real esteja correto.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Libera leitura pública de posts (somente GET).
                        // Isso cobre /posts/events, /posts/news e qualquer outro sub-path em /posts/**.
                        .requestMatchers(HttpMethod.GET, "/posts/**", "/error").permitAll()
                        // Libera leitura pública de auth (somente POST).
                        // Isso cobre /auth/register, /auth/login.
                        .requestMatchers(HttpMethod.POST, "/auth/register", "/auth/login").permitAll()

                        // Qualquer outro endpoint (PUT/PATCH/DELETE e demais rotas) exige autenticação.
                        // Quando migrar para JWT, aqui significa "tem que mandar Bearer token válido".
                        .anyRequest().authenticated()
                )
                .headers(headers -> {
                    // X-Content-Type-Options: nos-niff
                    headers.contentTypeOptions(Customizer.withDefaults());

                    // X-Frame-Options: DENY
                    headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::deny);

                    // HSTS somente em produção
                    if (isProd) {
                        headers.httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)
                        );
                    } else {
                        headers.httpStrictTransportSecurity(HeadersConfigurer.HstsConfig::disable);
                    }
                })

                // Autenticação HTTP Basic (temporária).
                // Hoje: Authorization: Basic base64(user:pass)
                // Futuro: trocar por JWT (Authorization: Bearer <token>).
                // Quando trocar para JWT, provavelmente você removerá esta linha e configurará oauth2ResourceServer().jwt()
                // ou um filtro custom de JWT.
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    /**
     * Configuração de CORS.
     * <p>
     * CORS (Cross-Origin Resource Sharing) é uma regra aplicada pelo NAVEGADOR.
     * Ele decide se o frontend em um domínio/porta (ex.: <a href="http://localhost:3000">...</a>)
     * pode chamar a API em outro domínio/porta (ex.: <a href="http://localhost:4000">...</a>).
     * <p>
     * Observação:
     * - Postman/curl não sofrem CORS.
     * - CORS não protege a sua API de ataques diretos; só controla chamadas via navegador.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        // Lê as origens de application.yaml
        // Exemplo:
        // CORS_ALLOWED_ORIGINS="http://localhost:3000,https://meu-front.com"

        // Se a env estiver vazia/nula => lista vazia => nenhuma origem será permitida (bloqueia CORS no browser).
        // Se estiver definida => separa por vírgula e limpa espaços.
        List<String> allowedOrigins = (originsEnv == null || originsEnv.isBlank())
                ? List.of()
                : Arrays.stream(originsEnv.split(","))
                  .map(String::trim)
                  .filter(s -> !s.isBlank())
                  .toList();

        CorsConfiguration config = new CorsConfiguration();

        // Define quais "origens" (frontends) podem chamar a API via browser.
        config.setAllowedOrigins(allowedOrigins);

        // Define quais métodos o browser pode usar em requisições cross-origin.
        // ATENÇÃO:
        // - Se futuramente você criar PUT/PATCH/DELETE e quiser chamar do frontend,
        //   precisa adicionar aqui também.
        // - O OPTIONS é usado no preflight (mas você liberou o OPTIONS no SecurityFilterChain).
        config.setAllowedMethods(List.of("GET", "POST"));

        // Define quais headers o browser pode enviar na requisição.
        // - Authorization: necessário para JWT Bearer.
        // - Content-Type: necessário para JSON.
        // - Accept: comum em requisições do frontend.
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));

        // Define quais headers o frontend pode LER na resposta.
        // Por padrão o browser esconde muitos headers da resposta.
        // Exemplo de uso: se você devolver o token no header Authorization em algum fluxo.
        // Observação: muitos projetos devolvem o token no corpo JSON, não no header.
        config.setExposedHeaders(List.of("Authorization"));

        // Se false: não permite enviar cookies/credenciais em cross-origin (credentials: 'include').
        // Como a ideia é usar JWT no header Authorization, geralmente fica false mesmo.
        // Só mude para true se você realmente for usar cookies no futuro (e aí precisa revisar CSRF também).
        config.setAllowCredentials(false);

        // Cache do preflight no navegador (em segundos).
        // Isso melhora performance, evitando mandar OPTIONS a toda a hora.
        config.setMaxAge(3600L);

        // Aplica essa configuração de CORS para todas as rotas da API (/**).
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}