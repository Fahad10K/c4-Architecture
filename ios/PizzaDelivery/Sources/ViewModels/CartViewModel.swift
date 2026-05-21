import Foundation

@MainActor
class CartViewModel: ObservableObject {
    @Published var cart: Cart?
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var showError = false
    @Published var itemCount: Int = 0
    
    private let cartService = CartService.shared
    
    var total: Double { cart?.total ?? 0 }
    var subtotal: Double { cart?.subtotal ?? 0 }
    var tax: Double { cart?.tax ?? 0 }
    var deliveryFee: Double { cart?.deliveryFee ?? 0 }
    var discount: Double { cart?.discount ?? 0 }
    
    func loadCart() async {
        isLoading = true
        do {
            cart = try await cartService.getCart()
            itemCount = cart?.items.reduce(0) { $0 + $1.quantity } ?? 0
        } catch {
            cart = nil
            itemCount = 0
        }
        isLoading = false
    }
    
    func addToCart(menuItem: MenuItem, quantity: Int, customizations: [SelectedCustomization]?, storeId: String) async {
        isLoading = true
        do {
            let request = AddToCartRequest(
                menuItemId: menuItem.id,
                quantity: quantity,
                customizations: customizations,
                storeId: storeId
            )
            cart = try await cartService.addToCart(request: request)
            itemCount = cart?.items.reduce(0) { $0 + $1.quantity } ?? 0
        } catch {
            errorMessage = error.localizedDescription
            showError = true
        }
        isLoading = false
    }
    
    func updateQuantity(itemId: String, quantity: Int) async {
        do {
            if quantity <= 0 {
                try await cartService.removeFromCart(itemId: itemId)
                await loadCart()
            } else {
                cart = try await cartService.updateCartItem(itemId: itemId, quantity: quantity)
                itemCount = cart?.items.reduce(0) { $0 + $1.quantity } ?? 0
            }
        } catch {
            errorMessage = error.localizedDescription
            showError = true
        }
    }
    
    func removeItem(itemId: String) async {
        do {
            try await cartService.removeFromCart(itemId: itemId)
            await loadCart()
        } catch {
            errorMessage = error.localizedDescription
            showError = true
        }
    }
    
    func clearCart() async {
        do {
            try await cartService.clearCart()
            cart = nil
            itemCount = 0
        } catch {
            errorMessage = error.localizedDescription
            showError = true
        }
    }
    
    func applyCoupon(code: String) async {
        isLoading = true
        do {
            cart = try await cartService.applyCoupon(code: code)
        } catch {
            errorMessage = error.localizedDescription
            showError = true
        }
        isLoading = false
    }
}
