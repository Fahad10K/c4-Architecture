import Foundation

@MainActor
class HomeViewModel: ObservableObject {
    @Published var recommendations: [Recommendation] = []
    @Published var offers: [Offer] = []
    @Published var nearbyStores: [Store] = []
    @Published var isLoading = false
    @Published var errorMessage: String?
    
    private let recommendationService = RecommendationServiceAPI.shared
    private let storeService = StoreService.shared
    
    func loadHomeData() async {
        isLoading = true
        errorMessage = nil
        
        async let fetchRecommendations = recommendationService.getRecommendations()
        async let fetchOffers = recommendationService.getOffers()
        async let fetchStores = storeService.getStores()
        
        do {
            recommendations = try await fetchRecommendations
        } catch {
            recommendations = []
        }
        
        do {
            offers = try await fetchOffers
        } catch {
            offers = []
        }
        
        do {
            nearbyStores = try await fetchStores
        } catch {
            nearbyStores = []
        }
        
        isLoading = false
    }
}
