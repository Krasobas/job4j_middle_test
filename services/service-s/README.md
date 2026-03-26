# Service S: Data Provider

Service S owns all student data. It stores records in PostgreSQL, photos in MinIO, and exposes them via SOAP interface and Kafka listener. No external traffic reaches this service directly — it communicates only within the Docker network.

## Responsibilities

- **Data Storage** — student records in PostgreSQL, managed by Liquibase migrations.
- **Photo Storage** — student photos in MinIO (S3-compatible). Served via internal HTTP endpoint.
- **SOAP Interface** — Contract-First web service (XSD → JAXB). Operations: `getAllStudents`, `getStudent`.
- **Kafka Consumer** — listens on `student-request` topic, processes requests, publishes XML responses to `student-response` with correlation ID.
- **JAXB Marshalling** — converts JPA entities to SOAP XML for both SOAP endpoint and Kafka responses.

## Storage

### PostgreSQL

Table `students` with the following columns:

| Column               | Type         | Constraint       |
|----------------------|--------------|------------------|
| `id`                 | `bigint`     | PK, auto-increment |
| `record_book_number` | `varchar(20)`| unique, not null |
| `faculty`            | `varchar(100)`| not null        |
| `last_name`          | `varchar(100)`| not null        |
| `first_name`         | `varchar(100)`| not null        |
| `middle_name`        | `varchar(100)`| nullable        |
| `photo_key`          | `varchar(255)`| nullable        |

Schema created by Liquibase. Hibernate runs in `validate` mode — it checks the schema but never modifies it.

### MinIO

Bucket `students` stores photo files. `photo_key` in the database maps to the object key in MinIO (e.g., `photo_001.jpg`). MinIO is never exposed outside the Docker network — Service R proxies photo requests via the internal HTTP endpoint.

## API

### SOAP (port 8080)

WSDL: `http://service-s:8080/ws/students.wsdl` (internal, not exposed to host)

| Operation                | Input                 | Output                          |
|--------------------------|-----------------------|---------------------------------|
| `getAllStudentsRequest`   | (none)               | List of all students as XML     |
| `getStudentRequest`      | `recordBookNumber`   | Single student as XML           |

Verify from within Docker network:

```bash
docker compose exec service-r wget -qO- http://service-s:8080/ws/students.wsdl
```

### Kafka

| Topic              | Role     | Payload                          |
|--------------------|----------|----------------------------------|
| `student-request`  | Consumer | `"ALL"` or `recordBookNumber`    |
| `student-response` | Producer | Marshalled SOAP XML + correlation ID |

Correlation ID from the request headers is copied to the response. Without it, Service R's `ReplyingKafkaTemplate` cannot match the response and times out.

### Internal HTTP (port 8080)

| Method | Endpoint                  | Description                      |
|--------|---------------------------|----------------------------------|
| `GET`  | `/internal/photos/{key}`  | Returns photo bytes from MinIO   |

This endpoint is called by Service R to proxy photos to the browser. Not accessible from outside Docker network.

## Tech Stack

| Technology              | Purpose                                    |
|-------------------------|--------------------------------------------|
| Spring Boot 3.4         | Core framework                             |
| Spring Web Services     | SOAP endpoint (Contract-First)             |
| Spring Data JPA         | PostgreSQL access                          |
| Spring Kafka            | Consumer on `student-request`, producer on `student-response` |
| JAXB                    | XML marshalling (XSD → Java classes)       |
| MinIO Java SDK 8.5.14   | S3-compatible object storage client        |
| Liquibase               | Database schema migration                  |
| wsdl4j                  | WSDL generation for `DefaultWsdl11Definition` |

## Configuration

```yaml
server:
  port: 8080

spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/students_db}
    username: ${SPRING_DATASOURCE_USERNAME:app}
    password: ${SPRING_DATASOURCE_PASSWORD:secret}
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
  kafka:
    bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.yaml

minio:
  url: ${MINIO_URL:http://localhost:9000}
  access-key: ${MINIO_ACCESS_KEY:minioadmin}
  secret-key: ${MINIO_SECRET_KEY:minioadmin}
  bucket: students
```

## Project Structure

```
service-s/src/main/
├── java/ru/job4j/s/
│   ├── ServiceSApplication.java
│   ├── config/
│   │   ├── minio/
│   │   │   ├── MinioConfig.java              # MinioClient bean
│   │   │   ├── MinioInitializer.java         # Verifies bucket on startup
│   │   │   └── MinioHealthIndicator.java     # Actuator health check
│   │   └── soap/
│   │       └── WebServiceConfig.java         # MessageDispatcherServlet + WSDL
│   ├── model/
│   │   └── Student.java                      # JPA entity
│   ├── repository/
│   │   └── StudentRepository.java            # Spring Data JPA
│   ├── service/
│   │   ├── StudentService.java               # JPA ↔ JAXB mapping
│   │   ├── StudentNotFoundException.java
│   │   └── PhotoService.java                 # MinIO get/put operations
│   ├── controller/
│   │   └── InternalPhotoController.java      # GET /internal/photos/{key}
│   ├── kafka/
│   │   └── StudentKafkaListener.java         # Kafka consumer + response producer
│   └── soap/
│       ├── StudentEndpoint.java              # @Endpoint with @PayloadRoot
│       ├── JaxbMarshaller.java               # Reusable JAXBContext + marshal()
│       └── gen/                              # Generated from XSD (jaxb2-maven-plugin)
│           ├── GetAllStudentsRequest.java
│           ├── GetAllStudentsResponse.java
│           ├── GetStudentRequest.java
│           ├── GetStudentResponse.java
│           ├── StudentType.java
│           └── ObjectFactory.java
└── resources/
    ├── application.yaml
    ├── xsd/
    │   └── students.xsd                      # SOAP contract (namespace: http://job4j.ru/students)
    └── db/changelog/
        ├── db.changelog-master.yaml
        └── migrations/
            ├── 001-create-students-table.yaml
            └── 002-insert-test-data.yaml
```