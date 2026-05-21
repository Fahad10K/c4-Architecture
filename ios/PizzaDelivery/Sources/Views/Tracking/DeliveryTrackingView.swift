import SwiftUI

struct DeliveryTrackingView: View {
    let orderId: String
    @StateObject private var viewModel = DeliveryViewModel()
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // Map Area
                mapSection
                
                // Delivery Info
                VStack(spacing: 16) {
                    // ETA
                    if let eta = viewModel.eta {
                        HStack {
                            Image(systemName: "clock.fill")
                                .foregroundColor(.brand)
                            Text("Estimated arrival in")
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                            Text("\(eta) min")
                                .font(.title3.bold())
                                .foregroundColor(.brand)
                        }
                        .padding()
                        .frame(maxWidth: .infinity)
                        .background(Color.brand.opacity(0.05))
                        .cornerRadius(12)
                    }
                    
                    // Delivery Status
                    if let delivery = viewModel.delivery {
                        deliveryStatusSection(delivery: delivery)
                    }
                    
                    // Driver Info
                    if let delivery = viewModel.delivery, delivery.driverName != nil {
                        driverInfoSection(delivery: delivery)
                    }
                    
                    // Connection status
                    HStack {
                        Circle()
                            .fill(viewModel.isConnected ? Color.success : Color.orange)
                            .frame(width: 8, height: 8)
                        Text(viewModel.isConnected ? "Live tracking active" : "Connecting...")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
                .padding()
            }
            .navigationTitle("Track Delivery")
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
                await viewModel.loadDeliveryTracking(orderId: orderId)
                viewModel.connectWebSocket(orderId: orderId)
            }
            .onDisappear {
                viewModel.disconnectWebSocket()
            }
        }
    }
    
    private var mapSection: some View {
        ZStack {
            // Map placeholder (would use MapKit in production)
            Rectangle()
                .fill(Color.secondaryBackground)
                .frame(height: 300)
            
            VStack(spacing: 16) {
                Image(systemName: "map.fill")
                    .font(.system(size: 40))
                    .foregroundColor(.secondary)
                
                if let location = viewModel.driverLocation {
                    VStack(spacing: 4) {
                        Text("Driver Location")
                            .font(.caption.bold())
                            .foregroundColor(.secondary)
                        Text("Lat: \(String(format: "%.4f", location.lat))")
                            .font(.caption)
                        Text("Lng: \(String(format: "%.4f", location.lng))")
                            .font(.caption)
                    }
                } else {
                    Text("Waiting for driver location...")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
            }
            
            // Driver pin
            if viewModel.driverLocation != nil {
                Image(systemName: "car.fill")
                    .font(.title)
                    .foregroundColor(.brand)
                    .padding(8)
                    .background(Color.white)
                    .clipShape(Circle())
                    .shadow(radius: 4)
            }
        }
    }
    
    private func deliveryStatusSection(delivery: Delivery) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Delivery Status")
                .font(.headline)
            
            HStack(spacing: 0) {
                ForEach(DeliveryStatus.allCases, id: \.rawValue) { status in
                    VStack(spacing: 4) {
                        Circle()
                            .fill(isStatusReached(status, current: delivery.status) ? Color.brand : Color.gray.opacity(0.3))
                            .frame(width: 12, height: 12)
                        Text(status.displayName)
                            .font(.caption2)
                            .foregroundColor(isStatusReached(status, current: delivery.status) ? .primary : .secondary)
                    }
                    .frame(maxWidth: .infinity)
                }
            }
        }
    }
    
    private func driverInfoSection(delivery: Delivery) -> some View {
        HStack(spacing: 14) {
            ZStack {
                Circle()
                    .fill(Color.brand.opacity(0.1))
                    .frame(width: 50, height: 50)
                Image(systemName: "person.fill")
                    .foregroundColor(.brand)
            }
            
            VStack(alignment: .leading, spacing: 4) {
                Text(delivery.driverName ?? "Driver")
                    .font(.headline)
                if let phone = delivery.driverPhone {
                    Text(phone)
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }
            
            Spacer()
            
            Button {
                // Call driver action
            } label: {
                Image(systemName: "phone.fill")
                    .foregroundColor(.white)
                    .frame(width: 40, height: 40)
                    .background(Color.success)
                    .clipShape(Circle())
            }
        }
        .padding()
        .background(Color.secondaryBackground)
        .cornerRadius(12)
    }
    
    private func isStatusReached(_ status: DeliveryStatus, current: DeliveryStatus) -> Bool {
        let allStatuses: [DeliveryStatus] = [.assigned, .pickedUp, .onTheWay, .delivered]
        guard let currentIndex = allStatuses.firstIndex(of: current),
              let statusIndex = allStatuses.firstIndex(of: status) else {
            return false
        }
        return statusIndex <= currentIndex
    }
}

extension DeliveryStatus: CaseIterable {
    static var allCases: [DeliveryStatus] = [.assigned, .pickedUp, .onTheWay, .delivered]
}
