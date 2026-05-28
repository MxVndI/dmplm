package com.diplom.selector.stream;

import com.diplom.selector.domain.model.SegmentCondition;
import com.diplom.selector.domain.model.SegmentRule;
import com.diplom.selector.domain.model.UserAggregateState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class SegmentEvaluator {

    private final List<SegmentRule> rules;

    public String evaluate(UserAggregateState state) {
        long now = System.currentTimeMillis();
        return rules.stream()
                .filter(rule -> matches(rule, state, now))
                .max(Comparator.comparingInt(SegmentRule::getPriority))
                .map(SegmentRule::getName)
                .orElse("UNKNOWN");
    }

    private boolean matches(SegmentRule rule, UserAggregateState state, long now) {
        List<SegmentCondition> conditions = rule.getConditions();
        if (conditions == null || conditions.isEmpty()) return false;
        if ("OR".equalsIgnoreCase(rule.getLogic())) {
            return conditions.stream().anyMatch(c -> evaluate(c, state, now));
        }
        return conditions.stream().allMatch(c -> evaluate(c, state, now));
    }

    private boolean evaluate(SegmentCondition cond, UserAggregateState state, long now) {
        double fieldValue = fieldValue(cond.getField(), state, now);
        double threshold = cond.getValue();
        return switch (cond.getOperator()) {
            case "GT"  -> fieldValue > threshold;
            case "GTE" -> fieldValue >= threshold;
            case "LT"  -> fieldValue < threshold;
            case "LTE" -> fieldValue <= threshold;
            case "EQ"  -> Double.compare(fieldValue, threshold) == 0;
            case "NEQ" -> Double.compare(fieldValue, threshold) != 0;
            default -> {
                log.warn("Unknown operator '{}' in rule '{}'", cond.getOperator(), cond.getField());
                yield false;
            }
        };
    }

    private double fieldValue(String field, UserAggregateState state, long now) {
        return switch (field) {
            case "visitCount7Days"   -> state.getVisitCount7Days();
            case "purchaseCount"     -> state.getPurchaseCount();
            case "totalSpent"        -> state.getTotalSpent();
            case "cartAddCount"      -> state.getCartAddCount();
            case "productViewCount"  -> state.getProductViewCount();
            case "cartAbandoned"     ->
                    (state.getLastCartAddTimestamp() != null && !state.isPurchasedAfterLastCart()) ? 1.0 : 0.0;
            case "daysSinceLastEvent" ->
                    state.getLastEventTimestamp() != null
                            ? (now - state.getLastEventTimestamp()) / 86_400_000.0
                            : Double.MAX_VALUE;
            case "hoursSinceLastCart" ->
                    state.getLastCartAddTimestamp() != null
                            ? (now - state.getLastCartAddTimestamp()) / 3_600_000.0
                            : Double.MAX_VALUE;
            default -> {
                log.warn("Unknown segment field '{}'", field);
                yield 0.0;
            }
        };
    }
}
