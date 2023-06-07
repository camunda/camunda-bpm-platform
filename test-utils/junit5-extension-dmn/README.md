# Camunda Platform DMN JUnit 5

JUnit 5 extension that allows you to inject a DMN engine into your test.

## Usage

### Maven dependency
Add the dependency to your pom.xml

```xml
    <dependency>
      <groupId>org.camunda.bpm.dmn</groupId>
      <artifactId>camunda-dmn-junit5</artifactId>
      <version>7.20.0</version>
      <scope>test</scope>
    </dependency>
```

### Test code
Add the annotation to your test class:

```java
    @ExtendWith(DmnEngineExtension.class)
```

For further access provide a field where the DMN engine gets injected:

```java
    public DmnEngine dmnEngine; 
```

Or specify it as a test method parameter, whichever better suits your needs.

```java
    @Test
    void testDecision(DmnEngine dmnEngine) {
    }
```
 