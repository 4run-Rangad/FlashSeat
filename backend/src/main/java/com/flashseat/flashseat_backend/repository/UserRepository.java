package com.flashseat.flashseat_backend.repository;

import com.flashseat.flashseat_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
}
