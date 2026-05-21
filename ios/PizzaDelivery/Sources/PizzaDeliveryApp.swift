import SwiftUI

@main
struct PizzaDeliveryApp: App {
    @StateObject private var authViewModel = AuthViewModel()
    @StateObject private var cartViewModel = CartViewModel()
    @StateObject private var notificationViewModel = NotificationViewModel()
    
    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(authViewModel)
                .environmentObject(cartViewModel)
                .environmentObject(notificationViewModel)
        }
    }
}
