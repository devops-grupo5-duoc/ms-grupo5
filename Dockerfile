# Etapa de build: compila el proyecto con Maven y JDK 21
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copia primero los archivos de configuración de Maven para aprovechar la caché de capas
COPY mvnw .
COPY .mvn/ .mvn/
COPY pom.xml .

# Descarga dependencias (offline) para que queden en caché si no cambia pom.xml
RUN ./mvnw -B dependency:go-offline

# Copia el código fuente y compila el proyecto
COPY src/ src/
RUN ./mvnw -B clean package -DskipTests

# Etapa final: imagen liviana con JRE 21 Alpine y usuario no root
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Crea usuario no root para seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copia solo el JAR generado desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Cambia propietario al usuario no root
RUN chown appuser:appgroup app.jar

# Usa usuario no root
USER appuser

# Puerto expuesto (definido en application.yaml: server.port=8080)
EXPOSE 8080

# Punto de entrada: ejecuta la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]