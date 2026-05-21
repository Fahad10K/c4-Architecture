import Foundation

@MainActor
class ChatbotViewModel: ObservableObject {
    @Published var messages: [ChatMessage] = []
    @Published var isLoading = false
    @Published var inputText = ""
    @Published var errorMessage: String?
    
    private let chatbotService = ChatbotService.shared
    private var webSocketService: WebSocketService?
    
    init() {
        messages.append(ChatMessage(
            content: "Hi! I'm your Pizza Assistant. How can I help you today? I can help with menu recommendations, order tracking, or answer any questions!",
            role: .assistant,
            suggestions: ["Show me today's specials", "Track my order", "What's popular?"]
        ))
    }
    
    func sendMessage(_ text: String? = nil) async {
        let messageText = text ?? inputText
        guard !messageText.trimmingCharacters(in: .whitespaces).isEmpty else { return }
        
        let userMessage = ChatMessage(content: messageText, role: .user)
        messages.append(userMessage)
        inputText = ""
        isLoading = true
        
        do {
            let response = try await chatbotService.sendMessage(message: messageText)
            let assistantMessage = ChatMessage(
                id: response.id,
                content: response.message,
                role: .assistant,
                suggestions: response.suggestions
            )
            messages.append(assistantMessage)
        } catch {
            let errorMsg = ChatMessage(
                content: "Sorry, I'm having trouble connecting. Please try again.",
                role: .assistant
            )
            messages.append(errorMsg)
            errorMessage = error.localizedDescription
        }
        
        isLoading = false
    }
    
    func connectWebSocket() {
        webSocketService = WebSocketService(url: AppConfig.WebSocket.chatbot)
        webSocketService?.connect()
    }
    
    func disconnectWebSocket() {
        webSocketService?.disconnect()
    }
}
