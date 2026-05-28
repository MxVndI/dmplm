package com.diplom.config;

/**
 * Holds the result of an A/B test resolution: which test and which variant
 * the current user should see.
 *
 * Produced by {@link ABTestResolver} and consumed by controllers via
 * request attributes "abTestId" and "variant" set by {@link ABInterceptor}.
 */
public record ABResolution(String abTestId, String variant) {}
