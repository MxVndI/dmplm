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

            Optional<ABResolution> resolution = abTestResolver.resolve(request, userId);

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
            log.debug("ABInterceptor non-fatal error: {}", e.getMessage());
        }
        return true;
    }
}
