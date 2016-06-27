package org.camunda.bpm.engine.test.api.multitenancy.tenantcheck;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.Task;
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

public class MultiTenancyTaskServiceCmdsTenantCheckTest {
  
  protected static final String TENANT_ONE = "tenant1";
  
  protected static final String PROCESS_DEFINITION_KEY = "oneTaskProcess";
  
  protected static final BpmnModelInstance ONE_TASK_PROCESS = Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
    .startEvent()
    .userTask()
    .endEvent()
    .done();
  
  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);
  
  protected TaskService taskService;
  protected IdentityService identityService;
  
  protected Task task;
  
  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);
  
  @Rule
  public ExpectedException thrown = ExpectedException.none();
  
  @Before
  public void init() {
    
    testRule.deployForTenant(TENANT_ONE, ONE_TASK_PROCESS);
    
    engineRule.getRuntimeService().startProcessInstanceByKey(PROCESS_DEFINITION_KEY).getId();
    
    task = engineRule.getTaskService().createTaskQuery().singleResult();
    
    taskService = engineRule.getTaskService();
    identityService = engineRule.getIdentityService();
  }

  // save test cases
  @Test
  public void saveTaskWithAuthenticatedTenant() {
    
    task = taskService.newTask("newTask");
    task.setTenantId(TENANT_ONE);
    
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    
    taskService.saveTask(task);
    // then
    assertThat(taskService.createTaskQuery().taskId(task.getId()).count(), is(1L));
    
    taskService.deleteTask(task.getId(), true);
  }

  @Test
  public void saveTaskWithNoAuthenticatedTenant() {
    
    task = taskService.newTask("newTask");
    task.setTenantId(TENANT_ONE);
    
    identityService.setAuthentication("aUserId", null);
    
    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot create the task '"
      + task.getId() +"' because it belongs to no authenticated tenant.");
    taskService.saveTask(task);
    
  }
  
  @Test
  public void saveTaskWithDisabledTenantCheck() {
    
    task = taskService.newTask("newTask");
    task.setTenantId(TENANT_ONE);
    
    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    
    taskService.saveTask(task);
    // then
    assertThat(taskService.createTaskQuery().taskId(task.getId()).count(), is(1L));
    taskService.deleteTask(task.getId(), true);
  }

  // update task test
  @Test
  public void updateTaskWithAuthenticatedTenant() {
    
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    task.setAssignee("aUser");
    taskService.saveTask(task);
    
    // then
    assertThat(taskService.createTaskQuery().taskAssignee("aUser").count(), is(1L));
  }
  
  @Test
  public void updateTaskWithNoAuthenticatedTenant() {
    
    task.setAssignee("aUser");
    identityService.setAuthentication("aUserId", null);
    
    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot assign the task '"
      + task.getId() +"' because it belongs to no authenticated tenant.");
    taskService.saveTask(task);
    
  }
  
  @Test
  public void updateTaskWithDisabledTenantCheck() {
    
    task.setAssignee("aUser");
    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    
    // then
    taskService.saveTask(task);
    assertThat(taskService.createTaskQuery().taskAssignee("aUser").count(), is(1L));
    
  }
  
  // claim task test
  @Test
  public void claimTaskWithAuthenticatedTenant() {
    
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    
    // then
    taskService.claim(task.getId(), "bUser");
    assertThat(taskService.createTaskQuery().taskAssignee("bUser").count(), is(1L));
  }
  
  @Test
  public void claimTaskWithNoAuthenticatedTenant() {
    
    identityService.setAuthentication("aUserId", null);
    
    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot work on task '"
      + task.getId() +"' because it belongs to no authenticated tenant.");
    taskService.claim(task.getId(), "bUser");
    
  }
  
  @Test
  public void claimTaskWithDisableTenantCheck() {
    
    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    
    // then
    taskService.claim(task.getId(), "bUser");
    assertThat(taskService.createTaskQuery().taskAssignee("bUser").count(), is(1L));
    
  }
  
  // complete the task test
  @Test
  public void completeTaskWithAuthenticatedTenant() {
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    
    // then
    taskService.complete(task.getId());
    assertThat(taskService.createTaskQuery().taskId(task.getId()).active().count(), is(0L));
  }
  
  @Test
  public void completeTaskWithNoAuthenticatedTenant() {
    
    identityService.setAuthentication("aUserId", null);
    
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot work on task '"
      + task.getId() +"' because it belongs to no authenticated tenant.");
    taskService.complete(task.getId());
  }
  
  @Test
  public void completeWithDisabledTenantCheck() {
    
    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    
    // then
    taskService.complete(task.getId());
    assertThat(taskService.createTaskQuery().taskId(task.getId()).active().count(), is(0L));
  }
  
  // delegate task test
  @Test
  public void delegateTaskWithAuthenticatedTenant() {
    
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    
    taskService.delegateTask(task.getId(), "demo");
    
    assertThat(taskService.createTaskQuery().taskAssignee("demo").count(), is(1L));
  }
  
  @Test
  public void delegateTaskWithNoAuthenticatedTenant() {
    
    identityService.setAuthentication("aUserId", null);
    
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot assign the task '"
      + task.getId() +"' because it belongs to no authenticated tenant.");
    taskService.delegateTask(task.getId(), "demo");
    
  }
  
  @Test
  public void delegateTaskWithDisabledTenantCheck() {
    
    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    
    // then
    taskService.delegateTask(task.getId(), "demo");
    assertThat(taskService.createTaskQuery().taskAssignee("demo").count(), is(1L));
  }
  
  // resolve task test
  @Test
  public void resolveTaskWithAuthenticatedTenant() {
    
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    
    taskService.resolveTask(task.getId());
    
    assertThat(taskService.createTaskQuery().taskDelegationState(DelegationState.RESOLVED).taskId(task.getId()).count(), is(1L));
  }
  
  @Test
  public void resolveTaskWithNoAuthenticatedTenant() {
    
    identityService.setAuthentication("aUserId", null);
    
    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot work on task '"
      + task.getId() +"' because it belongs to no authenticated tenant.");
    taskService.resolveTask(task.getId());
    
  }
  
  @Test
  public void resolveTaskWithDisableTenantCheck() {
    
    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    
    // then
    taskService.resolveTask(task.getId());
    assertThat(taskService.createTaskQuery().taskDelegationState(DelegationState.RESOLVED).taskId(task.getId()).count(), is(1L));
  }
  
  // set priority test cases
  @Test
  public void setPriorityForTaskWithAuthenticatedTenant() {
    
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    
    taskService.setPriority(task.getId(), 1);
    
    assertThat(taskService.createTaskQuery().taskPriority(1).taskId(task.getId()).count(), is(1L));
  }
  
  @Test
  public void setPriorityForTaskWithNoAuthenticatedTenant() {
    
    identityService.setAuthentication("aUserId", null);
    
    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot assign the task '"
      + task.getId() +"' because it belongs to no authenticated tenant.");
    taskService.setPriority(task.getId(), 1);
  }
  
  @Test
  public void setPriorityForTaskWithDisabledTenantCheck() {
    
    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    
    // then
    taskService.setPriority(task.getId(), 1);
    assertThat(taskService.createTaskQuery().taskPriority(1).taskId(task.getId()).count(), is(1L));
  }
  
  // delete task test
  @Test
  public void deleteTaskWithAuthenticatedTenant() {
    
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    task = createTaskforTenant();
    assertThat(taskService.createTaskQuery().taskId(task.getId()).count(), is(1L));
    
    // then
    taskService.deleteTask(task.getId(), true);
    assertThat(taskService.createTaskQuery().taskId(task.getId()).count(), is(0L));
  }
  
  @Test
  public void deleteTaskWithNoAuthenticatedTenant() {

    try {
      task = createTaskforTenant();
      identityService.setAuthentication("aUserId", null);
      // then
      thrown.expect(ProcessEngineException.class);
      thrown.expectMessage("Cannot delete the task '"
        + task.getId() +"' because it belongs to no authenticated tenant.");
      taskService.deleteTask(task.getId(), true);
    } finally {
      identityService.clearAuthentication();
      taskService.deleteTask(task.getId(), true);
    }
  }

  @Test
  public void deleteTaskWithDisabledTenantCheck() {

    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
 
    task = createTaskforTenant();
    assertThat(taskService.createTaskQuery().taskId(task.getId()).count(), is(1L));
    
    // then
    taskService.deleteTask(task.getId(), true);
    assertThat(taskService.createTaskQuery().taskId(task.getId()).count(), is(0L));
  }

  protected Task createTaskforTenant() {
    Task newTask = taskService.newTask("newTask");
    newTask.setTenantId(TENANT_ONE);
    taskService.saveTask(newTask);
    
    return newTask;

  }
}
