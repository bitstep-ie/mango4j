# Hibernate Proxy Resolver

## Overview
Hibernate proxy resolver used with `ObjectMutator` to unwrap lazy proxies.

## How to use
### Gradle
```gradle
implementation("ie.bitstep.mango:mango4j-hibernate-proxy-resolver:{{ env.MANGO4J_LATEST_VERSION or config.extra.mango4j_default_version }}")
```

### Maven
```xml
<dependency>
    <groupId>ie.bitstep.mango</groupId>
    <artifactId>mango4j-hibernate-proxy-resolver</artifactId>
    <version>{{ env.MANGO4J_LATEST_VERSION or config.extra.mango4j_default_version }}</version>
</dependency>
```

## Examples

```java
ObjectMutator mutator = new ObjectMutator(new HibernateProxyResolver())
    .on(Text.class, new HtmlEscapeMutator());

mutator.mutate(entity);
```
