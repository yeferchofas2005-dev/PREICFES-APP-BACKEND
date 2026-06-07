# PreICFES Gratuito — Backend

API REST del backend de **PreICFES Gratuito**, plataforma web de simulacros tipo ICFES para la organización homónima. Expone servicios de autenticación, consulta de simulacros y estadísticas de desempeño para estudiantes.

## Stack tecnológico

| Componente        | Tecnología                          |
|-------------------|-------------------------------------|
| Lenguaje          | Java 21                             |
| Framework         | Spring Boot 4.0.6                   |
| Persistencia      | Spring Data JPA / Hibernate         |
| Base de datos     | MySQL                               |
| Seguridad         | Spring Security + JWT (jjwt 0.12.7) |
| Validación        | Jakarta Bean Validation             |
| Utilidades        | Lombok, MapStruct                   |
| Monitoreo         | Spring Boot Actuator                |

## Requisitos previos

- JDK 21
- Maven 3.9+
- MySQL 8+ con la base de datos `db_preicfes_gratuito` creada y esquema aplicado
- Puerto `8080` disponible (configurable)

## Configuración

Las propiedades principales se definen en `src/main/resources/application.properties`:

| Propiedad                    | Descripción                                      | Valor por defecto              |
|------------------------------|--------------------------------------------------|--------------------------------|
| `server.port`                | Puerto HTTP del servidor                         | `8080`                         |
| `spring.datasource.url`      | URL JDBC de MySQL                                | `localhost:3306/db_preicfes_gratuito` |
| `spring.datasource.username` | Usuario de base de datos                         | `root`                         |
| `spring.datasource.password` | Contraseña de base de datos                      | *(vacío)*                      |
| `jwt.secret`                 | Clave HMAC para firmar tokens JWT                | *(cambiar en producción)*      |
| `jwt.expiration-ms`          | Duración del token en milisegundos               | `86400000` (24 h)              |

> **Importante:** en producción, externalice credenciales y `jwt.secret` mediante variables de entorno o un gestor de secretos. No exponga la clave JWT en el repositorio.

Hibernate está configurado con `ddl-auto=none`; el esquema de base de datos debe existir previamente.

## Ejecución

```bash
# Compilar y ejecutar
./mvnw spring-boot:run

# Compilar sin ejecutar
./mvnw clean package

# Ejecutar pruebas
./mvnw test
```

En Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Health check disponible en: `GET http://localhost:8080/actuator/health`

## Arquitectura

El proyecto sigue una arquitectura en capas típica de Spring Boot:

```
Controller  →  Service  →  Repository  →  MySQL
     ↓              ↓
   DTO          Entity / Projection
```

- **Controller:** expone endpoints REST y valida la entrada.
- **Service:** contiene la lógica de negocio y orquesta consultas.
- **Repository:** acceso a datos con Spring Data JPA; incluye consultas nativas agregadas para estadísticas.
- **DTO / Projection:** objetos de transferencia que evitan exponer entidades JPA directamente.
- **Security:** autenticación stateless basada en JWT con control de acceso por rol.

### Modelo de dominio (resumen)

```
Localidad → Nodo → Grupo ←→ Estudiante (Usuario)
                      ↓
                 Simulacro ←→ BancoPregunta → Pregunta → OpcionRespuesta
                      ↓
              IntentoSimulacro → RespuestaEstudiante
```

- Un **periodo académico** agrupa bancos de preguntas y simulacros.
- Un **simulacro** se compone de bancos de preguntas y se asigna a **grupos** de estudiantes.
- Un **intento** registra el resultado de un estudiante al finalizar un simulacro.
- Las **respuestas** alimentan las estadísticas por asignatura (área ICFES).

## Seguridad y roles

La API es **stateless**: no usa sesiones HTTP. Tras login o registro, el cliente debe enviar el token en cada petición protegida:

```
Authorization: Bearer <token>
```

### Roles

| Rol         | Prefijo API        | Descripción                                      |
|-------------|--------------------|--------------------------------------------------|
| `ADMIN`     | `/api/admin/**`    | Acceso administrativo completo                   |
| `DIRECTIVO` | `/api/directivo/**`| Gestión institucional                            |
| `PROFESOR`  | `/api/profesor/**` | Gestión de grupos y seguimiento                  |
| `ESTUDIANTE`| `/api/estudiante/**`| Consulta de simulacros y estadísticas propias   |

Los roles superiores heredan acceso a los prefijos inferiores según la configuración en `SecurityConfig`.

### Endpoints públicos

| Método | Ruta                  | Descripción                    |
|--------|-----------------------|--------------------------------|
| POST   | `/api/auth/login`     | Iniciar sesión                 |
| POST   | `/api/auth/register`  | Registrar usuario              |
| GET    | `/actuator/health`    | Estado del servicio            |

## API REST — Módulos implementados

### Autenticación (`/api/auth`)

#### `POST /api/auth/login`

Autentica por correo y contraseña.

**Request body:**

```json
{
  "correo": "estudiante@ejemplo.com",
  "password": "miClave123"
}
```

**Response `200 OK`:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "usuario": {
    "idUsuario": 1,
    "nombres": "Juan",
    "apellidos": "Pérez",
    "documento": "1234567890",
    "correo": "estudiante@ejemplo.com",
    "telefono": null,
    "activo": true,
    "rol": { "idRol": 4, "nombre": "ESTUDIANTE" }
  }
}
```

#### `POST /api/auth/register`

Crea un usuario con contraseña cifrada (BCrypt) y devuelve token JWT.

**Request body:**

```json
{
  "nombres": "Juan",
  "apellidos": "Pérez",
  "documento": "1234567890",
  "correo": "estudiante@ejemplo.com",
  "password": "miClave123",
  "telefono": "3001234567",
  "idRol": 4
}
```

**Response `201 Created`:** misma estructura que login.

### Estudiante (`/api/estudiante`)

Todos los endpoints usan el usuario autenticado del JWT; no aceptan IDs de estudiante en la URL.

| Método | Ruta                         | Descripción                                              |
|--------|------------------------------|----------------------------------------------------------|
| GET    | `/api/estudiante/resumen`    | Resumen: simulacros realizados, puntaje y tiempo promedio |
| GET    | `/api/estudiante/simulacros` | Simulacros del periodo activo asignados al grupo         |
| GET    | `/api/estudiante/estadisticas`| Métricas para dashboard y gráficas                      |

#### `GET /api/estudiante/resumen`

**Response `200 OK`:**

```json
{
  "idEstudiante": 1,
  "nombres": "Juan",
  "apellidos": "Pérez",
  "simulacrosRealizados": 5,
  "puntajePromedio": 72.50,
  "tiempoPromedioSegundos": 5400.00
}
```

#### `GET /api/estudiante/simulacros`

Lista simulacros activos del periodo vigente asignados al grupo del estudiante. El campo `estado` puede ser `REALIZADO` o `NO_REALIZADO`.

#### `GET /api/estudiante/estadisticas`

**Response `200 OK`:**

```json
{
  "puntajeGlobal": 68.25,
  "porcentajeNacional": 65.00,
  "tiempoPromedioSegundos": 5100.00,
  "desempenoPorAsignatura": [
    { "idArea": 1, "asignatura": "Matemáticas", "puntajePromedio": 75.00 }
  ],
  "tiempoPorAsignatura": [
    { "idArea": 1, "asignatura": "Matemáticas", "tiempoPromedioSegundos": 900.00 }
  ],
  "resultadoSimulacro": [
    {
      "idSimulacro": 1,
      "simulacro": "Simulacro 1",
      "idArea": 1,
      "asignatura": "Matemáticas",
      "puntaje": 80.00
    }
  ]
}
```

## Respuestas de error

Los errores siguen un formato JSON uniforme (`ErrorResponseDTO`):

```json
{
  "timestamp": "2026-06-07T10:30:00",
  "status": 400,
  "error": "Solicitud inválida",
  "message": "Ya existe un usuario con ese correo",
  "path": "/api/auth/register",
  "validationErrors": null
}
```

| Código HTTP | Situación                                      |
|-------------|------------------------------------------------|
| 400         | Validación fallida o regla de negocio          |
| 401         | Credenciales inválidas o token ausente/inválido  |
| 404         | Recurso no encontrado                          |

## Estructura del proyecto

```
src/main/java/com/edu/preicfesGratuito/
├── config/          # Configuración de Spring Security
├── controller/      # Controladores REST
├── dto/             # Objetos de transferencia
├── entity/          # Entidades JPA
├── enums/           # Enumeraciones de dominio
├── exception/       # Excepciones y manejo global de errores
├── mapper/          # Conversión Entity ↔ DTO
├── repository/      # Repositorios JPA y proyecciones
├── security/        # Filtros JWT y UserDetails
├── service/         # Interfaces de negocio
│   └── impl/        # Implementaciones de servicios
└── util/            # Utilidades (JWT)
```

## Estado del desarrollo

| Módulo              | Estado        |
|---------------------|---------------|
| Autenticación (JWT) | Implementado  |
| Estudiante          | Implementado  |
| Admin               | Pendiente     |
| Directivo           | Pendiente     |
| Profesor            | Pendiente     |
| Simulacro (resolver)| Pendiente     |

## Licencia

Proyecto académico / interno de PreICFES Gratuito. Consulte con la organización antes de redistribuir.
