## mango4j-collections
> Bitstep's common collections

Mango Collections provide useful algorithms and data structures library for Java applications.

* 
    **FluentHashMap** allows the creation of tree-structured style Maps.\
        Use as an alternative to parsing JSON into Maps.
        
    Instead of this 
        
    ~~~ java
    ObjectMapper om = ObjectMapper();
    String jsonString = "{\n" +
                            "  \"pg\":\n" +
                            "    {\n" +
                            "      \"credentials\": {\n" +
                            "        \"userid\": \"hello2\"\n" +
                            "      }\n" +
                            "    },\n" +
                            "  \"redis\":\n" +
                            "    {\n" +
                            "      \"credentials\": {\n" +
                            "        \"userid\": \"hello3\"\n" +
                            "      }\n" +
                            "    }\n" +
                            "}\n"; 
    Map m = om.readValue(jsonString, Map.class);
    ~~~
        
    Try this
        
    ~~~ java
    Map m = MapBuilder.<String, Object>map(new TreeMap()) // create top level map as TreeMap
        .with("pg",
            MapBuilder.<String, String>map()
                .with("credentials",
                    MapBuilder.<String, String>map()
                        .with("userid", "hello2")
                        .build()
                )
                .build()
        )
        .with("redis",
            MapBuilder.<String, String>map()
                .with("credentials",
                    MapBuilder.<String, String>map()
                        .with("userid", "hello3")
                        .builder()
                )
                .build()
        )
        .build();
    ~~~

*
    **ListBuilder**
    User instead of Arrays.asList(), returns a mutable ArrayList<> by default, also take an instance to modify
    
    ~~~ java
        List<String> list = ListBuilder.<String>list(new LinkedList()).add("Hello").add("Dolly").build();

		assertTrue(list instanceof LinkedList); // NOSONAR
		assertEquals(2, list.size());
    ~~~
    
    ~~~ java
        // Overloaded add() takes array, and Collection<> types
		String[] a = {"The", "cow", "jumped", "over", "the", "moon"};
        List<String> list = ListBuilder.<String>list().add(a).build();

		assertTrue(list instanceof LinkedList); // NOSONAR
		assertEquals(6, list.size());
    ~~~
    
*
    **MapUtils**
Map<k, List<V>> elementsToList(Map<K, V> m);
e.g. Transform from Map<K, V> to Map<K, List<V\>>

    ~~~ java
		Map<String, String> input = MapBuilder.<String, String>map().with("name", "java").build();
		Map<String, List<String>> output = MapUtils.<String, String>map().elementsToList(input);
    ~~~
