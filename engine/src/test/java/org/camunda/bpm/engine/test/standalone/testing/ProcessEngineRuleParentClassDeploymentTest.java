package org.camunda.bpm.engine.test.standalone.testing;


import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;


public class ProcessEngineRuleParentClassDeploymentTest extends ProcessEngineRuleParentClassDeployment  {

  @Rule
  public final ProcessEngineRule processEngineRule = new ProcessEngineRule(true);

  @Test
  public void testDeploymentOnParentClassLevel() {
    assertNotNull("process is not deployed",processEngineRule.getRepositoryService().createProcessDefinitionQuery().processDefinitionKey("testHelperDeploymentTest").singleResult());
  }


}
