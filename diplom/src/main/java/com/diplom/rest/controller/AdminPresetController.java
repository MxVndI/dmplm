package com.diplom.rest.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Serves the built-in preset template gallery.
 * GET /admin/api/presets        — list of preset metadata
 * GET /admin/api/presets/{id}/html — raw HTML of a preset
 */
@Slf4j
@RestController
@RequestMapping("/admin/api/presets")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPresetController {

    record PresetInfo(String id, String name, String description,
                      String pagePattern, String pageName, String thumbnail) {}

    private static final List<PresetInfo> PRESETS = List.of(
        new PresetInfo(
            "home-promo",
            "Главная: Акция",
            "Яркий баннер со скидкой, блок преимуществ и кнопка перехода в каталог",
            "/", "Главная страница", "🎉"
        ),
        new PresetInfo(
            "home-minimal",
            "Главная: Минимализм",
            "Чистый двухколоночный лэйаут с призывом к действию без лишних деталей",
            "/", "Главная страница", "✨"
        ),
        new PresetInfo(
            "home-dark",
            "Главная: Тёмная тема",
            "Тёмный стиль с градиентным заголовком и карточками возможностей",
            "/", "Главная страница", "🌙"
        ),
        new PresetInfo(
            "products-sale",
            "Товары: Распродажа",
            "Сетка товаров со значками скидок и таймером акции",
            "/products", "Список товаров", "🔥"
        ),
        new PresetInfo(
            "profile-loyalty",
            "Профиль: Программа лояльности",
            "Виджет с баллами, статусом и прогресс-баром до следующего уровня",
            "/profile", "Профиль пользователя", "👑"
        )
    );

    @GetMapping
    public List<PresetInfo> list() {
        return PRESETS;
    }

    @GetMapping("/{id}/html")
    public ResponseEntity<String> getHtml(@PathVariable String id) {
        boolean valid = PRESETS.stream().anyMatch(p -> p.id().equals(id));
        if (!valid) return ResponseEntity.notFound().build();

        try {
            ClassPathResource resource = new ClassPathResource("presets/" + id + ".html");
            String html = resource.getContentAsString(StandardCharsets.UTF_8);
            return ResponseEntity.ok(html);
        } catch (IOException e) {
            log.error("Failed to read preset {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("<!-- preset not found -->");
        }
    }
}
