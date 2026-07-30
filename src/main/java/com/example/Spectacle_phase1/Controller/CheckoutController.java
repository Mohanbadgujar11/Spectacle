package com.example.Spectacle_phase1.Controller;

import com.example.Spectacle_phase1.Model.*;
import com.example.Spectacle_phase1.Repository.*;
import com.example.Spectacle_phase1.Services.CheckoutService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class CheckoutController {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final CheckoutService checkoutService;

    public CheckoutController(CartRepository cartRepository, UserRepository userRepository, OrderRepository orderRepository, AddressRepository addressRepository, CheckoutService checkoutService) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.addressRepository = addressRepository;
        this.checkoutService = checkoutService;
    }

    @GetMapping("/checkout")
    @Transactional(readOnly = true)
    public String checkoutPage(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            // This case should not happen for an authenticated user, but as a safeguard:
            return "redirect:/login?error";
        }
        
        // Filter out cart items with null products to prevent errors.
        // This can happen if a product was deleted but cart items remained.
        List<Cart> cartItems = cartRepository.findByUser(user).stream()
                .filter(cartItem -> cartItem.getProduct() != null)
                .collect(Collectors.toList());

        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }
        
        double total = cartItems.stream()
                .mapToDouble(cartItem -> {
                    int quantity = cartItem.getQuantity() != null ? cartItem.getQuantity() : 0;
                    return quantity * cartItem.getProduct().getEffectivePrice();
                })
                .sum();

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        model.addAttribute("addresses", addressRepository.findByUser(user));
        model.addAttribute("newAddress", new Address());
        return "checkout";
    }

    @PostMapping("/checkout")
    @Transactional
    public String placeOrder(@RequestParam(required = false) Long addressId,
                             @ModelAttribute Address newAddress,
                             @RequestParam String paymentMethod,
                             Authentication auth,
                             RedirectAttributes redirectAttributes) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        try {
            Order order = checkoutService.placeOrder(user, addressId, newAddress, paymentMethod);
            return "redirect:/order-confirmation/" + order.getId();
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/checkout";
        }
    }

    @GetMapping("/orders")
    @Transactional(readOnly = true)
    public String myOrders(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        List<Order> orders = orderRepository.findByUserOrderByOrderDateDesc(user);
        // Eagerly fetch OrderItems to prevent LazyInitializationException in the template.
        // Also, ensure that related entities are not null before accessing them.
        orders.forEach(order -> {
            if (order.getOrderItems() != null) order.getOrderItems().size();
            if (order.getAddress() != null) order.getAddress().getCity(); // Example access
        });
        model.addAttribute("orders", orders);
        return "order";
    }

    @GetMapping("/order-confirmation/{orderId}")
    public String orderConfirmation(@PathVariable Long orderId, Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        Order order = orderRepository.findById(orderId)
                .filter(o -> o.getUser().equals(user)) // Security check
                .orElse(null);

        if (order == null) {
            return "redirect:/orders"; // Or show an error page
        }
        model.addAttribute("order", order);
        return "order-confirmation";
    }
}