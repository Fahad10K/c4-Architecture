import Foundation

struct AdminDashboard: Codable {
    var totalOrders: Int
    var totalRevenue: Double
    var activeUsers: Int
    var totalStores: Int
    var pendingOrders: Int
    var todayOrders: Int
    var todayRevenue: Double
    var averageOrderValue: Double
}

struct AdminAnalytics: Codable {
    var ordersByStatus: [StatusCount]?
    var revenueByDay: [DayRevenue]?
    var topItems: [TopItem]?
    var customerGrowth: [GrowthPoint]?
}

struct StatusCount: Codable, Identifiable {
    var id: String { status }
    var status: String
    var count: Int
}

struct DayRevenue: Codable, Identifiable {
    var id: String { date }
    var date: String
    var revenue: Double
}

struct TopItem: Codable, Identifiable {
    var id: String { name }
    var name: String
    var orders: Int
    var revenue: Double
}

struct GrowthPoint: Codable, Identifiable {
    var id: String { date }
    var date: String
    var count: Int
}

class AdminService {
    static let shared = AdminService()
    private let api = APIClient.shared
    
    private init() {}
    
    func getDashboard() async throws -> AdminDashboard {
        try await api.get(AppConfig.Endpoints.analyticsDashboard)
    }
    
    func getAnalytics() async throws -> AdminAnalytics {
        try await api.get(AppConfig.Endpoints.analyticsReports)
    }
    
    func getUsers() async throws -> [User] {
        try await api.get(AppConfig.Endpoints.adminUsers)
    }
    
    func getOrders() async throws -> [Order] {
        try await api.get(AppConfig.Endpoints.adminOrders)
    }
    
    func getStores() async throws -> [Store] {
        try await api.get(AppConfig.Endpoints.adminStores)
    }
}
