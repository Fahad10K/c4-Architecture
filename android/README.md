# Pizza Delivery - Android App

Native Android application for the Pizza Delivery Platform built with **Kotlin** and **Jetpack Compose**.

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM with Clean Architecture
- **DI**: Hilt (Dagger)
- **Networking**: Retrofit + OkHttp
- **Real-time**: OkHttp WebSocket (STOMP-compatible)
- **Navigation**: Jetpack Navigation Compose
- **Image Loading**: Coil
- **State Management**: StateFlow + Compose State
- **Token Storage**: DataStore Preferences
- **Maps**: Google Maps Compose

## Features

- **Authentication** - Login/Register with JWT
- **Store Locator** - Browse and find nearby stores
- **Menu Browsing** - Categories, items, customizations
- **Cart Management** - Add/remove items, apply coupons
- **Checkout** - Address selection, payment methods
- **Order Tracking** - Real-time delivery tracking via WebSocket
- **Notifications** - Push & in-app notifications
- **AI Chatbot** - Chat with AI assistant (RAG-powered)
- **Search** - Full-text search across menu & stores (OpenSearch)
- **Profile** - Account management, addresses
- **Admin Panel** - Order management, analytics dashboard

## Setup

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34
- Google Maps API Key (for map features)

### Configuration

1. Clone the repo and switch to `feature/android-app` branch
2. Open the `android/` folder in Android Studio
3. Add your Google Maps API key in `local.properties`:
   ```
   MAPS_API_KEY=your_google_maps_api_key_here
   ```
4. The app connects to backend at `http://10.0.2.2:3001/api/v1/` (emulator localhost)
5. For physical device, update `BASE_URL` in `app/build.gradle.kts`

### Build & Run

```bash
# Debug build
./gradlew assembleDebug

# Run on connected device/emulator
./gradlew installDebug
```

## Architecture

```
app/src/main/java/com/pizzadelivery/android/
├── PizzaDeliveryApp.kt          # Application class (Hilt entry point)
├── MainActivity.kt              # Single Activity
├── data/
│   ├── api/                     # Retrofit API service, interceptors, WebSocket
│   ├── local/                   # DataStore token manager
│   ├── model/                   # Data classes (User, Store, Order, etc.)
│   └── repository/              # Repository layer (API abstraction)
├── di/                          # Hilt modules (Network, App)
├── ui/
│   ├── navigation/              # NavHost + Screen routes
│   ├── theme/                   # Material 3 theme, colors, typography
│   ├── auth/                    # Login, Register
│   ├── home/                    # Home screen with recommendations
│   ├── store/                   # Store list, store detail
│   ├── menu/                    # Menu browsing, item detail
│   ├── cart/                    # Cart management
│   ├── checkout/                # Checkout flow
│   ├── order/                   # Order list, order detail
│   ├── tracking/                # Real-time delivery tracking
│   ├── notification/            # Notifications
│   ├── chatbot/                 # AI chatbot
│   ├── search/                  # Search
│   ├── profile/                 # Profile, addresses
│   └── admin/                   # Admin dashboard
└── util/                        # Extensions, Resource sealed class
```

## API Connection

The app connects to the shared backend service (Spring Boot) as defined in `TEAM_COORDINATION.md`.

- **Base URL**: `http://10.0.2.2:3001/api/v1/` (Android emulator → host localhost)
- **WebSocket**: `ws://10.0.2.2:3001/ws`
- **Auth**: JWT Bearer token in Authorization header
