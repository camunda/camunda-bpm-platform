package org.camunda.bpm.engine.test.api.multitenancy.tenantcheck;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.Arrays;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
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
public class MultiTenancyActivityCmdsTenantCheckTest {

  protected static final String TENANT_ONE = "tenant1";

  protected static final String PROCESS_DEFINITION_KEY = "oneTaskProcess";

  protected static final BpmnModelInstance ONE_TASK_PROCESS = Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
    .startEvent()
    .userTask()
    .endEvent()
    .done();

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  protected String processInstanceId;

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Rule
  public ExpectedException thrown= ExpectedException.none();

  @Before
  public void init() {
    
    testRule.deployForTenant(TENANT_ONE, ONE_TASK_PROCESS);
    
    processInstanceId = engineRule.getRuntimeService()
      .startProcessInstanceByKey(PROCESS_DEFINITION_KEY)
      .getId();
  }

  @Test
  public void getActivityInstanceWithAuthenticatedTenant() {

    engineRule.getIdentityService().setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    
    // then
    assertNotNull(engineRule.getRuntimeService().getActivityInstance(processInstanceId));
  }

  @Test
  public void getActivityInstanceWithNoAuthenticatedTenant() {

    engineRule.getIdentityService().setAuthentication("aUserId", null);

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot read the process instance '"
      + processInstanceId +"' because it belongs to no authenticated tenant.");
    engineRule.getRuntimeService().getActivityInstance(processInstanceId);
    
  }

  @Test
  public void getActivityInstanceWithDisabledTenantCheck() {

    engineRule.getIdentityService().setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    
    // then
    assertNotNull(engineRule.getRuntimeService().getActivityInstance(processInstanceId));
  }

  // get active activity id
  @Test
  public void getActivityIdsWithAuthenticatedTenant() {

    engineRule.getIdentityService().setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));

    // then
    assertEquals(1, engineRule.getRuntimeService().getActiveActivityIds(processInstanceId).size());

  }

  @Test
  public void getActivityIdsWithNoAuthenticatedTenant() {

    engineRule.getIdentityService().setAuthentication("aUserId", null);

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot read the process instance '"
      + processInstanceId +"' because it belongs to no authenticated tenant.");
    // when
    engineRule.getRuntimeService().getActiveActivityIds(processInstanceId);

  }

  @Test
  public void getActivityIdsWithDisabledTenantCheck() {

    engineRule.getIdentityService().setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    
    // then
    assertEquals(1, engineRule.getRuntimeService().getActiveActivityIds(processInstanceId).size());

  }
  
}
