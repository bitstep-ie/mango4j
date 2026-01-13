# mango4j-collections - Examples

<!--name=admonition;type=info;title=Note;body=All the below are available in ie.bitstep.mango.collections -->

## MapBuilder

### Build a map

```java language=java
Map<String, String> map = MapBuilder.<String, String>map().build();
```

### Build a map with specific implementation

```java language=java
Map<String, String> map = MapBuilder.<String, String>map(new TreeMap<>()).build();
```

### Build a map and add items (nested)

```java language=java
Map m = MapBuilder.<String, Object>map()
		.with("first", "Tom")
		.with("last", "Cruise")
		.with("address",
				MapBuilder.<String, String>map()
						.with("line1", "One South County")
						.with("line2", "Leopardstown")
						.build()
		).build();
```

## ListBuilder

### Build a list

```java language=java
List<String> list = ListBuilder.<String>list().build();
```

### Build a list with specific implementation

```java language=java
List<String> list = ListBuilder.<String>list(new LinkedList<String>()).build();
```

### Build a list and add items

```java language=java
List l = ListBuilder.<String>list()
		.add("Tom")
		.add("Cruise")
		.build();
```

### Build a list with a collection

```java language=java
List<String> a = Arrays.asList("The", "cow", "jumped", "over", "the", "moon");
List<String> list = ListBuilder.<String>list().add(a).build();
```

## Map Utils

> TODO — add docs around the map utils methods
