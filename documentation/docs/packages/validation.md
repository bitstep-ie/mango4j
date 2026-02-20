# Validation

## Overview
Jakarta validation helpers and reusable constraints.

## Architecture
- Constraint annotations: `@Type4UUID`, `@StrictType4UUID`, `@IsValidKebabCase`, `@IsValidDottedCase`.
- Validators: `KebabCaseValidator`, `DottedCaseValidator`, `IdentifierValidator`.
- `ValidationUtils` for programmatic validation and exception handling.

## How to use
### Gradle
```gradle
implementation("ie.bitstep.mango:mango4j-validation:{{ mango4j_latest_version }}")
```

### Maven
```xml
<dependency>
    <groupId>ie.bitstep.mango</groupId>
    <artifactId>mango4j-validation</artifactId>
    <version>{{ mango4j_latest_version }}</version>
</dependency>
```

## Examples

```java
class CreateRequest {
    @Type4UUID
    private String requestId;

    @IsValidKebabCase
    private String slug;
}

ValidationUtils.validate(request);
```
