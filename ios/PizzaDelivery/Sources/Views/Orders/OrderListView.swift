import SwiftUI

struct OrderListView: View {
    @StateObject private var viewModel = OrderViewModel()
    @State private var selectedTab = 0
    
    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // Tab Selector
                Picker("", selection: $selectedTab) {
                    Text("Active").tag(0)
                    Text("Past").tag(1)
                }
                .pickerStyle(.segmented)
                .padding()
                
                if viewModel.isLoading && viewModel.orders.isEmpty {
                    LoadingView(message: "Loading orders...")
                } else if (selectedTab == 0 ? viewModel.activeOrders : viewModel.pastOrders).isEmpty {
                    emptyState
                } else {
                    ScrollView {
                        LazyVStack(spacing: 14) {
                            ForEach(selectedTab == 0 ? viewModel.activeOrders : viewModel.pastOrders) { order in
                                NavigationLink {
                                    OrderDetailView(orderId: order.id)
                                } label: {
                                    OrderCard(order: order)
                                }
                                .buttonStyle(.plain)
                            }
                        }
                        .padding()
                    }
                }
            }
            .navigationTitle("Orders")
            .refreshable {
                await viewModel.loadOrders()
            }
            .task {
                await viewModel.loadOrders()
            }
        }
    }
    
    private var emptyState: some View {
        VStack(spacing: 16) {
            Image(systemName: selectedTab == 0 ? "bag" : "clock")
                .font(.system(size: 50))
                .foregroundColor(.secondary)
            Text(selectedTab == 0 ? "No active orders" : "No past orders")
                .font(.title3.bold())
            Text(selectedTab == 0 ? "Your active orders will appear here" : "Your completed orders will appear here")
                .font(.subheadline)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
