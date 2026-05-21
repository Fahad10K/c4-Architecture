import Foundation
import Combine

@MainActor
class SearchViewModel: ObservableObject {
    @Published var query = ""
    @Published var results: [SearchResult] = []
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var recentSearches: [String] = []
    
    private let searchService = SearchService.shared
    private var searchTask: Task<Void, Never>?
    
    init() {
        loadRecentSearches()
    }
    
    func search() async {
        guard !query.trimmingCharacters(in: .whitespaces).isEmpty else {
            results = []
            return
        }
        
        searchTask?.cancel()
        
        searchTask = Task {
            isLoading = true
            do {
                try await Task.sleep(nanoseconds: 300_000_000) // 300ms debounce
                guard !Task.isCancelled else { return }
                results = try await searchService.search(query: query)
                saveRecentSearch(query)
            } catch {
                if !Task.isCancelled {
                    errorMessage = error.localizedDescription
                }
            }
            isLoading = false
        }
    }
    
    func clearResults() {
        results = []
        query = ""
    }
    
    private func saveRecentSearch(_ term: String) {
        var searches = recentSearches
        searches.removeAll { $0 == term }
        searches.insert(term, at: 0)
        if searches.count > 10 {
            searches = Array(searches.prefix(10))
        }
        recentSearches = searches
        UserDefaults.standard.set(searches, forKey: "recentSearches")
    }
    
    private func loadRecentSearches() {
        recentSearches = UserDefaults.standard.stringArray(forKey: "recentSearches") ?? []
    }
    
    func clearRecentSearches() {
        recentSearches = []
        UserDefaults.standard.removeObject(forKey: "recentSearches")
    }
}
