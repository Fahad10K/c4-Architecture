import SwiftUI

struct AdminDashboardView: View {
    @StateObject private var viewModel = AdminViewModel()
    @Environment(\.dismiss) private var dismiss
    @State private var selectedTab = 0
    
    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // Tab Selector
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        AdminTab(title: "Dashboard", isSelected: selectedTab == 0) { selectedTab = 0 }
                        AdminTab(title: "Orders", isSelected: selectedTab == 1) { selectedTab = 1 }
                        AdminTab(title: "Users", isSelected: selectedTab == 2) { selectedTab = 2 }
                        AdminTab(title: "Stores", isSelected: selectedTab == 3) { selectedTab = 3 }
                        AdminTab(title: "Analytics", isSelected: selectedTab == 4) { selectedTab = 4 }
                    }
                    .padding(.horizontal)
                }
                .padding(.vertical, 8)
                
                Divider()
                
                // Content
                TabView(selection: $selectedTab) {
                    dashboardContent.tag(0)
                    AdminOrdersView(viewModel: viewModel).tag(1)
                    AdminUsersView(viewModel: viewModel).tag(2)
                    AdminStoresView(viewModel: viewModel).tag(3)
                    AdminAnalyticsView(viewModel: viewModel).tag(4)
                }
                .tabViewStyle(.page(indexDisplayMode: .never))
            }
            .navigationTitle("Admin Panel")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button { dismiss() } label: {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(.secondary)
                    }
                }
            }
            .task {
                await viewModel.loadAllData()
            }
        }
    }
    
    private var dashboardContent: some View {
        ScrollView {
            VStack(spacing: 16) {
                if viewModel.isLoading {
                    LoadingView(message: "Loading dashboard...")
                } else if let dashboard = viewModel.dashboard {
                    // Stats Grid
                    LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
                        StatCard(title: "Total Orders", value: "\(dashboard.totalOrders)", icon: "bag.fill", color: .blue)
                        StatCard(title: "Revenue", value: dashboard.totalRevenue.currencyFormatted, icon: "dollarsign.circle.fill", color: .green)
                        StatCard(title: "Active Users", value: "\(dashboard.activeUsers)", icon: "person.2.fill", color: .purple)
                        StatCard(title: "Stores", value: "\(dashboard.totalStores)", icon: "storefront.fill", color: .orange)
                    }
                    
                    Divider()
                    
                    // Today's Stats
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Today")
                            .font(.headline)
                        
                        HStack(spacing: 12) {
                            TodayStat(title: "Orders", value: "\(dashboard.todayOrders)", icon: "bag", color: .blue)
                            TodayStat(title: "Revenue", value: dashboard.todayRevenue.currencyFormatted, icon: "dollarsign.circle", color: .green)
                            TodayStat(title: "Pending", value: "\(dashboard.pendingOrders)", icon: "clock", color: .orange)
                        }
                    }
                    
                    Divider()
                    
                    // Average Order Value
                    HStack {
                        VStack(alignment: .leading) {
                            Text("Avg Order Value")
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                            Text(dashboard.averageOrderValue.currencyFormatted)
                                .font(.title2.bold())
                                .foregroundColor(.brand)
                        }
                        Spacer()
                        Image(systemName: "chart.line.uptrend.xyaxis")
                            .font(.title)
                            .foregroundColor(.success)
                    }
                    .padding()
                    .background(Color.secondaryBackground)
                    .cornerRadius(12)
                } else {
                    ErrorView(message: viewModel.errorMessage ?? "Failed to load dashboard") {
                        Task { await viewModel.loadDashboard() }
                    }
                }
            }
            .padding()
        }
    }
}

struct AdminTab: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.subheadline.weight(isSelected ? .semibold : .regular))
                .foregroundColor(isSelected ? .white : .primary)
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                .background(isSelected ? Color.brand : Color.secondaryBackground)
                .cornerRadius(20)
        }
    }
}

struct StatCard: View {
    let title: String
    let value: String
    let icon: String
    let color: Color
    
    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack {
                Image(systemName: icon)
                    .foregroundColor(color)
                Spacer()
            }
            Text(value)
                .font(.title2.bold())
            Text(title)
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .padding()
        .background(color.opacity(0.05))
        .cornerRadius(12)
    }
}

struct TodayStat: View {
    let title: String
    let value: String
    let icon: String
    let color: Color
    
    var body: some View {
        VStack(spacing: 6) {
            Image(systemName: icon)
                .foregroundColor(color)
            Text(value)
                .font(.subheadline.bold())
            Text(title)
                .font(.caption2)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 12)
        .background(Color.secondaryBackground)
        .cornerRadius(10)
    }
}
