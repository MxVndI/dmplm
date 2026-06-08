package com.diplom.config;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

public interface ABTestResolver {
    Optional<ABResolution> resolve(HttpServletRequest request, String userId);
}
