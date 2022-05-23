package ibez89.tinkoffinvestrobot.model;

import ibez89.tinkoffinvestrobot.api.dto.StrategyExecutionDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "strategy_execution")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class StrategyExecution extends AbstractEntity {

    private UUID strategyId;

    private String tinkoffAccountId;

    private boolean sandbox;

    private Instant startedAt;

    private Instant endedAt;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "strategyExecutionId")
    private List<Order> orders;

    public void setEndedAt(Instant endedAt) {
        this.endedAt = endedAt;
    }

    public StrategyExecutionDto toDto() {
        return StrategyExecutionDto.builder()
                .id(id)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .strategyId(strategyId)
                .tinkoffAccountId(tinkoffAccountId)
                .sandbox(sandbox)
                .startedAt(startedAt)
                .endedAt(endedAt)
                .orders(orders.stream()
                        .map(Order::toDto)
                        .collect(Collectors.toList()))
                .build();
    }
}
