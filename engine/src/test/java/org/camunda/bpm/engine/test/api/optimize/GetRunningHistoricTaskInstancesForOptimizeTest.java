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
package org.camunda.bpm.engine.test.api.optimize;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.OptimizeService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class GetRunningHistoricTaskInstancesForOptimizeTest {

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testHelper);

  private OptimizeService optimizeService;

  protected String userId = "test";

  private IdentityService identityService;
  private RuntimeService runtimeService;
  private AuthorizationService authorizationService;
  private TaskService taskService;


  @Before
  public void init() {
    ProcessEngineConfigurationImpl config =
      engineRule.getProcessEngineConfiguration();
    optimizeService = config.getOptimizeService();
    identityService = engineRule.getIdentityService();
    runtimeService = engineRule.getRuntimeService();
    authorizationService = engineRule.getAuthorizationService();
    taskService = engineRule.getTaskService();

    createUser(userId);
  }

  @After
  public void cleanUp() {
    for (User user : identityService.createUserQuery().list()) {
      identityService.deleteUser(user.getId());
    }
    for (Group group : identityService.createGroupQuery().list()) {
      identityService.deleteGroup(group.getId());
    }
    for (Authorization authorization : authorizationService.createAuthorizationQuery().list()) {
      authorizationService.deleteAuthorization(authorization.getId());
    }
    ClockUtil.reset();
  }

  @Test
  public void getRunningHistoricTaskInstances() {
     // given
    BpmnModelInstance simpleDefinition = Bpmn.createExecutableProcess("process")
      .startEvent("startEvent")
      .userTask("userTask")
        .name("task")
        .camundaAssignee(userId)
      .endEvent("endEvent")
      .done();
    testHelper.deploy(simpleDefinition);
    runtimeService.startProcessInstanceByKey("process");

    // when
    List<HistoricTaskInstance> runningHistoricTaskInstances =
      optimizeService.getRunningHistoricTaskInstances(null, null, 10);

    // then
    assertThat(runningHistoricTaskInstances.size(), is(1));
    assertThatTasksHaveAllImportantInformation(runningHistoricTaskInstances.get(0));
  }

  @Test
  public void startedAfterParameterWorks() {
     // given
    BpmnModelInstance simpleDefinition = Bpmn.createExecutableProcess("process")
      .startEvent()
      .userTask()
      .endEvent()
      .done();
    testHelper.deploy(simpleDefinition);
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    engineRule.getRuntimeService().startProcessInstanceByKey("process");

    Date nowPlus2Seconds = new Date(now.getTime() + 2000L);
    ClockUtil.setCurrentTime(nowPlus2Seconds);
    ProcessInstance processInstance2 = engineRule.getRuntimeService().startProcessInstanceByKey("process");

    // when
    List<HistoricTaskInstance> runningHistoricTaskInstances =
      optimizeService.getRunningHistoricTaskInstances(now, null, 10);

    // then
    assertThat(runningHistoricTaskInstances.size(), is(1));
    assertThat(runningHistoricTaskInstances.get(0).getProcessInstanceId(), is(processInstance2.getId()));
  }

  @Test
  public void startedAtParameterWorks() {
    // given
    BpmnModelInstance simpleDefinition = Bpmn.createExecutableProcess("process")
      .startEvent()
      .userTask()
      .endEvent()
      .done();
    testHelper.deploy(simpleDefinition);
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    ProcessInstance processInstance1 = engineRule.getRuntimeService().startProcessInstanceByKey("process");

    Date nowPlus2Seconds = new Date(now.getTime() + 2000L);
    ClockUtil.setCurrentTime(nowPlus2Seconds);
    engineRule.getRuntimeService().startProcessInstanceByKey("process");

    // when
    List<HistoricTaskInstance> runningHistoricTaskInstances =                                               
      optimizeService.getRunningHistoricTaskInstances(null, now, 10);

    // then
    assertThat(runningHistoricTaskInstances.size(), is(1));
    assertThat(runningHistoricTaskInstances.get(0).getProcessInstanceId(), is(processInstance1.getId()));
  }

  @Test
  public void startedAfterAndFinishedAtParameterWorks() {
    // given
    BpmnModelInstance simpleDefinition = Bpmn.createExecutableProcess("process")
      .startEvent()
      .userTask()
      .endEvent()
      .done();
    testHelper.deploy(simpleDefinition);
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    engineRule.getRuntimeService().startProcessInstanceByKey("process");

    Date nowPlus2Seconds = new Date(now.getTime() + 2000L);
    ClockUtil.setCurrentTime(nowPlus2Seconds);
    engineRule.getRuntimeService().startProcessInstanceByKey("process");

    // when
    List<HistoricTaskInstance> runningHistoricTaskInstances =
      optimizeService.getRunningHistoricTaskInstances(now, now, 10);

    // then
    assertThat(runningHistoricTaskInstances.size(), is(0));
  }

  @Test
  public void maxResultsParameterWorks() {
    // given
    BpmnModelInstance simpleDefinition = Bpmn.createExecutableProcess("process")
      .startEvent()
      .userTask()
      .endEvent()
      .done();
    testHelper.deploy(simpleDefinition);
    engineRule.getRuntimeService().startProcessInstanceByKey("process");
    engineRule.getRuntimeService().startProcessInstanceByKey("process");
    engineRule.getRuntimeService().startProcessInstanceByKey("process");
    engineRule.getRuntimeService().startProcessInstanceByKey("process");

    // when
    List<HistoricTaskInstance> runningHistoricTaskInstances =
      optimizeService.getRunningHistoricTaskInstances(pastDate(), null, 3);

    // then
    assertThat(runningHistoricTaskInstances.size(), is(3));
  }

  @Test
  public void resultIsSortedByStartTime() {
    // given
    BpmnModelInstance simpleDefinition = Bpmn.createExecutableProcess("process")
      .startEvent()
      .userTask()
      .endEvent()
      .done();
    testHelper.deploy(simpleDefinition);
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    ProcessInstance processInstance1 = engineRule.getRuntimeService().startProcessInstanceByKey("process");

    Date nowPlus2Seconds = new Date(now.getTime() + 2000L);
    ClockUtil.setCurrentTime(nowPlus2Seconds);
    ProcessInstance processInstance2 = engineRule.getRuntimeService().startProcessInstanceByKey("process");

    Date nowPlus4Seconds = new Date(now.getTime() + 4000L);
    ClockUtil.setCurrentTime(nowPlus4Seconds);
    ProcessInstance processInstance3 = engineRule.getRuntimeService().startProcessInstanceByKey("process");

    // when
    List<HistoricTaskInstance> runningHistoricTaskInstances =
      optimizeService.getRunningHistoricTaskInstances(pastDate(), null, 10);

    // then
    assertThat(runningHistoricTaskInstances.size(), is(3));
    assertThat(runningHistoricTaskInstances.get(0).getProcessInstanceId(), is(processInstance1.getId()));
    assertThat(runningHistoricTaskInstances.get(1).getProcessInstanceId(), is(processInstance2.getId()));
    assertThat(runningHistoricTaskInstances.get(2).getProcessInstanceId(), is(processInstance3.getId()));
  }

  @Test
  public void fetchOnlyRunningTasks() {
     // given
    BpmnModelInstance simpleDefinition = Bpmn.createExecutableProcess("process")
      .startEvent("startEvent")
      .userTask("userTask1")
      .userTask("userTask2")
      .userTask("userTask3")
      .endEvent()
      .done();
    testHelper.deploy(simpleDefinition);
    engineRule.getRuntimeService().startProcessInstanceByKey("process");
    completeAllUserTasks();

    // when
    List<HistoricTaskInstance> runningHistoricTaskInstances =
      optimizeService.getRunningHistoricTaskInstances(pastDate(), null, 10);

    // then
    assertThat(runningHistoricTaskInstances.size(), is(1));
    assertThat(runningHistoricTaskInstances.get(0).getTaskDefinitionKey(), is("userTask2"));
  }

  private Date pastDate() {
    return new Date(2L);
  }

  private void completeAllUserTasks() {
    List<Task> list = taskService.createTaskQuery().list();
    for (Task task : list) {
      taskService.claim(task.getId(), userId);
      taskService.complete(task.getId());
    }
  }

  protected void createUser(String userId) {
    User user = identityService.newUser(userId);
    identityService.saveUser(user);
  }

  private void assertThatTasksHaveAllImportantInformation(HistoricTaskInstance completedHistoricTaskInstance) {
    assertThat(completedHistoricTaskInstance, notNullValue());
    assertThat(completedHistoricTaskInstance.getId(), notNullValue());
    assertThat(completedHistoricTaskInstance.getTaskDefinitionKey(), is("userTask"));
    assertThat(completedHistoricTaskInstance.getName(), is("task"));
    assertThat(completedHistoricTaskInstance.getStartTime(), notNullValue());
    assertThat(completedHistoricTaskInstance.getEndTime(), nullValue());
    assertThat(completedHistoricTaskInstance.getProcessDefinitionKey(), is("process"));
    assertThat(completedHistoricTaskInstance.getProcessDefinitionId(), notNullValue());
    assertThat(completedHistoricTaskInstance.getAssignee(), is(userId));
  }

}
