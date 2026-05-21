import Foundation

@MainActor
class OrderViewModel: ObservableObject {
    @Published var orders: [Order] = []
    @Published var selectedOrder: Order?
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var showError = false
    @Published var orderPlaced = false
    
    private let orderService = OrderService.shared
    
    var activeOrders: [Order] {
        orders.filter { $0.status != .delivered && $0.status != .cancelled }
    }
    
    var pastOrders: [Order] {
        orders.filter { $0.status == .delivered || $0.status == .cancelled }
    }
    
    func loadOrders() async {
        isLoading = true
        errorMessage = nil
        do {
            orders = try await orderService.getOrders()
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }
    
    func loadOrder(id: String) async {
        isLoading = true
        do {
            selectedOrder = try await orderService.getOrder(id: id)
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }
    
    func createOrder(storeId: String, items: [OrderItemRequest], address: Address, paymentMethod: String) async -> Order? {
        isLoading = true
        errorMessage = nil
        
        do {
            let request = CreateOrderRequest(
                storeId: storeId,
                items: items,
                deliveryAddress: address,
                paymentMethod: paymentMethod
            )
            let order = try await orderService.createOrder(request: request)
            orderPlaced = true
            await loadOrders()
            isLoading = false
            return order
        } catch {
            errorMessage = error.localizedDescription
            showError = true
            isLoading = false
            return nil
        }
    }
    
    func cancelOrder(id: String) async {
        isLoading = true
        do {
            _ = try await orderService.cancelOrder(id: id)
            await loadOrders()
        } catch {
            errorMessage = error.localizedDescription
            showError = true
        }
        isLoading = false
    }
}
