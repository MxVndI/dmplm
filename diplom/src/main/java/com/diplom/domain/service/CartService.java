package com.diplom.domain.service;

import com.diplom.constant.AppConstants;
import com.diplom.persistance.entity.OrderEntity;
import com.diplom.persistance.entity.ProductEntity;
import com.diplom.persistance.entity.UserTestParticipationEntity;
import com.diplom.persistance.repository.OrderRepository;
import com.diplom.persistance.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ABTestService abTestService;

    public record CartItem(String productId, String productName, BigDecimal price, int quantity) {
        public BigDecimal subtotal() {
            return price.multiply(BigDecimal.valueOf(quantity));
        }
    }

    @SuppressWarnings("unchecked")
    public List<CartItem> getCart(jakarta.servlet.http.HttpSession session) {
        Object raw = session.getAttribute(AppConstants.SESSION_CART_KEY);
        if (raw instanceof List<?>) return (List<CartItem>) raw;
        List<CartItem> cart = new ArrayList<>();
        session.setAttribute(AppConstants.SESSION_CART_KEY, cart);
        return cart;
    }

    public void addItem(jakarta.servlet.http.HttpSession session, String productId, int quantity) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException(AppConstants.PRODUCT_NOT_FOUND + productId));
        if (product.getAvailableQuantity() == null || product.getAvailableQuantity() < quantity) {
            throw new IllegalArgumentException(AppConstants.NOT_ENOUGH_STOCK + product.getName());
        }

        List<CartItem> cart = getCart(session);
        Optional<CartItem> existing = cart.stream()
                .filter(i -> i.productId().equals(productId)).findFirst();

        if (existing.isPresent()) {
            int newQty = existing.get().quantity() + quantity;
            cart.replaceAll(i -> i.productId().equals(productId)
                    ? new CartItem(i.productId(), i.productName(), i.price(), newQty) : i);
        } else {
            cart.add(new CartItem(productId, product.getName(), product.getPrice(), quantity));
        }
        session.setAttribute(AppConstants.SESSION_CART_KEY, new ArrayList<>(cart));
    }

    public void removeItem(jakarta.servlet.http.HttpSession session, String productId) {
        List<CartItem> cart = getCart(session);
        cart.removeIf(i -> i.productId().equals(productId));
        session.setAttribute(AppConstants.SESSION_CART_KEY, cart);
    }

    public void clearCart(jakarta.servlet.http.HttpSession session) {
        session.setAttribute(AppConstants.SESSION_CART_KEY, new ArrayList<>());
    }

    public BigDecimal cartTotal(List<CartItem> cart) {
        return cart.stream().map(CartItem::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public OrderEntity checkout(jakarta.servlet.http.HttpSession session, String userId) {
        List<CartItem> cart = getCart(session);
        if (cart.isEmpty()) throw new IllegalArgumentException(AppConstants.CART_IS_EMPTY);

        for (CartItem item : cart) {
            productRepository.findById(item.productId()).ifPresent(p -> {
                int newQty = Math.max(0, (p.getAvailableQuantity() == null ? 0 : p.getAvailableQuantity()) - item.quantity());
                p.setAvailableQuantity(newQty);
                productRepository.save(p);
            });
        }

        List<OrderEntity.OrderItem> orderItems = cart.stream().map(ci -> {
            OrderEntity.OrderItem oi = new OrderEntity.OrderItem();
            oi.setProductId(ci.productId());
            oi.setProductName(ci.productName());
            oi.setPrice(ci.price());
            oi.setQuantity(ci.quantity());
            return oi;
        }).toList();

        Optional<UserTestParticipationEntity> participation = abTestService.findActiveParticipation(userId);

        OrderEntity order = new OrderEntity();
        order.setUserId(userId);
        order.setItems(orderItems);
        order.setTotalPrice(cartTotal(cart));
        order.setStatus(AppConstants.ORDER_STATUS_COMPLETED);
        order.setCreatedAt(LocalDateTime.now());
        participation.ifPresent(p -> {
            order.setTestId(p.getTestId());
            order.setVariant(p.getVariant());
        });

        OrderEntity saved = orderRepository.save(order);
        clearCart(session);
        log.info("Заказ {} оформлен пользователем {} — сумма {}{}, вариант={}",
                saved.getId(), userId, saved.getTotalPrice(), AppConstants.CURRENCY_RUB, saved.getVariant());
        return saved;
    }

    public List<OrderEntity> getOrdersByUser(String userId) {
        return orderRepository.findByUserId(userId);
    }
}
