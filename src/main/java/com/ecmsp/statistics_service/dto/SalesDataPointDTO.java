package com.ecmsp.statistics_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesDataPointDTO {
    private LocalDateTime date;
    private Integer quantity;
    private BigDecimal totalRevenue;
}
