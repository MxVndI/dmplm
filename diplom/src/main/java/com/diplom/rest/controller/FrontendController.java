package com.diplom.rest.controller;

import com.diplom.persistance.entity.UserEntity;
import com.diplom.domain.service.ProductService;
import com.diplom.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class FrontendController {

    private final UserService userService;
    private final ProductService productService;

    @GetMapping("/")
    public String home(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        UserEntity user = userService.findByLogin(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        model.addAttribute("user", user);
        model.addAttribute("products", productService.findAll());
        return "default/home";
    }
}
