import Foundation

struct Recommendation: Codable, Identifiable {
    let id: String
    var title: String
    var description: String?
    var items: [MenuItem]?
    var type: RecommendationType
}

enum RecommendationType: String, Codable {
    case personalized
    case trending
    case newItems = "new_items"
    case reorder
}

struct Offer: Codable, Identifiable {
    let id: String
    var title: String
    var description: String
    var code: String
    var discount: Double
    var discountType: String
    var minOrder: Double?
    var validUntil: String?
    var imageUrl: String?
}
