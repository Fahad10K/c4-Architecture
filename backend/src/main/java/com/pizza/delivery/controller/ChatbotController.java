package com.pizza.delivery.controller;

import com.pizza.delivery.dto.ChatMessageDTO;
import com.pizza.delivery.security.UserPrincipal;
import com.pizza.delivery.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/message")
    public ResponseEntity<ChatMessageDTO> sendMessage(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(chatbotService.processMessage(principal.getId(), body.get("message")));
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatMessageDTO>> getHistory(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(chatbotService.getHistory(principal.getId()));
    }
}
