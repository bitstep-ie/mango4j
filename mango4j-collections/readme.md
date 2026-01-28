# mango4j-collections

[Back to root README](../readme.md)

Bitstep collection helpers for fluent construction, map operations, reconciliation, and caching.

## Architecture
- Builders: `MapBuilder` and `ListBuilder` create collections fluently and let you supply concrete implementations.
- Map utilities: `MapUtils` and `MapUtilsInternal` merge, replace, copy, and create nested paths.
- Reconciliation: `CollectionReconciler` reconciles a current collection with a desired collection by key.
- Caching: `ConcurrentCache` provides TTL-based caching with a "current" entry and scheduled eviction.

## Functionality
- Build nested maps and lists without verbose initialization.
- Merge, replace, and copy maps with left-to-right semantics.
- Create or navigate nested map paths safely.
- Reconcile collections while preserving desired order.
- Cache values with TTL, grace periods, and automatic `AutoCloseable` cleanup.

## Usage
### Maven
```xml
<dependency>
    <groupId>ie.bitstep.mango</groupId>
    <artifactId>mango4j-collections</artifactId>
    <version>VERSION</version>
</dependency>
```

### Gradle
```gradle
implementation("ie.bitstep.mango:mango4j-collections:VERSION")
```

## Examples
### Fluent map construction
```java
MapBuilder<String, Object> builder = MapBuilder.map();
builder.with("service", "payments");
builder.withPath("config", "db")
    .with("host", "localhost")
    .with("port", 5432);

Map<String, Object> map = builder.build();
```

### ListBuilder with a concrete type
```java
List<String> names = ListBuilder.<String>list(new LinkedList<>())
    .add("Ada")
    .add("Linus")
    .build();
```

### Reconcile collections by key
```java
List<User> current = new ArrayList<>(List.of(new User("u1"), new User("u2")));
List<User> desired = List.of(new User("u2"), new User("u3"));

CollectionReconciler.reconcile(
    current,
    desired,
    User::id
);
// current now contains u2 (existing instance) and u3 (new), in desired order.
```

### ConcurrentCache with TTL
```java
ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();
ConcurrentCache<String, String> cache = new ConcurrentCache<>(
    Duration.ofMinutes(10),
    Duration.ofMinutes(10),
    Duration.ofSeconds(5),
    Duration.ofMinutes(1),
    cleaner,
    Clock.systemUTC()
);

cache.put("token", "abc123");
String value = cache.get("token");
```
