package com.example.Spectacle_phase1.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Spectacle_phase1.Model.Product;

public interface ProductRepository extends JpaRepository <Product,Long>{

    List<Product> findByCategory(String category);

    
} 
