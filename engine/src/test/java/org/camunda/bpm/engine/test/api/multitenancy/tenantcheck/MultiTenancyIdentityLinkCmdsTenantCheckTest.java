package org.camunda.bpm.engine.test.api.multitenancy.tenantcheck;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.IdentityLinkType;
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

public class MultiTenancyIdentityLinkCmdsTenantCheckTest {
  
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
  
  // set Assignee
  @Test
  public void setAssigneeForTaskWithAuthenticatedTenant() {
    
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    
    taskService.setAssignee(task.getId(), "demo");
    
    // then
    assertThat(taskService.createTaskQuery().taskAssignee("demo").count(), is(1L));
  }
  
  @Test
  public void setAssigneeForTaskWithNoAuthenticatedTenant() {
    
    identityService.setAuthentication("aUserId", null);
    
    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot assign the task '"
      + task.getId() +"' because it belongs to no authenticated tenant.");
    taskService.setAssignee(task.getId(), "demo");
    
  }
  
  @Test
  public void setAssigneeForTaskWithDisabledTenantCheck() {
    
    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    
    taskService.setAssignee(task.getId(), "demo");
    // then
    assertThat(taskService.createTaskQuery().taskAssignee("demo").count(), is(1L));
  }
  
  // set owner test cases
  @Test
  public void setOwnerForTaskWithAuthenticatedTenant() {
    
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    
    taskService.setOwner(task.getId(), "demo");
    
    // then
    assertThat(taskService.createTaskQuery().taskOwner("demo").count(), is(1L));
  }
  
  @Test
  public void setOwnerForTaskWithNoAuthenticatedTenant() {
    
    identityService.setAuthentication("aUserId", null);
    
    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot assign the task '"
      + task.getId() + "' because it belongs to no authenticated tenant.");

    taskService.setOwner(task.getId(), "demo");
    
  }
  
  @Test
  public void setOwnerForTaskWithDisabledTenantCheck() {
    
    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    
    taskService.setOwner(task.getId(), "demo");
    // then
    assertThat(taskService.createTaskQuery().taskOwner("demo").count(), is(1L));
  }
  
  // get identity links
  @Test
  public void getIdentityLinkWithAuthenticatedTenant() {
    
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    taskService.setOwner(task.getId(), "demo");
    
    assertThat(taskService.getIdentityLinksForTask(task.getId()).get(0).getType(), is("owner"));
  }
  
  @Test
  public void getIdentityLinkWitNoAuthenticatedTenant() {
    
    taskService.setOwner(task.getId(), "demo");
    identityService.setAuthentication("aUserId", null);
    
    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot read the task '"
      + task.getId() +"' because it belongs to no authenticated tenant.");
    taskService.getIdentityLinksForTask(task.getId());
    
  }
  
  @Test
  public void getIdentityLinkWithDisabledTenantCheck() {
    
    taskService.setOwner(task.getId(), "demo");
    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    
    // then
    assertThat(taskService.getIdentityLinksForTask(task.getId()).get(0).getType(), is("owner"));
    
  }
  
  // add candidate user
  @Test
  public void addCandidateUserWithAuthenticatedTenant() {
    
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    taskService.addCandidateUser(task.getId(), "demo");
    
    // then
    assertThat(taskService.createTaskQuery().taskCandidateUser("demo").count(), is(1L));
  }
  
  @Test
  public void addCandidateUserWithNoAuthenticatedTenant() {
    
    identityService.setAuthentication("aUserId", null);
    
    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot assign the task '"
      + task.getId() +"' because it belongs to no authenticated tenant.");
    taskService.addCandidateUser(task.getId(), "demo");
    
  }
  
  @Test
  public void addCandidateUserWithDisabledTenantCheck() {
    
    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    
    // when
    taskService.addCandidateUser(task.getId(), "demo");
    
    // then
    assertThat(taskService.createTaskQuery().taskCandidateUser("demo").count(), is(1L));
  }
  
  // add candidate group
  @Test
  public void addCandidateGroupWithAuthenticatedTenant() {
    
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    taskService.addCandidateGroup(task.getId(), "demo");
    
    // then
    assertThat(taskService.createTaskQuery().taskCandidateGroup("demo").count(), is(1L));
  }
  
  @Test
  public void addCandidateGroupWithNoAuthenticatedTenant() {
    
    identityService.setAuthentication("aUserId", null);
    
    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot assign the task '"
      +task.getId()+ "' because it belongs to no authenticated tenant.");

    taskService.addCandidateGroup(task.getId(), "demo");
    
  }
  
  @Test
  public void addCandidateGroupWithDisabledTenantCheck() {
    
    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    
    // when
    taskService.addCandidateGroup(task.getId(), "demo");
    
    // then
    assertThat(taskService.createTaskQuery().taskCandidateGroup("demo").count(), is(1L));
  }
  
  // delete candidate users
  @Test
  public void deleteCandidateUserWithAuthenticatedTenant() {
    
    taskService.addCandidateUser(task.getId(), "demo");
    assertThat(taskService.createTaskQuery().taskCandidateUser("demo").count(), is(1L));
    
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    
    taskService.deleteCandidateUser(task.getId(), "demo");
    // then
    assertThat(taskService.createTaskQuery().taskCandidateUser("demo").count(), is(0L));
  }
  
  @Test
  public void deleteCandidateUserWithNoAuthenticatedTenant() {
    
    taskService.addCandidateUser(task.getId(), "demo");
    identityService.setAuthentication("aUserId", null);
    
    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot assign the task '"
      + task.getId() +"' because it belongs to no authenticated tenant.");

    taskService.deleteCandidateUser(task.getId(), "demo");
    
  }
  
  @Test
  public void deleteCandidateUserWithDisabledTenantCheck() {
    
    taskService.addCandidateUser(task.getId(), "demo");
    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    
    // when
    taskService.deleteCandidateUser(task.getId(), "demo");
    
    // then
    assertThat(taskService.createTaskQuery().taskCandidateUser("demo").count(), is(0L));
  }
  
  // delete candidate groups
  @Test
  public void deleteCandidateGroupWithAuthenticatedTenant() {
    
    taskService.addCandidateGroup(task.getId(), "demo");
    assertThat(taskService.createTaskQuery().taskCandidateGroup("demo").count(), is(1L));
    
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    
    taskService.deleteCandidateGroup(task.getId(), "demo");
    // then
    assertThat(taskService.createTaskQuery().taskCandidateGroup("demo").count(), is(0L));
  }
  
  @Test
  public void deleteCandidateGroupWithNoAuthenticatedTenant() {
    
    taskService.addCandidateGroup(task.getId(), "demo");
    identityService.setAuthentication("aUserId", null);
    
    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot assign the task '"
      + task.getId() +"' because it belongs to no authenticated tenant.");
    taskService.deleteCandidateGroup(task.getId(), "demo");
    
  }
  
  @Test
  public void deleteCandidateGroupWithDisabledTenantCheck() {
    
    taskService.addCandidateGroup(task.getId(), "demo");
    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    
    // when
    taskService.deleteCandidateGroup(task.getId(), "demo");
    
    // then
    assertThat(taskService.createTaskQuery().taskCandidateGroup("demo").count(), is(0L));
  }
  
  // add user identity link
  @Test
  public void addUserIdentityLinkWithAuthenticatedTenant() {
    
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    taskService.addUserIdentityLink(task.getId(), "demo", IdentityLinkType.CANDIDATE);
    
    // then
    assertThat(taskService.createTaskQuery().taskCandidateUser("demo").count(), is(1L));
  }
  
  @Test
  public void addUserIdentityLinkWithNoAuthenticatedTenant() {
    
    identityService.setAuthentication("aUserId", null);
    
    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot assign the task '"
      + task.getId() +"' because it belongs to no authenticated tenant.");

    taskService.addUserIdentityLink(task.getId(), "demo", IdentityLinkType.CANDIDATE);
    
  }
  
  @Test
  public void addUserIdentityLinkWithDisabledTenantCheck() {
    
    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    
    // when
    taskService.addUserIdentityLink(task.getId(), "demo", IdentityLinkType.ASSIGNEE);
    
    // then
    assertThat(taskService.createTaskQuery().taskAssignee("demo").count(), is(1L));
  }
  
  // add group identity link
  @Test
  public void addGroupIdentityLinkWithAuthenticatedTenant() {
    
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    taskService.addGroupIdentityLink(task.getId(), "demo", IdentityLinkType.CANDIDATE);
    
    // then
    assertThat(taskService.createTaskQuery().taskCandidateGroup("demo").count(), is(1L));
  }
  
  @Test
  public void addGroupIdentityLinkWithNoAuthenticatedTenant() {
    
    identityService.setAuthentication("aUserId", null);
    
    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot assign the task '"
      + task.getId() + "' because it belongs to no authenticated tenant.");
    taskService.addGroupIdentityLink(task.getId(), "demo", IdentityLinkType.CANDIDATE);
    
  }
  
  @Test
  public void addGroupIdentityLinkWithDisabledTenantCheck() {
    
    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    
    // when
    taskService.addGroupIdentityLink(task.getId(), "demo", IdentityLinkType.CANDIDATE);
    
    // then
    assertThat(taskService.createTaskQuery().taskCandidateGroup("demo").count(), is(1L));
  }

  // delete user identity link
  @Test
  public void deleteUserIdentityLinkWithAuthenticatedTenant() {
    
    taskService.addUserIdentityLink(task.getId(), "demo", IdentityLinkType.ASSIGNEE);
    assertThat(taskService.createTaskQuery().taskAssignee("demo").count(), is(1L));
    
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    
    taskService.deleteUserIdentityLink(task.getId(), "demo", IdentityLinkType.ASSIGNEE);
    // then
    assertThat(taskService.createTaskQuery().taskAssignee("demo").count(), is(0L));
  }
  
  @Test
  public void deleteUserIdentityLinkWithNoAuthenticatedTenant() {
    
    taskService.addUserIdentityLink(task.getId(), "demo", IdentityLinkType.ASSIGNEE);
    identityService.setAuthentication("aUserId", null);
    
    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot assign the task '"
      + task.getId() +"' because it belongs to no authenticated tenant.");
    taskService.deleteUserIdentityLink(task.getId(), "demo", IdentityLinkType.ASSIGNEE);
    
  }
  
  @Test
  public void deleteUserIdentityLinkWithDisabledTenantCheck() {
    
    taskService.addUserIdentityLink(task.getId(), "demo", IdentityLinkType.ASSIGNEE);
    assertThat(taskService.createTaskQuery().taskAssignee("demo").count(), is(1L));

    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    
    // when
    taskService.deleteUserIdentityLink(task.getId(), "demo", IdentityLinkType.ASSIGNEE);
    
    // then
    assertThat(taskService.createTaskQuery().taskAssignee("demo").count(), is(0L));
  }

  // delete group identity link
  @Test
  public void deleteGroupIdentityLinkWithAuthenticatedTenant() {
    
    taskService.addGroupIdentityLink(task.getId(), "demo", IdentityLinkType.CANDIDATE);
    assertThat(taskService.createTaskQuery().taskCandidateGroup("demo").count(), is(1L));
    
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    
    taskService.deleteGroupIdentityLink(task.getId(), "demo", IdentityLinkType.CANDIDATE);
    // then
    assertThat(taskService.createTaskQuery().taskCandidateGroup("demo").count(), is(0L));
  }
  
  @Test
  public void deleteGroupIdentityLinkWithNoAuthenticatedTenant() {
    
    taskService.addGroupIdentityLink(task.getId(), "demo", IdentityLinkType.CANDIDATE);
    identityService.setAuthentication("aUserId", null);
    
    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot assign the task '"
      + task.getId() +"' because it belongs to no authenticated tenant.");

    taskService.deleteGroupIdentityLink(task.getId(), "demo", IdentityLinkType.CANDIDATE);
    
  }
  
  @Test
  public void deleteGroupIdentityLinkWithDisabledTenantCheck() {
    
    taskService.addGroupIdentityLink(task.getId(), "demo", IdentityLinkType.CANDIDATE);
    assertThat(taskService.createTaskQuery().taskCandidateGroup("demo").count(), is(1L));

    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    
    // when
    taskService.deleteGroupIdentityLink(task.getId(), "demo", IdentityLinkType.CANDIDATE);
    
    // then
    assertThat(taskService.createTaskQuery().taskCandidateGroup("demo").count(), is(0L));
  }
}
