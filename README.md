#  Mini Core Bancario - Billetera Virtual

Proyecto personal
Es una API REST de una billetera digital construida con Java y Spring Boot.

## ¿Qué aprendí construyendo esto?

- Cómo usar `@Transactional` correctamente y por qué importa el rollback
- La diferencia entre Optimistic y Pessimistic Locking (y cuándo usar cada uno)
- Por qué `BigDecimal` y no `double` para manejar dinero
- Cómo prevenir deadlocks ordenando los locks por ID
- Argon2 vs BCrypt
- Flyway para versionar la base de datos como se hace en proyectos reales
- JWT para autenticación stateless
- Docker para que cualquiera pueda correr el proyecto sin instalar nada

---

## Stack

- Java 17
- Spring Boot 3.4.3
- Spring Security + JWT
- PostgreSQL 16
- Flyway
- Docker + Docker Compose
- Swagger UI (OpenAPI 3)
- JUnit 5 + Mockito

---

## Correrlo localmente

Solo necesitas tener Docker Desktop instalado. Nada más.

**1. Clonar el repo**
```bash
git clone https://github.com/TU_USUARIO/mini-core-bancario.git
cd mini-core-bancario
```

**2. Crear el archivo de variables de entorno**
```bash
cp .env.example .env
```
El `.env.example` ya tiene valores por defecto que funcionan. No necesitas cambiar nada para probarlo.

**3. Levantar todo con Docker**
```bash
docker compose up --build -d
```
La primera vez tarda unos minutos porque descarga las dependencias. Las siguientes veces es mucho más rápido.

**4. Abrir Swagger**
```
http://localhost:8080/swagger-ui/index.html
```

Al arrancar, el sistema crea automáticamente dos usuarios de prueba:

| Usuario | Email | Contraseña | Balance |
|---|---|---|---|
| Alice Pérez | alice@demo.com | password123 | S/. 1,000.00 |
| Bob Rodríguez | bob@demo.com | password123 | S/. 500.00 |

---

## Endpoints

| Método| Endpoint | Descripción |
|---|---|---|---|
| POST | `/api/v1/auth/login`   | Obtener token JWT |
| POST | `/api/v1/usuarios/registro`  | Registrar usuario |
| POST | `/api/v1/transferencias/deposito`  | Depositar dinero |
| POST | `/api/v1/transferencias`  | Transferir entre cuentas |
| GET | `/api/v1/transferencias/historial/{id}` | Historial paginado |

---

## Tests

Cubrí los casos más importantes del servicio de transferencias:
-  Transferencia exitosa
-  Falla por saldo insuficiente
-  Falla por auto-transferencia
-  Falla por cuenta inexistente

---

## Estructura del proyecto

src/main/java/com/portafolio/billetera/
├── config/        # Seguridad, JWT, Swagger, CORS, datos de prueba
├── controller/    # Endpoints REST
├── service/       # Lógica de negocio
├── repository/    # Acceso a datos
├── entity/        # Entidades JPA
├── dto/           # Request y Response
└── exception/     # Manejo global de errores

---

## Funcionalidades

**Autenticación**
- Registro de usuario con contraseña hasheada en Argon2
- Login con JWT (token válido por 8 horas)
- Endpoints protegidos, solo accesibles con token válido

**Cuentas**
- Cada usuario tiene una cuenta bancaria generada automáticamente al registrarse
- Número de cuenta único con formato `BV-XXXXXXXXXX`

**Transferencias**
- Depósito de dinero a cualquier cuenta
- Transferencia entre dos cuentas distintas
- Validación de saldo antes de transferir
- No se permiten balances negativos
- No se permite transferirse dinero a uno mismo

**Historial**
- Registro inmutable de cada movimiento (no se puede editar ni borrar)
- Historial paginado por cuenta, ordenado del más reciente al más antiguo

**Integridad y concurrencia**
- Rollback automático si una transferencia falla a la mitad
- Bloqueo pesimista para evitar doble gasto en operaciones simultáneas
- Prevención de deadlocks ordenando los locks por ID
- Optimistic Locking como red de seguridad secundaria

**Infraestructura**
- Migraciones de base de datos versionadas con Flyway
- Datos de prueba cargados automáticamente al iniciar (Alice y Bob)
- Documentación interactiva con Swagger UI
- Dockerizado completo: un solo comando levanta todo el sistema

---


## Cosas a agregar

- Implementar notificaciones
- Agregar más cobertura de tests incluyendo tests de integración
- Separar los ambientes con perfiles de Spring (dev, prod)
