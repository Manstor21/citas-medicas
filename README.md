# Citas Medicas API

[![License](https://img.shields.io/badge/license-MIT-blue)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17-orange)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.9-6db33f)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.x-4479A1)](https://www.mysql.com/)
[![WebSphere Liberty](https://img.shields.io/badge/WebSphere_Liberty-24.0.0.9-8A2BE2)](https://www.ibm.com/products/open-liberty)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED)](https://www.docker.com/)

Sistema de gestion de citas medicas entre medicos y pacientes, con autenticacion JWT, API REST y servicio SOAP.

## Stack Tecnologico

| Componente | Tecnologia | Version |
|------------|-----------|---------|
| Lenguaje | Java | 17 |
| Framework | Spring Boot | 3.5.9 |
| Seguridad | Spring Security + JWT | 0.11.5 |
| Persistencia | Spring Data JPA / Hibernate | - |
| Base de datos | MySQL | 8.x+ |
| SOAP | Spring Web Services + JAXB | - |
| Servidor de aplicaciones | WebSphere Liberty | 24.0.0.9 |
| Contenedor | Docker / Docker Compose | - |
| Build | Maven (multi-module) | - |
| Utilidades | Lombok | - |

## Arquitectura

Proyecto multi-module Maven con empaquetado EAR para despliegue en WebSphere Liberty:

```
citas-parent (POM)
├── citas-war   (WAR)  → Logica de negocio, REST, SOAP, seguridad
└── citas-ear   (EAR)  → Empaqueta el WAR para Liberty
```

- **citas-war**: Contiene toda la aplicacion Spring Boot (controladores, servicios, entidades, seguridad JWT, endpoint SOAP).
- **citas-ear**: Envuelve el WAR en un EAR para despliegue en Liberty.
- **confLiberty/**: Configuracion del servidor Liberty (server.xml, datasources, JDBC driver).

## Requisitos Previos

- Java 17+
- Maven 3.8+
- MySQL 8.x+
- Docker + Docker Compose (para despliegue en Liberty)
- `gh` CLI (opcional, para crear el repositorio desde linea de comandos)

## Instalacion

### 1. Clonar el repositorio

```bash
git clone https://github.com/TU_USUARIO/citas-medicas.git
cd citas-medicas
```

### 2. Configurar la base de datos

Crear la base de datos en MySQL:

```sql
CREATE DATABASE IF NOT EXISTS citas;
```

Las tablas se crean automaticamente mediante Hibernate (`ddl-auto=update`).

### 3. Configurar variables de entorno

Definir las variables de entorno para la base de datos:

```bash
# Linux / macOS
export DB_PASSWORD=tu_password
export DB_USER=root

# Windows (PowerShell)
$env:DB_PASSWORD="tu_password"
$env:DB_USER="root"
```

> **Nota:** `DB_USER` es opcional, por defecto usa `root`.

### 4. Compilar el proyecto

```bash
mvn clean package
```

### 5. Desplegar con Docker

Copiar el EAR generado a la carpeta `ear/`:

```bash
# Linux / macOS
mkdir -p ear
cp citas-ear/target/citas.ear ear/

# Windows (PowerShell)
New-Item -ItemType Directory -Force -Path ear
Copy-Item citas-ear/target/citas.ear ear/
```

Levantar el servidor Liberty:

```bash
docker compose up -d
```

La aplicacion estara disponible en: `http://localhost:9080/citas`

## Estructura del Proyecto

```
citas-medicas/
├── pom.xml                          # POM padre (multi-module)
├── docker-compose.yml               # Docker Compose (Liberty)
├── LICENSE                          # Licencia MIT
├── .gitignore
├── .gitattributes
├── mvnw / mvnw.cmd                  # Maven Wrapper
├── confLiberty/
│   ├── mysql/                       # JDBC driver MySQL para Liberty
│   └── xml/
│       ├── server.xml               # Configuracion raiz Liberty
│       ├── configDropins/           # Auto-deploy del EAR
│       └── configuration/           # Configuracion detallada del servidor
│           ├── server_features.xml
│           ├── server_definition.xml
│           └── server_datasources.xml
├── citas-war/
│   ├── pom.xml
│   └── src/main/java/com/example/citas/
│       ├── CitasApplication.java           # Punto de entrada
│       ├── ServletInitializer.java         # Inicializador para WAR en Liberty
│       ├── controller/
│       │   ├── AuthController.java         # Login (POST /auth/login)
│       │   └── CitaController.java         # CRUD de citas
│       ├── service/
│       │   ├── CitaService.java            # Interfaz de servicio de citas
│       │   ├── UsuarioService.java         # Interfaz de servicio de usuarios
│       │   └── interfaces/impl/
│       │       ├── CitaServiceImpl.java    # Logica de negocio de citas
│       │       └── UsuarioServiceImpl.java # Logica de login
│       ├── persistence/entity/
│       │   ├── Usuario.java                # Entidad usuario (medico/paciente)
│       │   ├── Cita.java                   # Entidad cita
│       │   └── JornadaMedico.java          # Jornada laboral del medico
│       ├── repository/
│       │   ├── CitaRepository.java
│       │   ├── UsuarioRepository.java
│       │   └── JornadaMedicoRepository.java
│       ├── security/
│       │   ├── JwtUtil.java                # Generacion/validacion JWT
│       │   └── JwtFilter.java              # Filtro de autenticacion
│       ├── dto/
│       │   ├── CitaDTO.java
│       │   ├── UsuarioDTO.java
│       │   └── LoginResponseDTO.java
│       ├── soap/
│       │   ├── config/WebServiceConfig.java     # Config SOAP
│       │   ├── endpoint/CitasEndpoint.java      # Endpoint SOAP
│       │   └── generated/                       # Claves JAXB generadas
│       └── util/
│           ├── GlobalExceptionHandler.java
│           ├── ResourceNotFoundException.java
│           └── CitaException.java
└── citas-ear/
    ├── pom.xml
    └── src/main/application/META-INF/
        └── application.xml             # Descriptor EAR
```

## API REST

Todas las peticiones autenticadas deben incluir el header:

```
Authorization: Bearer <token_jwt>
```

### Autenticacion

| Metodo | Endpoint | Descripcion | Auth |
|--------|----------|-------------|------|
| `POST` | `/auth/login?email=...&password=...` | Iniciar sesion, devuelve token JWT | No |

**Respuesta exitosa:**
```json
{
  "usuario": {
    "nombre": "Juan",
    "apellidos": "Perez",
    "dni": "12345678Z",
    "roles": ["PACIENTE"]
  },
  "token": "eyJhbGci..."
}
```

### Citas

| Metodo | Endpoint | Rol Requerido | Descripcion |
|--------|----------|---------------|-------------|
| `POST` | `/citas` | Autenticado | Crear una cita |
| `GET` | `/citas/libres/{medicoId}?fecha=YYYY-MM-DD` | Autenticado | Ver horarios libres de un medico |
| `GET` | `/citas/paciente/{pacienteId}` | PACIENTE / MEDICO | Citas de un paciente |
| `GET` | `/citas/medico/{id}?fecha=YYYY-MM-DD&page=0` | MEDICO | Citas de un medico (paginado, 5 por pagina) |
| `PUT` | `/citas/{id}` | Autenticado | Actualizar una cita |
| `PATCH` | `/citas/{id}/realizada` | MEDICO | Marcar cita como realizada |
| `DELETE` | `/citas/{id}` | Autenticado | Eliminar una cita |

### Reglas de Negocio

- Las citas se crean en intervalos de **30 minutos** dentro de la jornada del medico.
- No se permiten **solapamientos** de citas en el mismo medico.
- Solo el **medico propietario** puede marcar una cita como realizada.
- Una cita **ya realizada** no puede modificarse.
- Las horas libres se calculan automaticamente segun la jornada del medico.

## Servicio SOAP

El endpoint SOAP esta disponible en:

```
http://localhost:9080/citas/herme-wss-api/citas
```

**Operacion disponible:**

| Operacion | Descripcion |
|-----------|-------------|
| `BuscarCitasRequest` | Consultar citas de un paciente por su ID |

**Ejemplo de request SOAP:**
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:example="http://example.com/citas">
  <soapenv:Header/>
  <soapenv:Body>
    <example:BuscarCitasRequest>
      <example:pacienteId>1</example:pacienteId>
    </example:BuscarCitasRequest>
  </soapenv:Body>
</soapenv:Envelope>
```

## Configuracion

### Variables de Entorno

| Variable | Descripcion | Por defecto |
|----------|-------------|-------------|
| `DB_PASSWORD` | Contrasena de MySQL | _(requerida)_ |
| `DB_USER` | Usuario de MySQL | `root` |

### Base de datos

- **URL**: `jdbc:mysql://localhost:3306/citas`
- **Usuario**: `root`
- **DDL**: `update` (Hibernate crea/actualiza tablas automaticamente)

### Puertos

| Puerto | Servicio |
|--------|----------|
| `9080` | HTTP (Liberty) |
| `9443` | HTTPS (Liberty) |
| `3306` | MySQL |

## Entidades

```
Usuario (Usuarios)
├── id (Long)
├── nombre (String)
├── apellidos (String)
├── dni (String, formato: 8 digitos + letra)
├── email (String, unico)
├── password (String)
└── roles (Set<String>)  → "MEDICO" | "PACIENTE"

Cita (Citas)
├── id (Long)
├── fecha (LocalDate)
├── horaInicio (LocalTime)
├── horaFin (LocalTime)
├── estado (String)  → "PENDIENTE" | "REALIZADA"
├── medico (Usuario)
└── paciente (Usuario)

JornadaMedico (Jornadas)
├── id (Long)
├── medico (Usuario) [OneToOne]
├── horaInicio (LocalTime)
└── horaFin (LocalTime)
```

## Licencia

Este proyecto esta licenciado bajo la [Licencia MIT](LICENSE).
