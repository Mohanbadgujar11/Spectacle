package com.example.Spectacle_phase1.Controller;

import com.example.Spectacle_phase1.Model.User;
import com.example.Spectacle_phase1.Repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

@Controller
@RequestMapping("/admin/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Assuming you have a PasswordEncoder bean configured

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
        existingUser.setRole(user.getRole()); // Assuming role can be updated
        if (newPassword != null && !newPassword.isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(newPassword));
        }
        userRepository.save(existingUser);
        return "redirect:/admin/users";
    }

    @PostMapping("/delete/{id}") // Changed to POST for better practice
    @Transactional
    public String deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return "redirect:/admin/users";
    }
}