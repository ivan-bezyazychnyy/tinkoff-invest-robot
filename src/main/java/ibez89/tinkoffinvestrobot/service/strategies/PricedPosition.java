package ibez89.tinkoffinvestrobot.service.strategies;

import lombok.Builder;
import lombok.Getter;
import ru.tinkoff.piapi.contract.v1.Instrument;

import java.math.BigDecimal;

@Builder
@Getter
class PricedPosition {

    private final Instrument instrument;

    private final long quantity;

    private final BigDecimal price;

    public BigDecimal getValue() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    public BigDecimal calculateInsufficiency(BigDecimal targetValue) {
        return targetValue.subtract(getValue());
    }

    public BigDecimal calculateExcess(BigDecimal targetValue) {
        return getValue().subtract(targetValue);
    }

    @Override
    public String toString() {
        return "PricedPosition{" +
                "instrument=" + instrument.getFigi() +
                ", quantity=" + quantity +
                ", price=" + price +
                '}';
    }
}
