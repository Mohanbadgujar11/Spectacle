package com.example.Spectacle_phase1.Model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "addresses")
public class Address {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String area;
    
    @Column(nullable = false)
    private String city;
    
    @Column(nullable = false)
    private String state;
    
    @Column(nullable = false)
    private Integer pincode;
    
    // Helper methods
    public String getFullAddress() {
        return area + ", " + city + ", " + state + " - " + pincode;
    }
    
    public String getFullName() {
        return name;
    }
}
