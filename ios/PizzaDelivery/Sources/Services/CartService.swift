import Foundation

class CartService {
    static let shared = CartService()
    private let api = APIClient.shared
    
    private init() {}
    
    func getCart() async throws -> Cart {
        try await api.get(AppConfig.Endpoints.cart)
    }
    
    func addToCart(request: AddToCartRequest) async throws -> Cart {
        try await api.post(AppConfig.Endpoints.cart, body: request)
    }
    
    func updateCartItem(itemId: String, quantity: Int) async throws -> Cart {
        let body = UpdateCartItemRequest(quantity: quantity)
        return try await api.put("\(AppConfig.Endpoints.cart)/\(itemId)", body: body)
    }
    
    func removeFromCart(itemId: String) async throws {
        try await api.deleteVoid("\(AppConfig.Endpoints.cart)/\(itemId)")
    }
    
    func clearCart() async throws {
        try await api.deleteVoid(AppConfig.Endpoints.cart)
    }
    
    func applyCoupon(code: String) async throws -> Cart {
        let body = ApplyCouponRequest(couponCode: code)
        return try await api.post(AppConfig.Endpoints.applyCoupon, body: body)
    }
}
