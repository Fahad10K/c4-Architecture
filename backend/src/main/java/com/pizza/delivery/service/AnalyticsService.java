package com.pizza.delivery.service;

import com.pizza.delivery.enums.OrderStatus;
import com.pizza.delivery.repository.OrderRepository;
import com.pizza.delivery.repository.UserRepository;
import com.pizza.delivery.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final CloudWatchClient cloudWatchClient;
    private final S3Client s3Client;

    @Value("${aws.s3.analytics-bucket:pizza-delivery-analytics}")
    private String analyticsBucket;

    @Value("${aws.cloudwatch.namespace:PizzaDelivery}")
    private String cloudWatchNamespace;

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
        dashboard.put("activeOrders", orderRepository.countByStatusIn(
                List.of(OrderStatus.PLACED, OrderStatus.CONFIRMED, OrderStatus.PREPARING,
                        OrderStatus.READY, OrderStatus.PICKED_UP, OrderStatus.OUT_FOR_DELIVERY)));
        dashboard.put("cancelledOrders", orderRepository.countByStatus(OrderStatus.CANCELLED));
        dashboard.put("deliveredOrders", orderRepository.countByStatus(OrderStatus.DELIVERED));
        dashboard.put("avgOrderValue", orderRepository.findAll().stream()
                .mapToDouble(o -> o.getTotal()).average().orElse(0));
        return dashboard;
    }

    public Map<String, Object> getReports(String period) {
        Map<String, Object> reports = new HashMap<>();
        reports.put("period", period);
        reports.put("generatedAt", LocalDateTime.now().toString());

        LocalDateTime startDate = switch (period) {
            case "daily" -> LocalDateTime.now().minusDays(1);
            case "weekly" -> LocalDateTime.now().minusWeeks(1);
            case "monthly" -> LocalDateTime.now().minusMonths(1);
            case "yearly" -> LocalDateTime.now().minusYears(1);
            default -> LocalDateTime.now().minusWeeks(1);
        };

        long periodOrders = orderRepository.countByCreatedAtBetween(startDate, LocalDateTime.now());
        double periodRevenue = orderRepository.findByCreatedAtBetween(startDate, LocalDateTime.now())
                .stream().mapToDouble(o -> o.getTotal()).sum();

        reports.put("totalOrders", periodOrders);
        reports.put("revenue", periodRevenue);
        reports.put("avgOrderValue", periodOrders > 0 ? periodRevenue / periodOrders : 0);
        reports.put("newUsers", userRepository.count());
        reports.put("topStores", getTopStores(startDate));

        return reports;
    }

    public void publishMetrics() {
        try {
            List<MetricDatum> metrics = new ArrayList<>();

            metrics.add(MetricDatum.builder()
                    .metricName("TotalOrders")
                    .value((double) orderRepository.count())
                    .unit(StandardUnit.COUNT)
                    .timestamp(Instant.now())
                    .build());

            metrics.add(MetricDatum.builder()
                    .metricName("ActiveOrders")
                    .value((double) orderRepository.countByStatusIn(
                            List.of(OrderStatus.PLACED, OrderStatus.CONFIRMED, OrderStatus.PREPARING)))
                    .unit(StandardUnit.COUNT)
                    .timestamp(Instant.now())
                    .build());

            metrics.add(MetricDatum.builder()
                    .metricName("TotalRevenue")
                    .value(orderRepository.findAll().stream().mapToDouble(o -> o.getTotal()).sum())
                    .unit(StandardUnit.NONE)
                    .timestamp(Instant.now())
                    .build());

            PutMetricDataRequest request = PutMetricDataRequest.builder()
                    .namespace(cloudWatchNamespace)
                    .metricData(metrics)
                    .build();

            cloudWatchClient.putMetricData(request);
            log.info("Published {} metrics to CloudWatch namespace {}", metrics.size(), cloudWatchNamespace);
        } catch (Exception e) {
            log.warn("Failed to publish CloudWatch metrics: {}", e.getMessage());
        }
    }

    public String exportToS3(String period) {
        try {
            Map<String, Object> report = getReports(period);
            String json = report.toString();

            String key = String.format("reports/%s/%s-report.json",
                    LocalDate.now(), period);

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(analyticsBucket)
                    .key(key)
                    .contentType("application/json")
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromString(json));
            log.info("Analytics report exported to s3://{}/{}", analyticsBucket, key);
            return "s3://" + analyticsBucket + "/" + key;
        } catch (Exception e) {
            log.warn("Failed to export analytics to S3: {}", e.getMessage());
            return null;
        }
    }

    public void recordEvent(String eventType, Map<String, String> eventData) {
        try {
            MetricDatum metric = MetricDatum.builder()
                    .metricName(eventType)
                    .value(1.0)
                    .unit(StandardUnit.COUNT)
                    .timestamp(Instant.now())
                    .dimensions(eventData.entrySet().stream()
                            .map(e -> Dimension.builder().name(e.getKey()).value(e.getValue()).build())
                            .toList())
                    .build();

            cloudWatchClient.putMetricData(PutMetricDataRequest.builder()
                    .namespace(cloudWatchNamespace + "/Events")
                    .metricData(metric)
                    .build());
        } catch (Exception e) {
            log.debug("Failed to record event {}: {}", eventType, e.getMessage());
        }
    }

    private List<Map<String, Object>> getTopStores(LocalDateTime since) {
        return storeRepository.findByIsActiveTrue().stream()
                .limit(5)
                .map(store -> {
                    Map<String, Object> storeData = new HashMap<>();
                    storeData.put("id", store.getId());
                    storeData.put("name", store.getName());
                    storeData.put("rating", store.getRating());
                    return storeData;
                })
                .toList();
    }
}
