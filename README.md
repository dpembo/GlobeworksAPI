# GlobeworksAPI

Phase 1 shared event-bus / data-types plugin for Globeworks.

Install this jar on the server. Other plugins declare:

```yaml
depend: [GlobeworksAPI]
```

(or `softdepend`) so they share the same classloader copy of the API classes.

## Public types

### `NationRef`
Immutable record: `UUID id` + optional `String cachedName`.

```java
NationRef ref = NationRef.of(nationUuid, "Rome");
```

### `GenericHistoryEvent`
Generic cross-plugin notification event.

**Subject model (hybrid):**
- Nation events → set `nation`, leave `subject` null
- Non-nation events → leave `nation` null, set `subject` (`"player:<uuid>"`, `"town:<uuid>"`, `"global"`, …)
- Fully global → both null (or `subject = "global"`)

**Builder example (producer):**
```java
GenericHistoryEvent event = GenericHistoryEvent.builder("diplomacy.alliance_formed")
    .nation(NationRef.of(nationId, nationName))
    .put("otherNation", otherName)
    .actors(player1.getUniqueId(), player2.getUniqueId())
    .build();

Bukkit.getPluginManager().callEvent(event);
```

**Listener example (consumer):**
```java
@EventHandler
public void onHistory(GenericHistoryEvent event) {
    // Return in microseconds. Hand off real work:
    GenericHistoryEvent.handleAsync(thisPlugin, event, e -> {
        // JDBC / HTTP / file I/O here
    });
}
```

## Build

```bash
./gradlew build
# → build/libs/globeworks-api-1.0.0.jar
```

Requires JDK 25 toolchain (Paper 26.2). Gradle can be launched with an older JDK if the toolchain is available.
