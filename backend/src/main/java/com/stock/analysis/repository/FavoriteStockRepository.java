package com.stock.analysis.repository;

import com.stock.analysis.entity.FavoriteStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteStockRepository extends JpaRepository<FavoriteStock, Long> {

    List<FavoriteStock> findByUserIdAndGroupIdOrderByAddTimeDesc(Long userId, Long groupId);

    List<FavoriteStock> findByUserIdOrderByAddTimeDesc(Long userId);

    Optional<FavoriteStock> findByUserIdAndCode(Long userId, String code);

    boolean existsByUserIdAndCode(Long userId, String code);

    @Modifying
    @Query("UPDATE FavoriteStock f SET f.groupId = :newGroupId WHERE f.userId = :userId AND f.groupId = :oldGroupId")
    void updateGroupIdByUserIdAndGroupId(Long userId, Long oldGroupId, Long newGroupId);

    @Modifying
    void deleteByUserIdAndCode(Long userId, String code);

    @Modifying
    void deleteByUserIdAndCodeIn(Long userId, List<String> codes);

    @Modifying
    void deleteByUserIdAndGroupId(Long userId, Long groupId);
}
