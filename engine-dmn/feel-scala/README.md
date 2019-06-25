# FEEL Engine Factory

An integration of the FEEL engine for Camunda BPM using the [Camunda DMN engine](https://github.com/camunda/camunda-engine-dmn) SPI for FEEL engines. It can be used for the standalone or the embedded DMN engine.

## Usage

1) Add the dependency to your project:

  ```xml
  <dependency>
    <groupId>org.camunda.bpm.extension.feel.scala</groupId>
    <artifactId>feel-engine-factory</artifactId>
    <version>${VERSION}</version>
  </dependency>
  ```

  Or copy the [jar file](https://github.com/camunda/feel-scala/releases) _(feel-engine-factory-${VERSION}-complete.jar)_ directly.

2) Replace the default FEEL engine factory in your [DMN engine configuration](https://docs.camunda.org/manual/latest/user-guide/dmn-engine/embed/#customize-expression-and-script-resolving).

  ### Via DMN Engine Configuration

  ```java
  DefaultDmnEngineConfiguration dmnEngineConfig = (DefaultDmnEngineConfiguration) DmnEngineConfiguration.createDefaultDmnEngineConfiguration(); 
  dmnEngineConfig.setFeelEngineFactory(new CamundaFeelEngineFactory());
  // more configs ...
  DmnEngine engine = dmnEngineConfig.buildEngine();
  ```

  ### Via Process Engine Spring XML Configuration

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
