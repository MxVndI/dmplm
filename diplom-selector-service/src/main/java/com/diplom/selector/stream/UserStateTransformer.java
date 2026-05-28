package com.diplom.selector.stream;

import com.diplom.selector.domain.model.UserAggregateState;
import com.diplom.selector.domain.model.UserEvent;
import com.diplom.selector.event.UserSegmentChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.ContextualProcessor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.List;
import java.util.Objects;

@Slf4j
public class UserStateTransformer
        extends ContextualProcessor<String, UserEvent, String, UserSegmentChangedEvent> {

    private final SegmentEvaluator evaluator;
    private final String storeName;
    /** Sliding-window size in milliseconds (default: 7 days). */
    private final long windowMillis;

    private KeyValueStore<String, UserAggregateState> stateStore;

    public UserStateTransformer(SegmentEvaluator evaluator, String storeName, long windowMillis) {
        this.evaluator = evaluator;
        this.storeName = storeName;
        this.windowMillis = windowMillis;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void init(ProcessorContext<String, UserSegmentChangedEvent> context) {
        super.init(context);
        this.stateStore = (KeyValueStore<String, UserAggregateState>) context.getStateStore(storeName);
        log.info("UserStateTransformer initialised, store='{}', window={}ms", storeName, windowMillis);
    }

    @Override
    public void process(Record<String, UserEvent> record) {
        String userId = record.key();
        UserEvent event = record.value();

        if (userId == null || event == null || event.getEventType() == null) {
            return;
        }

        UserAggregateState state = stateStore.get(userId);
        if (state == null) {
            state = new UserAggregateState();
            state.setUserId(userId);
        }

        String previousSegment = state.getCurrentSegment();

        applyEvent(state, event);

        String newSegment = evaluator.evaluate(state);

        if (!Objects.equals(newSegment, previousSegment)) {
            state.setCurrentSegment(newSegment);
            state.setSegmentChangedAt(event.getTimestamp());

            stateStore.put(userId, state);

            UserSegmentChangedEvent change = new UserSegmentChangedEvent(
                    userId, previousSegment, newSegment, event.getTimestamp());
            log.info("Segment change: user={} [{}] -> [{}]", userId, previousSegment, newSegment);
            context().forward(new Record<>(userId, change, record.timestamp()));
        } else {
            stateStore.put(userId, state);
        }
    }

    private void applyEvent(UserAggregateState state, UserEvent event) {
        long ts = event.getTimestamp();

        if (state.getFirstEventTimestamp() == null) {
            state.setFirstEventTimestamp(ts);
        }
        state.setLastEventTimestamp(ts);

        switch (event.getEventType()) {
            case "PAGE_VIEW", "LOGIN" -> addVisit(state, ts);
            case "PRODUCT_VIEW" -> {
                state.setProductViewCount(state.getProductViewCount() + 1);
                addVisit(state, ts);
            }
            case "ADD_TO_CART", "CART_ADD" -> {
                state.setCartAddCount(state.getCartAddCount() + 1);
                state.setLastCartAddTimestamp(ts);
                state.setPurchasedAfterLastCart(false);
            }
            case "PURCHASE", "ORDER", "CHECKOUT" -> {
                state.setPurchaseCount(state.getPurchaseCount() + 1);
                if (event.getAmount() != null) {
                    state.setTotalSpent(state.getTotalSpent() + event.getAmount());
                }
                state.setPurchasedAfterLastCart(true);
            }
            default -> {
            }
        }
    }

    /** Adds {@code ts} to the visit-timestamps list and evicts stale entries. */
    private void addVisit(UserAggregateState state, long ts) {
        List<Long> visits = state.getVisitTimestamps();
        visits.add(ts);
        long cutoff = ts - windowMillis;
        visits.removeIf(t -> t < cutoff);
        state.setVisitCount7Days(visits.size());
    }
}
