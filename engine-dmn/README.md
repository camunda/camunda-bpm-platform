Camunda DMN Engine
==================

Lightweight Execution Engine for DMN (Decision Model and Notation) written in Java.

<p>
  <a href="http://camunda.org/">Home</a> |
  <a href="http://camunda.org/community/forum.html">Forum</a> |
  <a href="https://app.camunda.com/jira/browse/CAM">Issues</a> |
  <a href="LICENSE">License</a>
</p>

The Decision Engine can be used seamlessly in combination with BPMN and CMMN or standalone.

## Usage with BPMN

## Standalone Usage

Add the following Maven Coordinates to your project:

```xml
<dependency>
  <groupId>org.camunda.bpm.dmn</groupId>
  <artifactId>camunda-scriptengine-dmn</artifactId>
  <version>${version.camunda}</versions>
</dependency>
```

Now you can use the DMN engine inside your Java Code:

```java
public class DmnApp {

  public static void main(String[] args) {

    // configure the engine
    DmnEngine dmnEngine = new DmnEngineConfigurationImpl().buildEngine();
    // parse a decision
    DmnDecision decision = dmnEngine.parseDecision("checkOrder.dmn");

    Map<String, Object> data = new HashMap<String, Object>();
    data.put("status", "gold");
    data.put("sum", 354.12d);

    // evaluate a decision
    DmnDecisionResult result = dmnEngine.evaluate(decision, data);
  }

}
```

