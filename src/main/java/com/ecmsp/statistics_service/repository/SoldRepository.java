package com.ecmsp.statistics_service.repository;

import com.ecmsp.statistics_service.model.Sold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SoldRepository extends JpaRepository<Sold, UUID> {

    @Query("SELECT s FROM Sold s WHERE s.variantId = :variantId AND s.date BETWEEN :fromDate AND :toDate ORDER BY s.date ASC")
    List<Sold> findByVariantIdAndDateBetween(
            @Param("variantId") UUID variantId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    @Query("SELECT s FROM Sold s WHERE s.variantId = :variantId ORDER BY s.date ASC")
    List<Sold> findByVariantIdOrderByDateAsc(@Param("variantId") UUID variantId);
}
