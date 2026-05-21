import SwiftUI

struct NotificationsView: View {
    @EnvironmentObject var notificationViewModel: NotificationViewModel
    
    var body: some View {
        NavigationStack {
            Group {
                if notificationViewModel.isLoading && notificationViewModel.notifications.isEmpty {
                    LoadingView(message: "Loading notifications...")
                } else if notificationViewModel.notifications.isEmpty {
                    emptyState
                } else {
                    notificationList
                }
            }
            .navigationTitle("Notifications")
            .toolbar {
                if notificationViewModel.unreadCount > 0 {
                    ToolbarItem(placement: .topBarTrailing) {
                        Button("Mark All Read") {
                            Task { await notificationViewModel.markAllAsRead() }
                        }
                        .font(.caption)
                        .foregroundColor(.brand)
                    }
                }
            }
            .refreshable {
                await notificationViewModel.loadNotifications()
            }
            .task {
                await notificationViewModel.loadNotifications()
            }
        }
    }
    
    private var notificationList: some View {
        ScrollView {
            LazyVStack(spacing: 1) {
                if !notificationViewModel.unreadNotifications.isEmpty {
                    Section {
                        ForEach(notificationViewModel.unreadNotifications) { notification in
                            NotificationRow(notification: notification) {
                                Task { await notificationViewModel.markAsRead(id: notification.id) }
                            }
                        }
                    } header: {
                        sectionHeader("New", count: notificationViewModel.unreadCount)
                    }
                }
                
                if !notificationViewModel.readNotifications.isEmpty {
                    Section {
                        ForEach(notificationViewModel.readNotifications) { notification in
                            NotificationRow(notification: notification) {}
                        }
                    } header: {
                        sectionHeader("Earlier", count: nil)
                    }
                }
            }
        }
    }
    
    private func sectionHeader(_ title: String, count: Int?) -> some View {
        HStack {
            Text(title)
                .font(.headline)
            if let count = count {
                Text("\(count)")
                    .font(.caption.bold())
                    .foregroundColor(.white)
                    .frame(width: 20, height: 20)
                    .background(Color.brand)
                    .clipShape(Circle())
            }
            Spacer()
        }
        .padding(.horizontal)
        .padding(.vertical, 8)
    }
    
    private var emptyState: some View {
        VStack(spacing: 16) {
            Image(systemName: "bell.slash")
                .font(.system(size: 50))
                .foregroundColor(.secondary)
            Text("No Notifications")
                .font(.title3.bold())
            Text("You're all caught up!")
                .font(.subheadline)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

struct NotificationRow: View {
    let notification: AppNotification
    let onTap: () -> Void
    
    var body: some View {
        Button(action: onTap) {
            HStack(alignment: .top, spacing: 12) {
                ZStack {
                    Circle()
                        .fill(notificationColor.opacity(0.1))
                        .frame(width: 44, height: 44)
                    Image(systemName: notification.type.icon)
                        .foregroundColor(notificationColor)
                }
                
                VStack(alignment: .leading, spacing: 4) {
                    HStack {
                        Text(notification.title)
                            .font(.subheadline.bold())
                            .foregroundColor(.primary)
                        Spacer()
                        Text(notification.createdAt.relativeTime())
                            .font(.caption2)
                            .foregroundColor(.secondary)
                    }
                    
                    Text(notification.message)
                        .font(.caption)
                        .foregroundColor(.secondary)
                        .lineLimit(2)
                }
                
                if !notification.isRead {
                    Circle()
                        .fill(Color.brand)
                        .frame(width: 8, height: 8)
                }
            }
            .padding(14)
            .background(notification.isRead ? Color.cardBackground : Color.brand.opacity(0.03))
        }
        .buttonStyle(.plain)
    }
    
    private var notificationColor: Color {
        switch notification.type {
        case .order: return .blue
        case .delivery: return .green
        case .promotion: return .orange
        case .system: return .purple
        case .chat: return .brand
        }
    }
}
