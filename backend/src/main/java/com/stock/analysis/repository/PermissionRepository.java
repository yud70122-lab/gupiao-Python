package com.stock.analysis.repository;

import com.stock.analysis.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    List<Permission> findByModule(String module);
    Optional<Permission> findByCode(String code);
    List<Permission> findAllByOrderByModuleAsc();
}