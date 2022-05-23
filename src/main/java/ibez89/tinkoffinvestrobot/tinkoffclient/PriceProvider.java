package ibez89.tinkoffinvestrobot.tinkoffclient;

import java.math.BigDecimal;

public interface PriceProvider {

    BigDecimal getPrice(String figi);
}
