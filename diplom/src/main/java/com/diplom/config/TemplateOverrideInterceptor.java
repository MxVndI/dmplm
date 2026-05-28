package com.diplom.config;

import com.diplom.domain.service.TestTemplateService;
import com.diplom.persistance.entity.TestTemplateEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

/**
 * Post-handle interceptor that routes A/B test participants to test-specific pages.
 *
 * Routing priority:
 * 1. Custom DB template (admin-configured for this test/variant/path)
 * 2. Hardcoded variant-specific template (templates/{testId}/{variant}/{page}.html)
 * 3. Default template (no test-specific override)
 *
 * ABInterceptor (preHandle) sets abTestId/variant attributes first;
 * this interceptor reads them in postHandle and applies routing.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TemplateOverrideInterceptor implements HandlerInterceptor {

    private final TestTemplateService templateService;
    private final ApplicationContext applicationContext;

    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView mav) {
        if (mav == null) return;
        String viewName = mav.getViewName();
        if (viewName == null || viewName.startsWith("redirect:") || viewName.startsWith("forward:")) return;

        String testId = (String) request.getAttribute("abTestId");
        String variant = (String) request.getAttribute("variant");
        if (testId == null || variant == null) return;

        String path = request.getRequestURI();

        try {
            // 1. Check for custom DB template
            Optional<TestTemplateEntity> tmpl = templateService.findTemplateForPath(testId, variant, path);
            if (tmpl.isPresent()) {
                String html = templateService.getHtmlContent(tmpl.get());
                mav.addObject("templateHtml", html);
                mav.addObject("templateName", tmpl.get().getName());
                mav.setViewName("user/test-template-page");
                log.debug("Custom template override: test={} variant={} path={} → '{}'", testId, variant, path, tmpl.get().getName());
                return;
            }

            // 2. Try hardcoded variant-specific template: templates/{testId}/{variant}/{basePage}.html
            String basePage = extractPageName(viewName);
            String variantViewName = testId + "/" + variant + "/" + basePage;

            if (templateExists(variantViewName)) {
                mav.setViewName(variantViewName);
                log.debug("Variant template routing: test={} variant={} path={} → '{}'", testId, variant, path, variantViewName);
                return;
            }

            // 3. No variant template found; keep default view
            log.debug("No variant template found for test={} variant={} path={}, using default: {}", testId, variant, path, viewName);

        } catch (Exception e) {
            log.warn("TemplateOverrideInterceptor non-fatal error for path {}: {}", path, e.getMessage());
        }
    }

    /**
     * Extracts the base page name from a view name.
     * E.g., "profile/view" → "view", "products/list" → "list", "home" → "home"
     */
    private String extractPageName(String viewName) {
        if (viewName == null || viewName.isEmpty()) return "index";
        int lastSlash = viewName.lastIndexOf('/');
        return lastSlash >= 0 ? viewName.substring(lastSlash + 1) : viewName;
    }

    /**
     * Checks if a Thymeleaf template exists on the classpath.
     * Thymeleaf templates are in resources/templates/
     */
    private boolean templateExists(String templateName) {
        try {
            String path = "classpath:/templates/" + templateName + ".html";
            Resource resource = applicationContext.getResource(path);
            return resource.exists();
        } catch (Exception e) {
            log.debug("Error checking template existence for {}: {}", templateName, e.getMessage());
            return false;
        }
    }
}
