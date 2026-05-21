import SwiftUI

struct AddressesView: View {
    @EnvironmentObject var authViewModel: AuthViewModel
    @Environment(\.dismiss) private var dismiss
    @State private var showAddAddress = false
    
    var body: some View {
        NavigationStack {
            Group {
                if let addresses = authViewModel.currentUser?.addresses, !addresses.isEmpty {
                    List {
                        ForEach(addresses) { address in
                            AddressRow(address: address)
                        }
                    }
                } else {
                    VStack(spacing: 16) {
                        Image(systemName: "mappin.slash")
                            .font(.system(size: 50))
                            .foregroundColor(.secondary)
                        Text("No Saved Addresses")
                            .font(.title3.bold())
                        Text("Add your delivery addresses for a faster checkout")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.center)
                        
                        Button {
                            showAddAddress = true
                        } label: {
                            Label("Add Address", systemImage: "plus")
                                .font(.headline)
                                .foregroundColor(.white)
                                .padding(.horizontal, 24)
                                .padding(.vertical, 12)
                                .background(Color.brand)
                                .cornerRadius(10)
                        }
                    }
                    .padding()
                }
            }
            .navigationTitle("Addresses")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("Done") { dismiss() }
                }
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        showAddAddress = true
                    } label: {
                        Image(systemName: "plus")
                    }
                }
            }
            .sheet(isPresented: $showAddAddress) {
                AddAddressView()
            }
        }
    }
}

struct AddressRow: View {
    let address: Address
    
    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: "mappin.circle.fill")
                .font(.title2)
                .foregroundColor(.brand)
            
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
        }
    }
}

struct AddAddressView: View {
    @Environment(\.dismiss) private var dismiss
    @State private var label = "Home"
    @State private var street = ""
    @State private var city = ""
    @State private var state = ""
    @State private var zipCode = ""
    @State private var isDefault = false
    
    let labels = ["Home", "Work", "Other"]
    
    var body: some View {
        NavigationStack {
            Form {
                Section("Label") {
                    Picker("Address Type", selection: $label) {
                        ForEach(labels, id: \.self) { l in
                            Text(l).tag(l)
                        }
                    }
                    .pickerStyle(.segmented)
                }
                
                Section("Address Details") {
                    TextField("Street Address", text: $street)
                    TextField("City", text: $city)
                    TextField("State", text: $state)
                    TextField("ZIP Code", text: $zipCode)
                        .keyboardType(.numberPad)
                }
                
                Section {
                    Toggle("Set as default", isOn: $isDefault)
                }
            }
            .navigationTitle("Add Address")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Save") {
                        // Save address via API
                        dismiss()
                    }
                    .fontWeight(.semibold)
                    .disabled(street.isEmpty || city.isEmpty || state.isEmpty || zipCode.isEmpty)
                }
            }
        }
    }
}
