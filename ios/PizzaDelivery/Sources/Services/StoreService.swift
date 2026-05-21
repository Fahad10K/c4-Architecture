import Foundation

class StoreService {
    static let shared = StoreService()
    private let api = APIClient.shared
    
    private init() {}
    
    func getStores() async throws -> [Store] {
        try await api.get(AppConfig.Endpoints.stores)
    }
    
    func getStore(id: String) async throws -> Store {
        try await api.get(AppConfig.Endpoints.storeDetail(id))
    }
    
    func getNearbyStores(lat: Double, lng: Double, radius: Double = 10) async throws -> [Store] {
        let params = ["lat": String(lat), "lng": String(lng), "radius": String(radius)]
        return try await api.get(AppConfig.Endpoints.nearbyStores, queryParams: params)
    }
}
