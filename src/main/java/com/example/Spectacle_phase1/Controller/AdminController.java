package com.example.Spectacle_phase1.Controller;

import com.example.Spectacle_phase1.Repository.*;
import com.example.Spectacle_phase1.Model.User;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ContactMessageRepository contactMessageRepository;
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AdminController(ContactMessageRepository contactMessageRepository,
                           OrderRepository orderRepository,
                           CartRepository cartRepository,
                           AddressRepository addressRepository,
                           UserRepository userRepository) {
        this.contactMessageRepository = contactMessageRepository;
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
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

    /**
     * Deletes all user-related data from the database, but preserves all products.
     * This is a destructive action intended for resetting the application state.
     * The @Transactional annotation ensures this is an all-or-nothing operation.
     */
    @PostMapping("/delete-all-user-data")
    @Transactional
    public String deleteAllUserData(RedirectAttributes redirectAttributes) {
        // 1. Find all users that are NOT admins.
        List<User> usersToDelete = userRepository.findAll().stream()
                .filter(user -> !"ADMIN".equals(user.getRole()))
                .collect(Collectors.toList());

        if (!usersToDelete.isEmpty()) {
            // The deletion order is critical to avoid foreign key constraint violations.
            // 2. Delete all data associated with the non-admin users.
            orderRepository.deleteAll(orderRepository.findByUserIn(usersToDelete));
            cartRepository.deleteAll(cartRepository.findByUserIn(usersToDelete));
            addressRepository.deleteAll(addressRepository.findByUserIn(usersToDelete));

            // 3. Now it's safe to delete the non-admin users themselves.
            userRepository.deleteAll(usersToDelete);
        }

        // 4. Delete all contact messages (these are not tied to specific users).
        contactMessageRepository.deleteAll();

        redirectAttributes.addFlashAttribute("message", "Successfully deleted all non-admin user data. Admin accounts and products were not affected.");
        return "redirect:/admin";
    }
}
