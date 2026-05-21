package com.pizza.delivery.service;

import com.pizza.delivery.dto.UserDTO;
import com.pizza.delivery.entity.User;
import com.pizza.delivery.enums.UserRole;
import com.pizza.delivery.exception.BadRequestException;
import com.pizza.delivery.exception.ResourceNotFoundException;
import com.pizza.delivery.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;

    private final Map<String, Boolean> featureFlags = new ConcurrentHashMap<>(Map.of(
            "chatbot_enabled", true,
            "ai_recommendations", true,
            "real_time_tracking", true,
            "push_notifications", true,
            "sms_notifications", false,
            "multi_language", false,
            "dark_mode", true,
            "voice_ordering", false
    ));

    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::mapToDTO);
    }

    public Page<UserDTO> getUsersByRole(UserRole role, Pageable pageable) {
        return userRepository.findByRole(role, pageable).map(this::mapToDTO);
    }

    public UserDTO getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return mapToDTO(user);
    }

    @Transactional
    public UserDTO updateUserRole(String userId, UserRole newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        UserRole oldRole = user.getRole();
        user.setRole(newRole);
        user = userRepository.save(user);

        log.info("User {} role changed: {} -> {}", userId, oldRole, newRole);
        return mapToDTO(user);
    }

    @Transactional
    public UserDTO toggleUserActive(String userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setIsActive(active);
        user = userRepository.save(user);

        log.info("User {} active status: {}", userId, active);
        return mapToDTO(user);
    }

    public Map<String, Boolean> getFeatureFlags() {
        return Collections.unmodifiableMap(featureFlags);
    }

    public Map<String, Boolean> updateFeatureFlag(String flag, boolean enabled) {
        if (!featureFlags.containsKey(flag)) {
            throw new BadRequestException("Unknown feature flag: " + flag);
        }
        featureFlags.put(flag, enabled);
        log.info("Feature flag '{}' set to {}", flag, enabled);
        return getFeatureFlags();
    }

    public Map<String, Boolean> createFeatureFlag(String flag, boolean enabled) {
        featureFlags.put(flag, enabled);
        log.info("Feature flag '{}' created with value {}", flag, enabled);
        return getFeatureFlags();
    }

    public Map<String, Object> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("totalUsers", userRepository.count());
        health.put("activeUsers", userRepository.countByIsActiveTrue());
        health.put("adminCount", userRepository.countByRole(UserRole.ADMIN));
        health.put("staffCount", userRepository.countByRole(UserRole.STAFF));
        health.put("driverCount", userRepository.countByRole(UserRole.DRIVER));
        health.put("customerCount", userRepository.countByRole(UserRole.CUSTOMER));
        health.put("featureFlags", featureFlags);
        health.put("systemStatus", "HEALTHY");
        health.put("uptime", System.currentTimeMillis());
        return health;
    }

    private UserDTO mapToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
