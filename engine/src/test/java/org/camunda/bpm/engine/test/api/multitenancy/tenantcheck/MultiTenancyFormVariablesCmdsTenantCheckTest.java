package org.camunda.bpm.engine.test.api.multitenancy.tenantcheck;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

/**
 * 
 * @author Deivarayan Azhagappan
 *
 */

public class MultiTenancyFormVariablesCmdsTenantCheckTest {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  protected static final String PROCESS_DEFINITION_KEY = "testProcess";

  protected static final String VARIABLE_1 = "testVariable1";
  protected static final String VARIABLE_2 = "testVariable2";
  
  protected static final String VARIABLE_VALUE_1 = "test1";
  protected static final String VARIABLE_VALUE_2 = "test2";
  
  protected static final BpmnModelInstance ONE_TASK_PROCESS = Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
      .startEvent()
      .userTask()
      .endEvent()
      .done();

  protected ProcessEngineRule engineRule = new ProcessEngineRule(true);

  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  protected static String processInstanceId;

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected static ProcessInstance instance;

  @Rule
  public ExpectedException thrown= ExpectedException.none();

  protected static final String START_FORM_RESOURCE = "org/camunda/bpm/engine/test/api/form/FormServiceTest.startFormFields.bpmn20.xml";

  
  @Before
  public void init() {
    
    // deploy tenants
    testRule.deployForTenant(TENANT_ONE, START_FORM_RESOURCE);
    instance = engineRule.getRuntimeService()
    .startProcessInstanceByKey(PROCESS_DEFINITION_KEY, Variables.createVariables().putValue(VARIABLE_1, VARIABLE_VALUE_1)
         .putValue(VARIABLE_2, VARIABLE_VALUE_2));
  }

  // start form variables
  @Test
  public void testGetStartFormVariablesWithUserAndAuthenticatedTenant() {

    engineRule.getIdentityService().setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
 
    assertEquals(4, engineRule.getFormService().getStartFormVariables(instance.getProcessDefinitionId()).size());

  }

  @Test
  public void testGetStartFormVariablesWithUserAndNoAuthenticatedTenant() {

    engineRule.getIdentityService().setAuthentication("aUserId", null);

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot get the process definition because it belongs to no authenticated tenant:");
    engineRule.getFormService().getStartFormVariables(instance.getProcessDefinitionId()).size();

  }

  @Test
  public void testGetStartFormVariablesWithNoUserAndNoAuthenticatedTenant() {

    engineRule.getIdentityService().setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    
    assertEquals(4, engineRule.getFormService().getStartFormVariables(instance.getProcessDefinitionId()).size());

  }

  @Test
  public void testGetTaskFormVariablesWithUserAndAuthenticatedTenant() {

    engineRule.getIdentityService().setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    
    Task task = engineRule.getTaskService().createTaskQuery().singleResult();
    
    assertEquals(2, engineRule.getFormService().getTaskFormVariables(task.getId()).size());

  }

  @Test
  public void testGetTaskFormVariablesWithUserAndNoAuthenticatedTenant() {

    Task task = engineRule.getTaskService().createTaskQuery().singleResult();
    
    engineRule.getIdentityService().setAuthentication("aUserId", null);
    
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot read the task because it belongs to no authenticated tenant:");
    engineRule.getFormService().getTaskFormVariables(task.getId());

  }

  @Test
  public void testGetTaskFormVariablesWithUserAndDisabledTenantCheck() {

    Task task = engineRule.getTaskService().createTaskQuery().singleResult();
    
    engineRule.getIdentityService().setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    
    assertEquals(2, engineRule.getFormService().getTaskFormVariables(task.getId()).size());

  }

  protected Deployment createDeployment(final String name, final String... resources) {
    DeploymentBuilder builder = engineRule.getRepositoryService().createDeployment();
    for (String resource : resources) {
      builder.addClasspathResource(resource);
    }
    return builder.deploy();
  } 
}
