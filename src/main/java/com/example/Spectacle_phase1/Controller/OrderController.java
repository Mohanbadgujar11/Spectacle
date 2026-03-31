package com.example.Spectacle_phase1.Controller;

import com.example.Spectacle_phase1.Model.Order;
import com.example.Spectacle_phase1.Model.OrderItem;
import com.example.Spectacle_phase1.Model.Product;
import com.example.Spectacle_phase1.Model.User;
import com.example.Spectacle_phase1.Repository.AddressRepository;
import com.example.Spectacle_phase1.Repository.OrderRepository;
import com.example.Spectacle_phase1.Repository.ProductRepository;
import com.example.Spectacle_phase1.Repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;

    public OrderController(OrderRepository orderRepository, UserRepository userRepository,
                           ProductRepository productRepository, AddressRepository addressRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.addressRepository = addressRepository;
    }

    // View All Orders
    @GetMapping
    public String getAllOrders(Model model) {
        List<Order> orders = orderRepository.findAll();
        // Defensively filter out orders with missing critical data to prevent template crashes
        List<Order> validOrders = orders.stream()
                .filter(o -> o.getUser() != null && o.getAddress() != null)
                .collect(Collectors.toList());
        model.addAttribute("orders", validOrders);
        return "Admin/Order/View_order";
    }

    // Show Add Order Form
    @GetMapping("/add")
    public String addOrderForm(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("addresses", addressRepository.findAll());
        return "Admin/Order/Add_order";
    }

    // Handle Add Order Submission
    @PostMapping("/add")
    public String addOrder(@RequestParam Long userId,
                           @RequestParam Long productId,
                           @RequestParam Long addressId,
                           @RequestParam Integer quantity) {
        Order order = new Order();
        User user = userRepository.findById(userId).orElseThrow();
        Product product = productRepository.findById(productId).orElseThrow();

        order.setUser(user);
        order.setAddress(addressRepository.findById(addressId).orElse(null));
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("Placed");
        order.setPaymentMethod("COD");

        // Calculate price
        double price = product.getDiscountedPrice() != null ? product.getDiscountedPrice() : product.getSellingPrice();
        order.setTotalAmount(price * quantity);

        // Create Order Item
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setPrice(price);

        List<OrderItem> items = new ArrayList<>();
        items.add(item);
        order.setOrderItems(items);

        orderRepository.save(order);
        return "redirect:/admin/orders";
    }

    // Show Update Order Form
    @GetMapping("/update/{id}")
    public String updateOrderForm(@PathVariable Long id, Model model) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
        
        model.addAttribute("order", order);
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("addresses", addressRepository.findAll());
        
        // Pre-fill logic for the first item in the order
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            OrderItem firstItem = order.getOrderItems().get(0);
            model.addAttribute("currentProductId", firstItem.getProduct().getId());
            model.addAttribute("currentQuantity", firstItem.getQuantity());
        }
        
        return "Admin/Order/Update_order";
    }

    // Handle Update Order Submission
    @PostMapping("/update")
    public String updateOrder(@RequestParam Long id,
                              @RequestParam Long productId,
                              @RequestParam Long addressId,
                              @RequestParam Integer quantity) {
        
        Order order = orderRepository.findById(id).orElseThrow();
        Product product = productRepository.findById(productId).orElseThrow();

        order.setAddress(addressRepository.findById(addressId).orElse(order.getAddress()));

        // Update logic: Replace items with the new selection
        List<OrderItem> items = order.getOrderItems();
        if (items == null) items = new ArrayList<>();
        
        OrderItem item;
        if (!items.isEmpty()) {
            item = items.get(0); 
        } else {
            item = new OrderItem();
            item.setOrder(order);
            items.add(item);
        }
        
        item.setProduct(product);
        item.setQuantity(quantity);
        double price = product.getDiscountedPrice() != null ? product.getDiscountedPrice() : product.getSellingPrice();
        item.setPrice(price);
        
        order.setTotalAmount(price * quantity);
        order.setOrderItems(items);

        orderRepository.save(order);
        return "redirect:/admin/orders";
    }

    // Delete Order
    @GetMapping("/delete/{id}")
    public String deleteOrder(@PathVariable Long id) {
        orderRepository.deleteById(id);
        return "redirect:/admin/orders";
    }
}
