package com.pizza.delivery.service;

import com.pizza.delivery.dto.ChatMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final BedrockRuntimeClient bedrockClient;

    @Value("${aws.bedrock.model-id}")
    private String modelId;

    private final Map<String, List<ChatMessageDTO>> conversationHistory = new HashMap<>();

    public ChatMessageDTO processMessage(String userId, String userMessage) {
        conversationHistory.computeIfAbsent(userId, k -> new ArrayList<>());
        conversationHistory.get(userId).add(ChatMessageDTO.builder()
                .id(UUID.randomUUID().toString()).role("user")
                .content(userMessage).timestamp(LocalDateTime.now()).build());

        String response = generateResponse(userMessage);

        ChatMessageDTO botMessage = ChatMessageDTO.builder()
                .id(UUID.randomUUID().toString()).role("assistant")
                .content(response).timestamp(LocalDateTime.now()).build();

        conversationHistory.get(userId).add(botMessage);
        return botMessage;
    }

    public List<ChatMessageDTO> getHistory(String userId) {
        return conversationHistory.getOrDefault(userId, new ArrayList<>());
    }

    private String generateResponse(String message) {
        String lower = message.toLowerCase();

        if (lower.contains("menu") || lower.contains("pizza")) {
            return "We have a wide variety of pizzas! Check our menu for options including Margherita, Pepperoni, BBQ Chicken, Hawaiian, and more. You can browse the full menu from the Menu page. Would you like me to recommend something?";
        } else if (lower.contains("order") || lower.contains("track")) {
            return "You can track your order in real-time from the Orders page. Each order shows its current status: Placed, Confirmed, Preparing, Ready, Picked Up, On the Way, or Delivered. Is there anything specific about your order I can help with?";
        } else if (lower.contains("deliver")) {
            return "We offer delivery to addresses within our delivery radius. Typical delivery time is 25-45 minutes depending on your location. You can track your delivery driver in real-time once your order is picked up!";
        } else if (lower.contains("coupon") || lower.contains("discount") || lower.contains("offer")) {
            return "We have several active promotions! Try these coupon codes:\n- WELCOME20: 20% off your first order\n- FLAT5: $5 off orders above $25\n- FREEDELIVERY: Free delivery on orders above $20";
        } else if (lower.contains("pay") || lower.contains("card")) {
            return "We accept all major credit/debit cards through our secure Stripe payment gateway. Your payment information is encrypted and PCI-DSS compliant.";
        } else if (lower.contains("help") || lower.contains("support")) {
            return "I'm here to help! I can assist with:\n- Menu recommendations\n- Order tracking\n- Delivery information\n- Coupon codes & offers\n- Payment questions\n- Account management\nWhat would you like help with?";
        } else {
            return "Thanks for reaching out! I'm your pizza assistant. I can help you with menu recommendations, order tracking, delivery info, and more. What would you like to know?";
        }
    }
}
