package ibez89.tinkoffinvestrobot.model;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import ibez89.tinkoffinvestrobot.api.dto.StrategyDto;
import ibez89.tinkoffinvestrobot.api.model.StrategyStatus;
import ibez89.tinkoffinvestrobot.api.model.StrategyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.time.Duration;
import java.util.List;

@Entity
@Table(name = "strategy")
@TypeDefs({
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Strategy extends AbstractEntity {

    @Enumerated(EnumType.STRING)
    private StrategyStatus status;

    private String tinkoffAccountId;

    private boolean sandbox;

    @Enumerated(EnumType.STRING)
    private StrategyType type;

    private Duration executionPeriod;

    @Type(type = "jsonb")
    private List<String> instruments;

    public void setStatus(StrategyStatus status) {
        this.status = status;
    }

    public void setExecutionPeriod(Duration executionPeriod) {
        this.executionPeriod = executionPeriod;
    }

    public void setInstruments(List<String> instruments) {
        this.instruments = instruments;
    }

    public StrategyDto toDto() {
        return StrategyDto.builder()
                .id(id)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .status(status)
                .tinkoffAccountId(tinkoffAccountId)
                .sandbox(sandbox)
                .type(type)
                .executionPeriod(executionPeriod)
                .instruments(instruments)
                .build();
    }
}
