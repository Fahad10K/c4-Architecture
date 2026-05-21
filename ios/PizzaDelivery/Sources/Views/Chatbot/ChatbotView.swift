import SwiftUI

struct ChatbotView: View {
    @StateObject private var viewModel = ChatbotViewModel()
    @Environment(\.dismiss) private var dismiss
    @FocusState private var isInputFocused: Bool
    
    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // Messages
                ScrollViewReader { proxy in
                    ScrollView {
                        LazyVStack(spacing: 12) {
                            ForEach(viewModel.messages) { message in
                                ChatBubble(message: message) { suggestion in
                                    Task { await viewModel.sendMessage(suggestion) }
                                }
                                .id(message.id)
                            }
                            
                            if viewModel.isLoading {
                                HStack {
                                    TypingIndicator()
                                    Spacer()
                                }
                                .padding(.horizontal)
                                .id("typing")
                            }
                        }
                        .padding()
                    }
                    .onChange(of: viewModel.messages.count) { _ in
                        withAnimation {
                            proxy.scrollTo(viewModel.messages.last?.id ?? "typing", anchor: .bottom)
                        }
                    }
                }
                
                Divider()
                
                // Input
                HStack(spacing: 12) {
                    TextField("Ask me anything...", text: $viewModel.inputText, axis: .vertical)
                        .textFieldStyle(.plain)
                        .lineLimit(1...4)
                        .padding(12)
                        .background(Color.secondaryBackground)
                        .cornerRadius(20)
                        .focused($isInputFocused)
                    
                    Button {
                        Task { await viewModel.sendMessage() }
                    } label: {
                        Image(systemName: "paperplane.fill")
                            .foregroundColor(.white)
                            .frame(width: 40, height: 40)
                            .background(viewModel.inputText.trimmingCharacters(in: .whitespaces).isEmpty ? Color.gray : Color.brand)
                            .clipShape(Circle())
                    }
                    .disabled(viewModel.inputText.trimmingCharacters(in: .whitespaces).isEmpty || viewModel.isLoading)
                }
                .padding()
                .background(.ultraThinMaterial)
            }
            .navigationTitle("Pizza Assistant")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    HStack(spacing: 6) {
                        Image(systemName: "bubble.left.and.bubble.right.fill")
                            .foregroundColor(.brand)
                        Text("AI Powered")
                            .font(.caption2)
                            .foregroundColor(.secondary)
                    }
                }
                ToolbarItem(placement: .topBarTrailing) {
                    Button { dismiss() } label: {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(.secondary)
                    }
                }
            }
        }
    }
}

struct ChatBubble: View {
    let message: ChatMessage
    let onSuggestionTap: (String) -> Void
    
    var isUser: Bool { message.role == .user }
    
    var body: some View {
        VStack(alignment: isUser ? .trailing : .leading, spacing: 6) {
            HStack {
                if isUser { Spacer(minLength: 50) }
                
                VStack(alignment: .leading, spacing: 8) {
                    Text(message.content)
                        .font(.body)
                        .foregroundColor(isUser ? .white : .primary)
                    
                    Text(message.timestamp.relativeTime())
                        .font(.caption2)
                        .foregroundColor(isUser ? .white.opacity(0.7) : .secondary)
                }
                .padding(12)
                .background(isUser ? Color.brand : Color.secondaryBackground)
                .cornerRadius(16, corners: isUser ? [.topLeft, .topRight, .bottomLeft] : [.topLeft, .topRight, .bottomRight])
                
                if !isUser { Spacer(minLength: 50) }
            }
            
            // Suggestions
            if let suggestions = message.suggestions, !suggestions.isEmpty, !isUser {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(suggestions, id: \.self) { suggestion in
                            Button {
                                onSuggestionTap(suggestion)
                            } label: {
                                Text(suggestion)
                                    .font(.caption)
                                    .foregroundColor(.brand)
                                    .padding(.horizontal, 12)
                                    .padding(.vertical, 6)
                                    .background(Color.brand.opacity(0.1))
                                    .cornerRadius(16)
                            }
                        }
                    }
                }
            }
        }
    }
}

struct TypingIndicator: View {
    @State private var dotCount = 0
    let timer = Timer.publish(every: 0.5, on: .main, in: .common).autoconnect()
    
    var body: some View {
        HStack(spacing: 4) {
            ForEach(0..<3) { index in
                Circle()
                    .fill(Color.secondary)
                    .frame(width: 8, height: 8)
                    .opacity(dotCount % 3 == index ? 1 : 0.4)
            }
        }
        .padding(12)
        .background(Color.secondaryBackground)
        .cornerRadius(16)
        .onReceive(timer) { _ in
            dotCount += 1
        }
    }
}
