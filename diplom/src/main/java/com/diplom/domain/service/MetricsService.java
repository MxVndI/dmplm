package com.diplom.domain.service;

import com.diplom.persistance.entity.OrderEntity;
import com.diplom.persistance.entity.UserEventEntity;
import com.diplom.persistance.repository.OrderRepository;
import com.diplom.persistance.repository.UserEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsService {

    private final UserEventRepository userEventRepository;
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void recordEvent(UserEventEntity event) {
        if (event.getTimestamp() == null) {
            event.setTimestamp(LocalDateTime.now());
        }
        userEventRepository.save(event);
        log.debug("[METRICS] event={} user={} page={} test={} variant={}",
                event.getEventType(), event.getUserId(),
                event.getPage(), event.getTestId(), event.getVariant());
        publishToKafka(event);
    }

    private void publishToKafka(UserEventEntity event) {
        if (event.getUserId() == null || event.getEventType() == null) return;
        try {
            long epochMillis = event.getTimestamp() != null
                    ? event.getTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli()
                    : System.currentTimeMillis();

            Double amount = null;
            if (event.getEventData() != null) {
                Object raw = event.getEventData().get("amount");
                if (raw instanceof Number n) amount = n.doubleValue();
            }

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("userId",    event.getUserId());
            payload.put("eventType", event.getEventType());
            payload.put("timestamp", epochMillis);
            payload.put("page",      event.getPage());
            payload.put("amount",    amount);
            payload.put("sessionId", event.getSessionId());

            kafkaTemplate.send("user-events", event.getUserId(), payload);
        } catch (Exception e) {
            log.warn("Failed to publish user-event to Kafka for userId={}: {}", event.getUserId(), e.getMessage());
        }
    }

    public List<UserEventEntity> getEventsByUser(String userId) {
        return userEventRepository.findByUserId(userId);
    }

    public List<UserEventEntity> getEventsByTest(String testId) {
        return userEventRepository.findByTestId(testId);
    }

    public Map<String, Object> getTestSummary(String testId) {
        List<UserEventEntity> events = userEventRepository.findByTestId(testId);

        Map<String, Map<String, Object>> byVariant = new LinkedHashMap<>();

        for (UserEventEntity e : events) {
            String v = e.getVariant() != null ? e.getVariant() : "unknown";
            byVariant.computeIfAbsent(v, k -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("variant", k);
                m.put("totalEvents", 0);
                m.put("uniqueUsers", new HashSet<String>());
                m.put("pageViews", 0);
                m.put("clicks", 0);
                m.put("buttonClicks", new LinkedHashMap<String, Integer>());
                m.put("scrollAvg", new ArrayList<Double>());
                m.put("sessionDurations", new ArrayList<Long>());
                m.put("pageCounts", new LinkedHashMap<String, Integer>());
                return m;
            });

            Map<String, Object> vm = byVariant.get(v);
            vm.put("totalEvents", (int) vm.get("totalEvents") + 1);
            ((Set<String>) vm.get("uniqueUsers")).add(e.getUserId());

            switch (e.getEventType() != null ? e.getEventType() : "") {
                case "PAGE_VIEW" -> vm.put("pageViews", (int) vm.get("pageViews") + 1);
                case "CLICK"     -> vm.put("clicks",    (int) vm.get("clicks")    + 1);
                case "SCROLL_DEPTH" -> {
                    Object d = e.getEventData() != null ? e.getEventData().get("depth") : null;
                    if (d instanceof Number n) ((List<Double>) vm.get("scrollAvg")).add(n.doubleValue());
                }
                case "TIME_ON_PAGE" -> {
                    Object dur = e.getEventData() != null ? e.getEventData().get("durationMs") : null;
                    if (dur instanceof Number n) ((List<Long>) vm.get("sessionDurations")).add(n.longValue());
                }
                case "BUTTON_CLICK" -> {
                    Object color = e.getEventData() != null ? e.getEventData().get("buttonColor") : null;
                    if (color instanceof String s) {
                        @SuppressWarnings("unchecked")
                        Map<String, Integer> bc = (Map<String, Integer>) vm.get("buttonClicks");
                        bc.merge(s, 1, Integer::sum);
                    }
                }
            }
            if (e.getPage() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Integer> pc = (Map<String, Integer>) vm.get("pageCounts");
                pc.merge(e.getPage(), 1, Integer::sum);
            }
        }

        List<Map<String, Object>> variantList = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> entry : byVariant.entrySet()) {
            Map<String, Object> vm = entry.getValue();
            Set<String> users = (Set<String>) vm.get("uniqueUsers");
            vm.put("uniqueUserCount", users.size());
            vm.remove("uniqueUsers");

            List<Double> scrollList = (List<Double>) vm.get("scrollAvg");
            vm.put("avgScrollDepth", scrollList.isEmpty() ? null
                    : scrollList.stream().mapToDouble(Double::doubleValue).average().orElse(0));
            vm.remove("scrollAvg");

            List<Long> durs = (List<Long>) vm.get("sessionDurations");
            vm.put("avgSessionMs", durs.isEmpty() ? null
                    : (long) durs.stream().mapToLong(Long::longValue).average().orElse(0));
            vm.remove("sessionDurations");

            @SuppressWarnings("unchecked")
            Map<String, Integer> pc = (Map<String, Integer>) vm.get("pageCounts");
            List<Map.Entry<String, Integer>> topPages = pc.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(5)
                    .collect(Collectors.toList());
            vm.put("topPages", topPages.stream()
                    .map(e2 -> Map.of("page", e2.getKey(), "count", e2.getValue()))
                    .collect(Collectors.toList()));
            vm.remove("pageCounts");

            variantList.add(vm);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("testId", testId);
        result.put("totalEvents", events.size());

        List<OrderEntity> orders = orderRepository.findByTestId(testId);
        Map<String, List<OrderEntity>> ordersByVariant = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getVariant() != null ? o.getVariant() : "unknown"));
        for (Map<String, Object> vm : variantList) {
            String v = (String) vm.get("variant");
            List<OrderEntity> vo = ordersByVariant.getOrDefault(v, List.of());
            vm.put("orders", vo.size());
            vm.put("orderRevenue", vo.stream().map(OrderEntity::getTotalPrice)
                    .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add));
            int uniqueOrderUsers = (int) vo.stream().map(OrderEntity::getUserId)
                    .filter(Objects::nonNull).distinct().count();
            vm.put("orderUsers", uniqueOrderUsers);
        }

        result.put("variants", variantList);

        Map<String, Map<String, Integer>> funnelByVariant = new LinkedHashMap<>();
        for (UserEventEntity e : events) {
            String v = e.getVariant() != null ? e.getVariant() : "unknown";
            funnelByVariant.computeIfAbsent(v, k -> {
                Map<String, Integer> fm = new LinkedHashMap<>();
                fm.put("PAGE_VIEW", 0);
                fm.put("PRODUCT_VIEW", 0);
                fm.put("ADD_TO_CART", 0);
                fm.put("CHECKOUT_START", 0);
                return fm;
            });
            String et = e.getEventType();
            if (et != null && funnelByVariant.get(v).containsKey(et)) {
                funnelByVariant.get(v).merge(et, 1, Integer::sum);
            }
        }
        List<OrderEntity> allOrders = orderRepository.findByTestId(testId);
        for (OrderEntity o : allOrders) {
            String v = o.getVariant() != null ? o.getVariant() : "unknown";
            funnelByVariant.computeIfAbsent(v, k -> new LinkedHashMap<>()).merge("ORDER", 1, Integer::sum);
        }
        result.put("funnel", funnelByVariant);

        result.put("significance", computeSignificance(variantList));

        return result;
    }

    private Map<String, Object> computeSignificance(List<Map<String, Object>> variants) {
        Map<String, Object> empty = new LinkedHashMap<>();
        empty.put("available", false);
        if (variants.size() < 2) return empty;

        Map<String, Object> vA = null, vB = null;
        for (Map<String, Object> v : variants) {
            String name = (String) v.get("variant");
            if (vA == null) { vA = v; continue; }
            if (vB == null) { vB = v; break; }
        }
        if (vA == null || vB == null) return empty;

        int nA = ((Number) vA.get("uniqueUserCount")).intValue();
        int nB = ((Number) vB.get("uniqueUserCount")).intValue();
        int cA = ((Number) vA.get("orderUsers")).intValue();
        int cB = ((Number) vB.get("orderUsers")).intValue();

        if (nA < 2 || nB < 2) {
            empty.put("available", false);
            empty.put("reason", "Недостаточно данных (менее 2 уникальных пользователей на вариант)");
            return empty;
        }

        double pA = (double) cA / nA;
        double pB = (double) cB / nB;
        double pPool = (double) (cA + cB) / (nA + nB);
        double se = Math.sqrt(pPool * (1 - pPool) * (1.0 / nA + 1.0 / nB));
        double z = (se == 0) ? 0 : (pA - pB) / se;
        double pValue = 2 * normalCdfTail(Math.abs(z));

        Map<String, Object> sig = new LinkedHashMap<>();
        sig.put("available", true);
        sig.put("variantA", vA.get("variant"));
        sig.put("variantB", vB.get("variant"));
        sig.put("conversionA", Math.round(pA * 10000.0) / 100.0);   // percent, 2 dec
        sig.put("conversionB", Math.round(pB * 10000.0) / 100.0);
        sig.put("nA", nA);
        sig.put("nB", nB);
        sig.put("zScore", Math.round(z * 1000.0) / 1000.0);
        sig.put("pValue", Math.round(pValue * 10000.0) / 10000.0);
        sig.put("significant", pValue < 0.05);
        if (pValue < 0.05) {
            sig.put("winner", pA > pB ? vA.get("variant") : vB.get("variant"));
        } else {
            sig.put("winner", null);
        }
        return sig;
    }

    private static double normalCdfTail(double z) {
        double t = 1.0 / (1.0 + 0.2316419 * z);
        double poly = t * (0.319381530
                + t * (-0.356563782
                + t * (1.781477937
                + t * (-1.821255978
                + t * 1.330274429))));
        double phi = Math.exp(-0.5 * z * z) / Math.sqrt(2 * Math.PI);
        return phi * poly;
    }

    public List<UserEventEntity> getRecentEvents(int limit) {
        return userEventRepository.findAll().stream()
                .sorted(Comparator.comparing(UserEventEntity::getTimestamp,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .collect(Collectors.toList());
    }
}
