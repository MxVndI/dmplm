package com.diplom.rest.controller;

import com.diplom.persistance.entity.OrderEntity;
import com.diplom.persistance.entity.UserEntity;
import com.diplom.domain.service.CartService;
import com.diplom.domain.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    @GetMapping
    public String viewCart(HttpSession session, Model model,
                           @AuthenticationPrincipal UserDetails userDetails) {
        List<CartService.CartItem> cart = cartService.getCart(session);
        model.addAttribute("cartItems", cart);
        model.addAttribute("cartTotal", cartService.cartTotal(cart));
        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam String productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            HttpSession session,
                            RedirectAttributes ra) {
        try {
            cartService.addItem(session, productId, quantity);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("cartError", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeFromCart(@RequestParam String productId,
                                 HttpSession session) {
        cartService.removeItem(session, productId);
        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String checkout(HttpSession session,
                           @AuthenticationPrincipal UserDetails userDetails,
                           RedirectAttributes ra) {
        if (userDetails == null) return "redirect:/auth/login";
        UserEntity user = userService.findByLogin(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        try {
            OrderEntity order = cartService.checkout(session, user.getId());
            ra.addFlashAttribute("orderSuccess",
                    "Order #" + order.getId() + " placed — total $" + order.getTotalPrice());
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("cartError", e.getMessage());
        }
        return "redirect:/cart";
    }
}
