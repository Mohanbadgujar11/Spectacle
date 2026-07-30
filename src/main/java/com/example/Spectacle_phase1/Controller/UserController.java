package com.example.Spectacle_phase1.Controller;

import com.example.Spectacle_phase1.Model.User;
import com.example.Spectacle_phase1.Repository.AddressRepository;
import com.example.Spectacle_phase1.Repository.CartRepository;
import com.example.Spectacle_phase1.Repository.OrderRepository;
import com.example.Spectacle_phase1.Repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Assuming you have a PasswordEncoder bean configured
    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final CartRepository cartRepository;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder, OrderRepository orderRepository, AddressRepository addressRepository, CartRepository cartRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.orderRepository = orderRepository;
        this.addressRepository = addressRepository;
        this.cartRepository = cartRepository;
    }

    @GetMapping
    public String viewUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "Admin/User/View_user";
    }

    @GetMapping("/add")
    public String addUserForm(Model model) {
        model.addAttribute("user", new User());
        return "Admin/User/Add_User";
    }

    @PostMapping("/add")
    public String addUser(@ModelAttribute User user) {
        // Encode password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER"); // Default role for new users
        userRepository.save(user);
        return "redirect:/admin/users";
    }

    @GetMapping("/update/{id}")
    public String updateUserForm(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        return "Admin/User/Update_User";
    }

    @PostMapping("/update")
    public String updateUser(@ModelAttribute User user, @RequestParam(value = "password", required = false) String newPassword) {
        User existingUser = userRepository.findById(user.getId()).orElseThrow(() -> new RuntimeException("User not found"));
        existingUser.setUsername(user.getUsername());
        existingUser.setEmail(user.getEmail());
        // Only update the role if a new one is provided and not blank.
        if (user.getRole() != null && !user.getRole().isBlank()) {
            existingUser.setRole(user.getRole());
        }
        if (newPassword != null && !newPassword.isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(newPassword));
        }
        userRepository.save(existingUser);
        return "redirect:/admin/users";
    }

    @PostMapping("/delete/{id}") // Changed to POST for better practice
    @Transactional
    public String deleteUser(@PathVariable Long id) {
        userRepository.findById(id).ifPresent(user -> {
            // The deletion order is critical to avoid foreign key constraint violations.
            // 1. Delete Orders associated with the user.
            orderRepository.deleteAll(orderRepository.findByUserOrderByOrderDateDesc(user));
            // 2. Delete Cart items associated with the user.
            cartRepository.deleteAll(cartRepository.findByUser(user));
            // 3. Delete Addresses associated with the user.
            addressRepository.deleteAll(addressRepository.findByUser(user));
            // 4. Now it's safe to delete the user.
            userRepository.delete(user);
        });
        return "redirect:/admin/users";
    }
}