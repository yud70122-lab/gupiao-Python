package com.stock.analysis.repository;

import com.stock.analysis.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Page<User> findByUsernameContainingOrRealNameContaining(String username, String realName, Pageable pageable);
    java.util.Optional<User> findByUsername(String username);
    java.util.Optional<User> findByPhone(String phone);
    java.util.Optional<User> findByEmail(String email);
}
