import Foundation

class DeliveryService {
    static let shared = DeliveryService()
    private let api = APIClient.shared
    
    private init() {}
    
    func getDeliveryTracking(orderId: String) async throws -> Delivery {
        try await api.get(AppConfig.Endpoints.deliveryTrack(orderId: orderId))
    }
}
