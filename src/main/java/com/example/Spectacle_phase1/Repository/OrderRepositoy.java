package com.example.Spectacle_phase1.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Spectacle_phase1.Model.Order;

public interface OrderRepositoy extends JpaRepository<Order,Long> {

    
} 
