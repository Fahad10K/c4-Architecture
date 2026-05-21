import SwiftUI

struct HomeView: View {
    @StateObject private var viewModel = HomeViewModel()
    @EnvironmentObject var authViewModel: AuthViewModel
    @EnvironmentObject var cartViewModel: CartViewModel
    @State private var showSearch = false
    @State private var showCart = false
    @State private var showChatbot = false
    
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 24) {
                    // Welcome Header
                    headerSection
                    
                    // Search Bar
                    searchBar
                    
                    // Offers Section
                    if !viewModel.offers.isEmpty {
                        offersSection
                    }
                    
                    // Nearby Stores
                    if !viewModel.nearbyStores.isEmpty {
                        nearbyStoresSection
                    }
                    
                    // Recommendations
                    if !viewModel.recommendations.isEmpty {
                        recommendationsSection
                    }
                    
                    // Quick Actions
                    quickActionsSection
                }
                .padding()
            }
            .refreshable {
                await viewModel.loadHomeData()
            }
            .navigationTitle("")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        showCart = true
                    } label: {
                        ZStack(alignment: .topTrailing) {
                            Image(systemName: "cart.fill")
                                .foregroundColor(.brand)
                            if cartViewModel.itemCount > 0 {
                                Text("\(cartViewModel.itemCount)")
                                    .font(.caption2.bold())
                                    .foregroundColor(.white)
                                    .frame(width: 16, height: 16)
                                    .background(Color.red)
                                    .clipShape(Circle())
                                    .offset(x: 8, y: -8)
                            }
                        }
                    }
                }
            }
            .sheet(isPresented: $showSearch) {
                SearchView()
            }
            .sheet(isPresented: $showCart) {
                CartView()
            }
            .sheet(isPresented: $showChatbot) {
                ChatbotView()
            }
            .task {
                await viewModel.loadHomeData()
                await cartViewModel.loadCart()
            }
            .overlay(alignment: .bottomTrailing) {
                // Floating Chatbot Button
                Button {
                    showChatbot = true
                } label: {
                    Image(systemName: "bubble.left.and.bubble.right.fill")
                        .font(.title2)
                        .foregroundColor(.white)
                        .frame(width: 56, height: 56)
                        .background(Color.brand)
                        .clipShape(Circle())
                        .shadow(color: .brand.opacity(0.4), radius: 8, y: 4)
                }
                .padding(.trailing, 16)
                .padding(.bottom, 16)
            }
        }
    }
    
    // MARK: - Sections
    
    private var headerSection: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text("Hello, \(authViewModel.currentUser?.name ?? "Pizza Lover")! 👋")
                .font(.title2.bold())
            Text("What are you craving today?")
                .font(.subheadline)
                .foregroundColor(.secondary)
        }
    }
    
    private var searchBar: some View {
        Button {
            showSearch = true
        } label: {
            HStack {
                Image(systemName: "magnifyingglass")
                    .foregroundColor(.secondary)
                Text("Search pizzas, stores...")
                    .foregroundColor(.secondary)
                Spacer()
            }
            .padding()
            .background(Color.secondaryBackground)
            .cornerRadius(12)
        }
    }
    
    private var offersSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Special Offers")
                .font(.title3.bold())
            
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 14) {
                    ForEach(viewModel.offers) { offer in
                        OfferCard(offer: offer)
                    }
                }
            }
        }
    }
    
    private var nearbyStoresSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("Nearby Stores")
                    .font(.title3.bold())
                Spacer()
                NavigationLink("See All") {
                    StoreListView()
                }
                .font(.subheadline)
                .foregroundColor(.brand)
            }
            
            ForEach(viewModel.nearbyStores.prefix(3)) { store in
                NavigationLink {
                    MenuView(store: store)
                } label: {
                    StoreCard(store: store)
                }
                .buttonStyle(.plain)
            }
        }
    }
    
    private var recommendationsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Recommended for You")
                .font(.title3.bold())
            
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 14) {
                    ForEach(viewModel.recommendations) { rec in
                        if let items = rec.items {
                            ForEach(items) { item in
                                MenuItemCard(item: item, onTap: {}, onAddToCart: {
                                    Task {
                                        await cartViewModel.addToCart(
                                            menuItem: item,
                                            quantity: 1,
                                            customizations: nil,
                                            storeId: item.storeId
                                        )
                                    }
                                })
                                .frame(width: 180)
                            }
                        }
                    }
                }
            }
        }
    }
    
    private var quickActionsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Quick Actions")
                .font(.title3.bold())
            
            LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
                QuickActionButton(icon: "bag.fill", title: "My Orders", color: .blue) {
                    // Navigate to orders tab
                }
                QuickActionButton(icon: "mappin.and.ellipse", title: "Find Store", color: .green) {
                    // Navigate to stores tab
                }
                QuickActionButton(icon: "heart.fill", title: "Favorites", color: .pink) {
                    // Navigate to favorites
                }
                QuickActionButton(icon: "clock.fill", title: "Reorder", color: .orange) {
                    // Reorder last order
                }
            }
        }
    }
}

struct OfferCard: View {
    let offer: Offer
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Image(systemName: "tag.fill")
                    .foregroundColor(.white)
                Text("\(Int(offer.discount))% OFF")
                    .font(.caption.bold())
                    .foregroundColor(.white)
            }
            .padding(.horizontal, 10)
            .padding(.vertical, 4)
            .background(Color.brand)
            .cornerRadius(6)
            
            Text(offer.title)
                .font(.headline)
                .lineLimit(2)
            
            Text(offer.description)
                .font(.caption)
                .foregroundColor(.secondary)
                .lineLimit(2)
            
            HStack {
                Text("Code: \(offer.code)")
                    .font(.caption.bold())
                    .foregroundColor(.brand)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(Color.brand.opacity(0.1))
                    .cornerRadius(4)
            }
        }
        .padding(16)
        .frame(width: 220)
        .background(Color.cardBackground)
        .cornerRadius(12)
        .shadow(color: .black.opacity(0.06), radius: 4, y: 2)
    }
}

struct QuickActionButton: View {
    let icon: String
    let title: String
    let color: Color
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            VStack(spacing: 10) {
                Image(systemName: icon)
                    .font(.title2)
                    .foregroundColor(color)
                Text(title)
                    .font(.caption.bold())
                    .foregroundColor(.primary)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 20)
            .background(color.opacity(0.08))
            .cornerRadius(12)
        }
    }
}
