package com.diplom.rest.controller;

import com.diplom.domain.service.CartService;
import com.diplom.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CartService cartService;
    private final UserService userService;

    @GetMapping
    public String listUserOrders(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }
        userService.findByLogin(userDetails.getUsername())
                .ifPresent(user -> model.addAttribute("orders", cartService.getOrdersByUser(user.getId())));
        return "user/orders";
    }
}
