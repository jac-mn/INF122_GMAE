package gmae.core.services;

import java.util.Optional;

/**
 * Holds optional references to all engine-provided services.
 *
 * <p>The engine constructs a {@code ServiceBundle} and passes it to each
 * adventure via {@link gmae.core.api.MiniAdventure#bindServices(ServiceBundle)}
 * before the game loop starts. Adventures that need a service retrieve it
 * from here; services that are unavailable return {@link Optional#empty()}.</p>
 *
 * <p>Use the {@link #builder()} for construction.</p>
 */
public final class ServiceBundle {

    private final GmaeTimeService       timeService;
    private final GmaeInventoryService  inventoryService;
    private final GmaeQuestEventService questEventService;

    private ServiceBundle(Builder b) {
        this.timeService       = b.timeService;
        this.inventoryService  = b.inventoryService;
        this.questEventService = b.questEventService;
    }

    public Optional<GmaeTimeService>       timeService()       { return Optional.ofNullable(timeService); }
    public Optional<GmaeInventoryService>  inventoryService()  { return Optional.ofNullable(inventoryService); }
    public Optional<GmaeQuestEventService> questEventService() { return Optional.ofNullable(questEventService); }

    /** Returns an empty bundle (no services available). */
    public static ServiceBundle empty() { return new Builder().build(); }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private GmaeTimeService       timeService;
        private GmaeInventoryService  inventoryService;
        private GmaeQuestEventService questEventService;

        private Builder() {}

        public Builder timeService(GmaeTimeService v)             { timeService = v;       return this; }
        public Builder inventoryService(GmaeInventoryService v)   { inventoryService = v;  return this; }
        public Builder questEventService(GmaeQuestEventService v) { questEventService = v; return this; }

        public ServiceBundle build() { return new ServiceBundle(this); }
    }
}
