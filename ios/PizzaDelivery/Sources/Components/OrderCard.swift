import SwiftUI

struct OrderCard: View {
    let order: Order
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text("Order #\(String(order.id.prefix(8)))")
                        .font(.headline)
                    Text(order.storeName ?? "Pizza Store")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
                
                Spacer()
                
                StatusBadge(status: order.status)
            }
            
            Divider()
            
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text("\(order.items.count) item(s)")
                        .font(.caption)
                        .foregroundColor(.secondary)
                    Text(order.createdAt.formattedDate())
                        .font(.caption2)
                        .foregroundColor(.secondary)
                }
                
                Spacer()
                
                Text(order.total.currencyFormatted)
                    .font(.title3.bold())
                    .foregroundColor(.brand)
            }
            
            if order.status != .delivered && order.status != .cancelled {
                ProgressView(value: order.status.progress)
                    .tint(.brand)
            }
        }
        .padding(16)
        .background(Color.cardBackground)
        .cornerRadius(12)
        .shadow(color: .black.opacity(0.06), radius: 4, y: 2)
    }
}

struct StatusBadge: View {
    let status: OrderStatus
    
    var color: Color {
        switch status {
        case .delivered: return .success
        case .cancelled: return .red
        case .onTheWay, .pickedUp: return .blue
        case .preparing, .ready: return .orange
        default: return .secondary
        }
    }
    
    var body: some View {
        HStack(spacing: 4) {
            Image(systemName: status.icon)
                .font(.caption2)
            Text(status.displayName)
                .font(.caption.bold())
        }
        .foregroundColor(color)
        .padding(.horizontal, 8)
        .padding(.vertical, 4)
        .background(color.opacity(0.1))
        .cornerRadius(6)
    }
}
