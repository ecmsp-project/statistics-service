package com.ecmsp.statistics_service.api.kafka;

import com.ecmsp.statistics_service.service.StatisticsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
class KafkaOrderEventConsumer {
    private final StatisticsService statisticsService;
    private final ObjectMapper objectMapper;

    KafkaOrderEventConsumer(StatisticsService statisticsService, ObjectMapper objectMapper) {
        this.statisticsService = statisticsService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${kafka.topic.product.variant-sold}")
    public void consumeVariantSold(@Payload String variantSoldEventJson) throws JsonProcessingException {
        log.info("Raw variant-sold message received: [{}]", variantSoldEventJson);

        KafkaVariantSoldEvent event = objectMapper.readValue(variantSoldEventJson, KafkaVariantSoldEvent.class);

        log.info("Received variant-sold event for variant: {}, product: {}, quantity: {}",
                event.variantId(), event.productId(), event.quantitySold());

        try {
            statisticsService.recordVariantSold(event);
        } catch (Exception e) {
            log.error("Failed to process variant-sold event for variant: {}", event.variantId(), e);
        }
    }

    @KafkaListener(topics = "${kafka.topic.product.variant-stock-updated}")
    public void consumeVariantStockUpdated(@Payload String variantStockUpdatedEventJson) throws JsonProcessingException {
        log.info("Raw variant-stock-updated message received: [{}]", variantStockUpdatedEventJson);

        KafkaVariantStockUpdatedEvent event = objectMapper.readValue(variantStockUpdatedEventJson, KafkaVariantStockUpdatedEvent.class);

        log.info("Received variant-stock-updated event for variant: {}, quantity: {}",
                event.variantId(), event.deliveredQuantity());

        try {
            statisticsService.recordVariantStockUpdated(event);
        } catch (Exception e) {
            log.error("Failed to process variant-stock-updated event for variant: {}", event.variantId(), e);
        }
    }
}