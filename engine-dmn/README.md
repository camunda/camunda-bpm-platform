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

## Standalone Usage

Add the following Maven Coordinates to your project:

```xml
<dependency>
  <groupId>org.camunda.bpm.dmn</groupId>
  <artifactId>camunda-engine-dmn</artifactId>
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

## Usage with DMN as BPMN Business Rule Task

Add the following Maven Coordinates to your project:
```xml
<dependency>
  <groupId>org.camunda.bpm</groupId>
  <artifactId>camunda-engine</artifactId>
  <version>${version.camunda}</versions>
</dependency>
<dependency>
  <groupId>com.h2database</groupId>
  <artifactId>h2</artifactId>
  <version>1.3.168</version>
  <scope>test</scope>
</dependency>
```

Next, we reference a DMN decision from a BPMN Business Rule Task:

```xml
<bpmn:businessRuleTask id="assignApprover"
  camunda:decisionRef="invoice-assign-approver"
  camunda:resultVariable="approverGroups"
  name="Assign Approver Group(s)">
</bpmn:businessRuleTask>
```

And start the BPMN process inside our application:

```java
public class App {

  public static void main(String[] args) {

    ProcessEngine processEngine = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration()
      .buildProcessEngine();

    try {
      processEngine.getRepositoryService()
        .createDeployment()
        .name("invoice deployment")
        .addClasspathResource("invoice.bpmn")
        .addClasspathResource("assign-approver-groups.dmn")
        .deploy();

      processEngine.getRuntimeService()
        .startProcessInstanceByKey("invoice", createVariables()
            .putValue("invoceNumber", "2323"));
    }
    finally {
      processEngine.close();
    }
  }
}
```

