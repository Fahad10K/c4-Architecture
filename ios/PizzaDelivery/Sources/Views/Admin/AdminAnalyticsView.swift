import SwiftUI

struct AdminAnalyticsView: View {
    @ObservedObject var viewModel: AdminViewModel
    
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                if viewModel.analytics == nil {
                    LoadingView(message: "Loading analytics...")
                        .task { await viewModel.loadAnalytics() }
                } else {
                    // Orders by Status
                    if let ordersByStatus = viewModel.analytics?.ordersByStatus, !ordersByStatus.isEmpty {
                        analyticsSection(title: "Orders by Status") {
                            ForEach(ordersByStatus) { item in
                                HStack {
                                    Circle()
                                        .fill(statusColor(item.status))
                                        .frame(width: 10, height: 10)
                                    Text(item.status.capitalized)
                                        .font(.subheadline)
                                    Spacer()
                                    Text("\(item.count)")
                                        .font(.subheadline.bold())
                                }
                                .padding(.vertical, 4)
                            }
                        }
                    }
                    
                    // Revenue by Day
                    if let revenueByDay = viewModel.analytics?.revenueByDay, !revenueByDay.isEmpty {
                        analyticsSection(title: "Revenue (Last 7 Days)") {
                            ForEach(revenueByDay) { item in
                                HStack {
                                    Text(item.date)
                                        .font(.caption)
                                        .foregroundColor(.secondary)
                                        .frame(width: 80, alignment: .leading)
                                    
                                    GeometryReader { geo in
                                        let maxRevenue = revenueByDay.map(\.revenue).max() ?? 1
                                        let width = CGFloat(item.revenue / maxRevenue) * geo.size.width
                                        
                                        RoundedRectangle(cornerRadius: 4)
                                            .fill(Color.brand.opacity(0.7))
                                            .frame(width: max(width, 4), height: 20)
                                    }
                                    .frame(height: 20)
                                    
                                    Text(item.revenue.currencyFormatted)
                                        .font(.caption.bold())
                                        .frame(width: 70, alignment: .trailing)
                                }
                                .padding(.vertical, 2)
                            }
                        }
                    }
                    
                    // Top Items
                    if let topItems = viewModel.analytics?.topItems, !topItems.isEmpty {
                        analyticsSection(title: "Top Selling Items") {
                            ForEach(Array(topItems.prefix(10).enumerated()), id: \.element.id) { index, item in
                                HStack {
                                    Text("#\(index + 1)")
                                        .font(.caption.bold())
                                        .foregroundColor(.brand)
                                        .frame(width: 30)
                                    
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text(item.name)
                                            .font(.subheadline)
                                        Text("\(item.orders) orders")
                                            .font(.caption)
                                            .foregroundColor(.secondary)
                                    }
                                    
                                    Spacer()
                                    
                                    Text(item.revenue.currencyFormatted)
                                        .font(.subheadline.bold())
                                        .foregroundColor(.success)
                                }
                                .padding(.vertical, 4)
                            }
                        }
                    }
                    
                    // Customer Growth
                    if let growth = viewModel.analytics?.customerGrowth, !growth.isEmpty {
                        analyticsSection(title: "Customer Growth") {
                            ForEach(growth) { point in
                                HStack {
                                    Text(point.date)
                                        .font(.caption)
                                        .foregroundColor(.secondary)
                                    Spacer()
                                    Text("+\(point.count)")
                                        .font(.subheadline.bold())
                                        .foregroundColor(.success)
                                }
                                .padding(.vertical, 2)
                            }
                        }
                    }
                }
            }
            .padding()
        }
    }
    
    private func analyticsSection<Content: View>(title: String, @ViewBuilder content: () -> Content) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(title)
                .font(.headline)
            
            VStack(spacing: 4) {
                content()
            }
            .padding()
            .background(Color.cardBackground)
            .cornerRadius(12)
            .shadow(color: .black.opacity(0.04), radius: 2, y: 1)
        }
    }
    
    private func statusColor(_ status: String) -> Color {
        switch status.lowercased() {
        case "delivered": return .success
        case "cancelled": return .red
        case "preparing": return .orange
        case "on_the_way": return .blue
        default: return .secondary
        }
    }
}
