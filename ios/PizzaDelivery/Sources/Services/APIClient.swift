import Foundation

enum APIError: Error, LocalizedError {
    case invalidURL
    case noData
    case decodingError(Error)
    case serverError(Int, String?)
    case networkError(Error)
    case unauthorized
    case unknown
    
    var errorDescription: String? {
        switch self {
        case .invalidURL: return "Invalid URL"
        case .noData: return "No data received"
        case .decodingError(let error): return "Decoding error: \(error.localizedDescription)"
        case .serverError(let code, let msg): return "Server error (\(code)): \(msg ?? "Unknown")"
        case .networkError(let error): return "Network error: \(error.localizedDescription)"
        case .unauthorized: return "Unauthorized. Please login again."
        case .unknown: return "An unknown error occurred"
        }
    }
}

struct APIResponse<T: Codable>: Codable {
    let data: T?
    let message: String?
    let error: String?
    let success: Bool?
}

class APIClient {
    static let shared = APIClient()
    
    private let session: URLSession
    private let baseURL: String
    private let decoder: JSONDecoder
    
    private init() {
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = 30
        config.timeoutIntervalForResource = 60
        self.session = URLSession(configuration: config)
        self.baseURL = AppConfig.baseURL
        self.decoder = JSONDecoder()
        self.decoder.keyDecodingStrategy = .convertFromSnakeCase
    }
    
    // MARK: - Token Management
    
    var accessToken: String? {
        get { UserDefaults.standard.string(forKey: "accessToken") }
        set { UserDefaults.standard.set(newValue, forKey: "accessToken") }
    }
    
    var refreshToken: String? {
        get { UserDefaults.standard.string(forKey: "refreshToken") }
        set { UserDefaults.standard.set(newValue, forKey: "refreshToken") }
    }
    
    func clearTokens() {
        accessToken = nil
        refreshToken = nil
        UserDefaults.standard.removeObject(forKey: "currentUser")
    }
    
    // MARK: - Request Methods
    
    func get<T: Codable>(_ endpoint: String, queryParams: [String: String]? = nil) async throws -> T {
        let request = try buildRequest(endpoint: endpoint, method: "GET", queryParams: queryParams)
        return try await execute(request)
    }
    
    func post<T: Codable, B: Codable>(_ endpoint: String, body: B) async throws -> T {
        var request = try buildRequest(endpoint: endpoint, method: "POST")
        request.httpBody = try JSONEncoder().encode(body)
        return try await execute(request)
    }
    
    func postEmpty<T: Codable>(_ endpoint: String) async throws -> T {
        let request = try buildRequest(endpoint: endpoint, method: "POST")
        return try await execute(request)
    }
    
    func put<T: Codable, B: Codable>(_ endpoint: String, body: B) async throws -> T {
        var request = try buildRequest(endpoint: endpoint, method: "PUT")
        request.httpBody = try JSONEncoder().encode(body)
        return try await execute(request)
    }
    
    func putEmpty<T: Codable>(_ endpoint: String) async throws -> T {
        let request = try buildRequest(endpoint: endpoint, method: "PUT")
        return try await execute(request)
    }
    
    func delete<T: Codable>(_ endpoint: String) async throws -> T {
        let request = try buildRequest(endpoint: endpoint, method: "DELETE")
        return try await execute(request)
    }
    
    func deleteVoid(_ endpoint: String) async throws {
        let request = try buildRequest(endpoint: endpoint, method: "DELETE")
        let (_, response) = try await session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse else { throw APIError.unknown }
        if httpResponse.statusCode == 401 { throw APIError.unauthorized }
        if httpResponse.statusCode >= 400 {
            throw APIError.serverError(httpResponse.statusCode, nil)
        }
    }
    
    // MARK: - Private Helpers
    
    private func buildRequest(endpoint: String, method: String, queryParams: [String: String]? = nil) throws -> URLRequest {
        var urlString = baseURL + endpoint
        
        if let params = queryParams, !params.isEmpty {
            let queryString = params.map { "\($0.key)=\($0.value.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? $0.value)" }.joined(separator: "&")
            urlString += "?\(queryString)"
        }
        
        guard let url = URL(string: urlString) else {
            throw APIError.invalidURL
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = method
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        
        if let token = accessToken {
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        
        return request
    }
    
    private func execute<T: Codable>(_ request: URLRequest) async throws -> T {
        do {
            let (data, response) = try await session.data(for: request)
            
            guard let httpResponse = response as? HTTPURLResponse else {
                throw APIError.unknown
            }
            
            if httpResponse.statusCode == 401 {
                // Try token refresh
                if let _ = refreshToken {
                    let refreshed = try? await refreshAccessToken()
                    if refreshed == true {
                        var newRequest = request
                        newRequest.setValue("Bearer \(accessToken ?? "")", forHTTPHeaderField: "Authorization")
                        let (newData, newResponse) = try await session.data(for: newRequest)
                        guard let newHttp = newResponse as? HTTPURLResponse, newHttp.statusCode < 400 else {
                            throw APIError.unauthorized
                        }
                        return try decoder.decode(T.self, from: newData)
                    }
                }
                throw APIError.unauthorized
            }
            
            if httpResponse.statusCode >= 400 {
                let errorMsg = String(data: data, encoding: .utf8)
                throw APIError.serverError(httpResponse.statusCode, errorMsg)
            }
            
            return try decoder.decode(T.self, from: data)
        } catch let error as APIError {
            throw error
        } catch let error as DecodingError {
            throw APIError.decodingError(error)
        } catch {
            throw APIError.networkError(error)
        }
    }
    
    private func refreshAccessToken() async throws -> Bool {
        guard let refreshToken = refreshToken else { return false }
        
        let url = URL(string: baseURL + AppConfig.Endpoints.refresh)!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        let body = ["refreshToken": refreshToken]
        request.httpBody = try JSONEncoder().encode(body)
        
        let (data, response) = try await session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            clearTokens()
            return false
        }
        
        let authResponse = try decoder.decode(AuthResponse.self, from: data)
        self.accessToken = authResponse.accessToken
        self.refreshToken = authResponse.refreshToken
        return true
    }
}
