import Foundation

struct Store: Codable, Identifiable {
    let id: String
    var name: String
    var address: StoreAddress
    var location: Location
    var hours: StoreHours?
    var isOpen: Bool
    var rating: Double?
    var phone: String?
    var imageUrl: String?
    
    enum CodingKeys: String, CodingKey {
        case id, name, address, location, hours, isOpen, rating, phone, imageUrl
    }
}

struct StoreAddress: Codable {
    var street: String
    var city: String
    var state: String
    var zipCode: String
    
    var formatted: String {
        "\(street), \(city), \(state) \(zipCode)"
    }
}

struct Location: Codable {
    var lat: Double
    var lng: Double
}

struct StoreHours: Codable {
    var monday: DayHours?
    var tuesday: DayHours?
    var wednesday: DayHours?
    var thursday: DayHours?
    var friday: DayHours?
    var saturday: DayHours?
    var sunday: DayHours?
}

struct DayHours: Codable {
    var open: String
    var close: String
}
