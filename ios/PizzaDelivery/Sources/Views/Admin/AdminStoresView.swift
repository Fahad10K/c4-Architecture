import SwiftUI

struct AdminStoresView: View {
    @ObservedObject var viewModel: AdminViewModel
    
    var body: some View {
        VStack {
            if viewModel.stores.isEmpty {
                LoadingView(message: "Loading stores...")
                    .task { await viewModel.loadStores() }
            } else {
                ScrollView {
                    LazyVStack(spacing: 12) {
                        ForEach(viewModel.stores) { store in
                            AdminStoreRow(store: store)
                        }
                    }
                    .padding()
                }
            }
        }
    }
}

struct AdminStoreRow: View {
    let store: Store
    
    var body: some View {
        HStack(spacing: 14) {
            ZStack {
                Circle()
                    .fill(store.isOpen ? Color.success.opacity(0.1) : Color.red.opacity(0.1))
                    .frame(width: 44, height: 44)
                Image(systemName: "storefront.fill")
                    .foregroundColor(store.isOpen ? .success : .red)
            }
            
            VStack(alignment: .leading, spacing: 4) {
                Text(store.name)
                    .font(.subheadline.bold())
                Text(store.address.formatted)
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .lineLimit(1)
            }
            
            Spacer()
            
            VStack(alignment: .trailing, spacing: 4) {
                HStack(spacing: 2) {
                    Circle()
                        .fill(store.isOpen ? Color.success : Color.red)
                        .frame(width: 8, height: 8)
                    Text(store.isOpen ? "Open" : "Closed")
                        .font(.caption2)
                        .foregroundColor(store.isOpen ? .success : .red)
                }
                
                if let rating = store.rating {
                    HStack(spacing: 2) {
                        Image(systemName: "star.fill")
                            .font(.caption2)
                            .foregroundColor(.accent2)
                        Text(String(format: "%.1f", rating))
                            .font(.caption2)
                    }
                }
            }
        }
        .padding(14)
        .background(Color.cardBackground)
        .cornerRadius(10)
        .shadow(color: .black.opacity(0.04), radius: 2, y: 1)
    }
}
