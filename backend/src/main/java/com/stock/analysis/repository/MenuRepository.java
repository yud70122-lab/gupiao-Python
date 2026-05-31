package com.stock.analysis.repository;

import com.stock.analysis.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findByParentIdOrderBySortOrderAsc(Long parentId);
    List<Menu> findAllByOrderBySortOrderAsc();
    List<Menu> findByVisibleTrueOrderBySortOrderAsc();
}