import SwiftUI

struct RegisterView: View {
    @EnvironmentObject var authViewModel: AuthViewModel
    @Environment(\.dismiss) private var dismiss
    
    @State private var name = ""
    @State private var email = ""
    @State private var phone = ""
    @State private var password = ""
    @State private var confirmPassword = ""
    
    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                // Header
                VStack(spacing: 8) {
                    Image(systemName: "person.badge.plus.fill")
                        .font(.system(size: 50))
                        .foregroundColor(.brand)
                    
                    Text("Create Account")
                        .font(.title.bold())
                    
                    Text("Join us for delicious pizza!")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
                .padding(.top, 20)
                
                // Form
                VStack(spacing: 16) {
                    FormField(title: "Full Name", placeholder: "John Doe", text: $name)
                    
                    FormField(title: "Email", placeholder: "your@email.com", text: $email, keyboardType: .emailAddress)
                    
                    FormField(title: "Phone (Optional)", placeholder: "+1 234 567 8900", text: $phone, keyboardType: .phonePad)
                    
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Password")
                            .font(.caption.bold())
                            .foregroundColor(.secondary)
                        SecureField("Min 6 characters", text: $password)
                            .textContentType(.newPassword)
                            .padding()
                            .background(Color.secondaryBackground)
                            .cornerRadius(12)
                    }
                    
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Confirm Password")
                            .font(.caption.bold())
                            .foregroundColor(.secondary)
                        SecureField("Re-enter password", text: $confirmPassword)
                            .textContentType(.newPassword)
                            .padding()
                            .background(Color.secondaryBackground)
                            .cornerRadius(12)
                        
                        if !confirmPassword.isEmpty && password != confirmPassword {
                            Text("Passwords don't match")
                                .font(.caption)
                                .foregroundColor(.red)
                        }
                    }
                    
                    Button(action: {
                        Task {
                            await authViewModel.register(
                                email: email,
                                password: password,
                                name: name,
                                phone: phone.isEmpty ? nil : phone
                            )
                        }
                    }) {
                        HStack {
                            if authViewModel.isLoading {
                                ProgressView().tint(.white)
                            } else {
                                Text("Create Account")
                                    .fontWeight(.semibold)
                            }
                        }
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(isFormValid ? Color.brand : Color.gray)
                        .foregroundColor(.white)
                        .cornerRadius(12)
                    }
                    .disabled(!isFormValid || authViewModel.isLoading)
                }
                .padding(.horizontal)
            }
            .padding()
        }
        .navigationTitle("Sign Up")
        .navigationBarTitleDisplayMode(.inline)
        .alert("Error", isPresented: $authViewModel.showError) {
            Button("OK") { authViewModel.showError = false }
        } message: {
            Text(authViewModel.errorMessage ?? "An error occurred")
        }
    }
    
    private var isFormValid: Bool {
        !name.trimmingCharacters(in: .whitespaces).isEmpty &&
        !email.trimmingCharacters(in: .whitespaces).isEmpty &&
        email.contains("@") &&
        password.count >= 6 &&
        password == confirmPassword
    }
}

struct FormField: View {
    let title: String
    let placeholder: String
    @Binding var text: String
    var keyboardType: UIKeyboardType = .default
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.caption.bold())
                .foregroundColor(.secondary)
            TextField(placeholder, text: $text)
                .keyboardType(keyboardType)
                .autocapitalization(keyboardType == .emailAddress ? .none : .words)
                .padding()
                .background(Color.secondaryBackground)
                .cornerRadius(12)
        }
    }
}
