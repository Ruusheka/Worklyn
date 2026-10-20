# Worklyn — Scalable Human Resource Management System

> **Master Implementation Specification · Architecture Document · Team Handoff Guide · API Contract · Testing Checklist**

---

## Repository Inspection Notice

| Item | Detail |
|---|---|
| **Repository URL** | [github.com/Ruusheka/Worklyn](https://github.com/Ruusheka/Worklyn) |
| **Inspection date** | 2026-10-20 |
| **Default branch** | `main` |
| **Repository size** | 0 bytes — **empty repository, no commits** |
| **Languages detected** | None |
| **Existing code** | None |

> [!IMPORTANT]
> The GitHub repository is brand new and contains **zero source files**. Every file, method, endpoint, and test documented below is a **Proposed specification** unless explicitly stated otherwise. Nothing is marked `Implemented` or `Passed` because no code exists yet. This README serves as the authoritative implementation contract that all four team members will follow.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Problem Statement and Goals](#2-problem-statement-and-goals)
3. [Features and Implementation Status](#3-features-and-implementation-status)
4. [Technology Stack and Versions](#4-technology-stack-and-versions)
5. [Architecture Overview](#5-architecture-overview)
6. [Repository Directory Tree](#6-repository-directory-tree)
7. [Four-Person Responsibility Matrix](#7-four-person-responsibility-matrix)
8. [Shared Integration Contracts](#8-shared-integration-contracts)
9. [Backend Foundation — Person 1](#9-backend-foundation--person-1)
10. [Core HR Workflows — Person 2](#10-core-hr-workflows--person-2)
11. [Distributed Systems Modules — Person 3](#11-distributed-systems-modules--person-3)
12. [HR-REWIND Recovery Engine — Person 3](#12-hr-rewind-recovery-engine--person-3)
13. [Frontend Implementation — Person 4](#13-frontend-implementation--person-4)
14. [Complete REST API Reference](#14-complete-rest-api-reference)
15. [Database Models and Indexes](#15-database-models-and-indexes)
16. [Redis and Caching Behavior](#16-redis-and-caching-behavior)
17. [Consistent Hashing and Shard Routing](#17-consistent-hashing-and-shard-routing)
18. [Infrastructure and Configuration — Person 4](#18-infrastructure-and-configuration--person-4)
19. [Environment Variable Reference](#19-environment-variable-reference)
20. [Detailed Test-Case Catalogue](#20-detailed-test-case-catalogue)
21. [Test File Implementation Contracts](#21-test-file-implementation-contracts)
22. [Local Setup and Execution](#22-local-setup-and-execution)
23. [Docker and Deployment](#23-docker-and-deployment)
24. [Postman Collection](#24-postman-collection)
25. [Git Workflow and Integration Sequence](#25-git-workflow-and-integration-sequence)
26. [Definition of Done](#26-definition-of-done)
27. [Known Limitations and Future Improvements](#27-known-limitations-and-future-improvements)

---

## 1. Project Overview

**Worklyn** is a comprehensive, scalable Human Resource Management System designed as a semester project for Software Design Engineering (SDE). It combines a full-stack HR application (employee management, attendance, leave, payroll) with system-design modules (Redis caching, distributed rate limiting, consistent hashing, shard routing, unique ID generation, short-link service, job crawler, autocomplete, and the HR-REWIND dependency-aware recovery engine).

The project is built by a **four-person team**, with each member owning a distinct vertical of the system while collaborating on shared contracts and integration points.

---

## 2. Problem Statement and Goals

### Problem

Organizations need a unified platform to manage employee lifecycle operations — onboarding, attendance tracking, leave management, and payroll processing. Existing systems often lack:
- Reliable recovery from interrupted multi-step HR workflows
- Scalable caching and rate-limiting infrastructure
- Clear separation between business logic and infrastructure concerns

### Goals

1. Deliver a working HR management system with authentication, employee profiles, attendance, leave, and payroll.
2. Implement system-design modules demonstrating distributed algorithms (consistent hashing, token-bucket rate limiting, unique ID generation).
3. Build HR-REWIND: a dependency-aware selective recovery engine for interrupted HR workflows.
4. Provide a React frontend with role-based dashboards and responsive UI.
5. Containerize all services with Docker Compose for reproducible local development.
6. Maintain comprehensive test coverage across unit, integration, security, and end-to-end layers.

---

## 3. Features and Implementation Status

| # | Feature | Status | Owner | Priority |
|---|---------|--------|-------|----------|
| F-01 | Authentication and JWT-based authorization | **Planned** | Person 1 | Critical |
| F-02 | Role-based access control (ADMIN, HR, MANAGER, EMPLOYEE) | **Planned** | Person 1 | Critical |
| F-03 | Employee profile CRUD | **Planned** | Person 1 | Critical |
| F-04 | Employee search and department filtering | **Planned** | Person 1 | High |
| F-05 | Attendance check-in / check-out | **Planned** | Person 2 | Critical |
| F-06 | Attendance history and reporting | **Planned** | Person 2 | High |
| F-07 | Leave application, approval, rejection, cancellation | **Planned** | Person 2 | Critical |
| F-08 | Leave balance tracking | **Planned** | Person 2 | Critical |
| F-09 | Payroll processing and viewing | **Planned** | Person 2 | Critical |
| F-10 | Redis-based caching | **Planned** | Person 3 | High |
| F-11 | Distributed token-bucket rate limiting | **Planned** | Person 3 | High |
| F-12 | Consistent hashing | **Planned** | Person 3 | High |
| F-13 | Shard routing and persistence boundary | **Planned** | Person 3 | High |
| F-14 | Unique ID generation (Snowflake-style) | **Planned** | Person 3 | High |
| F-15 | Short-link generation and resolution | **Planned** | Person 3 | Medium |
| F-16 | Job-listing crawler | **Planned** | Person 3 | Medium |
| F-17 | Search autocomplete | **Planned** | Person 3 | Medium |
| F-18 | HR-REWIND recovery engine | **Planned** | Person 3 | Critical |
| F-19 | React frontend with dashboards | **Planned** | Person 4 | Critical |
| F-20 | Docker Compose multi-service setup | **Planned** | Person 4 | High |
| F-21 | Nginx reverse proxy | **Planned** | Person 4 | High |
| F-22 | Postman API verification collection | **Planned** | Person 4 | High |
| F-23 | Dashboard statistics and activity summaries | **Planned** | Person 4 | Medium |
| F-24 | Notifications and audit history | **Planned** | Person 2 / 3 | Medium — Future |
| F-25 | Performance management | **Planned** | — | Future |
| F-26 | Recruitment and candidate management | **Planned** | — | Future |

---

## 4. Technology Stack and Versions

| Layer | Technology | Version (Target) | Responsibility | Status |
|---|---|---|---|---|
| **Frontend** | React | 18.x | UI, pages, forms, dashboards | Planned |
| **Frontend tooling** | Vite | 5.x | Dev server and production builds | Planned |
| **Styling** | Tailwind CSS | 3.x | Responsive layouts, consistent UI | Planned |
| **Backend language** | Java | 17 LTS | Application logic, system-design modules | Planned |
| **Backend framework** | Spring Boot | 3.2.x | REST endpoints, DI, validation, config | Planned |
| **API style** | REST | — | Frontend ↔ Backend communication | Planned |
| **Primary database** | MongoDB | 7.x | Document persistence for all HR entities | Planned |
| **Cache** | Redis | 7.x | Fast lookups, caching, rate-limit state | Planned |
| **Rate limiting** | Redis | 7.x | Shared token-bucket state | Planned |
| **Reverse proxy** | Nginx | 1.25.x | API routing, load balancing, static serving | Planned |
| **Containerization** | Docker | 24.x | Reproducible environments | Planned |
| **Multi-container** | Docker Compose | 2.x | Coordinated service startup | Planned |
| **Backend testing** | JUnit 5 + Mockito | 5.10.x / 5.x | Unit and service-level tests | Planned |
| **Integration testing** | Spring Boot Test + Testcontainers | 3.2.x / 1.19.x | Cross-boundary verification | Planned |
| **Frontend testing** | Vitest + React Testing Library | 1.x / 14.x | Component and API-client tests | Planned |
| **API verification** | Postman | Latest | Endpoint testing and workflows | Planned |
| **Version control** | Git + GitHub | — | Branching, review, CI | Active |
| **Build tool (Java)** | Maven | 3.9.x | Dependency management, build | Planned |
| **Package manager (JS)** | npm | 10.x | Frontend dependency management | Planned |

> [!NOTE]
> No technologies beyond those listed above are introduced. PostgreSQL, Kafka, Elasticsearch, and Kubernetes are **not** used in this project.


---

## 5. Architecture Overview

Worklyn follows a modular layered architecture with clear boundaries between the presentation layer, the Spring Boot application core, distributed system components, and persistent storage layers.

```mermaid
flowchart TD
    Client["Client Browser (React 18 + Vite + Tailwind)"] -->|HTTP / REST| Nginx["Nginx Reverse Proxy (:80)"]
    
    subgraph Gateway ["Edge & Gateway Layer"]
        Nginx -->|"/api/*"| SpringBoot["Spring Boot 3 Backend (:8080)"]
        Nginx -->|"/*"| Static["Vite Static Frontend Assets"]
    end

    subgraph BackendCore ["Backend Core Application (Spring Boot 3.2.x)"]
        RateLimitFilter["Distributed Token-Bucket Rate Limiter"] --> AuthFilter["JWT Authentication & Security Filter"]
        AuthFilter --> RestControllers["REST Controllers (Auth, Employee, Attendance, Leave, Payroll)"]
        
        RestControllers --> ServiceLayer["Service Layer (Business Logic & HR Rules)"]
        
        ServiceLayer --> RewindEngine["HR-REWIND Recovery Engine"]
        ServiceLayer --> DistModules["Distributed Modules (ID Gen, Consistent Hash, Crawler, Autocomplete)"]
    end

    subgraph CachingAndState ["In-Memory Data Store"]
        ServiceLayer -->|Cache Lookups / Invalidation| RedisCache["Redis 7 (Cache, Keys, TTL)"]
        RateLimitFilter -->|Atomic Token State| RedisTokens["Redis 7 (Token Bucket State)"]
    end

    subgraph PersistenceLayer ["Persistence Layer"]
        ServiceLayer --> ShardRouter["Shard Routing Layer (Consistent Hashing Abstraction)"]
        ShardRouter --> MongoStore["MongoDB 7 Database (Replica / Primary Persistence)"]
        RewindEngine --> MongoStore
    end
```

### Architectural Principles
1. **Strict Layer Boundary:** REST controllers do not execute business logic or access repositories directly; all requests flow through service layers.
2. **Stateless Backend:** Authentication is stateless via JWT tokens. Instance session state is not stored in JVM memory, enabling horizontal scaling behind Nginx.
3. **Controlled Caching:** Redis caching employs explicit key namespacing and deterministic invalidation hooks. Failures in Redis fallback gracefully to MongoDB without crashing request threads.
4. **Resilient Recovery (HR-REWIND):** Multi-step business processes (e.g., Leave Approval cascading into Attendance and Payroll adjustments) register step metadata with HR-REWIND. If any step fails, the recovery planner determines re-executable steps and compensation steps without manual database surgery.
5. **Clear Shard Boundary:** The Consistent Hashing ring is implemented as an application-level partition router to guide multi-tenant / shard assignment while MongoDB provides underlying document consistency.

---

## 6. Repository Directory Tree

The complete project structure is organized into a mono-repository containing the Spring Boot backend, the React frontend, and container orchestration files:

```
Worklyn/
├── .gitignore
├── README.md
├── docker-compose.yml
├── docker-compose.override.yml.example
├── nginx/
│   ├── Dockerfile
│   └── nginx.conf
├── backend/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   │   └── com/
│       │   │       └── worklyn/
│       │   │           ├── WorklynApplication.java
│       │   │           ├── common/
│       │   │           │   ├── config/
│       │   │           │   │   ├── MongoConfig.java
│       │   │           │   │   ├── RedisConfig.java
│       │   │           │   │   └── WebMvcConfig.java
│       │   │           │   ├── dto/
│       │   │           │   │   ├── ApiErrorResponse.java
│       │   │           │   │   └── PagedResponse.java
│       │   │           │   ├── exception/
│       │   │           │   │   ├── ApiException.java
│       │   │           │   │   ├── ConflictException.java
│       │   │           │   │   ├── GlobalExceptionHandler.java
│       │   │           │   │   ├── ResourceNotFoundException.java
│       │   │           │   │   └── UnauthorizedException.java
│       │   │           │   ├── security/
│       │   │           │   │   ├── JwtAuthenticationFilter.java
│       │   │           │   │   ├── JwtTokenProvider.java
│       │   │           │   │   ├── SecurityConfig.java
│       │   │           │   │   ├── UserPrincipal.java
│       │   │           │   └── util/
│       │   │           │       ├── DateUtils.java
│       │   │           │       └── ValidationUtils.java
│       │   │           ├── modules/
│       │   │           │   ├── auth/
│       │   │           │   │   ├── controller/
│       │   │           │   │   │   └── AuthController.java
│       │   │           │   │   ├── dto/
│       │   │           │   │   │   ├── LoginRequest.java
│       │   │           │   │   │   ├── LoginResponse.java
│       │   │           │   │   │   ├── RegisterRequest.java
│       │   │           │   │   │   └── UserResponse.java
│       │   │           │   │   ├── model/
│       │   │           │   │   │   ├── Role.java
│       │   │           │   │   │   └── User.java
│       │   │           │   │   ├── repository/
│       │   │           │   │   │   └── UserRepository.java
│       │   │           │   │   └── service/
│       │   │           │   │       ├── AuthService.java
│       │   │           │   │       └── UserDetailsServiceImpl.java
│       │   │           │   ├── employee/
│       │   │           │   │   ├── controller/
│       │   │           │   │   │   └── EmployeeController.java
│       │   │           │   │   ├── dto/
│       │   │           │   │   │   ├── CreateEmployeeRequest.java
│       │   │           │   │   │   ├── EmployeeResponse.java
│       │   │           │   │   │   └── UpdateEmployeeRequest.java
│       │   │           │   │   ├── model/
│       │   │           │   │   │   ├── Department.java
│       │   │           │   │   │   ├── Employee.java
│       │   │           │   │   │   └── EmployeeStatus.java
│       │   │           │   │   ├── repository/
│       │   │           │   │   │   └── EmployeeRepository.java
│       │   │           │   │   └── service/
│       │   │           │   │       └── EmployeeService.java
│       │   │           │   ├── attendance/
│       │   │           │   │   ├── controller/
│       │   │           │   │   │   └── AttendanceController.java
│       │   │           │   │   ├── dto/
│       │   │           │   │   │   ├── AttendanceCheckInRequest.java
│       │   │           │   │   │   ├── AttendanceCheckOutRequest.java
│       │   │           │   │   │   └── AttendanceResponse.java
│       │   │           │   │   ├── model/
│       │   │           │   │   │   ├── Attendance.java
│       │   │           │   │   │   └── AttendanceStatus.java
│       │   │           │   │   ├── repository/
│       │   │           │   │   │   └── AttendanceRepository.java
│       │   │           │   │   └── service/
│       │   │           │   │       └── AttendanceService.java
│       │   │           │   ├── leave/
│       │   │           │   │   ├── controller/
│       │   │           │   │   │   └── LeaveController.java
│       │   │           │   │   ├── dto/
│       │   │           │   │   │   ├── ApplyLeaveRequest.java
│       │   │           │   │   │   ├── LeaveBalanceResponse.java
│       │   │           │   │   │   ├── LeaveDecisionRequest.java
│       │   │           │   │   │   └── LeaveResponse.java
│       │   │           │   │   ├── model/
│       │   │           │   │   │   ├── LeaveBalance.java
│       │   │           │   │   │   ├── LeaveRequest.java
│       │   │           │   │   │   ├── LeaveStatus.java
│       │   │           │   │   │   └── LeaveType.java
│       │   │           │   │   ├── repository/
│       │   │           │   │   │   ├── LeaveBalanceRepository.java
│       │   │           │   │   │   └── LeaveRequestRepository.java
│       │   │           │   │   └── service/
│       │   │           │   │       └── LeaveService.java
│       │   │           │   ├── payroll/
│       │   │           │   │   ├── controller/
│       │   │           │   │   │   └── PayrollController.java
│       │   │           │   │   ├── dto/
│       │   │           │   │   │   ├── PayrollProcessRequest.java
│       │   │           │   │   │   ├── PayrollProcessResult.java
│       │   │           │   │   │   └── PayrollResponse.java
│       │   │           │   │   ├── model/
│       │   │           │   │   │   ├── PayrollRecord.java
│       │   │           │   │   │   └── PayrollStatus.java
│       │   │           │   │   ├── repository/
│       │   │           │   │   │   └── PayrollRecordRepository.java
│       │   │           │   │   └── service/
│       │   │           │   │       └── PayrollService.java
│       │   │           │   ├── distributed/
│       │   │           │   │   ├── cache/
│       │   │           │   │   │   └── DistributedCacheService.java
│       │   │           │   │   ├── ratelimit/
│       │   │           │   │   │   ├── RateLimitFilter.java
│       │   │           │   │   │   ├── RateLimitResult.java
│       │   │           │   │   │   └── TokenBucketService.java
│       │   │           │   │   ├── hashing/
│       │   │           │   │   │   ├── ConsistentHashRouter.java
│       │   │           │   │   │   ├── HashFunction.java
│       │   │           │   │   │   └── Murmur3HashFunction.java
│       │   │           │   │   ├── sharding/
│       │   │           │   │   │   ├── ShardPersistenceBoundary.java
│       │   │           │   │   │   └── ShardRouterService.java
│       │   │           │   │   ├── idgen/
│       │   │           │   │   │   └── SnowflakeIdGenerator.java
│       │   │           │   │   ├── shortlink/
│       │   │           │   │   │   ├── ShortLinkController.java
│       │   │           │   │   │   ├── ShortLinkModel.java
│       │   │           │   │   │   ├── ShortLinkRepository.java
│       │   │           │   │   │   └── ShortLinkService.java
│       │   │           │   │   ├── crawler/
│       │   │           │   │   │   ├── CrawledJobListing.java
│       │   │           │   │   │   ├── JobCrawlerController.java
│       │   │           │   │   │   └── JobCrawlerService.java
│       │   │           │   │   └── autocomplete/
│       │   │           │   │       ├── AutocompleteController.java
│       │   │           │   │       ├── AutocompleteService.java
│       │   │           │   │       └── SuggestionResponse.java
│       │   │           │   └── rewind/
│       │   │           │       ├── controller/
│       │   │           │       │   └── RewindRecoveryController.java
│       │   │           │       ├── dto/
│       │   │           │       │   ├── CompensationResult.java
│       │   │           │       │   ├── RecoveryPlanResponse.java
│       │   │           │       │   ├── RecoveryResultResponse.java
│       │   │           │       │   ├── StartWorkflowRequest.java
│       │   │           │       │   └── WorkflowResponse.java
│       │   │           │       ├── model/
│       │   │           │       │   ├── StepActionType.java
│       │   │           │       │   ├── StepStatus.java
│       │   │           │       │   ├── WorkflowAuditRecord.java
│       │   │           │       │   ├── WorkflowInstance.java
│       │   │           │       │   ├── WorkflowStatus.java
│       │   │           │       │   └── WorkflowStep.java
│       │   │           │       ├── planner/
│       │   │           │       │   ├── DependencyValidator.java
│       │   │           │       │   ├── InvariantChecker.java
│       │   │           │       │   └── RecoveryPlanner.java
│       │   │           │       ├── repository/
│       │   │           │       │   ├── WorkflowAuditRepository.java
│       │   │           │       │   └── WorkflowInstanceRepository.java
│       │   │           │       └── service/
│       │   │           │           ├── CompensationHandler.java
│       │   │           │           ├── RecoveryExecutor.java
│       │   │           │           └── WorkflowService.java
│       │   │           └── resources/
│       │   │               ├── application.yml
│       │   │               ├── application-dev.yml
│       │   │               └── application-prod.yml
│       └── test/
│           └── java/
│               └── com/
│                   └── worklyn/
│                       ├── auth/AuthServiceTest.java
│                       ├── employee/EmployeeServiceTest.java
│                       ├── attendance/AttendanceServiceTest.java
│                       ├── leave/LeaveServiceTest.java
│                       ├── payroll/PayrollServiceTest.java
│                       ├── distributed/
│                       │   ├── TokenBucketServiceTest.java
│                       │   ├── ConsistentHashRouterTest.java
│                       │   ├── SnowflakeIdGeneratorTest.java
│                       │   └── ShortLinkServiceTest.java
│                       └── rewind/
│                           ├── DependencyValidatorTest.java
│                           ├── InvariantCheckerTest.java
│                           └── WorkflowRecoveryServiceTest.java
├── frontend/
│   ├── package.json
│   ├── vite.config.js
│   ├── tailwind.config.js
│   ├── postcss.config.js
│   ├── index.html
│   └── src/
│       ├── main.jsx
│       ├── App.jsx
│       ├── index.css
│       ├── api/
│       │   ├── client.js
│       │   ├── authApi.js
│       │   ├── employeeApi.js
│       │   ├── attendanceApi.js
│       │   ├── leaveApi.js
│       │   ├── payrollApi.js
│       │   ├── rewindApi.js
│       │   └── systemApi.js
│       ├── context/
│       │   └── AuthContext.jsx
│       ├── components/
│       │   ├── common/
│       │   │   ├── Button.jsx
│       │   │   ├── Card.jsx
│       │   │   ├── Input.jsx
│       │   │   ├── Modal.jsx
│       │   │   ├── Navbar.jsx
│       │   │   ├── ProtectedRoute.jsx
│       │   │   ├── Sidebar.jsx
│       │   │   ├── Table.jsx
│       │   │   └── Toast.jsx
│       │   └── rewind/
│       │       ├── DependencyGraphView.jsx
│       │       ├── PlanPreviewTable.jsx
│       │       └── RecoveryExecutionModal.jsx
│       ├── pages/
│       │   ├── Login.jsx
│       │   ├── Dashboard.jsx
│       │   ├── Employees.jsx
│       │   ├── EmployeeDetails.jsx
│       │   ├── Attendance.jsx
│       │   ├── LeaveManagement.jsx
│       │   ├── Payroll.jsx
│       │   ├── RewindDashboard.jsx
│       │   └── NotFound.jsx
│       └── test/
│           ├── client.test.js
│           ├── AuthContext.test.jsx
│           └── WorkflowRecovery.test.jsx
└── postman/
    ├── Worklyn_Environment.postman_environment.json
    └── Worklyn_Master_Collection.postman_collection.json
```

---

## 7. Four-Person Responsibility Matrix

Every file in the repository has **exactly one primary owner**. Other members interact via explicit interfaces, contracts, and DTOs.

| Member | Primary Ownership | Core Focus & Deliverables | Direct Dependencies |
|---|---|---|---|
| **Person 1** | Backend Foundation, Auth, Security & Employee Module | Spring Boot core configuration, Mongo/Redis connection setup, Global Exception handling, JWT Authentication & Role RBAC, Employee CRUD & Department indexing, SecurityFilter chains | Foundation for Person 2 & 3; provides `UserPrincipal` & security contracts |
| **Person 2** | Core HR Workflows (Attendance, Leave, Payroll) | Attendance check-in/out state logic, Leave balance computation and approval workflows, Payroll processing engine with duplicate protection, HR business rules | Consumes Person 1's `Employee` entity & `UserPrincipal`; registers steps with Person 3's `HR-REWIND` |
| **Person 3** | Distributed Systems & HR-REWIND Recovery Engine | Redis Cache abstraction, Distributed Token-Bucket rate limiting, Consistent Hashing ring & Shard router, Snowflake ID generator, Short-links, Job crawler, Autocomplete, HR-REWIND engine | Consumes Person 2's business operations for compensation callbacks; protects all APIs via RateLimitFilter |
| **Person 4** | Frontend, Infrastructure, CI & End-to-End Verification | React 18 UI (Vite + Tailwind), API client, Auth Context, Complete HR & HR-REWIND pages, Docker Compose, Nginx reverse proxy, Postman master test suite, E2E verification | Consumes REST endpoints from Persons 1, 2, and 3; enforces end-to-end quality |

### Complete File-to-Owner Mapping

| File Path | Primary Owner | Module Category | Status |
|---|---|---|---|
| `backend/.../WorklynApplication.java` | Person 1 | Application Entry Point | Planned |
| `backend/.../common/config/MongoConfig.java` | Person 1 | Database Configuration | Planned |
| `backend/.../common/config/RedisConfig.java` | Person 1 (coord. P3) | Cache Infrastructure | Planned |
| `backend/.../common/config/WebMvcConfig.java` | Person 1 | Web & CORS Configuration | Planned |
| `backend/.../common/dto/ApiErrorResponse.java` | Person 1 | Shared Error Contract | Planned |
| `backend/.../common/dto/PagedResponse.java` | Person 1 | Shared Pagination Contract | Planned |
| `backend/.../common/exception/GlobalExceptionHandler.java` | Person 1 | Error Handling | Planned |
| `backend/.../common/security/JwtTokenProvider.java` | Person 1 | Security | Planned |
| `backend/.../common/security/JwtAuthenticationFilter.java` | Person 1 | Security Filter | Planned |
| `backend/.../common/security/SecurityConfig.java` | Person 1 | Security Authorization | Planned |
| `backend/.../common/security/UserPrincipal.java` | Person 1 | Security Identity | Planned |
| `backend/.../modules/auth/*` (all files) | Person 1 | Authentication Service & Controller | Planned |
| `backend/.../modules/employee/*` (all files) | Person 1 | Employee Management | Planned |
| `backend/.../modules/attendance/*` (all files) | Person 2 | Attendance Tracking | Planned |
| `backend/.../modules/leave/*` (all files) | Person 2 | Leave Management | Planned |
| `backend/.../modules/payroll/*` (all files) | Person 2 | Payroll Processing | Planned |
| `backend/.../modules/distributed/cache/*` | Person 3 | Distributed Cache | Planned |
| `backend/.../modules/distributed/ratelimit/*` | Person 3 | Token-Bucket Rate Limiter | Planned |
| `backend/.../modules/distributed/hashing/*` | Person 3 | Consistent Hashing Ring | Planned |
| `backend/.../modules/distributed/sharding/*` | Person 3 | Shard Router Abstraction | Planned |
| `backend/.../modules/distributed/idgen/*` | Person 3 | Snowflake Unique ID Generator | Planned |
| `backend/.../modules/distributed/shortlink/*` | Person 3 | Short-Link Service | Planned |
| `backend/.../modules/distributed/crawler/*` | Person 3 | Job Crawler Service | Planned |
| `backend/.../modules/distributed/autocomplete/*` | Person 3 | Autocomplete Suggestion | Planned |
| `backend/.../modules/rewind/*` (all files) | Person 3 | HR-REWIND Recovery Engine | Planned |
| `frontend/src/api/*` (all files) | Person 4 | Frontend API Client Layer | Planned |
| `frontend/src/context/AuthContext.jsx` | Person 4 | Frontend Auth State | Planned |
| `frontend/src/components/*` (all files) | Person 4 | Reusable UI & Recovery Graph | Planned |
| `frontend/src/pages/*` (all files) | Person 4 | Page Views & User Workflows | Planned |
| `docker-compose.yml` | Person 4 | Infrastructure Orchestration | Planned |
| `nginx/nginx.conf` | Person 4 | Reverse Proxy & Routing | Planned |
| `postman/*` (all files) | Person 4 | API Automation & Verification | Planned |

---

## 8. Shared Integration Contracts

To enable frictionless parallel implementation among all four developers, the following frozen technical contracts are established.

### 8.1 API Conventions & Base Routes
- **Global API Base Prefix:** `/api/v1`
- **Content-Type:** `application/json; charset=UTF-8`
- **Standard Date/Time Serialization:** ISO-8601 UTC string (`YYYY-MM-DDTHH:mm:ss.SSSZ`)
- **Calendar Date Serialization:** ISO-8601 local date (`YYYY-MM-DD`)
- **Identifiers:** Standard MongoDB hexadecimal strings (`^[0-9a-fA-F]{24}$`) or Snowflake 64-bit numerical strings for distributed entities.

### 8.2 Authentication & Authorization Scheme
- **Mechanism:** Stateless JWT in the `Authorization` header.
- **Header Format:** `Authorization: Bearer <jwt-token>`
- **Token Claims:**
  - `sub`: User ID (MongoDB ObjectId or unique user string)
  - `email`: User email address
  - `roles`: Array of string roles, e.g. `["ROLE_ADMIN"]` or `["ROLE_EMPLOYEE"]`
  - `empId`: Associated Employee Profile ID (if linked)
  - `iat`: Issue timestamp (seconds since epoch)
  - `exp`: Expiry timestamp (default: 86400 seconds = 24 hours)

### 8.3 Standard Role Taxonomy
- `ROLE_ADMIN`: Complete platform access (User registration, administrative override, system-design testing, HR-REWIND execution).
- `ROLE_HR`: Employee creation/modification, leave approvals, attendance auditing, payroll processing.
- `ROLE_MANAGER`: Subordinate employee attendance viewing, department leave approvals.
- `ROLE_EMPLOYEE`: Self employee profile view, self attendance check-in/out, self leave application and balance viewing, self payroll payslip retrieval.

### 8.4 Standard Error Response (`ApiErrorResponse`)
All endpoints return errors following this deterministic JSON schema:
```json
{
  "timestamp": "2026-10-20T13:45:00.000Z",
  "status": 409,
  "error": "Conflict",
  "code": "EMPLOYEE_CODE_EXISTS",
  "message": "Employee code 'EMP-1002' is already assigned to an existing record.",
  "path": "/api/v1/employees",
  "validationErrors": null
}
```

When HTTP 400 validation fails (`MethodArgumentNotValidException`), `validationErrors` contains field-specific feedback:
```json
{
  "timestamp": "2026-10-20T13:45:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "code": "VALIDATION_FAILED",
  "message": "Input validation failed for 2 fields.",
  "path": "/api/v1/employees",
  "validationErrors": [
    { "field": "email", "rejectedValue": "invalid-email", "message": "Must be a well-formed email address" },
    { "field": "baseSalary", "rejectedValue": -500, "message": "Base salary must be greater than zero" }
  ]
}
```

### 8.5 Standard Pagination Envelope (`PagedResponse<T>`)
All paginated endpoints return:
```json
{
  "content": [ ... ],
  "pageNumber": 0,
  "pageSize": 20,
  "totalElements": 142,
  "totalPages": 8,
  "last": false
}
```

### 8.6 Idempotency Header Contract
For financial operations (Payroll) and critical state changes (HR-REWIND recovery execution, Leave approval):
- **Header:** `X-Idempotency-Key: <UUIDv4-string>`
- **Behavior:** The backend saves processed idempotency keys in Redis (`idemp:{key}`) with a 24-hour TTL. If a duplicate request arrives while processing, HTTP 409 is returned. If retried after completion, the cached original response is returned with HTTP 200 without re-executing side effects.

---

## 9. Backend Foundation — Person 1

Person 1 is responsible for the Spring Boot application skeleton, database connection configuration, global exception handling, JWT authentication, Spring Security filter chains, role-based access control (RBAC), and the Employee Management module.

### 9.1 Application Entry Point & Core Configuration

#### File: `backend/src/main/java/com/worklyn/WorklynApplication.java`
- **Status:** Planned
- **Owner:** Person 1
- **Purpose:** Spring Boot bootstrap class.
- **Responsibilities:**
  - Launch the Spring ApplicationContext.
  - Enable Mongo Auditing via `@EnableMongoAuditing`.
  - Enable caching via `@EnableCaching`.
  - Enforce that no business logic resides in this main class.
- **Methods:**

| Method | Parameters | Return Type | Detailed Behavior | Validation & Failures |
|---|---|---|---|---|
| `main` | `String[] args` | `void` | Executes `SpringApplication.run(WorklynApplication.class, args)` | Exits with status non-zero if port binding or MongoDB/Redis connections fail on startup. |

#### File: `backend/src/main/java/com/worklyn/common/config/MongoConfig.java`
- **Status:** Planned
- **Owner:** Person 1
- **Purpose:** Configures MongoDB connection, converters, and transaction management.
- **Configuration Keys:**
  - `spring.data.mongodb.uri`: e.g. `mongodb://worklyn_user:worklyn_pass@localhost:27017/worklyn_db?authSource=admin`
- **Key Beans:**
  - `MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory)`: Enables declarative `@Transactional` boundaries.

#### File: `backend/src/main/java/com/worklyn/common/config/RedisConfig.java`
- **Status:** Planned
- **Owner:** Person 1 (Coordinated with Person 3)
- **Purpose:** Configures Lettuce connection factory, RedisTemplate with String serializers, and RedisCacheManager.
- **Key Beans:**
  - `RedisConnectionFactory redisConnectionFactory()`: Configures Lettuce with connection pooling.
  - `RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory)`: Sets `StringRedisSerializer` for keys and `GenericJackson2JsonRedisSerializer` for values.

#### File: `backend/src/main/java/com/worklyn/common/exception/GlobalExceptionHandler.java`
- **Status:** Planned
- **Owner:** Person 1
- **Purpose:** Intercepts uncaught exceptions and translates them into uniform `ApiErrorResponse` structures.
- **Methods:**

| Method | Parameters | Return Type | Detailed Behavior | HTTP Status |
|---|---|---|---|---|
| `handleValidation` | `MethodArgumentNotValidException ex, HttpServletRequest req` | `ResponseEntity<ApiErrorResponse>` | Iterates field errors, constructs `validationErrors` list | 400 Bad Request |
| `handleNotFound` | `ResourceNotFoundException ex, HttpServletRequest req` | `ResponseEntity<ApiErrorResponse>` | Returns resource missing message | 404 Not Found |
| `handleConflict` | `ConflictException ex, HttpServletRequest req` | `ResponseEntity<ApiErrorResponse>` | Returns conflict code (e.g. duplicate key) | 409 Conflict |
| `handleUnauthorized` | `UnauthorizedException ex, HttpServletRequest req` | `ResponseEntity<ApiErrorResponse>` | Returns unauthorized message | 401 Unauthorized |
| `handleAccessDenied` | `AccessDeniedException ex, HttpServletRequest req` | `ResponseEntity<ApiErrorResponse>` | Returns forbidden message | 403 Forbidden |
| `handleUnexpected` | `Exception ex, HttpServletRequest req` | `ResponseEntity<ApiErrorResponse>` | Logs error with stack trace; returns generic message without leaking internals | 500 Internal Error |

---

### 9.2 Authentication & Security Contracts

#### File: `backend/src/main/java/com/worklyn/common/security/JwtTokenProvider.java`
- **Status:** Planned
- **Owner:** Person 1
- **Responsibilities:** Signs and verifies HMAC-SHA256 JWT tokens.
- **Fields:**
  - `jwtSecret`: Injected from `app.jwt.secret` (minimum 256 bits).
  - `jwtExpirationMs`: Injected from `app.jwt.expiration-ms` (default: 86400000 = 24h).
- **Methods:**

| Method | Parameters | Return Type | Detailed Behavior | Validation & Failures |
|---|---|---|---|---|
| `generateToken` | `Authentication auth` | `String` | Extracts principal, builds claims (`sub`, `roles`, `email`, `empId`), signs token using `Keys.hmacShaKeyFor` | Throws `IllegalArgumentException` if secret < 32 bytes |
| `validateToken` | `String token` | `boolean` | Parses claims with parser builder; validates signature, expiry, and structure | Catches `MalformedJwtException`, `ExpiredJwtException`, `UnsupportedJwtException`, returns `false` |
| `getUsernameFromToken`| `String token` | `String` | Extracts subject claim (`sub`) | Throws `JwtException` if signature invalid |
| `getRolesFromToken` | `String token` | `List<String>` | Extracts custom `roles` array claim | Returns empty list if claim missing |

#### File: `backend/src/main/java/com/worklyn/common/security/JwtAuthenticationFilter.java`
- **Status:** Planned
- **Owner:** Person 1
- **Extends:** `OncePerRequestFilter`
- **Methods:**

| Method | Parameters | Return Type | Detailed Behavior | Validation & Failures |
|---|---|---|---|---|
| `doFilterInternal` | `HttpServletRequest req, HttpServletResponse res, FilterChain chain` | `void` | Extracts `Authorization` header, verifies `Bearer ` prefix, calls `jwtTokenProvider.validateToken()`, loads UserDetails, builds `UsernamePasswordAuthenticationToken`, sets in `SecurityContextHolder` | Continues filter chain even if token is absent (leaves context unauthenticated) |

#### File: `backend/src/main/java/com/worklyn/common/security/SecurityConfig.java`
- **Status:** Planned
- **Owner:** Person 1
- **Responsibilities:** Configures CORS, CSRF (disabled for stateless REST), session management (`STATELESS`), and URL authorization rules.
- **Authorization Rules:**
  - `/api/v1/auth/**`: `permitAll()`
  - `/api/v1/shortlinks/{code}` (GET redirect): `permitAll()`
  - `/api/v1/employees/**`: `hasAnyRole('ADMIN', 'HR', 'MANAGER')` (except self-profile: `hasAnyRole('EMPLOYEE', 'ADMIN', 'HR', 'MANAGER')`)
  - `/api/v1/attendance/check-in`, `/check-out`: `isAuthenticated()`
  - `/api/v1/leave/apply`, `/leave/balance`: `isAuthenticated()`
  - `/api/v1/leave/{id}/approve`, `/reject`: `hasAnyRole('ADMIN', 'HR', 'MANAGER')`
  - `/api/v1/payroll/process`: `hasAnyRole('ADMIN', 'HR')`
  - `/api/v1/rewind/**`: `hasRole('ADMIN')`
  - All other `/api/v1/**`: `authenticated()`

---

### 9.3 Authentication Service & Controller

#### File: `backend/src/main/java/com/worklyn/modules/auth/service/AuthService.java`
- **Status:** Planned
- **Owner:** Person 1
- **Dependencies:** `UserRepository`, `PasswordEncoder`, `JwtTokenProvider`, `AuthenticationManager`
- **Methods:**

| Method | Parameters | Return Type | Detailed Behavior | Validation & Failure Cases |
|---|---|---|---|---|
| `login` | `LoginRequest request` | `LoginResponse` | Authenticates via `AuthenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password))`. If valid, generates JWT token, returns token + user metadata | Throws `BadCredentialsException` (translated to HTTP 401) on invalid password; throws `ResourceNotFoundException` if email not found |
| `register` | `RegisterRequest request, String actorRole` | `UserResponse` | Verifies actor has `ROLE_ADMIN` (public registration is disabled for security). Checks email uniqueness. Encodes password using BCrypt. Persists new `User`. | Throws `AccessDeniedException` if actor is not ADMIN. Throws `ConflictException` (HTTP 409) if email is already taken. |
| `getCurrentUser` | `UserPrincipal principal` | `UserResponse` | Queries `UserRepository` by principal ID. Returns sanitized profile without password hash. | Throws `ResourceNotFoundException` if user was deleted. |

#### File: `backend/src/main/java/com/worklyn/modules/auth/controller/AuthController.java`
- **Status:** Planned
- **Owner:** Person 1
- **Routes:**
  - `POST /api/v1/auth/login`: Public login
  - `POST /api/v1/auth/register`: Admin-only user provisioning
  - `GET /api/v1/auth/me`: Authenticated caller profile

---

### 9.4 Employee Profile Management

#### File: `backend/src/main/java/com/worklyn/modules/employee/model/Employee.java`
- **Status:** Planned
- **Owner:** Person 1
- **Collection:** `@Document(collection = "employees")`
- **Fields:**
  - `id`: `String` (MongoDB ObjectId)
  - `employeeCode`: `String` (Unique business key, e.g. `EMP-1001`, Indexed)
  - `userId`: `String` (Link to auth User document, Unique, Indexed)
  - `firstName`: `String` (Required, 2-50 chars)
  - `lastName`: `String` (Required, 2-50 chars)
  - `email`: `String` (Unique, Indexed)
  - `department`: `Department` (Enum: `ENGINEERING`, `HUMAN_RESOURCES`, `FINANCE`, `SALES`, `MARKETING`, `OPERATIONS`)
  - `designation`: `String` (Required, e.g. "Senior Software Engineer")
  - `dateOfJoining`: `LocalDate` (Required)
  - `status`: `EmployeeStatus` (Enum: `ACTIVE`, `PROBATION`, `SUSPENDED`, `TERMINATED`)
  - `baseSalary`: `BigDecimal` (Positive, precision 12, scale 2)
  - `managerId`: `String` (Nullable, refers to another Employee `id`)
  - `createdAt`: `Instant` (`@CreatedDate`)
  - `updatedAt`: `Instant` (`@LastModifiedDate`)

#### File: `backend/src/main/java/com/worklyn/modules/employee/repository/EmployeeRepository.java`
- **Status:** Planned
- **Owner:** Person 1
- **Extends:** `MongoRepository<Employee, String>`
- **Query Methods:**
  - `Optional<Employee> findByEmployeeCode(String employeeCode)`
  - `Optional<Employee> findByEmail(String email)`
  - `Optional<Employee> findByUserId(String userId)`
  - `boolean existsByEmployeeCode(String employeeCode)`
  - `boolean existsByEmail(String email)`
  - `Page<Employee> findByDepartment(Department department, Pageable pageable)`
  - `Page<Employee> findByStatus(EmployeeStatus status, Pageable pageable)`
  - `@Query("{'$or': [{'firstName': {$regex: ?0, $options: 'i'}}, {'lastName': {$regex: ?0, $options: 'i'}}, {'employeeCode': {$regex: ?0, $options: 'i'}}]}") Page<Employee> searchEmployees(String regex, Pageable pageable)`

#### File: `backend/src/main/java/com/worklyn/modules/employee/service/EmployeeService.java`
- **Status:** Planned
- **Owner:** Person 1
- **Methods:**

| Method | Parameters | Return Type | Detailed Behavior | Validation & Failures |
|---|---|---|---|---|
| `createEmployee` | `CreateEmployeeRequest req, UserPrincipal actor` | `EmployeeResponse` | Verifies `actor` has HR or ADMIN role. Enforces uniqueness of `employeeCode` and `email`. Builds and saves `Employee`. Automatically initializes default `LeaveBalance` (calls LeaveService hook). Evicts employee search caches. | Throws `ConflictException` on duplicate code or email. Throws `MethodArgumentNotValidException` on negative salary or blank names. |
| `getEmployeeById` | `String id, UserPrincipal actor` | `EmployeeResponse` | Checks authorization: EMPLOYEE role can only view self record; HR/ADMIN/MANAGER can view any. Fetches from Redis cache `emp:{id}` first; on miss, queries Mongo and caches with 1h TTL. | Throws `ResourceNotFoundException` if ID not found. Throws `AccessDeniedException` if employee queries another without permission. |
| `updateEmployee` | `String id, UpdateEmployeeRequest req, UserPrincipal actor` | `EmployeeResponse` | Authorizes caller (HR or ADMIN). Validates manager ID if provided. Updates allowed fields (designation, department, baseSalary, managerId). Saves record and evicts Redis keys `emp:{id}`. | Throws `ResourceNotFoundException` if ID missing. Throws `BadRequestException` if managerId == id (self-manager cycle). |
| `updateStatus` | `String id, EmployeeStatus newStatus, UserPrincipal actor` | `EmployeeResponse` | Enforces state transitions: `ACTIVE` -> `SUSPENDED` or `TERMINATED`; `PROBATION` -> `ACTIVE` or `TERMINATED`. Cannot reactivate `TERMINATED` without ADMIN role. | Throws `ConflictException` on invalid status transition. |
| `searchEmployees` | `String query, Department dept, EmployeeStatus status, Pageable pageable` | `PagedResponse<EmployeeResponse>` | Sanitizes search query string. Applies optional department and status filters. Returns paginated DTO list. | Empty query returns all with pagination. |

---

## 10. Core HR Workflows — Person 2

Person 2 is responsible for business logic, persistence, and validation governing Attendance, Leave Management, and Payroll.

### 10.1 Attendance Module

#### File: `backend/src/main/java/com/worklyn/modules/attendance/model/Attendance.java`
- **Status:** Planned
- **Owner:** Person 2
- **Collection:** `@Document(collection = "attendance")`
- **Compound Index:** `{"employeeId": 1, "attendanceDate": 1}`, Unique = true
- **Fields:**
  - `id`: `String` (MongoDB ObjectId)
  - `employeeId`: `String` (Required, Indexed)
  - `attendanceDate`: `LocalDate` (Required, date of check-in)
  - `checkInTime`: `Instant` (Required timestamp)
  - `checkOutTime`: `Instant` (Nullable until checked out)
  - `durationMinutes`: `Long` (Computed upon check-out: `checkOutTime - checkInTime`)
  - `status`: `AttendanceStatus` (Enum: `PRESENT`, `HALF_DAY`, `LATE`, `ABSENT`)
  - `ipAddress`: `String` (Audit IP address)
  - `idempotencyKey`: `String` (Unique check-in key to prevent duplicate clicks)

#### File: `backend/src/main/java/com/worklyn/modules/attendance/service/AttendanceService.java`
- **Status:** Planned
- **Owner:** Person 2
- **Methods:**

| Method | Parameters | Return Type | Detailed Behavior | Validation & Failures |
|---|---|---|---|---|
| `checkIn` | `String employeeId, Instant timestamp, String clientIp, String idempotencyKey` | `AttendanceResponse` | 1. Determines `LocalDate` in corporate timezone.<br>2. Verifies employee exists and status is `ACTIVE`.<br>3. Checks if attendance record already exists for (employeeId, today). If exists, rejects duplicate.<br>4. Computes status: if `timestamp > 09:30 AM` local time, mark `LATE`, else `PRESENT`.<br>5. Saves record. | Throws `ConflictException` ("Already checked in for date: {today}") if record exists.<br>Throws `BadRequestException` if employee is not ACTIVE.<br>Throws `ConflictException` on duplicate idempotencyKey. |
| `checkOut` | `String employeeId, Instant timestamp, String idempotencyKey` | `AttendanceResponse` | 1. Queries record for (employeeId, today).<br>2. Verifies `checkOutTime` is currently null.<br>3. Verifies `timestamp >= checkInTime`.<br>4. Calculates `durationMinutes`. If `durationMinutes < 240` (4h), status updated to `HALF_DAY`.<br>5. Saves updated record. | Throws `ResourceNotFoundException` ("No check-in record found for today").<br>Throws `ConflictException` ("Employee already checked out at {time}").<br>Throws `BadRequestException` if check-out time is earlier than check-in time. |
| `getAttendanceHistory` | `String employeeId, LocalDate start, LocalDate end, Pageable pageable, UserPrincipal actor` | `PagedResponse<AttendanceResponse>` | Authorizes actor (EMPLOYEE can only view own history; MANAGER/HR/ADMIN can view any). Enforces `start <= end`. Queries repository by date range. | Throws `BadRequestException` if date range exceeds 366 days or start > end.<br>Throws `AccessDeniedException` if unauthorized. |

---

### 10.2 Leave Management Module

#### File: `backend/src/main/java/com/worklyn/modules/leave/model/LeaveRequest.java`
- **Status:** Planned
- **Owner:** Person 2
- **Collection:** `@Document(collection = "leave_requests")`
- **Fields:**
  - `id`: `String` (MongoDB ObjectId)
  - `employeeId`: `String` (Required, Indexed)
  - `leaveType`: `LeaveType` (Enum: `CASUAL`, `SICK`, `EARNED`, `MATERNITY`, `PATERNITY`, `UNPAID`)
  - `startDate`: `LocalDate` (Required)
  - `endDate`: `LocalDate` (Required)
  - `totalDays`: `Integer` (Calculated inclusive business days)
  - `reason`: `String` (Required, 5-255 chars)
  - `status`: `LeaveStatus` (Enum: `PENDING`, `APPROVED`, `REJECTED`, `CANCELLED`)
  - `approverId`: `String` (User ID who approved/rejected)
  - `decisionRemarks`: `String` (Optional comments)
  - `decisionDate`: `Instant`
  - `workflowId`: `String` (Link to HR-REWIND workflow instance)
  - `createdAt`: `Instant`

#### File: `backend/src/main/java/com/worklyn/modules/leave/model/LeaveBalance.java`
- **Status:** Planned
- **Owner:** Person 2
- **Collection:** `@Document(collection = "leave_balances")`
- **Compound Index:** `{"employeeId": 1, "year": 1}`, Unique = true
- **Fields:**
  - `id`: `String`
  - `employeeId`: `String` (Required)
  - `year`: `Integer` (e.g. 2026)
  - `casualLeaveAvailable`: `Integer` (default: 12)
  - `sickLeaveAvailable`: `Integer` (default: 10)
  - `earnedLeaveAvailable`: `Integer` (default: 15)
  - `casualLeaveUsed`: `Integer` (default: 0)
  - `sickLeaveUsed`: `Integer` (default: 0)
  - `earnedLeaveUsed`: `Integer` (default: 0)

#### File: `backend/src/main/java/com/worklyn/modules/leave/service/LeaveService.java`
- **Status:** Planned
- **Owner:** Person 2
- **State Transition Machine:**
  - `PENDING` -> `APPROVED` (by Manager/HR)
  - `PENDING` -> `REJECTED` (by Manager/HR)
  - `PENDING` -> `CANCELLED` (by Employee)
  - `APPROVED` -> `CANCELLED` (by HR only; requires leave balance credit compensation)
- **Methods:**

| Method | Parameters | Return Type | Detailed Behavior | Validation & Failures |
|---|---|---|---|---|
| `applyLeave` | `ApplyLeaveRequest req, String employeeId` | `LeaveResponse` | 1. Validates `startDate <= endDate` and `startDate >= today`.<br>2. Checks for existing overlapping leave requests with status `PENDING` or `APPROVED`.<br>3. Computes business days (`totalDays`).<br>4. Checks if `LeaveBalance` has sufficient days for requested `leaveType`.<br>5. Creates `LeaveRequest` in `PENDING` status. | Throws `BadRequestException` if `startDate > endDate` or date is in past.<br>Throws `ConflictException` if overlapping request exists.<br>Throws `ConflictException` ("Insufficient leave balance: requested X, available Y"). |
| `approveLeave` | `String leaveId, String approverId, String remarks, String idempotencyKey` | `LeaveResponse` | 1. Fetches `LeaveRequest`; verifies status is `PENDING`.<br>2. Verifies approver is manager or HR.<br>3. Atomically deducts `totalDays` from `LeaveBalance`.<br>4. Updates `LeaveRequest.status = APPROVED`.<br>5. Registers workflow step in `HR-REWIND` engine.<br>6. Automatically marks `Attendance` records for the leave range as `ON_LEAVE`. | Throws `ResourceNotFoundException` if leaveId missing.<br>Throws `ConflictException` if status is not `PENDING`.<br>Throws `AccessDeniedException` if approver cannot approve employee's leaves. |
| `rejectLeave` | `String leaveId, String approverId, String remarks` | `LeaveResponse` | 1. Fetches `LeaveRequest`; verifies status is `PENDING`.<br>2. Requires non-blank remarks.<br>3. Sets status `REJECTED`, sets `approverId` and `decisionDate`. | Throws `ConflictException` if already processed.<br>Throws `BadRequestException` if remarks are blank. |
| `cancelLeave` | `String leaveId, UserPrincipal actor` | `LeaveResponse` | If `PENDING`, employee can cancel directly. If `APPROVED`, only HR/ADMIN can cancel; credits deducted days back to `LeaveBalance` and removes `ON_LEAVE` attendance markers. | Throws `AccessDeniedException` if employee attempts to cancel an already `APPROVED` leave without HR role.<br>Throws `ConflictException` if already `CANCELLED` or `REJECTED`. |

---

### 10.3 Payroll Module

#### File: `backend/src/main/java/com/worklyn/modules/payroll/model/PayrollRecord.java`
- **Status:** Planned
- **Owner:** Person 2
- **Collection:** `@Document(collection = "payroll_records")`
- **Compound Index:** `{"employeeId": 1, "payrollPeriod": 1}`, Unique = true
- **Fields:**
  - `id`: `String` (MongoDB ObjectId)
  - `employeeId`: `String` (Required, Indexed)
  - `payrollPeriod`: `String` (Format: `YYYY-MM`, e.g. "2026-10", Required, Indexed)
  - `baseSalary`: `BigDecimal` (From Employee profile)
  - `workingDays`: `Integer` (Total days in period, e.g. 22)
  - `presentDays`: `Integer` (From Attendance records)
  - `paidLeaveDays`: `Integer` (From approved Leave records)
  - `unpaidLeaveDays`: `Integer` (Absent / unpaid leave)
  - `grossPay`: `BigDecimal` (`(baseSalary / workingDays) * (presentDays + paidLeaveDays)`)
  - `taxDeduction`: `BigDecimal` (Calculated based on standard tax bracket)
  - `netPay`: `BigDecimal` (`grossPay - taxDeduction`)
  - `status`: `PayrollStatus` (Enum: `DRAFT`, `PROCESSED`, `FINALIZED`, `VOIDED`)
  - `processedAt`: `Instant`
  - `processedBy`: `String` (HR User ID)
  - `idempotencyKey`: `String` (Unique key per period run)

#### File: `backend/src/main/java/com/worklyn/modules/payroll/service/PayrollService.java`
- **Status:** Planned
- **Owner:** Person 2
- **Methods:**

| Method | Parameters | Return Type | Detailed Behavior | Validation & Failures |
|---|---|---|---|---|
| `processPayrollForPeriod` | `String period, String actorId, String idempotencyKey` | `PayrollProcessResult` | 1. Validates period format (`^\d{4}-(0[1-9] or 1[0-2])$`).<br>2. Verifies period is not already `FINALIZED`.<br>3. Checks Redis idempotency key: if exists and finished, returns previous result.<br>4. Fetches all `ACTIVE` employees.<br>5. For each employee, queries attendance & approved leaves in period.<br>6. Calculates prorated gross pay, tax deductions, and net pay.<br>7. Saves `PayrollRecord` documents in batch.<br>8. Saves idempotency key in Redis with 24h TTL. | Throws `BadRequestException` on invalid period string.<br>Throws `ConflictException` if period is `FINALIZED` and locked.<br>Handles partial failure by rolling back through transactional boundary or registering failure with HR-REWIND. |
| `getEmployeePayslip` | `String employeeId, String period, UserPrincipal actor` | `PayrollResponse` | Authorizes caller (EMPLOYEE can only view own payslip; HR/ADMIN can view any). Returns detailed salary breakdown DTO. | Throws `ResourceNotFoundException` if no payroll generated for (employeeId, period).<br>Throws `AccessDeniedException` if unauthorized. |
| `finalizePeriod` | `String period, UserPrincipal actor` | `void` | Changes all records for `period` from `PROCESSED` to `FINALIZED`. Locks the period against further modifications. | Throws `ConflictException` if period has records in `DRAFT` or already `FINALIZED`. |

---

## 11. Distributed Systems Modules — Person 3

Person 3 is responsible for designing and implementing high-throughput distributed system components, caching layers, partition routers, and algorithms.

### 11.1 Redis Cache Abstraction

#### File: `backend/src/main/java/com/worklyn/modules/distributed/cache/DistributedCacheService.java`
- **Status:** Planned
- **Owner:** Person 3
- **Responsibilities:** Encapsulates Redis caching operations with type safety, standard TTL defaults, exception suppression (cache failure should not break critical database queries), and prefix invalidation.
- **Methods:**

| Method | Parameters | Return Type | Detailed Behavior | Validation & Failures |
|---|---|---|---|---|
| `get` | `String key, Class<T> clazz` | `Optional<T>` | Fetches JSON string from Redis, deserializes to `T`. If Redis is down, catches Lettuce exception, logs warning, returns `Optional.empty()` (fallback to DB). | Returns empty if key absent or unparseable. |
| `put` | `String key, Object value, Duration ttl` | `void` | Serializes value to JSON, stores in Redis with explicit TTL (`SETEX key seconds value`). | Catches Redis connection errors, logs warning, does not throw. |
| `evict` | `String key` | `void` | Deletes key immediately via `DEL key`. | Safe no-op if key does not exist. |
| `evictByPrefix` | `String prefix` | `void` | Executes `SCAN` in batches of 100 with pattern `prefix:*` and executes pipelined `UNLINK`. Never calls dangerous blocking `KEYS *` command. | Safe against large key spaces. |

---

### 11.2 Distributed Token-Bucket Rate Limiter

#### File: `backend/src/main/java/com/worklyn/modules/distributed/ratelimit/TokenBucketService.java`
- **Status:** Planned
- **Owner:** Person 3
- **Algorithm:** Atomic Token Bucket evaluated via an embedded Redis Lua script.
  - State per bucket stored as Redis Hash (`tokens`: floating point, `lastRefreshed`: unix timestamp ms).
  - Tokens refill continuously: $\text{tokens} = \min(\text{capacity}, \text{current} + (\Delta t \times \text{refillRate}))$.
- **Redis Lua Script:**
```lua
local key = KEYS[1]
local capacity = tonumber(ARGV[1])
local refillRate = tonumber(ARGV[2])
local requested = tonumber(ARGV[3])
local now = tonumber(ARGV[4])

local data = redis.call('HMGET', key, 'tokens', 'lastRefreshed')
local tokens = tonumber(data[1])
local lastRefreshed = tonumber(data[2])

if not tokens then
    tokens = capacity
    lastRefreshed = now
else
    local delta = math.max(0, now - lastRefreshed) / 1000.0
    tokens = math.min(capacity, tokens + (delta * refillRate))
    lastRefreshed = now
end

if tokens >= requested then
    tokens = tokens - requested
    redis.call('HMSET', key, 'tokens', tokens, 'lastRefreshed', lastRefreshed)
    redis.call('EXPIRE', key, math.ceil(capacity / refillRate) * 2)
    return {1, tokens, 0}
else
    local waitSeconds = (requested - tokens) / refillRate
    return {0, tokens, math.ceil(waitSeconds)}
end
```
- **Methods:**

| Method | Parameters | Return Type | Detailed Behavior | Validation & Failures |
|---|---|---|---|---|
| `tryConsume` | `String bucketKey, long capacity, double refillRate, long tokens` | `RateLimitResult` | Executes Lua script atomically. Returns whether request is allowed, remaining tokens, and retry-after seconds if rejected. | If Redis is unavailable, policy is **fail-open** with alert logging so legitimate traffic is not blocked. |

#### File: `backend/src/main/java/com/worklyn/modules/distributed/ratelimit/RateLimitFilter.java`
- **Status:** Planned
- **Owner:** Person 3
- **Extends:** `OncePerRequestFilter`
- **Tiers & Policies:**
  - **Authenticated API Calls:** Keyed by `rl:user:{userId}`, Capacity = 100 tokens, Refill = 10 tokens/sec.
  - **Public / Unauthenticated Calls:** Keyed by client IP (`rl:ip:{ipAddress}`), Capacity = 20 tokens, Refill = 2 tokens/sec.
  - **Expensive Endpoints (e.g. Payroll Process, HR-REWIND Recover):** Keyed by `rl:expensive:{userId}`, Capacity = 5 tokens, Refill = 0.2 tokens/sec (1 every 5s).
- **HTTP 429 Response Format:**
```json
{
  "timestamp": "2026-10-20T13:46:00.000Z",
  "status": 429,
  "error": "Too Many Requests",
  "code": "RATE_LIMIT_EXCEEDED",
  "message": "Token bucket exhausted. Please wait 4 seconds before retrying.",
  "path": "/api/v1/payroll/process"
}
```
- **Response Headers:** `X-RateLimit-Remaining`, `X-RateLimit-Retry-After-Seconds`.

---

### 11.3 Consistent Hashing Ring

#### File: `backend/src/main/java/com/worklyn/modules/distributed/hashing/ConsistentHashRouter.java`
- **Status:** Planned
- **Owner:** Person 3
- **Algorithm:** Virtual-Node Consistent Hash Ring backed by a `ConcurrentSkipListMap<Long, String>`.
  - Number of virtual nodes per physical shard: default 150 (balances key distribution across shards within < 5% variance).
  - Deterministic 64-bit Murmur3 hash function.
- **Methods:**

| Method | Parameters | Return Type | Detailed Behavior | Validation & Failures |
|---|---|---|---|---|
| `addShard` | `String shardId` | `void` | For `i = 0` to `virtualNodeCount - 1`, computes `hash(shardId + "#vn#" + i)` and inserts entry `(hash, shardId)` into ring. Thread-safe. | Throws `IllegalArgumentException` if shardId is blank or already registered. |
| `removeShard` | `String shardId` | `void` | Iterates and removes all virtual nodes matching `shardId`. | Throws `IllegalStateException` if removing last shard would leave ring empty. |
| `getShardForKey` | `String key` | `String` | Computes `hash(key)`. Performs ceiling lookup on `ConcurrentSkipListMap`. If ceiling is null (wraparound), takes `ring.firstEntry().getValue()`. | Throws `IllegalStateException` if ring is empty. Returns deterministic shard string. |
| `getRingSnapshot` | `None` | `Map<Long, String>` | Returns unmodifiable snapshot of current ring state for diagnostics and UI visualization. | None |

---

### 11.4 Shard Routing & Persistence Boundary

#### File: `backend/src/main/java/com/worklyn/modules/distributed/sharding/ShardRouterService.java`
- **Status:** Planned
- **Owner:** Person 3
- **Purpose:** Bridges application logical partition logic to storage boundaries.
- **Key Boundary Principle:**
  > [!NOTE]
  > The `ConsistentHashRouter` operates at the application level to determine logical partition assignment (e.g. assigning an enterprise customer or employee cluster to a specific tenant shard key). MongoDB stores documents in collections indexed by this shard key. This application router does NOT pretend to be MongoDB physical cluster sharding; it serves as the partition gateway for multi-tenant and archival routing.

---

### 11.5 Snowflake Unique ID Generator

#### File: `backend/src/main/java/com/worklyn/modules/distributed/idgen/SnowflakeIdGenerator.java`
- **Status:** Planned
- **Owner:** Person 3
- **Bit Allocation (64-bit Long):**
  - Bit 63: 1 unused sign bit (always 0)
  - Bits 62-22 (41 bits): Timestamp in milliseconds since custom Worklyn epoch (`1767225600000L` = 2026-01-01T00:00:00Z). Supports 69 years.
  - Bits 21-17 (5 bits): Datacenter ID (0 - 31)
  - Bits 16-12 (5 bits): Worker / Pod Machine ID (0 - 31)
  - Bits 11-0 (12 bits): Sequence counter (0 - 4095 per millisecond)
- **Concurrency & Rollback Safeguards:**
  - Synchronized block on sequence increment.
  - If sequence exceeds 4095 within same millisecond, spins until next millisecond.
  - **Clock Rollback Protection:** If current timestamp `< lastTimestamp`, if drift is `< 10ms`, sleeps drift duration; if drift `> 10ms`, throws `IllegalStateException("NTP Clock moved backwards by X ms; refusing to generate IDs")`.
- **Methods:**

| Method | Parameters | Return Type | Detailed Behavior | Validation & Failures |
|---|---|---|---|---|
| `nextId` | `None` | `long` | Generates next monotonic 64-bit ID | Thread-safe, generates up to 4,096,000 unique IDs/sec per instance |
| `nextIdString` | `None` | `String` | Returns `Long.toString(nextId())` | Suitable for JSON serialization without 53-bit JS float truncation |

---

### 11.6 Short-Link Service

#### File: `backend/src/main/java/com/worklyn/modules/distributed/shortlink/ShortLinkService.java`
- **Status:** Planned
- **Owner:** Person 3
- **Collection:** `@Document(collection = "short_links")`
- **Short-Code Algorithm:** Base62 encoding (`[0-9a-zA-Z]`) of Snowflake ID or Murmur3 hash, truncated to 7 alphanumeric characters ($62^7 \approx 3.52 \text{ trillion}$ combinations).
- **Methods:**

| Method | Parameters | Return Type | Detailed Behavior | Validation & Failures |
|---|---|---|---|---|
| `createShortLink` | `String destinationUrl, Duration ttl, String creatorId` | `ShortLinkResponse` | 1. Validates destination URL (must be valid HTTP/HTTPS; blocks localhost/internal SSRF targets).<br>2. Generates 7-char code.<br>3. Checks uniqueness; handles collision by rehashing with salt.<br>4. Saves to MongoDB and caches in Redis (`sl:{code}` -> destinationUrl) with TTL.<br>5. Returns shortened URL (`http://worklyn.io/s/{code}`). | Throws `BadRequestException` on invalid or internal SSRF URL.<br>Retries up to 3 times on collision. |
| `resolveShortLink` | `String code` | `String` | Looks up code in Redis `sl:{code}`. On miss, queries MongoDB. If found and not expired, updates click counter asynchronously and returns destination URL. | Throws `ResourceNotFoundException` if code is invalid or expired. |

---

### 11.7 Controlled Job Crawler

#### File: `backend/src/main/java/com/worklyn/modules/distributed/crawler/JobCrawlerService.java`
- **Status:** Planned
- **Owner:** Person 3
- **Purpose:** Extracts public job postings from approved corporate careers domains to support HR salary benchmarking and recruitment intake.
- **Safety & Control Rules:**
  - **Whitelisted Domains Only:** Configured in `app.crawler.allowed-domains` (e.g. `careers.example.com`). All other hosts rejected with HTTP 403.
  - **Rate Limiting:** Maximum 1 request per 2 seconds per target host.
  - **Timeouts:** 3000ms connect timeout, 5000ms read timeout.
  - **Content Limit:** Max 2MB HTML payload.
- **Methods:**

| Method | Parameters | Return Type | Detailed Behavior | Validation & Failures |
|---|---|---|---|---|
| `crawlSource` | `URI sourceUri` | `CrawlResult` | Validates host against whitelist. Fetches HTML with polite user-agent `WorklynBot/1.0`. Parses listings using Jsoup. Deduplicates listings by title + location hash. | Throws `SecurityException` on non-whitelisted URL.<br>Throws `HttpTimeoutException` on slow target. |

---

### 11.8 Autocomplete Search Engine

#### File: `backend/src/main/java/com/worklyn/modules/distributed/autocomplete/AutocompleteService.java`
- **Status:** Planned
- **Owner:** Person 3
- **Algorithm:** Prefix query backed by an in-memory Radix Trie and Redis Sorted Sets (`ZADD autocomplete:employees 0 "john doe:EMP-101"`).
- **Methods:**

| Method | Parameters | Return Type | Detailed Behavior | Validation & Failures |
|---|---|---|---|---|
| `suggestEmployees` | `String query, int limit, UserPrincipal actor` | `List<SuggestionResponse>` | 1. Trims query, validates `length >= 2`.<br>2. Executes Redis `ZRANGEBYLEX` with `[query` to `[query\xff` limited to `limit` entries.<br>3. Returns ranked suggestions with Employee Name, Code, and Department. | Returns empty list if query < 2 chars.<br>Enforces maximum `limit <= 50`. |

---

## 12. HR-REWIND Recovery Engine — Person 3

**HR-REWIND** is a dependency-aware selective recovery engine for complex, multi-step HR workflows. 

### 12.1 The Core Problem & Design Solution
When a multi-step HR operation (such as *Leave Approval Cascading Workflow* or *Monthly Payroll Finalization*) fails midway:
- A naive database rollback (`@Transactional`) is insufficient because external systems (notifications, payment gateways) cannot be un-written, and steps may span asynchronous boundaries.
- Indiscriminate rollback deletes or reverses independent, successful steps, corrupting valid data.
- **HR-REWIND's Solution:** Models workflows as Directed Acyclic Graphs (DAGs) of steps. When a step fails, the engine analyzes dependencies:
  1. Identifies **eligible re-executions** (steps that failed transiently and are safe to replay).
  2. Identifies **required compensations** (steps whose subsequent dependencies failed and whose side effects must be explicitly reversed).
  3. Preserves **unrelated successful steps** untouched.
  4. Identifies **manual review steps** where non-reversible business actions occurred.

```mermaid
stateDiagram-v2
    [*] --> PENDING
    PENDING --> RUNNING: startWorkflow()
    RUNNING --> COMPLETED: all steps succeed
    RUNNING --> FAILED: step fails
    FAILED --> PREVIEWING: previewRecovery()
    PREVIEWING --> RECOVERING: recover() confirmed
    RECOVERING --> COMPLETED: replay/compensation succeeds
    RECOVERING --> REQUIRES_MANUAL_REVIEW: non-compensable failure
```

---

### 12.2 Workflow Data Models

#### File: `backend/src/main/java/com/worklyn/modules/rewind/model/WorkflowInstance.java`
- **Status:** Planned
- **Owner:** Person 3
- **Collection:** `@Document(collection = "rewind_workflows")`
- **Fields:**
  - `workflowId`: `String` (Snowflake ID, Indexed)
  - `workflowType`: `String` (e.g. `LEAVE_APPROVAL_CASCADE`, `PAYROLL_DISBURSEMENT`)
  - `initiatorId`: `String` (User who started the workflow)
  - `status`: `WorkflowStatus` (`PENDING`, `RUNNING`, `COMPLETED`, `FAILED`, `RECOVERING`, `REQUIRES_MANUAL_REVIEW`)
  - `steps`: `List<WorkflowStep>` (Ordered collection of steps with dependency pointers)
  - `contextData`: `Map<String, Object>` (Input payloads, e.g. `leaveId`, `employeeId`, `days`)
  - `createdAt`: `Instant`
  - `updatedAt`: `Instant`

#### File: `backend/src/main/java/com/worklyn/modules/rewind/model/WorkflowStep.java`
- **Status:** Planned
- **Owner:** Person 3
- **Fields:**
  - `stepId`: `String` (e.g. `STEP_DEDUCT_LEAVE_BALANCE`, `STEP_MARK_ATTENDANCE`, `STEP_ADJUST_PAYROLL`)
  - `actionType`: `StepActionType` (`DB_MUTATION`, `BALANCE_DEDUCTION`, `STATUS_UPDATE`, `EXTERNAL_NOTIFICATION`)
  - `dependsOnStepIds`: `List<String>` (Prerequisite step IDs in DAG)
  - `status`: `StepStatus` (`PENDING`, `RUNNING`, `COMPLETED`, `FAILED`, `COMPENSATED`, `SKIPPED`)
  - `isCompensable`: `boolean` (True if a registered compensation handler exists)
  - `isRetryable`: `boolean` (True if step is idempotent and can be safely re-run)
  - `failureReason`: `String` (Error message if failed)
  - `retryCount`: `int` (Current retry attempt)
  - `maxRetries`: `int` (Default: 3)
  - `executedAt`: `Instant`

---

### 12.3 Engine Components & Contracts

#### File: `backend/src/main/java/com/worklyn/modules/rewind/planner/DependencyValidator.java`
- **Status:** Planned
- **Owner:** Person 3
- **Algorithm:** Kahn's Topological Sort algorithm to validate DAG acyclicity and check prerequisites.
- **Methods:**

| Method | Parameters | Return Type | Detailed Behavior | Validation & Failures |
|---|---|---|---|---|
| `validateDAG` | `List<WorkflowStep> steps` | `boolean` | Checks for circular dependencies (`A -> B -> A`) and ensures all IDs in `dependsOnStepIds` exist in step list. | Throws `IllegalStateException("Circular dependency detected in steps: ...")` if cycle found. |

#### File: `backend/src/main/java/com/worklyn/modules/rewind/planner/RecoveryPlanner.java`
- **Status:** Planned
- **Owner:** Person 3
- **Responsibilities:** Evaluates a failed `WorkflowInstance` and builds a deterministic `RecoveryPlanResponse` without modifying business data.
- **Decision Logic:**
  1. For each `FAILED` step:
     - If `isRetryable == true` and `retryCount < maxRetries`: mark for **REPLAY**.
     - If `isRetryable == false` and `isCompensable == true`: mark for **COMPENSATE**.
     - Otherwise: mark for **MANUAL_REVIEW**.
  2. For downstream steps that depend on the failed step:
     - If currently `PENDING`: mark as **SKIP**.
  3. For upstream steps that already `COMPLETED`:
     - If downstream failure requires overall workflow rollback and upstream `isCompensable`: mark for **COMPENSATE** in reverse topological order.
     - If upstream step is independent: mark as **PRESERVE_UNTOUCHED**.

#### File: `backend/src/main/java/com/worklyn/modules/rewind/planner/InvariantChecker.java`
- **Status:** Planned
- **Owner:** Person 3
- **Responsibilities:** Pre-flight and post-flight validation of business invariants.
- **Invariants Checked:**
  - `LeaveBalance.available >= 0` (No negative leave balance after compensation).
  - `PayrollRecord.netPay >= 0` (No negative net pay generated).
  - `Attendance` records do not contain duplicate dates for any employee.

#### File: `backend/src/main/java/com/worklyn/modules/rewind/service/CompensationHandler.java`
- **Status:** Planned
- **Owner:** Person 3 (Coordinated with Person 2)
- **Responsibilities:** Executes typed reverse actions.
- **Compensations Handled:**
  - `STEP_DEDUCT_LEAVE_BALANCE`: Credits back deducted days to `LeaveBalance`.
  - `STEP_MARK_ATTENDANCE`: Reverts `ON_LEAVE` status back to previous status.
  - `STEP_CREATE_PAYROLL_ENTRY`: Voids the draft `PayrollRecord`.

#### File: `backend/src/main/java/com/worklyn/modules/rewind/service/RecoveryExecutor.java`
- **Status:** Planned
- **Owner:** Person 3
- **Methods:**

| Method | Parameters | Return Type | Detailed Behavior | Validation & Failures |
|---|---|---|---|---|
| `executePlan` | `String workflowId, String actorId, String idempotencyKey` | `RecoveryResultResponse` | 1. Verifies caller is ADMIN.<br>2. Acquires Redis distributed lock `lock:rewind:{workflowId}` (10s TTL).<br>3. Re-validates recovery plan against fresh DB state.<br>4. Invariant pre-check via `InvariantChecker`.<br>5. Executes compensations in reverse dependency order.<br>6. Replays eligible retryable steps with new idempotency key.<br>7. Invariant post-check.<br>8. Persists audit trail in `WorkflowAuditRecord`.<br>9. Releases Redis lock. | Throws `ConflictException` if recovery lock cannot be acquired (concurrent recovery attempt).<br>Throws `InvariantViolationException` if pre/post invariants fail, halting execution and setting status to `REQUIRES_MANUAL_REVIEW`. |

---

### 12.4 End-to-End Demonstration Scenario

The following concrete scenario must be demonstrated to verify HR-REWIND:
1. **Initiation:** Manager approves Leave Request `LR-401` (3 days casual leave for `EMP-101`).
2. **Step 1 (Succeeds):** `STEP_DEDUCT_LEAVE_BALANCE` deducts 3 days from `LeaveBalance` (Remaining: 9).
3. **Step 2 (Succeeds):** `STEP_MARK_ATTENDANCE` marks Oct 21-23 as `ON_LEAVE` in `Attendance`.
4. **Step 3 (Injected Failure):** `STEP_ADJUST_PAYROLL` attempts to apply unpaid salary adjustment, but a deliberate network timeout or constraint error is injected.
5. **Detection:** Workflow status transitions to `FAILED`. Step 3 is marked `FAILED`, failure reason recorded.
6. **Preview:** Admin invokes `GET /api/v1/rewind/{id}/preview`.
   - Engine returns plan:
     - `STEP_ADJUST_PAYROLL`: Eligible for **REPLAY** (if transient) OR **COMPENSATE**.
     - `STEP_MARK_ATTENDANCE`: Reversible via **COMPENSATE**.
     - `STEP_DEDUCT_LEAVE_BALANCE`: Reversible via **COMPENSATE**.
   - Admin observes that no database modifications have occurred during preview.
7. **Execution:** Admin calls `POST /api/v1/rewind/{id}/recover`.
8. **Result:** Compensations execute cleanly in reverse order (Step 2 attendance un-marked, Step 1 leave days refunded). Invariants verified (`LeaveBalance` returns to 12). Audit entry stored. Workflow marked `COMPENSATED`.

---

## 13. Frontend Implementation — Person 4

Person 4 is responsible for the single-page application built with React 18, Vite, and Tailwind CSS. The frontend integrates with all REST endpoints provided by Persons 1, 2, and 3.

### 13.1 Design System & Theme

The UI adopts a warm, professional HR aesthetic:
- **Base / Neutral:** Warm cream/stone (`bg-[#FDFBF7]`, text `text-[#2C2825]`, borders `border-[#EAE5DE]`)
- **Primary Brand:** Deep Bordeaux Wine (`bg-[#6B1D2F]`, hover `bg-[#531422]`, text `text-white`)
- **Secondary Accent:** Soft Rose / Amber (`bg-[#F4EBE6]`, text `text-[#6B1D2F]`)
- **Status Colors:**
  - Success / Active: Emerald (`bg-emerald-50`, `text-emerald-700`, `border-emerald-200`)
  - Pending / Warning: Amber (`bg-amber-50`, `text-amber-700`, `border-amber-200`)
  - Error / Terminated: Crimson (`bg-rose-50`, `text-rose-700`, `border-rose-200`)
  - Info / Draft: Slate (`bg-slate-50`, `text-slate-700`, `border-slate-200`)

---

### 13.2 Core Frontend Architecture

#### File: `frontend/src/api/client.js`
- **Status:** Planned
- **Owner:** Person 4
- **Responsibilities:** Centralized Fetch / Axios wrapper with base URL handling, automatic JWT attachment, unified error parsing, and 401 redirect hooks.
- **Methods:**

| Function | Parameters | Return Type | Detailed Behavior |
|---|---|---|---|
| `apiClient` | `string endpoint, object options` | `Promise<any>` | Prepends `VITE_API_BASE_URL` (default: `/api/v1`). Attaches `Authorization: Bearer <token>` from `localStorage` if present. Sets `Content-Type: application/json`. Intercepts HTTP 401 and dispatches an auth expiration event. On HTTP error, parses JSON `ApiErrorResponse` and throws a typed `ApiError`. |
| `get` | `string path, object params` | `Promise<any>` | Serializes query parameters, delegates to `apiClient`. |
| `post` | `string path, object body, string idempotencyKey` | `Promise<any>` | Serializes body, optionally attaches `X-Idempotency-Key`, delegates to `apiClient`. |
| `put` | `string path, object body` | `Promise<any>` | Sends HTTP PUT request. |
| `del` | `string path` | `Promise<any>` | Sends HTTP DELETE request. |

#### File: `frontend/src/context/AuthContext.jsx`
- **Status:** Planned
- **Owner:** Person 4
- **Responsibilities:** Manages global user authentication state, token persistence in `localStorage`, role evaluation helpers, and session logout.
- **State Fields:**
  - `user`: `{ id, email, firstName, lastName, roles, employeeId } | null`
  - `token`: `string | null`
  - `isLoading`: `boolean`
  - `isAuthenticated`: `boolean`
- **Exposed Helpers:**
  - `login(email, password)`: Calls `authApi.login`, saves token, populates user state.
  - `logout()`: Clears storage, resets state to null, navigates to `/login`.
  - `hasRole(role)`: Returns `boolean` checking if `user.roles` contains specified role.
  - `hasAnyRole(rolesArray)`: Returns true if user has at least one of the roles.

---

### 13.3 Page Implementations

#### 1. Login Page (`frontend/src/pages/Login.jsx`)
- **Route:** `/login`
- **Access:** Public (redirects to `/dashboard` if already authenticated)
- **Features:**
  - Email and password inputs with client-side validation (non-empty, email format).
  - Submit button with spinner loading state.
  - Error banner rendering `ApiErrorResponse.message`.
  - Demo quick-fill credential buttons for testing (Admin, HR, Employee).

#### 2. Dashboard Page (`frontend/src/pages/Dashboard.jsx`)
- **Route:** `/dashboard`
- **Access:** Protected (`isAuthenticated`)
- **Features:**
  - Summary KPI cards: Total Active Employees, Present Today, Pending Leave Requests, Next Payroll Run.
  - Role-specific widgets:
    - **Admin/HR:** Quick-action buttons (Add Employee, Process Payroll, View Recoveries).
    - **Employee:** Quick Check-in/Check-out button showing current day status and live work timer; Quick Leave Balance badges.

#### 3. Employee Directory & Forms (`frontend/src/pages/Employees.jsx` & `EmployeeDetails.jsx`)
- **Route:** `/employees`, `/employees/:id`
- **Access:** HR, ADMIN, MANAGER (Employee view restricted to self)
- **Features:**
  - Paginated table with columns: Code, Name, Department, Designation, Status, Actions.
  - Live prefix search input hooked to Autocomplete API.
  - Department and Status dropdown filters.
  - "Add Employee" modal form with validation (code uniqueness, email, base salary).
  - Employee detail view with tabs: Profile, Attendance History, Leaves Taken, Payroll History.

#### 4. Attendance Page (`frontend/src/pages/Attendance.jsx`)
- **Route:** `/attendance`
- **Access:** All roles
- **Features:**
  - Single-click Check-in and Check-out card with live clock and IP audit display.
  - Monthly calendar / table view of attendance records with color-coded status badges (`PRESENT`, `HALF_DAY`, `LATE`, `ON_LEAVE`, `ABSENT`).
  - HR view: Department filter and date-range picker to export or audit employee check-ins.

#### 5. Leave Management Page (`frontend/src/pages/LeaveManagement.jsx`)
- **Route:** `/leave`
- **Access:** All roles
- **Features:**
  - Visual Leave Balance summary cards (Casual, Sick, Earned: Available vs Used).
  - "Apply for Leave" modal with date picker, leave type selector, reason textarea, and dynamic business-day count calculation.
  - My Requests table showing application status with timeline.
  - **Approvals Tab (HR / Manager only):** List of pending leave requests with employee details, overlapping check indicators, and Approve / Reject action modals requiring remarks.

#### 6. Payroll Page (`frontend/src/pages/Payroll.jsx`)
- **Route:** `/payroll`
- **Access:** HR and ADMIN for processing; EMPLOYEE for payslip viewing
- **Features:**
  - Period selector (e.g. `2026-10`).
  - "Run Payroll" button with confirmation modal (enforces `X-Idempotency-Key` generation).
  - Payroll batch summary: Total Gross, Total Deductions, Total Net Disbursement.
  - Employee payslip view modal showing base salary, present days, leave deductions, and net pay.
  - "Finalize Period" lock button.

#### 7. HR-REWIND Recovery Dashboard (`frontend/src/pages/RewindDashboard.jsx`)
- **Route:** `/rewind`
- **Access:** ADMIN only
- **Components:**
  - **Workflow Instance Table:** Lists workflows with status badges (`COMPLETED`, `FAILED`, `REQUIRES_MANUAL_REVIEW`).
  - **DAG Visualization View (`DependencyGraphView.jsx`):** Renders interactive step graph with status-coded nodes (Green = Completed, Red = Failed, Blue = Replay eligible, Yellow = Compensate eligible).
  - **Recovery Preview Table (`PlanPreviewTable.jsx`):** Displays computed actions from `GET /api/v1/rewind/{id}/preview` (Replay, Compensate, Skip) without mutating DB state.
  - **Execution Modal (`RecoveryExecutionModal.jsx`):** Requires admin confirmation with warning of compensations to be executed; streams or polls execution progress and shows final invariant results.

---

## 14. Complete REST API Reference

All requests accept and return `application/json` with base prefix `/api/v1`.

### 14.1 Authentication Endpoints

| Method | Endpoint | Allowed Roles | Description | Status |
|---|---|---|---|---|
| `POST` | `/api/v1/auth/login` | Public | Authenticate user with email and password | 200 OK, 401 Unauthorized |
| `POST` | `/api/v1/auth/register` | `ROLE_ADMIN` | Provision a new user account | 201 Created, 400 Bad Request, 409 Conflict |
| `GET` | `/api/v1/auth/me` | Authenticated | Retrieve current user profile and roles | 200 OK, 401 Unauthorized |

#### Example: `POST /api/v1/auth/login`
- **Request Body:**
```json
{
  "email": "hr.admin@worklyn.io",
  "password": "Password@123"
}
```
- **Success Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "id": "6714a8f9c102b34a11000101",
    "email": "hr.admin@worklyn.io",
    "firstName": "Sarah",
    "lastName": "Jenkins",
    "roles": ["ROLE_ADMIN", "ROLE_HR"],
    "employeeId": "6714a8f9c102b34a11000102"
  }
}
```

---

### 14.2 Employee Endpoints

| Method | Endpoint | Allowed Roles | Description | Status |
|---|---|---|---|---|
| `POST` | `/api/v1/employees` | `ROLE_ADMIN`, `ROLE_HR` | Create employee profile | 201 Created, 400 Bad Request, 409 Conflict |
| `GET` | `/api/v1/employees/{id}` | All (Self or HR/Admin/Mgr) | Fetch employee by ID | 200 OK, 404 Not Found |
| `PUT` | `/api/v1/employees/{id}` | `ROLE_ADMIN`, `ROLE_HR` | Update employee profile | 200 OK, 400 Bad Request, 404 Not Found |
| `PATCH`| `/api/v1/employees/{id}/status`| `ROLE_ADMIN`, `ROLE_HR` | Update employee status | 200 OK, 409 Conflict |
| `GET` | `/api/v1/employees` | `ROLE_ADMIN`, `ROLE_HR`, `ROLE_MANAGER` | Search and filter employees | 200 OK |

#### Example: `POST /api/v1/employees`
- **Request Body:**
```json
{
  "employeeCode": "EMP-2045",
  "userId": "6714a8f9c102b34a11000103",
  "firstName": "David",
  "lastName": "Chen",
  "email": "david.chen@worklyn.io",
  "department": "ENGINEERING",
  "designation": "Backend Engineer",
  "dateOfJoining": "2026-11-01",
  "baseSalary": 95000.00,
  "managerId": "6714a8f9c102b34a11000102"
}
```
- **Success Response (201 Created):**
```json
{
  "id": "6714a900c102b34a11000104",
  "employeeCode": "EMP-2045",
  "firstName": "David",
  "lastName": "Chen",
  "email": "david.chen@worklyn.io",
  "department": "ENGINEERING",
  "designation": "Backend Engineer",
  "dateOfJoining": "2026-11-01",
  "status": "ACTIVE",
  "baseSalary": 95000.00,
  "managerId": "6714a8f9c102b34a11000102",
  "createdAt": "2026-10-20T13:48:00.000Z"
}
```

---

### 14.3 Attendance Endpoints

| Method | Endpoint | Allowed Roles | Description | Status |
|---|---|---|---|---|
| `POST` | `/api/v1/attendance/check-in` | Authenticated | Record daily check-in | 201 Created, 409 Conflict |
| `POST` | `/api/v1/attendance/check-out` | Authenticated | Record daily check-out | 200 OK, 400 Bad Request, 409 Conflict |
| `GET` | `/api/v1/attendance/my-history` | Authenticated | Get caller attendance history | 200 OK |
| `GET` | `/api/v1/attendance/employee/{empId}`| `ROLE_ADMIN`, `ROLE_HR`, `ROLE_MGR` | Get specific employee history | 200 OK, 403 Forbidden |

#### Example: `POST /api/v1/attendance/check-in`
- **Request Body:**
```json
{
  "timestamp": "2026-10-20T08:55:00.000Z",
  "idempotencyKey": "a80c98f1-5bb7-4f2b-8a71-61cfb3e1001a"
}
```
- **Success Response (201 Created):**
```json
{
  "id": "6714a920c102b34a11000105",
  "employeeId": "6714a8f9c102b34a11000104",
  "attendanceDate": "2026-10-20",
  "checkInTime": "2026-10-20T08:55:00.000Z",
  "checkOutTime": null,
  "durationMinutes": null,
  "status": "PRESENT"
}
```

---

### 14.4 Leave Endpoints

| Method | Endpoint | Allowed Roles | Description | Status |
|---|---|---|---|---|
| `POST` | `/api/v1/leave/apply` | Authenticated | Apply for leave | 201 Created, 400 Bad Request, 409 Conflict |
| `GET` | `/api/v1/leave/balance` | Authenticated | Get caller leave balances | 200 OK |
| `GET` | `/api/v1/leave/my-requests` | Authenticated | List caller leave applications | 200 OK |
| `POST` | `/api/v1/leave/{id}/approve` | `ROLE_ADMIN`, `ROLE_HR`, `ROLE_MGR` | Approve leave request | 200 OK, 409 Conflict |
| `POST` | `/api/v1/leave/{id}/reject` | `ROLE_ADMIN`, `ROLE_HR`, `ROLE_MGR` | Reject leave request | 200 OK, 400 Bad Request |
| `POST` | `/api/v1/leave/{id}/cancel` | Authenticated | Cancel leave request | 200 OK, 403 Forbidden |

---

### 14.5 Payroll Endpoints

| Method | Endpoint | Allowed Roles | Description | Status |
|---|---|---|---|---|
| `POST` | `/api/v1/payroll/process` | `ROLE_ADMIN`, `ROLE_HR` | Execute batch payroll calculation | 200 OK, 409 Conflict |
| `GET` | `/api/v1/payroll/period/{period}` | `ROLE_ADMIN`, `ROLE_HR` | Get all payroll records for period | 200 OK |
| `GET` | `/api/v1/payroll/payslip` | Authenticated | Get caller payslip for period | 200 OK, 404 Not Found |
| `POST` | `/api/v1/payroll/finalize` | `ROLE_ADMIN`, `ROLE_HR` | Lock period records | 200 OK, 409 Conflict |

---

### 14.6 Distributed Systems Endpoints

| Method | Endpoint | Allowed Roles | Description | Status |
|---|---|---|---|---|
| `POST` | `/api/v1/shortlinks` | Authenticated | Create a shortened URL | 201 Created, 400 Bad Request |
| `GET` | `/s/{code}` | Public | Redirect to destination URL | 302 Found, 404 Not Found |
| `GET` | `/api/v1/autocomplete/employees` | Authenticated | Prefix suggestions for employee search | 200 OK |
| `POST` | `/api/v1/crawler/jobs` | `ROLE_ADMIN` | Trigger controlled crawl of whitelisted source | 200 OK, 403 Forbidden |
| `GET` | `/api/v1/system/hash-ring` | `ROLE_ADMIN` | Inspect current consistent hash ring snapshot | 200 OK |

---

### 14.7 HR-REWIND Recovery Endpoints

| Method | Endpoint | Allowed Roles | Description | Status |
|---|---|---|---|---|
| `GET` | `/api/v1/rewind/workflows` | `ROLE_ADMIN` | List all tracked multi-step workflows | 200 OK |
| `GET` | `/api/v1/rewind/workflows/{id}` | `ROLE_ADMIN` | Get detailed DAG step status | 200 OK, 404 Not Found |
| `GET` | `/api/v1/rewind/workflows/{id}/preview`| `ROLE_ADMIN` | Generate non-mutating recovery plan | 200 OK, 404 Not Found |
| `POST` | `/api/v1/rewind/workflows/{id}/recover`| `ROLE_ADMIN` | Execute dependency-aware recovery | 200 OK, 409 Conflict |

#### Example: `GET /api/v1/rewind/workflows/6714a930c102/preview`
- **Success Response (200 OK):**
```json
{
  "workflowId": "6714a930c102",
  "workflowType": "LEAVE_APPROVAL_CASCADE",
  "status": "FAILED",
  "failedStepId": "STEP_ADJUST_PAYROLL",
  "failureReason": "HTTP Connection timeout calling external payroll service",
  "actions": [
    {
      "stepId": "STEP_ADJUST_PAYROLL",
      "action": "REPLAY",
      "reason": "Step is marked idempotent and has retry attempts remaining (1/3)."
    },
    {
      "stepId": "STEP_MARK_ATTENDANCE",
      "action": "PRESERVE",
      "reason": "Step completed successfully and has no failed upstream dependency."
    },
    {
      "stepId": "STEP_DEDUCT_LEAVE_BALANCE",
      "action": "PRESERVE",
      "reason": "Step completed successfully."
    }
  ],
  "invariantsSatisfied": true
}
```

---

## 15. Database Models and Indexes

MongoDB 7 is the primary persistent document store. The collections and indexing strategies are defined below:

| Collection Name | Model Class | Indexes | Unique | Purpose |
|---|---|---|---|---|
| `users` | `User` | `{"email": 1}` | Yes | User authentication credentials |
| `employees` | `Employee` | `{"employeeCode": 1}`<br>`{"email": 1}`<br>`{"userId": 1}`<br>`{"department": 1, "status": 1}` | Yes<br>Yes<br>Yes<br>No | Employee records and department filtering |
| `attendance` | `Attendance` | `{"employeeId": 1, "attendanceDate": 1}`<br>`{"attendanceDate": 1}` | Yes<br>No | One attendance entry per employee per day |
| `leave_requests` | `LeaveRequest` | `{"employeeId": 1, "status": 1}`<br>`{"startDate": 1, "endDate": 1}` | No<br>No | Fast lookup of employee pending/approved leaves |
| `leave_balances` | `LeaveBalance` | `{"employeeId": 1, "year": 1}` | Yes | Annual employee leave allocation |
| `payroll_records` | `PayrollRecord` | `{"employeeId": 1, "payrollPeriod": 1}`<br>`{"payrollPeriod": 1, "status": 1}` | Yes<br>No | Monthly payslips and period finalization queries |
| `short_links` | `ShortLinkModel` | `{"shortCode": 1}`<br>`{"createdAt": 1}` (TTL = 30d) | Yes<br>No | Fast redirection lookups and auto-expiry |
| `rewind_workflows` | `WorkflowInstance` | `{"workflowId": 1}`<br>`{"status": 1}` | Yes<br>No | Recovery engine workflow DAG storage |
| `rewind_audits` | `WorkflowAuditRecord` | `{"workflowId": 1}`<br>`{"executedAt": 1}` | No<br>No | Immutable audit logs of recovery executions |

---

## 16. Redis and Caching Behavior

Redis 7 serves as the distributed cache, atomic rate-limit tracker, and idempotency store.

### 16.1 Key Namespaces and TTL Policies

| Key Pattern | Data Structure | TTL | Invalidation Trigger |
|---|---|---|---|
| `emp:{id}` | String (JSON) | 1 hour | Evicted on `EmployeeService.updateEmployee` or status change |
| `emp:code:{code}` | String (JSON) | 1 hour | Evicted on employee updates |
| `leave:balance:{empId}:{year}` | String (JSON) | 30 minutes | Evicted on `LeaveService.approveLeave` or cancellation |
| `rl:user:{userId}` | Hash (`tokens`, `lastRefreshed`) | Dynamic | Automatic expiration calculated via Token Bucket script |
| `rl:ip:{ipAddress}` | Hash (`tokens`, `lastRefreshed`) | Dynamic | Automatic expiration |
| `idemp:{key}` | String (Result JSON) | 24 hours | Retained for 24h to prevent duplicate runs |
| `sl:{code}` | String (Target URL) | 24 hours | Evicted on manual link expiration |
| `lock:rewind:{workflowId}` | String (Token) | 10 seconds | Released immediately upon recovery plan completion |
| `autocomplete:employees` | Sorted Set | Persistent | Updated incrementally when employees are created/updated |

### 16.2 Invalidation & Fallback Strategy
- **Read-Through with Fallback:** All service cache reads catch Redis exceptions and fallback to MongoDB. Redis downtime never causes HTTP 500 errors for queries.
- **Transactional Invalidation:** Cache evictions occur after database persistence completes successfully.
- **No Blocking Scans:** Bulk evictions use cursor-based `SCAN` rather than blocking `KEYS *`.

---

## 17. Consistent Hashing and Shard Routing

### 17.1 Hash Function & Virtual Nodes
- **Murmur3 64-bit:** Generates deterministic hashes with uniform distribution across the 64-bit integer space ($[-2^{63}, 2^{63}-1]$).
- **Virtual Node Layout:** Each physical partition or shard (e.g. `shard-alpha`, `shard-beta`, `shard-gamma`) is assigned 150 virtual nodes on the ring:
  $$
\text{VNodeHash} = \text{Murmur3}(\text{shardId} + "#vn#" + i) \quad \text{for } i \in [0, 149]
$$
- **Key Assignment:**
  $$
\text{KeyHash} = \text{Murmur3}(\text{tenantOrEmployeeKey})
$$
  The ring selects the first virtual node whose hash is $\ge \text{KeyHash}$. If $\text{KeyHash}$ is greater than all nodes on the ring, it wraps around to the lowest hash on the ring.

### 17.2 Shard Addition and Minimal Remapping
When a new shard (`shard-delta`) is added:
- Exactly $1 / (N+1)$ fraction of keys are remapped on average.
- The rest of the keys remain mapped to their original physical shards.
- The ring snapshot is exposed via `GET /api/v1/system/hash-ring` for verification.

---

## 18. Infrastructure and Configuration — Person 4

Person 4 owns container orchestration, reverse proxy routing, and integration testing assets.

### 18.1 Docker Compose Configuration (`docker-compose.yml`)

The multi-container specification runs the entire stack locally with health checks and network isolation:

```yaml
version: '3.8'

services:
  mongodb:
    image: mongo:7.0
    container_name: worklyn-mongodb
    restart: always
    environment:
      MONGO_INITDB_ROOT_USERNAME: worklyn_admin
      MONGO_INITDB_ROOT_PASSWORD: secret_password
      MONGO_INITDB_DATABASE: worklyn_db
    ports:
      - "27017:27017"
    volumes:
      - mongo_data:/data/db
    networks:
      - worklyn-net
    healthcheck:
      test: ["CMD", "mongosh", "--eval", "db.adminCommand('ping')"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7.2-alpine
    container_name: worklyn-redis
    restart: always
    command: ["redis-server", "--requirepass", "redis_secret"]
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - worklyn-net
    healthcheck:
      test: ["CMD", "redis-cli", "-a", "redis_secret", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: worklyn-backend
    restart: always
    depends_on:
      mongodb:
        condition: service_healthy
      redis:
        condition: service_healthy
    environment:
      SPRING_DATA_MONGODB_URI: mongodb://worklyn_admin:secret_password@mongodb:27017/worklyn_db?authSource=admin
      SPRING_DATA_REDIS_HOST: redis
      SPRING_DATA_REDIS_PORT: 6379
      SPRING_DATA_REDIS_PASSWORD: redis_secret
      APP_JWT_SECRET: 9f8a3c8d1e2b4f5a6b7c8d9e0f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a
      APP_JWT_EXPIRATION_MS: 86400000
    ports:
      - "8080:8080"
    networks:
      - worklyn-net

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: worklyn-frontend
    restart: always
    networks:
      - worklyn-net

  nginx:
    build:
      context: ./nginx
      dockerfile: Dockerfile
    container_name: worklyn-nginx
    restart: always
    depends_on:
      - backend
      - frontend
    ports:
      - "80:80"
    networks:
      - worklyn-net

networks:
  worklyn-net:
    driver: bridge

volumes:
  mongo_data:
  redis_data:
```

### 18.2 Nginx Configuration (`nginx/nginx.conf`)

Routes client requests between the React frontend static build and the Spring Boot backend:

```nginx
events {
    worker_connections 1024;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;
    sendfile on;
    client_max_body_size 10M;

    upstream backend_upstream {
        server backend:8080;
    }

    upstream frontend_upstream {
        server frontend:80;
    }

    server {
        listen 80;
        server_name localhost;

        # API routing
        location /api/ {
            proxy_pass http://backend_upstream;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            proxy_connect_timeout 5s;
            proxy_read_timeout 60s;
        }

        # Short-link redirection route
        location /s/ {
            proxy_pass http://backend_upstream;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
        }

        # Frontend SPA routing
        location / {
            proxy_pass http://frontend_upstream;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
        }
    }
}
```

---

## 19. Environment Variable Reference

| Variable Name | Required | Example Value | Description | Default / Fallback |
|---|---|---|---|---|
| `SPRING_DATA_MONGODB_URI` | Yes | `mongodb://admin:pass@localhost:27017/worklyn_db?authSource=admin` | Connection string for MongoDB | Fails startup if absent |
| `SPRING_DATA_REDIS_HOST` | Yes | `localhost` | Redis server hostname | `localhost` |
| `SPRING_DATA_REDIS_PORT` | Yes | `6379` | Redis port | `6379` |
| `SPRING_DATA_REDIS_PASSWORD` | Optional | `redis_secret` | Redis authentication password | Empty string (no auth) |
| `APP_JWT_SECRET` | Yes | `9f8a3c8d1e2b4f5a...` (min 256 bits) | HMAC-SHA256 signing secret | Fails startup if < 32 bytes |
| `APP_JWT_EXPIRATION_MS` | No | `86400000` (24 hours) | Token validity duration in milliseconds | `86400000` |
| `VITE_API_BASE_URL` | No | `/api/v1` | Frontend API target | `/api/v1` |
| `APP_CRAWLER_ALLOWED_DOMAINS` | No | `careers.company.com,jobs.techcorp.io` | Whitelist for job crawler module | Empty (crawler rejects all) |

---

## 20. Detailed Test-Case Catalogue

The following PeerLink-style test matrix establishes every verification scenario across the system.

### 20.1 Authentication & Security (AUTH)

| Test ID | Module | Scenario | Preconditions / Input | Action | Expected Result | Verification | Status |
|---|---|---|---|---|---|---|---|
| AUTH-001 | Auth | Valid Login | Active user with password `Password@123` | `POST /api/v1/auth/login` | HTTP 200, valid JWT returned | Inspect token claims (`sub`, `roles`) | Planned |
| AUTH-002 | Auth | Wrong Password | Active user with incorrect password | `POST /api/v1/auth/login` | HTTP 401 Unauthorized | Assert error code `INVALID_CREDENTIALS` | Planned |
| AUTH-003 | Auth | Unknown User | Non-existent email `unknown@worklyn.io` | `POST /api/v1/auth/login` | HTTP 401 Unauthorized | Assert generic error message | Planned |
| AUTH-004 | Auth | Missing Fields | Request with blank email and password | `POST /api/v1/auth/login` | HTTP 400 Bad Request | Assert `validationErrors` list has 2 entries | Planned |
| AUTH-005 | Auth | Admin Provisions User | Actor has `ROLE_ADMIN`, valid new user payload | `POST /api/v1/auth/register` | HTTP 201 Created | Verify user saved in MongoDB with BCrypt hash | Planned |
| AUTH-006 | Auth | Non-Admin Self-Register | Actor has `ROLE_EMPLOYEE` or unauthenticated | `POST /api/v1/auth/register` | HTTP 403 Forbidden | Assert operation blocked | Planned |
| AUTH-007 | Auth | Duplicate Email Registration | Email already registered in system | `POST /api/v1/auth/register` | HTTP 409 Conflict | Assert error code `EMAIL_ALREADY_EXISTS` | Planned |
| AUTH-008 | Auth | Expired JWT | Token with `exp` in the past | Send request to protected route | HTTP 401 Unauthorized | Assert JWT filter rejects with expired reason | Planned |
| AUTH-009 | Auth | Tampered JWT Signature | Valid token with modified payload | Send request to protected route | HTTP 401 Unauthorized | Assert signature verification fails | Planned |
| AUTH-010 | Auth | Missing Bearer Token | No `Authorization` header provided | `GET /api/v1/employees` | HTTP 401 Unauthorized | Assert request rejected | Planned |
| AUTH-011 | Auth | RBAC Route Guard | User with `ROLE_EMPLOYEE` calls HR endpoint | `POST /api/v1/payroll/process` | HTTP 403 Forbidden | Assert access denied | Planned |

---

### 20.2 Employee Profile Management (EMP)

| Test ID | Module | Scenario | Preconditions / Input | Action | Expected Result | Verification | Status |
|---|---|---|---|---|---|---|---|
| EMP-001 | Employee | Create Employee Success | Actor is HR, unique employeeCode `EMP-101` | `POST /api/v1/employees` | HTTP 201, Employee DTO returned | Verify MongoDB record created; Leave balance init | Planned |
| EMP-002 | Employee | Duplicate Employee Code | `EMP-101` already exists in MongoDB | `POST /api/v1/employees` | HTTP 409 Conflict | Assert error code `EMPLOYEE_CODE_EXISTS` | Planned |
| EMP-003 | Employee | Duplicate Email | Email already exists | `POST /api/v1/employees` | HTTP 409 Conflict | Assert error code `EMAIL_ALREADY_EXISTS` | Planned |
| EMP-004 | Employee | Negative Base Salary | `baseSalary: -1000` | `POST /api/v1/employees` | HTTP 400 Bad Request | Assert validation message on baseSalary | Planned |
| EMP-005 | Employee | Get Employee by ID | Valid ID, actor is HR | `GET /api/v1/employees/{id}` | HTTP 200, Employee profile | Verify profile matches MongoDB | Planned |
| EMP-006 | Employee | Cache Read-Through | Employee queried twice | `GET /api/v1/employees/{id}` x2 | HTTP 200; 2nd query hits Redis | Verify Redis key `emp:{id}` exists | Planned |
| EMP-007 | Employee | Cache Invalidation on Update | Update employee designation | `PUT /api/v1/employees/{id}` | HTTP 200; Redis key evicted | Verify Redis key `emp:{id}` is deleted | Planned |
| EMP-008 | Employee | Status Transition to Terminated | Active employee marked `TERMINATED` | `PATCH /api/v1/employees/{id}/status` | HTTP 200, status updated | Verify status in DB | Planned |

---

### 20.3 Attendance Module (ATT)

| Test ID | Module | Scenario | Preconditions / Input | Action | Expected Result | Verification | Status |
|---|---|---|---|---|---|---|---|
| ATT-001 | Attendance | Valid Check-in | Employee has not checked in today | `POST /api/v1/attendance/check-in` | HTTP 201, record created | Verify status `PRESENT` or `LATE` | Planned |
| ATT-002 | Attendance | Duplicate Check-in Today | Employee already checked in today | `POST /api/v1/attendance/check-in` | HTTP 409 Conflict | Verify only 1 attendance record in MongoDB | Planned |
| ATT-003 | Attendance | Valid Check-out | Employee checked in 8 hours ago | `POST /api/v1/attendance/check-out` | HTTP 200, checkOutTime set | Verify durationMinutes ≈ 480 | Planned |
| ATT-004 | Attendance | Check-out Without Check-in | No check-in record for today | `POST /api/v1/attendance/check-out` | HTTP 404 Not Found | Assert error code `NO_CHECKIN_FOUND` | Planned |
| ATT-005 | Attendance | Repeated Check-out | Check-out already completed | `POST /api/v1/attendance/check-out` | HTTP 409 Conflict | Assert error code `ALREADY_CHECKED_OUT` | Planned |
| ATT-006 | Attendance | Check-in Late Marking | Timestamp is 10:15 AM (> 09:30 AM) | `POST /api/v1/attendance/check-in` | HTTP 201, status = `LATE` | Assert status field in response | Planned |
| ATT-007 | Attendance | Idempotency Key Replay | Same idempotency key submitted twice | `POST /api/v1/attendance/check-in` x2 | HTTP 200 with original response | Verify no duplicate check-in executed | Planned |

---

### 20.4 Leave Management (LEAVE)

| Test ID | Module | Scenario | Preconditions / Input | Action | Expected Result | Verification | Status |
|---|---|---|---|---|---|---|---|
| LEAVE-001 | Leave | Apply Valid Leave | Sufficient casual leave balance | `POST /api/v1/leave/apply` | HTTP 201, status `PENDING` | Verify record in MongoDB | Planned |
| LEAVE-002 | Leave | End Date Before Start Date | `startDate: 2026-11-10`, `endDate: 2026-11-05` | `POST /api/v1/leave/apply` | HTTP 400 Bad Request | Assert date validation message | Planned |
| LEAVE-003 | Leave | Overlapping Leave Request | Pending/approved leave on same dates | `POST /api/v1/leave/apply` | HTTP 409 Conflict | Assert error code `OVERLAPPING_LEAVE` | Planned |
| LEAVE-004 | Leave | Insufficient Balance | Request 15 days casual leave (available: 12) | `POST /api/v1/leave/apply` | HTTP 409 Conflict | Assert error code `INSUFFICIENT_BALANCE` | Planned |
| LEAVE-005 | Leave | Manager Approves Leave | Pending leave, actor is manager | `POST /api/v1/leave/{id}/approve` | HTTP 200, status `APPROVED` | Verify balance deducted; attendance marked | Planned |
| LEAVE-006 | Leave | Unauthorized Approval | Actor is regular employee | `POST /api/v1/leave/{id}/approve` | HTTP 403 Forbidden | Assert access denied | Planned |
| LEAVE-007 | Leave | Double Approval Attempt | Request already `APPROVED` | `POST /api/v1/leave/{id}/approve` | HTTP 409 Conflict | Assert invalid state transition | Planned |
| LEAVE-008 | Leave | Employee Cancels Pending | Request is currently `PENDING` | `POST /api/v1/leave/{id}/cancel` | HTTP 200, status `CANCELLED` | Balance unchanged | Planned |

---

### 20.5 Payroll Processing (PAY)

| Test ID | Module | Scenario | Preconditions / Input | Action | Expected Result | Verification | Status |
|---|---|---|---|---|---|---|---|
| PAY-001 | Payroll | Process Period Success | Active employees with attendance data | `POST /api/v1/payroll/process` | HTTP 200, batch result summary | Verify `PayrollRecord` created for each employee | Planned |
| PAY-002 | Payroll | Duplicate Run with Same Key | Request with same idempotency key | `POST /api/v1/payroll/process` x2 | HTTP 200, returns cached result | Verify no duplicate financial records created | Planned |
| PAY-003 | Payroll | Invalid Period Format | `period: "2026-13"` | `POST /api/v1/payroll/process` | HTTP 400 Bad Request | Assert period regex validation error | Planned |
| PAY-004 | Payroll | Finalized Period Locked | Period marked `FINALIZED` | `POST /api/v1/payroll/process` | HTTP 409 Conflict | Assert locked period cannot be re-processed | Planned |
| PAY-005 | Payroll | Employee Views Payslip | Employee requests own payslip | `GET /api/v1/payroll/payslip?period=...` | HTTP 200, payslip breakdown | Assert salary numbers match calculations | Planned |
| PAY-006 | Payroll | Unauthorized Payslip Access | Employee requests other employee's payslip | `GET /api/v1/payroll/employee/{otherId}` | HTTP 403 Forbidden | Assert forbidden | Planned |

---

### 20.6 Distributed Systems Modules (DIST & RL)

| Test ID | Module | Scenario | Preconditions / Input | Action | Expected Result | Verification | Status |
|---|---|---|---|---|---|---|---|
| RL-001 | Rate Limiter | Burst Below Capacity | 10 rapid requests from same IP | Send 10 calls | All 10 return HTTP 200 | Assert `X-RateLimit-Remaining` decreases | Planned |
| RL-002 | Rate Limiter | Exhaustion Returns 429 | Exceed bucket capacity (21st request) | Send 21st call | HTTP 429 Too Many Requests | Assert `RATE_LIMIT_EXCEEDED` code & retry header | Planned |
| RL-003 | Rate Limiter | Refill Over Time | Exhaust bucket, sleep 2 seconds | Send request | HTTP 200 OK | Assert tokens replenished | Planned |
| HASH-001 | Consistent Hashing | Deterministic Mapping | Ring with 3 shards, fixed key "EMP-101" | Route key 100 times | All 100 route to identical shard | Assert zero route drift | Planned |
| HASH-002 | Consistent Hashing | Shard Addition Minimal Remap | Ring with 3 shards, add 4th shard | Remap 10,000 test keys | ~25% remapped, ~75% unchanged | Assert minimal remapping guarantee | Planned |
| ID-001 | ID Generator | Monotonic & Unique | Generate 50,000 IDs concurrently across threads | Multi-threaded invocation | All 50,000 IDs strictly unique and ascending | Assert Set size == 50,000 | Planned |
| SHORT-001 | Short Links | Create & Resolve | Destination: `https://worklyn.io/docs/payroll` | `POST /api/v1/shortlinks`, then `GET /s/{code}` | HTTP 302 redirect to original URL | Verify click counter incremented | Planned |
| SHORT-002 | Short Links | SSRF Prevention | Destination: `http://localhost:8080/admin` | `POST /api/v1/shortlinks` | HTTP 400 Bad Request | Assert internal addresses blocked | Planned |
| CRAWL-001 | Job Crawler | Whitelisted Domain Allowed | Source: `https://careers.example.com` | `POST /api/v1/crawler/jobs` | HTTP 200, parsed listings | Verify deduplicated job records returned | Planned |
| CRAWL-002 | Job Crawler | Disallowed Domain Blocked | Source: `https://malicious-site.com` | `POST /api/v1/crawler/jobs` | HTTP 403 Forbidden | Assert domain rejected by security guard | Planned |
| AUTO-001 | Autocomplete | Prefix Query Match | Employees "David Chen", "Daniel Smith" exist | `GET /api/v1/autocomplete/employees?q=Da` | HTTP 200, returns both employees | Assert ranked prefix response | Planned |

---

### 20.7 HR-REWIND Recovery Engine (REW)

| Test ID | Module | Scenario | Preconditions / Input | Action | Expected Result | Verification | Status |
|---|---|---|---|---|---|---|---|
| REW-001 | HR-REWIND | DAG Cycle Detection | Steps: A depends on B, B depends on A | Register workflow | `IllegalStateException` thrown | Cycle detected before execution | Planned |
| REW-002 | HR-REWIND | Non-Mutating Preview | Workflow has failed Step 3 | `GET /api/v1/rewind/{id}/preview` | HTTP 200 with plan | Assert MongoDB state identical before and after | Planned |
| REW-003 | HR-REWIND | Selective Replay | Step 3 failed transiently, retryable = true | `POST /api/v1/rewind/{id}/recover` | Step 3 replayed; status -> `COMPLETED` | Independent Steps 1 & 2 untouched | Planned |
| REW-004 | HR-REWIND | Reverse Compensation | Non-retryable failure in leave cascade | `POST /api/v1/rewind/{id}/recover` | Compensations executed in reverse order | Balance refunded; attendance reverted | Planned |
| REW-005 | HR-REWIND | Invariant Enforcement | Compensation would cause negative balance | Trigger compensation with corrupt data | Recovery halts; status -> `REQUIRES_MANUAL_REVIEW` | Assert invariant guard blocked invalid state | Planned |
| REW-006 | HR-REWIND | Concurrent Recovery Lock | Two recovery requests dispatched simultaneously | `POST /api/v1/rewind/{id}/recover` x2 | 1st executes; 2nd receives HTTP 409 Lock Conflict | Verify no duplicate compensations | Planned |

---

### 20.8 Frontend User Interface (UI)

| Test ID | Module | Scenario | Action | Expected Result | Verification | Status |
|---|---|---|---|---|---|---|
| UI-001 | Frontend | Login Success | Enter valid credentials and submit | Redirects to `/dashboard`; token saved | Check `localStorage.getItem('token')` | Planned |
| UI-002 | Frontend | Protected Route Guard | Access `/employees` without token | Redirects immediately to `/login` | Assert browser URL is `/login` | Planned |
| UI-003 | Frontend | Attendance Button Toggle | Click "Check In" button | Status updates to "Checked In"; button toggles to "Check Out" | UI reflects updated state without reload | Planned |
| UI-004 | Frontend | Leave Form Date Validation | Select end date earlier than start date | UI shows inline error banner; submit disabled | Submit button disabled attribute | Planned |
| UI-005 | Frontend | REWIND DAG Visualizer | View failed workflow on `/rewind` | Renders interactive DAG nodes with color codes | Assert node count matches workflow steps | Planned |
| UI-006 | Frontend | REWIND Confirmation Modal | Click "Execute Recovery" | Warning modal displays list of compensations; requires typed confirmation | Recovery API dispatched only after confirm | Planned |

---

## 21. Test File Implementation Contracts

| Test File Path | Owner | Framework | Target Under Test | Execution Command |
|---|---|---|---|---|
| `backend/.../auth/AuthServiceTest.java` | Person 1 | JUnit 5 + Mockito | `AuthService.java` (login, register, token validation) | `./mvnw test -Dtest=AuthServiceTest` |
| `backend/.../employee/EmployeeServiceTest.java` | Person 1 | JUnit 5 + Mockito | `EmployeeService.java` (uniqueness, CRUD, status) | `./mvnw test -Dtest=EmployeeServiceTest` |
| `backend/.../attendance/AttendanceServiceTest.java` | Person 2 | JUnit 5 + Mockito | `AttendanceService.java` (duplicate check-in, duration) | `./mvnw test -Dtest=AttendanceServiceTest` |
| `backend/.../leave/LeaveServiceTest.java` | Person 2 | JUnit 5 + Mockito | `LeaveService.java` (balances, overlapping, approval) | `./mvnw test -Dtest=LeaveServiceTest` |
| `backend/.../payroll/PayrollServiceTest.java` | Person 2 | JUnit 5 + Mockito | `PayrollService.java` (calculations, locking, idempotency) | `./mvnw test -Dtest=PayrollServiceTest` |
| `backend/.../distributed/TokenBucketServiceTest.java` | Person 3 | JUnit 5 + Embedded Redis | `TokenBucketService.java` (Lua execution, concurrency) | `./mvnw test -Dtest=TokenBucketServiceTest` |
| `backend/.../distributed/ConsistentHashRouterTest.java` | Person 3 | JUnit 5 | `ConsistentHashRouter.java` (ring stability, vnodes) | `./mvnw test -Dtest=ConsistentHashRouterTest` |
| `backend/.../distributed/SnowflakeIdGeneratorTest.java` | Person 3 | JUnit 5 | `SnowflakeIdGenerator.java` (concurrency, rollback) | `./mvnw test -Dtest=SnowflakeIdGeneratorTest` |
| `backend/.../rewind/WorkflowRecoveryServiceTest.java` | Person 3 | JUnit 5 + Mockito | `RecoveryPlanner.java`, `RecoveryExecutor.java` | `./mvnw test -Dtest=WorkflowRecoveryServiceTest` |
| `frontend/src/test/client.test.js` | Person 4 | Vitest | `api/client.js` (header injection, error handling) | `npm test -- client.test.js` |
| `frontend/src/test/WorkflowRecovery.test.jsx` | Person 4 | React Testing Library | `RewindDashboard.jsx` (preview, confirmation) | `npm test -- WorkflowRecovery.test.jsx` |

---

## 22. Local Setup and Execution

### 22.1 Prerequisites
- **JDK:** OpenJDK 17 LTS (verify with `java -version`)
- **Maven:** 3.9+ (or use repository `./mvnw`)
- **Node.js:** 20.x LTS + `npm` 10+ (verify with `node -v`)
- **Docker & Docker Compose:** Docker Desktop 24+ with Compose v2

### 22.2 Manual Local Run (Without Docker)

#### 1. Start MongoDB and Redis
```bash
# Terminal 1: Run local Redis
redis-server --port 6379

# Terminal 2: Run local MongoDB
mongod --dbpath /path/to/data/db
```

#### 2. Run Backend
```bash
cd backend
# Set local environment variables
export SPRING_DATA_MONGODB_URI="mongodb://localhost:27017/worklyn_db"
export SPRING_DATA_REDIS_HOST="localhost"
export SPRING_DATA_REDIS_PORT=6379
export APP_JWT_SECRET="9f8a3c8d1e2b4f5a6b7c8d9e0f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a"

# Run tests
./mvnw clean test

# Start application
./mvnw spring-boot:run
```
Backend will listen at `http://localhost:8080`.

#### 3. Run Frontend
```bash
cd frontend
npm install
npm run dev
```
Frontend development server will listen at `http://localhost:5173`.

---

## 23. Docker and Deployment

### 23.1 Coordinated Container Startup
To build and launch all containers (MongoDB, Redis, Spring Boot Backend, Frontend, and Nginx reverse proxy):

```bash
# From repository root
docker compose up --build -d
```

### 23.2 Checking Health & Logs
```bash
# View running container status
docker compose ps

# View backend application logs
docker compose logs -f backend

# View reverse proxy access logs
docker compose logs -f nginx
```

### 23.3 Access Points in Docker Mode
- **Application Portal:** `http://localhost` (served via Nginx port 80)
- **Direct Backend API:** `http://localhost:8080`
- **MongoDB:** `localhost:27017`
- **Redis CLI:** `redis-cli -h localhost -p 6379 -a redis_secret`

### 23.4 Teardown
```bash
# Stop and remove containers, preserving data volumes
docker compose down

# Stop and purge all persistent database volumes
docker compose down -v
```

---

## 24. Postman Collection

The repository includes a ready-to-run Postman collection located in `postman/`:
- `Worklyn_Master_Collection.postman_collection.json`
- `Worklyn_Environment.postman_environment.json`

### 24.1 Folder Organization
1. `01-Authentication`: Login (saves `jwt_token` to environment), Admin User Register, Current User profile.
2. `02-Employee-Management`: Create Employee, Get Profile, Update Details, Search by Department.
3. `03-Attendance`: Check-In (idempotent), Check-Out, History Query.
4. `04-Leave-Management`: Apply Leave, Query Balance, Approve Leave, Cancel Leave.
5. `05-Payroll`: Run Monthly Payroll (idempotent), Finalize Period, View Payslip.
6. `06-Distributed-Modules`: Rate Limit Burst Test, Short-Link Create & Redirect, Autocomplete Query.
7. `07-HR-REWIND-Engine`: Trigger Cascade Workflow, Injected Fault Execution, Preview Recovery Plan, Execute Recovery.

### 24.2 Automated Assertions
Every request in the collection contains automated test scripts:
```javascript
pm.test("Status code is 200 or 201", function () {
    pm.expect(pm.response.code).to.be.oneOf([200, 201]);
});

pm.test("Response matches standard schema", function () {
    pm.response.to.have.jsonBody();
});
```

---

## 25. Git Workflow and Integration Sequence

The team follows a strict Git feature-branch workflow:

```mermaid
gitGraph
    commit id: "Init Repo and Contracts"
    branch p1-backend-auth
    checkout main
    branch p2-core-hr
    checkout main
    branch p3-distributed-rewind
    checkout main
    branch p4-frontend-infra
    checkout p1-backend-auth
    commit id: "Auth and Employee CRUD"
    checkout main
    merge p1-backend-auth
    checkout p2-core-hr
    commit id: "Attendance Leave Payroll"
    checkout main
    merge p2-core-hr
    checkout p3-distributed-rewind
    commit id: "Cache RateLimiter REWIND"
    checkout main
    merge p3-distributed-rewind
    checkout p4-frontend-infra
    commit id: "UI Nginx Compose Postman"
    checkout main
    merge p4-frontend-infra
    commit id: "Final Integration and DoD Audit"
```

### 11-Step Phased Implementation Plan
1. **Phase 1: Contract Lock (Day 1):** All members review and sign off on Section 8 Shared Contracts.
2. **Phase 2: Person 1 Spring Boot Core (Days 2-4):** Person 1 commits base config, `ApiErrorResponse`, `JwtTokenProvider`, and `Employee` CRUD.
3. **Phase 3: Person 2 Core HR Workflows (Days 4-8):** Person 2 builds Attendance, Leave, and Payroll modules based on Person 1's employee model.
4. **Phase 4: Person 3 Distributed Algorithms (Days 4-8):** Person 3 builds Redis Cache abstraction, Token-Bucket Rate Limiter, Consistent Hash Ring, and Snowflake generator.
5. **Phase 5: Person 3 HR-REWIND Integration (Days 8-10):** Person 3 connects the recovery engine to Person 2's Leave and Payroll state machines.
6. **Phase 6: Person 4 Frontend Core (Days 4-8):** Person 4 builds UI layout, `AuthContext`, and API client using mocked JSON matching contracts.
7. **Phase 7: Person 4 Page Integration (Days 8-11):** Connects React UI to live backend REST endpoints.
8. **Phase 8: Infrastructure Orchestration (Day 11):** Person 4 writes `docker-compose.yml` and `nginx.conf`; verifies container networking.
9. **Phase 9: Cross-Module Integration Testing (Day 12):** All four members execute the Postman Master Collection and resolve API contract discrepancies.
10. **Phase 10: Fault-Injection & Recovery Demo (Day 13):** Test HR-REWIND recovery live against injected failures.
11. **Phase 11: Final Sign-off (Day 14):** Audit against Definition of Done checklist.

---

## 26. Definition of Done

A file or feature is considered **Done** only when all of the following criteria are met:

- [ ] **Documented Specification:** Exact file path, owner, purpose, public methods, and parameters match this README.
- [ ] **Validation Enforced:** Spring validation (`@Valid`, `@NotNull`, `@Size`, `@Positive`) or frontend form validation implemented.
- [ ] **Security Bound:** Explicit authorization annotations (`@PreAuthorize`) enforced on every endpoint. No hardcoded credentials.
- [ ] **Error Contract Adhered:** Failures return uniform `ApiErrorResponse` with appropriate HTTP status codes (400, 401, 403, 404, 409, 429).
- [ ] **Idempotency Guarded:** Financial mutations and recovery operations require and enforce `X-Idempotency-Key`.
- [ ] **Automated Test Coverage:** Accompanying unit/integration test class exists with positive, negative, and edge test cases matching the Test Catalogue IDs.
- [ ] **Tested in Container:** Code builds and executes cleanly inside Docker without host-specific path assumptions.
- [ ] **Zero Unhandled Leaks:** No stack traces or internal database connection strings exposed to API callers.

---

## 27. Known Limitations and Future Improvements

1. **MongoDB Transactions:** Multi-document ACID transactions require a MongoDB replica set. In standalone local development instances, atomic operations are constrained to single documents unless a replica set is initialized.
2. **NTP Clock Rollback:** The Snowflake ID generator protects against small clock drifts (< 10ms) by spinning. Sustained clock drift > 10ms halts ID generation to guarantee strict uniqueness.
3. **Physical Sharding:** The Consistent Hashing ring is currently an application-level tenant and partition router; integrating with MongoDB's native cluster mongos balancer is a future improvement.
4. **Asynchronous Notification Delivery:** Email and SMS delivery during leave approval or payroll finalization is planned for a future release using a dedicated background worker queue.

---

## 28. Verified Implementation Status Summary

| Area | Planned Files | Implemented Files | Target Tests | Status |
|---|---|---|---|---|
| **Backend Foundation (Person 1)** | 18 | 0 | 25 | **Planned (Specification Ready)** |
| **Core HR Workflows (Person 2)** | 15 | 0 | 30 | **Planned (Specification Ready)** |
| **Distributed & HR-REWIND (Person 3)**| 22 | 0 | 45 | **Planned (Specification Ready)** |
| **Frontend & Infra (Person 4)** | 28 | 0 | 20 | **Planned (Specification Ready)** |
| **Total Stack** | **83** | **0** | **120** | **Ready for Team Implementation** |

> **Ready for implementation! Team members (Person 1, Person 2, Person 3, Person 4) can now open their assigned files and begin coding against these specifications.**
