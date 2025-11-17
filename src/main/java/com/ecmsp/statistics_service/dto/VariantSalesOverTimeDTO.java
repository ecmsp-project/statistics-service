package com.ecmsp.statistics_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariantSalesOverTimeDTO {
    private UUID variantId;
    private String productName;
    private List<SalesDataPointDTO> dataPoints;
    private List<LinearRegressionLineDTO> regressionLines;
}
