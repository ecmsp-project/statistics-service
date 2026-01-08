package com.ecmsp.statistics_service.api.kafka;

public record KafkaVariantStockUpdatedEvent(
        String eventId,
        String variantId,
        Integer deliveredQuantity,
        String deliveredAt
) {
}
