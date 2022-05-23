package ibez89.tinkoffinvestrobot.model;

import ibez89.tinkoffinvestrobot.api.dto.OrderDto;
import ibez89.tinkoffinvestrobot.api.model.OrderDirection;
import ibez89.tinkoffinvestrobot.api.model.OrderStatus;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "\"order\"")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@ToString
public class Order extends AbstractEntity {

    private String tinkoffAccountId;

    private boolean sandbox;

    private String figi;

    private long lots;

    @Enumerated(EnumType.STRING)
    private OrderDirection direction;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private Instant placeAt;

    private String tinkoffOrderId;

    private UUID strategyExecutionId;

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public void setPlaceAt(Instant placeAt) {
        this.placeAt = placeAt;
    }

    public void setTinkoffOrderId(String tinkoffOrderId) {
        this.tinkoffOrderId = tinkoffOrderId;
    }

    public OrderDto toDto() {
        return OrderDto.builder()
                .id(id)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .figi(figi)
                .lots(lots)
                .direction(direction)
                .status(status)
                .placeAt(placeAt)
                .tinkoffOrderId(tinkoffOrderId)
                .build();
    }
}
