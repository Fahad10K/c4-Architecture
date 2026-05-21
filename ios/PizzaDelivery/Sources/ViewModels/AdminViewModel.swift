import Foundation

@MainActor
class AdminViewModel: ObservableObject {
    @Published var dashboard: AdminDashboard?
    @Published var analytics: AdminAnalytics?
    @Published var users: [User] = []
    @Published var orders: [Order] = []
    @Published var stores: [Store] = []
    @Published var isLoading = false
    @Published var errorMessage: String?
    
    private let adminService = AdminService.shared
    
    func loadDashboard() async {
        isLoading = true
        do {
            dashboard = try await adminService.getDashboard()
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }
    
    func loadAnalytics() async {
        do {
            analytics = try await adminService.getAnalytics()
        } catch {
            errorMessage = error.localizedDescription
        }
    }
    
    func loadUsers() async {
        do {
            users = try await adminService.getUsers()
        } catch {
            errorMessage = error.localizedDescription
        }
    }
    
    func loadOrders() async {
        do {
            orders = try await adminService.getOrders()
        } catch {
            errorMessage = error.localizedDescription
        }
    }
    
    func loadStores() async {
        do {
            stores = try await adminService.getStores()
        } catch {
            errorMessage = error.localizedDescription
        }
    }
    
    func loadAllData() async {
        isLoading = true
        async let d = adminService.getDashboard()
        async let a = adminService.getAnalytics()
        
        dashboard = try? await d
        analytics = try? await a
        isLoading = false
    }
}
