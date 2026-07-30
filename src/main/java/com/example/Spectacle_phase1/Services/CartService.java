package com.example.Spectacle_phase1.Services;

import com.example.Spectacle_phase1.Model.Cart;
import com.example.Spectacle_phase1.Model.Product;
import com.example.Spectacle_phase1.Model.User;
import com.example.Spectacle_phase1.Repository.CartRepository;
import com.example.Spectacle_phase1.Repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    public List<Cart> getCartItems(User user) {
        if (user == null) {
            return Collections.emptyList();
        }
        return cartRepository.findByUser(user);
    }

    public double getCartTotal(List<Cart> cartItems) {
        return cartItems.stream()
                .mapToDouble(item -> {
                    int quantity = item.getQuantity() != null ? item.getQuantity() : 0;
                    return quantity * item.getProduct().getEffectivePrice();
                })
                .sum();
    }

    @Transactional
    public int addProductToCart(User user, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        Cart cartItem = cartRepository.findByUserAndProduct(user, product).orElse(new Cart());

        if (cartItem.getId() == null) { // New item
            cartItem.setUser(user);
            cartItem.setProduct(product);
            cartItem.setQuantity(1);
        } else { // Existing item
            cartItem.setQuantity(cartItem.getQuantity() + 1);
        }
        cartRepository.save(cartItem);

        return cartRepository.findByUser(user).size();
    }

    @Transactional
    public void removeCartItem(Long cartId) {
        cartRepository.deleteById(cartId);
    }

    @Transactional
    public Map<String, Object> updateCartItemQuantity(User user, Long cartId, Integer quantity) {
        Cart cartItem = cartRepository.findById(cartId)
                .filter(item -> item.getUser().equals(user)) // Security check
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

        cartItem.setQuantity(quantity);
        cartRepository.save(cartItem);

        List<Cart> userCartItems = getCartItems(user);
        double total = getCartTotal(userCartItems);
        double itemTotal = quantity * cartItem.getProduct().getEffectivePrice();

        Map<String, Object> response = new HashMap<>();
        response.put("total", total);
        response.put("itemTotal", itemTotal);
        return response;
    }
}