#Getting started using Spin

Spin can be used in any Java-based application by adding the following maven dependency to your
`pom.xml` file:

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.camunda.spin</groupId>
      <artifactId>camunda-spin-bom</artifactId>
      <scope>import</scope>
      <type>pom</type>
      <version>${version.spin}</version>
    </dependency>
  </dependencies>
</dependencyManagement>
```

```xml
<dependencies>
  <dependency>
    <groupId>org.camunda.spin</groupId>
    <artifactId>camunda-spin-core</artifactId>
  </dependency>

  <dependency>
    <groupId>org.camunda.spin</groupId>
    <artifactId>camunda-spin-dataformat-all</artifactId>
  </dependency>
</dependencies>
```


camunda Spin is published to [maven central][1].

[1]: http://search.maven.org/#search%7Cga%7C1%7Ccamunda

