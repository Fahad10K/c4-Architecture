import Foundation

class OrderService {
    static let shared = OrderService()
    private let api = APIClient.shared
    
    private init() {}
    
    func createOrder(request: CreateOrderRequest) async throws -> Order {
        try await api.post(AppConfig.Endpoints.orders, body: request)
    }
    
    func getOrders() async throws -> [Order] {
        try await api.get(AppConfig.Endpoints.orders)
    }
    
    func getOrder(id: String) async throws -> Order {
        try await api.get(AppConfig.Endpoints.orderDetail(id))
    }
    
    func cancelOrder(id: String) async throws -> Order {
        try await api.putEmpty(AppConfig.Endpoints.cancelOrder(id))
    }
}
