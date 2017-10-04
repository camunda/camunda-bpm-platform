# FEEL Engine Factory

Provide an integration of the FEEL engine for Camunda BPM using the SPI of the [Camunda DMN engine](https://github.com/camunda/camunda-engine-dmn). It can be used for the standalone or the embedded DMN engine.

## How to use it?

Add the factory including the FEEL engine to your project by copying the [jar file](https://github.com/camunda/feel-scala/releases) _(feel-engine-factory-${VERSION}-complete.jar)_ or adding the project as dependency.

```xml
<dependency>
  <groupId>org.camunda.bpm.extension.feel.scala</groupId>
  <artifactId>feel-engine-factory</artifactId>
  <version>${VERSION}</version>
</dependency>
```

Then, replace the default FEEL engine factory in your [DMN engine configuration](https://docs.camunda.org/manual/latest/user-guide/dmn-engine/embed/#customize-expression-and-script-resolving).

### Using the DMN Engine Configuration

```java
DefaultDmnEngineConfiguration dmnEngineConfig = (DefaultDmnEngineConfiguration) DmnEngineConfiguration.createDefaultDmnEngineConfiguration(); 
dmnEngineConfig.setFeelEngineFactory(new CamundaFeelEngineFactory());
// more configs ...
DmnEngine engine = dmnEngineConfig.buildEngine();
```

### Using the Process Engine Spring XML Configuration

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

### Configure the default Expression Languages

> Using Camunda BPM >= 7.7.0-alpha1

If you want to use FEEL for output entries, input expressions or decision literal expressions then you have to change the default expression languages in the configuration. See the [user guide](https://docs.camunda.org/manual/latest/user-guide/dmn-engine/embed/#change-default-expression-languages) for details.

```java
configuration.setDefaultOutputEntryExpressionLanguage("feel");
```

or

```xml
  <property name="dmnEngineConfiguration">
    <bean class="org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration">
      <property name="defaultOutputEntryExpressionLanguage" value="feel" />
      <!-- replace factory and other configs --> 
    </bean>
  </property>  
```

> Using Camunda BPM < 7.7.0

You have to set the default expression language to `feel-scala` instead of `feel`. This uses the FEEL engine as script engine to avoid a previous bug. 

## How to build it?

You can build the project with [SBT](http://www.scala-sbt.org) or [Maven](http://maven.apache.org).

### Using SBT

In the root directory:

Run the tests with
```
sbt factory/test
```

Build the jar including all dependencies with
```
sbt factory/assemply
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
