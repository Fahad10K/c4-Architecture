import SwiftUI

struct AdminOrdersView: View {
    @ObservedObject var viewModel: AdminViewModel
    @State private var filterStatus: OrderStatus?
    
    var filteredOrders: [Order] {
        if let status = filterStatus {
            return viewModel.orders.filter { $0.status == status }
        }
        return viewModel.orders
    }
    
    var body: some View {
        VStack(spacing: 0) {
            // Filter Bar
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    FilterChip(title: "All", isSelected: filterStatus == nil) {
                        filterStatus = nil
                    }
                    FilterChip(title: "Placed", isSelected: filterStatus == .placed) {
                        filterStatus = .placed
                    }
                    FilterChip(title: "Preparing", isSelected: filterStatus == .preparing) {
                        filterStatus = .preparing
                    }
                    FilterChip(title: "On Way", isSelected: filterStatus == .onTheWay) {
                        filterStatus = .onTheWay
                    }
                    FilterChip(title: "Delivered", isSelected: filterStatus == .delivered) {
                        filterStatus = .delivered
                    }
                    FilterChip(title: "Cancelled", isSelected: filterStatus == .cancelled) {
                        filterStatus = .cancelled
                    }
                }
                .padding(.horizontal)
            }
            .padding(.vertical, 8)
            
            if viewModel.orders.isEmpty {
                LoadingView(message: "Loading orders...")
                    .task { await viewModel.loadOrders() }
            } else {
                ScrollView {
                    LazyVStack(spacing: 12) {
                        ForEach(filteredOrders) { order in
                            AdminOrderRow(order: order)
                        }
                    }
                    .padding()
                }
            }
        }
    }
}

struct AdminOrderRow: View {
    let order: Order
    
    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text("Order #\(String(order.id.prefix(8)))")
                        .font(.subheadline.bold())
                    Text(order.createdAt.formattedDate())
                        .font(.caption2)
                        .foregroundColor(.secondary)
                }
                Spacer()
                StatusBadge(status: order.status)
            }
            
            HStack {
                Text("\(order.items.count) items")
                    .font(.caption)
                    .foregroundColor(.secondary)
                Spacer()
                Text(order.total.currencyFormatted)
                    .font(.headline)
                    .foregroundColor(.brand)
            }
        }
        .padding(14)
        .background(Color.cardBackground)
        .cornerRadius(10)
        .shadow(color: .black.opacity(0.04), radius: 2, y: 1)
    }
}

struct FilterChip: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.caption.weight(isSelected ? .semibold : .regular))
                .foregroundColor(isSelected ? .white : .secondary)
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .background(isSelected ? Color.brand : Color.secondaryBackground)
                .cornerRadius(16)
        }
    }
}
