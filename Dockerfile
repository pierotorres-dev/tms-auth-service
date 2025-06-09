# Stage 1: Build the application with Maven
# Usamos un alias 'BUILD' para esta etapa
FROM maven:3.9.6-eclipse-temurin-17-alpine AS BUILD

# Establecemos el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos solo el pom.xml primero para aprovechar el caché de capas de Docker.
# Si las dependencias no cambian, Docker no las volverá a descargar.
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiamos el resto del código fuente
COPY src ./src

# Compilamos la aplicación y generamos el .jar
# El -DskipTests acelera la construcción en el pipeline
RUN mvn clean package -DskipTests

# Stage 2: Create the final, lightweight image
# Usamos una imagen base de Java 17 delgada, sin las herramientas de compilación.
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# MEJORA CLAVE: En lugar de un nombre fijo, usamos un comodín (*.jar).
# Esto encontrará automáticamente el JAR que Maven construyó, sin importar el nombre o la versión.
# Así, este Dockerfile funciona para 'auth-service', 'fleet-service', etc. sin cambios.
COPY --from=BUILD /app/target/*.jar app.jar

# Exponemos el puerto que usará la aplicación (esto es solo documentación)
# El puerto real se define en tu application.yml
EXPOSE 8081

# Comando para ejecutar la aplicación cuando el contenedor inicie
CMD ["java", "-jar", "app.jar"]
