package com.diplom.rest.controller;

import com.diplom.constant.AppConstants;
import com.diplom.mapper.ProductMapper;
import com.diplom.rest.dto.ProductDto;
import com.diplom.domain.service.ProductService;
import com.diplom.persistance.entity.ProductEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", productService.findAll());
        model.addAttribute("dto", new ProductDto());
        return "admin/products";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("dto") ProductDto dto,
                         BindingResult bindingResult,
                         @RequestParam(value = "photo", required = false) MultipartFile photo,
                         Model model) throws Exception {
        if (bindingResult.hasErrors()) {
            model.addAttribute("products", productService.findAll());
            return "admin/products";
        }
        productService.create(dto, photo);
        return "redirect:/admin/products?created=true";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable String id) {
        productService.delete(id);
        return "redirect:/admin/products?deleted=true";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable String id, Model model) {
        ProductEntity product = productService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(AppConstants.PRODUCT_NOT_FOUND + id));
        model.addAttribute("product", product);
        model.addAttribute("dto", productMapper.toDto(product));
        return "admin/product-edit";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable String id,
                         @Valid @ModelAttribute("dto") ProductDto dto,
                         BindingResult bindingResult,
                         @RequestParam(value = "photo", required = false) MultipartFile photo,
                         Model model) throws Exception {
        if (bindingResult.hasErrors()) {
            ProductEntity product = productService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException(AppConstants.PRODUCT_NOT_FOUND + id));
            model.addAttribute("product", product);
            return "admin/product-edit";
        }
        productService.update(id, dto, photo);
        return "redirect:/admin/products?updated=true";
    }
}
