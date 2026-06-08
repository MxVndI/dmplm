package com.diplom.domain.service;

import com.diplom.persistance.entity.ABTestEntity;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ABTestMetricsExporter {

    private final MeterRegistry registry;
    private final ABTestService abTestService;
    private final MetricsService metricsService;

    private final ConcurrentHashMap<String, double[]> holders = new ConcurrentHashMap<>();

    @Scheduled(fixedDelay = 30_000, initialDelay = 15_000)
    public void export() {
        try {
            List<ABTestEntity> tests = abTestService.findAllTests();
            for (ABTestEntity test : tests) {
                Map<String, Object> summary = metricsService.getTestSummary(test.getId());
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> variants =
                        (List<Map<String, Object>>) summary.get("variants");
                if (variants == null) continue;

                for (Map<String, Object> v : variants) {
                    String variant = String.valueOf(v.get("variant"));
                    Tags tags = Tags.of(
                            "test_id",   test.getId(),
                            "test_name", test.getName(),
                            "variant",   variant);

                    int users      = intVal(v, "uniqueUserCount");
                    int orderUsers = intVal(v, "orderUsers");

                    set("ab_test_events_total",    tags, intVal(v, "totalEvents"));
                    set("ab_test_unique_users",    tags, users);
                    set("ab_test_page_views",      tags, intVal(v, "pageViews"));
                    set("ab_test_clicks",          tags, intVal(v, "clicks"));
                    set("ab_test_orders",          tags, intVal(v, "orders"));
                    set("ab_test_conversion_rate", tags,
                        users > 0 ? orderUsers * 100.0 / users : 0.0);

                    Object scroll = v.get("avgScrollDepth");
                    if (scroll instanceof Number n)
                        set("ab_test_avg_scroll_pct", tags, n.doubleValue());

                    Object sessionMs = v.get("avgSessionMs");
                    if (sessionMs instanceof Number n)
                        set("ab_test_avg_session_ms", tags, n.doubleValue());

                    Object revenue = v.get("orderRevenue");
                    if (revenue instanceof Number n)
                        set("ab_test_revenue", tags, n.doubleValue());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to export A/B metrics: {}", e.getMessage());
        }
    }

    private void set(String name, Tags tags, double value) {
        StringBuilder keyBuilder = new StringBuilder(name).append('{');
        tags.stream().sorted(java.util.Comparator.comparing(io.micrometer.core.instrument.Tag::getKey))
                .forEach(t -> keyBuilder.append(t.getKey()).append('=').append(t.getValue()).append(','));
        String key = keyBuilder.append('}').toString();

        double[] holder = holders.computeIfAbsent(key, k -> {
            double[] h = {0.0};
            Gauge.builder(name, h, hh -> hh[0])
                 .tags(tags)
                 .description("A/B test metric — " + name)
                 .register(registry);
            return h;
        });
        holder[0] = value;
    }

    private int intVal(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v instanceof Number n ? n.intValue() : 0;
    }
}
