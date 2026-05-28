package com.diplom.config;

import com.diplom.persistance.entity.UserEntity;
import com.diplom.persistance.repository.UserRepository;
import com.diplom.utils.AssignmentServiceClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

/**
 * Pre-handle interceptor that resolves the A/B test routing for every
 * authenticated page request.
 *
 * Sets two request attributes that controllers read to choose a template:
 *   request.getAttribute("abTestId") → String, e.g. "profile_test"
 *   request.getAttribute("variant")  → String, e.g. "A" or "B"
 *
 * When no test applies (unauthenticated, no matching rule, service down),
 * the attributes are simply not set and controllers fall back to their
 * default templates.
 *
 * This interceptor never throws — failures are silently swallowed so that
 * a broken A/B service cannot take down the shop.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ABInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;
    private final ABTestResolver abTestResolver;
    private final AssignmentServiceClient assignmentServiceClient;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            return true;
        }

        try {
            Optional<UserEntity> userOpt = userRepository.findByLogin(auth.getName());
            if (userOpt.isEmpty()) return true;

            String userId = userOpt.get().getId();

            // 1. Try rule-based resolution (old system: ABRuleEntity + ABConfigEntity).
            Optional<ABResolution> resolution = abTestResolver.resolve(request, userId);

            // 2. Fallback to selector-service participation (new system: TestConfigEntity).
            //    This is what gives templates configured via the config-tests UI a chance
            //    to apply — without it, TemplateOverrideInterceptor never fires.
            if (resolution.isEmpty()) {
                resolution = assignmentServiceClient.getAssignment(userId)
                        .filter(p -> p.getTestId() != null && p.getVariant() != null)
                        .map(p -> new ABResolution(p.getTestId(), p.getVariant()));
            }

            resolution.ifPresent(r -> {
                request.setAttribute("abTestId", r.abTestId());
                request.setAttribute("variant",  r.variant());
                log.debug("A/B: userId={} path={} → test={} variant={}",
                        userId, request.getRequestURI(),
                        r.abTestId(), r.variant());
            });
        } catch (Exception e) {
            // Never let A/B resolution break a page render.
            log.debug("ABInterceptor non-fatal error: {}", e.getMessage());
        }
        return true;
    }
}
