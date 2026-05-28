package com.diplom.selector.config;

import com.diplom.selector.domain.model.SegmentCondition;
import com.diplom.selector.domain.model.SegmentRule;
import com.diplom.selector.stream.SegmentEvaluator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SegmentRulesConfig {

    @Value("${app.segments.cart-abandonment-hours:1}")
    private int cartAbandonmentHours;

    @Value("${app.segments.inactive-days:30}")
    private int inactiveDays;

    @Value("${app.segments.active-buyer-visits:5}")
    private int activeBuyerVisits;

    @Value("${app.segments.high-value-threshold:5000}")
    private double highValueThreshold;

    @Bean
    public SegmentEvaluator segmentEvaluator() {
        List<SegmentRule> rules = List.of(

            new SegmentRule("CART_ABANDONER", 30, "AND", List.of(
                new SegmentCondition("cartAbandoned",       "EQ",  1.0),
                new SegmentCondition("hoursSinceLastCart",  "LTE", cartAbandonmentHours)
            )),

            new SegmentRule("HIGH_VALUE", 20, "AND", List.of(
                new SegmentCondition("totalSpent", "GT", highValueThreshold)
            )),

            new SegmentRule("ACTIVE_BUYER", 15, "AND", List.of(
                new SegmentCondition("visitCount7Days", "GT", activeBuyerVisits),
                new SegmentCondition("purchaseCount",   "GT", 0.0)
            )),

            new SegmentRule("REGULAR_VISITOR", 10, "AND", List.of(
                new SegmentCondition("visitCount7Days", "GT", activeBuyerVisits)
            )),

            new SegmentRule("INACTIVE", 5, "AND", List.of(
                new SegmentCondition("daysSinceLastEvent", "GT", inactiveDays)
            )),

            new SegmentRule("NEW_USER", 1, "AND", List.of(
                new SegmentCondition("purchaseCount",   "EQ",  0.0),
                new SegmentCondition("visitCount7Days", "LTE", 3.0)
            ))
        );

        return new SegmentEvaluator(rules);
    }
}
