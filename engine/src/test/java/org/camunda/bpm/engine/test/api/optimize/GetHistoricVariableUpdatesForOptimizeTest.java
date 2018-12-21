/*
 * Copyright © 2013-2018 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.history.HistoricVariableUpdate;
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
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;



@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class GetHistoricVariableUpdatesForOptimizeTest {

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
  public void getHistoricVariableUpdates() {
     // given
    BpmnModelInstance simpleDefinition = Bpmn.createExecutableProcess("process")
      .startEvent()
      .endEvent()
      .done();
    testHelper.deploy(simpleDefinition);
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("stringVar", "foo");
    runtimeService.startProcessInstanceByKey("process", variables);

    // when
    List<HistoricVariableUpdate> historicVariableUpdates =
      optimizeService.getHistoricVariableUpdates(new Date(1L), null, 10);

    // then
    assertThat(historicVariableUpdates.size(), is(1));
    assertThatUpdateHasAllImportantInformation(historicVariableUpdates.get(0));
  }

  @Test
  public void occurredAfterParameterWorks() {
     // given
    BpmnModelInstance simpleDefinition = Bpmn.createExecutableProcess("process")
      .startEvent()
      .endEvent()
      .done();
    testHelper.deploy(simpleDefinition);
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("stringVar", "value1");
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    runtimeService.startProcessInstanceByKey("process", variables);
    Date nowPlus2Seconds = new Date(new Date().getTime() + 2000L);
    ClockUtil.setCurrentTime(nowPlus2Seconds);
    variables.put("stringVar", "value2");
    runtimeService.startProcessInstanceByKey("process", variables);

    // when
    List<HistoricVariableUpdate> variableUpdates =
      optimizeService.getHistoricVariableUpdates(now, null, 10);

    // then
    assertThat(variableUpdates.size(), is(1));
    assertThat(variableUpdates.get(0).getValue().toString(), is("value2"));
  }

  @Test
  public void occurredAtParameterWorks() {
     // given
    BpmnModelInstance simpleDefinition = Bpmn.createExecutableProcess("process")
      .startEvent()
      .endEvent()
      .done();
    testHelper.deploy(simpleDefinition);
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("stringVar", "value1");
    runtimeService.startProcessInstanceByKey("process", variables);
    Date nowPlus2Seconds = new Date(now.getTime() + 2000L);
    ClockUtil.setCurrentTime(nowPlus2Seconds);
    variables.put("stringVar", "value2");
    runtimeService.startProcessInstanceByKey("process", variables);

    // when
    List<HistoricVariableUpdate> variableUpdates =
      optimizeService.getHistoricVariableUpdates(null, now, 10);

    // then
    assertThat(variableUpdates.size(), is(1));
    assertThat(variableUpdates.get(0).getValue().toString(), is("value1"));
  }

  @Test
  public void occurredAfterAndOccurredAtParameterWorks() {
     // given
    BpmnModelInstance simpleDefinition = Bpmn.createExecutableProcess("process")
      .startEvent()
      .endEvent()
      .done();
    testHelper.deploy(simpleDefinition);
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("stringVar", "value1");
    runtimeService.startProcessInstanceByKey("process", variables);
    Date nowPlus2Seconds = new Date(now.getTime() + 2000L);
    ClockUtil.setCurrentTime(nowPlus2Seconds);
    variables.put("stringVar", "value2");
    runtimeService.startProcessInstanceByKey("process", variables);

    // when
    List<HistoricVariableUpdate> variableUpdates =
      optimizeService.getHistoricVariableUpdates(now, now, 10);

    // then
    assertThat(variableUpdates.size(), is(0));
  }

  @Test
  public void maxResultsParameterWorks() {
     // given
    BpmnModelInstance simpleDefinition = Bpmn.createExecutableProcess("process")
      .startEvent()
      .endEvent()
      .done();
    testHelper.deploy(simpleDefinition);
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("stringVar", "value1");
    variables.put("integerVar", 1);
    runtimeService.startProcessInstanceByKey("process", variables);
    runtimeService.startProcessInstanceByKey("process", variables);
    runtimeService.startProcessInstanceByKey("process", variables);
    runtimeService.startProcessInstanceByKey("process", variables);
    runtimeService.startProcessInstanceByKey("process", variables);

    // when
    List<HistoricVariableUpdate> variableUpdates =
      optimizeService.getHistoricVariableUpdates(pastDate(), null, 3);

    // then
    assertThat(variableUpdates.size(), is(3));
  }

  @Test
  public void resultIsSortedByTime() {
     // given
    BpmnModelInstance simpleDefinition = Bpmn.createExecutableProcess("process")
      .startEvent()
      .endEvent()
      .done();
    testHelper.deploy(simpleDefinition);
    Date now = new Date();
    Date nowPlus1Second = new Date(now.getTime() + 1000L);
    ClockUtil.setCurrentTime(nowPlus1Second);
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("var1", "value1");
      runtimeService.startProcessInstanceByKey("process", variables);
    Date nowPlus2Seconds = new Date(now.getTime() + 2000L);
    ClockUtil.setCurrentTime(nowPlus2Seconds);
    variables.clear();
    variables.put("var2", "value2");
      runtimeService.startProcessInstanceByKey("process", variables);
    Date nowPlus4Seconds = new Date(nowPlus2Seconds.getTime() + 2000L);
    ClockUtil.setCurrentTime(nowPlus4Seconds);
    variables.clear();
    variables.put("var3", "value3");
      runtimeService.startProcessInstanceByKey("process", variables);

    // when
    List<HistoricVariableUpdate> variableUpdates =
      optimizeService.getHistoricVariableUpdates(now, null, 10);

    // then
    assertThat(variableUpdates.size(), is(3));
    assertThat(variableUpdates.get(0).getVariableName(), is("var1"));
    assertThat(variableUpdates.get(1).getVariableName(), is("var2"));
    assertThat(variableUpdates.get(2).getVariableName(), is("var3"));
  }

  @Test
  public void fetchOnlyVariableUpdates() {
     // given
    BpmnModelInstance simpleDefinition = Bpmn.createExecutableProcess("process")
      .startEvent()
      .userTask()
      .endEvent()
      .done();
    testHelper.deploy(simpleDefinition);
    runtimeService.startProcessInstanceByKey("process");

    Task task = taskService.createTaskQuery().singleResult();
    Map<String, String> formFields = new HashMap<String, String>();
    formFields.put("var", "foo");
    engineRule.getFormService().submitTaskFormData(task.getId(), formFields);
    long detailCount = engineRule.getHistoryService().createHistoricDetailQuery().count();
    assertThat(detailCount, is(2L)); // variable update + form property

    // when
    List<HistoricVariableUpdate> variableUpdates =
      optimizeService.getHistoricVariableUpdates(pastDate(), null, 10);

    // then
    assertThat(variableUpdates.size(), is(1));
  }

  @Test
  public void getHistoricVariableByteArrayUpdates() {
     // given
    BpmnModelInstance simpleDefinition = Bpmn.createExecutableProcess("process")
      .startEvent()
      .endEvent()
      .done();
    testHelper.deploy(simpleDefinition);

    List<String> serializable = new ArrayList<String>();
    serializable.add("one");
    serializable.add("two");
    serializable.add("three");

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("var", serializable);

    runtimeService.startProcessInstanceByKey("process", variables);
    runtimeService.startProcessInstanceByKey("process", variables);
    runtimeService.startProcessInstanceByKey("process", variables);
    runtimeService.startProcessInstanceByKey("process", variables);

    // when
    List<HistoricVariableUpdate> historicVariableUpdates =
      optimizeService.getHistoricVariableUpdates(new Date(1L), null, 10);

    // then
    assertThat(historicVariableUpdates.size(), is(4));

    for (HistoricVariableUpdate variableUpdate : historicVariableUpdates) {
      ObjectValue typedValue = (ObjectValue) variableUpdate.getTypedValue();
      assertThat(typedValue.isDeserialized(), is(false));
      assertThat(typedValue.getValueSerialized(), notNullValue());
    }

  }

  private Date pastDate() {
    return new Date(2L);
  }

  protected void createUser(String userId) {
    User user = identityService.newUser(userId);
    identityService.saveUser(user);
  }

  private void assertThatUpdateHasAllImportantInformation(HistoricVariableUpdate variableUpdate) {
    assertThat(variableUpdate, notNullValue());
    assertThat(variableUpdate.getId(), notNullValue());
    assertThat(variableUpdate.getProcessDefinitionKey(), is("process"));
    assertThat(variableUpdate.getProcessDefinitionId(), notNullValue());
    assertThat(variableUpdate.getVariableName(), is("stringVar"));
    assertThat(variableUpdate.getValue().toString(), is("foo"));
    assertThat(variableUpdate.getTypeName(), is("string"));
    assertThat(variableUpdate.getTime(), notNullValue());
  }

}
