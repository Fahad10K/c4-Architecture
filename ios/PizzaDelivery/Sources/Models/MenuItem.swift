import Foundation

struct MenuItem: Codable, Identifiable {
    let id: String
    var storeId: String
    var name: String
    var description: String
    var price: Double
    var category: String
    var image: String?
    var customizations: [Customization]?
    var isAvailable: Bool
    
    enum CodingKeys: String, CodingKey {
        case id, storeId, name, description, price, category, image, customizations, isAvailable
    }
}

struct Customization: Codable, Identifiable {
    var id: String { name }
    var name: String
    var options: [CustomizationOption]
    var required: Bool
    var maxSelections: Int?
}

struct CustomizationOption: Codable, Identifiable, Hashable {
    var id: String { name }
    var name: String
    var price: Double
    
    func hash(into hasher: inout Hasher) {
        hasher.combine(name)
    }
    
    static func == (lhs: CustomizationOption, rhs: CustomizationOption) -> Bool {
        lhs.name == rhs.name && lhs.price == rhs.price
    }
}

struct MenuCategory: Codable, Identifiable {
    var id: String { name }
    var name: String
    var itemCount: Int?
}
