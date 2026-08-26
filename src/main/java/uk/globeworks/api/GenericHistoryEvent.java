package uk.globeworks.api;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Generic cross-plugin history / notification event.
 * <p>
 * Producers fire this after their own state change has committed.
 * Consumers must return quickly; any real work (DB, HTTP, file I/O)
 * should be handed off via {@link #handleAsync(Plugin, GenericHistoryEvent, Consumer)}.
 * <p>
 * <b>Subject model (hybrid):</b>
 * <ul>
 *   <li>Nation-scoped events → set {@link #getNation()}, leave subject null
 *       (or also set subject = "nation:&lt;uuid&gt;" for consistency).</li>
 *   <li>Non-nation events → leave nation null, set {@link #getSubject()}
 *       (e.g. "player:&lt;uuid&gt;", "town:&lt;uuid&gt;", "global").</li>
 *   <li>Truly global with no entity → both null (or subject = "global").</li>
 * </ul>
 */
public final class GenericHistoryEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final @Nullable NationRef nation;
    private final @Nullable String subject;
    private final @NotNull String eventType;
    private final @NotNull Map<String, String> payload;
    private final @NotNull List<UUID> actorIds;
    private final @NotNull Instant timestamp;
    private final @NotNull String eventId;

    private GenericHistoryEvent(
            @Nullable NationRef nation,
            @Nullable String subject,
            @NotNull String eventType,
            @NotNull Map<String, String> payload,
            @NotNull List<UUID> actorIds,
            @NotNull Instant timestamp,
            @NotNull String eventId
    ) {
        this.nation = nation;
        this.subject = subject;
        this.eventType = Objects.requireNonNull(eventType, "eventType");
        this.payload = Collections.unmodifiableMap(new HashMap<>(payload));
        this.actorIds = List.copyOf(actorIds);
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
        this.eventId = Objects.requireNonNull(eventId, "eventId");
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /**
     * Nation this event is primarily about, or null for non-nation / global events.
     */
    public @Nullable NationRef getNation() {
        return nation;
    }

    /**
     * Optional generic subject key for non-nation entities.
     * Examples: {@code "player:<uuid>"}, {@code "town:<uuid>"}, {@code "global"}.
     */
    public @Nullable String getSubject() {
        return subject;
    }

    /**
     * Namespaced event type, e.g. {@code "diplomacy.alliance_formed"}, {@code "siege.won"}.
     */
    public @NotNull String getEventType() {
        return eventType;
    }

    /**
     * Event-specific extra data. Unmodifiable.
     */
    public @NotNull Map<String, String> getPayload() {
        return payload;
    }

    /**
     * Players involved in the event (may be empty). Unmodifiable.
     */
    public @NotNull List<UUID> getActorIds() {
        return actorIds;
    }

    public @NotNull Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Unique id for this logical event. Useful for de-duplication.
     */
    public @NotNull String getEventId() {
        return eventId;
    }

    // -------------------------------------------------------------------------
    // Async helper – the convention enforcer
    // -------------------------------------------------------------------------

    /**
     * Hand off real work to an async task. Call this from a listener instead of
     * doing blocking work on the main thread.
     *
     * <pre>{@code
     * @EventHandler
     * public void onHistory(GenericHistoryEvent event) {
     *     GenericHistoryEvent.handleAsync(thisPlugin, event, e -> {
     *         // DB write, HTTP call, etc.
     *     });
     * }
     * }</pre>
     */
    public static void handleAsync(
            @NotNull Plugin plugin,
            @NotNull GenericHistoryEvent event,
            @NotNull Consumer<GenericHistoryEvent> work
    ) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(work, "work");
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> work.accept(event));
    }

    /**
     * Same as above but for work that does not need the event reference.
     */
    public static void handleAsync(
            @NotNull Plugin plugin,
            @NotNull Runnable work
    ) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(work, "work");
        Bukkit.getScheduler().runTaskAsynchronously(plugin, work);
    }

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    public static @NotNull Builder builder(@NotNull String eventType) {
        return new Builder(eventType);
    }

    public static final class Builder {
        private final String eventType;
        private NationRef nation;
        private String subject;
        private final Map<String, String> payload = new HashMap<>();
        private List<UUID> actorIds = List.of();
        private Instant timestamp = Instant.now();
        private String eventId = UUID.randomUUID().toString();

        private Builder(String eventType) {
            this.eventType = Objects.requireNonNull(eventType, "eventType");
        }

        public @NotNull Builder nation(@Nullable NationRef nation) {
            this.nation = nation;
            return this;
        }

        public @NotNull Builder subject(@Nullable String subject) {
            this.subject = subject;
            return this;
        }

        public @NotNull Builder payload(@NotNull Map<String, String> payload) {
            this.payload.clear();
            this.payload.putAll(payload);
            return this;
        }

        public @NotNull Builder put(@NotNull String key, @Nullable String value) {
            if (value != null) {
                this.payload.put(key, value);
            }
            return this;
        }

        public @NotNull Builder actorIds(@NotNull List<UUID> actorIds) {
            this.actorIds = actorIds;
            return this;
        }

        public @NotNull Builder actors(@NotNull UUID... actors) {
            this.actorIds = List.of(actors);
            return this;
        }

        public @NotNull Builder timestamp(@NotNull Instant timestamp) {
            this.timestamp = Objects.requireNonNull(timestamp);
            return this;
        }

        public @NotNull Builder eventId(@NotNull String eventId) {
            this.eventId = Objects.requireNonNull(eventId);
            return this;
        }

        public @NotNull GenericHistoryEvent build() {
            return new GenericHistoryEvent(
                    nation,
                    subject,
                    eventType,
                    payload,
                    actorIds,
                    timestamp,
                    eventId
            );
        }
    }

    // -------------------------------------------------------------------------
    // Bukkit boilerplate
    // -------------------------------------------------------------------------

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
