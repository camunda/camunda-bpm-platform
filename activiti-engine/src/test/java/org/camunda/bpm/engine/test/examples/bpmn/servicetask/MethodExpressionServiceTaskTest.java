package org.camunda.bpm.engine.test.examples.bpmn.servicetask;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Christian Stettler
 */
public class MethodExpressionServiceTaskTest extends PluggableProcessEngineTestCase {

  @Deployment
  public void testSetServiceResultToProcessVariables() {
    Map<String,Object> variables = new HashMap<String, Object>();
    variables.put("okReturningService", new OkReturningService());

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("setServiceResultToProcessVariables", variables);

    assertEquals("ok", runtimeService.getVariable(pi.getId(), "result"));
  }

}