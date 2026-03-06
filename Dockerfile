# =====================================================
# Dockerfile — Multi-stage build
#
# Stage 1 (build): compila el proyecto con Maven
# Stage 2 (run):   solo copia el .jar, imagen liviana
#
# =====================================================

# ── Stage 1: Compilar ──────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copiamos primero solo el pom.xml para aprovechar
# el cache de Docker: si no cambiaron dependencias,
# no vuelve a descargar todo Maven
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# Ahora copiamos el código fuente
COPY src ./src

# Compilamos saltando los tests
# (los tests se corren en CI, no en la imagen Docker)
RUN ./mvnw package -DskipTests -B

# ── Stage 2: Imagen final liviana ──────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiamos solo el .jar del stage anterior
COPY --from=build /app/target/*.jar app.jar

# Puerto que expone la app
EXPOSE 8080

# Comando de arranque
ENTRYPOINT ["java", "-jar", "app.jar"]