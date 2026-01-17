# mango4j-reflection

[Back to root README](../readme.md)

Reflection helpers with cached metadata and property accessors.

## Architecture
- `ClassInfo` caches class fields, methods, and annotated method metadata.
- `PropertyAccessor` resolves getters/setters using conventions or explicit annotations (`@Accessor`, `@PropertyGetter`, `@PropertySetter`).
- `ReflectionUtils` provides cached lookups and convenience methods for field access.
- `MethodInfo` and `StringUtils` support method discovery and name formatting.

## Functionality
- Read and write fields by name or `Field` instance.
- Use cached metadata to avoid repeated reflection lookups.
- Resolve accessor methods from annotations or naming conventions.
- Identify core types (String, Number, Boolean, Enum).

## Usage
### Maven
```xml
<dependency>
    <groupId>ie.bitstep.mango</groupId>
    <artifactId>mango4j-reflection</artifactId>
    <version>VERSION</version>
</dependency>
```

### Gradle
```gradle
implementation("ie.bitstep.mango:mango4j-reflection:VERSION")
```

## Examples
### Read and write a field by name
```java
User user = new User();
ReflectionUtils.setField(user, "email", "person@example.com");
String email = (String) ReflectionUtils.getField(user, "email");
```

### Use custom accessors
```java
class User {
    @Accessor(getter = "emailValue", setter = "setEmailValue")
    private String email;

    public String emailValue() { return email; }
    public void setEmailValue(String email) { this.email = email; }
}

PropertyAccessor<User> accessor = new PropertyAccessor<>(User.class, "email");
accessor.get(user);
accessor.set(user, "person@example.com");
```
