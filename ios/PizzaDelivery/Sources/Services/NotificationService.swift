import Foundation

class NotificationServiceAPI {
    static let shared = NotificationServiceAPI()
    private let api = APIClient.shared
    
    private init() {}
    
    func getNotifications() async throws -> [AppNotification] {
        try await api.get(AppConfig.Endpoints.notifications)
    }
    
    func markAsRead(id: String) async throws -> AppNotification {
        try await api.putEmpty(AppConfig.Endpoints.readNotification(id))
    }
}
