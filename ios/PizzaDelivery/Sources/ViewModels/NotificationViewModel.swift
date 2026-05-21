import Foundation

@MainActor
class NotificationViewModel: ObservableObject {
    @Published var notifications: [AppNotification] = []
    @Published var isLoading = false
    @Published var errorMessage: String?
    
    private let notificationService = NotificationServiceAPI.shared
    
    var unreadCount: Int {
        notifications.filter { !$0.isRead }.count
    }
    
    var unreadNotifications: [AppNotification] {
        notifications.filter { !$0.isRead }
    }
    
    var readNotifications: [AppNotification] {
        notifications.filter { $0.isRead }
    }
    
    func loadNotifications() async {
        isLoading = true
        do {
            notifications = try await notificationService.getNotifications()
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }
    
    func markAsRead(id: String) async {
        do {
            _ = try await notificationService.markAsRead(id: id)
            if let index = notifications.firstIndex(where: { $0.id == id }) {
                notifications[index].isRead = true
            }
        } catch {
            errorMessage = error.localizedDescription
        }
    }
    
    func markAllAsRead() async {
        for notification in unreadNotifications {
            await markAsRead(id: notification.id)
        }
    }
}
