package org.camunda.bpm.engine.test.api.multitenancy.tenantcheck;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
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
public class MultiTenancyProcessInstanceCmdsTenantCheckTest {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";
  
  protected static final String PROCESS_DEFINITION_KEY = "oneTaskProcess";

  protected ProcessEngineRule engineRule = new ProcessEngineRule(true);

  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  protected static String processInstanceId;

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Rule
  public ExpectedException thrown= ExpectedException.none();

  protected static final BpmnModelInstance ONE_TASK_PROCESS = Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
   .startEvent()
   .userTask("task")
   .endEvent()
   .done();

  @Before
  public void init() {
    // deploy tenants
    testRule.deployForTenant(TENANT_ONE, ONE_TASK_PROCESS);
    
    processInstanceId = engineRule.getRuntimeService()
      .startProcessInstanceByKey(PROCESS_DEFINITION_KEY)
      .getId();

    assertEquals(1, engineRule.getRuntimeService()
    .createProcessInstanceQuery()
    .processInstanceId(processInstanceId)
    .list()
    .size());    
  }

  @Test
  public void deleteProcessInstanceForUserAndAuthenticatedTenant() {

    engineRule.getIdentityService().setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
 
    engineRule.getRuntimeService().deleteProcessInstance(processInstanceId, null);

    assertEquals(0, engineRule.getRuntimeService()
      .createProcessInstanceQuery()
      .processInstanceId(processInstanceId)
      .list()
      .size());
  }

  @Test
  public void deleteProcessInstanceWithUserAndNoAuthenticatedTenant() {

    engineRule.getIdentityService().setAuthentication("aUserId", null);

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot delete the process instance because it belongs to no authenticated tenant:");
  
    // when
    engineRule.getRuntimeService().deleteProcessInstance(processInstanceId, null);
  }

  @Test
  public void deleteProcessInstanceForUserAndDisabledTenantCheck() {

    engineRule.getIdentityService().setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    
    //then
    engineRule.getRuntimeService().deleteProcessInstance(processInstanceId, null);

    assertEquals(0, engineRule.getRuntimeService()
      .createProcessInstanceQuery()
      .processInstanceId(processInstanceId)
      .list()
      .size());
  }
  
  // modify instances
  @Test
  public void modifyProcessInstanceWithUserAndAuthenticatedTenant() {

    assertNotNull(engineRule.getRuntimeService().getActivityInstance(processInstanceId));

    engineRule.getIdentityService().setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));

    // when
    engineRule.getRuntimeService()
    .createProcessInstanceModification(processInstanceId)
    .cancelAllForActivity("task")
    .execute();

    assertNull(engineRule.getRuntimeService().getActivityInstance(processInstanceId));
  }

  @Test
  public void modifyProcessInstanceWithUserAndNoAuthenticatedTenant() {

    engineRule.getIdentityService().setAuthentication("aUserId", null);

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot update the process instance because it belongs to no authenticated tenant:");
  
    // when
    engineRule.getRuntimeService()
    .createProcessInstanceModification(processInstanceId)
    .cancelAllForActivity("task")
    .execute();
  }

  @Test
  public void modifyProcessInstanceWithUserAndDisabledTenantCheck() {

    assertNotNull(engineRule.getRuntimeService().getActivityInstance(processInstanceId));

    engineRule.getIdentityService().setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);

    // when
    engineRule.getRuntimeService()
    .createProcessInstanceModification(processInstanceId)
    .cancelAllForActivity("task")
    .execute();

    assertNull(engineRule.getRuntimeService().getActivityInstance(processInstanceId));
  }
}
