package org.camunda.bpm.engine.impl.test;


import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;


@Deployment
public class TestHelperDeploymentTest {

  @Rule
  public final ProcessEngineRule processEngineRule = new ProcessEngineRule();

  @Test
  public void testDeploymentOnClassLevel() {
    assertNotNull("process is not deployed",processEngineRule.getRepositoryService().createProcessDefinitionQuery().processDefinitionKey("testHelperDeploymentTest").singleResult());
  }

  @Test
  @Deployment
  public void testDeploymentOnMethodOverridesClass() {
    assertNotNull("process is not deployed",processEngineRule.getRepositoryService().createProcessDefinitionQuery().processDefinitionKey("testHelperDeploymentTestOverride").singleResult());
  }

}