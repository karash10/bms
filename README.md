# BMS - Booking Management System

BMS is a multi-event booking application built with Spring Boot, Thymeleaf, HTMX, H2, and Redis.
It supports movies, concerts, and sports events with booking, payment simulation, admin management, reporting, and caching.

## Tech Stack

- Java 21
- Spring Boot 4.0.2
- Spring Security 7
- Spring Data JPA + Hibernate 7
- Thymeleaf + HTMX
- H2 in-memory database
- Redis
- Maven Wrapper
- Docker + Docker Compose

## What You Need Before Running

Install these tools first:

- Java 21
- Docker Desktop or a local Redis server

You do not need Maven installed because the project includes `mvnw` and `mvnw.cmd`.

## Project Behavior You Should Know

- The app uses an in-memory H2 database, so data resets every time the application restarts.
- Redis is used for caching and seat-lock related components.
- On startup, demo data is seeded automatically.
- Browser login uses a JWT stored in an `HttpOnly` cookie named `BMS_TOKEN`.

## Demo Accounts

These users are created automatically at startup:

- Admin: `admin@bms.com` / `admin123`
- User: `alice@example.com` / `password`
- User: `bob@example.com` / `password`

## Run Locally - Step by Step

### 1. Clone the project

```bash
git clone <your-repository-url>
cd bms
```

### 2. Start Redis

If you already have Redis on `localhost:6379`, keep it running and skip to the next step.

If you want to start Redis with Docker:

```bash
docker run --name bms-redis -p 6379:6379 redis:7-alpine
```

### 3. Run the application

On Windows:

```bash
mvnw.cmd spring-boot:run
```

On macOS/Linux:

```bash
./mvnw spring-boot:run
```

### 4. Open the app

Once startup finishes, open:

- App: `http://localhost:8080`
- H2 Console: `http://localhost:8080/h2-console`
- Actuator Health: `http://localhost:8080/actuator/health`

### 5. H2 console settings

Use these values in the H2 console:

- JDBC URL: `jdbc:h2:mem:bms`
- Username: `sa`
- Password: leave blank

## Build the Jar

If you want a packaged jar instead of `spring-boot:run`:

On Windows:

```bash
mvnw.cmd clean package -DskipTests
java -jar target/bms-0.0.1-SNAPSHOT.jar
```

On macOS/Linux:

```bash
./mvnw clean package -DskipTests
java -jar target/bms-0.0.1-SNAPSHOT.jar
```

Redis still needs to be running before starting the jar.

## Run with Docker Compose

This is the easiest way to run the full app with Redis.

### 1. Start everything

```bash
docker compose up --build
```

### 2. Open the app

- App: `http://localhost:8080`
- Redis: `localhost:6379`

### 3. Stop everything

```bash
docker compose down
```

## Run with Docker Only

If you want to build the image manually:

### 1. Build the image

```bash
docker build -t bms .
```

### 2. Make sure Redis is running

Example:

```bash
docker run --name bms-redis -p 6379:6379 redis:7-alpine
```

### 3. Run the app container

```bash
docker run -p 8080:8080 -e SPRING_DATA_REDIS_HOST=host.docker.internal -e SPRING_DATA_REDIS_PORT=6379 bms
```

On Linux, replace `host.docker.internal` with the reachable Redis host if needed.

## Run Tests

On Windows:

```bash
mvnw.cmd test
```

On macOS/Linux:

```bash
./mvnw test
```

The test profile disables Redis requirements, so tests run without Redis.

## Useful Endpoints

- Home: `http://localhost:8080/`
- Login: `http://localhost:8080/login`
- Register: `http://localhost:8080/register`
- Admin dashboard: `http://localhost:8080/admin/dashboard`
- H2 console: `http://localhost:8080/h2-console`
- Health: `http://localhost:8080/actuator/health`
- Metrics: `http://localhost:8080/actuator/metrics`
- Caches: `http://localhost:8080/actuator/caches`

## Common Problems

### Redis connection error on startup

Cause: Redis is not running.

Fix: Start Redis first, then restart the application.

### Port 8080 already in use

Cause: Another app is already running on port 8080.

Fix: Stop the other app or run BMS on a different port.

Example:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

### Port 6379 already in use

Cause: Another Redis instance is already using the port.

Fix: Reuse the existing Redis server or stop the conflicting service.

## Development Notes

- The database is reset on every restart because H2 is in-memory.
- Demo events, venues, shows, and bookings are reseeded on startup.
- Payment processing is simulated.
- Admin and browser pages are server-rendered with Thymeleaf and HTMX.

## Verified Commands

These commands were verified successfully in this project:

- `./mvnw test`
- `./mvnw clean package -DskipTests`
