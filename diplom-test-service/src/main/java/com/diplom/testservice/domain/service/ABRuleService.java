package com.diplom.testservice.domain.service;

import com.diplom.testservice.rest.dto.ABResolutionDto;
import com.diplom.testservice.persistance.entity.ABAssignmentEntity;
import com.diplom.testservice.persistance.entity.ABConfigEntity;
import com.diplom.testservice.persistance.entity.ABRuleEntity;
import com.diplom.testservice.persistance.repository.ABAssignmentRepository;
import com.diplom.testservice.persistance.repository.ABConfigRepository;
import com.diplom.testservice.persistance.repository.ABRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class ABRuleService {

    private final ABRuleRepository ruleRepository;
    private final ABConfigRepository configRepository;
    private final ABAssignmentRepository assignmentRepository;

    public Optional<ABResolutionDto> resolve(String userId, String path) {
        List<ABRuleEntity> rules = getActiveRules();

        Optional<ABRuleEntity> best = rules.stream()
                .filter(r -> userMatches(r, userId) && pathMatches(r, path))
                .max(Comparator.comparingInt(r -> score(r, userId)));

        if (best.isEmpty()) {
            log.debug("No A/B rule matched for userId={} path={}", userId, path);
            return Optional.empty();
        }

        ABRuleEntity rule = best.get();
        String abTestId = rule.getAbTestId();

        Optional<ABConfigEntity> configOpt = configRepository.findById(abTestId);
        if (configOpt.isEmpty() || !configOpt.get().isActive()) {
            log.debug("ABConfig '{}' not found or inactive", abTestId);
            return Optional.empty();
        }

        ABConfigEntity config = configOpt.get();
        String variant = getOrAssignVariant(userId, abTestId, config.getVariants());
        log.debug("Resolved: userId={} path={} → test={} variant={}", userId, path, abTestId, variant);
        return Optional.of(new ABResolutionDto(abTestId, variant));
    }

    private boolean userMatches(ABRuleEntity rule, String userId) {
        return rule.getUserId() == null || rule.getUserId().equals(userId);
    }

    private boolean pathMatches(ABRuleEntity rule, String path) {
        String pattern = rule.getPathPattern();
        if (pattern == null || pattern.equals("/**")) return true;
        return globMatch(pattern, path);
    }

    private boolean globMatch(String pattern, String path) {
        if (path == null) return false;
        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 3);
            return path.equals(prefix) || path.startsWith(prefix + "/");
        }
        return pattern.equals(path);
    }

    private int score(ABRuleEntity rule, String userId) {
        int s = rule.getPriority() * 100;
        if (rule.getUserId() != null && rule.getUserId().equals(userId)) s += 20;
        if (rule.getPathPattern() != null && !rule.getPathPattern().equals("/**")) s += 10;
        return s;
    }


    private String getOrAssignVariant(String userId, String abTestId,
                                      Map<String, Integer> variants) {
        return assignmentRepository.findByUserIdAndAbTestId(userId, abTestId)
                .map(ABAssignmentEntity::getVariant)
                .orElseGet(() -> createAssignment(userId, abTestId, variants));
    }

    private String createAssignment(String userId, String abTestId,
                                    Map<String, Integer> variants) {
        String chosen = chooseVariant(variants);
        ABAssignmentEntity assignment = new ABAssignmentEntity();
        assignment.setUserId(userId);
        assignment.setAbTestId(abTestId);
        assignment.setVariant(chosen);
        assignment.setAssignedAt(Instant.now());
        try {
            assignmentRepository.save(assignment);
            log.info("New A/B assignment: userId={} test={} variant={}", userId, abTestId, chosen);
        } catch (Exception e) {
            return assignmentRepository.findByUserIdAndAbTestId(userId, abTestId)
                    .map(ABAssignmentEntity::getVariant)
                    .orElse(chosen);
        }
        return chosen;
    }

    private String chooseVariant(Map<String, Integer> variants) {
        int total = variants.values().stream().mapToInt(Integer::intValue).sum();
        if (total <= 0) return variants.keySet().iterator().next();

        int roll = ThreadLocalRandom.current().nextInt(total);
        int cumulative = 0;
        for (Map.Entry<String, Integer> entry : variants.entrySet()) {
            cumulative += entry.getValue();
            if (roll < cumulative) return entry.getKey();
        }
        return variants.keySet().iterator().next();
    }


    @Cacheable("ab-rules")
    public List<ABRuleEntity> getActiveRules() {
        return ruleRepository.findByActiveTrue();
    }

    @CacheEvict(value = "ab-rules", allEntries = true)
    public ABRuleEntity saveRule(ABRuleEntity rule) {
        return ruleRepository.save(rule);
    }

    @CacheEvict(value = "ab-rules", allEntries = true)
    public void deleteRule(String id) {
        ruleRepository.deleteById(id);
    }

    public List<ABRuleEntity> getAllRules() {
        return ruleRepository.findAll();
    }


    public ABConfigEntity saveConfig(ABConfigEntity config) {
        return configRepository.save(config);
    }

    public void deleteConfig(String id) {
        configRepository.deleteById(id);
    }

    public List<ABConfigEntity> getAllConfigs() {
        return configRepository.findAll();
    }


    public List<ABAssignmentEntity> getAssignmentsByUser(String userId) {
        return assignmentRepository.findByUserId(userId);
    }

    public void deleteAssignment(String userId, String abTestId) {
        assignmentRepository.deleteByUserIdAndAbTestId(userId, abTestId);
    }
}
