package com.pizza.delivery.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AuthRequest {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Register {
        @NotBlank @Email
        private String email;
        @NotBlank @Size(min = 6, max = 100)
        private String password;
        @NotBlank
        private String name;
        private String phone;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Login {
        @NotBlank @Email
        private String email;
        @NotBlank
        private String password;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RefreshToken {
        @NotBlank
        private String refreshToken;
    }
}
