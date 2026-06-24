package com.diplom.rest.controller;

import com.diplom.constant.AppConstants;
import com.diplom.mapper.UserMapper;
import com.diplom.rest.dto.UserUpdateDto;
import com.diplom.persistance.entity.UserEntity;
import com.diplom.domain.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.demographic-service-url:http://localhost:8084}")
    private String demographicServiceUrl;

    @GetMapping
    public String viewProfile(@AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        UserEntity user = userService.findByLogin(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException(AppConstants.USER_NOT_FOUND));
        model.addAttribute("user", user);
        return "profile/view";
    }

    @GetMapping("/edit")
    public String editPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        UserEntity user = userService.findByLogin(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException(AppConstants.USER_NOT_FOUND));

        UserUpdateDto dto = userMapper.toUpdateDto(user);

        // Pre-populate demographic fields from demographic-service
        try {
            var resp = restTemplate.exchange(
                    demographicServiceUrl + "/api/demographics/" + user.getId(),
                    HttpMethod.GET, null,
                    new ParameterizedTypeReference<Map<String, Object>>() {});
            if (resp.getBody() != null) {
                Map<String, Object> d = resp.getBody();
                if (d.get("incomeLevel") != null) dto.setIncomeLevel(d.get("incomeLevel").toString());
                if (d.get("educationLevel") != null) dto.setEducationLevel(d.get("educationLevel").toString());
                if (d.get("occupation") != null) dto.setOccupation(d.get("occupation").toString());
                if (d.get("interests") instanceof java.util.List<?> list) {
                    dto.setInterestsRaw(String.join(", ", list.stream().map(Object::toString).toList()));
                }
            }
        } catch (Exception e) {
            log.debug("Could not load demographics for userId={}: {}", user.getId(), e.getMessage());
        }

        model.addAttribute("dto", dto);
        model.addAttribute("user", user);
        return "profile/edit";
    }

    @PostMapping("/edit")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @Valid @ModelAttribute("dto") UserUpdateDto dto,
                                BindingResult bindingResult,
                                Model model) {
        if (bindingResult.hasErrors()) {
            UserEntity user = userService.findByLogin(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalStateException(AppConstants.USER_NOT_FOUND));
            model.addAttribute("user", user);
            return "profile/edit";
        }
        try {
            UserEntity user = userService.findByLogin(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalStateException(AppConstants.USER_NOT_FOUND));
            userService.update(user.getId(), dto);
            return "redirect:/profile?updated=true";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "profile/edit";
        }
    }
}
