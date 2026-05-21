import Foundation

@MainActor
class MenuViewModel: ObservableObject {
    @Published var menuItems: [MenuItem] = []
    @Published var categories: [MenuCategory] = []
    @Published var selectedCategory: String = "All"
    @Published var selectedItem: MenuItem?
    @Published var isLoading = false
    @Published var errorMessage: String?
    
    private let menuService = MenuService.shared
    
    var filteredItems: [MenuItem] {
        if selectedCategory == "All" {
            return menuItems
        }
        return menuItems.filter { $0.category == selectedCategory }
    }
    
    var groupedItems: [String: [MenuItem]] {
        Dictionary(grouping: menuItems) { $0.category }
    }
    
    func loadMenu(storeId: String) async {
        isLoading = true
        errorMessage = nil
        
        do {
            menuItems = try await menuService.getMenu(storeId: storeId)
            let uniqueCategories = Set(menuItems.map { $0.category })
            categories = [MenuCategory(name: "All")] + uniqueCategories.map { MenuCategory(name: $0) }
        } catch {
            errorMessage = error.localizedDescription
        }
        
        isLoading = false
    }
    
    func loadMenuItem(id: String) async {
        do {
            selectedItem = try await menuService.getMenuItem(id: id)
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
