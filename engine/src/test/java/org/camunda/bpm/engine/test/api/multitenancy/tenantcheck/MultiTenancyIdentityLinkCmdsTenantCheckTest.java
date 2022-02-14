/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.api.multitenancy.tenantcheck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    assertThat(taskService.createTaskQuery().taskAssignee("demo").count()).isEqualTo(1L);
  }

  @Test
  public void setAssigneeForTaskWithNoAuthenticatedTenant() {

    identityService.setAuthentication("aUserId", null);

    // when/then
    assertThatThrownBy(() -> taskService.setAssignee(task.getId(), "demo"))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot assign the task '"
          + task.getId() +"' because it belongs to no authenticated tenant.");

  }

  @Test
  public void setAssigneeForTaskWithDisabledTenantCheck() {

    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);

    taskService.setAssignee(task.getId(), "demo");
    // then
    assertThat(taskService.createTaskQuery().taskAssignee("demo").count()).isEqualTo(1L);
  }

  // set owner test cases
  @Test
  public void setOwnerForTaskWithAuthenticatedTenant() {

    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));

    taskService.setOwner(task.getId(), "demo");

    // then
    assertThat(taskService.createTaskQuery().taskOwner("demo").count()).isEqualTo(1L);
  }

  @Test
  public void setOwnerForTaskWithNoAuthenticatedTenant() {

    identityService.setAuthentication("aUserId", null);

    // when/then
    assertThatThrownBy(() -> taskService.setOwner(task.getId(), "demo"))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot assign the task '"
          + task.getId() + "' because it belongs to no authenticated tenant.");

  }

  @Test
  public void setOwnerForTaskWithDisabledTenantCheck() {

    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);

    taskService.setOwner(task.getId(), "demo");
    // then
    assertThat(taskService.createTaskQuery().taskOwner("demo").count()).isEqualTo(1L);
  }

  // get identity links
  @Test
  public void getIdentityLinkWithAuthenticatedTenant() {

    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    taskService.setOwner(task.getId(), "demo");

    assertThat(taskService.getIdentityLinksForTask(task.getId()).get(0).getType()).isEqualTo("owner");
  }

  @Test
  public void getIdentityLinkWitNoAuthenticatedTenant() {

    taskService.setOwner(task.getId(), "demo");
    identityService.setAuthentication("aUserId", null);

    // when/then
    assertThatThrownBy(() -> taskService.getIdentityLinksForTask(task.getId()))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot read the task '"
          + task.getId() +"' because it belongs to no authenticated tenant.");

  }

  @Test
  public void getIdentityLinkWithDisabledTenantCheck() {

    taskService.setOwner(task.getId(), "demo");
    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);

    // then
    assertThat(taskService.getIdentityLinksForTask(task.getId()).get(0).getType()).isEqualTo("owner");

  }

  // add candidate user
  @Test
  public void addCandidateUserWithAuthenticatedTenant() {

    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    taskService.addCandidateUser(task.getId(), "demo");

    // then
    assertThat(taskService.createTaskQuery().taskCandidateUser("demo").count()).isEqualTo(1L);
  }

  @Test
  public void addCandidateUserWithNoAuthenticatedTenant() {

    identityService.setAuthentication("aUserId", null);

    // when/then
    assertThatThrownBy(() -> taskService.addCandidateUser(task.getId(), "demo"))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot assign the task '"
          + task.getId() +"' because it belongs to no authenticated tenant.");

  }

  @Test
  public void addCandidateUserWithDisabledTenantCheck() {

    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);

    // when
    taskService.addCandidateUser(task.getId(), "demo");

    // then
    assertThat(taskService.createTaskQuery().taskCandidateUser("demo").count()).isEqualTo(1L);
  }

  // add candidate group
  @Test
  public void addCandidateGroupWithAuthenticatedTenant() {

    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    taskService.addCandidateGroup(task.getId(), "demo");

    // then
    assertThat(taskService.createTaskQuery().taskCandidateGroup("demo").count()).isEqualTo(1L);
  }

  @Test
  public void addCandidateGroupWithNoAuthenticatedTenant() {

    identityService.setAuthentication("aUserId", null);

    // when/then
    assertThatThrownBy(() -> taskService.addCandidateGroup(task.getId(), "demo"))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot assign the task '"
          +task.getId()+ "' because it belongs to no authenticated tenant.");

  }

  @Test
  public void addCandidateGroupWithDisabledTenantCheck() {

    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);

    // when
    taskService.addCandidateGroup(task.getId(), "demo");

    // then
    assertThat(taskService.createTaskQuery().taskCandidateGroup("demo").count()).isEqualTo(1L);
  }

  // delete candidate users
  @Test
  public void deleteCandidateUserWithAuthenticatedTenant() {

    taskService.addCandidateUser(task.getId(), "demo");
    assertThat(taskService.createTaskQuery().taskCandidateUser("demo").count()).isEqualTo(1L);

    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));

    taskService.deleteCandidateUser(task.getId(), "demo");
    // then
    assertThat(taskService.createTaskQuery().taskCandidateUser("demo").count()).isEqualTo(0L);
  }

  @Test
  public void deleteCandidateUserWithNoAuthenticatedTenant() {

    taskService.addCandidateUser(task.getId(), "demo");
    identityService.setAuthentication("aUserId", null);

    // when/then
    assertThatThrownBy(() -> taskService.deleteCandidateUser(task.getId(), "demo"))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot assign the task '"
          + task.getId() +"' because it belongs to no authenticated tenant.");

  }

  @Test
  public void deleteCandidateUserWithDisabledTenantCheck() {

    taskService.addCandidateUser(task.getId(), "demo");
    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);

    // when
    taskService.deleteCandidateUser(task.getId(), "demo");

    // then
    assertThat(taskService.createTaskQuery().taskCandidateUser("demo").count()).isEqualTo(0L);
  }

  // delete candidate groups
  @Test
  public void deleteCandidateGroupWithAuthenticatedTenant() {

    taskService.addCandidateGroup(task.getId(), "demo");
    assertThat(taskService.createTaskQuery().taskCandidateGroup("demo").count()).isEqualTo(1L);

    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));

    taskService.deleteCandidateGroup(task.getId(), "demo");
    // then
    assertThat(taskService.createTaskQuery().taskCandidateGroup("demo").count()).isEqualTo(0L);
  }

  @Test
  public void deleteCandidateGroupWithNoAuthenticatedTenant() {

    taskService.addCandidateGroup(task.getId(), "demo");
    identityService.setAuthentication("aUserId", null);

    // when/then
    assertThatThrownBy(() -> taskService.deleteCandidateGroup(task.getId(), "demo"))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot assign the task '"
          + task.getId() +"' because it belongs to no authenticated tenant.");

  }

  @Test
  public void deleteCandidateGroupWithDisabledTenantCheck() {

    taskService.addCandidateGroup(task.getId(), "demo");
    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);

    // when
    taskService.deleteCandidateGroup(task.getId(), "demo");

    // then
    assertThat(taskService.createTaskQuery().taskCandidateGroup("demo").count()).isEqualTo(0L);
  }

  // add user identity link
  @Test
  public void addUserIdentityLinkWithAuthenticatedTenant() {

    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    taskService.addUserIdentityLink(task.getId(), "demo", IdentityLinkType.CANDIDATE);

    // then
    assertThat(taskService.createTaskQuery().taskCandidateUser("demo").count()).isEqualTo(1L);
  }

  @Test
  public void addUserIdentityLinkWithNoAuthenticatedTenant() {

    identityService.setAuthentication("aUserId", null);

    // when/then
    assertThatThrownBy(() -> taskService.addUserIdentityLink(task.getId(), "demo", IdentityLinkType.CANDIDATE))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot assign the task '"
          + task.getId() +"' because it belongs to no authenticated tenant.");

  }

  @Test
  public void addUserIdentityLinkWithDisabledTenantCheck() {

    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);

    // when
    taskService.addUserIdentityLink(task.getId(), "demo", IdentityLinkType.ASSIGNEE);

    // then
    assertThat(taskService.createTaskQuery().taskAssignee("demo").count()).isEqualTo(1L);
  }

  // add group identity link
  @Test
  public void addGroupIdentityLinkWithAuthenticatedTenant() {

    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    taskService.addGroupIdentityLink(task.getId(), "demo", IdentityLinkType.CANDIDATE);

    // then
    assertThat(taskService.createTaskQuery().taskCandidateGroup("demo").count()).isEqualTo(1L);
  }

  @Test
  public void addGroupIdentityLinkWithNoAuthenticatedTenant() {

    identityService.setAuthentication("aUserId", null);

    // when/then
    assertThatThrownBy(() -> taskService.addGroupIdentityLink(task.getId(), "demo", IdentityLinkType.CANDIDATE))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot assign the task '"
          + task.getId() + "' because it belongs to no authenticated tenant.");

  }

  @Test
  public void addGroupIdentityLinkWithDisabledTenantCheck() {

    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);

    // when
    taskService.addGroupIdentityLink(task.getId(), "demo", IdentityLinkType.CANDIDATE);

    // then
    assertThat(taskService.createTaskQuery().taskCandidateGroup("demo").count()).isEqualTo(1L);
  }

  // delete user identity link
  @Test
  public void deleteUserIdentityLinkWithAuthenticatedTenant() {

    taskService.addUserIdentityLink(task.getId(), "demo", IdentityLinkType.ASSIGNEE);
    assertThat(taskService.createTaskQuery().taskAssignee("demo").count()).isEqualTo(1L);

    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));

    taskService.deleteUserIdentityLink(task.getId(), "demo", IdentityLinkType.ASSIGNEE);
    // then
    assertThat(taskService.createTaskQuery().taskAssignee("demo").count()).isEqualTo(0L);
  }

  @Test
  public void deleteUserIdentityLinkWithNoAuthenticatedTenant() {

    taskService.addUserIdentityLink(task.getId(), "demo", IdentityLinkType.ASSIGNEE);
    identityService.setAuthentication("aUserId", null);

    // when/then
    assertThatThrownBy(() -> taskService.deleteUserIdentityLink(task.getId(), "demo", IdentityLinkType.ASSIGNEE))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot assign the task '"
          + task.getId() +"' because it belongs to no authenticated tenant.");

  }

  @Test
  public void deleteUserIdentityLinkWithDisabledTenantCheck() {

    taskService.addUserIdentityLink(task.getId(), "demo", IdentityLinkType.ASSIGNEE);
    assertThat(taskService.createTaskQuery().taskAssignee("demo").count()).isEqualTo(1L);

    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);

    // when
    taskService.deleteUserIdentityLink(task.getId(), "demo", IdentityLinkType.ASSIGNEE);

    // then
    assertThat(taskService.createTaskQuery().taskAssignee("demo").count()).isEqualTo(0L);
  }

  // delete group identity link
  @Test
  public void deleteGroupIdentityLinkWithAuthenticatedTenant() {

    taskService.addGroupIdentityLink(task.getId(), "demo", IdentityLinkType.CANDIDATE);
    assertThat(taskService.createTaskQuery().taskCandidateGroup("demo").count()).isEqualTo(1L);

    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));

    taskService.deleteGroupIdentityLink(task.getId(), "demo", IdentityLinkType.CANDIDATE);
    // then
    assertThat(taskService.createTaskQuery().taskCandidateGroup("demo").count()).isEqualTo(0L);
  }

  @Test
  public void deleteGroupIdentityLinkWithNoAuthenticatedTenant() {

    taskService.addGroupIdentityLink(task.getId(), "demo", IdentityLinkType.CANDIDATE);
    identityService.setAuthentication("aUserId", null);

    // when/then
    assertThatThrownBy(() -> taskService.deleteGroupIdentityLink(task.getId(), "demo", IdentityLinkType.CANDIDATE))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot assign the task '"
          + task.getId() +"' because it belongs to no authenticated tenant.");

  }

  @Test
  public void deleteGroupIdentityLinkWithDisabledTenantCheck() {

    taskService.addGroupIdentityLink(task.getId(), "demo", IdentityLinkType.CANDIDATE);
    assertThat(taskService.createTaskQuery().taskCandidateGroup("demo").count()).isEqualTo(1L);

    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);

    // when
    taskService.deleteGroupIdentityLink(task.getId(), "demo", IdentityLinkType.CANDIDATE);

    // then
    assertThat(taskService.createTaskQuery().taskCandidateGroup("demo").count()).isEqualTo(0L);
  }
}
