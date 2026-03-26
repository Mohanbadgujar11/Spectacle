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

import java.util.List;

@Controller
@RequestMapping("/cart")
public class UserCartController {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public UserCartController(CartRepository cartRepository,
                              ProductRepository productRepository,
                              UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    // show current user's cart items (or provide empty model for guests)
    @GetMapping
    public String viewCart(Model model, Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            User user = userRepository.findByUsername(auth.getName()).orElse(null);
            if (user != null) {
                List<Cart> items = cartRepository.findByUser(user);
                model.addAttribute("cartItems", items);
                double total = items.stream()
                        .mapToDouble(i -> i.getQuantity() * (i.getProduct().getDiscountedPrice() != null ? i.getProduct().getDiscountedPrice() : i.getProduct().getSellingPrice() ))
                        .sum();
                model.addAttribute("total", total);
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
    public org.springframework.http.ResponseEntity<Integer> addToCart(@PathVariable Long productId,
                                                                    Authentication auth,
                                                                    jakarta.servlet.http.HttpServletRequest request) {
        // If user is not properly authenticated, return 401 Unauthorized
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.FOUND)
                    .location(java.net.URI.create("/")).build(); // product not found
        }
        
        Cart cartItem = cartRepository.findByUserAndProduct(user, product).orElse(null);
        if (cartItem == null) {
            cartItem = new Cart();
            cartItem.setUser(user);
            cartItem.setProduct(product);
            cartItem.setQuantity(1);
        } else {
            cartItem.setQuantity((cartItem.getQuantity() == null ? 0 : cartItem.getQuantity()) + 1);
        }
        cartRepository.save(cartItem);
        // if AJAX request, respond with new cart size
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            return org.springframework.http.ResponseEntity.ok(cartRepository.findByUser(user).size());
        }
        // otherwise redirect to cart page
        return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.FOUND)
                .location(java.net.URI.create("/cart")).build();
    }

    // remove an item from cart
    @PostMapping("/remove/{cartId}")
    public String removeFromCart(@PathVariable Long cartId, Authentication auth) {
        // optional: verify that the cart item belongs to current user
        cartRepository.deleteById(cartId);
        return "redirect:/cart";
    }

    // change quantity
    @PostMapping("/update/{cartId}")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> updateQuantity(@PathVariable Long cartId,
                                 @RequestParam Integer quantity,
                                 Authentication auth,
                                 jakarta.servlet.http.HttpServletRequest request) {
        
        Cart cartItem = cartRepository.findById(cartId).orElse(null);
        if (cartItem != null) {
            cartItem.setQuantity(quantity);
            cartRepository.save(cartItem);
        }

        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
             User user = userRepository.findByUsername(auth.getName()).orElse(null);
             double total = 0;
             double itemTotal = 0;
             if (user != null) {
                 List<Cart> items = cartRepository.findByUser(user);
                 total = items.stream()
                         .mapToDouble(i -> i.getQuantity() * (i.getProduct().getDiscountedPrice() != null ? i.getProduct().getDiscountedPrice() : i.getProduct().getSellingPrice() ))
                         .sum();
                 
                 if (cartItem != null) {
                     itemTotal = cartItem.getQuantity() * (cartItem.getProduct().getDiscountedPrice() != null ? cartItem.getProduct().getDiscountedPrice() : cartItem.getProduct().getSellingPrice());
                 }
             }
             java.util.Map<String, Object> response = new java.util.HashMap<>();
             response.put("total", total);
             response.put("itemTotal", itemTotal);
             return org.springframework.http.ResponseEntity.ok(response);
        }

        return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.FOUND)
                .location(java.net.URI.create("/cart")).build();
    }
}
