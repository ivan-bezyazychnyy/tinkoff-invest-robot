package ibez89.tinkoffinvestrobot.api.dto;

import ibez89.tinkoffinvestrobot.api.model.OrderDirection;
import ibez89.tinkoffinvestrobot.api.model.OrderStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record OrderDto(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        String figi,
        long lots,
        OrderDirection direction,
        OrderStatus status,
        Instant placeAt,
        String tinkoffOrderId) {
}
