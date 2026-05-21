import Foundation

struct ChatMessage: Codable, Identifiable {
    let id: String
    var content: String
    var role: ChatRole
    var timestamp: String
    var suggestions: [String]?
    
    init(id: String = UUID().uuidString, content: String, role: ChatRole, timestamp: String = ISO8601DateFormatter().string(from: Date()), suggestions: [String]? = nil) {
        self.id = id
        self.content = content
        self.role = role
        self.timestamp = timestamp
        self.suggestions = suggestions
    }
}

enum ChatRole: String, Codable {
    case user
    case assistant
    case system
}

struct ChatRequest: Codable {
    let message: String
}

struct ChatResponse: Codable {
    let id: String
    let message: String
    let suggestions: [String]?
}
