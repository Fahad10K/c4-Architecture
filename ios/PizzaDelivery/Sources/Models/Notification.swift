import Foundation

struct AppNotification: Codable, Identifiable {
    let id: String
    var userId: String
    var title: String
    var message: String
    var type: NotificationType
    var isRead: Bool
    var data: NotificationData?
    var createdAt: String
    
    enum CodingKeys: String, CodingKey {
        case id, userId, title, message, type, isRead, data, createdAt
    }
}

enum NotificationType: String, Codable {
    case order
    case delivery
    case promotion
    case system
    case chat
    
    var icon: String {
        switch self {
        case .order: return "bag.fill"
        case .delivery: return "car.fill"
        case .promotion: return "tag.fill"
        case .system: return "gear"
        case .chat: return "message.fill"
        }
    }
}

struct NotificationData: Codable {
    var orderId: String?
    var storeId: String?
    var actionUrl: String?
}
