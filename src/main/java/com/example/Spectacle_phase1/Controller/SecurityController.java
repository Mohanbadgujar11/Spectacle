package com.example.Spectacle_phase1.Controller;


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
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");

        // Save to MySQL
        userRepository.save(user);

        // Redirect to login page after registration
        return "redirect:/login";
    }


    

}