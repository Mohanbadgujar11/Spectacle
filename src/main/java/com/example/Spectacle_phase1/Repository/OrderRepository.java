package com.example.Spectacle_phase1.Repository;

import com.example.Spectacle_phase1.Model.Order;
import com.example.Spectacle_phase1.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByOrderDateDesc(User user);
}