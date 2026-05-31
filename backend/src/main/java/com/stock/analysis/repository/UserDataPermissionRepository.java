package com.stock.analysis.repository;

import com.stock.analysis.entity.UserDataPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDataPermissionRepository extends JpaRepository<UserDataPermission, Long> {
    Optional<UserDataPermission> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}