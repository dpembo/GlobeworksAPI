package uk.globeworks.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/**
 * Lightweight, immutable reference to a nation.
 * <p>
 * The UUID is the source of truth (names can change).
 * {@code cachedName} is a snapshot taken at event-creation time so consumers
 * can render a log line without a live Towny lookup.
 */
public record NationRef(@NotNull UUID id, @Nullable String cachedName) {

    public NationRef {
        Objects.requireNonNull(id, "id must not be null");
    }

    /**
     * Convenience factory.
     */
    public static @NotNull NationRef of(@NotNull UUID id, @Nullable String cachedName) {
        return new NationRef(id, cachedName);
    }

    /**
     * Convenience factory when only the UUID is known.
     */
    public static @NotNull NationRef of(@NotNull UUID id) {
        return new NationRef(id, null);
    }
}
