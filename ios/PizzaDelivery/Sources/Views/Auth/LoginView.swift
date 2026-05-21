import SwiftUI

struct LoginView: View {
    @EnvironmentObject var authViewModel: AuthViewModel
    @State private var email = ""
    @State private var password = ""
    @State private var showRegister = false
    
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 32) {
                    // Logo
                    VStack(spacing: 12) {
                        Image(systemName: "flame.fill")
                            .font(.system(size: 60))
                            .foregroundColor(.brand)
                        
                        Text("Pizza Delivery")
                            .font(.largeTitle.bold())
                        
                        Text("Fresh & Fast, Right to Your Door")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                    .padding(.top, 60)
                    
                    // Login Form
                    VStack(spacing: 16) {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Email")
                                .font(.caption.bold())
                                .foregroundColor(.secondary)
                            
                            TextField("your@email.com", text: $email)
                                .textContentType(.emailAddress)
                                .keyboardType(.emailAddress)
                                .autocapitalization(.none)
                                .padding()
                                .background(Color.secondaryBackground)
                                .cornerRadius(12)
                        }
                        
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Password")
                                .font(.caption.bold())
                                .foregroundColor(.secondary)
                            
                            SecureField("Enter password", text: $password)
                                .textContentType(.password)
                                .padding()
                                .background(Color.secondaryBackground)
                                .cornerRadius(12)
                        }
                        
                        Button(action: {
                            Task { await authViewModel.login(email: email, password: password) }
                        }) {
                            HStack {
                                if authViewModel.isLoading {
                                    ProgressView()
                                        .tint(.white)
                                } else {
                                    Text("Sign In")
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
                    
                    // Register Link
                    HStack {
                        Text("Don't have an account?")
                            .foregroundColor(.secondary)
                        Button("Sign Up") {
                            showRegister = true
                        }
                        .foregroundColor(.brand)
                        .fontWeight(.semibold)
                    }
                    .font(.subheadline)
                    
                    // Demo credentials
                    VStack(spacing: 8) {
                        Text("Demo Credentials")
                            .font(.caption.bold())
                            .foregroundColor(.secondary)
                        
                        Button {
                            email = "demo@pizza.com"
                            password = "password123"
                        } label: {
                            Text("Use Demo Account")
                                .font(.caption)
                                .foregroundColor(.brand)
                                .padding(.horizontal, 12)
                                .padding(.vertical, 6)
                                .background(Color.brand.opacity(0.1))
                                .cornerRadius(6)
                        }
                    }
                }
                .padding()
            }
            .navigationDestination(isPresented: $showRegister) {
                RegisterView()
            }
            .alert("Error", isPresented: $authViewModel.showError) {
                Button("OK") { authViewModel.showError = false }
            } message: {
                Text(authViewModel.errorMessage ?? "An error occurred")
            }
        }
    }
    
    private var isFormValid: Bool {
        !email.trimmingCharacters(in: .whitespaces).isEmpty &&
        !password.trimmingCharacters(in: .whitespaces).isEmpty &&
        email.contains("@")
    }
}
