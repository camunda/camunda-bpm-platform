package org.camunda.bpm.engine.test.bpmn.servicetask;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.bpmn.servicetask.util.OkReturningService;

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