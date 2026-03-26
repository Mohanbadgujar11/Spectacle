package com.example.Spectacle_phase1.Repository;

import com.example.Spectacle_phase1.Model.Address;
import com.example.Spectacle_phase1.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUser(User user);
}