package com.example.Spectacle_phase1.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.Spectacle_phase1.Model.Cart;
import com.example.Spectacle_phase1.Repository.CartRepository;
import com.example.Spectacle_phase1.Repository.ProductRepository;
import com.example.Spectacle_phase1.Repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/cart")
public class CartController {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartController(CartRepository cartRepository, UserRepository userRepository,
            ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @GetMapping
    public String listCart(Model model) {
        List<Cart> cartItems = cartRepository.findAll();
        // Filter out cart items that might have a null user or product to prevent template crashes
        List<Cart> validCartItems = cartItems.stream()
                .filter(c -> c.getUser() != null && c.getProduct() != null)
                .collect(Collectors.toList());
        model.addAttribute("cartItems", validCartItems);
        return "Admin/Cart/View_list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("cart", new Cart());
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("products", productRepository.findAll());
        return "Admin/Cart/Add_list";
    }

    @PostMapping("/add")
    public String addCart(@ModelAttribute Cart cart) {
        cartRepository.save(cart);
        return "redirect:/admin/cart";
    }

    @GetMapping("/delete/{id}")
    public String deleteCartItem(@PathVariable Long id) {
        cartRepository.deleteById(id);
        return "redirect:/admin/cart";
    }
}
