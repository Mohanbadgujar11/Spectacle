package com.example.Spectacle_phase1.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Spectacle_phase1.Model.ContactMessage;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

    List<ContactMessage> findTop5ByOrderByIdDesc();
}
