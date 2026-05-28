package com.diplom.rest.controller;

import com.diplom.persistance.entity.ProductEntity;
import com.diplom.domain.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public String list(@RequestParam(required = false) String search, Model model) {
        if (search != null && !search.isBlank()) {
            model.addAttribute("products", productService.search(search));
            model.addAttribute("search", search);
        } else {
            model.addAttribute("products", productService.findAll());
        }
        return "products/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable String id, Model model) {
        ProductEntity product = productService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
        model.addAttribute("product", product);
        return "products/detail";
    }
}
