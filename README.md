# TMS Auth Service

## Descripción

TMS Auth Service es un microservicio de autenticación y autorización para el sistema de administración e inspecciones de neumáticos para flotas de vehículos pesados como camiones, grúas y stackers. Este servicio gestiona la autenticación de usuarios, validación de tokens y control de acceso para todos los servicios de la plataforma.

## Características principales

- Autenticación de usuarios mediante credenciales (username/password)
- Generación y validación de tokens JWT
- Soporte para múltiples empresas por usuario
- Registro de nuevos usuarios
- Almacenamiento de tokens de sesión en Redis
- Limitación de tasa de peticiones con Resilience4j
- Observabilidad mediante Micrometer y Prometheus
- Documentación de API con Swagger

## Tecnologías utilizadas

- Java 17
- Spring Boot 3.4.5
- Spring WebFlux (programación reactiva)
- Spring Security
- JWT (JSON Web Tokens)
- R2DBC con MySQL (base de datos reactiva)
- Redis para gestión de sesiones
- Docker para contenerización
- Micrometer para métricas y observabilidad
- Resilience4j para patrones de resiliencia

## Requisitos previos

- JDK 17
- Docker y Docker Compose
- Maven 3.6+

## Instalación y ejecución

### 1. Clonar el repositorio

```bash
git clone https://github.com/pierotorres-dev/tms-auth-service
cd tms-auth-service
```

### 2. Iniciar Redis con Docker Compose

```bash
docker-compose up -d
```

El archivo `docker-compose.yml` está configurado para iniciar automáticamente Redis tras reinicios del sistema gracias a la directiva `restart: always`.

### 3. Compilar el proyecto

```bash
mvn clean package
```

### 4. Ejecutar la aplicación

```bash
java -jar target/auth-service-0.0.1.jar
```

## Endpoints principales

### Autenticación

- `POST /api/auth/login` - Iniciar sesión con credenciales
- `GET /api/auth/validate` - Validar token JWT

### Usuarios

- `POST /api/users/register` - Registrar nuevo usuario

### Tokens

- `POST /api/tokens/generate` - Generar token para una empresa específica
- `POST /api/tokens/refresh` - Renovar token activo

## Configuración de Redis

El servicio utiliza Redis como almacenamiento para la gestión de tokens de sesión. La configuración del contenedor Redis incluye:

- Autenticación con contraseña
- Puerto expuesto: 6379
- Persistencia de datos mediante volumen
- Comprobación de salud para garantizar disponibilidad

## Desarrollo y pruebas

### Ejecutar pruebas

```bash
mvn test
```

### Acceder a la documentación de la API

La documentación Swagger está disponible en:

```
http://localhost:8080/swagger-ui.html
```

## Seguridad

El servicio implementa seguridad mediante:

- Autenticación basada en JWT
- Contraseñas encriptadas
- Control de acceso basado en roles
- Limitación de tasa de peticiones para prevenir ataques

## Monitoreo y observabilidad

- Endpoints de Actuator expuestos para monitoreo
- Métricas disponibles en formato Prometheus
- Trazabilidad de operaciones con Micrometer