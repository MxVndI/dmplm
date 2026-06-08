package com.diplom.rest.controller;

import com.diplom.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/button-test")
@RequiredArgsConstructor
public class ButtonTestController {

    private final UserService userService;

    @GetMapping
    public String showButtonTest(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            userService.findByLogin(userDetails.getUsername())
                    .ifPresent(user -> model.addAttribute("user", user));
        }
        return "button_test/default";
    }
}
