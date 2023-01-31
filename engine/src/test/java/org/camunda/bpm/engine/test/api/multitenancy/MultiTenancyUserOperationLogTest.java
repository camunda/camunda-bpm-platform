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
package org.camunda.bpm.engine.test.api.multitenancy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_FULL;
import static org.junit.Assert.assertNull;

import java.util.Arrays;

import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

@RequiredHistoryLevel(HISTORY_FULL)
public class MultiTenancyUserOperationLogTest {

  private static final String PROCESS_NAME = "process";
  protected static final String USER_ID = "demo";
  protected static final String TENANT_TWO = "tenant2";
  protected static final String TENANT_ONE = "tenant1";
  protected static final String TASK_ID = "aTaskId";

  protected static final BpmnModelInstance process = Bpmn.createExecutableProcess(PROCESS_NAME).startEvent().userTask(TASK_ID).done();

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected ProcessEngineConfiguration processEngineConfiguration;
  protected TaskService taskService;
  protected HistoryService historyService;
  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;
  protected IdentityService identityService;

  @Before
  public void init() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    taskService = engineRule.getTaskService();
    historyService = engineRule.getHistoryService();
    repositoryService = engineRule.getRepositoryService();
    runtimeService = engineRule.getRuntimeService();
    identityService = engineRule.getIdentityService();
  }

  @Test
  public void shouldSetAnnotationWithoutTenant() {
    // given
    testRule.deploy(process);
    identityService.setAuthentication("aUserId", null);

    runtimeService.startProcessInstanceByKey(PROCESS_NAME);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    taskService.complete(taskId);

    UserOperationLogEntry singleResult = historyService.createUserOperationLogQuery().entityType("Task").singleResult();

    // when
    historyService.setAnnotationForOperationLogById(singleResult.getOperationId(), "anAnnotation");
    singleResult = historyService.createUserOperationLogQuery()
        .entityType(EntityTypes.TASK)
        .singleResult();

    // then
    assertThat(singleResult.getTenantId()).isEqualTo(null);
  }

  @Test
  public void shouldSetAnnotationWithTenant() {
    // given
    testRule.deployForTenant(TENANT_ONE, process);
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));

    runtimeService.startProcessInstanceByKey(PROCESS_NAME);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    taskService.complete(taskId);

    UserOperationLogEntry singleResult = historyService.createUserOperationLogQuery().entityType("Task").singleResult();

    // when
    historyService.setAnnotationForOperationLogById(singleResult.getOperationId(), "anAnnotation");
    singleResult = historyService.createUserOperationLogQuery()
        .entityType(EntityTypes.TASK)
        .singleResult();

    // then
    assertThat(singleResult.getTenantId()).isEqualTo(TENANT_ONE);
  }

  @Test
  public void shouldThrowExceptionWhenSetAnnotationWithNoAuthenticatedTenant() {
    // given
    testRule.deployForTenant(TENANT_ONE, process);
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));

    runtimeService.startProcessInstanceByKey(PROCESS_NAME);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    taskService.complete(taskId);

    UserOperationLogEntry singleResult = historyService.createUserOperationLogQuery().entityType("Task").singleResult();

    identityService.setAuthentication("aUserId1", null);

    // when/then
    assertThatThrownBy(() -> historyService.setAnnotationForOperationLogById(singleResult.getOperationId(), "anAnnotation"))
    .isInstanceOf(ProcessEngineException.class)
    .hasMessageContaining("Cannot update the user operation log entry '"
        + singleResult.getId() +"' because it belongs to no authenticated tenant.");
  }

  @Test
  public void shouldClearAnnotationWithoutTenant() {
    // given
    testRule.deploy(process);
    identityService.setAuthentication("aUserId", null);

    runtimeService.startProcessInstanceByKey(PROCESS_NAME);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    taskService.complete(taskId);

    UserOperationLogEntry singleResult = historyService.createUserOperationLogQuery().entityType("Task").singleResult();
    historyService.setAnnotationForOperationLogById(singleResult.getOperationId(), "anAnnotation");

    // when
    historyService.clearAnnotationForOperationLogById(singleResult.getOperationId());
    singleResult = historyService.createUserOperationLogQuery()
        .entityType(EntityTypes.TASK)
        .singleResult();

    // then
    assertThat(singleResult.getTenantId()).isEqualTo(null);
  }

  @Test
  public void shouldClearAnnotationWithTenant() {
    testRule.deployForTenant(TENANT_ONE, process);
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));

    runtimeService.startProcessInstanceByKey(PROCESS_NAME);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    taskService.complete(taskId);

    UserOperationLogEntry singleResult = historyService.createUserOperationLogQuery().entityType("Task").singleResult();

    historyService.setAnnotationForOperationLogById(singleResult.getOperationId(), "anAnnotation");

    // when
    historyService.clearAnnotationForOperationLogById(singleResult.getOperationId());
    singleResult = historyService.createUserOperationLogQuery()
        .entityType(EntityTypes.TASK)
        .singleResult();

    // then
    assertThat(singleResult.getTenantId()).isEqualTo(TENANT_ONE);
  }

  @Test
  public void shouldThrowExceptionWhenClearAnnotationWithNoAuthenticatedTenant() {
    // given
    testRule.deployForTenant(TENANT_ONE, process);
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));

    runtimeService.startProcessInstanceByKey(PROCESS_NAME);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    taskService.complete(taskId);

    UserOperationLogEntry singleResult = historyService.createUserOperationLogQuery().entityType("Task").singleResult();
    historyService.setAnnotationForOperationLogById(singleResult.getOperationId(), "anAnnotation");

    identityService.setAuthentication("aUserId1", null);

    // when/then
    assertThatThrownBy(() ->  historyService.clearAnnotationForOperationLogById(singleResult.getOperationId()))
    .isInstanceOf(ProcessEngineException.class)
    .hasMessageContaining("Cannot update the user operation log entry '"
        + singleResult.getId() +"' because it belongs to no authenticated tenant.");
  }

  @Test
  public void shouldDeleteAnnotationWithoutTenant() {
    // given
    testRule.deploy(process);
    runtimeService.startProcessInstanceByKey(PROCESS_NAME);
    String taskId = taskService.createTaskQuery().singleResult().getId();
    identityService.setAuthenticatedUserId("paul");

    taskService.complete(taskId);

    String entryId = historyService.createUserOperationLogQuery().entityType("Task").singleResult().getId();

    // when
    historyService.deleteUserOperationLogEntry(entryId);

    // then
    UserOperationLogEntry singleResult = historyService.createUserOperationLogQuery().entityType("Task").singleResult();
    assertNull(singleResult);
  }

  @Test
  public void shouldDeleteAnnotationWithTenant() {
    testRule.deployForTenant(TENANT_ONE, process);
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));

    runtimeService.startProcessInstanceByKey(PROCESS_NAME);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    taskService.complete(taskId);

    UserOperationLogEntry singleResult = historyService.createUserOperationLogQuery().entityType("Task").singleResult();
    String entryId = singleResult.getId();


    // when
    historyService.deleteUserOperationLogEntry(entryId);

    // then
    singleResult = historyService.createUserOperationLogQuery().entityType("Task").singleResult();
    assertNull(singleResult);
  }

  @Test
  public void shouldThrownExceptionWhenDeleteWithNoAuthenticatedTenant() {
    // given
    testRule.deployForTenant(TENANT_ONE, process);
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));

    runtimeService.startProcessInstanceByKey(PROCESS_NAME);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    taskService.complete(taskId);

    UserOperationLogEntry singleResult = historyService.createUserOperationLogQuery().entityType("Task").singleResult();
    String entryId = singleResult.getId();

    identityService.setAuthentication("aUserId1", null);

    // when/then
    assertThatThrownBy(() -> historyService.deleteUserOperationLogEntry(entryId))
    .isInstanceOf(ProcessEngineException.class)
    .hasMessageContaining("Cannot delete the user operation log entry '"
        + entryId +"' because it belongs to no authenticated tenant.");
  }

}
