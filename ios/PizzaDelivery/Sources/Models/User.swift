import Foundation

struct User: Codable, Identifiable {
    let id: String
    var email: String
    var name: String
    var phone: String?
    var role: UserRole
    var preferences: UserPreferences?
    var addresses: [Address]?
    var createdAt: String?
    
    enum CodingKeys: String, CodingKey {
        case id, email, name, phone, role, preferences, addresses, createdAt
    }
}

enum UserRole: String, Codable {
    case customer
    case admin
    case storeStaff = "store_staff"
    case deliveryPartner = "delivery_partner"
}

struct UserPreferences: Codable {
    var favoriteStores: [String]?
    var dietaryRestrictions: [String]?
    var notificationsEnabled: Bool?
}

struct Address: Codable, Identifiable {
    let id: String
    var label: String
    var street: String
    var city: String
    var state: String
    var zipCode: String
    var isDefault: Bool
    var lat: Double?
    var lng: Double?
    
    enum CodingKeys: String, CodingKey {
        case id, label, street, city, state, zipCode, isDefault, lat, lng
    }
}

struct AuthResponse: Codable {
    let accessToken: String
    let refreshToken: String
    let user: User
}

struct LoginRequest: Codable {
    let email: String
    let password: String
}

struct RegisterRequest: Codable {
    let email: String
    let password: String
    let name: String
    let phone: String?
}
