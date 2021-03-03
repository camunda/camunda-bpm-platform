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
package org.camunda.bpm.engine.test.standalone.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstanceQuery;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Test;

/**
 * @author Thorben Lindhauer
 *
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
public class HistoricTaskInstanceQueryTest extends PluggableProcessEngineTest {

  protected static final String VARIABLE_NAME = "variableName";
  protected static final String VARIABLE_NAME_LC = VARIABLE_NAME.toLowerCase();
  protected static final String VARIABLE_VALUE = "variableValue";
  protected static final String VARIABLE_VALUE_LC = VARIABLE_VALUE.toLowerCase();
  protected static final String VARIABLE_VALUE_LC_LIKE = "%" + VARIABLE_VALUE_LC.substring(2, 10) + "%";
  protected static final String VARIABLE_VALUE_NE = "nonExistent";
  protected static Map<String, Object> VARIABLES = new HashMap<>();
  static {
    VARIABLES.put(VARIABLE_NAME, VARIABLE_VALUE);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  @Test
  public void testProcessVariableValueEqualsNumber() throws Exception {
    // long
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 123L));

    // non-matching long
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 12345L));

    // short
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", (short) 123));

    // double
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 123.0d));

    // integer
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 123));

    // untyped null (should not match)
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", null));

    // typed null (should not match)
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", Variables.longValue(null)));

    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", "123"));

    assertEquals(4, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("var", Variables.numberValue(123)).count());
    assertEquals(4, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("var", Variables.numberValue(123L)).count());
    assertEquals(4, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("var", Variables.numberValue(123.0d)).count());
    assertEquals(4, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("var", Variables.numberValue((short) 123)).count());

    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("var", Variables.numberValue(null)).count());

    assertEquals(8, historyService.createHistoricTaskInstanceQuery().processVariableValueNotEquals("var", 999L).count());
    assertEquals(8, historyService.createHistoricTaskInstanceQuery().processVariableValueNotEquals("var",  (short) 999).count());
    assertEquals(8, historyService.createHistoricTaskInstanceQuery().processVariableValueNotEquals("var", 999).count());
    assertEquals(8, historyService.createHistoricTaskInstanceQuery().processVariableValueNotEquals("var", "999").count());
    assertEquals(8, historyService.createHistoricTaskInstanceQuery().processVariableValueNotEquals("var", false).count());

  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  @Test
  public void testProcessVariableValueLike() throws Exception {
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
            Collections.<String, Object>singletonMap("requester", "vahid alizadeh"));

    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueLike("requester", "vahid%").count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueLike("requester", "%alizadeh").count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueLike("requester", "%ali%").count());

    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processVariableValueLike("requester", "requester%").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processVariableValueLike("requester", "%ali").count());

    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processVariableValueLike("requester", "vahid").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processVariableValueLike("nonExistingVar", "string%").count());

    // test with null value
    try {
      historyService.createHistoricTaskInstanceQuery().processVariableValueLike("requester", null).count();
      fail("expected exception");
    } catch (final ProcessEngineException e) {/*OK*/}
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  @Test
  public void testProcessVariableValueNotLike() throws Exception {
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
            Collections.<String, Object>singletonMap("requester", "vahid alizadeh"));

    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processVariableValueNotLike("requester", "vahid%").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processVariableValueNotLike("requester", "%alizadeh").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processVariableValueNotLike("requester", "%ali%").count());

    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueNotLike("requester", "requester%").count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueNotLike("requester", "%ali").count());

    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueNotLike("requester", "vahid").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processVariableValueNotLike("nonExistingVar", "string%").count());

    // test with null value
    assertThatThrownBy(() -> historyService.createHistoricTaskInstanceQuery().processVariableValueNotLike("requester", null).count())
      .isInstanceOf(ProcessEngineException.class);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  @Test
  public void testProcessVariableValueGreaterThan() throws Exception {
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
            Collections.<String, Object>singletonMap("requestNumber", 123));

    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueGreaterThan("requestNumber", 122).count());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  @Test
  public void testProcessVariableValueGreaterThanOrEqual() throws Exception {
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
            Collections.<String, Object>singletonMap("requestNumber", 123));

    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueGreaterThanOrEquals("requestNumber", 122).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueGreaterThanOrEquals("requestNumber", 123).count());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  @Test
  public void testProcessVariableValueLessThan() throws Exception {
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
            Collections.<String, Object>singletonMap("requestNumber", 123));

    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueLessThan("requestNumber", 124).count());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  @Test
  public void testProcessVariableValueLessThanOrEqual() throws Exception {
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
            Collections.<String, Object>singletonMap("requestNumber", 123));

    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueLessThanOrEquals("requestNumber", 123).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueLessThanOrEquals("requestNumber", 124).count());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testProcessVariableNameEqualsIgnoreCase() {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess", VARIABLES);

    // when
    List<HistoricTaskInstance> eq = queryNameIgnoreCase().processVariableValueEquals(VARIABLE_NAME, VARIABLE_VALUE).list();
    List<HistoricTaskInstance> eqNameLC = queryNameIgnoreCase().processVariableValueEquals(VARIABLE_NAME_LC, VARIABLE_VALUE).list();
    List<HistoricTaskInstance> eqValueLC = queryNameIgnoreCase().processVariableValueEquals(VARIABLE_NAME, VARIABLE_VALUE_LC).list();
    List<HistoricTaskInstance> eqNameValueLC = queryNameIgnoreCase().processVariableValueEquals(VARIABLE_NAME_LC, VARIABLE_VALUE_LC).list();

    // then
    assertThatListContainsOnlyExpectedElement(eq, instance);
    assertThatListContainsOnlyExpectedElement(eqNameLC, instance);
    assertThat(eqValueLC).isEmpty();
    assertThat(eqNameValueLC).isEmpty();
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testProcessVariableNameNotEqualsIgnoreCase() {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess", VARIABLES);
    // when
    List<HistoricTaskInstance> neq = queryNameIgnoreCase().processVariableValueNotEquals(VARIABLE_NAME, VARIABLE_VALUE).list();
    List<HistoricTaskInstance> neqNameLC = queryNameIgnoreCase().processVariableValueNotEquals(VARIABLE_NAME_LC, VARIABLE_VALUE).list();
    List<HistoricTaskInstance> neqValueNE = queryNameIgnoreCase().processVariableValueNotEquals(VARIABLE_NAME, VARIABLE_VALUE_NE).list();
    List<HistoricTaskInstance> neqNameLCValueNE = queryNameIgnoreCase().processVariableValueNotEquals(VARIABLE_NAME_LC, VARIABLE_VALUE_NE).list();

    // then
    assertThat(neq).isEmpty();
    assertThat(neqNameLC).isEmpty();
    assertThatListContainsOnlyExpectedElement(neqValueNE, instance);
    assertThatListContainsOnlyExpectedElement(neqNameLCValueNE, instance);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testProcessVariableValueEqualsIgnoreCase() {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess", VARIABLES);
    // when
    List<HistoricTaskInstance> eq = queryValueIgnoreCase().processVariableValueEquals(VARIABLE_NAME, VARIABLE_VALUE).list();
    List<HistoricTaskInstance> eqNameLC = queryValueIgnoreCase().processVariableValueEquals(VARIABLE_NAME_LC, VARIABLE_VALUE).list();
    List<HistoricTaskInstance> eqValueLC = queryValueIgnoreCase().processVariableValueEquals(VARIABLE_NAME, VARIABLE_VALUE_LC).list();
    List<HistoricTaskInstance> eqNameValueLC = queryValueIgnoreCase().processVariableValueEquals(VARIABLE_NAME_LC, VARIABLE_VALUE_LC).list();

    // then
    assertThatListContainsOnlyExpectedElement(eq, instance);
    assertThat(eqNameLC).isEmpty();
    assertThatListContainsOnlyExpectedElement(eqValueLC, instance);
    assertThat(eqNameValueLC).isEmpty();
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testProcessVariableValueNotEqualsIgnoreCase() {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess", VARIABLES);
    // when
    List<HistoricTaskInstance> neq = queryValueIgnoreCase().processVariableValueNotEquals(VARIABLE_NAME, VARIABLE_VALUE).list();
    List<HistoricTaskInstance> neqNameLC = queryValueIgnoreCase().processVariableValueNotEquals(VARIABLE_NAME_LC, VARIABLE_VALUE).list();
    List<HistoricTaskInstance> neqValueNE = queryValueIgnoreCase().processVariableValueNotEquals(VARIABLE_NAME, VARIABLE_VALUE_NE).list();
    List<HistoricTaskInstance> neqNameLCValueNE = queryValueIgnoreCase().processVariableValueNotEquals(VARIABLE_NAME_LC, VARIABLE_VALUE_NE).list();

    // then
    assertThat(neq).isEmpty();
    assertThat(neqNameLC).isEmpty();
    assertThatListContainsOnlyExpectedElement(neqValueNE, instance);
    assertThat(neqNameLCValueNE).isEmpty();
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testProcessVariableValueLikeIgnoreCase() {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess", VARIABLES);
    // when
    List<HistoricTaskInstance> like = queryNameValueIgnoreCase().processVariableValueLike(VARIABLE_NAME, VARIABLE_VALUE).list();
    List<HistoricTaskInstance> likeValueLC = queryValueIgnoreCase().processVariableValueLike(VARIABLE_NAME, VARIABLE_VALUE_LC_LIKE).list();

    // then
    assertThatListContainsOnlyExpectedElement(like, instance);
    assertThatListContainsOnlyExpectedElement(likeValueLC, instance);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testProcessVariableValueNotLikeIgnoreCase() {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess", VARIABLES);
    // when
    List<HistoricTaskInstance> notLike = queryValueIgnoreCase().processVariableValueNotLike(VARIABLE_NAME, VARIABLE_VALUE).list();
    List<HistoricTaskInstance> notLikeValueNE = queryValueIgnoreCase().processVariableValueNotLike(VARIABLE_NAME, VARIABLE_VALUE_NE).list();
    List<HistoricTaskInstance> notLikeNameLC = queryValueIgnoreCase().processVariableValueNotLike(VARIABLE_NAME_LC, VARIABLE_VALUE).list();
    List<HistoricTaskInstance> notLikeNameLCValueNE = queryValueIgnoreCase().processVariableValueNotLike(VARIABLE_NAME_LC, VARIABLE_VALUE_NE).list();

    // then
    assertThat(notLike).isEmpty();
    assertThatListContainsOnlyExpectedElement(notLikeValueNE, instance);
    assertThat(notLikeNameLC).isEmpty();
    assertThat(notLikeNameLCValueNE).isEmpty();
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testProcessVariableNameAndValueEqualsIgnoreCase() {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess", VARIABLES);
    // when
    List<HistoricTaskInstance> eq = queryNameValueIgnoreCase().processVariableValueEquals(VARIABLE_NAME, VARIABLE_VALUE).list();
    List<HistoricTaskInstance> eqNameLC = queryNameValueIgnoreCase().processVariableValueEquals(VARIABLE_NAME_LC, VARIABLE_VALUE).list();
    List<HistoricTaskInstance> eqValueLC = queryNameValueIgnoreCase().processVariableValueEquals(VARIABLE_NAME, VARIABLE_VALUE_LC).list();
    List<HistoricTaskInstance> eqValueNE = queryNameValueIgnoreCase().processVariableValueEquals(VARIABLE_NAME, VARIABLE_VALUE_NE).list();
    List<HistoricTaskInstance> eqNameValueLC = queryNameValueIgnoreCase().processVariableValueEquals(VARIABLE_NAME_LC, VARIABLE_VALUE_LC).list();
    List<HistoricTaskInstance> eqNameLCValueNE = queryNameValueIgnoreCase().processVariableValueEquals(VARIABLE_NAME_LC, VARIABLE_VALUE_NE).list();

    // then
    assertThatListContainsOnlyExpectedElement(eq, instance);
    assertThatListContainsOnlyExpectedElement(eqNameLC, instance);
    assertThatListContainsOnlyExpectedElement(eqValueLC, instance);
    assertThat(eqValueNE).isEmpty();
    assertThatListContainsOnlyExpectedElement(eqNameValueLC, instance);
    assertThat(eqNameLCValueNE).isEmpty();
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testProcessVariableNameAndValueNotEqualsIgnoreCase() {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess", VARIABLES);
    // when
    List<HistoricTaskInstance> neq = queryNameValueIgnoreCase().processVariableValueNotEquals(VARIABLE_NAME, VARIABLE_VALUE).list();
    List<HistoricTaskInstance> neqNameLC = queryNameValueIgnoreCase().processVariableValueNotEquals(VARIABLE_NAME_LC, VARIABLE_VALUE).list();
    List<HistoricTaskInstance> neqValueLC = queryNameValueIgnoreCase().processVariableValueNotEquals(VARIABLE_NAME, VARIABLE_VALUE_LC).list();
    List<HistoricTaskInstance> neqValueNE = queryNameValueIgnoreCase().processVariableValueNotEquals(VARIABLE_NAME, VARIABLE_VALUE_NE).list();
    List<HistoricTaskInstance> neqNameValueLC = queryNameValueIgnoreCase().processVariableValueNotEquals(VARIABLE_NAME_LC, VARIABLE_VALUE_LC).list();
    List<HistoricTaskInstance> neqNameLCValueNE = queryNameValueIgnoreCase().processVariableValueNotEquals(VARIABLE_NAME_LC, VARIABLE_VALUE_NE).list();

    // then
    assertThat(neq).isEmpty();
    assertThat(neqNameLC).isEmpty();
    assertThat(neqValueLC).isEmpty();
    assertThatListContainsOnlyExpectedElement(neqValueNE, instance);
    assertThat(neqNameValueLC).isEmpty();
    assertThatListContainsOnlyExpectedElement(neqNameLCValueNE, instance);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  @Test
  public void testTaskVariableValueEqualsNumber() throws Exception {
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    List<Task> tasks = taskService.createTaskQuery().processDefinitionKey("oneTaskProcess").list();
    assertEquals(8, tasks.size());
    taskService.setVariableLocal(tasks.get(0).getId(), "var", 123L);
    taskService.setVariableLocal(tasks.get(1).getId(), "var", 12345L);
    taskService.setVariableLocal(tasks.get(2).getId(), "var", (short) 123);
    taskService.setVariableLocal(tasks.get(3).getId(), "var", 123.0d);
    taskService.setVariableLocal(tasks.get(4).getId(), "var", 123);
    taskService.setVariableLocal(tasks.get(5).getId(), "var", null);
    taskService.setVariableLocal(tasks.get(6).getId(), "var", Variables.longValue(null));
    taskService.setVariableLocal(tasks.get(7).getId(), "var", "123");

    assertEquals(4, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("var", Variables.numberValue(123)).count());
    assertEquals(4, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("var", Variables.numberValue(123L)).count());
    assertEquals(4, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("var", Variables.numberValue(123.0d)).count());
    assertEquals(4, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("var", Variables.numberValue((short) 123)).count());

    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("var", Variables.numberValue(null)).count());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  @Test
  public void testTaskVariableValueEqualsNumberIgnoreCase() {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().singleResult();
    taskService.setVariablesLocal(task.getId(), VARIABLES);

    // when
    List<HistoricTaskInstance> eq =  queryValueIgnoreCase().taskVariableValueEquals(VARIABLE_NAME, VARIABLE_VALUE).list();
    List<HistoricTaskInstance> eqNameLC = queryValueIgnoreCase().taskVariableValueEquals(VARIABLE_NAME_LC, VARIABLE_VALUE).list();
    List<HistoricTaskInstance> eqValueLC = queryValueIgnoreCase().taskVariableValueEquals(VARIABLE_NAME, VARIABLE_VALUE_LC).list();
    List<HistoricTaskInstance> eqNameValueLC = queryValueIgnoreCase().taskVariableValueEquals(VARIABLE_NAME_LC, VARIABLE_VALUE_LC).list();

    // then
    assertThatListContainsOnlyExpectedElement(eq, instance);
    assertThat(eqNameLC).isEmpty();
    assertThatListContainsOnlyExpectedElement(eqValueLC, instance);
    assertThat(eqNameValueLC).isEmpty();
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testTaskVariableNameEqualsIgnoreCase() {
 // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().singleResult();
    taskService.setVariablesLocal(task.getId(), VARIABLES);

    // when
    List<HistoricTaskInstance> eq = queryNameIgnoreCase().taskVariableValueEquals(VARIABLE_NAME, VARIABLE_VALUE).list();
    List<HistoricTaskInstance> eqNameLC = queryNameIgnoreCase().taskVariableValueEquals(VARIABLE_NAME_LC, VARIABLE_VALUE).list();
    List<HistoricTaskInstance> eqValueLC = queryNameIgnoreCase().taskVariableValueEquals(VARIABLE_NAME, VARIABLE_VALUE_LC).list();
    List<HistoricTaskInstance> eqNameValueLC = queryNameIgnoreCase().taskVariableValueEquals(VARIABLE_NAME_LC, VARIABLE_VALUE_LC).list();

    // then
    assertThatListContainsOnlyExpectedElement(eq, instance);
    assertThatListContainsOnlyExpectedElement(eqNameLC, instance);
    assertThat(eqValueLC).isEmpty();
    assertThat(eqNameValueLC).isEmpty();
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testTaskVariableNameAndValueEqualsIgnoreCase() {
 // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().singleResult();
    taskService.setVariablesLocal(task.getId(), VARIABLES);

    // when
    List<HistoricTaskInstance> eq = queryNameValueIgnoreCase().taskVariableValueEquals(VARIABLE_NAME, VARIABLE_VALUE).list();
    List<HistoricTaskInstance> eqNameLC = queryNameValueIgnoreCase().taskVariableValueEquals(VARIABLE_NAME_LC, VARIABLE_VALUE).list();
    List<HistoricTaskInstance> eqValueLC = queryNameValueIgnoreCase().taskVariableValueEquals(VARIABLE_NAME, VARIABLE_VALUE_LC).list();
    List<HistoricTaskInstance> eqValueNE = queryNameValueIgnoreCase().taskVariableValueEquals(VARIABLE_NAME, VARIABLE_VALUE_NE).list();
    List<HistoricTaskInstance> eqNameValueLC = queryNameValueIgnoreCase().taskVariableValueEquals(VARIABLE_NAME_LC, VARIABLE_VALUE_LC).list();
    List<HistoricTaskInstance> eqNameLCValueNE = queryNameValueIgnoreCase().taskVariableValueEquals(VARIABLE_NAME_LC, VARIABLE_VALUE_NE).list();

    // then
    assertThatListContainsOnlyExpectedElement(eq, instance);
    assertThatListContainsOnlyExpectedElement(eqNameLC, instance);
    assertThatListContainsOnlyExpectedElement(eqValueLC, instance);
    assertThat(eqValueNE).isEmpty();
    assertThatListContainsOnlyExpectedElement(eqNameValueLC, instance);
    assertThat(eqNameLCValueNE).isEmpty();
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testTaskInvolvedUser() {
    // given
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String taskId = taskService.createTaskQuery().singleResult().getId();
    // if
    identityService.setAuthenticatedUserId("aAssignerId");
    taskService.addCandidateUser(taskId, "aUserId");
    taskService.addCandidateUser(taskId, "bUserId");
    taskService.deleteCandidateUser(taskId, "aUserId");
    taskService.deleteCandidateUser(taskId, "bUserId");
    Task taskAssignee = taskService.newTask("newTask");
    taskAssignee.setAssignee("aUserId");
    taskService.saveTask(taskAssignee);
    // query test
    assertEquals(2, historyService.createHistoricTaskInstanceQuery().taskInvolvedUser("aUserId").count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskInvolvedUser("bUserId").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskInvolvedUser("invalidUserId").count());
    taskService.deleteTask("newTask",true);
  }


  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testTaskInvolvedUserAsOwner() {
    // given
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String taskId = taskService.createTaskQuery().singleResult().getId();

    taskService.setOwner(taskId, "user");

    // when
    List<HistoricTaskInstance> historicTasks =
        historyService.createHistoricTaskInstanceQuery().taskInvolvedUser("user").list();

    // query test
    assertThat(historicTasks).hasSize(1);
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testTaskInvolvedGroup() {
    // given
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String taskId = taskService.createTaskQuery().singleResult().getId();
    // if
    identityService.setAuthenticatedUserId("aAssignerId");
    taskService.addCandidateGroup(taskId, "aGroupId");
    taskService.addCandidateGroup(taskId, "bGroupId");
    taskService.deleteCandidateGroup(taskId, "aGroupId");
    taskService.deleteCandidateGroup(taskId, "bGroupId");
    // query test
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskInvolvedGroup("aGroupId").count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskInvolvedGroup("bGroupId").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskInvolvedGroup("invalidGroupId").count());

    taskService.deleteTask("newTask",true);
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testTaskHadCandidateUser() {
    // given
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String taskId = taskService.createTaskQuery().singleResult().getId();
    // if
    identityService.setAuthenticatedUserId("aAssignerId");
    taskService.addCandidateUser(taskId, "aUserId");
    taskService.addCandidateUser(taskId, "bUserId");
    taskService.deleteCandidateUser(taskId, "bUserId");
    Task taskAssignee = taskService.newTask("newTask");
    taskAssignee.setAssignee("aUserId");
    taskService.saveTask(taskAssignee);
    // query test
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskHadCandidateUser("aUserId").count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskHadCandidateUser("bUserId").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskHadCandidateUser("invalidUserId").count());
    // delete test
    taskService.deleteTask("newTask",true);
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testTaskHadCandidateGroup() {
    // given
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String taskId = taskService.createTaskQuery().singleResult().getId();
    // if
    identityService.setAuthenticatedUserId("aAssignerId");
    taskService.addCandidateGroup(taskId, "bGroupId");
    taskService.deleteCandidateGroup(taskId, "bGroupId");
    // query test
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskHadCandidateGroup("bGroupId").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskHadCandidateGroup("invalidGroupId").count());
    // delete test
    taskService.deleteTask("newTask",true);
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testWithCandidateGroups() {
    // given
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    identityService.setAuthenticatedUserId("aAssignerId");
    taskService.addCandidateGroup(taskId, "aGroupId");

    // then
    assertEquals(historyService.createHistoricTaskInstanceQuery().withCandidateGroups().count(), 1);

    // cleanup
    taskService.deleteTask("newTask", true);
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testWithoutCandidateGroups() {
    // given
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String taskId = taskService.createTaskQuery().singleResult().getId();
    identityService.setAuthenticatedUserId("aAssignerId");
    taskService.addCandidateGroup(taskId, "aGroupId");

    // when
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // then
    assertEquals(historyService.createHistoricTaskInstanceQuery().count(), 2);
    assertEquals(historyService.createHistoricTaskInstanceQuery().withoutCandidateGroups().count(), 1);

    // cleanup
    taskService.deleteTask("newTask", true);
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testGroupTaskQuery() {
    // given
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String taskId = taskService.createTaskQuery().singleResult().getId();
    // if
    identityService.setAuthenticatedUserId("aAssignerId");
    taskService.addCandidateUser(taskId, "aUserId");
    taskService.addCandidateGroup(taskId, "aGroupId");
    taskService.addCandidateGroup(taskId, "bGroupId");
    Task taskOne = taskService.newTask("taskOne");
    taskOne.setAssignee("aUserId");
    taskService.saveTask(taskOne);
    Task taskTwo = taskService.newTask("taskTwo");
    taskTwo.setAssignee("aUserId");
    taskService.saveTask(taskTwo);
    Task taskThree = taskService.newTask("taskThree");
    taskThree.setOwner("aUserId");
    taskService.saveTask(taskThree);
    taskService.deleteCandidateGroup(taskId, "aGroupId");
    taskService.deleteCandidateGroup(taskId, "bGroupId");
    historyService.createHistoricTaskInstanceQuery();

    // Query test
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();
    assertEquals(4, query.taskInvolvedUser("aUserId").count());
    query = historyService.createHistoricTaskInstanceQuery();
    assertEquals(1, query.taskHadCandidateUser("aUserId").count());
    query = historyService.createHistoricTaskInstanceQuery();
    assertEquals(1, query.taskHadCandidateGroup("aGroupId").count());
    assertEquals(1, query.taskHadCandidateGroup("bGroupId").count());
    assertEquals(0, query.taskInvolvedUser("aUserId").count());
    query = historyService.createHistoricTaskInstanceQuery();
    assertEquals(4, query.taskInvolvedUser("aUserId").count());
    assertEquals(1, query.taskHadCandidateUser("aUserId").count());
    assertEquals(1, query.taskInvolvedUser("aUserId").count());
    // delete task
    taskService.deleteTask("taskOne",true);
    taskService.deleteTask("taskTwo",true);
    taskService.deleteTask("taskThree",true);
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testTaskWasAssigned() {
    // given
    Task taskOne = taskService.newTask("taskOne");
    Task taskTwo = taskService.newTask("taskTwo");
    Task taskThree = taskService.newTask("taskThree");

    // when
    taskOne.setAssignee("aUserId");
    taskService.saveTask(taskOne);

    taskTwo.setAssignee("anotherUserId");
    taskService.saveTask(taskTwo);

    taskService.saveTask(taskThree);

    List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().taskAssigned().list();

    // then
    assertEquals(list.size(), 2);

    // cleanup
    taskService.deleteTask("taskOne",true);
    taskService.deleteTask("taskTwo",true);
    taskService.deleteTask("taskThree",true);
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testTaskWasUnassigned() {
    // given
    Task taskOne = taskService.newTask("taskOne");
    Task taskTwo = taskService.newTask("taskTwo");
    Task taskThree = taskService.newTask("taskThree");

    // when
    taskOne.setAssignee("aUserId");
    taskService.saveTask(taskOne);

    taskTwo.setAssignee("anotherUserId");
    taskService.saveTask(taskTwo);

    taskService.saveTask(taskThree);

    List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().taskUnassigned().list();

    // then
    assertEquals(list.size(), 1);

    // cleanup
    taskService.deleteTask("taskOne",true);
    taskService.deleteTask("taskTwo",true);
    taskService.deleteTask("taskThree",true);
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testTaskReturnedBeforeEndTime() {
    // given
    Task taskOne = taskService.newTask("taskOne");

    // when
    taskOne.setAssignee("aUserId");
    taskService.saveTask(taskOne);

    Calendar hourAgo = Calendar.getInstance();
    hourAgo.add(Calendar.HOUR_OF_DAY, -1);
    ClockUtil.setCurrentTime(hourAgo.getTime());

    taskService.complete(taskOne.getId());

    List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery()
            .finishedBefore(hourAgo.getTime()).list();

    // then
    assertEquals(1, list.size());

    // cleanup
    taskService.deleteTask("taskOne",true);
    ClockUtil.reset();
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testTaskNotReturnedAfterEndTime() {
    // given
    Task taskOne = taskService.newTask("taskOne");

    // when
    taskOne.setAssignee("aUserId");
    taskService.saveTask(taskOne);

    Calendar hourAgo = Calendar.getInstance();
    hourAgo.add(Calendar.HOUR_OF_DAY, -1);
    ClockUtil.setCurrentTime(hourAgo.getTime());

    taskService.complete(taskOne.getId());

    List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery()
            .finishedAfter(Calendar.getInstance().getTime()).list();

    // then
    assertEquals(0, list.size());

    // cleanup
    taskService.deleteTask("taskOne",true);

    ClockUtil.reset();
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void shouldQueryForTasksWithoutDueDate() {
    // given
    Task taskOne = taskService.newTask("taskOne");
    taskOne.setDueDate(new Date());
    taskService.saveTask(taskOne);
    Task taskTwo = taskService.newTask("taskTwo");
    taskService.saveTask(taskTwo);

    // when
    taskService.complete(taskOne.getId());
    taskService.complete(taskTwo.getId());

    // then
    assertThat(historyService.createHistoricTaskInstanceQuery().withoutTaskDueDate().count())
      .isEqualTo(1L);

    // cleanup
    taskService.deleteTask("taskOne", true);
    taskService.deleteTask("taskTwo", true);
  }

  private void assertThatListContainsOnlyExpectedElement(List<HistoricTaskInstance> instances, ProcessInstance instance) {
    assertThat(instances.size()).isEqualTo(1);
    assertThat(instances.get(0).getProcessInstanceId()).isEqualTo(instance.getId());
  }

  private HistoricTaskInstanceQuery queryNameIgnoreCase() {
    return historyService.createHistoricTaskInstanceQuery().matchVariableNamesIgnoreCase();
  }

  private HistoricTaskInstanceQuery queryValueIgnoreCase() {
    return historyService.createHistoricTaskInstanceQuery().matchVariableValuesIgnoreCase();
  }

  private HistoricTaskInstanceQuery queryNameValueIgnoreCase() {
    return historyService.createHistoricTaskInstanceQuery().matchVariableNamesIgnoreCase().matchVariableValuesIgnoreCase();
  }
}
