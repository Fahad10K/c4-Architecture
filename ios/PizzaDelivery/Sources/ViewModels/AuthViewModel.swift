import Foundation
import SwiftUI

@MainActor
class AuthViewModel: ObservableObject {
    @Published var isAuthenticated = false
    @Published var currentUser: User?
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var showError = false
    
    private let authService = AuthService.shared
    
    func checkAuthStatus() {
        if authService.isLoggedIn, let user = authService.getSavedUser() {
            self.currentUser = user
            self.isAuthenticated = true
        }
    }
    
    func login(email: String, password: String) async {
        isLoading = true
        errorMessage = nil
        
        do {
            let response = try await authService.login(email: email, password: password)
            self.currentUser = response.user
            self.isAuthenticated = true
        } catch {
            self.errorMessage = error.localizedDescription
            self.showError = true
        }
        
        isLoading = false
    }
    
    func register(email: String, password: String, name: String, phone: String?) async {
        isLoading = true
        errorMessage = nil
        
        do {
            let response = try await authService.register(email: email, password: password, name: name, phone: phone)
            self.currentUser = response.user
            self.isAuthenticated = true
        } catch {
            self.errorMessage = error.localizedDescription
            self.showError = true
        }
        
        isLoading = false
    }
    
    func logout() async {
        await authService.logout()
        self.currentUser = nil
        self.isAuthenticated = false
    }
    
    func updateProfile(name: String, phone: String?) async {
        isLoading = true
        do {
            let user = try await authService.updateProfile(name: name, phone: phone)
            self.currentUser = user
        } catch {
            self.errorMessage = error.localizedDescription
            self.showError = true
        }
        isLoading = false
    }
}
