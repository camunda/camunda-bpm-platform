package org.camunda.bpm.engine.test.api.multitenancy.tenantcheck;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

public class MultiTenancyExternalTaskCmdsTenantCheckTest {
  
  protected static final String TENANT_ONE = "tenant1";
  
  protected static final String PROCESS_DEFINITION_KEY = "twoExternalTaskProcess";
  private static final String ERROR_DETAILS = "anErrorDetail";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);
  
  protected static final String WORKER_ID = "aWorkerId";
  
  protected static final long LOCK_TIME = 10000L;
  
  protected static final String TOPIC_NAME = "externalTaskTopic";
  
  protected static final String ERROR_MESSAGE = "errorMessage";
  
  protected ExternalTaskService externalTaskService;
  
  protected TaskService taskService;

  protected String processInstanceId;

  protected IdentityService identityService;
  
  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);
  
  @Rule
  public ExpectedException thrown = ExpectedException.none();
  
  @Before
  public void init() {
    
    externalTaskService = engineRule.getExternalTaskService();
    
    taskService = engineRule.getTaskService();
    
    identityService = engineRule.getIdentityService();
    
    testRule.deployForTenant(TENANT_ONE,
      "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml");
    
    processInstanceId = engineRule.getRuntimeService().startProcessInstanceByKey(PROCESS_DEFINITION_KEY).getId();
    
  }

  // fetch and lock test cases
  @Test
  public void testFetchAndLockWithAuthenticatedTenant() {
    
    identityService.setAuthentication("aUserId", null,  Arrays.asList(TENANT_ONE));

    // then
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();
    assertEquals(1, externalTasks.size());
    
  }

  @Test
  public void testFetchAndLockWithNoAuthenticatedTenant() {
    
    identityService.setAuthentication("aUserId", null);

    // then external task cannot be fetched due to the absence of tenant Id authentication
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();
    assertEquals(0, externalTasks.size());
    
  }

  @Test
  public void testFetchAndLockWithDifferentTenant() {
    
    identityService.setAuthentication("aUserId", null, Arrays.asList("tenantTwo"));

    // then external task cannot be fetched due to the absence of 'tenant1' authentication
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();
    assertEquals(0, externalTasks.size());
    
  }

  @Test
  public void testFetchAndLockWithDisabledTenantCheck() {
    
    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    // then
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();
    assertEquals(1, externalTasks.size());
    
  }

  // complete external task test cases
  @Test
  public void testCompleteWithAuthenticatedTenant() {
    
    String externalTaskId = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0)
      .getId();

    assertEquals(1, externalTaskService.createExternalTaskQuery().active().count());
    
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    
    externalTaskService.complete(externalTaskId, WORKER_ID);
    
    assertThat(externalTaskService.createExternalTaskQuery().active().count(), is(0L));
    
  }
  
  @Test
  public void testCompleteWithNoAuthenticatedTenant() {
    
    String externalTaskId = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0)
      .getId();

    assertEquals(1, externalTaskService.createExternalTaskQuery().active().count());
    
    identityService.setAuthentication("aUserId", null);
    
    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot update the process instance '"
      + processInstanceId +"' because it belongs to no authenticated tenant.");
    externalTaskService.complete(externalTaskId, WORKER_ID);
    
  }
  
  @Test
  public void testCompleteWithDisableTenantCheck() {
    
    String externalTaskId = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0)
      .getId();

    assertEquals(1, externalTaskService.createExternalTaskQuery().active().count());
    
    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    
    externalTaskService.complete(externalTaskId, WORKER_ID);
    // then
    assertThat(externalTaskService.createExternalTaskQuery().active().count(), is(0L));
  }
  
  // handle failure test cases
  @Test
  public void testHandleFailureWithAuthenticatedTenant() {
    
    LockedExternalTask task = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0);
    
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    
    externalTaskService.handleFailure(task.getId(), WORKER_ID, ERROR_MESSAGE, 1, 0);
    
    // then
    assertEquals(ERROR_MESSAGE, externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0)
      .getErrorMessage());
    
  }
  
  @Test
  public void testHandleFailureWithNoAuthenticatedTenant() {
    
    LockedExternalTask task = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0);
    
    identityService.setAuthentication("aUserId", null);
    
    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot update the process instance '"
      + processInstanceId +"' because it belongs to no authenticated tenant.");
    externalTaskService.handleFailure(task.getId(), WORKER_ID, ERROR_MESSAGE, 1, 0);
  }
  
  @Test
  public void testHandleFailureWithDisabledTenantCheck() {
    
    String taskId = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0)
      .getId();
    
    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    
    externalTaskService.handleFailure(taskId, WORKER_ID, ERROR_MESSAGE, 1, 0);
    // then
    assertEquals(ERROR_MESSAGE, externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0)
      .getErrorMessage());
  }
  
  // handle BPMN error
  @Test
  public void testHandleBPMNErrorWithAuthenticatedTenant() {
    
    String taskId = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0)
      .getId();
    
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    
    // when
    externalTaskService.handleBpmnError(taskId, WORKER_ID, "ERROR-OCCURED");
    
    // then
    assertEquals(taskService.createTaskQuery().singleResult().getTaskDefinitionKey(), "afterBpmnError");
  }
  
  @Test
  public void testHandleBPMNErrorWithNoAuthenticatedTenant() {
    
    String taskId = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0)
      .getId();
    
    identityService.setAuthentication("aUserId", null);
    
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot update the process instance '"
      + processInstanceId +"' because it belongs to no authenticated tenant.");
    // when
    externalTaskService.handleBpmnError(taskId, WORKER_ID, "ERROR-OCCURED");
    
  }
  
  @Test
  public void testHandleBPMNErrorWithDisabledTenantCheck() {
    
    String taskId = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0)
      .getId();
    
    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    
    // when
    externalTaskService.handleBpmnError(taskId, WORKER_ID, "ERROR-OCCURED");
    
    // then
    assertEquals(taskService.createTaskQuery().singleResult().getTaskDefinitionKey(), "afterBpmnError");
    
  }
  
  // setRetries test
  @Test
  public void testSetRetriesWithAuthenticatedTenant() {
    // given
    String externalTaskId = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0)
      .getId();
    
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    
    // when
    externalTaskService.setRetries(externalTaskId, 5);
    
    // then
    assertEquals(5, (int) externalTaskService.createExternalTaskQuery().singleResult().getRetries());
  }
  
  @Test
  public void testSetRetriesWithNoAuthenticatedTenant() {
    // given
    String externalTaskId = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0)
      .getId();
    
    identityService.setAuthentication("aUserId", null);
    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot update the process instance '"
      + processInstanceId +"' because it belongs to no authenticated tenant.");
    // when
    externalTaskService.setRetries(externalTaskId, 5);
    
  }
  
  @Test
  public void testSetRetriesWithDisabledTenantCheck() {
    // given
    String externalTaskId = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0)
      .getId();
    
    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    
    // when
    externalTaskService.setRetries(externalTaskId, 5);
    
    // then
    assertEquals(5, (int) externalTaskService.createExternalTaskQuery().singleResult().getRetries());
    
  }
  
  // set priority test cases
  @Test
  public void testSetPriorityWithAuthenticatedTenant() {
    // given
    String externalTaskId = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0)
      .getId();
    
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    
    // when
    externalTaskService.setPriority(externalTaskId, 1);
    
    // then
    assertEquals(1, (int) externalTaskService.createExternalTaskQuery().singleResult().getPriority());
  }
  
  @Test
  public void testSetPriorityWithNoAuthenticatedTenant() {
    // given
    String externalTaskId = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0)
      .getId();
    
    identityService.setAuthentication("aUserId", null);
    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot update the process instance '"
      + processInstanceId +"' because it belongs to no authenticated tenant.");
    // when
    externalTaskService.setPriority(externalTaskId, 1);
    
  }
  
  @Test
  public void testSetPriorityWithDisabledTenantCheck() {
    // given
    String externalTaskId = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0)
      .getId();
    
    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    
    // when
    externalTaskService.setPriority(externalTaskId, 1);
    
    // then
    assertEquals(1, (int) externalTaskService.createExternalTaskQuery().singleResult().getPriority());
  }
  
  // unlock test cases
  @Test
  public void testUnlockWithAuthenticatedTenant() {
    // given
    String externalTaskId = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0)
      .getId();
    
    assertThat(externalTaskService.createExternalTaskQuery().locked().count(), is(1L));
    
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    
    // when
    externalTaskService.unlock(externalTaskId);
    
    // then
    assertThat(externalTaskService.createExternalTaskQuery().locked().count(), is(0L));
  }

  @Test
  public void testUnlockWithNoAuthenticatedTenant() {
    // given
    String externalTaskId = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0)
      .getId();
    
    identityService.setAuthentication("aUserId", null);
    
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot update the process instance '"
      + processInstanceId +"' because it belongs to no authenticated tenant.");
    // when
    externalTaskService.unlock(externalTaskId);
  }
  
  @Test
  public void testUnlockWithDisabledTenantCheck() {
    // given
    String externalTaskId = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0)
      .getId();
    
    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);

    externalTaskService.unlock(externalTaskId);
    // then
    assertThat(externalTaskService.createExternalTaskQuery().locked().count(), is(0L));
  }

  // get error details tests
  @Test
  public void testGetErrorDetailsWithAuthenticatedTenant() {
    // given
    String externalTaskId = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0)
      .getId();

    externalTaskService.handleFailure(externalTaskId,WORKER_ID,ERROR_MESSAGE,ERROR_DETAILS,1,1000L);

    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));

    // when then
    assertThat(externalTaskService.getExternalTaskErrorDetails(externalTaskId), is(ERROR_DETAILS));
  }

  @Test
  public void testGetErrorDetailsWithNoAuthenticatedTenant() {
    // given
    String externalTaskId = externalTaskService.fetchAndLock(5, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute()
        .get(0)
        .getId();

    externalTaskService.handleFailure(externalTaskId,WORKER_ID,ERROR_MESSAGE,ERROR_DETAILS,1,1000L);

    identityService.setAuthentication("aUserId", null);

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot read the process instance '"
      + processInstanceId +"' because it belongs to no authenticated tenant.");
    // when
    externalTaskService.getExternalTaskErrorDetails(externalTaskId);
  }

  @Test
  public void testGetErrorDetailsWithDisabledTenantCheck() {
    // given
    String externalTaskId = externalTaskService.fetchAndLock(5, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute()
        .get(0)
        .getId();

    externalTaskService.handleFailure(externalTaskId,WORKER_ID,ERROR_MESSAGE,ERROR_DETAILS,1,1000L);

    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);

    // then
    assertThat(externalTaskService.getExternalTaskErrorDetails(externalTaskId), is(ERROR_DETAILS));
  }
}
