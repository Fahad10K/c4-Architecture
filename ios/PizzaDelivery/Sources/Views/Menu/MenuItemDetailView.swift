import SwiftUI

struct MenuItemDetailView: View {
    let item: MenuItem
    let storeId: String
    @EnvironmentObject var cartViewModel: CartViewModel
    @Environment(\.dismiss) private var dismiss
    
    @State private var quantity = 1
    @State private var selectedOptions: [String: Set<String>] = [:]
    @State private var addedToCart = false
    
    var totalPrice: Double {
        var price = item.price * Double(quantity)
        for (_, options) in selectedOptions {
            for optionName in options {
                if let customizations = item.customizations {
                    for customization in customizations {
                        if let option = customization.options.first(where: { $0.name == optionName }) {
                            price += option.price * Double(quantity)
                        }
                    }
                }
            }
        }
        return price
    }
    
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    // Image
                    ZStack {
                        Rectangle()
                            .fill(Color.secondaryBackground)
                            .frame(height: 220)
                        
                        if let imageUrl = item.image, !imageUrl.isEmpty {
                            AsyncImage(url: URL(string: imageUrl)) { image in
                                image.resizable().aspectRatio(contentMode: .fill)
                            } placeholder: {
                                Image(systemName: "fork.knife.circle.fill")
                                    .font(.system(size: 60))
                                    .foregroundColor(.brand)
                            }
                        } else {
                            Image(systemName: "fork.knife.circle.fill")
                                .font(.system(size: 60))
                                .foregroundColor(.brand)
                        }
                    }
                    .frame(height: 220)
                    .clipped()
                    
                    VStack(alignment: .leading, spacing: 12) {
                        // Name & Price
                        HStack(alignment: .top) {
                            VStack(alignment: .leading, spacing: 4) {
                                Text(item.name)
                                    .font(.title2.bold())
                                Text(item.category)
                                    .font(.caption)
                                    .foregroundColor(.white)
                                    .padding(.horizontal, 8)
                                    .padding(.vertical, 3)
                                    .background(Color.brand.opacity(0.8))
                                    .cornerRadius(4)
                            }
                            Spacer()
                            Text(item.price.currencyFormatted)
                                .font(.title2.bold())
                                .foregroundColor(.brand)
                        }
                        
                        // Description
                        Text(item.description)
                            .font(.body)
                            .foregroundColor(.secondary)
                        
                        Divider()
                        
                        // Customizations
                        if let customizations = item.customizations, !customizations.isEmpty {
                            ForEach(customizations) { customization in
                                VStack(alignment: .leading, spacing: 8) {
                                    HStack {
                                        Text(customization.name)
                                            .font(.headline)
                                        if customization.required {
                                            Text("Required")
                                                .font(.caption2)
                                                .foregroundColor(.red)
                                                .padding(.horizontal, 6)
                                                .padding(.vertical, 2)
                                                .background(Color.red.opacity(0.1))
                                                .cornerRadius(4)
                                        }
                                    }
                                    
                                    ForEach(customization.options) { option in
                                        HStack {
                                            let isSelected = selectedOptions[customization.name]?.contains(option.name) ?? false
                                            
                                            Button {
                                                toggleOption(customization: customization.name, option: option.name)
                                            } label: {
                                                HStack {
                                                    Image(systemName: isSelected ? "checkmark.circle.fill" : "circle")
                                                        .foregroundColor(isSelected ? .brand : .secondary)
                                                    Text(option.name)
                                                        .foregroundColor(.primary)
                                                    Spacer()
                                                    if option.price > 0 {
                                                        Text("+\(option.price.currencyFormatted)")
                                                            .font(.caption)
                                                            .foregroundColor(.secondary)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                .padding(.vertical, 4)
                            }
                            
                            Divider()
                        }
                        
                        // Quantity Selector
                        HStack {
                            Text("Quantity")
                                .font(.headline)
                            Spacer()
                            HStack(spacing: 16) {
                                Button {
                                    if quantity > 1 { quantity -= 1 }
                                } label: {
                                    Image(systemName: "minus.circle.fill")
                                        .font(.title2)
                                        .foregroundColor(quantity > 1 ? .brand : .gray)
                                }
                                .disabled(quantity <= 1)
                                
                                Text("\(quantity)")
                                    .font(.title3.bold())
                                    .frame(width: 30)
                                
                                Button {
                                    quantity += 1
                                } label: {
                                    Image(systemName: "plus.circle.fill")
                                        .font(.title2)
                                        .foregroundColor(.brand)
                                }
                            }
                        }
                    }
                    .padding(.horizontal)
                }
                .padding(.bottom, 100)
            }
            .overlay(alignment: .bottom) {
                // Add to Cart Button
                Button {
                    Task {
                        let customizations = selectedOptions.map { key, values in
                            SelectedCustomization(name: key, selectedOptions: Array(values), additionalPrice: 0)
                        }
                        await cartViewModel.addToCart(
                            menuItem: item,
                            quantity: quantity,
                            customizations: customizations.isEmpty ? nil : customizations,
                            storeId: storeId
                        )
                        addedToCart = true
                        DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
                            dismiss()
                        }
                    }
                } label: {
                    HStack {
                        if addedToCart {
                            Image(systemName: "checkmark.circle.fill")
                            Text("Added!")
                        } else {
                            Image(systemName: "cart.badge.plus")
                            Text("Add to Cart - \(totalPrice.currencyFormatted)")
                        }
                    }
                    .font(.headline)
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(addedToCart ? Color.success : Color.brand)
                    .cornerRadius(14)
                    .padding()
                }
                .disabled(!item.isAvailable || addedToCart)
                .background(.ultraThinMaterial)
            }
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button { dismiss() } label: {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(.secondary)
                    }
                }
            }
        }
    }
    
    private func toggleOption(customization: String, option: String) {
        var options = selectedOptions[customization] ?? Set<String>()
        if options.contains(option) {
            options.remove(option)
        } else {
            options.insert(option)
        }
        selectedOptions[customization] = options
    }
}
