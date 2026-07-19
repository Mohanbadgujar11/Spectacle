package com.example.Spectacle_phase1.Controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.Spectacle_phase1.Model.User;
import com.example.Spectacle_phase1.Repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;



@Controller
public class SecurityController {

    private static final Logger log = LoggerFactory.getLogger(SecurityController.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SecurityController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // @GetMapping("/")
    // public String index() {
    //     return "index";  // your index.html
    // }

    @GetMapping("/login")
    public String login(Authentication auth) {
        if(auth != null && auth.isAuthenticated()){
            return "redirect:/";    // redirect logged-in users to home page
        }
        return "login";  // your login.html
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user) {
        String username = user.getUsername() == null ? "" : user.getUsername().trim();
        String email = user.getEmail() == null ? "" : user.getEmail().trim();

        if (username.isBlank() || email.isBlank() || user.getPassword() == null || user.getPassword().isBlank()) {
            return "redirect:/register?error=missing";
        }

        if (userRepository.existsByUsernameIgnoreCase(username) || userRepository.existsByEmailIgnoreCase(email)) {
            return "redirect:/register?error=exists";
        }

        log.info("Registering user username={} email={}", username, email);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");

        // Save to MySQL
        User saved = userRepository.save(user);
        log.info("Saved user id={} username={} role={} passwordHashPresent={}", saved.getId(), saved.getUsername(), saved.getRole(), saved.getPassword() != null);
        log.info("Lookup after save exists={}", userRepository.findByUsernameIgnoreCase(saved.getUsername()).isPresent());

        // Redirect to login page after registration
        return "redirect:/login";
    }


    

}