import SwiftUI

struct SearchView: View {
    @StateObject private var viewModel = SearchViewModel()
    @Environment(\.dismiss) private var dismiss
    @FocusState private var isSearchFocused: Bool
    
    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // Search Bar
                HStack(spacing: 12) {
                    HStack {
                        Image(systemName: "magnifyingglass")
                            .foregroundColor(.secondary)
                        TextField("Search pizza, stores, categories...", text: $viewModel.query)
                            .focused($isSearchFocused)
                            .autocapitalization(.none)
                            .onChange(of: viewModel.query) { _ in
                                Task { await viewModel.search() }
                            }
                        
                        if !viewModel.query.isEmpty {
                            Button {
                                viewModel.clearResults()
                            } label: {
                                Image(systemName: "xmark.circle.fill")
                                    .foregroundColor(.secondary)
                            }
                        }
                    }
                    .padding(12)
                    .background(Color.secondaryBackground)
                    .cornerRadius(12)
                    
                    Button("Cancel") {
                        dismiss()
                    }
                    .foregroundColor(.brand)
                }
                .padding()
                
                // Content
                if viewModel.query.isEmpty {
                    recentSearchesView
                } else if viewModel.isLoading {
                    LoadingView(message: "Searching...")
                } else if viewModel.results.isEmpty {
                    noResultsView
                } else {
                    searchResultsList
                }
            }
            .navigationBarHidden(true)
            .onAppear {
                isSearchFocused = true
            }
        }
    }
    
    private var recentSearchesView: some View {
        VStack(alignment: .leading, spacing: 16) {
            if !viewModel.recentSearches.isEmpty {
                HStack {
                    Text("Recent Searches")
                        .font(.headline)
                    Spacer()
                    Button("Clear") {
                        viewModel.clearRecentSearches()
                    }
                    .font(.caption)
                    .foregroundColor(.secondary)
                }
                .padding(.horizontal)
                
                ScrollView {
                    LazyVStack(alignment: .leading, spacing: 0) {
                        ForEach(viewModel.recentSearches, id: \.self) { search in
                            Button {
                                viewModel.query = search
                                Task { await viewModel.search() }
                            } label: {
                                HStack {
                                    Image(systemName: "clock")
                                        .foregroundColor(.secondary)
                                    Text(search)
                                        .foregroundColor(.primary)
                                    Spacer()
                                    Image(systemName: "arrow.up.left")
                                        .font(.caption)
                                        .foregroundColor(.secondary)
                                }
                                .padding(.horizontal)
                                .padding(.vertical, 12)
                            }
                            Divider().padding(.leading, 44)
                        }
                    }
                }
            } else {
                VStack(spacing: 12) {
                    Image(systemName: "magnifyingglass")
                        .font(.system(size: 40))
                        .foregroundColor(.secondary)
                    Text("Search for your favorites")
                        .font(.headline)
                        .foregroundColor(.secondary)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
        }
        .padding(.top, 8)
    }
    
    private var noResultsView: some View {
        VStack(spacing: 12) {
            Image(systemName: "magnifyingglass")
                .font(.system(size: 40))
                .foregroundColor(.secondary)
            Text("No results for \"\(viewModel.query)\"")
                .font(.headline)
            Text("Try a different search term")
                .font(.subheadline)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
    
    private var searchResultsList: some View {
        ScrollView {
            LazyVStack(spacing: 12) {
                ForEach(viewModel.results) { result in
                    SearchResultRow(result: result)
                }
            }
            .padding()
        }
    }
}

struct SearchResultRow: View {
    let result: SearchResult
    
    var body: some View {
        HStack(spacing: 12) {
            ZStack {
                RoundedRectangle(cornerRadius: 8)
                    .fill(resultColor.opacity(0.1))
                    .frame(width: 50, height: 50)
                Image(systemName: resultIcon)
                    .foregroundColor(resultColor)
            }
            
            VStack(alignment: .leading, spacing: 4) {
                Text(result.name)
                    .font(.subheadline.bold())
                
                if let description = result.description {
                    Text(description)
                        .font(.caption)
                        .foregroundColor(.secondary)
                        .lineLimit(1)
                }
                
                HStack {
                    Text(result.type.rawValue.replacingOccurrences(of: "_", with: " ").capitalized)
                        .font(.caption2)
                        .foregroundColor(.secondary)
                    
                    if let storeName = result.storeName {
                        Text("• \(storeName)")
                            .font(.caption2)
                            .foregroundColor(.secondary)
                    }
                }
            }
            
            Spacer()
            
            if let price = result.price {
                Text(price.currencyFormatted)
                    .font(.subheadline.bold())
                    .foregroundColor(.brand)
            }
        }
        .padding(12)
        .background(Color.cardBackground)
        .cornerRadius(10)
    }
    
    private var resultIcon: String {
        switch result.type {
        case .menuItem: return "fork.knife"
        case .store: return "storefront.fill"
        case .category: return "square.grid.2x2.fill"
        }
    }
    
    private var resultColor: Color {
        switch result.type {
        case .menuItem: return .brand
        case .store: return .blue
        case .category: return .orange
        }
    }
}
