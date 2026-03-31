package com.example.Spectacle_phase1.Controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping
    public String dashboard() {
        return "Admin/dashboard"; // view for administrator main page
    }

    @GetMapping("/whoami")
    @ResponseBody
    public String whoami(Authentication authentication) {
        return "user=" + authentication.getName() + ", authorities=" + authentication.getAuthorities();
    }
}
