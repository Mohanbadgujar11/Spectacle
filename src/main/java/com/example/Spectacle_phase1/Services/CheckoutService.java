package com.example.Spectacle_phase1.Services;

import com.example.Spectacle_phase1.Model.*;
import com.example.Spectacle_phase1.Repository.AddressRepository;
import com.example.Spectacle_phase1.Repository.CartRepository;
import com.example.Spectacle_phase1.Repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CheckoutService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;

    public CheckoutService(CartRepository cartRepository, OrderRepository orderRepository, AddressRepository addressRepository) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.addressRepository = addressRepository;
    }

    @Transactional
    public Order placeOrder(User user, Long addressId, Address newAddress, String paymentMethod) {
        // 1. Resolve Address
        Address deliveryAddress = resolveDeliveryAddress(user, addressId, newAddress);

        // 2. Get valid cart items
        List<Cart> cartItems = cartRepository.findByUser(user).stream()
                .filter(cartItem -> cartItem.getProduct() != null)
                .collect(Collectors.toList());

        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Cannot place an order with an empty cart.");
        }

        // 3. Create and save the order
        Order order = new Order();
        order.setUser(user);
        order.setAddress(deliveryAddress);
        order.setPaymentMethod(paymentMethod);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("Placed");

        List<OrderItem> orderItems = new ArrayList<>();
        double total = 0;

        for (Cart cart : cartItems) {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(cart.getProduct());
            int quantity = cart.getQuantity() != null ? cart.getQuantity() : 0;
            item.setQuantity(quantity);
            item.setPrice(cart.getProduct().getEffectivePrice());
            orderItems.add(item);
            total += item.getPrice() * quantity;
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(total);
        Order savedOrder = orderRepository.save(order);

        // 4. Clear the user's cart
        cartRepository.deleteAll(cartItems);

        return savedOrder;
    }

    private Address resolveDeliveryAddress(User user, Long addressId, Address newAddress) {
        if (addressId != null && addressId > 0) {
            return addressRepository.findById(addressId)
                    .filter(addr -> addr.getUser().equals(user))
                    .orElseThrow(() -> new IllegalArgumentException("Invalid address ID: " + addressId));
        } else if (newAddress != null && newAddress.getName() != null && !newAddress.getName().isBlank()) {
            newAddress.setUser(user);
            return addressRepository.save(newAddress);
        } else {
            throw new IllegalArgumentException("A delivery address must be selected or provided.");
        }
    }
}