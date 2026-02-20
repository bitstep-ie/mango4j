# Hibernate Proxy Resolver

## Overview
Hibernate proxy resolver used with `ObjectMutator` to unwrap lazy proxies.

## How to use
### Gradle
```gradle
implementation("ie.bitstep.mango:mango4j-hibernate-proxy-resolver:{{ mango4j_latest_version }}")
```

### Maven
```xml
<dependency>
    <groupId>ie.bitstep.mango</groupId>
    <artifactId>mango4j-hibernate-proxy-resolver</artifactId>
    <version>{{ mango4j_latest_version }}</version>
</dependency>
```

## Examples

```java
ObjectMutator mutator = new ObjectMutator(new HibernateProxyResolver())
    .on(Text.class, new HtmlEscapeMutator());

mutator.mutate(entity);
```
