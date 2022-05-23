package ibez89.tinkoffinvestrobot.dbqueue;

import ru.yoomoney.tech.dbqueue.api.TaskPayloadTransformer;

import java.util.UUID;

public class UuidTaskPayloadTransformer implements TaskPayloadTransformer<UUID> {

    private static final UuidTaskPayloadTransformer INSTANCE = new UuidTaskPayloadTransformer();

    public static UuidTaskPayloadTransformer getInstance() {
        return INSTANCE;
    }

    private UuidTaskPayloadTransformer() {
    }

    @Override
    public UUID toObject(String value) {
        return value == null ? null : UUID.fromString(value);
    }

    @Override
    public String fromObject(UUID uuid) {
        return uuid == null ? null : uuid.toString();
    }
}
