import Foundation
import Starscream

protocol WebSocketServiceDelegate: AnyObject {
    func didReceiveMessage(_ message: String)
    func didConnect()
    func didDisconnect(error: Error?)
}

class WebSocketService: WebSocketDelegate {
    private var socket: WebSocket?
    private var isConnected = false
    weak var delegate: WebSocketServiceDelegate?
    private let url: String
    
    init(url: String) {
        self.url = url
    }
    
    func connect() {
        guard let wsURL = URL(string: url) else { return }
        
        var request = URLRequest(url: wsURL)
        request.timeoutInterval = 5
        
        if let token = APIClient.shared.accessToken {
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        
        socket = WebSocket(request: request)
        socket?.delegate = self
        socket?.connect()
    }
    
    func disconnect() {
        socket?.disconnect()
        isConnected = false
    }
    
    func send(message: String) {
        guard isConnected else { return }
        socket?.write(string: message)
    }
    
    func send<T: Codable>(data: T) {
        guard isConnected else { return }
        if let jsonData = try? JSONEncoder().encode(data),
           let jsonString = String(data: jsonData, encoding: .utf8) {
            socket?.write(string: jsonString)
        }
    }
    
    // MARK: - WebSocketDelegate
    
    func didReceive(event: WebSocketEvent, client: WebSocketClient) {
        switch event {
        case .connected(_):
            isConnected = true
            delegate?.didConnect()
        case .disconnected(_, _):
            isConnected = false
            delegate?.didDisconnect(error: nil)
        case .text(let text):
            delegate?.didReceiveMessage(text)
        case .error(let error):
            isConnected = false
            delegate?.didDisconnect(error: error)
        case .cancelled:
            isConnected = false
            delegate?.didDisconnect(error: nil)
        default:
            break
        }
    }
}
