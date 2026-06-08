package com.diplom.rest.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/api/presets")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPresetController {

    record PresetInfo(String id, String name, String description,
                      String pagePattern, String pageName, String thumbnail) {
    }

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
            ),
            new PresetInfo(
                    "button-test-A",
                    "Тест кнопок A: Красная vs Зелёная",
                    "Два цветных CTA. Вариант A: красная кнопка слева, зелёная справа",
                    "/button-test", "Тест кнопок", "🔴"
            ),
            new PresetInfo(
                    "button-test-B",
                    "Тест кнопок B: Зелёная vs Фиолетовая",
                    "Вариант B: зелёная кнопка слева, фиолетовая справа",
                    "/button-test", "Тест кнопок", "🟢"
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
