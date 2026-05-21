import Foundation

@MainActor
class StoreViewModel: ObservableObject {
    @Published var stores: [Store] = []
    @Published var selectedStore: Store?
    @Published var isLoading = false
    @Published var errorMessage: String?
    
    private let storeService = StoreService.shared
    
    func loadStores() async {
        isLoading = true
        errorMessage = nil
        
        do {
            stores = try await storeService.getStores()
        } catch {
            errorMessage = error.localizedDescription
        }
        
        isLoading = false
    }
    
    func loadStore(id: String) async {
        isLoading = true
        do {
            selectedStore = try await storeService.getStore(id: id)
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }
    
    func loadNearbyStores(lat: Double, lng: Double) async {
        isLoading = true
        do {
            stores = try await storeService.getNearbyStores(lat: lat, lng: lng)
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }
}
