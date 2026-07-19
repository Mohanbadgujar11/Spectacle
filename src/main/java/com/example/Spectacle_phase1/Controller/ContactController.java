package com.example.Spectacle_phase1.Controller;

import java.util.List;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.Spectacle_phase1.Model.ContactMessage;
import com.example.Spectacle_phase1.Repository.ContactMessageRepository;

@Controller
public class ContactController {

    private final ContactMessageRepository contactMessageRepository;

    public ContactController(ContactMessageRepository contactMessageRepository) {
        this.contactMessageRepository = contactMessageRepository;
    }

    @GetMapping("/contact")
    public String showContactForm() {
        return "contact";
    }

    @PostMapping("/contact")
    public String handleContactForm(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("contact-number") String contactNumber,
            @RequestParam("message") String message,
            Authentication authentication) {
        if (authentication == null
                || authentication instanceof AnonymousAuthenticationToken
                || "anonymousUser".equals(authentication.getName())) {
            return "redirect:/login";
        }

        ContactMessage contactMessage = new ContactMessage();
        contactMessage.setName(name);
        contactMessage.setEmail(email);
        contactMessage.setContactNumber(contactNumber);
        contactMessage.setMessage(message);
        contactMessageRepository.save(contactMessage);
        return "redirect:/contact?sent=true";
    }

    @GetMapping("/admin/messages")
    public String showAdminMessages(Model model) {
        List<ContactMessage> messages = contactMessageRepository.findAll();
        model.addAttribute("messages", messages);
        return "admin-messages";
    }

    @PostMapping("/admin/messages/delete/{id}")
    public String deleteMessage(@PathVariable Long id) {
        contactMessageRepository.deleteById(id);
        return "redirect:/admin/messages";
    }
}