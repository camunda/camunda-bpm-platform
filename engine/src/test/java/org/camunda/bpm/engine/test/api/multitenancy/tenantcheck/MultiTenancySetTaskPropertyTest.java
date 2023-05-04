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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.MethodInvocation;
import org.camunda.bpm.engine.test.util.ObjectProperty;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.test.util.TriConsumer;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class MultiTenancySetTaskPropertyTest {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String PROCESS_DEFINITION_KEY = "oneTaskProcess";

  protected static final BpmnModelInstance ONE_TASK_PROCESS = Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
      .startEvent()
      .userTask()
      .endEvent()
      .done();

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  // populated by data in constructor
  protected final String operationName;
  protected final TriConsumer<TaskService, String, Object> operation;
  protected final Object value;
  protected final String taskQueryBuilderMethodName;

  // initialized during @Before
  protected TaskService taskService;
  protected IdentityService identityService;
  protected String taskId;
  protected Task task;

  public MultiTenancySetTaskPropertyTest(String operationName,
                                         TriConsumer<TaskService, String, Object> operation,
                                         Object value,
                                         String taskQueryBuilderMethodName) {
    this.operationName = operationName;
    this.operation = operation;
    this.value = value;
    this.taskQueryBuilderMethodName = taskQueryBuilderMethodName;
  }

  @Parameters(name = "{0}")
  public static List<Object[]> data() {
    TriConsumer<TaskService, String, Object> setName = (taskService, taskId, value) -> taskService.setName(taskId, (String) value);
    TriConsumer<TaskService, String, Object> setDescription = (taskService, taskId, value) -> taskService.setDescription(taskId, (String) value);
    TriConsumer<TaskService, String, Object> setDueDate = (taskService, taskId, value) -> taskService.setDueDate(taskId, (Date) value);
    TriConsumer<TaskService, String, Object> setFollowUpDate = (taskService, taskId, value) -> taskService.setFollowUpDate(taskId, (Date) value);

    return Arrays.asList(new Object[][] {
        { "setName", setName, "name", "taskName" },
        { "setDescription", setDescription, "description", "taskDescription" },
        { "setDueDate", setDueDate, DateTime.now().toDate(), "dueDate" },
        { "setFollowUpDate", setFollowUpDate, DateTime.now().toDate(), "followUpDate" }
    });
  }

  @Before
  public void init() {
    testRule.deployForTenant(TENANT_ONE, ONE_TASK_PROCESS);

    engineRule.getRuntimeService().startProcessInstanceByKey(PROCESS_DEFINITION_KEY);

    task = engineRule.getTaskService().createTaskQuery().singleResult();
    taskId = task.getId();

    taskService = engineRule.getTaskService();
    identityService = engineRule.getIdentityService();
  }

  @Test
  public void setOperationForTaskWithAuthenticatedTenant() {
    // given
    identityService.setAuthentication("aUserId", null, Collections.singletonList(TENANT_ONE));

    // when
    operation.accept(taskService, taskId, value);

    // then
    String propertyName = ObjectProperty.ofSetterMethod(taskService, operationName).getPropertyName();
    TaskQuery query = taskService.createTaskQuery().taskId(taskId);
    query = withTaskCriteria(query, taskQueryBuilderMethodName, value);

    assertThat(query.count()).isEqualTo(1L);
  }

  @Test
  public void setOperationForTaskWithNoAuthenticatedTenant() {
    // given
    identityService.setAuthentication("aUserId", null);

    // when/then
    assertThatThrownBy(() -> taskService.setPriority(task.getId(), 1)).isInstanceOf(ProcessEngineException.class)
        .hasMessageContaining(
            "Cannot assign the task '" + task.getId() + "' because it belongs to no authenticated tenant.");

  }

  @Test
  public void setOperationForTaskWithDisabledTenantCheck() {
    // given
    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);

    // when
    taskService.setPriority(task.getId(), 1);

    // then
    assertThat(taskService.createTaskQuery().taskPriority(1).taskId(task.getId()).count()).isEqualTo(1L);
  }

  private TaskQuery withTaskCriteria(TaskQuery taskQuery, String methodName, Object propertyValue) {
    return (TaskQuery) MethodInvocation.of(taskQuery, methodName, new Object[] { propertyValue }).invoke();
  }
}