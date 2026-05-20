# Pizza Delivery Platform - Team Coordination

## Project Overview
Building a **Pizza Delivery Platform** based on the WWT Digital Architect Challenge.  
Architecture follows C4 Model (Levels 1-4) on AWS infrastructure.

---

## Team Structure

| Role | Branch | Status | Agent |
|------|--------|--------|-------|
| **Team Lead + Web + Backend** | `feature/web` | 🟢 ACTIVE | Chat 1 |
| **Android App** | `feature/android-app` | � COMPLETE | Chat 2 |
| **iOS App** | `feature/ios-app` | 🔴 NOT STARTED | Chat 3 |

---

## Shared Backend API (Built by Web/Backend Agent)

Base URL: `http://localhost:3001/api/v1`

### API Endpoints (REST + WebSocket)

| Service | Endpoints | Port |
|---------|-----------|------|
| **API Gateway** | Central entry point | 3001 |
| **Auth Service** | POST /auth/register, POST /auth/login, POST /auth/refresh, POST /auth/logout | - |
| **Account Service** | GET/PUT /accounts/profile, GET/POST/PUT/DELETE /accounts/addresses | - |
| **Store Service** | GET /stores, GET /stores/:id, GET /stores/nearby | - |
| **Menu Service** | GET /stores/:storeId/menu, GET /menu/items/:id, GET /menu/categories | - |
| **Cart Service** | GET/POST/PUT/DELETE /cart, POST /cart/apply-coupon | - |
| **Order Service** | POST /orders, GET /orders, GET /orders/:id, PUT /orders/:id/cancel | - |
| **Payment Service** | POST /payments, GET /payments/:id, POST /payments/webhook | - |
| **Delivery Service** | GET /delivery/:orderId/track, WebSocket /delivery/track/:orderId | - |
| **Notification Service** | GET /notifications, PUT /notifications/:id/read, WebSocket /notifications | - |
| **Search Service** | GET /search?q=query | - |
| **Chatbot Service** | POST /chatbot/message, WebSocket /chatbot | - |
| **Recommendation Service** | GET /recommendations, GET /recommendations/offers | - |
| **Admin Service** | CRUD /admin/users, /admin/stores, /admin/orders, /admin/analytics | - |
| **Analytics Service** | GET /analytics/dashboard, GET /analytics/reports | - |

### Authentication
- JWT-based (simulating Amazon Cognito)
- Access Token + Refresh Token
- Tokens passed via `Authorization: Bearer <token>` header

### WebSocket Events
- `delivery:location_update` - Real-time driver location
- `delivery:status_change` - Order status changes
- `notification:new` - New notification push
- `chatbot:response` - AI chatbot responses

---

## Shared Data Models

### User
```json
{
  "id": "uuid",
  "email": "string",
  "name": "string",
  "phone": "string",
  "role": "customer | admin | store_staff | delivery_partner",
  "preferences": {},
  "addresses": [],
  "createdAt": "timestamp"
}
```

### Store
```json
{
  "id": "uuid",
  "name": "string",
  "address": {},
  "location": { "lat": 0, "lng": 0 },
  "hours": {},
  "isOpen": "boolean",
  "rating": "number"
}
```

### MenuItem
```json
{
  "id": "uuid",
  "storeId": "uuid",
  "name": "string",
  "description": "string",
  "price": "number",
  "category": "string",
  "image": "string",
  "customizations": [],
  "isAvailable": "boolean"
}
```

### Cart
```json
{
  "id": "uuid",
  "userId": "uuid",
  "storeId": "uuid",
  "items": [{ "menuItemId": "uuid", "quantity": 1, "customizations": [], "price": 0 }],
  "couponCode": "string",
  "subtotal": "number",
  "tax": "number",
  "deliveryFee": "number",
  "discount": "number",
  "total": "number"
}
```

### Order
```json
{
  "id": "uuid",
  "userId": "uuid",
  "storeId": "uuid",
  "items": [],
  "status": "placed | confirmed | preparing | ready | picked_up | on_the_way | delivered | cancelled",
  "payment": {},
  "delivery": {},
  "total": "number",
  "createdAt": "timestamp"
}
```

### Delivery
```json
{
  "orderId": "uuid",
  "driverId": "uuid",
  "status": "assigned | picked_up | on_the_way | delivered",
  "currentLocation": { "lat": 0, "lng": 0 },
  "eta": "number",
  "route": []
}
```

---

## Technology Stack

### Backend (Shared API) — Java + AWS
- **Language**: Java 17+
- **Framework**: Spring Boot 3.2.x
- **Web**: Spring Web (REST Controllers)
- **Data**: Spring Data JPA + Hibernate
- **Cache**: Spring Data Redis (Amazon ElastiCache)
- **Security**: Spring Security + JWT (Amazon Cognito integration)
- **Real-time**: Spring WebSocket + STOMP
- **Validation**: Jakarta Bean Validation
- **Build**: Maven
- **API Docs**: SpringDoc OpenAPI (Swagger)

### AWS Services (from C4 Diagrams)

#### Compute & Networking
- **Amazon ECS Fargate / EKS** — Microservices hosting (containerized Spring Boot)
- **Amazon API Gateway** — REST APIs + WebSocket APIs (central entry point)
- **Amazon Route 53** — DNS
- **Amazon CloudFront** — CDN (static assets, web app)
- **AWS WAF & Shield** — DDoS protection, Web ACL

#### Authentication & Authorization
- **Amazon Cognito** — User pools, AuthN/AuthZ, JWT tokens

#### Databases & Storage
- **Amazon RDS (PostgreSQL)** — Orders, Users, Stores, Payments
- **Amazon ElastiCache (Redis)** — Cart, Sessions, Cache
- **Amazon OpenSearch Service** — Search & Menu Index
- **Amazon S3** — Media storage, static assets, data lake
- **Amazon Kendra** — Knowledge index for RAG chatbot

#### Messaging & Events
- **Amazon SQS** — Message queues (order processing, async tasks)
- **Amazon SNS** — Push notifications, SMS, Email fan-out
- **Amazon EventBridge** — Event-driven orchestration between services

#### AI / Chatbot (RAG Stack)
- **Amazon Transcribe** — Speech to text
- **Amazon Bedrock (Claude / Titan)** — LLM for chatbot AI orchestrator
- **Amazon Kendra** — Enterprise search / knowledge index
- **Amazon Polly** — Text to speech

#### Analytics & Data Pipeline
- **AWS DMS / Glue** — Data ingestion
- **Amazon S3 Data Lake** — Raw & curated data
- **Amazon Athena** — Ad-hoc queries
- **Amazon QuickSight** — Reports & dashboards
- **PowerBI** — Operations team reporting

#### Observability, Security & Operations
- **Amazon CloudWatch** — Metrics, logs, alarms
- **AWS X-Ray** — Distributed tracing
- **AWS CloudTrail** — Audit logs
- **AWS Secrets Manager** — Secrets management
- **AWS KMS** — Encryption keys
- **AWS Backup** — Automated backups
- **AWS IAM** — Roles & policies

#### External Integrations
- **Stripe / Adyen** — Payment gateway (PCI-DSS compliant)
- **SeeEmLess CMS** — Headless CMS (REST/GraphQL) for content, menu, media
- **Twilio / SNS** — SMS provider
- **Amazon SES** — Email provider
- **Google Maps / Geo API** — Maps & geolocation
- **Third-party Delivery** — Fleet, tracking, driver updates

### Web Frontend
- **Framework**: React 18 + Vite
- **Routing**: React Router v6
- **State**: Redux Toolkit + RTK Query
- **UI**: Tailwind CSS + shadcn/ui
- **Icons**: Lucide React
- **Maps**: Leaflet (free alternative to Google Maps)
- **Real-time**: SockJS + STOMP.js Client
- **Charts**: Recharts (admin dashboard)

### Android App (Chat 2)
- **Language**: Kotlin
- **Framework**: Jetpack Compose
- **HTTP**: Retrofit + OkHttp
- **DI**: Hilt
- **Navigation**: Jetpack Navigation

### iOS App (Chat 3)
- **Language**: Swift
- **Framework**: SwiftUI
- **HTTP**: URLSession / Alamofire
- **Architecture**: MVVM
- **Navigation**: NavigationStack

---

## File Structure Convention

```
c4/
├── TEAM_COORDINATION.md          # This file
├── backend/                       # Java Spring Boot backend (Web agent owns)
│   ├── src/main/java/com/pizza/delivery/
│   │   ├── config/                # Security, WebSocket, Redis, CORS config
│   │   ├── controller/            # REST Controllers (one per microservice)
│   │   ├── dto/                   # Request/Response DTOs
│   │   ├── entity/                # JPA Entities
│   │   ├── enums/                 # Enums (OrderStatus, UserRole, etc.)
│   │   ├── exception/             # Custom exceptions + global handler
│   │   ├── repository/            # Spring Data JPA repositories
│   │   ├── security/              # JWT provider, filters
│   │   ├── service/               # Business logic services
│   │   ├── websocket/             # WebSocket handlers
│   │   └── PizzaDeliveryApplication.java
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── data.sql               # Seed data
│   ├── pom.xml
│   └── Dockerfile
├── web/                           # React web app (Web agent owns)
│   ├── src/
│   ├── package.json
│   └── Dockerfile
├── android/                       # Android app (Android agent owns)
├── ios/                           # iOS app (iOS agent owns)
└── docker-compose.yml             # Local dev orchestration
```

---

## Progress Log

### Web + Backend Agent (Chat 1) — Team Lead
| Date | Update |
|------|--------|
| 2025-05-20 | Created TEAM_COORDINATION.md, started backend + web setup |

### Android Agent (Chat 2)
| Date | Update |
|------|--------|
| 2025-05-20 | Created `feature/android-app` branch, full Android app implemented |
| 2025-05-20 | Tech: Kotlin + Jetpack Compose + Hilt + Retrofit + OkHttp WebSocket |
| 2025-05-20 | All screens complete: Auth, Home, Stores, Menu, Cart, Checkout, Orders, Tracking, Notifications, Chatbot, Search, Profile, Admin |
| 2025-05-20 | Connects to shared backend at `http://10.0.2.2:3001/api/v1/` (emulator) |

### iOS Agent (Chat 3)
| Date | Update |
|------|--------|
| - | Awaiting start |

---

## Rules for All Agents
1. **DO NOT** modify files outside your designated folder (`web/`, `android/`, `ios/`, `backend/`)
2. **DO** read this file before starting work
3. **DO** update the Progress Log section with your status
4. **DO** use the shared API endpoints defined above
5. **DO** follow the shared data models for consistency
6. Mobile apps connect to backend at `http://localhost:3001/api/v1` (configurable)
7. All apps must implement: Auth, Menu Browse, Cart, Checkout, Order Tracking, Notifications, Chatbot, Store Locator, Profile, Admin Panel

---

## Non-Functional Requirements (from C4 diagrams)
- **Availability**: 99.9%+
- **Scalability**: Auto Scaling (thousands of req/sec)
- **Security**: Encryption in transit/at rest, Least Privilege, JWT
- **Performance**: API Latency < 200ms, CDN, Caching
- **Reliability**: Multi-AZ, Backups, Disaster Recovery
- **Observability**: Monitoring, Logging, Alerting
- **Compliance**: PCI-DSS (via Payment Gateway), GDPR Ready
