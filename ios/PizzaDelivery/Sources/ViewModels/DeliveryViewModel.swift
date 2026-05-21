import Foundation

@MainActor
class DeliveryViewModel: ObservableObject {
    @Published var delivery: Delivery?
    @Published var driverLocation: Location?
    @Published var eta: Int?
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var isConnected = false
    
    private let deliveryService = DeliveryService.shared
    private var webSocketService: WebSocketService?
    
    func loadDeliveryTracking(orderId: String) async {
        isLoading = true
        do {
            delivery = try await deliveryService.getDeliveryTracking(orderId: orderId)
            driverLocation = delivery?.currentLocation
            eta = delivery?.eta
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }
    
    func connectWebSocket(orderId: String) {
        let wsURL = AppConfig.WebSocket.deliveryTrack(orderId: orderId)
        webSocketService = WebSocketService(url: wsURL)
        webSocketService?.delegate = WebSocketHandler(viewModel: self)
        webSocketService?.connect()
    }
    
    func disconnectWebSocket() {
        webSocketService?.disconnect()
        webSocketService = nil
        isConnected = false
    }
    
    func handleLocationUpdate(_ update: DeliveryLocationUpdate) {
        driverLocation = update.location
        eta = update.eta
    }
    
    func handleStatusChange(_ change: DeliveryStatusChange) {
        delivery?.status = change.status
    }
}

class WebSocketHandler: WebSocketServiceDelegate {
    private weak var viewModel: DeliveryViewModel?
    
    init(viewModel: DeliveryViewModel) {
        self.viewModel = viewModel
    }
    
    func didReceiveMessage(_ message: String) {
        guard let data = message.data(using: .utf8) else { return }
        let decoder = JSONDecoder()
        decoder.keyDecodingStrategy = .convertFromSnakeCase
        
        if let locationUpdate = try? decoder.decode(DeliveryLocationUpdate.self, from: data) {
            Task { @MainActor in
                viewModel?.handleLocationUpdate(locationUpdate)
            }
        } else if let statusChange = try? decoder.decode(DeliveryStatusChange.self, from: data) {
            Task { @MainActor in
                viewModel?.handleStatusChange(statusChange)
            }
        }
    }
    
    func didConnect() {
        Task { @MainActor in
            viewModel?.isConnected = true
        }
    }
    
    func didDisconnect(error: Error?) {
        Task { @MainActor in
            viewModel?.isConnected = false
        }
    }
}
