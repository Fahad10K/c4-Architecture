import Foundation

struct PaymentRequest: Codable {
    let orderId: String
    let method: String
    let amount: Double
    let cardToken: String?
}

struct Payment: Codable, Identifiable {
    let id: String
    var orderId: String
    var method: String
    var status: PaymentStatus
    var amount: Double
    var transactionId: String?
    var createdAt: String?
}

enum PaymentStatus: String, Codable {
    case pending
    case processing
    case completed
    case failed
    case refunded
}

class PaymentService {
    static let shared = PaymentService()
    private let api = APIClient.shared
    
    private init() {}
    
    func createPayment(request: PaymentRequest) async throws -> Payment {
        try await api.post(AppConfig.Endpoints.payments, body: request)
    }
    
    func getPayment(id: String) async throws -> Payment {
        try await api.get(AppConfig.Endpoints.paymentDetail(id))
    }
}
