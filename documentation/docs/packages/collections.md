# Collections

## Overview
Common collections utilities for map/list construction, map operations, reconciliation, and caching.

## Architecture
- Builders: `MapBuilder` and `ListBuilder` for fluent construction.
- Utilities: `MapUtils` for merge/replace/copy and path creation.
- Reconciliation: `CollectionReconciler` for current vs desired collections.
- Caching: `ConcurrentCache` with TTL and eviction support.

## Quick start
### Gradle
```gradle
implementation("ie.bitstep.mango:mango4j-collections:{{ env.MANGO4J_LATEST_VERSION or config.extra.mango4j_default_version }}")
```

### Maven
```xml
<dependency>
    <groupId>ie.bitstep.mango</groupId>
    <artifactId>mango4j-collections</artifactId>
    <version>{{ env.MANGO4J_LATEST_VERSION or config.extra.mango4j_default_version }}</version>
</dependency>
```

## Examples

All examples use classes from `ie.bitstep.mango.collections`.

### MapBuilder
Fluent builder for nested maps.

#### Build a map
```java
Map<String, String> map = MapBuilder.<String, String>map().build();
```

#### Build a map with specific implementation
```java
Map<String, String> map = MapBuilder.<String, String>map(new TreeMap<>()).build();
```

#### Build a map and add items (nested)
```java
Map<String, Object> map = MapBuilder.<String, Object>map()
    .with("first", "Tom")
    .with("last", "Cruise")
    .with("address",
        MapBuilder.<String, String>map()
            .with("line1", "One South County")
            .with("line2", "Leopardstown")
            .build()
    )
    .build();
```

### ListBuilder
Fluent builder for lists, with optional concrete list implementations.

#### Build a list
```java
List<String> list = ListBuilder.<String>list().build();
```

#### Build a list with specific implementation
```java
List<String> list = ListBuilder.<String>list(new LinkedList<>()).build();
```

#### Build a list and add items
```java
List<String> list = ListBuilder.<String>list()
    .add("Tom")
    .add("Cruise")
    .build();
```

#### Build a list from an existing collection
```java
List<String> words = Arrays.asList("The", "cow", "jumped", "over", "the", "moon");
List<String> list = ListBuilder.<String>list().add(words).build();
```

### MapUtils
Utilities for merging, replacing, copying, and list-wrapping map values.

#### Wrap values in lists
```java
Map<String, String> input = MapBuilder.<String, String>map()
    .with("name", "java")
    .build();

Map<String, List<String>> output = MapUtils.entriesToList(input);
```


### CollectionReconciler
Reconciles a current collection against a desired collection using a key extractor.

```java
List<User> current = new ArrayList<>(List.of(new User("u1"), new User("u2")));
List<User> desired = List.of(new User("u2"), new User("u3"));

CollectionReconciler.reconcile(current, desired, User::id);
```

### ConcurrentCache
TTL-based cache with eviction and optional `AutoCloseable` cleanup.

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
