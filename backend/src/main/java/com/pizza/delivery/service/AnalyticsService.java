package com.pizza.delivery.service;

import com.pizza.delivery.repository.OrderRepository;
import com.pizza.delivery.repository.UserRepository;
import com.pizza.delivery.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    public Map<String, Object> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalOrders", orderRepository.count());
        dashboard.put("totalUsers", userRepository.count());
        dashboard.put("totalStores", storeRepository.count());
        dashboard.put("todayOrders", orderRepository.countByCreatedAtBetween(
                LocalDateTime.now().withHour(0).withMinute(0),
                LocalDateTime.now()));
        dashboard.put("revenue", orderRepository.findAll().stream()
                .mapToDouble(o -> o.getTotal()).sum());
        return dashboard;
    }

    public Map<String, Object> getReports(String period) {
        Map<String, Object> reports = new HashMap<>();
        reports.put("period", period);
        reports.put("totalOrders", orderRepository.count());
        reports.put("avgOrderValue", orderRepository.findAll().stream()
                .mapToDouble(o -> o.getTotal()).average().orElse(0));
        return reports;
    }
}
