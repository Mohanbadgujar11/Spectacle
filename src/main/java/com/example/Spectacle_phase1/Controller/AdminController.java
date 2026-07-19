package com.example.Spectacle_phase1.Controller;

import com.example.Spectacle_phase1.Repository.ContactMessageRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ContactMessageRepository contactMessageRepository;

    public AdminController(ContactMessageRepository contactMessageRepository) {
        this.contactMessageRepository = contactMessageRepository;
    }

    @GetMapping
    public String dashboard(Model model) {
        // Add recent messages to the dashboard model
        model.addAttribute("recentMessages", contactMessageRepository.findTop5ByOrderByIdDesc());

        return "Admin/dashboard"; // view for administrator main page
    }

    @GetMapping("/whoami")
    @ResponseBody
    public String whoami(Authentication authentication) {
        return "user=" + authentication.getName() + ", authorities=" + authentication.getAuthorities();
    }
}
