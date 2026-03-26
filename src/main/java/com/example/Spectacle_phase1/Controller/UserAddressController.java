package com.example.Spectacle_phase1.Controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.Spectacle_phase1.Model.Address;
import com.example.Spectacle_phase1.Model.User;
import com.example.Spectacle_phase1.Repository.AddressRepository;
import com.example.Spectacle_phase1.Repository.UserRepository;

import java.util.List;

@Controller
@RequestMapping("/user/addresses")
public class UserAddressController {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public UserAddressController(AddressRepository addressRepository,
                                 UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        return userRepository.findByUsername(auth.getName()).orElse(null);
    }

    @GetMapping
    public String listAddresses(Model model, Authentication auth) {
        User user = getCurrentUser(auth);
        if (user == null) {
            return "redirect:/login";
        }
        List<Address> addrs = addressRepository.findByUser(user);
        model.addAttribute("addresses", addrs);
        return "user/addresses/list";
    }

    @GetMapping("/add")
    public String addForm(Model model, Authentication auth) {
        User user = getCurrentUser(auth);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("address", new Address());
        return "user/addresses/form";
    }

    @PostMapping("/add")
    public String saveAddress(@ModelAttribute Address address, Authentication auth) {
        User user = getCurrentUser(auth);
        if (user == null) {
            return "redirect:/login";
        }
        address.setUser(user);
        addressRepository.save(address);
        return "redirect:/user/addresses";
    }

    @GetMapping("/update/{id}")
    public String updateForm(@PathVariable Long id, Model model, Authentication auth) {
        User user = getCurrentUser(auth);
        if (user == null) {
            return "redirect:/login";
        }
        Address addr = addressRepository.findById(id).orElse(new Address());
        if (!addr.getUser().equals(user)) {
            return "redirect:/user/addresses";
        }
        model.addAttribute("address", addr);
        return "user/addresses/form";
    }

    @PostMapping("/update")
    public String updateAddress(@ModelAttribute Address address, Authentication auth) {
        User user = getCurrentUser(auth);
        if (user == null) {
            return "redirect:/login";
        }
        address.setUser(user);
        addressRepository.save(address);
        return "redirect:/user/addresses";
    }

    @GetMapping("/delete/{id}")
    public String deleteAddress(@PathVariable Long id, Authentication auth) {
        User user = getCurrentUser(auth);
        if (user == null) {
            return "redirect:/login";
        }
        addressRepository.findById(id).ifPresent(addr -> {
            if (addr.getUser().equals(user)) {
                addressRepository.delete(addr);
            }
        });
        return "redirect:/user/addresses";
    }
}
