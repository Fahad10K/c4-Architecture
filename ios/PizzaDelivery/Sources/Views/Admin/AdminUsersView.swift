import SwiftUI

struct AdminUsersView: View {
    @ObservedObject var viewModel: AdminViewModel
    @State private var searchText = ""
    
    var filteredUsers: [User] {
        if searchText.isEmpty {
            return viewModel.users
        }
        return viewModel.users.filter {
            $0.name.localizedCaseInsensitiveContains(searchText) ||
            $0.email.localizedCaseInsensitiveContains(searchText)
        }
    }
    
    var body: some View {
        VStack(spacing: 0) {
            // Search
            HStack {
                Image(systemName: "magnifyingglass")
                    .foregroundColor(.secondary)
                TextField("Search users...", text: $searchText)
                    .autocapitalization(.none)
            }
            .padding(10)
            .background(Color.secondaryBackground)
            .cornerRadius(10)
            .padding()
            
            if viewModel.users.isEmpty {
                LoadingView(message: "Loading users...")
                    .task { await viewModel.loadUsers() }
            } else {
                ScrollView {
                    LazyVStack(spacing: 8) {
                        ForEach(filteredUsers) { user in
                            AdminUserRow(user: user)
                        }
                    }
                    .padding(.horizontal)
                }
            }
        }
    }
}

struct AdminUserRow: View {
    let user: User
    
    var body: some View {
        HStack(spacing: 12) {
            ZStack {
                Circle()
                    .fill(roleColor.opacity(0.1))
                    .frame(width: 44, height: 44)
                Text(String(user.name.prefix(1)).uppercased())
                    .font(.headline)
                    .foregroundColor(roleColor)
            }
            
            VStack(alignment: .leading, spacing: 2) {
                Text(user.name)
                    .font(.subheadline.bold())
                Text(user.email)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            
            Spacer()
            
            Text(user.role.rawValue.replacingOccurrences(of: "_", with: " ").capitalized)
                .font(.caption2.bold())
                .foregroundColor(roleColor)
                .padding(.horizontal, 8)
                .padding(.vertical, 4)
                .background(roleColor.opacity(0.1))
                .cornerRadius(6)
        }
        .padding(12)
        .background(Color.cardBackground)
        .cornerRadius(10)
    }
    
    private var roleColor: Color {
        switch user.role {
        case .admin: return .red
        case .storeStaff: return .orange
        case .deliveryPartner: return .blue
        case .customer: return .green
        }
    }
}
