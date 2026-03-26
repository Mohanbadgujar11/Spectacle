package com.example.Spectacle_phase1.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Spectacle_phase1.Model.Cart;

public interface CartRepository extends JpaRepository <Cart,Long>{

    // find all cart items belonging to a specific user
    java.util.List<Cart> findByUser(com.example.Spectacle_phase1.Model.User user);

    // find cart entry for a user+product combo (useful for incrementing quantity)
    java.util.Optional<Cart> findByUserAndProduct(com.example.Spectacle_phase1.Model.User user, com.example.Spectacle_phase1.Model.Product product);

    // delete an entry by user and product (if needed)
    void deleteByUserAndProduct(com.example.Spectacle_phase1.Model.User user, com.example.Spectacle_phase1.Model.Product product);
} 
