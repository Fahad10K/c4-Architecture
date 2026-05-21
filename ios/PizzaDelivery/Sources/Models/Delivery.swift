import Foundation

struct Delivery: Codable {
    var orderId: String
    var driverId: String?
    var driverName: String?
    var driverPhone: String?
    var status: DeliveryStatus
    var currentLocation: Location?
    var eta: Int?
    var route: [Location]?
    var pickupTime: String?
    var deliveryTime: String?
}

enum DeliveryStatus: String, Codable {
    case assigned
    case pickedUp = "picked_up"
    case onTheWay = "on_the_way"
    case delivered
    
    var displayName: String {
        switch self {
        case .assigned: return "Driver Assigned"
        case .pickedUp: return "Picked Up"
        case .onTheWay: return "On the Way"
        case .delivered: return "Delivered"
        }
    }
}

struct DeliveryLocationUpdate: Codable {
    var orderId: String
    var location: Location
    var eta: Int?
    var timestamp: String?
}

struct DeliveryStatusChange: Codable {
    var orderId: String
    var status: DeliveryStatus
    var timestamp: String?
}
