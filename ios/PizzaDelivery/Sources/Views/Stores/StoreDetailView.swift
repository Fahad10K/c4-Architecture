import SwiftUI

struct StoreDetailView: View {
    let store: Store
    
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                // Store Header
                VStack(alignment: .center, spacing: 12) {
                    ZStack {
                        Circle()
                            .fill(Color.brand.opacity(0.1))
                            .frame(width: 80, height: 80)
                        Image(systemName: "storefront.fill")
                            .font(.system(size: 36))
                            .foregroundColor(.brand)
                    }
                    
                    Text(store.name)
                        .font(.title.bold())
                    
                    HStack(spacing: 16) {
                        if store.isOpen {
                            Label("Open", systemImage: "checkmark.circle.fill")
                                .foregroundColor(.success)
                        } else {
                            Label("Closed", systemImage: "xmark.circle.fill")
                                .foregroundColor(.red)
                        }
                        
                        if let rating = store.rating {
                            Label(String(format: "%.1f", rating), systemImage: "star.fill")
                                .foregroundColor(.accent2)
                        }
                    }
                    .font(.subheadline)
                }
                .frame(maxWidth: .infinity)
                .padding()
                
                Divider()
                
                // Address
                VStack(alignment: .leading, spacing: 8) {
                    Label("Address", systemImage: "mappin.and.ellipse")
                        .font(.headline)
                    Text(store.address.formatted)
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
                .padding(.horizontal)
                
                // Phone
                if let phone = store.phone {
                    VStack(alignment: .leading, spacing: 8) {
                        Label("Phone", systemImage: "phone.fill")
                            .font(.headline)
                        Text(phone)
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                    .padding(.horizontal)
                }
                
                // Hours
                if store.hours != nil {
                    VStack(alignment: .leading, spacing: 8) {
                        Label("Hours", systemImage: "clock.fill")
                            .font(.headline)
                        Text("Mon-Fri: 10:00 AM - 10:00 PM")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                        Text("Sat-Sun: 11:00 AM - 11:00 PM")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                    .padding(.horizontal)
                }
                
                // View Menu Button
                NavigationLink {
                    MenuView(store: store)
                } label: {
                    Text("View Menu")
                        .font(.headline)
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(store.isOpen ? Color.brand : Color.gray)
                        .cornerRadius(12)
                }
                .disabled(!store.isOpen)
                .padding(.horizontal)
            }
            .padding(.vertical)
        }
        .navigationTitle("Store Details")
        .navigationBarTitleDisplayMode(.inline)
    }
}
