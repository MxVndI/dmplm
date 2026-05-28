package com.diplom.rest.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Serves the admin templates management page.
 */
@Controller
@RequestMapping("/admin/templates")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTemplatesPageController {

    @GetMapping
    public String templatesPage() {
        return "admin/templates";
    }
}
