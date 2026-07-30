package com.example.Spectacle_phase1.Controller;

import com.example.Spectacle_phase1.Model.*;
import com.example.Spectacle_phase1.Repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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

    public CheckoutController(CartRepository cartRepository, UserRepository userRepository, OrderRepository orderRepository, AddressRepository addressRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.addressRepository = addressRepository;
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
                             Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login?error";
        }
        
        // 1. Resolve Address
        Address deliveryAddress;
        // Check if an existing address was selected
        if (addressId != null && addressId > 0) {
            deliveryAddress = addressRepository.findById(addressId)
                .filter(addr -> addr.getUser().equals(user)) // Ensure the address belongs to the user
                .orElse(null);
        // If not, check if a new address was submitted with content
        } else if (newAddress != null && newAddress.getName() != null && !newAddress.getName().isBlank()) {
            newAddress.setUser(user);
            deliveryAddress = addressRepository.save(newAddress);
        } else {
            // No address was selected or provided, this is an error.
            return "redirect:/checkout?error=NoAddress";
        }

        if (deliveryAddress == null) return "redirect:/checkout?error=InvalidAddress";

        // 2. Get Cart Items and ensure they have valid products
        // Filter out cart items with null products to prevent errors during order creation.
        List<Cart> cartItems = cartRepository.findByUser(user).stream()
                .filter(cartItem -> cartItem.getProduct() != null)
                .collect(Collectors.toList());
        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        // 3. Create Order
        Order order = new Order();
        order.setUser(user);
        order.setAddress(deliveryAddress);
        order.setPaymentMethod(paymentMethod);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("Placed");

        double total = 0;
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (Cart cart : cartItems) {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(cart.getProduct());
            int quantity = cart.getQuantity() != null ? cart.getQuantity() : 0;
            item.setQuantity(quantity);
            item.setPrice(cart.getProduct().getEffectivePrice());
            orderItems.add(item);
            total += cart.getProduct().getEffectivePrice() * quantity;
        }
        
        order.setTotalAmount(total);
        order.setOrderItems(orderItems);
        
        orderRepository.save(order);

        // 4. Clear Cart
        cartRepository.deleteAll(cartItems);

        return "redirect:/orders";
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
}