# 🚀 FinFlow — Loan Management System

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-6DB33F)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.1-6DB33F)
![Microservices](https://img.shields.io/badge/Architecture-Microservices-blue)
![Security](https://img.shields.io/badge/Security-JWT-red)
![Database](https://img.shields.io/badge/Database-MySQL%208.0-4479A1)
![RabbitMQ](https://img.shields.io/badge/Messaging-RabbitMQ-FF6600)
![Docker](https://img.shields.io/badge/Container-Docker-2496ED)
![Build](https://img.shields.io/badge/Build-Maven-C71A36)
![Status](https://img.shields.io/badge/Status-Active-success)

## 📌 Overview

**FinFlow** is a production-ready, microservices-based loan management system built with **Spring Boot 3** and **Spring Cloud**. It enables users to apply for loans across **6 loan categories**, upload required documents, track application status, and receive event-driven notifications. Admins can review submitted applications, verify documents, approve or reject loans, and generate dashboard reports.

The system implements a complete **distributed architecture** with service discovery (Eureka), centralized configuration (Config Server), API Gateway routing, asynchronous event-driven messaging (RabbitMQ), distributed tracing (Zipkin), inter-service communication (OpenFeign), and containerized deployment (Docker Compose).

---

## 🧠 Key Features

### 👤 Applicant (USER Role)

- JWT-based registration & login with terms acceptance
- Create and manage loan applications (Draft → Submit)
- Apply for **6 loan types**: Home, Education, Business, Vehicle, Personal, Marriage
- Upload loan-type-specific documents with validation
- Replace previously uploaded documents
- Track application status in real-time
- Co-applicant support (name, income, occupation)

### 🛠️ Admin (ADMIN Role)

- View all submitted applications (draft applications are hidden)
- Review individual application details
- Verify or reject uploaded documents with remarks
- Approve loans (only after all documents are verified and complete)
- Reject loans with mandatory remarks
- Generate dashboard reports (total, approved, rejected, pending counts)

### 🔔 Notifications (Event-Driven)

- Asynchronous event processing via RabbitMQ
- Events for: user registration, loan status changes, document updates
- Notification history persisted to database
- Extensible architecture for email/SMS integration

---

## 🏗️ Architecture

This system consists of **8 independently deployable services**:

| Service | Port | Description |
|---|---|---|
| **Config Server** | 8888 | Centralized configuration management |
| **Discovery Server** | 8761 | Eureka-based service registry |
| **API Gateway** | 8080 | Single entry point with route management |
| **Auth Service** | — | Authentication, JWT generation, role management |
| **Application Service** | — | Loan application lifecycle management |
| **Document Service** | — | Document upload, validation & verification |
| **Admin Service** | — | Admin workflows, decisions & reporting |
| **Notification Service** | — | Event-driven notification processing |

### Infrastructure Components

| Component | Purpose |
|---|---|
| **MySQL 8.0** | Separate database per service (5 databases) |
| **RabbitMQ** | Asynchronous inter-service messaging |
| **Zipkin** | Distributed tracing & request tracking |

---

## 🔁 System Flow

```
                          ┌─────────────────────────────────────────┐
                          │            API Gateway (:8080)           │
                          └──────────────┬──────────────────────────┘
                                         │
            ┌────────────┬───────────────┼───────────────┬──────────────┐
            ▼            ▼               ▼               ▼              ▼
       Auth Service   Application    Document        Admin         Notification
       (JWT Auth)     Service        Service         Service        Service
            │            │               │               │              ▲
            │            │◄──Feign──────►│               │              │
            │◄───Feign───┤               │◄──Feign──────►│              │
            │            │               │               │         [RabbitMQ]
            │            │               │               │              │
            ▼            ▼               ▼               ▼              │
         auth_db     application_db  document_db     admin_db     notification_db
```

---

## 🔄 Loan Application Lifecycle

```
DRAFT  ──►  SUBMITTED  ──►  APPROVED
                │
                └──────────►  REJECTED
```

- **DRAFT** → Application created, editable by user
- **SUBMITTED** → Application submitted, visible to admin for review
- **APPROVED** → Admin approves after all documents are verified
- **REJECTED** → Admin rejects with mandatory remarks

---

## 🧑‍💻 Tech Stack

### Backend

| Technology | Usage |
|---|---|
| Java 17 | Core language |
| Spring Boot 3.2.5 | Application framework |
| Spring Security | Authentication & authorization |
| Spring Cloud Gateway | API routing (WebFlux-based) |
| Spring Cloud Config | Centralized configuration |
| Netflix Eureka | Service discovery & registry |
| Spring Data JPA (Hibernate) | ORM & database access |
| OpenFeign | Declarative inter-service communication |
| Spring AMQP | RabbitMQ messaging integration |
| Micrometer + Zipkin | Distributed tracing |
| SpringDoc OpenAPI | Swagger UI & API documentation |
| JJWT (0.11.5) | JWT token generation & validation |
| Lombok | Boilerplate code reduction |

### Database & Messaging

| Technology | Usage |
|---|---|
| MySQL 8.0 | Relational database (5 isolated databases) |
| RabbitMQ 3 | Asynchronous event-driven messaging |

### DevOps & Tools

| Technology | Usage |
|---|---|
| Docker & Docker Compose | Containerized deployment |
| Maven | Build & dependency management |
| Postman | API testing |
| Git & GitHub | Version control |

---

## 📁 Microservices Breakdown

### 🔐 Auth Service

- User registration (signup) & login with JWT token generation
- Role-based authentication: `USER` and `ADMIN`
- Terms & conditions acceptance workflow
- Token validation endpoint
- User lookup by email (used internally by other services via Feign)
- Publishes user registration events to RabbitMQ

---

### 📄 Application Service

- Create loan application in `DRAFT` status
- Support for **6 loan types**: `HOME`, `EDUCATION`, `BUSINESS`, `VEHICLE`, `PERSONAL`, `MARRIAGE`
- **4 occupation types**: `STUDENT`, `SALARIED`, `BUSINESS`, `UNEMPLOYED`
- Update application details (user only, draft only)
- Submit application (transitions from `DRAFT` → `SUBMITTED`)
- Co-applicant information support
- Role-aware status tracking (users see only their own, admins see all)
- Internal endpoints for admin approval/rejection via Feign

---

### 📂 Document Service

- **Loan-type-specific upload endpoints** — each loan type requires different documents:
  - `/upload/home` — Home loan documents
  - `/upload/education` — Education loan documents
  - `/upload/business` — Business loan documents
  - `/upload/vehicle` — Vehicle loan documents
  - `/upload/personal` — Personal loan documents
  - `/upload/marriage` — Marriage loan documents
- Document replacement with file management
- Document completeness validation per loan type
- Blocks admin users from uploading documents (applicants only)
- Internal endpoints for admin document verification/rejection
- Local file system storage with unique timestamped filenames

---

### 🧑‍💼 Admin Service

- View all submitted applications (automatically excludes `DRAFT`)
- View individual application details via Feign (Application Service)
- View documents by application ID via Feign (Document Service)
- **Verify documents** — marks individual documents as verified
- **Reject documents** — with mandatory remarks
- **Approve applications** — with triple validation:
  1. Application must be in `SUBMITTED` status
  2. All uploaded documents must be `VERIFIED`
  3. All required documents for the loan type must be present
- **Reject applications** — with mandatory remarks
- **Dashboard reporting** — generates reports with counts:
  - Total applications, approved, rejected, pending
- Persists all decisions (approve/reject) with admin ID & timestamps

---

### 📩 Notification Service

- Consumes events from RabbitMQ (`notification-queue`)
- Listens to three routing keys:
  - `auth.user.#` — User registration events
  - `loan.status.#` — Loan approval/rejection events
  - `document.status.#` — Document verification events
- Persists notification history to database
- Mock email logging (extensible to real email/SMS providers)
- JSON message serialization with Jackson

---

## 🔐 Security

- **JWT-based authentication** using JJWT library
- **Role-based access control (RBAC)**: `USER` and `ADMIN` roles
- Token propagation across services via `Authorization` header
- Admin-only endpoints are protected with role validation
- User-only endpoints (create/update/submit applications, upload documents) block admin access
- Ownership validation — users can only access their own applications
- API Gateway as the single secured entry point

---

## 📡 API Endpoints

### Auth Service — `/auth`

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/auth/signup` | Public | Register new user |
| POST | `/auth/login` | Public | Login & get JWT token |
| POST | `/auth/accept-terms/{userId}` | Public | Accept terms & conditions |
| GET | `/auth/test-token` | Authenticated | Validate JWT token |
| GET | `/auth/user/email/{email}` | Authenticated | Get user ID by email |

### Application Service — `/applications`

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/applications` | USER | Create loan application |
| PUT | `/applications/{id}` | USER | Update application |
| POST | `/applications/{id}/submit` | USER | Submit application |
| GET | `/applications/my` | USER | Get own applications |
| GET | `/applications/{id}/status` | USER / ADMIN | Get application status |

### Document Service — `/documents`

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/documents/upload/{loanType}` | USER | Upload document by loan type |
| PUT | `/documents/{id}` | USER | Replace document |
| GET | `/documents/application/{appId}` | Authenticated | Get documents by application |
| GET | `/documents/validate/{appId}/{loanType}` | Authenticated | Validate document completeness |

### Admin Service

| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/applications` | ADMIN | View all submitted applications |
| GET | `/applications/{appId}` | ADMIN | View application details |
| GET | `/documents/{appId}` | ADMIN | View documents for application |
| PUT | `/documents/{docId}/verify` | ADMIN | Verify a document |
| PUT | `/documents/{docId}/reject` | ADMIN | Reject document with remarks |
| POST | `/applications/{appId}/approve` | ADMIN | Approve loan application |
| POST | `/applications/{appId}/reject` | ADMIN | Reject loan application |
| GET | `/dashboard` | ADMIN | Get dashboard report |

---

## 🗂️ Project Structure

```
finflow/
│
├── config-server/              # Centralized configuration
├── discovery-server/           # Eureka service registry
├── api-gateway/                # Spring Cloud Gateway
│
├── auth-service/               # Authentication & JWT
│   └── com.finflow.auth
│       ├── controller/         # REST endpoints
│       ├── service/            # Business logic
│       ├── security/           # JWT utilities & filters
│       ├── model/              # User, Role entities
│       ├── dto/                # Request/Response DTOs
│       ├── repository/         # JPA repositories
│       ├── config/             # Security & app config
│       └── exception/          # Global exception handling
│
├── application-service/        # Loan lifecycle management
│   └── com.finflow.application
│       ├── controller/         # REST endpoints
│       ├── service/            # Business logic
│       ├── client/             # Feign clients (Auth, Document)
│       ├── model/              # LoanApplication, LoanType, etc.
│       ├── dto/                # Request/Response DTOs
│       ├── security/           # JWT utilities
│       └── repository/
│
├── document-service/           # Document management
│   └── com.finflow.document
│       ├── controller/         # REST endpoints
│       ├── service/            # Business logic
│       ├── client/             # Feign clients (Application, Auth)
│       ├── model/              # Document, DocumentStatus
│       ├── enums/              # Loan-type-specific document enums
│       ├── util/               # File utilities
│       └── security/
│
├── admin-service/              # Admin workflows
│   └── com.finflow.admin
│       ├── controller/         # REST endpoints
│       ├── service/            # Business logic
│       ├── client/             # Feign clients (Application, Document, Auth)
│       ├── model/              # Decision, Report entities
│       ├── consumer/           # RabbitMQ consumers
│       └── security/
│
├── notification-service/       # Event-driven notifications
│   └── com.finflow.notification
│       ├── config/             # RabbitMQ configuration
│       ├── consumer/           # Message consumer
│       ├── model/              # NotificationHistory entity
│       ├── dto/                # NotificationEvent DTO
│       └── repository/
│
├── eureka-server/              # Service discovery
├── mysql-init/                 # Database initialization scripts
│   └── init.sql                # Creates 5 databases automatically
│
├── docker-compose.yml          # Full-stack container orchestration
├── build_all.bat               # Batch build script
├── .gitignore
└── README.md
```

---

## 🗄️ Database Architecture

Each service uses its own isolated MySQL database (Database-per-Service pattern):

| Database | Service | Key Tables |
|---|---|---|
| `auth_db` | Auth Service | `user` (id, email, password, role, terms) |
| `application_db` | Application Service | `loan_application` (id, userId, amount, tenure, loanType, status, ...) |
| `document_db` | Document Service | `document` (id, userId, applicationId, documentType, fileUrl, status) |
| `admin_db` | Admin Service | `decision` (id, appId, adminId, decision, remarks), `report` |
| `notification_db` | Notification Service | `notification_history` (id, eventType, email, message, sentAt) |

---

## ⚙️ Setup Instructions

### Option 1: Docker Compose (Recommended)

```bash
# Clone the repository
git clone https://github.com/cse-shivraj-jaiswal/sprint-loan-management-system.git
cd sprint-loan-management-system

# Build all services
./build_all.bat

# Start the entire stack
docker-compose up -d
```

This starts all services including MySQL, RabbitMQ, and Zipkin with proper health checks and startup ordering.

### Option 2: Manual Setup

#### 1️⃣ Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8.0
- RabbitMQ 3.x (optional, for notifications)

#### 2️⃣ Create Databases

```sql
CREATE DATABASE IF NOT EXISTS auth_db;
CREATE DATABASE IF NOT EXISTS application_db;
CREATE DATABASE IF NOT EXISTS document_db;
CREATE DATABASE IF NOT EXISTS admin_db;
CREATE DATABASE IF NOT EXISTS notification_db;
```

#### 3️⃣ Configure Database

Update `application.properties` or `application.yml` in each service:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/<db_name>
spring.datasource.username=root
spring.datasource.password=your_password
```

#### 4️⃣ Start Services (in order)

```bash
# 1. Config Server
cd config-server && mvn spring-boot:run

# 2. Discovery Server (Eureka)
cd discovery-server && mvn spring-boot:run

# 3. API Gateway
cd api-gateway && mvn spring-boot:run

# 4. Business Services (any order)
cd auth-service && mvn spring-boot:run
cd application-service && mvn spring-boot:run
cd document-service && mvn spring-boot:run
cd admin-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
```

#### 5️⃣ Access Points

| Service | URL |
|---|---|
| API Gateway | http://localhost:8080 |
| Eureka Dashboard | http://localhost:8761 |
| Config Server | http://localhost:8888 |
| Zipkin Dashboard | http://localhost:9411 |
| RabbitMQ Management | http://localhost:15672 |

---

## 🧪 Testing

- **Unit testing** with JUnit 5 & Mockito
- **API testing** with Postman
- **Swagger UI** available on each service at `/swagger-ui.html`

### ✅ End-to-End Flow

```
Register → Login → Create Loan → Upload Documents → Submit
    → Admin Reviews → Verify Documents → Approve/Reject → Notification Event
```

---

## 💡 Future Enhancements

- [ ] Credit score integration for automated risk assessment
- [ ] EMI calculator with amortization schedule
- [ ] Real email delivery via Spring Mail / SendGrid
- [ ] SMS notifications via Twilio
- [ ] Kafka migration for high-throughput event streaming
- [ ] AWS S3 / MinIO for cloud document storage
- [ ] Frontend dashboard with Angular / React
- [ ] CI/CD pipeline with GitHub Actions
- [ ] Centralized logging with ELK Stack (Elasticsearch, Logstash, Kibana)
- [ ] Rate limiting & circuit breaker (Resilience4j)

---

## 🎯 Key Learnings

- Microservices architecture design with Spring Cloud ecosystem
- API Gateway pattern with Spring Cloud Gateway (WebFlux)
- Service discovery & registration with Netflix Eureka
- Centralized configuration management with Config Server
- JWT-based authentication & role-based authorization
- Synchronous inter-service communication with OpenFeign
- Asynchronous event-driven messaging with RabbitMQ
- Distributed tracing with Micrometer & Zipkin
- Database-per-service pattern with MySQL
- Containerized deployment with Docker Compose
- OpenAPI documentation with SpringDoc

---

## 🧑‍💼 Author

**Shivraj Jaiswal**
Software Engineer | Java Developer

[![GitHub](https://img.shields.io/badge/GitHub-cse--shivraj--jaiswal-181717?logo=github)](https://github.com/cse-shivraj-jaiswal)

---

## ⭐ About This Project

FinFlow demonstrates a complete **production-grade backend system** with real-world distributed architecture, making it suitable for understanding enterprise-level microservices design and interview preparation. The system covers the full loan lifecycle — from user registration to loan disbursement decisions — with proper security, async messaging, and observability built in.

---
