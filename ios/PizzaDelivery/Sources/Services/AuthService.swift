import Foundation

class AuthService {
    static let shared = AuthService()
    private let api = APIClient.shared
    
    private init() {}
    
    func login(email: String, password: String) async throws -> AuthResponse {
        let request = LoginRequest(email: email, password: password)
        let response: AuthResponse = try await api.post(AppConfig.Endpoints.login, body: request)
        api.accessToken = response.accessToken
        api.refreshToken = response.refreshToken
        saveUser(response.user)
        return response
    }
    
    func register(email: String, password: String, name: String, phone: String?) async throws -> AuthResponse {
        let request = RegisterRequest(email: email, password: password, name: name, phone: phone)
        let response: AuthResponse = try await api.post(AppConfig.Endpoints.register, body: request)
        api.accessToken = response.accessToken
        api.refreshToken = response.refreshToken
        saveUser(response.user)
        return response
    }
    
    func logout() async {
        try? await api.postEmpty(AppConfig.Endpoints.logout) as EmptyResponse
        api.clearTokens()
        clearUser()
    }
    
    func getProfile() async throws -> User {
        try await api.get(AppConfig.Endpoints.profile)
    }
    
    func updateProfile(name: String, phone: String?) async throws -> User {
        let body = ["name": name, "phone": phone ?? ""]
        let user: User = try await api.put(AppConfig.Endpoints.profile, body: body)
        saveUser(user)
        return user
    }
    
    // MARK: - Local Storage
    
    func saveUser(_ user: User) {
        if let data = try? JSONEncoder().encode(user) {
            UserDefaults.standard.set(data, forKey: "currentUser")
        }
    }
    
    func getSavedUser() -> User? {
        guard let data = UserDefaults.standard.data(forKey: "currentUser") else { return nil }
        return try? JSONDecoder().decode(User.self, from: data)
    }
    
    func clearUser() {
        UserDefaults.standard.removeObject(forKey: "currentUser")
    }
    
    var isLoggedIn: Bool {
        api.accessToken != nil
    }
}

struct EmptyResponse: Codable {}
