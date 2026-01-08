package com.ecmsp.statistics_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO containing information about product variants available in statistics.
 * Used for autocomplete/selection in analytics frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariantInfoDTO {

    /**
     * Unique identifier of the product variant
     */
    private UUID variantId;

    /**
     * Product identifier this variant belongs to
     */
    private UUID productId;

    /**
     * Display name of the product
     */
    private String productName;

    /**
     * Whether this variant has sales data in SOLD table
     */
    private boolean hasSalesData;

    /**
     * Whether this variant has stock/delivery data in DELIVERY table
     */
    private boolean hasStockData;

    /**
     * Date of the most recent sale for this variant (if any)
     */
    private LocalDateTime lastSaleDate;

    /**
     * Most recent stock level from SOLD table (if any)
     */
    private Integer currentStock;
}
