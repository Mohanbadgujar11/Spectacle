package com.example.Spectacle_phase1.Controller;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.Spectacle_phase1.Model.Product;
import com.example.Spectacle_phase1.Model.enums.Category;
import com.example.Spectacle_phase1.Repository.ProductRepository;
import org.springframework.transaction.annotation.Transactional;

@Controller
@RequestMapping("/admin/products")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public String viewproducts(Model model) {
        model.addAttribute("products", productRepository.findAll());
        return "Admin/Product/View_product";
    }

    @GetMapping("/add")
    public String addproductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", Category.values());
        return "Admin/Product/Add_product";
    }

    @PostMapping("/add")
    public String addproduct(@ModelAttribute Product product,
            @RequestParam("imageFile") MultipartFile imageFile) throws IOException {

        if (!imageFile.isEmpty()) {
            product.setProductImage(imageFile.getBytes());
        }

        productRepository.save(product);
        return "redirect:/admin/products";
    }

    @GetMapping("/update/{id}")
    public String updateproductForm(@PathVariable Long id, Model model) {
        Product product = productRepository.findById(id).orElse(new Product());
        model.addAttribute("product", product);
        model.addAttribute("categories", Category.values());
        return "Admin/Product/Update_product";
    }

    @PostMapping("/update")
    public String updateproduct(@ModelAttribute Product product,
            @RequestParam("imageFile") MultipartFile imageFile) throws IOException {

        // If a new image is uploaded, set it
        if (!imageFile.isEmpty()) {
            product.setProductImage(imageFile.getBytes());
        } else {
            // If no new image, keep the existing one from the database
            Product existingProduct = productRepository.findById(product.getId()).orElse(new Product());
            product.setProductImage(existingProduct.getProductImage());
        }

        productRepository.save(product);
        return "redirect:/admin/products";
    }

    @GetMapping("/delete/{id}")
    public String deleteproduct(@PathVariable Long id) {
        productRepository.deleteById(id);
        return "redirect:/admin/products";
    }

    @GetMapping("/delete")
    public String deleteAllProducts(RedirectAttributes redirectAttributes) {
        productRepository.deleteAll();
        redirectAttributes.addFlashAttribute("message", "All products deleted successfully!");
        return "redirect:/admin/products";
    }

    @GetMapping("/image/{id}")
    @ResponseBody // returned raw object directly to the HTTP response body not the template.
    public byte[] getImage(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(Product::getProductImage)
                .orElse(null); // Return null if product not found, preventing a crash.
    }

    // REST API endpoint for fetching product details (used by cart.html)
    @GetMapping("/api/{id}")
    @ResponseBody
    public Product getProductAPI(@PathVariable Long id) {
        return productRepository.findById(id).orElse(null);
    }
}