package com.pizza.delivery.service;

import com.pizza.delivery.dto.ChatMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;
import software.amazon.awssdk.services.kendra.KendraClient;
import software.amazon.awssdk.services.kendra.model.QueryRequest;
import software.amazon.awssdk.services.kendra.model.QueryResponse;
import software.amazon.awssdk.services.kendra.model.QueryResultItem;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final BedrockRuntimeClient bedrockClient;
    private final KendraClient kendraClient;

    @Value("${aws.bedrock.model-id:anthropic.claude-3-haiku-20240307-v1:0}")
    private String modelId;

    @Value("${aws.kendra.index-id:}")
    private String kendraIndexId;

    private final Map<String, List<ChatMessageDTO>> conversationHistory = new HashMap<>();

    public ChatMessageDTO processMessage(String userId, String userMessage) {
        conversationHistory.computeIfAbsent(userId, k -> new ArrayList<>());
        conversationHistory.get(userId).add(ChatMessageDTO.builder()
                .id(UUID.randomUUID().toString()).role("user")
                .content(userMessage).timestamp(LocalDateTime.now()).build());

        String response = generateRAGResponse(userId, userMessage);

        ChatMessageDTO botMessage = ChatMessageDTO.builder()
                .id(UUID.randomUUID().toString()).role("assistant")
                .content(response).timestamp(LocalDateTime.now()).build();

        conversationHistory.get(userId).add(botMessage);
        return botMessage;
    }

    public List<ChatMessageDTO> getHistory(String userId) {
        return conversationHistory.getOrDefault(userId, new ArrayList<>());
    }

    public void clearHistory(String userId) {
        conversationHistory.remove(userId);
    }

    private String generateRAGResponse(String userId, String userMessage) {
        String context = retrieveKnowledgeContext(userMessage);

        List<ChatMessageDTO> history = conversationHistory.getOrDefault(userId, List.of());
        String conversationContext = history.stream()
                .skip(Math.max(0, history.size() - 6))
                .map(m -> m.getRole() + ": " + m.getContent())
                .collect(Collectors.joining("\n"));

        String response = invokeBedrockLLM(userMessage, context, conversationContext);

        if (response == null || response.isBlank()) {
            response = generateFallbackResponse(userMessage);
        }

        return response;
    }

    private String retrieveKnowledgeContext(String query) {
        if (kendraIndexId == null || kendraIndexId.isEmpty()) {
            log.debug("Kendra index not configured, using built-in knowledge base");
            return getBuiltInKnowledge(query);
        }

        try {
            QueryRequest request = QueryRequest.builder()
                    .indexId(kendraIndexId)
                    .queryText(query)
                    .build();

            QueryResponse response = kendraClient.query(request);

            return response.resultItems().stream()
                    .limit(3)
                    .map(item -> {
                        String title = item.documentTitle() != null ? item.documentTitle().text() : "";
                        String excerpt = item.documentExcerpt() != null ? item.documentExcerpt().text() : "";
                        return title + ": " + excerpt;
                    })
                    .collect(Collectors.joining("\n\n"));
        } catch (Exception e) {
            log.warn("Kendra retrieval failed: {}, using built-in knowledge", e.getMessage());
            return getBuiltInKnowledge(query);
        }
    }

    private String invokeBedrockLLM(String userMessage, String ragContext, String conversationContext) {
        try {
            String systemPrompt = """
                You are a helpful pizza delivery assistant for Pizza Palace.
                Answer questions about menu items, orders, delivery, payments, and promotions.
                Be concise, friendly, and helpful. Use the provided context to answer accurately.
                If you don't know something, suggest the user check the app or contact support.
                """;

            String prompt = String.format("""
                <context>
                %s
                </context>
                
                <conversation_history>
                %s
                </conversation_history>
                
                <user_message>
                %s
                </user_message>
                
                Provide a helpful, concise response:""",
                    ragContext, conversationContext, userMessage);

            String requestBody = String.format("""
                {
                    "anthropic_version": "bedrock-2023-05-31",
                    "max_tokens": 500,
                    "system": "%s",
                    "messages": [{"role": "user", "content": "%s"}]
                }""",
                    escapeJson(systemPrompt), escapeJson(prompt));

            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId(modelId)
                    .contentType("application/json")
                    .accept("application/json")
                    .body(SdkBytes.fromString(requestBody, StandardCharsets.UTF_8))
                    .build();

            InvokeModelResponse response = bedrockClient.invokeModel(request);
            String responseBody = response.body().asUtf8String();

            int contentStart = responseBody.indexOf("\"text\":\"") + 8;
            int contentEnd = responseBody.indexOf("\"", contentStart);
            if (contentStart > 8 && contentEnd > contentStart) {
                return unescapeJson(responseBody.substring(contentStart, contentEnd));
            }

            return extractTextFromResponse(responseBody);
        } catch (Exception e) {
            log.warn("Bedrock invocation failed: {}, using fallback", e.getMessage());
            return null;
        }
    }

    private String extractTextFromResponse(String responseBody) {
        try {
            int idx = responseBody.indexOf("\"text\"");
            if (idx >= 0) {
                int start = responseBody.indexOf("\"", idx + 7) + 1;
                int end = responseBody.indexOf("\"", start);
                while (end > 0 && responseBody.charAt(end - 1) == '\\') {
                    end = responseBody.indexOf("\"", end + 1);
                }
                if (start > 0 && end > start) {
                    return unescapeJson(responseBody.substring(start, end));
                }
            }
        } catch (Exception e) {
            log.debug("Failed to parse Bedrock response: {}", e.getMessage());
        }
        return null;
    }

    private String getBuiltInKnowledge(String query) {
        String lower = query.toLowerCase();
        StringBuilder context = new StringBuilder();

        context.append("Pizza Palace offers delivery from multiple store locations. ");
        context.append("Average delivery time is 25-45 minutes. ");
        context.append("Minimum order amount varies by store ($10-$15). ");

        if (lower.contains("menu") || lower.contains("pizza") || lower.contains("food")) {
            context.append("Menu includes: Margherita ($12.99), Pepperoni ($14.99), BBQ Chicken ($15.99), ");
            context.append("Hawaiian ($13.99), Veggie Supreme ($13.99), Meat Lovers ($16.99). ");
            context.append("Sides: Garlic Bread ($5.99), Wings ($9.99), Caesar Salad ($7.99). ");
            context.append("Drinks: Soda ($2.99), Iced Tea ($3.49), Milkshake ($5.99). ");
        }
        if (lower.contains("coupon") || lower.contains("discount") || lower.contains("offer") || lower.contains("promo")) {
            context.append("Active promotions: WELCOME20 (20% off first order), ");
            context.append("FLAT5 ($5 off orders above $25), FREEDELIVERY (free delivery on orders above $20). ");
        }
        if (lower.contains("pay") || lower.contains("card") || lower.contains("payment")) {
            context.append("Payment methods: All major credit/debit cards via Stripe. PCI-DSS compliant. ");
            context.append("Refunds processed within 5-7 business days. ");
        }
        if (lower.contains("track") || lower.contains("order") || lower.contains("status")) {
            context.append("Order statuses: Placed > Confirmed > Preparing > Ready > Picked Up > Out for Delivery > Delivered. ");
            context.append("Real-time GPS tracking available once driver picks up order. ");
        }
        if (lower.contains("deliver") || lower.contains("time") || lower.contains("how long")) {
            context.append("Delivery radius varies by store. Estimated delivery time shown at checkout. ");
            context.append("Free delivery on orders over $20 with promo code. ");
        }
        if (lower.contains("account") || lower.contains("profile") || lower.contains("address")) {
            context.append("Users can save multiple delivery addresses. Profile includes order history. ");
        }

        return context.toString();
    }

    private String generateFallbackResponse(String message) {
        String lower = message.toLowerCase();

        if (lower.contains("menu") || lower.contains("pizza")) {
            return "We have a wide variety of pizzas! Our popular options include Margherita ($12.99), Pepperoni ($14.99), BBQ Chicken ($15.99), and Hawaiian ($13.99). Check our Menu page for the full selection. Would you like me to recommend something?";
        } else if (lower.contains("order") || lower.contains("track")) {
            return "You can track your order in real-time from the Orders page. Each order shows its current status and you'll get notifications at each step. Is there anything specific about your order I can help with?";
        } else if (lower.contains("deliver")) {
            return "We offer delivery to addresses within our delivery radius. Typical delivery time is 25-45 minutes. You can track your delivery driver in real-time once your order is picked up!";
        } else if (lower.contains("coupon") || lower.contains("discount") || lower.contains("offer")) {
            return "We have several active promotions! Try these coupon codes:\n- WELCOME20: 20% off your first order\n- FLAT5: $5 off orders above $25\n- FREEDELIVERY: Free delivery on orders above $20";
        } else if (lower.contains("pay") || lower.contains("card")) {
            return "We accept all major credit/debit cards through our secure Stripe payment gateway. Your payment information is encrypted and PCI-DSS compliant. Refunds are processed within 5-7 business days.";
        } else if (lower.contains("help") || lower.contains("support")) {
            return "I'm here to help! I can assist with:\n- Menu recommendations\n- Order tracking\n- Delivery information\n- Coupon codes & offers\n- Payment questions\n- Account management\nWhat would you like help with?";
        } else {
            return "Thanks for reaching out! I'm your Pizza Palace assistant. I can help you with menu recommendations, order tracking, delivery info, promotions, and more. What would you like to know?";
        }
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    private String unescapeJson(String text) {
        return text.replace("\\n", "\n").replace("\\r", "\r")
                .replace("\\t", "\t").replace("\\\"", "\"").replace("\\\\", "\\");
    }
}
