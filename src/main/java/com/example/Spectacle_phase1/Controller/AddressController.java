package com.example.Spectacle_phase1.Controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.Spectacle_phase1.Model.Address;
import com.example.Spectacle_phase1.Repository.AddressRepository;
import com.example.Spectacle_phase1.Repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/addresses")
public class AddressController {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressController(AddressRepository addressRepository, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String viewAddresses(Model model) {
        List<Address> addresses = addressRepository.findAll();
        // Filter out addresses that might have a null user to prevent template crashes
        List<Address> validAddresses = addresses.stream()
                .filter(a -> a.getUser() != null)
                .collect(Collectors.toList());
        model.addAttribute("addresses", validAddresses);
        return "Admin/Address/View_address";  
    }

    @GetMapping("/add")
    public String addAddressForm(Model model) {
        model.addAttribute("address", new Address());  
        model.addAttribute("users", userRepository.findAll());  
        return "Admin/Address/Add_address";  
    }

    @PostMapping("/add")
    public String addAddress(@ModelAttribute Address address) {
        addressRepository.save(address);  
        return "redirect:/admin/addresses";
    }

    @GetMapping("/update/{id}")
    public String updateAddressForm(@PathVariable Long id, Model model) {
        Address address = addressRepository.findById(id).orElse(new Address());
        
        model.addAttribute("address", address);
        model.addAttribute("users", userRepository.findAll());  
        return "Admin/Address/Update_address";  
    }

    @PostMapping("/update")
    public String updateAddress(@ModelAttribute Address address) {
        addressRepository.save(address);  
        return "redirect:/admin/addresses";  
    }

    @GetMapping("/delete/{id}")
    public String deleteAddress(@PathVariable Long id) {
        addressRepository.deleteById(id);  
        return "redirect:/admin/addresses";  
    }
}