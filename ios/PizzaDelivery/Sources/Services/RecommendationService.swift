import Foundation

class RecommendationServiceAPI {
    static let shared = RecommendationServiceAPI()
    private let api = APIClient.shared
    
    private init() {}
    
    func getRecommendations() async throws -> [Recommendation] {
        try await api.get(AppConfig.Endpoints.recommendations)
    }
    
    func getOffers() async throws -> [Offer] {
        try await api.get(AppConfig.Endpoints.offers)
    }
}
