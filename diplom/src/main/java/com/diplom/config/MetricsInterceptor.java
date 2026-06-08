package com.diplom.config;

import com.diplom.persistance.entity.UserEntity;
import com.diplom.persistance.entity.UserTestParticipationEntity;
import com.diplom.persistance.repository.UserRepository;
import com.diplom.utils.AssignmentServiceClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MetricsInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;
    private final AssignmentServiceClient assignmentServiceClient;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView mav) {
        if (mav == null || mav.getViewName() == null) return;
        String view = mav.getViewName();
        if (view.startsWith("redirect:") || view.startsWith("forward:")) return;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) return;

        try {
            Optional<UserEntity> userOpt = userRepository.findByLogin(auth.getName());
            if (userOpt.isEmpty()) return;
            UserEntity user = userOpt.get();
            mav.addObject("_trackUserId", user.getId());

            Optional<UserTestParticipationEntity> p = assignmentServiceClient.getAssignment(user.getId());
            p.ifPresent(part -> {
                mav.addObject("_trackTestId", part.getTestId());
                mav.addObject("_trackVariant", part.getVariant());
            });

            try {
                jakarta.servlet.http.HttpSession session = request.getSession(false);
                if (session != null) {
                    Object cart = session.getAttribute("shopCart");
                    if (cart instanceof java.util.List<?> cartList) {
                        int totalQty = 0;
                        for (Object item : cartList) {
                            try {
                                Object q = item.getClass().getMethod("quantity").invoke(item);
                                totalQty += q instanceof Number n ? n.intValue() : 1;
                            } catch (Exception ignored3) {
                                totalQty += 1;
                            }
                        }
                        mav.addObject("_cartCount", totalQty);
                    }
                }
            } catch (Exception ignored2) {}
        } catch (Exception ignored) {
        }
    }
}
