# FEEL Engine Factory

Provide an integration of the FEEL engine for Camunda BPM using the SPI of the [Camunda DMN engine](https://github.com/camunda/camunda-engine-dmn). 

## How to use it?

Add the factory including the FEEL engine to your project by copying the jar file or adding the project as dependency.

```xml
<dependency>
  <groupId>org.camunda.bpm.extension.feel.scala</groupId>
  <artifactId>feel-engine-factory</artifactId>
  <version>1.0.0</version>
</dependency>
```

Then, replace the default FEEL engine factory in your DMN engine configuration.

### DMN Engine Configuration

```java
DefaultDmnEngineConfiguration dmnEngineConfig = (DefaultDmnEngineConfiguration) DmnEngineConfiguration.createDefaultDmnEngineConfiguration(); 
dmnEngineConfig.setFeelEngineFactory(new CamundaFeelEngineFactory());
// more configs ...
DmnEngine engine = dmnEngineConfig.buildEngine();
```

### Process Engine Spring XML Configuration

```xml
<bean id="processEngineConfiguration" class="org.camunda.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration">
  
  <property name="dmnEngineConfiguration">
    <bean class="org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration">
      <property name="feelEngineFactory">
        <bean class="org.camunda.feel.integration.CamundaFeelEngineFactory" />
      </property>
    </bean>
  </property>  
  
  <!-- more configs -->
</bean>
```

## How to build it?

You can build the project with [SBT](http://www.scala-sbt.org) or [Maven](http://maven.apache.org).

### Using SBT

Run the tests with
```
sbt test
```

Build the jar including all dependencies with
```
sbt assemply
```

### Using Maven

Run the tests with
```
mvn test
```

Build the jar including all dependencies with
```
mvn install
```
