import Foundation

class ChatbotService {
    static let shared = ChatbotService()
    private let api = APIClient.shared
    
    private init() {}
    
    func sendMessage(message: String) async throws -> ChatResponse {
        let request = ChatRequest(message: message)
        return try await api.post(AppConfig.Endpoints.chatbotMessage, body: request)
    }
}
