package com.ecmsp.statistics_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sold")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sold {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "variant_id", nullable = false)
    private UUID variantId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(name = "price", nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "margin", precision = 19, scale = 4)
    private BigDecimal margin;

    @Column(name = "stock_remaining")
    private Integer stockRemaining;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;
}
