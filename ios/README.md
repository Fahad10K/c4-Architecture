# Pizza Delivery - iOS App

A native iOS application for the Pizza Delivery Platform, built with **SwiftUI** and **MVVM architecture**.

## Tech Stack

| Component | Technology |
|-----------|-----------|
| **Language** | Swift 5.9 |
| **UI Framework** | SwiftUI |
| **Architecture** | MVVM (Model-View-ViewModel) |
| **Navigation** | NavigationStack (iOS 17+) |
| **Networking** | URLSession (async/await) |
| **WebSocket** | Starscream |
| **Min iOS** | iOS 17.0 |
| **State Management** | @StateObject, @EnvironmentObject, @Published |

## Features

### Customer Features
- **User Registration & Login** - JWT-based authentication (Cognito-simulated)
- **Home Screen** - Offers, recommendations, nearby stores
- **Store Locator** - Browse and search stores
- **Menu Browsing** - Categories, items, customizations
- **Cart Management** - Add/remove items, apply coupons
- **Checkout & Payment** - Multiple payment methods, address selection
- **Order Tracking** - Real-time delivery tracking via WebSocket
- **Order History** - Active and past orders
- **Notifications** - Push notifications with read/unread state
- **AI Chatbot** - RAG-powered assistant (Bedrock/Claude)
- **Search** - Full-text search across menu and stores
- **Profile Management** - Edit profile, manage addresses

### Admin Features
- **Dashboard** - Key metrics overview
- **Order Management** - Filter, view, manage all orders
- **User Management** - View and manage users
- **Store Management** - Monitor store status
- **Analytics** - Revenue charts, top items, customer growth

## Project Structure

```
ios/PizzaDelivery/
├── Package.swift                  # SPM dependencies
└── Sources/
    ├── PizzaDeliveryApp.swift     # App entry point
    ├── ContentView.swift          # Root view + tab bar
    ├── Config/
    │   └── AppConfig.swift        # API endpoints, URLs
    ├── Models/                    # Data models (Codable)
    │   ├── User.swift
    │   ├── Store.swift
    │   ├── MenuItem.swift
    │   ├── Cart.swift
    │   ├── Order.swift
    │   ├── Delivery.swift
    │   ├── Notification.swift
    │   ├── ChatMessage.swift
    │   └── Recommendation.swift
    ├── Services/                  # API & networking layer
    │   ├── APIClient.swift        # HTTP client with JWT
    │   ├── WebSocketService.swift # Real-time connections
    │   ├── AuthService.swift
    │   ├── StoreService.swift
    │   ├── MenuService.swift
    │   ├── CartService.swift
    │   ├── OrderService.swift
    │   ├── PaymentService.swift
    │   ├── DeliveryService.swift
    │   ├── NotificationService.swift
    │   ├── SearchService.swift
    │   ├── ChatbotService.swift
    │   ├── RecommendationService.swift
    │   └── AdminService.swift
    ├── ViewModels/                # MVVM ViewModels
    │   ├── AuthViewModel.swift
    │   ├── HomeViewModel.swift
    │   ├── StoreViewModel.swift
    │   ├── MenuViewModel.swift
    │   ├── CartViewModel.swift
    │   ├── OrderViewModel.swift
    │   ├── DeliveryViewModel.swift
    │   ├── NotificationViewModel.swift
    │   ├── ChatbotViewModel.swift
    │   ├── SearchViewModel.swift
    │   └── AdminViewModel.swift
    ├── Views/                     # SwiftUI Views
    │   ├── Auth/
    │   ├── Home/
    │   ├── Stores/
    │   ├── Menu/
    │   ├── Cart/
    │   ├── Checkout/
    │   ├── Orders/
    │   ├── Tracking/
    │   ├── Notifications/
    │   ├── Chatbot/
    │   ├── Search/
    │   ├── Profile/
    │   └── Admin/
    ├── Components/                # Reusable UI components
    │   ├── LoadingView.swift
    │   ├── ErrorView.swift
    │   ├── MenuItemCard.swift
    │   ├── StoreCard.swift
    │   └── OrderCard.swift
    └── Extensions/
        ├── Color+Extensions.swift
        └── Date+Extensions.swift
```

## Backend Connection

The app connects to the shared backend API:
- **Base URL**: `http://localhost:3001/api/v1`
- **WebSocket**: `ws://localhost:3001`
- Configure in `Config/AppConfig.swift`

For physical device testing, update the base URL to your machine's IP address.

## Building & Running

### Requirements
- Xcode 15.0+
- iOS 17.0+ Simulator or Device
- Swift 5.9+

### Steps
1. Open `ios/PizzaDelivery/` in Xcode
2. Select `Package.swift` to resolve SPM dependencies
3. Select target device/simulator
4. Build and Run (⌘+R)

### Backend Setup
Ensure the backend is running at `localhost:3001`:
```bash
cd backend
./mvnw spring-boot:run
```

## API Integration

All API calls use JWT authentication with automatic token refresh. The `APIClient` handles:
- Token storage in UserDefaults
- Automatic `Authorization: Bearer` header injection
- 401 → token refresh → retry flow
- JSON encoding/decoding with snake_case conversion

## Architecture Decisions

- **MVVM**: Clean separation of concerns with reactive SwiftUI bindings
- **Singleton Services**: Shared service instances for API calls
- **async/await**: Modern Swift concurrency for all network operations
- **WebSocket**: Real-time delivery tracking and chatbot via Starscream
- **NavigationStack**: Type-safe navigation (iOS 17+)
- **Environment Objects**: Shared state (auth, cart, notifications) across views
