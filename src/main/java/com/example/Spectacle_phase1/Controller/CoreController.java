package com.example.Spectacle_phase1.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.Spectacle_phase1.Model.Product;
import com.example.Spectacle_phase1.Repository.ProductRepository;
import org.springframework.ui.Model;
@Controller

public class CoreController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    private final ProductRepository productRepository;

    public CoreController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    // @GetMapping("/register")
    // public String register() {
    //     return "register";
    // }

    // cart and checkout are now handled by dedicated controllers
    // kept in UserCartController and CheckoutController respectively.
    @GetMapping("/product-details")
    public String productDetails(@RequestParam(name = "id", required = false) Long id, Model model) {
        if (id != null) {
            Product product = productRepository.findById(id).orElse(null);
            if (product != null) {
                model.addAttribute("product", product);
            }
        }
        return "product-details";
    }

    @GetMapping("/image/{id}")
    @ResponseBody
    public byte[] getImage(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(Product::getProductImage)
                .orElse(null);
    }
}