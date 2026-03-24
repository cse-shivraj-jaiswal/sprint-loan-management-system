# 🚀 FinFlow Loan Management System

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/SpringBoot-3.x-green)
![Microservices](https://img.shields.io/badge/Architecture-Microservices-blue)
![Security](https://img.shields.io/badge/Security-JWT-red)
![Database](https://img.shields.io/badge/Database-MySQL%20%7C%20PostgreSQL-blueviolet)
![Build](https://img.shields.io/badge/Build-Maven-C71A36)
![Status](https://img.shields.io/badge/Project-Active-success)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

## 📌 Overview

**FinFlow** is a scalable, microservices-based loan management system that enables users to apply for loans, upload documents, track application status, and receive real-time notifications. Admin users can review applications, verify documents, and approve or reject loans.

This project is designed using **Spring Boot Microservices Architecture** with **API Gateway**, **JWT Security**, and **separate databases per service**.

---

## 🧠 Key Features

### 👤 Applicant Features

* User Registration & Login (JWT based authentication)
* Create and manage loan applications (Draft → Submit)
* Upload and manage documents
* Track loan application status
* Receive email notifications (submission, approval, rejection)

### 🛠️ Admin Features

* View all loan applications
* Verify uploaded documents
* Approve or reject applications
* Manage users
* Generate reports (basic)

---

## 🏗️ Architecture

This system follows a **Microservices Architecture**:

* API Gateway (Single entry point)
* Auth Service (Authentication & JWT)
* Application Service (Loan lifecycle management)
* Document Service (Document handling)
* Admin Service (Approval & admin workflows)
* Notification Service (Email notifications)

Each service is:

* Independently deployable
* Connected via API Gateway
* Using its own database

---

## 🔁 System Flow

```
User → API Gateway → Auth Service (JWT)
     → Application Service → Document Service
     → Admin Service → Notification Service → Email
```

---

## 🔄 Loan Application Lifecycle

```
Draft → Submitted → Docs Pending → Docs Verified → Under Review → Approved / Rejected → Closed
```

---

## 🧑‍💻 Tech Stack

### 🔹 Backend

* Java 17
* Spring Boot
* Spring Security (JWT)
* Spring Cloud Gateway
* Spring Data JPA (Hibernate)

### 🔹 Database

* MySQL / PostgreSQL

### 🔹 Tools

* Postman (API Testing)
* Maven
* Git & GitHub

### 🔹 Notification

* Spring Mail (Email Service)

---

## 📁 Microservices Breakdown

### 🔐 Auth Service

* User signup & login
* JWT token generation
* Role-based authentication (Applicant/Admin)

---

### 📄 Application Service

* Create loan application (Draft)
* Update application
* Submit application
* Track application status

---

### 📂 Document Service

* Upload documents
* Replace documents
* Manage document status

---

### 🧑‍💼 Admin Service

* View all applications
* Verify documents
* Approve / Reject applications
* Manage users

---

### 📩 Notification Service

* Sends email notifications:

  * Application Submitted
  * Loan Approved / Rejected

---

## 🔐 Security

* JWT-based authentication
* Role-based access control using `@PreAuthorize`
* Secured API routes via API Gateway

---

## 📡 API Endpoints (Gateway Routes)

```
/gateway/auth/*
/gateway/applications/*
/gateway/documents/*
/gateway/admin/*
```

---

## 🗂️ Project Structure

```
finflow/
│
├── api-gateway/
├── eureka-server/
├── auth-service/
├── application-service/
├── document-service/
├── admin-service/
├── notification-service/
└── README.md
```

---

## ⚙️ Setup Instructions

### 1️⃣ Clone Repository

bash
git clone https://github.com/cse-shivraj-jaiswal/sprint-loan-management-system.git


### 2️⃣ Run Services in Order

1. Eureka Server
2. API Gateway
3. All Microservices

---

### 3️⃣ Configure Database

Update `application.properties` in each service:


spring.datasource.url=jdbc:mysql://localhost:3306/db_name
spring.datasource.username=root
spring.datasource.password=your_password


### 4️⃣ Run Application

Use:


mvn spring-boot:run


---

## 🧪 Testing

* API testing using **Postman**
* Unit testing using **JUnit & Mockito**

### ✔ End-to-End Flow Tested:

```
Login → Apply Loan → Upload Docs → Submit → Admin Review → Decision → Notification
```

---

## 📅 Development Roadmap

* Day 1: Authentication & Setup
* Day 2: Application Service
* Day 3: Document Service
* Day 4: Admin Workflow
* Day 5: Notification System
* Day 6: Testing & Validation

---

## 💡 Future Enhancements

* Credit Score Integration
* EMI Calculator
* SMS Notification (Twilio)
* Kafka-based Event-Driven Architecture
* AWS S3 for Document Storage
* Dashboard Analytics

---

## 🎯 Learning Outcomes

* Microservices architecture design
* API Gateway implementation
* JWT-based authentication
* Inter-service communication
* Real-world backend system design

---

## 🧑‍💼 Author

**Shivraj Jaiswal**
Software Engineer | Java Developer

---

## ⭐ Conclusion

FinFlow demonstrates a complete **end-to-end backend system** with real-world architecture, making it suitable for **industry-level understanding and interviews**.

---
