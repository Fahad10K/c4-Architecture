import SwiftUI

struct ProfileView: View {
    @EnvironmentObject var authViewModel: AuthViewModel
    @State private var showEditProfile = false
    @State private var showAddresses = false
    @State private var showAdmin = false
    @State private var showLogoutConfirmation = false
    
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 20) {
                    // Profile Header
                    profileHeader
                    
                    // Menu Items
                    VStack(spacing: 2) {
                        ProfileMenuItem(icon: "person.fill", title: "Edit Profile", color: .blue) {
                            showEditProfile = true
                        }
                        ProfileMenuItem(icon: "mappin.and.ellipse", title: "Addresses", color: .green) {
                            showAddresses = true
                        }
                        ProfileMenuItem(icon: "bag.fill", title: "Order History", color: .orange) {
                            // Navigate to orders
                        }
                        ProfileMenuItem(icon: "heart.fill", title: "Favorites", color: .pink) {
                            // Navigate to favorites
                        }
                        ProfileMenuItem(icon: "bell.fill", title: "Notification Settings", color: .purple) {
                            // Navigate to settings
                        }
                        ProfileMenuItem(icon: "creditcard.fill", title: "Payment Methods", color: .indigo) {
                            // Navigate to payment methods
                        }
                    }
                    .background(Color.cardBackground)
                    .cornerRadius(12)
                    
                    // Admin Panel (if admin)
                    if authViewModel.currentUser?.role == .admin || authViewModel.currentUser?.role == .storeStaff {
                        VStack(spacing: 2) {
                            ProfileMenuItem(icon: "gearshape.fill", title: "Admin Panel", color: .brand) {
                                showAdmin = true
                            }
                        }
                        .background(Color.cardBackground)
                        .cornerRadius(12)
                    }
                    
                    // App Info
                    VStack(spacing: 2) {
                        ProfileMenuItem(icon: "questionmark.circle.fill", title: "Help & Support", color: .teal) {}
                        ProfileMenuItem(icon: "doc.text.fill", title: "Terms & Conditions", color: .secondary) {}
                        ProfileMenuItem(icon: "shield.fill", title: "Privacy Policy", color: .secondary) {}
                    }
                    .background(Color.cardBackground)
                    .cornerRadius(12)
                    
                    // Logout
                    Button {
                        showLogoutConfirmation = true
                    } label: {
                        HStack {
                            Image(systemName: "rectangle.portrait.and.arrow.right")
                            Text("Sign Out")
                        }
                        .font(.headline)
                        .foregroundColor(.red)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.red.opacity(0.08))
                        .cornerRadius(12)
                    }
                    
                    // Version
                    Text("Pizza Delivery v1.0.0")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                .padding()
            }
            .navigationTitle("Profile")
            .sheet(isPresented: $showEditProfile) {
                EditProfileView()
            }
            .sheet(isPresented: $showAddresses) {
                AddressesView()
            }
            .sheet(isPresented: $showAdmin) {
                AdminDashboardView()
            }
            .alert("Sign Out", isPresented: $showLogoutConfirmation) {
                Button("Cancel", role: .cancel) {}
                Button("Sign Out", role: .destructive) {
                    Task { await authViewModel.logout() }
                }
            } message: {
                Text("Are you sure you want to sign out?")
            }
        }
    }
    
    private var profileHeader: some View {
        VStack(spacing: 12) {
            ZStack {
                Circle()
                    .fill(Color.brand.opacity(0.1))
                    .frame(width: 80, height: 80)
                Text(initials)
                    .font(.title.bold())
                    .foregroundColor(.brand)
            }
            
            Text(authViewModel.currentUser?.name ?? "User")
                .font(.title2.bold())
            
            Text(authViewModel.currentUser?.email ?? "")
                .font(.subheadline)
                .foregroundColor(.secondary)
            
            if let phone = authViewModel.currentUser?.phone, !phone.isEmpty {
                Text(phone)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            
            if let role = authViewModel.currentUser?.role, role != .customer {
                Text(role.rawValue.replacingOccurrences(of: "_", with: " ").capitalized)
                    .font(.caption.bold())
                    .foregroundColor(.white)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 4)
                    .background(Color.brand)
                    .cornerRadius(6)
            }
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(Color.cardBackground)
        .cornerRadius(12)
    }
    
    private var initials: String {
        let name = authViewModel.currentUser?.name ?? "U"
        let parts = name.split(separator: " ")
        if parts.count >= 2 {
            return "\(parts[0].prefix(1))\(parts[1].prefix(1))".uppercased()
        }
        return String(name.prefix(2)).uppercased()
    }
}

struct ProfileMenuItem: View {
    let icon: String
    let title: String
    let color: Color
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack(spacing: 14) {
                Image(systemName: icon)
                    .foregroundColor(color)
                    .frame(width: 24)
                Text(title)
                    .font(.subheadline)
                    .foregroundColor(.primary)
                Spacer()
                Image(systemName: "chevron.right")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
        }
    }
}

struct EditProfileView: View {
    @EnvironmentObject var authViewModel: AuthViewModel
    @Environment(\.dismiss) private var dismiss
    @State private var name = ""
    @State private var phone = ""
    
    var body: some View {
        NavigationStack {
            Form {
                Section("Personal Information") {
                    TextField("Full Name", text: $name)
                    TextField("Phone Number", text: $phone)
                        .keyboardType(.phonePad)
                }
                
                Section("Email") {
                    Text(authViewModel.currentUser?.email ?? "")
                        .foregroundColor(.secondary)
                }
            }
            .navigationTitle("Edit Profile")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Save") {
                        Task {
                            await authViewModel.updateProfile(name: name, phone: phone.isEmpty ? nil : phone)
                            dismiss()
                        }
                    }
                    .fontWeight(.semibold)
                    .disabled(name.isEmpty)
                }
            }
            .onAppear {
                name = authViewModel.currentUser?.name ?? ""
                phone = authViewModel.currentUser?.phone ?? ""
            }
        }
    }
}
