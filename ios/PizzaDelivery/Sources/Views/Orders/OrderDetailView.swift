import SwiftUI

struct OrderDetailView: View {
    let orderId: String
    @StateObject private var viewModel = OrderViewModel()
    @State private var showTracking = false
    @State private var showCancelConfirmation = false
    
    var body: some View {
        Group {
            if viewModel.isLoading && viewModel.selectedOrder == nil {
                LoadingView(message: "Loading order...")
            } else if let order = viewModel.selectedOrder {
                orderContent(order: order)
            } else {
                ErrorView(message: viewModel.errorMessage ?? "Order not found") {
                    Task { await viewModel.loadOrder(id: orderId) }
                }
            }
        }
        .navigationTitle("Order Details")
        .navigationBarTitleDisplayMode(.inline)
        .task {
            await viewModel.loadOrder(id: orderId)
        }
    }
    
    private func orderContent(order: Order) -> some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                // Order Status
                VStack(spacing: 12) {
                    Image(systemName: order.status.icon)
                        .font(.system(size: 44))
                        .foregroundColor(.brand)
                    
                    Text(order.status.displayName)
                        .font(.title2.bold())
                    
                    if order.status != .delivered && order.status != .cancelled {
                        ProgressView(value: order.status.progress)
                            .tint(.brand)
                            .padding(.horizontal, 40)
                    }
                }
                .frame(maxWidth: .infinity)
                .padding()
                
                Divider()
                
                // Order Info
                VStack(alignment: .leading, spacing: 12) {
                    HStack {
                        Text("Order #")
                            .foregroundColor(.secondary)
                        Text(String(order.id.prefix(8)).uppercased())
                            .font(.headline)
                    }
                    
                    HStack {
                        Text("Placed at")
                            .foregroundColor(.secondary)
                        Text(order.createdAt.formattedDate())
                    }
                    
                    if let storeName = order.storeName {
                        HStack {
                            Text("Store")
                                .foregroundColor(.secondary)
                            Text(storeName)
                        }
                    }
                }
                .font(.subheadline)
                .padding(.horizontal)
                
                Divider()
                
                // Items
                VStack(alignment: .leading, spacing: 12) {
                    Text("Items")
                        .font(.headline)
                    
                    ForEach(order.items) { item in
                        HStack {
                            Text("\(item.quantity)x")
                                .font(.caption.bold())
                                .foregroundColor(.brand)
                                .frame(width: 30)
                            Text(item.name)
                                .font(.subheadline)
                            Spacer()
                            Text((item.price * Double(item.quantity)).currencyFormatted)
                                .font(.subheadline)
                        }
                    }
                }
                .padding(.horizontal)
                
                Divider()
                
                // Total
                HStack {
                    Text("Total")
                        .font(.title3.bold())
                    Spacer()
                    Text(order.total.currencyFormatted)
                        .font(.title3.bold())
                        .foregroundColor(.brand)
                }
                .padding(.horizontal)
                
                // Delivery Info
                if let delivery = order.delivery {
                    Divider()
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Delivery")
                            .font(.headline)
                        if let driverName = delivery.driverName {
                            HStack {
                                Image(systemName: "person.fill")
                                    .foregroundColor(.secondary)
                                Text(driverName)
                            }
                        }
                        if let eta = delivery.eta {
                            HStack {
                                Image(systemName: "clock.fill")
                                    .foregroundColor(.secondary)
                                Text("ETA: \(eta) min")
                            }
                        }
                    }
                    .font(.subheadline)
                    .padding(.horizontal)
                }
                
                // Actions
                VStack(spacing: 12) {
                    if order.status != .delivered && order.status != .cancelled {
                        if order.status == .onTheWay || order.status == .pickedUp {
                            Button {
                                showTracking = true
                            } label: {
                                Label("Track Delivery", systemImage: "location.fill")
                                    .font(.headline)
                                    .foregroundColor(.white)
                                    .frame(maxWidth: .infinity)
                                    .padding()
                                    .background(Color.blue)
                                    .cornerRadius(12)
                            }
                        }
                        
                        if order.status == .placed || order.status == .confirmed {
                            Button {
                                showCancelConfirmation = true
                            } label: {
                                Label("Cancel Order", systemImage: "xmark.circle.fill")
                                    .font(.headline)
                                    .foregroundColor(.red)
                                    .frame(maxWidth: .infinity)
                                    .padding()
                                    .background(Color.red.opacity(0.1))
                                    .cornerRadius(12)
                            }
                        }
                    }
                }
                .padding(.horizontal)
                .padding(.top, 8)
            }
            .padding(.vertical)
        }
        .sheet(isPresented: $showTracking) {
            DeliveryTrackingView(orderId: order.id)
        }
        .alert("Cancel Order", isPresented: $showCancelConfirmation) {
            Button("Keep Order", role: .cancel) {}
            Button("Cancel Order", role: .destructive) {
                Task { await viewModel.cancelOrder(id: orderId) }
            }
        } message: {
            Text("Are you sure you want to cancel this order?")
        }
    }
}
