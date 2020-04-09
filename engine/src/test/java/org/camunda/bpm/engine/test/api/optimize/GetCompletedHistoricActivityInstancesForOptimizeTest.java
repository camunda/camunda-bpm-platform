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

import static junit.framework.TestCase.assertTrue;
import static org.camunda.bpm.engine.delegate.ExecutionListener.EVENTNAME_START;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.OptimizeService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.util.ClockUtil;
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
public class GetCompletedHistoricActivityInstancesForOptimizeTest {

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testHelper);

  private OptimizeService optimizeService;

  protected String userId = "test";

  protected static final String VARIABLE_NAME = "aVariableName";
  protected static final String VARIABLE_VALUE = "aVariableValue";

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
  public void getCompletedHistoricActivityInstances() {
     // given
    BpmnModelInstance simpleDefinition = Bpmn.createExecutableProcess("process")
      .startEvent("startEvent")
        .name("start")
      .endEvent("endEvent")
        .name("end")
      .done();
    testHelper.deploy(simpleDefinition);
    runtimeService.startProcessInstanceByKey("process");

    // when
    List<HistoricActivityInstance> completedHistoricActivityInstances =
      optimizeService.getCompletedHistoricActivityInstances(pastDate(), null, 10);

    // then
    assertThat(completedHistoricActivityInstances.size(), is(2));
    assertThatActivitiesHaveAllImportantInformation(completedHistoricActivityInstances);
  }

  @Test
  public void fishedAfterParameterWorks() {
     // given
    BpmnModelInstance simpleDefinition = Bpmn.createExecutableProcess("process")
      .startEvent()
      .userTask("userTask")
      .endEvent("endEvent")
      .done();
    testHelper.deploy(simpleDefinition);
    Date now = new Date();
    Date nowPlus2Seconds = new Date(now.getTime() + 2000L);
    ClockUtil.setCurrentTime(now);
    engineRule.getRuntimeService().startProcessInstanceByKey("process");

    // when
    ClockUtil.setCurrentTime(nowPlus2Seconds);
    completeAllUserTasks();
    List<HistoricActivityInstance> completedHistoricActivityInstances =
      optimizeService.getCompletedHistoricActivityInstances(now, null, 10);

    // then
    Set<String> allowedActivityIds = new HashSet<>(Arrays.asList("userTask", "endEvent"));
    assertThat(completedHistoricActivityInstances.size(), is(2));
    assertTrue(allowedActivityIds.contains(completedHistoricActivityInstances.get(0).getActivityId()));
    assertTrue(allowedActivityIds.contains(completedHistoricActivityInstances.get(1).getActivityId()));
  }

  @Test
  public void fishedAtParameterWorks() {
     // given
    BpmnModelInstance simpleDefinition = Bpmn.createExecutableProcess("process")
      .startEvent("startEvent")
      .userTask("userTask")
      .endEvent("endEvent")
      .done();
    testHelper.deploy(simpleDefinition);
    Date now = new Date();
    Date nowPlus2Seconds = new Date(now.getTime() + 2000L);
    ClockUtil.setCurrentTime(now);
    engineRule.getRuntimeService().startProcessInstanceByKey("process");

    // when
    ClockUtil.setCurrentTime(nowPlus2Seconds);
    completeAllUserTasks();
    List<HistoricActivityInstance> completedHistoricActivityInstances =
      optimizeService.getCompletedHistoricActivityInstances(null, now, 10);

    // then
    assertThat(completedHistoricActivityInstances.size(), is(1));
    assertThat(completedHistoricActivityInstances.get(0).getActivityId(), is("startEvent"));
  }

  @Test
  public void fishedAfterAndFinishedAtParameterWorks() {
     // given
    BpmnModelInstance simpleDefinition = Bpmn.createExecutableProcess("process")
      .startEvent("startEvent")
      .userTask("userTask")
      .endEvent("endEvent")
      .done();
    testHelper.deploy(simpleDefinition);
    Date now = new Date();
    Date nowPlus2Seconds = new Date(now.getTime() + 2000L);
    ClockUtil.setCurrentTime(now);
    engineRule.getRuntimeService().startProcessInstanceByKey("process");

    // when
    ClockUtil.setCurrentTime(nowPlus2Seconds);
    completeAllUserTasks();
    List<HistoricActivityInstance> completedHistoricActivityInstances =
      optimizeService.getCompletedHistoricActivityInstances(now, now, 10);

    // then
    assertThat(completedHistoricActivityInstances.size(), is(0));
  }

  @Test
  public void maxResultsParameterWorks() {
     // given
    BpmnModelInstance simpleDefinition = Bpmn.createExecutableProcess("process")
      .startEvent("startEvent")
      .serviceTask()
        .camundaExpression("${true}")
      .serviceTask()
        .camundaExpression("${true}")
      .serviceTask()
        .camundaExpression("${true}")
      .serviceTask()
        .camundaExpression("${true}")
      .endEvent("endEvent")
      .done();
    testHelper.deploy(simpleDefinition);
    engineRule.getRuntimeService().startProcessInstanceByKey("process");

    // when
    List<HistoricActivityInstance> completedHistoricActivityInstances =
      optimizeService.getCompletedHistoricActivityInstances(pastDate(), null, 3);

    // then
    assertThat(completedHistoricActivityInstances.size(), is(3));
  }

  @Test
  public void resultIsSortedByEndTime() {
     // given
    BpmnModelInstance simpleDefinition = Bpmn.createExecutableProcess("process")
      .startEvent("startEvent")
      .serviceTask("ServiceTask1")
        .camundaExpression("${true}")
        .camundaExecutionListenerClass(EVENTNAME_START, ShiftTimeByOneMinuteListener.class.getName())
      .serviceTask("ServiceTask2")
        .camundaExpression("${true}")
        .camundaExecutionListenerClass(EVENTNAME_START, ShiftTimeByOneMinuteListener.class.getName())
      .serviceTask("ServiceTask3")
        .camundaExpression("${true}")
        .camundaExecutionListenerClass(EVENTNAME_START, ShiftTimeByOneMinuteListener.class.getName())
      .endEvent("endEvent")
        .camundaExecutionListenerClass(EVENTNAME_START, ShiftTimeByOneMinuteListener.class.getName())
      .done();
    testHelper.deploy(simpleDefinition);
    ClockUtil.setCurrentTime(new Date());
    engineRule.getRuntimeService().startProcessInstanceByKey("process");
    ClockUtil.reset();

    // when
    List<HistoricActivityInstance> completedHistoricActivityInstances =
      optimizeService.getCompletedHistoricActivityInstances(pastDate(), null, 4);

    // then
    assertThat(completedHistoricActivityInstances.size(), is(4));
    assertThat(completedHistoricActivityInstances.get(0).getActivityId(), is("startEvent"));
    assertThat(completedHistoricActivityInstances.get(1).getActivityId(), is("ServiceTask1"));
    assertThat(completedHistoricActivityInstances.get(2).getActivityId(), is("ServiceTask2"));
    assertThat(completedHistoricActivityInstances.get(3).getActivityId(), is("ServiceTask3"));
  }

  @Test
  public void fetchOnlyCompletedActivities() {
     // given
    BpmnModelInstance simpleDefinition = Bpmn.createExecutableProcess("process")
      .startEvent("startEvent")
      .userTask()
      .endEvent()
      .done();
    testHelper.deploy(simpleDefinition);
    engineRule.getRuntimeService().startProcessInstanceByKey("process");

    // when
    List<HistoricActivityInstance> completedHistoricActivityInstances =
      optimizeService.getCompletedHistoricActivityInstances(pastDate(), null, 10);

    // then
    assertThat(completedHistoricActivityInstances.size(), is(1));
    assertThat(completedHistoricActivityInstances.get(0).getActivityId(), is("startEvent"));
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

  private void assertThatActivitiesHaveAllImportantInformation(List<HistoricActivityInstance> completedHistoricActivityInstances) {
    HistoricActivityInstance startEvent = null, endEvent = null;
    for (HistoricActivityInstance completedHistoricActivityInstance : completedHistoricActivityInstances) {
      if (completedHistoricActivityInstance.getActivityId().equals("startEvent")) {
        startEvent = completedHistoricActivityInstance;
      } else if (completedHistoricActivityInstance.getActivityId().equals("endEvent")) {
        endEvent = completedHistoricActivityInstance;
      }
    }
    assertThat(startEvent, notNullValue());
    assertThat(startEvent.getActivityName(), is("start"));
    assertThat(startEvent.getActivityType(), is("startEvent"));
    assertThat(startEvent.getStartTime(), notNullValue());
    assertThat(startEvent.getEndTime(), notNullValue());
    assertThat(startEvent.getProcessDefinitionKey(), is("process"));
    assertThat(startEvent.getProcessDefinitionId(), notNullValue());
    assertThat(((HistoryEvent) startEvent).getSequenceCounter(), notNullValue());

    assertThat(endEvent, notNullValue());
    assertThat(endEvent.getActivityName(), is("end"));
    assertThat(endEvent.getActivityType(), is("noneEndEvent"));
    assertThat(endEvent.getStartTime(), notNullValue());
    assertThat(endEvent.getEndTime(), notNullValue());
    assertThat(endEvent.getProcessDefinitionKey(), is("process"));
    assertThat(endEvent.getProcessDefinitionId(), notNullValue());
    assertThat(((HistoryEvent) endEvent).getSequenceCounter(), notNullValue());
  }

}
