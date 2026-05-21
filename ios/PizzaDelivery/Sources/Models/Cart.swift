import Foundation

struct Cart: Codable {
    let id: String
    var userId: String
    var storeId: String
    var items: [CartItem]
    var couponCode: String?
    var subtotal: Double
    var tax: Double
    var deliveryFee: Double
    var discount: Double
    var total: Double
}

struct CartItem: Codable, Identifiable {
    let id: String
    var menuItemId: String
    var name: String
    var quantity: Int
    var customizations: [SelectedCustomization]
    var price: Double
    var itemTotal: Double
    
    enum CodingKeys: String, CodingKey {
        case id, menuItemId, name, quantity, customizations, price, itemTotal
    }
}

struct SelectedCustomization: Codable {
    var name: String
    var selectedOptions: [String]
    var additionalPrice: Double
}

struct AddToCartRequest: Codable {
    let menuItemId: String
    let quantity: Int
    let customizations: [SelectedCustomization]?
    let storeId: String
}

struct UpdateCartItemRequest: Codable {
    let quantity: Int
}

struct ApplyCouponRequest: Codable {
    let couponCode: String
}
