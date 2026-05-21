import SwiftUI

struct MenuItemCard: View {
    let item: MenuItem
    let onTap: () -> Void
    let onAddToCart: () -> Void
    
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Image placeholder
            ZStack {
                Rectangle()
                    .fill(Color.secondaryBackground)
                    .aspectRatio(1.3, contentMode: .fit)
                
                if let imageUrl = item.image, !imageUrl.isEmpty {
                    AsyncImage(url: URL(string: imageUrl)) { image in
                        image.resizable().aspectRatio(contentMode: .fill)
                    } placeholder: {
                        Image(systemName: "fork.knife")
                            .font(.system(size: 30))
                            .foregroundColor(.secondary)
                    }
                } else {
                    Image(systemName: "fork.knife")
                        .font(.system(size: 30))
                        .foregroundColor(.secondary)
                }
                
                if !item.isAvailable {
                    Color.black.opacity(0.5)
                    Text("Unavailable")
                        .font(.caption.bold())
                        .foregroundColor(.white)
                        .padding(6)
                        .background(Color.red.opacity(0.8))
                        .cornerRadius(4)
                }
            }
            .clipped()
            .cornerRadius(12, corners: [.topLeft, .topRight])
            
            VStack(alignment: .leading, spacing: 6) {
                Text(item.name)
                    .font(.subheadline.bold())
                    .lineLimit(2)
                
                Text(item.description)
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .lineLimit(2)
                
                HStack {
                    Text(item.price.currencyFormatted)
                        .font(.headline)
                        .foregroundColor(.brand)
                    
                    Spacer()
                    
                    if item.isAvailable {
                        Button(action: onAddToCart) {
                            Image(systemName: "plus.circle.fill")
                                .font(.title2)
                                .foregroundColor(.brand)
                        }
                    }
                }
            }
            .padding(12)
        }
        .background(Color.cardBackground)
        .cornerRadius(12)
        .shadow(color: .black.opacity(0.08), radius: 4, y: 2)
        .onTapGesture(perform: onTap)
    }
}

struct RoundedCorner: Shape {
    var radius: CGFloat = .infinity
    var corners: UIRectCorner = .allCorners
    
    func path(in rect: CGRect) -> Path {
        let path = UIBezierPath(roundedRect: rect, byRoundingCorners: corners, cornerRadii: CGSize(width: radius, height: radius))
        return Path(path.cgPath)
    }
}

extension View {
    func cornerRadius(_ radius: CGFloat, corners: UIRectCorner) -> some View {
        clipShape(RoundedCorner(radius: radius, corners: corners))
    }
}
