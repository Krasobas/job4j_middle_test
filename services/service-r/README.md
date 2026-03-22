# Service R: API Gateway & Transformer

Service R is the single entry point for external clients. It accepts REST requests, routes them through Kafka to Service S, transforms XML responses to JSON, and proxies student photos.

## Responsibilities

- **REST API** — JSON interface for browsers and clients, secured with Spring Security (HTTP Basic).
- **Kafka Request-Reply** — sends requests to Service S via Kafka and awaits responses using `ReplyingKafkaTemplate` with correlation ID matching.
- **XML → JSON Transformation** — converts SOAP/XML responses from Service S into clean JSON for the client.
- **Photo Proxying** — forwards photo requests to Service S over HTTP. MinIO is never exposed to the outside.
- **Routing (Apache Camel)** — linear request chains (Kafka → transform → respond) are handled by Camel routes.
- **Logging** — all incoming requests, Kafka interactions, and transformations are logged to console.

## API Endpoints

All endpoints require HTTP Basic authentication (`admin` / `admin`).

| Method | Endpoint                        | Description              |
|--------|---------------------------------|--------------------------|
| `GET`  | `/api/students`                 | List all students (JSON) |
| `GET`  | `/api/students/{id}`            | Get student by ID (JSON) |
| `GET`  | `/api/students/{id}/photo`      | Get student photo (JPEG) |

Swagger UI available at `/swagger-ui.html`.

## Request Flow

### Data (via Kafka)

```
Browser → GET /api/students
       → Service R
       → Kafka topic: student-request  (+ correlation ID)
       → Service S processes, queries PostgreSQL
       → Kafka topic: student-response (+ same correlation ID)
       → Service R receives, XML → JSON
       → Browser gets JSON
```

### Photos (via HTTP)

```
Browser → GET /api/students/RB-001/photo
       → Service R
       → HTTP GET → Service S /internal/photos/{key}
       → Service S fetches from MinIO
       → byte[] streamed back to Browser
```

## Tech Stack

| Technology              | Purpose                                    |
|-------------------------|--------------------------------------------|
| Spring Boot 3.4         | Core framework                             |
| Spring Web              | REST controllers                           |
| Spring Kafka            | `ReplyingKafkaTemplate` for request-reply  |
| Spring Security         | HTTP Basic authentication                  |
| Spring WS               | SOAP client capabilities                   |
| Apache Camel            | Declarative route definitions              |
| Jackson + JAXB          | XML ↔ JSON transformation                  |
| Springdoc OpenAPI       | Swagger UI at `/swagger-ui.html`           |
| HTTP Interface Client   | Type-safe HTTP calls to Service S          |
| Virtual Threads (Java 21) | Efficient blocking on Kafka replies      |

## Configuration

```yaml
server:
  port: 8080

spring:
  kafka:
    bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
  security:
    user:
      name: admin
      password: admin
  threads:
    virtual:
      enabled: true

service-s:
  url: ${SERVICE_S_URL:http://service-s:9900}
```

## Project Structure

```
service-r/src/main/java/ru/job4j/r/
├── ServiceRApplication.java
├── client/
│   └── ServiceSClient.java          # HTTP Interface Client for photo proxying
├── config/
│   ├── CamelRouteConfig.java        # Camel routes: Kafka → transform → respond
│   ├── KafkaConfig.java             # ReplyingKafkaTemplate + reply container
│   ├── RestClientConfig.java        # ServiceSClient bean factory
│   └── SecurityConfig.java          # HTTP Basic, Swagger/actuator permit
├── controller/
│   └── StudentRestController.java   # REST endpoints
└── service/
    ├── StudentRequestService.java   # Kafka send-and-receive
    ├── XmlToJsonTransformer.java    # XML → JSON + photoKey → photoUrl
    ├── XmlPhotoKeyExtractor.java    # Extract photoKey from XML
    └── PhotoKeyCache.java           # In-memory cache: recordBookNumber → photoKey
```