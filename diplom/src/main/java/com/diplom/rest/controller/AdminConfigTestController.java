package com.diplom.rest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/config-tests")
public class AdminConfigTestController {

    @GetMapping
    public String index() {
        return "admin/config-tests";
    }
}
