package com.pizza.delivery.dto;

import com.pizza.delivery.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserDTO {
    private String id;
    private String email;
    private String name;
    private String phone;
    private UserRole role;
    private String avatar;
    private String preferences;
    private List<AddressDTO> addresses;
    private LocalDateTime createdAt;
}
