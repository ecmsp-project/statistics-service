package com.ecmsp.statistics_service.repository;

import com.ecmsp.statistics_service.model.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {

    @Query("SELECT d FROM Delivery d WHERE d.variantId = :variantId AND d.deliveredAt BETWEEN :fromDate AND :toDate ORDER BY d.deliveredAt ASC")
    List<Delivery> findByVariantIdAndDeliveredAtBetween(
            @Param("variantId") UUID variantId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    @Query("SELECT d FROM Delivery d WHERE d.variantId = :variantId ORDER BY d.deliveredAt ASC")
    List<Delivery> findByVariantIdOrderByDeliveredAtAsc(@Param("variantId") UUID variantId);

    @Query("SELECT DISTINCT d.variantId FROM Delivery d")
    List<UUID> findDistinctVariantIds();
}
