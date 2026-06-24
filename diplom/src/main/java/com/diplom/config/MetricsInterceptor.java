package com.diplom.config;

import com.diplom.constant.AppConstants;
import com.diplom.persistance.entity.UserEntity;
import com.diplom.persistance.repository.UserRepository;
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

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView mav) {
        if (mav == null || mav.getViewName() == null) return;
        String view = mav.getViewName();
        if (view.startsWith(AppConstants.REDIRECT_PREFIX) || view.startsWith(AppConstants.FORWARD_PREFIX)) return;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) return;

        try {
            Optional<UserEntity> userOpt = userRepository.findByLogin(auth.getName());
            if (userOpt.isEmpty()) return;
            UserEntity user = userOpt.get();
            mav.addObject(AppConstants.MODEL_TRACK_USER_ID, user.getId());

            Object abTestId = request.getAttribute(AppConstants.REQUEST_ATTRIBUTE_AB_TEST_ID);
            Object variant = request.getAttribute(AppConstants.REQUEST_ATTRIBUTE_VARIANT);
            if (abTestId instanceof String testId && variant instanceof String variantName) {
                mav.addObject(AppConstants.MODEL_TRACK_TEST_ID, testId);
                mav.addObject(AppConstants.MODEL_TRACK_VARIANT, variantName);
            }

            try {
                jakarta.servlet.http.HttpSession session = request.getSession(false);
                if (session != null) {
                    Object cart = session.getAttribute(AppConstants.SESSION_CART_KEY);
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
                        mav.addObject(AppConstants.MODEL_CART_COUNT, totalQty);
                    }
                }
            } catch (Exception ignored2) {}
        } catch (Exception ignored) {
        }
    }
}
