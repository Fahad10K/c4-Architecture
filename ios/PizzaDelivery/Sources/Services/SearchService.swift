import Foundation

struct SearchResult: Codable, Identifiable {
    var id: String { "\(type)_\(itemId)" }
    var itemId: String
    var name: String
    var description: String?
    var type: SearchResultType
    var price: Double?
    var image: String?
    var storeId: String?
    var storeName: String?
    
    enum CodingKeys: String, CodingKey {
        case itemId, name, description, type, price, image, storeId, storeName
    }
}

enum SearchResultType: String, Codable {
    case menuItem = "menu_item"
    case store
    case category
}

class SearchService {
    static let shared = SearchService()
    private let api = APIClient.shared
    
    private init() {}
    
    func search(query: String) async throws -> [SearchResult] {
        let params = ["q": query]
        return try await api.get(AppConfig.Endpoints.search, queryParams: params)
    }
}
