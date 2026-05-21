import SwiftUI

struct StoreListView: View {
    @StateObject private var viewModel = StoreViewModel()
    @State private var searchText = ""
    
    var filteredStores: [Store] {
        if searchText.isEmpty {
            return viewModel.stores
        }
        return viewModel.stores.filter {
            $0.name.localizedCaseInsensitiveContains(searchText) ||
            $0.address.formatted.localizedCaseInsensitiveContains(searchText)
        }
    }
    
    var body: some View {
        NavigationStack {
            Group {
                if viewModel.isLoading && viewModel.stores.isEmpty {
                    LoadingView(message: "Finding stores...")
                } else if let error = viewModel.errorMessage, viewModel.stores.isEmpty {
                    ErrorView(message: error) {
                        Task { await viewModel.loadStores() }
                    }
                } else {
                    ScrollView {
                        LazyVStack(spacing: 12) {
                            ForEach(filteredStores) { store in
                                NavigationLink {
                                    MenuView(store: store)
                                } label: {
                                    StoreCard(store: store)
                                }
                                .buttonStyle(.plain)
                            }
                        }
                        .padding()
                    }
                }
            }
            .navigationTitle("Stores")
            .searchable(text: $searchText, prompt: "Search stores...")
            .refreshable {
                await viewModel.loadStores()
            }
            .task {
                await viewModel.loadStores()
            }
        }
    }
}
