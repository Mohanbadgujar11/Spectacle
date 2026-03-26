package com.example.Spectacle_phase1.Controller;

import com.example.Spectacle_phase1.Repository.CartRepository;
import com.example.Spectacle_phase1.Repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;

    public GlobalControllerAdvice(UserRepository userRepository, CartRepository cartRepository) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
    }

    @ModelAttribute("cartCount")
    public int populateCartCount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // Check if user is logged in and not anonymous
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return userRepository.findByUsername(auth.getName())
                    .map(user -> cartRepository.findByUser(user).size())
                    .orElse(0);
        }
        return 0;
    }
}