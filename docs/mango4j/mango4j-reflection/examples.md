# mango4j-reflection - Examples

### Manipulate a property using a PropertyAccessor

```java language=java
// create a profile instance
Profile p = new Profile(...);

// Create a property accessor for the "firstName" field in the Profile class
PropertyAccessor<String> pa = new PropertyAccessor<>(Profile.class, "firstName");

// Change the value of the field specified by the property accessor
pa.

set(p, "Fred");

// print the value of the field specified by the property accessor
System.out.

println(pa.get(p));
```

### Get a cached PropertyAccessor

```java language=java
// create a profile instance
Profile p = new Profile(...);

PropertyAccessor<String> pa = ReflectionUtils.getClassInfo(Profile.class).getPropertyAccessor("firstName");

// Change the value of the field specified by the property accessor
pa.

set(p, "Fred");

// print the value of the field specified by the property accessor
System.out.

println(pa.get(p));
```

### Manipulate a property using the ReflectionUtils class

```java language=java
// create a profile instance
Profile p = new Profile(...);

		ReflectionUtils.

setField(p, "firstName","Fred");

// print the value of the field specified by the property accessor
System.out.

println(ReflectionUtils.getField(p, "firstName"));
```
