package org.camunda.bpm.engine.delegate;

import java.util.Map;

public class FeelContextDelegate implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    Map<String, Object> context = (Map<String, Object>) execution.getVariable("context");
    Map<String, Object> nestedMap = (Map<String, Object>) context.get("innerContext");

    String content = (String) nestedMap.get("content");
    execution.setVariable("result", content);
  }
}
