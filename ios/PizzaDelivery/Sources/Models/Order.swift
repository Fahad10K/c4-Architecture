import Foundation

struct Order: Codable, Identifiable {
    let id: String
    var userId: String
    var storeId: String
    var storeName: String?
    var items: [OrderItem]
    var status: OrderStatus
    var payment: OrderPayment?
    var delivery: OrderDelivery?
    var total: Double
    var createdAt: String
    
    enum CodingKeys: String, CodingKey {
        case id, userId, storeId, storeName, items, status, payment, delivery, total, createdAt
    }
}

struct OrderItem: Codable, Identifiable {
    var id: String { menuItemId }
    var menuItemId: String
    var name: String
    var quantity: Int
    var customizations: [SelectedCustomization]?
    var price: Double
}

enum OrderStatus: String, Codable {
    case placed
    case confirmed
    case preparing
    case ready
    case pickedUp = "picked_up"
    case onTheWay = "on_the_way"
    case delivered
    case cancelled
    
    var displayName: String {
        switch self {
        case .placed: return "Order Placed"
        case .confirmed: return "Confirmed"
        case .preparing: return "Preparing"
        case .ready: return "Ready for Pickup"
        case .pickedUp: return "Picked Up"
        case .onTheWay: return "On the Way"
        case .delivered: return "Delivered"
        case .cancelled: return "Cancelled"
        }
    }
    
    var icon: String {
        switch self {
        case .placed: return "clock.fill"
        case .confirmed: return "checkmark.circle.fill"
        case .preparing: return "flame.fill"
        case .ready: return "bag.fill"
        case .pickedUp: return "car.fill"
        case .onTheWay: return "bicycle"
        case .delivered: return "checkmark.seal.fill"
        case .cancelled: return "xmark.circle.fill"
        }
    }
    
    var progress: Double {
        switch self {
        case .placed: return 0.15
        case .confirmed: return 0.3
        case .preparing: return 0.45
        case .ready: return 0.6
        case .pickedUp: return 0.75
        case .onTheWay: return 0.85
        case .delivered: return 1.0
        case .cancelled: return 0.0
        }
    }
}

struct OrderPayment: Codable {
    var method: String?
    var status: String?
    var transactionId: String?
    var amount: Double?
}

struct OrderDelivery: Codable {
    var driverId: String?
    var driverName: String?
    var status: String?
    var eta: Int?
    var currentLocation: Location?
}

struct CreateOrderRequest: Codable {
    let storeId: String
    let items: [OrderItemRequest]
    let deliveryAddress: Address
    let paymentMethod: String
}

struct OrderItemRequest: Codable {
    let menuItemId: String
    let quantity: Int
    let customizations: [SelectedCustomization]?
}
