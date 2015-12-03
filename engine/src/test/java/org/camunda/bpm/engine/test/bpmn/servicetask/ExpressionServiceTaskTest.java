package org.camunda.bpm.engine.test.bpmn.servicetask;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.bpmn.servicetask.util.ValueBean;

/**
 * @author Christian Stettler
 */
public class ExpressionServiceTaskTest extends PluggableProcessEngineTestCase {

  @Deployment
  public void testSetServiceResultToProcessVariables() {
    Map<String,Object> variables = new HashMap<String, Object>();
    variables.put("bean", new ValueBean("ok"));
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("setServiceResultToProcessVariables", variables);
    assertEquals("ok", runtimeService.getVariable(pi.getId(), "result"));
  }

  @Deployment
  public void testBackwardsCompatibleExpression() {
    Map<String,Object> variables = new HashMap<String, Object>();
    variables.put("var", "---");
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("BackwardsCompatibleExpressionProcess", variables);
    assertEquals("...---...", runtimeService.getVariable(pi.getId(), "result"));
  }
}
