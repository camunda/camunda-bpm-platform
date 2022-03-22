# Camunda Platform JUnit 5

JUnit 5 extension that allows you to inject a process engine into your test.

## Usage

### Maven dependency
Add the dependency to your pom.xml

```xml
    <dependency>
      <groupId>org.camunda.bpm</groupId>
      <artifactId>camunda-bpm-junit5</artifactId>
      <version>7.17.0</version>
      <scope>test</scope>
    </dependency>
```

### Test code
Add the annotation to your test class:

```java
    @ExtendWith(ProcessEngineExtension.class)
```

For further access provide a field where the process engine gets injected:

```java
    public ProcessEngine processEngine; 
```

Or register the extension from the builder:

```java
    @RegisterExtension
    ProcessEngineExtension extension = ProcessEngineExtension.builder()
      .configurationResource("audithistory.camunda.cfg.xml")
      .build();
```

and access the process engine from the extension object:

```java
    RuntimeService runtimeService = extension.getProcessEngine().getRuntimeService(); 
```

If you don't want to create a configuration file, you can add a process engine, that you configure programmatically:

```java
    public ProcessEngine myProcessEngine = ProcessEngineConfiguration
        .createStandaloneInMemProcessEngineConfiguration()
        .setJdbcUrl("jdbc:h2:mem:camunda;DB_CLOSE_DELAY=1000")
        .buildProcessEngine();
    
    @RegisterExtension
    ProcessEngineExtension extension = ProcessEngineExtension
        .builder()
        .useProcessEngine(myProcessEngine)
        .build();
```
