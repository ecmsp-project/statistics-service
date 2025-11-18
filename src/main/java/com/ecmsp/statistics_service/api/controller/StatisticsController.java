package com.ecmsp.statistics_service.api.controller;

import com.ecmsp.statistics_service.dto.StockLevelOverTimeDTO;
import com.ecmsp.statistics_service.dto.VariantSalesOverTimeDTO;
import com.ecmsp.statistics_service.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/variants/{variantId}/sales")
    public ResponseEntity<VariantSalesOverTimeDTO> getVariantSalesOverTime(
            @PathVariable UUID variantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false, defaultValue = "30") Integer trendDays) {

        VariantSalesOverTimeDTO result = statisticsService.getVariantSalesOverTime(variantId, fromDate, toDate, trendDays);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/variants/{variantId}/stock")
    public ResponseEntity<StockLevelOverTimeDTO> getStockLevelOverTime(
            @PathVariable UUID variantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false, defaultValue = "30") Integer trendDays) {

        StockLevelOverTimeDTO result = statisticsService.getStockLevelOverTime(variantId, fromDate, toDate, trendDays);
        return ResponseEntity.ok(result);
    }
}
