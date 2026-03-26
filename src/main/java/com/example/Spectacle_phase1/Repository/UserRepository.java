package com.example.Spectacle_phase1.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Spectacle_phase1.Model.User;

public interface UserRepository extends JpaRepository<User,Long>{

    Optional<User> findByUsername(String username);

    
}