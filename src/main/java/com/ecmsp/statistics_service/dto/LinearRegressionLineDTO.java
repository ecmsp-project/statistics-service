package com.ecmsp.statistics_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinearRegressionLineDTO {
    private Double slope;
    private Double intercept;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private LocalDateTime estimatedDepletionDate;
    private Double rSquared;
}
