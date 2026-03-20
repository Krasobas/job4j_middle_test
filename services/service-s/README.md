# 🛠 Service S: Data Provisioning Service

Service S is the core data provider of the system. It orchestrates information from relational and object storage and exposes it via a **SOAP interface**.

## 🎯 Responsibilities
* **Data Retrieval**: Fetches student records from **PostgreSQL**.
* **Object Storage**: Retrieves binary data (student photos) from **MinIO**.
* **SOAP Interface**: Implements the contract-first web service for internal system communication.
* **Message Handling**: Consumes requests from the Broker and produces XML-based responses.

## 💾 Storage Details
* **PostgreSQL**: Stores student profiles (ID, Faculty, Last Name, First Name, Middle Name).
* **MinIO**: S3-compatible storage for image files associated with student IDs.

## 🔌 API Reference (SOAP)
The service exposes a WSDL at: `http://localhost:8082/ws/students.wsdl`

### Available Operations:
* `getAllUnitsRequest`: Returns a list of all students with their attributes.
* `getOneUnitRequest`: Returns a single student record by their "Record Book Number" (ID).

## ⚙️ Configuration
Key properties in `application.yml`:
- `spring.datasource.url`: Connection string for PostgreSQL.
- `minio.bucket.name`: Target bucket for student photos.
- `spring.rabbitmq.template.routing-key`: Queue for sending processed data back.

## 🛠 Tech Stack
- **Spring Boot**: Core framework.
- **Spring Web Services**: SOAP implementation.
- **Spring Data JPA**: Database abstraction.
- **MinIO Java SDK**: S3 integration.