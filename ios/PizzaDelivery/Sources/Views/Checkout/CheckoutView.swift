import SwiftUI

struct CheckoutView: View {
    @EnvironmentObject var cartViewModel: CartViewModel
    @EnvironmentObject var authViewModel: AuthViewModel
    @StateObject private var orderViewModel = OrderViewModel()
    @Environment(\.dismiss) private var dismiss
    
    @State private var selectedAddress: Address?
    @State private var paymentMethod = "card"
    @State private var showAddAddress = false
    @State private var showOrderSuccess = false
    @State private var newOrder: Order?
    
    let paymentMethods = [
        ("card", "Credit/Debit Card", "creditcard.fill"),
        ("cash", "Cash on Delivery", "banknote.fill"),
        ("wallet", "Digital Wallet", "wallet.pass.fill")
    ]
    
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 24) {
                    // Delivery Address
                    deliveryAddressSection
                    
                    Divider()
                    
                    // Payment Method
                    paymentMethodSection
                    
                    Divider()
                    
                    // Order Summary
                    orderSummarySection
                    
                    Divider()
                    
                    // Price Breakdown
                    if let cart = cartViewModel.cart {
                        priceBreakdown(cart: cart)
                    }
                }
                .padding()
                .padding(.bottom, 100)
            }
            .overlay(alignment: .bottom) {
                placeOrderButton
            }
            .navigationTitle("Checkout")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button { dismiss() } label: {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(.secondary)
                    }
                }
            }
            .alert("Order Placed!", isPresented: $showOrderSuccess) {
                Button("OK") { dismiss() }
            } message: {
                Text("Your order has been placed successfully! You can track it in the Orders tab.")
            }
            .loadingOverlay(orderViewModel.isLoading)
            .onAppear {
                selectedAddress = authViewModel.currentUser?.addresses?.first
            }
        }
    }
    
    private var deliveryAddressSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Label("Delivery Address", systemImage: "location.fill")
                    .font(.headline)
                Spacer()
                Button("Add New") { showAddAddress = true }
                    .font(.caption)
                    .foregroundColor(.brand)
            }
            
            if let addresses = authViewModel.currentUser?.addresses, !addresses.isEmpty {
                ForEach(addresses) { address in
                    AddressSelectionRow(
                        address: address,
                        isSelected: selectedAddress?.id == address.id
                    ) {
                        selectedAddress = address
                    }
                }
            } else {
                HStack {
                    Image(systemName: "mappin.slash")
                        .foregroundColor(.secondary)
                    Text("No saved addresses")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
                .padding()
                .frame(maxWidth: .infinity)
                .background(Color.secondaryBackground)
                .cornerRadius(10)
            }
        }
    }
    
    private var paymentMethodSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Label("Payment Method", systemImage: "creditcard.fill")
                .font(.headline)
            
            ForEach(paymentMethods, id: \.0) { method in
                Button {
                    paymentMethod = method.0
                } label: {
                    HStack {
                        Image(systemName: method.2)
                            .foregroundColor(paymentMethod == method.0 ? .brand : .secondary)
                            .frame(width: 30)
                        Text(method.1)
                            .foregroundColor(.primary)
                        Spacer()
                        Image(systemName: paymentMethod == method.0 ? "checkmark.circle.fill" : "circle")
                            .foregroundColor(paymentMethod == method.0 ? .brand : .secondary)
                    }
                    .padding(14)
                    .background(paymentMethod == method.0 ? Color.brand.opacity(0.05) : Color.secondaryBackground)
                    .cornerRadius(10)
                    .overlay(
                        RoundedRectangle(cornerRadius: 10)
                            .stroke(paymentMethod == method.0 ? Color.brand : Color.clear, lineWidth: 1.5)
                    )
                }
            }
        }
    }
    
    private var orderSummarySection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Label("Order Summary", systemImage: "bag.fill")
                .font(.headline)
            
            if let cart = cartViewModel.cart {
                ForEach(cart.items) { item in
                    HStack {
                        Text("\(item.quantity)x")
                            .font(.caption.bold())
                            .foregroundColor(.brand)
                            .frame(width: 30)
                        Text(item.name)
                            .font(.subheadline)
                        Spacer()
                        Text(item.itemTotal.currencyFormatted)
                            .font(.subheadline)
                    }
                }
            }
        }
    }
    
    private func priceBreakdown(cart: Cart) -> some View {
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
                    .font(.title2.bold())
                    .foregroundColor(.brand)
            }
        }
    }
    
    private var placeOrderButton: some View {
        Button {
            Task { await placeOrder() }
        } label: {
            HStack {
                Image(systemName: "checkmark.seal.fill")
                Text("Place Order")
                    .fontWeight(.semibold)
            }
            .font(.headline)
            .foregroundColor(.white)
            .frame(maxWidth: .infinity)
            .padding()
            .background(canPlaceOrder ? Color.brand : Color.gray)
            .cornerRadius(14)
        }
        .disabled(!canPlaceOrder)
        .padding()
        .background(.ultraThinMaterial)
    }
    
    private var canPlaceOrder: Bool {
        selectedAddress != nil && !orderViewModel.isLoading
    }
    
    private func placeOrder() async {
        guard let cart = cartViewModel.cart, let address = selectedAddress else { return }
        
        let items = cart.items.map { item in
            OrderItemRequest(menuItemId: item.menuItemId, quantity: item.quantity, customizations: nil)
        }
        
        let order = await orderViewModel.createOrder(
            storeId: cart.storeId,
            items: items,
            address: address,
            paymentMethod: paymentMethod
        )
        
        if order != nil {
            await cartViewModel.clearCart()
            showOrderSuccess = true
        }
    }
}

struct AddressSelectionRow: View {
    let address: Address
    let isSelected: Bool
    let onTap: () -> Void
    
    var body: some View {
        Button(action: onTap) {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    HStack {
                        Text(address.label)
                            .font(.subheadline.bold())
                        if address.isDefault {
                            Text("Default")
                                .font(.caption2)
                                .foregroundColor(.brand)
                                .padding(.horizontal, 6)
                                .padding(.vertical, 2)
                                .background(Color.brand.opacity(0.1))
                                .cornerRadius(4)
                        }
                    }
                    Text("\(address.street), \(address.city), \(address.state) \(address.zipCode)")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                Spacer()
                Image(systemName: isSelected ? "checkmark.circle.fill" : "circle")
                    .foregroundColor(isSelected ? .brand : .secondary)
            }
            .padding(12)
            .background(isSelected ? Color.brand.opacity(0.05) : Color.secondaryBackground)
            .cornerRadius(10)
            .overlay(
                RoundedRectangle(cornerRadius: 10)
                    .stroke(isSelected ? Color.brand : Color.clear, lineWidth: 1.5)
            )
        }
        .buttonStyle(.plain)
    }
}
