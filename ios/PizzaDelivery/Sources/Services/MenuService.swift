import Foundation

class MenuService {
    static let shared = MenuService()
    private let api = APIClient.shared
    
    private init() {}
    
    func getMenu(storeId: String) async throws -> [MenuItem] {
        try await api.get(AppConfig.Endpoints.menu(storeId: storeId))
    }
    
    func getMenuItem(id: String) async throws -> MenuItem {
        try await api.get(AppConfig.Endpoints.menuItem(id))
    }
    
    func getCategories() async throws -> [MenuCategory] {
        try await api.get(AppConfig.Endpoints.menuCategories)
    }
}
