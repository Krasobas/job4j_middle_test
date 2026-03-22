# Student Info System: Microservices

A distributed system built with Spring Boot, implementing asynchronous data retrieval via Kafka and integrating multiple storage types (PostgreSQL and MinIO).

> **Academic Note**: This architecture is a requirement of the Job4j programming course. The data flow with SOAP/XML intermediaries and synchronous Kafka request-reply is mandated by the curriculum and does not represent production best practices. See [Architectural Decisions](#architectural-decisions) for details.

## Architecture

```
                              Docker network
┌──────────┐    REST/JSON   ┌─────────────┐    Kafka     ┌─────────────┐
│ Browser  │ ◄────────────► │  Service R  │ ◄──────────► │  Service S  │
│          │   port 8080    │  (gateway)  │              │   (data)    │
└──────────┘                └──────┬──────┘              └──┬──────┬───┘
                                   │ HTTP (photos)          │      │
                                   └────────────────────────┘      │
                                                             ┌─────┴─────┐
                                                             │           │
                                                        ┌────┴───┐ ┌────┴───┐
                                                        │Postgres│ │ MinIO  │
                                                        └────────┘ └────────┘
```

### Data Flow

1. User sends `GET /api/students` to **Service R**.
2. Service R logs the request and sends a message to Kafka topic `student-request` with a correlation ID.
3. **Service S** consumes the message, queries **PostgreSQL** for student data and resolves photo keys from **MinIO**.
4. Service S marshals the response as SOAP XML and publishes it to Kafka topic `student-response` with the same correlation ID.
5. Service R receives the XML, transforms it to JSON, replaces internal `photoKey` with a public `photoUrl`, and returns clean JSON to the browser.
6. When the browser requests a photo, Service R proxies the request directly to Service S over HTTP (photos never travel through Kafka).

## Tech Stack

| Component         | Technology                                          |
|-------------------|-----------------------------------------------------|
| Language          | Java 21, Virtual Threads                            |
| Framework         | Spring Boot 3.4                                     |
| Messaging         | Apache Kafka (KRaft mode, no Zookeeper)             |
| Database          | PostgreSQL 17                                       |
| Object Storage    | MinIO (S3-compatible)                               |
| SOAP              | Spring Web Services (Contract-First, XSD → JAXB)   |
| Routing           | Apache Camel                                        |
| Security          | Spring Security (HTTP Basic)                        |
| API Docs          | Springdoc OpenAPI / Swagger UI                      |
| Schema Migration  | Liquibase                                           |
| Containerization  | Docker, Docker Compose V2                           |

## Project Structure

```
.
├── compose.yaml
├── service-r/                 # REST gateway, Kafka producer, XML→JSON
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/ru/job4j/r/
│       ├── client/            # HTTP Interface Client for Service S
│       ├── config/            # Kafka, Camel, Security, RestClient
│       ├── controller/        # REST endpoints
│       └── service/           # Kafka request-reply, transformation, cache
└── service-s/                 # SOAP service, Kafka consumer, data layer
    ├── Dockerfile
    ├── pom.xml
    └── src/main/java/ru/job4j/s/
        ├── config/            # SOAP (WebServiceConfig), MinIO
        ├── model/             # JPA entity
        ├── repository/        # Spring Data JPA
        ├── service/           # Business logic, photo service
        ├── controller/        # Internal photo endpoint
        ├── kafka/             # Kafka listener
        └── soap/              # SOAP endpoint, JAXB marshaller, gen/
```

## Getting Started

### Prerequisites

- Docker and Docker Compose V2
- Java 21 (for local development)
- Maven 3.9+

### Build and Run

```bash
docker compose up --build
```

This starts all five containers: PostgreSQL, MinIO, Kafka, Service S, and Service R.
Health checks ensure correct startup order: PostgreSQL and Kafka must be healthy before services start.

### Verify REST API

All REST endpoints require HTTP Basic authentication (`admin` / `admin`).

```bash
# List all students
curl -u admin:admin http://localhost:8080/api/students | jq

# Get single student
curl -u admin:admin http://localhost:8080/api/students/RB-001 | jq

# Download student photo
curl -u admin:admin http://localhost:8080/api/students/RB-001/photo -o photo.jpg
```

Expected response:

```json
[
  {
    "recordBookNumber": "RB-001",
    "faculty": "Computer Science",
    "lastName": "Ivanov",
    "firstName": "Ivan",
    "middleName": "Ivanovich",
    "photoUrl": "/api/students/RB-001/photo"
  }
]
```

### Verify SOAP

Service S SOAP interface is internal (not exposed to host). Verify from within the Docker network:

```bash
docker compose exec service-r wget -qO- http://service-s:9900/ws/students.wsdl
```

This should return the WSDL document with `getAllStudents` and `getStudent` operations.

### Verify Swagger UI

Open in browser: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

### Stop

```bash
docker compose down
```

To also remove stored data (PostgreSQL, MinIO):

```bash
docker compose down -v
```

## API Reference

### Service R — REST (port 8080, exposed)

| Method | Endpoint                        | Auth     | Description              |
|--------|---------------------------------|----------|--------------------------|
| `GET`  | `/api/students`                 | Required | List all students (JSON) |
| `GET`  | `/api/students/{id}`            | Required | Get student by ID (JSON) |
| `GET`  | `/api/students/{id}/photo`      | Required | Get student photo (JPEG) |
| `GET`  | `/swagger-ui.html`              | Public   | API documentation        |
| `GET`  | `/actuator/health`              | Public   | Health check             |

### Service S — SOAP (port 9900, internal only)

| Operation                | Description                              |
|--------------------------|------------------------------------------|
| `getAllStudentsRequest`   | Returns all student records as XML       |
| `getStudentRequest`      | Returns single student by record book number |

WSDL: `http://service-s:9900/ws/students.wsdl` (accessible within Docker network only)

## Architectural Decisions

**Why Kafka for GET requests?** The assignment requires a message broker between services. In production, synchronous reads would use direct HTTP/gRPC. Kafka is appropriate for async events after write operations (e.g., `StudentCreated`, `StudentExpelled`), not for synchronous request-reply. Implemented as specified by the curriculum.

**Why SOAP on Service S?** Assignment requirement to demonstrate legacy protocol integration. In production, gRPC or REST would be the standard choice.

**Why photos via HTTP instead of Kafka?** Kafka has a default message size limit of 1 MB. Photos can be 5-10 MB. Binary data is streamed via direct HTTP between services, bypassing Kafka entirely.

**Why Apache Camel?** Assignment requirement. For two linear routes it adds no practical value. Camel is justified in systems with dozens of integrations across different transports (Kafka, FTP, JMS, SMTP).

---

*Developed as a technical demonstration for the Job4j programming course.*