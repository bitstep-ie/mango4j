# mango4j-validation

[Back to root README](../readme.md)

Jakarta validation helpers and reusable constraints.

## Architecture
- Constraint annotations: `@Type4UUID`, `@StrictType4UUID`, `@IsValidKebabCase`, `@IsValidDottedCase`.
- Validators: `KebabCaseValidator`, `DottedCaseValidator`, and `IdentifierValidator` utilities.
- `ValidationUtils` wraps `jakarta.validation` to throw `ValidationUtilsException` on violations.

## Functionality
- Validate lower-case UUID strings, with strict v4 support.
- Enforce kebab-case or dotted-case identifier rules.
- Programmatic bean validation with a single helper call.

## Usage
### Maven
```xml
<dependency>
    <groupId>ie.bitstep.mango</groupId>
    <artifactId>mango4j-validation</artifactId>
    <version>VERSION</version>
</dependency>
```

### Gradle
```gradle
implementation("ie.bitstep.mango:mango4j-validation:VERSION")
```

## Examples
### Bean validation
```java
class CreateRequest {
    @Type4UUID
    private String requestId;

    @IsValidKebabCase
    private String slug;
}

ValidationUtils.validate(request);
```

### IdentifierValidator utility
```java
boolean ok = IdentifierValidator.isValidDottedCase("service.v1.endpoint");
```
