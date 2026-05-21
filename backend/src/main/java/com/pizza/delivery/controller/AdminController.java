package com.pizza.delivery.controller;

import com.pizza.delivery.dto.UserDTO;
import com.pizza.delivery.enums.UserRole;
import com.pizza.delivery.service.AdminService;
import com.pizza.delivery.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        return ResponseEntity.ok(analyticsService.getDashboard());
    }

    @GetMapping("/reports")
    public ResponseEntity<Map<String, Object>> getReports(@RequestParam(defaultValue = "weekly") String period) {
        return ResponseEntity.ok(analyticsService.getReports(period));
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserDTO>> getUsers(Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllUsers(pageable));
    }

    @GetMapping("/users/role/{role}")
    public ResponseEntity<Page<UserDTO>> getUsersByRole(@PathVariable UserRole role, Pageable pageable) {
        return ResponseEntity.ok(adminService.getUsersByRole(role, pageable));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDTO> getUser(@PathVariable String userId) {
        return ResponseEntity.ok(adminService.getUserById(userId));
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<UserDTO> updateRole(@PathVariable String userId, @RequestBody Map<String, String> request) {
        UserRole role = UserRole.valueOf(request.get("role").toUpperCase());
        return ResponseEntity.ok(adminService.updateUserRole(userId, role));
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<UserDTO> toggleActive(@PathVariable String userId, @RequestBody Map<String, Boolean> request) {
        return ResponseEntity.ok(adminService.toggleUserActive(userId, request.getOrDefault("active", true)));
    }

    @GetMapping("/feature-flags")
    public ResponseEntity<Map<String, Boolean>> getFeatureFlags() {
        return ResponseEntity.ok(adminService.getFeatureFlags());
    }

    @PutMapping("/feature-flags/{flag}")
    public ResponseEntity<Map<String, Boolean>> updateFeatureFlag(
            @PathVariable String flag, @RequestBody Map<String, Boolean> request) {
        return ResponseEntity.ok(adminService.updateFeatureFlag(flag, request.getOrDefault("enabled", false)));
    }

    @PostMapping("/feature-flags")
    public ResponseEntity<Map<String, Boolean>> createFeatureFlag(@RequestBody Map<String, Object> request) {
        String flag = (String) request.get("name");
        boolean enabled = (Boolean) request.getOrDefault("enabled", false);
        return ResponseEntity.ok(adminService.createFeatureFlag(flag, enabled));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        return ResponseEntity.ok(adminService.getSystemHealth());
    }
}
