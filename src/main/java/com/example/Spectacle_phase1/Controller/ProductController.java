package com.example.Spectacle_phase1.Controller;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.Spectacle_phase1.Model.Product;
import com.example.Spectacle_phase1.Model.enums.Category;
import com.example.Spectacle_phase1.Repository.CartRepository;
import com.example.Spectacle_phase1.Repository.ProductRepository;
import org.springframework.transaction.annotation.Transactional;

@Controller
@RequestMapping("/admin/products")
public class ProductController {

    private final ProductRepository productRepository;
    private final CartRepository cartRepository;

    public ProductController(ProductRepository productRepository, CartRepository cartRepository) {
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public String viewproducts(Model model) {
        model.addAttribute("products", productRepository.findAll());
        return "Admin/Product/view_Product";
    }

    @GetMapping("/add")
    public String addproductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", Category.values());
        return "Admin/Product/add_Product";
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
        return "Admin/Product/update_Product";
    }

    @PostMapping("/update")
    public String updateproduct(@ModelAttribute Product product,
            @RequestParam("imageFile") MultipartFile imageFile) throws IOException {

        if (!imageFile.isEmpty()) {
            product.setProductImage(imageFile.getBytes());
        } else {
            Product existingProduct = productRepository.findById(product.getId()).orElse(new Product());
            product.setProductImage(existingProduct.getProductImage());
        }

        productRepository.save(product);
        return "redirect:/admin/products";
    }

    @PostMapping("/delete/{id}")
    @Transactional
    public String deleteproduct(@PathVariable Long id) {
        productRepository.findById(id).ifPresent(product -> {
            // Before deleting the product, we must delete all associated Cart items
            // to maintain data integrity and prevent checkout errors.
            // NOTE: This requires `List<Cart> findByProduct(Product product);` in your CartRepository.
            cartRepository.deleteAll(cartRepository.findByProduct(product));

            productRepository.delete(product);
        });
        return "redirect:/admin/products";
    }

    @PostMapping("/delete-all")
    @Transactional
    public String deleteAllProducts(RedirectAttributes redirectAttributes) {
        // To maintain data integrity, first delete all items from carts,
        // then delete all products.
        cartRepository.deleteAll();
        productRepository.deleteAll();
        redirectAttributes.addFlashAttribute("message", "All products deleted successfully!");
        return "redirect:/admin/products"; // Changed path to be more descriptive
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