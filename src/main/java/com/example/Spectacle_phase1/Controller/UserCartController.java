package com.example.Spectacle_phase1.Controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.Spectacle_phase1.Model.Cart;
import com.example.Spectacle_phase1.Model.Product;
import com.example.Spectacle_phase1.Model.User;
import com.example.Spectacle_phase1.Repository.CartRepository;
import com.example.Spectacle_phase1.Repository.ProductRepository;
import com.example.Spectacle_phase1.Repository.UserRepository;
import com.example.Spectacle_phase1.Services.CartService;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Controller
@RequestMapping("/cart")
public class UserCartController {

    private final UserRepository userRepository;
    private final CartService cartService;

    public UserCartController(UserRepository userRepository, CartService cartService) {
        this.userRepository = userRepository;
        this.cartService = cartService;
    }

    // show current user's cart items (or provide empty model for guests)
    @GetMapping
    @Transactional(readOnly = true)
    public String viewCart(Model model, Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            User user = userRepository.findByUsername(auth.getName()).orElse(null);
            if (user != null) {
                List<Cart> items = cartService.getCartItems(user);
                model.addAttribute("cartItems", items);
                model.addAttribute("total", cartService.getCartTotal(items));
            }
        }
        // make sure attributes exist even if user is anonymous or nothing found
        if (!model.containsAttribute("cartItems")) {
            model.addAttribute("cartItems", java.util.Collections.emptyList());
            model.addAttribute("total", 0);
        }
        return "cart";
    }

    // add a product to the logged-in user's cart (increase quantity if already present)
    @PostMapping("/add/{productId}")
    @ResponseBody
    public org.springframework.http.ResponseEntity<Integer> addToCart(@PathVariable Long productId,
                                                                    Authentication auth,
                                                                    jakarta.servlet.http.HttpServletRequest request) {
        // If user is not properly authenticated, return 401 Unauthorized
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }
        User user = userRepository.findByUsername(auth.getName()).orElse(null);

        try {
            int newCartSize = cartService.addProductToCart(user, productId);
            return org.springframework.http.ResponseEntity.ok(newCartSize);
        } catch (IllegalArgumentException e) {
            // Product not found
            return org.springframework.http.ResponseEntity.notFound().build();
        }
    }

    // remove an item from cart
    @PostMapping("/remove/{cartId}")
    public String removeFromCart(@PathVariable Long cartId, Authentication auth) {
        // optional: verify that the cart item belongs to current user
        // A more robust implementation would use the service and check ownership
        cartService.removeCartItem(cartId);
        return "redirect:/cart";
    }

    // change quantity
    @PostMapping("/update/{cartId}")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> updateQuantity(@PathVariable Long cartId,
                                 @RequestParam Integer quantity,
                                 Authentication auth,
                                 jakarta.servlet.http.HttpServletRequest request) {

        if (auth == null || !auth.isAuthenticated()) {
            return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        try {
            java.util.Map<String, Object> response = cartService.updateCartItemQuantity(user, cartId, quantity);
            return org.springframework.http.ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return org.springframework.http.ResponseEntity.notFound().build();
        }
    }
}
