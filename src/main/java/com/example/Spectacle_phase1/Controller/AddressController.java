package com.example.Spectacle_phase1.Controller;


import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.Spectacle_phase1.Model.Address;
import com.example.Spectacle_phase1.Model.Order;
import com.example.Spectacle_phase1.Repository.AddressRepository;
import com.example.Spectacle_phase1.Repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/addresses")
public class AddressController {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final com.example.Spectacle_phase1.Repository.OrderRepository orderRepository;

    public AddressController(AddressRepository addressRepository, UserRepository userRepository, com.example.Spectacle_phase1.Repository.OrderRepository orderRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public String viewAddresses(Model model) {
        List<Address> addresses = addressRepository.findAll();
        // Filter out addresses that might have a null user to prevent template crashes
        List<Address> validAddresses = addresses.stream()
                .filter(a -> a.getUser() != null)
                .collect(Collectors.toList());
        model.addAttribute("addresses", validAddresses);
		return "Admin/Address/view_Address";  
	}

	@GetMapping("/add")
	public String addAddressForm(Model model) {
		model.addAttribute("address", new Address());  
		model.addAttribute("users", userRepository.findAll());  
		return "Admin/Address/add_address";  
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
		return "Admin/Address/update_Address";  
	}

	@PostMapping("/update")
    public String updateAddress(@ModelAttribute @NonNull Address address) {
        addressRepository.save(address);  
        return "redirect:/admin/addresses";  
    }

    @PostMapping("/delete/{id}")
    @Transactional
    public String deleteAddress(@PathVariable Long id) {
        Address addressToDelete = addressRepository.findById(id).orElse(null);
        if (addressToDelete != null) {
            // Find all orders associated with this address
            List<Order> orders = orderRepository.findByAddress(addressToDelete);
            // Set the address to null for each order to break the link
            orders.forEach(order -> order.setAddress(null));
            orderRepository.saveAll(orders);
            // Now it's safe to delete the address
            addressRepository.delete(addressToDelete);
        }
        return "redirect:/admin/addresses";  
    }
}