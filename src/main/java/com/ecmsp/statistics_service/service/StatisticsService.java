package com.ecmsp.statistics_service.service;

import com.ecmsp.statistics_service.api.kafka.KafkaVariantSoldEvent;
import com.ecmsp.statistics_service.api.kafka.KafkaVariantStockUpdatedEvent;
import com.ecmsp.statistics_service.dto.*;
import com.ecmsp.statistics_service.model.Delivery;
import com.ecmsp.statistics_service.model.Sold;
import com.ecmsp.statistics_service.repository.DeliveryRepository;
import com.ecmsp.statistics_service.repository.SoldRepository;
import com.ecmsp.statistics_service.service.util.LinearRegressionCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsService {

    private final SoldRepository soldRepository;
    private final DeliveryRepository deliveryRepository;

    @Transactional
    public void recordVariantSold(KafkaVariantSoldEvent event) {
        Sold sold = Sold.builder()
                .id(UUID.fromString(event.eventId()))
                .variantId(UUID.fromString(event.variantId()))
                .productId(UUID.fromString(event.productId()))
                .productName(event.productName())
                .price(event.soldAt())
                .quantity(event.quantitySold())
                .margin(event.margin())
                .stockRemaining(event.stockRemaining())
                .date(LocalDateTime.now())
                .build();

        soldRepository.save(sold);
        log.info("Recorded sold event for variant: {}, quantity: {}", event.variantId(), event.quantitySold());
    }

    @Transactional
    public void recordVariantStockUpdated(KafkaVariantStockUpdatedEvent event) {
        Delivery delivery = Delivery.builder()
                .id(UUID.fromString(event.eventId()))
                .variantId(UUID.fromString(event.variantId()))
                .deliveredQuantity(event.deliveredQuantity())
                .deliveredAt(LocalDateTime.parse(event.deliveredAt()))
                .build();

        deliveryRepository.save(delivery);
        log.info("Recorded delivery event for variant: {}, quantity: {}", event.variantId(), event.deliveredQuantity());
    }

    public VariantSalesOverTimeDTO getVariantSalesOverTime(UUID variantId, LocalDate fromDate, LocalDate toDate, Integer trendDays) {
        LocalDateTime fromDateTime = fromDate.atStartOfDay();
        LocalDateTime toDateTime = toDate.atTime(LocalTime.MAX);

        List<Sold> sales = soldRepository.findByVariantIdAndDateBetween(variantId, fromDateTime, toDateTime);

        Map<LocalDate, List<Sold>> salesByDate = sales.stream()
                .collect(Collectors.groupingBy(sale -> sale.getDate().toLocalDate()));

        List<SalesDataPointDTO> dataPoints = salesByDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<Sold> dailySales = entry.getValue();

                    int totalQuantity = dailySales.stream()
                            .mapToInt(Sold::getQuantity)
                            .sum();

                    BigDecimal totalRevenue = dailySales.stream()
                            .map(sale -> sale.getPrice().multiply(BigDecimal.valueOf(sale.getQuantity())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return SalesDataPointDTO.builder()
                            .date(date.atStartOfDay())
                            .quantity(totalQuantity)
                            .totalRevenue(totalRevenue)
                            .build();
                })
                .collect(Collectors.toList());

        List<LinearRegressionLineDTO> regressionLines = calculateSalesRegressionLines(salesByDate, sales);

        String productName = sales.isEmpty() ? null : sales.get(0).getProductName();

        return VariantSalesOverTimeDTO.builder()
                .variantId(variantId)
                .productName(productName)
                .dataPoints(dataPoints)
                .regressionLines(regressionLines)
                .build();
    }

    private List<LinearRegressionLineDTO> calculateSalesRegressionLines(Map<LocalDate, List<Sold>> salesByDate, List<Sold> allSales) {
        List<LinearRegressionLineDTO> regressionLines = new ArrayList<>();

        Map<LocalDate, Integer> stockRemainingByDate = new TreeMap<>();
        for (Sold sale : allSales) {
            LocalDate saleDate = sale.getDate().toLocalDate();
            if (sale.getStockRemaining() != null) {
                stockRemainingByDate.put(saleDate, sale.getStockRemaining());
            }
        }

        List<LocalDate> stockDepletionDates = new ArrayList<>();
        Integer previousStock = null;
        for (Map.Entry<LocalDate, Integer> entry : stockRemainingByDate.entrySet()) {
            if (previousStock != null && previousStock > 0 && entry.getValue() == 0) {
                stockDepletionDates.add(entry.getKey());
            }
            previousStock = entry.getValue();
        }

        List<LocalDate> sortedDates = salesByDate.keySet().stream().sorted().collect(Collectors.toList());
        if (sortedDates.isEmpty()) {
            return regressionLines;
        }

        List<LocalDateTime> boundaries = new ArrayList<>();
        boundaries.add(sortedDates.get(0).atStartOfDay());
        for (LocalDate depletionDate : stockDepletionDates) {
            boundaries.add(depletionDate.atStartOfDay());
        }
        boundaries.add(sortedDates.get(sortedDates.size() - 1).atTime(LocalTime.MAX));

        for (int i = 0; i < boundaries.size() - 1; i++) {
            LocalDateTime periodStart = boundaries.get(i);
            LocalDateTime periodEnd = boundaries.get(i + 1);

            List<LinearRegressionCalculator.DataPoint> periodData = salesByDate.entrySet().stream()
                    .filter(entry -> {
                        LocalDateTime date = entry.getKey().atStartOfDay();
                        return !date.isBefore(periodStart) && date.isBefore(periodEnd);
                    })
                    .map(entry -> {
                        int totalQuantity = entry.getValue().stream()
                                .mapToInt(Sold::getQuantity)
                                .sum();
                        return new LinearRegressionCalculator.DataPoint(
                                entry.getKey().atStartOfDay(),
                                (double) totalQuantity
                        );
                    })
                    .collect(Collectors.toList());

            if (periodData.size() >= 2) {
                LinearRegressionCalculator.RegressionResult result =
                        LinearRegressionCalculator.calculateLinearRegression(periodData, periodStart);

                if (result != null) {
                    regressionLines.add(LinearRegressionLineDTO.builder()
                            .slope(result.getSlope())
                            .intercept(result.getIntercept())
                            .validFrom(periodStart)
                            .validTo(periodEnd)
                            .rSquared(result.getRSquared())
                            .build());
                }
            }
        }

        return regressionLines;
    }

    public StockLevelOverTimeDTO getStockLevelOverTime(UUID variantId, LocalDate fromDate, LocalDate toDate, Integer trendDays) {
        LocalDateTime fromDateTime = fromDate.atStartOfDay();
        LocalDateTime toDateTime = toDate.atTime(LocalTime.MAX);

        List<Sold> sales = soldRepository.findByVariantIdAndDateBetween(variantId, fromDateTime, toDateTime);
        List<Delivery> deliveries = deliveryRepository.findByVariantIdAndDeliveredAtBetween(variantId, fromDateTime, toDateTime);

        List<StockEvent> events = new ArrayList<>();

        for (Sold sale : sales) {
            events.add(new StockEvent(sale.getDate(), -sale.getQuantity(), sale.getStockRemaining()));
        }

        for (Delivery delivery : deliveries) {
            events.add(new StockEvent(delivery.getDeliveredAt(), delivery.getDeliveredQuantity(), null));
        }

        events.sort(Comparator.comparing(StockEvent::getDateTime));

        Map<LocalDate, Integer> stockByDate = new TreeMap<>();
        Integer currentStock = null;

        for (StockEvent event : events) {
            LocalDate eventDate = event.getDateTime().toLocalDate();

            if (currentStock == null) {
                if (event.getStockRemaining() != null) {
                    currentStock = event.getStockRemaining();
                } else {
                    currentStock = event.getChange();
                }
            } else {
                currentStock += event.getChange();
            }

            stockByDate.put(eventDate, currentStock);
        }

        List<StockDataPointDTO> dataPoints = stockByDate.entrySet().stream()
                .map(entry -> StockDataPointDTO.builder()
                        .date(entry.getKey().atStartOfDay())
                        .stockLevel(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        List<LinearRegressionLineDTO> regressionLines = calculateStockRegressionLines(stockByDate, deliveries, fromDateTime, toDateTime);

        LinearRegressionLineDTO trendLine = null;
        if (trendDays != null && trendDays > 0) {
            trendLine = calculateTrendLine(stockByDate, trendDays, toDate);
        }

        String productName = sales.isEmpty() ? null : sales.get(0).getProductName();

        return StockLevelOverTimeDTO.builder()
                .variantId(variantId)
                .productName(productName)
                .dataPoints(dataPoints)
                .regressionLines(regressionLines)
                .trendLine(trendLine)
                .build();
    }

    private List<LinearRegressionLineDTO> calculateStockRegressionLines(Map<LocalDate, Integer> stockByDate,
                                                                          List<Delivery> deliveries,
                                                                          LocalDateTime fromDateTime,
                                                                          LocalDateTime toDateTime) {
        List<LinearRegressionLineDTO> regressionLines = new ArrayList<>();

        List<LocalDateTime> deliveryDates = deliveries.stream()
                .map(Delivery::getDeliveredAt)
                .sorted()
                .collect(Collectors.toList());

        List<LocalDateTime> boundaries = new ArrayList<>();
        boundaries.add(fromDateTime);
        boundaries.addAll(deliveryDates);
        boundaries.add(toDateTime);

        for (int i = 0; i < boundaries.size() - 1; i++) {
            LocalDateTime periodStart = boundaries.get(i);
            LocalDateTime periodEnd = boundaries.get(i + 1);

            List<LinearRegressionCalculator.DataPoint> periodData = stockByDate.entrySet().stream()
                    .filter(entry -> {
                        LocalDateTime date = entry.getKey().atStartOfDay();
                        return !date.isBefore(periodStart) && date.isBefore(periodEnd);
                    })
                    .map(entry -> new LinearRegressionCalculator.DataPoint(
                            entry.getKey().atStartOfDay(),
                            entry.getValue().doubleValue()
                    ))
                    .collect(Collectors.toList());

            if (periodData.size() >= 2) {
                LinearRegressionCalculator.RegressionResult result =
                        LinearRegressionCalculator.calculateLinearRegression(periodData, periodStart);

                if (result != null) {
                    LocalDateTime depletionDate = LinearRegressionCalculator.estimateDepletionDate(result);

                    regressionLines.add(LinearRegressionLineDTO.builder()
                            .slope(result.getSlope())
                            .intercept(result.getIntercept())
                            .validFrom(periodStart)
                            .validTo(periodEnd)
                            .estimatedDepletionDate(depletionDate)
                            .rSquared(result.getRSquared())
                            .build());
                }
            }
        }

        return regressionLines;
    }

    private LinearRegressionLineDTO calculateTrendLine(Map<LocalDate, Integer> stockByDate, int trendDays, LocalDate toDate) {
        LocalDate trendStartDate = toDate.minusDays(trendDays);

        List<LinearRegressionCalculator.DataPoint> trendData = stockByDate.entrySet().stream()
                .filter(entry -> !entry.getKey().isBefore(trendStartDate) && !entry.getKey().isAfter(toDate))
                .map(entry -> new LinearRegressionCalculator.DataPoint(
                        entry.getKey().atStartOfDay(),
                        entry.getValue().doubleValue()
                ))
                .collect(Collectors.toList());

        if (trendData.size() < 2) {
            return null;
        }

        LocalDateTime referenceDate = trendStartDate.atStartOfDay();
        LinearRegressionCalculator.RegressionResult result =
                LinearRegressionCalculator.calculateLinearRegression(trendData, referenceDate);

        if (result == null) {
            return null;
        }

        LocalDateTime depletionDate = LinearRegressionCalculator.estimateDepletionDate(result);

        return LinearRegressionLineDTO.builder()
                .slope(result.getSlope())
                .intercept(result.getIntercept())
                .validFrom(referenceDate)
                .validTo(toDate.atTime(LocalTime.MAX))
                .estimatedDepletionDate(depletionDate)
                .rSquared(result.getRSquared())
                .build();
    }

    private static class StockEvent {
        private final LocalDateTime dateTime;
        private final Integer change;
        private final Integer stockRemaining;

        public StockEvent(LocalDateTime dateTime, Integer change, Integer stockRemaining) {
            this.dateTime = dateTime;
            this.change = change;
            this.stockRemaining = stockRemaining;
        }

        public LocalDateTime getDateTime() {
            return dateTime;
        }

        public Integer getChange() {
            return change;
        }

        public Integer getStockRemaining() {
            return stockRemaining;
        }
    }

    /**
     * Get all available variants that have statistical data (sales or deliveries)
     * @return List of variant information DTOs
     */
    public List<VariantInfoDTO> getAvailableVariants() {
        log.info("Fetching available variants with statistical data");

        // Get all distinct variants from SOLD table
        List<Object[]> distinctVariants = soldRepository.findDistinctVariants();

        // Get variants with delivery data
        List<UUID> variantsWithStock = deliveryRepository.findDistinctVariantIds();
        Set<UUID> stockVariantSet = new HashSet<>(variantsWithStock);

        // Get last sale dates
        List<Object[]> lastSaleDates = soldRepository.findLastSaleDates();
        Map<UUID, LocalDateTime> lastSaleDateMap = lastSaleDates.stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (LocalDateTime) row[1]
                ));

        // Get current stock levels
        List<Object[]> currentStocks = soldRepository.findCurrentStockLevels();
        Map<UUID, Integer> currentStockMap = currentStocks.stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (Integer) row[1]
                ));

        // Build variant info DTOs
        List<VariantInfoDTO> result = new ArrayList<>();

        for (Object[] row : distinctVariants) {
            UUID variantId = (UUID) row[0];
            UUID productId = (UUID) row[1];
            String productName = (String) row[2];

            VariantInfoDTO dto = VariantInfoDTO.builder()
                    .variantId(variantId)
                    .productId(productId)
                    .productName(productName)
                    .hasSalesData(true) // We got it from SOLD table
                    .hasStockData(stockVariantSet.contains(variantId))
                    .lastSaleDate(lastSaleDateMap.get(variantId))
                    .currentStock(currentStockMap.get(variantId))
                    .build();

            result.add(dto);
        }

        // Sort by product name
        result.sort(Comparator.comparing(VariantInfoDTO::getProductName));

        log.info("Found {} variants with statistical data", result.size());
        return result;
    }
}
