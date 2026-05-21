import SwiftUI

struct MenuView: View {
    let store: Store
    @StateObject private var viewModel = MenuViewModel()
    @EnvironmentObject var cartViewModel: CartViewModel
    @State private var showCart = false
    @State private var selectedItem: MenuItem?
    
    var body: some View {
        ZStack(alignment: .bottom) {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    // Store Info Header
                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            Text(store.name)
                                .font(.title2.bold())
                            Text(store.address.formatted)
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                        Spacer()
                        if let rating = store.rating {
                            HStack(spacing: 4) {
                                Image(systemName: "star.fill")
                                    .foregroundColor(.accent2)
                                Text(String(format: "%.1f", rating))
                                    .fontWeight(.semibold)
                            }
                        }
                    }
                    .padding(.horizontal)
                    
                    // Category Filter
                    if !viewModel.categories.isEmpty {
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 10) {
                                ForEach(viewModel.categories) { category in
                                    CategoryChip(
                                        name: category.name,
                                        isSelected: viewModel.selectedCategory == category.name
                                    ) {
                                        viewModel.selectedCategory = category.name
                                    }
                                }
                            }
                            .padding(.horizontal)
                        }
                    }
                    
                    // Menu Items Grid
                    if viewModel.isLoading {
                        LoadingView(message: "Loading menu...")
                            .frame(height: 200)
                    } else if viewModel.filteredItems.isEmpty {
                        VStack(spacing: 12) {
                            Image(systemName: "tray")
                                .font(.system(size: 40))
                                .foregroundColor(.secondary)
                            Text("No items found")
                                .font(.headline)
                                .foregroundColor(.secondary)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.top, 40)
                    } else {
                        LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 14) {
                            ForEach(viewModel.filteredItems) { item in
                                MenuItemCard(
                                    item: item,
                                    onTap: { selectedItem = item },
                                    onAddToCart: {
                                        Task {
                                            await cartViewModel.addToCart(
                                                menuItem: item,
                                                quantity: 1,
                                                customizations: nil,
                                                storeId: store.id
                                            )
                                        }
                                    }
                                )
                            }
                        }
                        .padding(.horizontal)
                    }
                }
                .padding(.vertical)
                .padding(.bottom, 80)
            }
            
            // Cart Preview Bar
            if cartViewModel.itemCount > 0 {
                cartPreviewBar
            }
        }
        .navigationTitle("Menu")
        .navigationBarTitleDisplayMode(.inline)
        .sheet(item: $selectedItem) { item in
            MenuItemDetailView(item: item, storeId: store.id)
        }
        .sheet(isPresented: $showCart) {
            CartView()
        }
        .task {
            await viewModel.loadMenu(storeId: store.id)
        }
    }
    
    private var cartPreviewBar: some View {
        Button {
            showCart = true
        } label: {
            HStack {
                HStack(spacing: 8) {
                    Text("\(cartViewModel.itemCount)")
                        .font(.caption.bold())
                        .foregroundColor(.brand)
                        .frame(width: 24, height: 24)
                        .background(Color.white)
                        .clipShape(Circle())
                    
                    Text("View Cart")
                        .font(.headline)
                        .foregroundColor(.white)
                }
                
                Spacer()
                
                Text(cartViewModel.total.currencyFormatted)
                    .font(.headline)
                    .foregroundColor(.white)
            }
            .padding()
            .background(Color.brand)
            .cornerRadius(16)
            .padding()
        }
    }
}

struct CategoryChip: View {
    let name: String
    let isSelected: Bool
    let onTap: () -> Void
    
    var body: some View {
        Button(action: onTap) {
            Text(name)
                .font(.subheadline.weight(isSelected ? .semibold : .regular))
                .foregroundColor(isSelected ? .white : .primary)
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                .background(isSelected ? Color.brand : Color.secondaryBackground)
                .cornerRadius(20)
        }
    }
}
