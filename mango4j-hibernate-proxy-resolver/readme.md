# mango4j-hibernate-proxy-resolver

[Back to root README](../readme.md)

Hibernate proxy resolver for `mango4j-utils` mutators.

## Architecture
- `HibernateProxyResolver` implements `ProxyResolver` and unwraps `HibernateProxy` instances before mutation.

## Usage
### Maven
```xml
<dependency>
    <groupId>ie.bitstep.mango</groupId>
    <artifactId>mango4j-hibernate-proxy-resolver</artifactId>
    <version>VERSION</version>
</dependency>
```

### Gradle
```gradle
implementation("ie.bitstep.mango:mango4j-hibernate-proxy-resolver:VERSION")
```

## Example
```java
ObjectMutator mutator = new ObjectMutator(new HibernateProxyResolver())
    .on(Text.class, new HtmlEscapeMutator());

mutator.mutate(entity);
```
