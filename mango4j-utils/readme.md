# mango4j-utils

[Back to root README](../readme.md)

Bitstep general-purpose utilities used across mango4j modules.

## Architecture
- Conformance and mutation: `ObjectMutator`, `Conformer`, and annotations (`@Reduce`, `@Tolerate`, `@Text`) enforce field-level rules and transformations.
- Masking: `Masker` hierarchy (`PanMasker`, `IdMasker`, `AccountIdMasker`, list maskers) plus `MaskerFactory` and `MaskingUtils`.
- Date/time: `DateUtils`, `CalendarUtils`, `Proximity`, `MovingClock`.
- Mapping and conversion: `MappingUtils`, `Converter`/`ConverterFactory`, and `CollectionToSize`.
- URL and strings: `URLGenerator`, `QueryParam`, and `MapFormat` for templated formatting.
- Threading and entities: `NamedScheduledExecutorBuilder`, `EntityToStringBuilder`.
- Proxy resolution: `ProxyResolver` and `IdentityProxyResolver` for mutator integration.

## Functionality
- Enforce field-length and tolerance rules on objects via annotations.
- Mutate annotated fields (escape/unescape, custom mutators) with deep or shallow traversal.
- Mask sensitive values consistently, including PANs and IDs.
- Generate stable URLs with normalized paths and query parameters.
- Convert objects to maps/JSON via Jackson.
- Create time-ordered UUIDv7 values.
- Build named, configured `ScheduledExecutorService` instances.

## Usage
### Maven
```xml
<dependency>
    <groupId>ie.bitstep.mango</groupId>
    <artifactId>mango4j-utils</artifactId>
    <version>VERSION</version>
</dependency>
```

### Gradle
```gradle
implementation("ie.bitstep.mango:mango4j-utils:VERSION")
```

## Examples
### Conformance with @Tolerate and @Reduce
```java
class Category {
    @Tolerate(min = 3, max = 3)
    private String code;

    @Reduce(max = 5)
    private String label;
}

Category category = new Category();
category.setCode("SQL Database");
category.setLabel("Payments Platform");

Conformer.conform(category);
// code becomes "SQL", label becomes "Payme" (with ellipsis if configured).
```

### ObjectMutator with HTML escaping
```java
class Message {
    @Text
    private String body;
}

ObjectMutator mutator = new ObjectMutator()
    .on(Text.class, new HtmlEscapeMutator());

mutator.mutate(message);
```

### Masking utilities
```java
String maskedPan = MaskerFactory.getMasker(PanMasker.class).mask("5105105105105100");
String maskedId = new IdMasker("Y").mask("01234567890ABCDEF");
```

### URLGenerator
```java
String url = URLGenerator.ofURL("http://api.stage.bitstep.ie//mdes/")
    .path("consumer")
    .path("allocate")
    .param("limit", "100")
    .toString();
```

### MappingUtils
```java
Map<String, Object> payload = MappingUtils.fromObjectToMap(somePojo);
String json = MappingUtils.fromObjectToJson(somePojo);
```

### UUIDv7
```java
UUID id = new UUIDv7().generate();
```

### NamedScheduledExecutorBuilder
```java
ScheduledExecutorService executor = NamedScheduledExecutorBuilder.builder()
    .poolSize(4)
    .threadNamePrefix("crypto-retry")
    .daemon(true)
    .build();
```
