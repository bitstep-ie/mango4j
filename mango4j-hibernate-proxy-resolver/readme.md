## mango4j-hibernate-proxy-resolver

> Bitstep's common utilities

### How to use?

When using the ObjectMutator in a project that has hibernate entities you need to depend on
mango4j-hibernate-proxy-resolver and create the ObjectMutator like so

~~~java
ObjectMutator(new HibernateProxyResolver());
~~~


#### Maven
It can
be used
by projects
by adding
the following
dependency to
the POM
file
~~~xml
	<dependency>
        <groupId>ie.bitstep.mango</groupId>
        <artifactId>mango4j-hibernate-proxy-resolver</artifactId>
        <version>${mango4j-utils.version}</version>
    </dependency>
~~~

#### Gradle

TBU

