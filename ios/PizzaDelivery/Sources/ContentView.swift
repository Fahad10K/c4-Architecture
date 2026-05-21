import SwiftUI

struct ContentView: View {
    @EnvironmentObject var authViewModel: AuthViewModel
    
    var body: some View {
        Group {
            if authViewModel.isAuthenticated {
                MainTabView()
            } else {
                LoginView()
            }
        }
        .onAppear {
            authViewModel.checkAuthStatus()
        }
    }
}

struct MainTabView: View {
    @EnvironmentObject var cartViewModel: CartViewModel
    @EnvironmentObject var notificationViewModel: NotificationViewModel
    
    var body: some View {
        TabView {
            HomeView()
                .tabItem {
                    Label("Home", systemImage: "house.fill")
                }
            
            StoreListView()
                .tabItem {
                    Label("Stores", systemImage: "mappin.and.ellipse")
                }
            
            OrderListView()
                .tabItem {
                    Label("Orders", systemImage: "bag.fill")
                }
            
            NotificationsView()
                .tabItem {
                    Label("Alerts", systemImage: "bell.fill")
                }
                .badge(notificationViewModel.unreadCount)
            
            ProfileView()
                .tabItem {
                    Label("Profile", systemImage: "person.fill")
                }
        }
        .tint(Color.brand)
    }
}
