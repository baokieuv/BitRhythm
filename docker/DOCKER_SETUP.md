# BitRhythm Docker setup

This directory contains the local Docker Compose stack for BitRhythm. The
recommended entrypoint is `docker-compose.yml`, which includes:

- `docker-compose.common.yml`: Kafka, Redis, MinIO, the MinIO bucket setup job,
  shared networks, and shared volumes.
- `docker-compose.auth.yml`: the authentication service and its MariaDB
  database.
- `docker-compose.notification.yml`: the notification service, its MariaDB
  database, and Mailpit.
- `docker-compose.gateway.yml`: the API gateway.

The backend source is built from `..\backend`, so the repository must contain
both `docker` and `backend` directories.

## Prerequisites

- Docker Desktop with the Linux container engine enabled.
- Docker Compose v2 (`docker compose ...`).
- A running Docker daemon.
- The BitRhythm repository checked out with `backend` next to `docker`.

Run all commands below from this directory:

```powershell
cd D:\HUST\after_uni\BitRhythm\docker
```

## 1. Create local environment files

Copy every example file to the filename used by Compose. These files are
ignored by Git and must not be committed:

```powershell
Copy-Item .\env\auth.env.example .\env\auth.env
Copy-Item .\env\gateway.env.example .\env\gateway.env
Copy-Item .\env\minio.env.example .\env\minio.env
Copy-Item .\env\notification.env.example .\env\notification.env
Copy-Item .\env\redis.env.example .\env\redis.env
Copy-Item .\env\shared.env.example .\env\shared.env
Copy-Item .\env\.env.example .\env\.env
Copy-Item .\env\user.env.example .\env\user.env
```

Edit the copied files before starting the stack:

- `auth.env`: database passwords, the initial admin password, and optional JWT
  keys.
- `gateway.env`: gateway origins, service URLs, JWT settings, and the internal
  API key.
- `minio.env`: MinIO credentials and browser/API URLs.
- `notification.env`: notification database credentials and email settings.
- `redis.env`: the Redis password.
- `shared.env`: values shared by services, including `INTERNAL_API_KEY` and
  Kafka settings.
- `env\.env`: optional host-port overrides used for Compose interpolation.

At minimum, replace every `change-me-*` value. Keep the following values
consistent:

- `auth.env:DB_PASSWORD` = `auth.env:MARIADB_PASSWORD`
- `notification.env:DB_PASSWORD` =
  `notification.env:MARIADB_PASSWORD`
- `auth.env:TOKEN_ISSUER` = `gateway.env:JWT_ISSUER` =
  `notification.env:JWT_ISSUER`
- `auth.env:JWT_KEY_ID` and the JWT public/private key pair must belong
  together.
- `shared.env:INTERNAL_API_KEY` must match the value expected by the services.

### JWT keys

For local development, leaving `JWT_PRIVATE_KEY` and `JWT_PUBLIC_KEY` empty
allows the authentication service to create an ephemeral key pair. Tokens then
become invalid whenever the service restarts.

For a stable local setup, generate an RSA key pair and put the PEM body on one
line in `auth.env`:

```powershell
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out private.pem
openssl rsa -pubout -in private.pem -out public.pem
(Get-Content .\private.pem) -notmatch '^-----' -join ''
(Get-Content .\public.pem) -notmatch '^-----' -join ''
```

Do not commit the key files or the resulting values.

### Compose-level `.env` note

`env\.env.example` contains Compose-level settings, not application
variables. The current stack is assembled by `docker-compose.yml`; use that
file explicitly:

```powershell
docker compose --env-file .\env\.env -f .\docker-compose.yml config
```

The `COMPOSE_FILE` value currently shown in `env\.env.example` refers to
older Compose files and services. Do not rely on it for the current stack.
If it is present in `env\.env`, the commands in this guide still select
`docker-compose.yml` explicitly.

## 2. Check the merged Compose configuration

Before starting containers, render the merged configuration:

```powershell
docker compose --env-file .\env\.env -f .\docker-compose.yml config
```

Review the output for missing environment files, unresolved variables, or
unexpected host ports. This command does not start containers.

## 3. Start the complete stack

Build the backend images and start all services in the background:

```powershell
docker compose --env-file .\env\.env -f .\docker-compose.yml up -d --build
```

The first startup may take several minutes. Database and application services
wait for their configured health checks. `minio-setup` creates the
`music-streaming` bucket and applies a local-development read policy.

To start without rebuilding images:

```powershell
docker compose --env-file .\env\.env -f .\docker-compose.yml up -d
```

## 4. Verify the stack

List container status and health:

```powershell
docker compose --env-file .\env\.env -f .\docker-compose.yml ps
```

Follow all logs:

```powershell
docker compose --env-file .\env\.env -f .\docker-compose.yml logs -f
```

Follow one service:

```powershell
docker compose --env-file .\env\.env -f .\docker-compose.yml logs -f auth-service
```

Useful local endpoints and host ports:

| Component | URL or address | Default |
|---|---|---:|
| Gateway | `http://localhost:8080` | `GATEWAY_HOST_PORT=8080` |
| Auth service | `http://localhost:8081` | `AUTH_SERVICE_HOST_PORT=8081` |
| Notification service | `http://localhost:8084` | `NOTIFICATION_SERVICE_HOST_PORT=8084` |
| Mailpit inbox | `http://localhost:8025` | `MAILPIT_UI_HOST_PORT=8025` |
| MinIO API | `http://localhost:9000` | `MINIO_API_HOST_PORT=9000` |
| MinIO console | `http://localhost:9001` | `MINIO_CONSOLE_HOST_PORT=9001` |
| Redis | `localhost:6379` | `REDIS_HOST_PORT=6379` |
| Kafka external listener | `localhost:9094` | `KAFKA_HOST_PORT=9094` |
| Auth MariaDB | `localhost:3307` | `AUTH_DB_HOST_PORT=3307` |
| Notification MariaDB | `localhost:3308` | `NOTIFICATION_DB_HOST_PORT=3308` |

The service-to-service addresses are different from host addresses. Containers
use names such as `kafka:9092`, `redis:6379`, `auth-database:3306`, and
`auth-service:8081`.

## 5. Daily operations

Stop containers without deleting data:

```powershell
docker compose --env-file .\env\.env -f .\docker-compose.yml stop
```

Start previously created containers:

```powershell
docker compose --env-file .\env\.env -f .\docker-compose.yml start
```

Stop and remove containers and networks while preserving named volumes:

```powershell
docker compose --env-file .\env\.env -f .\docker-compose.yml down
```

Rebuild one backend image:

```powershell
docker compose --env-file .\env\.env -f .\docker-compose.yml build auth-service
docker compose --env-file .\env\.env -f .\docker-compose.yml up -d auth-service
```

## Data and reset behavior

The stack persists data in:

- `kafka_data` and `redis_data` named Docker volumes.
- `auth_database_data` and `notification_database_data` named Docker volumes.
- The `minio_data` directory in this folder.

`docker compose down` does not remove these volumes. To intentionally reset
the databases and Kafka/Redis data, remove the Compose volumes:

```powershell
docker compose --env-file .\env\.env -f .\docker-compose.yml down -v
```

This permanently deletes the named-volume data. The `minio_data` directory is
not removed by that command; back it up or remove its contents separately
only when a full MinIO reset is intended.

## Common problems

### `env/notification.env` is missing

Create it from `env/notification.env.example` as shown in the setup step.

### Ports are already in use

Change the corresponding host-port variable in `env\.env`, for example:

```dotenv
GATEWAY_HOST_PORT=18080
```

The port on the right side of the mapping remains the container port.

### A service cannot connect to another service

Use the Docker service name and container port, not `localhost`. For example,
the gateway reaches auth at `http://auth-service:8081`, while a browser on the
host reaches it at `http://localhost:8081`.

### Authentication fails after a restart

If the JWT key variables are empty, the auth service uses ephemeral keys.
Configure a persistent key pair in `auth.env` and restart the stack.

### Emails are not visible in a real inbox

The default provider is Mailpit. Open `http://localhost:8025` to inspect
messages. To send real email, configure the Gmail app-password settings in
`notification.env`, set `EMAIL_PROVIDER=gmail`, and never commit those
credentials.

## Production warning

These Compose files are configured for local development: plaintext internal
traffic, development credentials, a public-read MinIO bucket policy, and
Mailpit. Before production use, replace credentials with secret-manager
values, configure TLS and secure Kafka/Redis connections, remove Mailpit,
restrict MinIO access, and use persistent JWT keys.
