package com.example.Spectacle_phase1.Repository;

import com.example.Spectacle_phase1.Model.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

    List<ContactMessage> findTop5ByOrderByIdDesc();
}