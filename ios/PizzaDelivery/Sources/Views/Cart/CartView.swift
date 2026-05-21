import SwiftUI

struct CartView: View {
    @EnvironmentObject var cartViewModel: CartViewModel
    @Environment(\.dismiss) private var dismiss
    @State private var couponCode = ""
    @State private var showCheckout = false
    
    var body: some View {
        NavigationStack {
            Group {
                if let cart = cartViewModel.cart, !cart.items.isEmpty {
                    cartContent(cart: cart)
                } else {
                    emptyCartView
                }
            }
            .navigationTitle("Cart")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button { dismiss() } label: {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(.secondary)
                    }
                }
                
                if cartViewModel.cart?.items.isEmpty == false {
                    ToolbarItem(placement: .topBarLeading) {
                        Button("Clear") {
                            Task { await cartViewModel.clearCart() }
                        }
                        .foregroundColor(.red)
                    }
                }
            }
            .sheet(isPresented: $showCheckout) {
                CheckoutView()
            }
            .task {
                await cartViewModel.loadCart()
            }
        }
    }
    
    private func cartContent(cart: Cart) -> some View {
        VStack(spacing: 0) {
            ScrollView {
                VStack(spacing: 16) {
                    // Cart Items
                    ForEach(cart.items) { item in
                        CartItemRow(item: item) { newQuantity in
                            Task { await cartViewModel.updateQuantity(itemId: item.id, quantity: newQuantity) }
                        } onRemove: {
                            Task { await cartViewModel.removeItem(itemId: item.id) }
                        }
                    }
                    
                    Divider()
                    
                    // Coupon Section
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Promo Code")
                            .font(.headline)
                        HStack {
                            TextField("Enter code", text: $couponCode)
                                .padding()
                                .background(Color.secondaryBackground)
                                .cornerRadius(10)
                            
                            Button("Apply") {
                                Task { await cartViewModel.applyCoupon(code: couponCode) }
                            }
                            .padding(.horizontal, 16)
                            .padding(.vertical, 12)
                            .background(Color.brand)
                            .foregroundColor(.white)
                            .cornerRadius(10)
                            .disabled(couponCode.isEmpty)
                        }
                    }
                    
                    Divider()
                    
                    // Price Breakdown
                    VStack(spacing: 10) {
                        PriceRow(title: "Subtotal", value: cart.subtotal)
                        PriceRow(title: "Tax", value: cart.tax)
                        PriceRow(title: "Delivery Fee", value: cart.deliveryFee)
                        if cart.discount > 0 {
                            PriceRow(title: "Discount", value: -cart.discount, color: .success)
                        }
                        Divider()
                        HStack {
                            Text("Total")
                                .font(.title3.bold())
                            Spacer()
                            Text(cart.total.currencyFormatted)
                                .font(.title3.bold())
                                .foregroundColor(.brand)
                        }
                    }
                }
                .padding()
            }
            
            // Checkout Button
            Button {
                showCheckout = true
            } label: {
                Text("Proceed to Checkout")
                    .font(.headline)
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.brand)
                    .cornerRadius(14)
            }
            .padding()
            .background(.ultraThinMaterial)
        }
    }
    
    private var emptyCartView: some View {
        VStack(spacing: 20) {
            Image(systemName: "cart")
                .font(.system(size: 60))
                .foregroundColor(.secondary)
            
            Text("Your cart is empty")
                .font(.title2.bold())
            
            Text("Browse our menu and add some delicious items!")
                .font(.subheadline)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
            
            Button {
                dismiss()
            } label: {
                Text("Browse Menu")
                    .font(.headline)
                    .foregroundColor(.white)
                    .padding(.horizontal, 32)
                    .padding(.vertical, 14)
                    .background(Color.brand)
                    .cornerRadius(12)
            }
        }
        .padding()
    }
}

struct CartItemRow: View {
    let item: CartItem
    let onUpdateQuantity: (Int) -> Void
    let onRemove: () -> Void
    
    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            ZStack {
                RoundedRectangle(cornerRadius: 8)
                    .fill(Color.secondaryBackground)
                    .frame(width: 60, height: 60)
                Image(systemName: "fork.knife")
                    .foregroundColor(.secondary)
            }
            
            VStack(alignment: .leading, spacing: 4) {
                Text(item.name)
                    .font(.subheadline.bold())
                
                if !item.customizations.isEmpty {
                    Text(item.customizations.map { $0.selectedOptions.joined(separator: ", ") }.joined(separator: " | "))
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                
                Text(item.itemTotal.currencyFormatted)
                    .font(.subheadline)
                    .foregroundColor(.brand)
            }
            
            Spacer()
            
            VStack(spacing: 4) {
                HStack(spacing: 10) {
                    Button { onUpdateQuantity(item.quantity - 1) } label: {
                        Image(systemName: "minus.circle")
                            .foregroundColor(.secondary)
                    }
                    Text("\(item.quantity)")
                        .font(.subheadline.bold())
                    Button { onUpdateQuantity(item.quantity + 1) } label: {
                        Image(systemName: "plus.circle")
                            .foregroundColor(.brand)
                    }
                }
                
                Button(action: onRemove) {
                    Image(systemName: "trash")
                        .font(.caption)
                        .foregroundColor(.red)
                }
            }
        }
        .padding(12)
        .background(Color.cardBackground)
        .cornerRadius(10)
    }
}

struct PriceRow: View {
    let title: String
    let value: Double
    var color: Color = .primary
    
    var body: some View {
        HStack {
            Text(title)
                .font(.subheadline)
                .foregroundColor(.secondary)
            Spacer()
            Text(value.currencyFormatted)
                .font(.subheadline)
                .foregroundColor(color)
        }
    }
}
