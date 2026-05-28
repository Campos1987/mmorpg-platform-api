# =============================================================================
# STAGE 1 — BUILD
# Usa a imagem JDK completa apenas para compilar o projeto com Maven.
# O resultado final (JAR) é copiado para o estágio seguinte.
# =============================================================================
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /build

# Copia apenas os arquivos de dependência primeiro para aproveitar o cache do Docker.
# Se pom.xml não mudar, o 'mvn dependency:go-offline' não será re-executado.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Agora copia o código-fonte e compila, pulando os testes
# (os testes devem rodar em pipeline CI, não no build da imagem)
COPY src/ src/
RUN ./mvnw package -Dmaven.test.skip=true -B

# =============================================================================
# STAGE 2 — RUNTIME
# Usa apenas o JRE (sem ferramentas de build), mantendo a imagem final enxuta.
# =============================================================================
FROM eclipse-temurin:21-jre-alpine AS runtime

# Boas práticas de segurança: não execute o processo como root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copia apenas o JAR gerado no estágio de build
COPY --from=build /build/target/*.jar app.jar

# Ajusta o dono do arquivo para o usuário não-root
RUN chown appuser:appgroup app.jar

USER appuser

# Porta da aplicação (definida no application.yaml)
EXPOSE 4000

# Opções da JVM:
#   -XX:+UseContainerSupport       → respeita os limites de CPU/memória do container
#   -XX:MaxRAMPercentage=75.0      → usa até 75% da RAM alocada ao container
#   -Djava.security.egd=...        → acelera a geração de números aleatórios (comum em containers)
ENTRYPOINT ["java", \
  "--add-opens", "java.base/java.lang=ALL-UNNAMED", \
  "--add-opens", "java.base/java.util=ALL-UNNAMED", \
  "--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]