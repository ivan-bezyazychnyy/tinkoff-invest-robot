package ibez89.tinkoffinvestrobot.tinkoffclient;

import lombok.Data;
import ru.tinkoff.piapi.core.models.Money;
import ru.tinkoff.piapi.core.models.SecurityPosition;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class Portfolio {

    private final List<Money> money;

    private final List<SecurityPosition> securities;

    public BigDecimal getMoneyWithCurrency(String currencyCode) {
        return money.stream()
                .filter(money -> money.getCurrency().getCurrencyCode().equalsIgnoreCase(currencyCode))
                .findFirst()
                .map(Money::getValue)
                .orElse(BigDecimal.ZERO);
    }

    public List<SecurityPosition> getPositionsForInstruments(Set<String> instrumentNames) {
        return securities.stream()
                .filter(securityPosition -> instrumentNames.contains(securityPosition.getFigi()))
                .collect(Collectors.toList());
    }
}
