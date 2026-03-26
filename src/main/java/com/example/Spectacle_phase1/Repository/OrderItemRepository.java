package com.example.Spectacle_phase1.Repository;

import com.example.Spectacle_phase1.Model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}