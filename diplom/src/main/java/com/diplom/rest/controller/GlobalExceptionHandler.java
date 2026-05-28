package com.diplom.rest.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException e, Model model) {
        log.warn("IllegalArgument: {}", e.getMessage());
        model.addAttribute("error", e.getMessage());
        return "error";
    }

    /**
     * 404 — static resource or route not found.
     * Logged at DEBUG to avoid noise from health-check probes and missing assets.
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNoResource(NoResourceFoundException e, Model model) {
        log.debug("Resource not found: {}", e.getMessage());
        model.addAttribute("error", "Page not found.");
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneral(Exception e, Model model, HttpServletResponse response) {
        log.error("Unhandled exception", e);
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        model.addAttribute("error", "An unexpected error occurred.");
        return "error";
    }
}
