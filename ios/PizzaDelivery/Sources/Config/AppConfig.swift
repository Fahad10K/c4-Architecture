import Foundation

enum AppConfig {
    static let baseURL = "http://localhost:3001/api/v1"
    static let wsBaseURL = "ws://localhost:3001"
    
    enum Endpoints {
        // Auth
        static let register = "/auth/register"
        static let login = "/auth/login"
        static let refresh = "/auth/refresh"
        static let logout = "/auth/logout"
        
        // Account
        static let profile = "/accounts/profile"
        static let addresses = "/accounts/addresses"
        
        // Stores
        static let stores = "/stores"
        static func storeDetail(_ id: String) -> String { "/stores/\(id)" }
        static let nearbyStores = "/stores/nearby"
        
        // Menu
        static func menu(storeId: String) -> String { "/stores/\(storeId)/menu" }
        static func menuItem(_ id: String) -> String { "/menu/items/\(id)" }
        static let menuCategories = "/menu/categories"
        
        // Cart
        static let cart = "/cart"
        static let applyCoupon = "/cart/apply-coupon"
        
        // Orders
        static let orders = "/orders"
        static func orderDetail(_ id: String) -> String { "/orders/\(id)" }
        static func cancelOrder(_ id: String) -> String { "/orders/\(id)/cancel" }
        
        // Payments
        static let payments = "/payments"
        static func paymentDetail(_ id: String) -> String { "/payments/\(id)" }
        
        // Delivery
        static func deliveryTrack(orderId: String) -> String { "/delivery/\(orderId)/track" }
        
        // Notifications
        static let notifications = "/notifications"
        static func readNotification(_ id: String) -> String { "/notifications/\(id)/read" }
        
        // Search
        static let search = "/search"
        
        // Chatbot
        static let chatbotMessage = "/chatbot/message"
        
        // Recommendations
        static let recommendations = "/recommendations"
        static let offers = "/recommendations/offers"
        
        // Admin
        static let adminUsers = "/admin/users"
        static let adminStores = "/admin/stores"
        static let adminOrders = "/admin/orders"
        static let adminAnalytics = "/admin/analytics"
        
        // Analytics
        static let analyticsDashboard = "/analytics/dashboard"
        static let analyticsReports = "/analytics/reports"
    }
    
    enum WebSocket {
        static func deliveryTrack(orderId: String) -> String { "\(wsBaseURL)/delivery/track/\(orderId)" }
        static let notifications = "\(wsBaseURL)/notifications"
        static let chatbot = "\(wsBaseURL)/chatbot"
    }
}
