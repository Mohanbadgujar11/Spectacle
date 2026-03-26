package com.example.Spectacle_phase1.Model;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

@Entity
@Data
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;

    private String category;

    @Column(length = 1000)
    private String small_description;
    private String description;

    private Integer sellingPrice;
    private Integer discountedPrice;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    @JsonIgnore
    private byte[] productImage;

    // Automatically delete associated cart items when a product is deleted
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Cart> cartItems;
}
