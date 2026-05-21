# Key Architecture Document (KAD)
## Pizza Delivery Platform on AWS

---

## 1. System Overview

| Attribute | Value |
|-----------|-------|
| **Project Name** | Pizza Delivery Platform |
| **Architecture Style** | Microservices (Modular Monolith for MVP) |
| **Deployment Target** | AWS ECS Fargate / EKS |
| **API Style** | REST + WebSocket |
| **Authentication** | JWT + AWS Cognito |
| **CI/CD** | Docker + docker-compose |

---

## 2. C4 Model Mapping

### Level 1 – System Context

| Actor | Description | Interface |
|-------|-------------|-----------|
| **Consumers** | Place orders, track delivery, manage accounts | Web App, Mobile App (iOS/Android) |
| **Store / Corporate Users** | Manage stores, orders, menu, reports | Admin Control App, Staff Portal |
| **Delivery Partners** | Accept deliveries, update location, mark delivery | Driver App (Mobile) |
| **Marketing / CMS Team** | Manage content in SeeEmLess CMS | CMS Dashboard |
| **Operations / BI** | Reports & Analytics | PowerBI Integration |

### Level 2 – Container Diagram

| Container | Technology | Purpose |
|-----------|-----------|---------|
| **Web App** | React 18 + TypeScript + Vite + TailwindCSS | Responsive consumer web interface |
| **iOS App** | SwiftUI + MVVM + WebSocket | Native iOS consumer app |
| **Android App** | Kotlin + Jetpack Compose | Native Android consumer app |
| **API Gateway** | Amazon API Gateway (REST + WebSocket) | Single entry point, rate limiting, auth |
| **Backend Microservices** | Spring Boot 3 + Java 17 | Business logic layer |
| **Database** | Amazon RDS (PostgreSQL) | Persistent data storage |
| **Cache** | Amazon ElastiCache (Redis) | Sessions, cart, caching |
| **Search** | Amazon OpenSearch + Kendra | Full-text & knowledge search |
| **Storage** | Amazon S3 | Media, static assets, analytics exports |
| **CDN** | Amazon CloudFront | Static asset delivery |
| **DNS** | Amazon Route 53 | Domain routing |
| **Security** | AWS WAF + Shield | DDoS protection, Web ACL |

---

## 3. Microservices Architecture (Level 3)

### 3.1 Account Service
| Aspect | Detail |
|--------|--------|
| **Responsibility** | Profile management, addresses, preferences |
| **Controller** | `AccountController.java` |
| **Service** | `AccountService.java` |
| **Endpoints** | `GET/PUT /account/profile`, `GET/POST/DELETE /account/addresses` |
| **AWS Integration** | Cognito (user pool sync) |

### 3.2 Auth Service
| Aspect | Detail |
|--------|--------|
| **Responsibility** | Registration, login, JWT token management |
| **Controller** | `AuthController.java` |
| **Service** | `AuthService.java` |
| **Endpoints** | `POST /auth/register`, `POST /auth/login`, `POST /auth/refresh` |
| **AWS Integration** | Cognito (AuthN/AuthZ) |
| **Security** | BCrypt password hashing, JWT access + refresh tokens |

### 3.3 Store Service
| Aspect | Detail |
|--------|--------|
| **Responsibility** | Store locator, details, hours, availability |
| **Controller** | `StoreController.java` |
| **Service** | `StoreService.java` |
| **Endpoints** | `GET /stores`, `GET /stores/{id}`, `GET /stores/nearby` |
| **Data** | Store entity with lat/lng, city, delivery radius, fees |

### 3.4 Menu Service
| Aspect | Detail |
|--------|--------|
| **Responsibility** | Menu items, categories, offers, pricing |
| **Controller** | `MenuController.java` |
| **Service** | `MenuService.java` |
| **Endpoints** | `GET /stores/{id}/menu`, `GET /menu/items/{id}` |
| **Data** | MenuItem with customizations, tags, calories, images |

### 3.5 Cart Service
| Aspect | Detail |
|--------|--------|
| **Responsibility** | Manage cart, apply coupons, price calculation |
| **Controller** | `CartController.java` |
| **Service** | `CartService.java` |
| **Endpoints** | `GET/POST/PUT/DELETE /cart`, `POST /cart/coupon` |
| **AWS Integration** | ElastiCache (Redis) for session-based cart |

### 3.6 Order Service
| Aspect | Detail |
|--------|--------|
| **Responsibility** | Order lifecycle, history, reorder |
| **Controller** | `OrderController.java` |
| **Service** | `OrderService.java` |
| **Endpoints** | `POST /orders`, `GET /orders`, `GET /orders/{id}`, `POST /orders/{id}/cancel` |
| **Sub-Components** | Order Domain Model, Order Repository, Status History |

### 3.7 Order Orchestration Service
| Aspect | Detail |
|--------|--------|
| **Responsibility** | State machine, status transitions, event publishing |
| **Service** | `OrderOrchestrationService.java` |
| **AWS Integration** | **Amazon EventBridge** (event bus for order events) |
| **Pattern** | State Machine with valid transition enforcement |
| **Events Published** | `order.placed`, `order.confirmed`, `order.ready`, `order.delivered`, `order.cancelled` |

### 3.8 Payment Service
| Aspect | Detail |
|--------|--------|
| **Responsibility** | Payment processing, refunds, webhooks |
| **Controller** | `PaymentController.java` |
| **Service** | `PaymentService.java` |
| **Endpoints** | `POST /payments/initiate`, `POST /payments/confirm`, `POST /payments/refund`, `POST /payments/webhook` |
| **AWS Integration** | EventBridge (payment events) |
| **External** | **Stripe** (primary), **Adyen** (configurable) |
| **Compliance** | PCI-DSS via payment gateway tokenization |

### 3.9 Delivery Service
| Aspect | Detail |
|--------|--------|
| **Responsibility** | Delivery tracking, ETA calculation, driver management |
| **Controllers** | `DeliveryController.java`, `DriverController.java` |
| **Service** | `DeliveryService.java` |
| **Endpoints** | `GET /delivery/{orderId}`, `PUT /delivery/{orderId}/location`, `POST /driver/deliveries/{id}/pickup`, `POST /driver/deliveries/{id}/delivered` |
| **Features** | Haversine distance calculation, real-time GPS via WebSocket |
| **AWS Integration** | WebSocket API Gateway for real-time tracking |

### 3.10 Notification Service
| Aspect | Detail |
|--------|--------|
| **Responsibility** | Push notifications, email, SMS, in-app alerts |
| **Controller** | `NotificationController.java` |
| **Service** | `NotificationService.java` |
| **Endpoints** | `GET /notifications`, `PUT /notifications/{id}/read`, `PUT /notifications/read-all` |
| **AWS Integration** | **Amazon SES** (email), **Amazon SNS** (SMS), WebSocket (push) |
| **Templates** | HTML email templates with Pizza Palace branding |

### 3.11 Chatbot Service (RAG)
| Aspect | Detail |
|--------|--------|
| **Responsibility** | AI-powered pizza assistant, intent handling, conversation |
| **Controller** | `ChatbotController.java` |
| **Service** | `ChatbotService.java` |
| **Endpoints** | `POST /chatbot/message`, `GET /chatbot/history`, `DELETE /chatbot/history` |
| **AWS Integration** | **Amazon Bedrock** (Claude LLM), **Amazon Kendra** (RAG knowledge retrieval) |
| **Architecture** | RAG Pipeline: User Query → Kendra Retrieval → Context Augmentation → Bedrock LLM → Response |
| **Fallback** | Built-in knowledge base when AWS services unavailable |

### 3.12 Recommendation Service
| Aspect | Detail |
|--------|--------|
| **Responsibility** | Personalized offers, next-best-item, AI recommendations |
| **Controller** | `RecommendationController.java` |
| **Service** | `RecommendationService.java` |
| **Endpoints** | `GET /recommendations`, `GET /recommendations/offers` |
| **AWS Integration** | **Amazon Bedrock** (AI-powered personalization based on order history) |
| **Fallback** | Popularity-based recommendations when AI unavailable |

### 3.13 Search Service
| Aspect | Detail |
|--------|--------|
| **Responsibility** | Global search for menu items, stores, knowledge base |
| **Controller** | `SearchController.java` |
| **Service** | `SearchService.java` |
| **Endpoints** | `GET /search?q=`, `GET /search/suggestions` |
| **AWS Integration** | **Amazon Kendra** (knowledge search), **OpenSearch** (full-text) |
| **Fallback** | Database LIKE queries when Kendra/OpenSearch unavailable |

### 3.14 Admin Service
| Aspect | Detail |
|--------|--------|
| **Responsibility** | User & role management, feature flags, system health |
| **Controller** | `AdminController.java`, `StaffController.java` |
| **Service** | `AdminService.java` |
| **Endpoints** | `GET/PUT /admin/users`, `GET/PUT /admin/feature-flags`, `GET /admin/health`, `GET /admin/dashboard`, `GET /admin/reports` |
| **RBAC** | Role-based access: CUSTOMER, STAFF, DRIVER, ADMIN |
| **Features** | Feature flag system, user activation/deactivation |

### 3.15 Analytics Service
| Aspect | Detail |
|--------|--------|
| **Responsibility** | Metrics, reporting, event tracking, data export |
| **Service** | `AnalyticsService.java` |
| **Endpoints** | Via AdminController: `GET /admin/dashboard`, `GET /admin/reports` |
| **AWS Integration** | **Amazon CloudWatch** (metrics publishing), **Amazon S3** (report export) |
| **Metrics** | TotalOrders, ActiveOrders, TotalRevenue, custom dimensions |

---

## 4. AWS Services Integration Matrix

| AWS Service | SDK Module | Used By | Purpose |
|-------------|-----------|---------|---------|
| **Amazon S3** | `s3` | AnalyticsService, Media | Media storage, analytics data lake |
| **Amazon SQS** | `sqs` | OrderService | Message queues for async processing |
| **Amazon SNS** | `sns` | NotificationService | SMS notifications |
| **Amazon SES** | `ses` | NotificationService | Email notifications (HTML templates) |
| **Amazon Cognito** | `cognitoidentityprovider` | AuthService | User pool authentication |
| **Amazon EventBridge** | `eventbridge` | OrderOrchestrationService, PaymentService | Event-driven architecture |
| **Amazon Bedrock** | `bedrockruntime` | ChatbotService, RecommendationService | LLM (Claude) for AI features |
| **Amazon Kendra** | `kendra` | ChatbotService, SearchService | RAG knowledge retrieval |
| **Amazon CloudWatch** | `cloudwatch` | AnalyticsService | Metrics & monitoring |
| **Amazon OpenSearch** | `opensearch` | SearchService | Full-text search indexing |
| **Amazon Polly** | `polly` | ChatbotService (future) | Text-to-speech |
| **Amazon Transcribe** | `transcribe` | ChatbotService (future) | Speech-to-text |
| **AWS Secrets Manager** | `secretsmanager` | AwsConfig | Secure credentials storage |
| **AWS CloudFront** | Infrastructure | Frontend | CDN for static assets |
| **AWS WAF + Shield** | Infrastructure | API Gateway | DDoS protection |
| **Amazon Route 53** | Infrastructure | DNS | Domain routing |
| **Amazon RDS** | JDBC | All Services | PostgreSQL database |
| **Amazon ElastiCache** | Spring Data Redis | CartService, Sessions | Redis caching layer |

---

## 5. Data Layer

### 5.1 Entities (JPA / PostgreSQL)

| Entity | Key Fields | Relationships |
|--------|-----------|---------------|
| `User` | id, email, name, phone, role, isActive | Orders, Addresses, Notifications |
| `Store` | id, name, city, lat, lng, rating, deliveryFee | MenuItems, Orders |
| `MenuItem` | id, name, description, price, category, tags | Store, OrderItems |
| `Category` | id, name, storeId | MenuItems |
| `Cart` | id, userId, storeId, discount, couponCode | CartItems |
| `CartItem` | id, menuItemId, quantity, customizations | Cart |
| `Order` | id, orderNumber, userId, storeId, status, total | OrderItems, Payment, Delivery |
| `OrderItem` | id, orderId, menuItemId, name, quantity, price | Order |
| `OrderStatusHistory` | id, orderId, status, note, createdAt | Order |
| `Payment` | id, orderId, stripePaymentId, status, amount | Order |
| `Delivery` | id, orderId, driverId, status, currentLat/Lng | Order, Driver |
| `Notification` | id, userId, type, title, message, isRead | User |
| `Address` | id, userId, label, street, city, lat, lng | User |

### 5.2 Enums

| Enum | Values |
|------|--------|
| `UserRole` | CUSTOMER, ADMIN, STAFF, STORE_STAFF, DRIVER, DELIVERY_PARTNER |
| `OrderStatus` | PLACED, CONFIRMED, PREPARING, READY, PICKED_UP, OUT_FOR_DELIVERY, ON_THE_WAY, DELIVERED, CANCELLED |
| `PaymentStatus` | PENDING, PROCESSING, COMPLETED, FAILED, REFUNDED |
| `DeliveryStatus` | PENDING, ASSIGNED, PICKED_UP, IN_TRANSIT, ON_THE_WAY, DELIVERED, FAILED |
| `NotificationType` | ORDER_UPDATE, PROMOTION, SYSTEM, DELIVERY |

---

## 6. Client Applications

### 6.1 Web Application (React)

| Attribute | Value |
|-----------|-------|
| **Framework** | React 18 + TypeScript |
| **Build Tool** | Vite |
| **State Management** | Redux Toolkit |
| **Styling** | TailwindCSS |
| **Routing** | React Router v6 |
| **API Base** | `http://localhost:3001/api/v1` |

**Pages (15 total):**
| Page | Route | Description |
|------|-------|-------------|
| Home | `/` | Landing with featured stores & recommendations |
| Login | `/login` | Email/password authentication |
| Register | `/register` | New user registration |
| Stores | `/stores` | Store listing with location filter |
| Menu | `/stores/:id/menu` | Store menu with categories |
| Search | `/search` | Global search (menu items, stores) |
| Cart | `/cart` | Cart management with coupon support |
| Checkout | `/checkout` | Address, payment method, order summary |
| Orders | `/orders` | Order history list |
| Order Tracking | `/orders/:id/track` | Real-time delivery tracking |
| Profile | `/profile` | Account settings, addresses |
| Notifications | `/notifications` | In-app notification center |
| Chatbot | `/chatbot` | AI-powered pizza assistant |
| Admin Dashboard | `/admin` | Admin metrics, system health |
| Admin Analytics | `/admin/analytics` | Reports with period filter |

### 6.2 iOS Application (SwiftUI)

| Attribute | Value |
|-----------|-------|
| **Framework** | SwiftUI + MVVM |
| **Networking** | URLSession + async/await |
| **WebSocket** | Starscream |
| **API Base** | `http://localhost:3001/api/v1` |

**Views (22 total):**
Auth (Login, Register), Home, Stores (List, Detail), Menu (List, ItemDetail), Cart, Checkout, Orders (List, Detail), Tracking (DeliveryTracking), Notifications, Chatbot, Search, Profile (Profile, Addresses), Admin (Dashboard, Analytics, Orders, Stores, Users)

### 6.3 Android Application (Kotlin)

| Attribute | Value |
|-----------|-------|
| **Framework** | Jetpack Compose |
| **Architecture** | MVVM + Repository Pattern |
| **API Base** | `http://localhost:3001/api/v1` |

---

## 7. Security Architecture

| Layer | Implementation |
|-------|---------------|
| **Transport** | TLS 1.3 (HTTPS enforced) |
| **Authentication** | JWT (15min access + 7day refresh) + Cognito |
| **Authorization** | Spring Security `@PreAuthorize` with role-based access |
| **API Protection** | AWS WAF (Web ACL), Shield (DDoS) |
| **Secrets** | AWS Secrets Manager, env variables |
| **Payment** | PCI-DSS via Stripe tokenization (no card data stored) |
| **Data** | Encryption at rest (RDS, S3), in transit (TLS) |
| **CORS** | Configurable allowed origins |

---

## 8. Non-Functional Requirements (NFRs)

| NFR | Target | Implementation |
|-----|--------|---------------|
| **Availability** | 99.9%+ | Multi-AZ deployment, health checks |
| **Scalability** | 1000+ stores, auto-scaling | ECS Fargate auto-scaling, ElastiCache |
| **Performance** | < 200ms API response (p95) | Redis caching, CDN, optimized queries |
| **Security** | WAF, IAM, Encryption | AWS WAF, Secrets Manager, TLS |
| **Reliability** | Multi-AZ, backups | RDS Multi-AZ, S3 versioning, AWS Backup |
| **Observability** | Full stack monitoring | CloudWatch (metrics, logs, alarms), X-Ray (tracing) |
| **Compliance** | PCI-DSS (payments) | Stripe/Adyen gateway handles PCI scope |

---

## 9. Event-Driven Architecture

### EventBridge Events Published:

| Source | Event | Consumers |
|--------|-------|-----------|
| OrderOrchestrationService | `order.status_changed` | NotificationService, AnalyticsService |
| PaymentService | `payment.completed` | OrderService |
| PaymentService | `payment.failed` | NotificationService |
| PaymentService | `payment.refunded` | OrderService, NotificationService |

---

## 10. Deployment Architecture

```
┌─────────────────────────────────────────────────────┐
│                    AWS Cloud                          │
│                                                      │
│  Route53 → CloudFront → S3 (Static Web)            │
│                                                      │
│  Route53 → WAF → API Gateway → ECS Fargate         │
│                        │                             │
│              ┌─────────┼─────────────┐              │
│              │         │             │              │
│          RDS (PG)  ElastiCache   S3 (Media)        │
│              │                                      │
│         EventBridge → SNS/SES → Users              │
│              │                                      │
│         Bedrock + Kendra (AI/RAG)                   │
└─────────────────────────────────────────────────────┘
```

### Docker Compose (Local Development):
- `backend` → Spring Boot (port 3001)
- `frontend` → Nginx (port 80)
- `postgres` → PostgreSQL (port 5432)
- `redis` → Redis (port 6379)

---

## 11. API Contract Summary

**Base URL:** `http://localhost:3001/api/v1`

| Module | Prefix | Auth Required |
|--------|--------|---------------|
| Auth | `/auth` | No |
| Account | `/account` | Yes |
| Stores | `/stores` | No |
| Menu | `/stores/{id}/menu` | No |
| Cart | `/cart` | Yes |
| Orders | `/orders` | Yes |
| Payments | `/payments` | Yes |
| Delivery | `/delivery` | Yes |
| Notifications | `/notifications` | Yes |
| Chatbot | `/chatbot` | Yes |
| Recommendations | `/recommendations` | Yes |
| Search | `/search` | No |
| Staff | `/staff` | Yes (STAFF+) |
| Driver | `/driver` | Yes (DRIVER+) |
| Admin | `/admin` | Yes (ADMIN) |

---

## 12. AI/Chatbot (RAG) Stack

```
User Query (Text)
       │
       ▼
┌──────────────────┐
│  Chatbot Service │
│  (Orchestrator)  │
└────────┬─────────┘
         │
    ┌────┴────┐
    ▼         ▼
┌────────┐ ┌──────────────┐
│ Amazon │ │   Amazon     │
│ Kendra │ │   Bedrock    │
│ (RAG)  │ │ (Claude LLM) │
└────┬───┘ └──────┬───────┘
     │             │
     └──────┬──────┘
            ▼
    Augmented Response
            │
            ▼
       User (Text)
```

---

## 13. Technology Stack Summary

| Layer | Technology |
|-------|-----------|
| **Frontend Web** | React 18, TypeScript, Vite, TailwindCSS, Redux Toolkit |
| **Frontend iOS** | SwiftUI, Combine, Starscream (WebSocket) |
| **Frontend Android** | Kotlin, Jetpack Compose |
| **Backend** | Java 17, Spring Boot 3, Spring Security, Spring Data JPA |
| **Database** | PostgreSQL (RDS), H2 (dev) |
| **Cache** | Redis (ElastiCache) |
| **Search** | OpenSearch, Amazon Kendra |
| **AI/ML** | Amazon Bedrock (Claude), Amazon Kendra (RAG) |
| **Messaging** | Amazon EventBridge, SQS, SNS |
| **Email** | Amazon SES |
| **Storage** | Amazon S3 |
| **Monitoring** | Amazon CloudWatch, Spring Actuator, Micrometer |
| **API Docs** | SpringDoc OpenAPI / Swagger UI |
| **Containerization** | Docker, docker-compose |
| **Security** | JWT, BCrypt, AWS WAF, Cognito, Secrets Manager |

---

## 14. Repository Structure

```
c4/
├── backend/                    # Spring Boot Backend
│   ├── src/main/java/com/pizza/delivery/
│   │   ├── config/            # AWS, Security, WebSocket configs
│   │   ├── controller/        # 15 REST controllers
│   │   ├── service/           # 15 business services
│   │   ├── repository/        # JPA repositories
│   │   ├── entity/            # JPA entities
│   │   ├── dto/               # Data transfer objects
│   │   ├── enums/             # Status enums
│   │   ├── exception/         # Custom exceptions
│   │   └── security/          # JWT filter, auth provider
│   ├── src/main/resources/
│   │   ├── application.yml    # Configuration
│   │   └── data.sql           # Seed data
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                   # React Web App
│   ├── src/
│   │   ├── pages/             # 15 page components
│   │   ├── components/        # Shared UI components
│   │   ├── services/          # API client services
│   │   ├── store/             # Redux slices
│   │   └── types/             # TypeScript definitions
│   ├── Dockerfile
│   └── package.json
├── ios/                        # iOS SwiftUI App
│   └── PizzaDelivery/Sources/
│       ├── Views/             # 22 SwiftUI views (13 modules)
│       ├── Services/          # 13 API service clients
│       ├── Models/            # Data models
│       └── Config/            # App configuration
├── docker-compose.yml          # Local orchestration
└── KAD_Architecture_Document.md
```

---

*Document Version: 1.0*
*Last Updated: May 2025*
*Architecture: C4 Model (Level 1-3)*
