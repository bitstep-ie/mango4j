# Reflection

## Overview
Reflection helpers with cached metadata and property accessors.

## Architecture
- `ClassInfo` caches fields and methods for faster reflection.
- `PropertyAccessor` resolves getters/setters via conventions or annotations.
- `ReflectionUtils` offers convenient get/set operations and method lookups.

## How to use
### Gradle
```gradle
implementation("ie.bitstep.mango:mango4j-reflection:{{ env.MANGO4J_LATEST_VERSION or config.extra.mango4j_default_version }}")
```

### Maven
```xml
<dependency>
    <groupId>ie.bitstep.mango</groupId>
    <artifactId>mango4j-reflection</artifactId>
    <version>{{ env.MANGO4J_LATEST_VERSION or config.extra.mango4j_default_version }}</version>
</dependency>
```

## Examples


### Manipulate a property using a PropertyAccessor
```java
Profile profile = new Profile(...);
PropertyAccessor<String> accessor = new PropertyAccessor<>(Profile.class, "firstName");

accessor.set(profile, "Fred");
System.out.println(accessor.get(profile));
```

### Get a cached PropertyAccessor
```java
Profile profile = new Profile(...);
PropertyAccessor<String> accessor = ReflectionUtils
    .getClassInfo(Profile.class)
    .getPropertyAccessor("firstName");

accessor.set(profile, "Fred");
System.out.println(accessor.get(profile));
```

### Manipulate a property using ReflectionUtils
```java
Profile profile = new Profile(...);

ReflectionUtils.setField(profile, "firstName", "Fred");
System.out.println(ReflectionUtils.getField(profile, "firstName"));
```
