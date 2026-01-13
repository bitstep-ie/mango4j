## mango4j-utils
> Bitstep's common utilities

Generally useful classes for Java applications.

### How to use?
#### Maven
It can be used by projects by adding the following dependency to the POM file
~~~xml
    <dependency>
        <groupId>ie.bitstep.mango</groupId>
        <artifactId>mango4j-utils</artifactId>
        <version>${mango4j-utils.version}</version>
    </dependency>
~~~
#### Gradle
TBU

### Functionality
#### Dates

#### Masking

    ~~~java
        new IdMasker("Y").mask("01234567890ABCDEF")).isEqualTo("01234YYYYYYYYYYYY")
    ~~~

#### Object conformance

* Conformer

  The conformer classes goal is to make an object conform to requirements.
  To make an object conformant you call one of the Conformer classes conform() methods on a suitably annotated class.
  eg:
    ~~~java
      Conformer.conform(object);
      // or
      Conformer.deepConform(object);
    ~~~
  conformer() will only make direct members compliant of the object, whereas deepConformer() will recurse into sub-objects and make them compliant also.

* Annotations
    * @Reduce(max, ellipsis = true)

      Reduce a string to the maximum length, adding ellipsis if requested (defaults to true)

    * @Tolerate(min, max)

      Make an object conform to the tolerance values.

      Applies to String and numeric types only. For Strings min & max are lengths.

* Examples

  Annotated class
    ~~~java
        package ie.bitstep.mango.utils.conformance;

        class Category {
            // Tolerate a value between 3 and 3 characters
            @Tolerate(min = 3, max = 3)
            private String category;

            public Category(
                String category) {
                this.category = category;
            }

            public Category(Category t) {
                this.category = t.category;
            }

            public String getCategory() {
                return category;
            }
        }
    ~~~

  Application class
    ~~~java
        package ie.bitstep.application;

        import ie.bitstep.mango.utils.conformance.Conformer;

        class Application {
              Category ct = new Category("SQL Database");
              Conformer.conform(ct);
              assertThat(ct.getCategory()).isEqualTo("SQL");
        }
    ~~~

#### ObjectMutator

~~~java
ObjectMutator objectMutator = new ObjectMutator()
        .on(Text.class, new HtmlUnescapeMutator())
        .on(Text.class, new HtmlEscapeMutator());

    objectMutator.mutate(this);
~~~

~~~java
// if using mutator on hibernate entities
ObjectMutator objectMutator = new ObjectMutator(new HibernateProxyResolver())
        .on(Text.class, new HtmlUnescapeMutator())
        .on(Text.class, new HtmlEscapeMutator());

    objectMutator.mutate(this);
~~~

#### URLs
* URLGenerator

  The URLGenerator class can be used to manage the url paths and help to
  ensure that there are no inconsistencies such as
  accidental duplicate slashes etc.

    ~~~java
    package ie.bitstep.mango.url.examples;

    import ie.bitstep.mango.utils.url.URLGenerator;

    public class Test {
        public String getAllocateURL() {
            URLGenerator urlGenerator = URLGenerator.ofURL("http://api.stage.bitstep.com//mdes/consumer//");
            urlGenerator.path("//allocate//");
            urlGenerator.param("validFrom", "11/22");
            urlGenerator.param("validTo", "02/23");
            urlGenerator.param("singleUse", "true");
            urlGenerator.param("merchantLocked", "true");
            urlGenerator.param("limit", "100");

            return urlGenerator.toString();
        }
    }
    ~~~
