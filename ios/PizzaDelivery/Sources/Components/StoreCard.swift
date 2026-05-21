import SwiftUI

struct StoreCard: View {
    let store: Store
    
    var body: some View {
        HStack(spacing: 14) {
            ZStack {
                Circle()
                    .fill(store.isOpen ? Color.brand.opacity(0.1) : Color.gray.opacity(0.1))
                    .frame(width: 56, height: 56)
                
                Image(systemName: "storefront.fill")
                    .font(.title2)
                    .foregroundColor(store.isOpen ? .brand : .gray)
            }
            
            VStack(alignment: .leading, spacing: 4) {
                HStack {
                    Text(store.name)
                        .font(.headline)
                    
                    if store.isOpen {
                        Text("Open")
                            .font(.caption2.bold())
                            .foregroundColor(.success)
                            .padding(.horizontal, 6)
                            .padding(.vertical, 2)
                            .background(Color.success.opacity(0.1))
                            .cornerRadius(4)
                    } else {
                        Text("Closed")
                            .font(.caption2.bold())
                            .foregroundColor(.red)
                            .padding(.horizontal, 6)
                            .padding(.vertical, 2)
                            .background(Color.red.opacity(0.1))
                            .cornerRadius(4)
                    }
                }
                
                Text(store.address.formatted)
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .lineLimit(1)
                
                if let rating = store.rating {
                    HStack(spacing: 4) {
                        Image(systemName: "star.fill")
                            .font(.caption2)
                            .foregroundColor(.accent2)
                        Text(String(format: "%.1f", rating))
                            .font(.caption.bold())
                    }
                }
            }
            
            Spacer()
            
            Image(systemName: "chevron.right")
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .padding(14)
        .background(Color.cardBackground)
        .cornerRadius(12)
        .shadow(color: .black.opacity(0.05), radius: 3, y: 1)
    }
}
