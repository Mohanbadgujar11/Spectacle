package com.example.Spectacle_phase1.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.Spectacle_phase1.Model.enums.Category;
import com.example.Spectacle_phase1.Repository.ProductRepository;

@Controller
public class ShopController {

    private final ProductRepository productRepository;

    public ShopController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/eyeglasses")
    public String eyeglasses(Model model) {
        model.addAttribute("products", productRepository.findByCategory(Category.EYEGLASSES.name()));
        return "eyeglasses";
    }

    @GetMapping("/sunglasses")
    public String sunglasses(Model model) {
        model.addAttribute("products", productRepository.findByCategory(Category.SUNGLASSES.name()));
        return "sunglasses";
    }

    @GetMapping("/screen-glasses")
    public String screenGlasses(Model model) {
        // Changed to SPECIAL_POWER to match your "3.special power" category description
        model.addAttribute("products", productRepository.findByCategory(Category.SCREEN_GLASSES.name()));
        return "screen-glasses";
    }

    @GetMapping("/contact-lenses")
    public String contactLenses(Model model) {
        model.addAttribute("products", productRepository.findByCategory(Category.CONTACT_LENSES.name()));
        return "contact-lenses";
    }

    @GetMapping("/kids-glasses")
    public String kidsGlasses(Model model) {
        model.addAttribute("products", productRepository.findByCategory(Category.KIDS_GLASSES.name()));
        return "kids-glasses";
    }
}