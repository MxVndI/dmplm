package com.diplom.config;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * Strategy interface for resolving which A/B test and variant apply to a request.
 *
 * Implementations:
 *   - {@link RemoteABTestResolver} — delegates to diplom-test-service via HTTP.
 *
 * Returns empty when no test applies (controllers fall back to default templates).
 */
public interface ABTestResolver {
    Optional<ABResolution> resolve(HttpServletRequest request, String userId);
}
