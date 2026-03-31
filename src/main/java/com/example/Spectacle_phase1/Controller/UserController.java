package com.example.Spectacle_phase1.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.Spectacle_phase1.Model.User;
import com.example.Spectacle_phase1.Repository.AddressRepository;
import com.example.Spectacle_phase1.Repository.CartRepository;
import com.example.Spectacle_phase1.Repository.OrderRepository;
import com.example.Spectacle_phase1.Repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Controller
@RequestMapping("/admin/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;

    public UserController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          CartRepository cartRepository,
                          AddressRepository addressRepository,
                          OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.cartRepository = cartRepository;
        this.addressRepository = addressRepository;
        this.orderRepository = orderRepository;
    }

    // Display all users
    @GetMapping
    public String viewUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());  // List of all users
        return "Admin/User/View_user";  
    }

    // Show form to add a new user
    @GetMapping("/add")
    public String addUserForm(Model model) {
        model.addAttribute("user", new User());  // Add an empty user object for the form
        return "Admin/User/Add_User";  
    }

    // Handle form submission for adding a new user
    @PostMapping("/add")
    public String addUser(@ModelAttribute User user) {
        // Always hash the password for a new user
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "redirect:/admin/users";  
    }

    // Show form to update an existing user
    @GetMapping("/update/{id}")
    public String updateUserForm(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id).orElse(new User());
        model.addAttribute("user", user);
        return "Admin/User/Update_User";  // Show the update form
    }

    // Handle form submission for updating an existing user
    @PostMapping("/update")
    public String updateUser(@ModelAttribute User user) {
        // Find the existing user to avoid overwriting the password unintentionally
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + user.getId()));

        // Update non-sensitive fields
        existingUser.setUsername(user.getUsername());
        existingUser.setEmail(user.getEmail());
        existingUser.setRole(user.getRole()); // Assuming you add a role field to your form

        // Only update the password if a new one was provided
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userRepository.save(existingUser);
        return "redirect:/admin/users";  
    }

    // Delete a user
    @GetMapping("/delete/{id}")
    @Transactional
    public String deleteUser(@PathVariable Long id) {
        userRepository.findById(id).ifPresent(user -> {
            // Before deleting the user, we must delete all associated data
            // that has a foreign key relationship to the user.

            // 1. Delete associated Cart items
            cartRepository.deleteAll(cartRepository.findByUser(user));

            // 2. Delete associated Addresses
            addressRepository.deleteAll(addressRepository.findByUser(user));

            // 3. Delete associated Orders
            orderRepository.deleteAll(orderRepository.findByUserOrderByOrderDateDesc(user));

            // 4. Now, it's safe to delete the user
            userRepository.delete(user);
        });
        return "redirect:/admin/users";  
    }
}