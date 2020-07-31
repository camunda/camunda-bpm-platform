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
package org.camunda.bpm.engine.test.bpmn.iomapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.ParseException;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Thorben Lindhauer
 *
 */
public class InputOutputEventTest extends PluggableProcessEngineTest {

  @Before
  public void setUp() throws Exception {


    VariableLogDelegate.reset();
  }


  @Deployment
  @Test
  public void testMessageThrowEvent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    // input mapping
    Map<String, Object> mappedVariables = VariableLogDelegate.LOCAL_VARIABLES;
    assertEquals(1, mappedVariables.size());
    assertEquals("mappedValue", mappedVariables.get("mappedVariable"));

    // output mapping
    String variable = (String) runtimeService.getVariableLocal(processInstance.getId(), "outVariable");
    assertEquals("mappedValue", variable);
  }

  @Deployment
  @Test
  public void testMessageCatchEvent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    Execution messageExecution = runtimeService.createExecutionQuery().activityId("messageCatch").singleResult();

    Map<String, Object> localVariables = runtimeService.getVariablesLocal(messageExecution.getId());
    assertEquals(1, localVariables.size());
    assertEquals("mappedValue", localVariables.get("mappedVariable"));

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("messageVariable", "outValue");
    runtimeService.messageEventReceived("IncomingMessage", messageExecution.getId(), variables);

    // output mapping
    String variable = (String) runtimeService.getVariableLocal(processInstance.getId(), "outVariable");
    assertEquals("outValue", variable);
  }

  @Deployment
  @Test
  public void testTimerCatchEvent() {
    Map<String, Object> variables = new HashMap<String, Object>();
    Date dueDate = DateTimeUtil.now().plusMinutes(5).toDate();
    variables.put("outerVariable", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(dueDate));
    runtimeService.startProcessInstanceByKey("testProcess", variables);

    Job job = managementService.createJobQuery().singleResult();
    TimerEntity timer = (TimerEntity) job;
    assertDateEquals(dueDate, timer.getDuedate());
  }

  protected void assertDateEquals(Date expected, Date actual) {
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    assertEquals(format.format(expected), format.format(actual));
  }

  @Deployment
  @Test
  public void testNoneThrowEvent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    Map<String, Object> mappedVariables = VariableLogDelegate.LOCAL_VARIABLES;
    assertEquals(1, mappedVariables.size());
    assertEquals("mappedValue", mappedVariables.get("mappedVariable"));

    // output mapping
    String variable = (String) runtimeService.getVariableLocal(processInstance.getId(), "outVariable");
    assertEquals("mappedValue", variable);
  }

  @Test
  public void testMessageStartEvent() {

    try {
      repositoryService
        .createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/iomapping/InputOutputEventTest.testMessageStartEvent.bpmn20.xml")
        .deploy();
      fail("expected exception");
    } catch (ParseException e) {
      testRule.assertTextPresent("camunda:inputOutput mapping unsupported for element type 'startEvent'", e.getMessage());
      assertThat(e.getResorceReports().get(0).getErrors().get(0).getMainElementId()).isEqualTo("start");
    }
  }

  @Test
  public void testNoneEndEvent() {
    try {
      repositoryService
        .createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/iomapping/InputOutputEventTest.testNoneEndEvent.bpmn20.xml")
        .deploy();
      fail("expected exception");
    } catch (ParseException e) {
      testRule.assertTextPresent("camunda:outputParameter not allowed for element type 'endEvent'", e.getMessage());
      assertThat(e.getResorceReports().get(0).getErrors().get(0).getMainElementId()).isEqualTo("endMapping");
    }
  }

  @Deployment
  @Test
  public void testMessageEndEvent() {
    runtimeService.startProcessInstanceByKey("testProcess");

    assertEquals(0, runtimeService.createProcessInstanceQuery().count());

    // input mapping
    Map<String, Object> mappedVariables = VariableLogDelegate.LOCAL_VARIABLES;
    assertEquals(1, mappedVariables.size());
    assertEquals("mappedValue", mappedVariables.get("mappedVariable"));
  }

  @Deployment
  @Test
  public void testMessageCatchAfterEventGateway() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    // when
    runtimeService.createMessageCorrelation("foo")
      .processInstanceId(processInstance.getId())
      .correlate();

    // then
    VariableInstance variableInstance = runtimeService.createVariableInstanceQuery()
      .processInstanceIdIn(processInstance.getId())
      .variableName("foo")
      .singleResult();

    assertNotNull(variableInstance);
    assertEquals("bar", variableInstance.getValue());
  }

  @Deployment
  @Test
  public void testTimerCatchAfterEventGateway() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    Job job = managementService.createJobQuery()
        .processInstanceId(processInstance.getId())
        .singleResult();

    // when
    managementService.executeJob(job.getId());

    // then
    VariableInstance variableInstance = runtimeService.createVariableInstanceQuery()
      .processInstanceIdIn(processInstance.getId())
      .variableName("foo")
      .singleResult();

    assertNotNull(variableInstance);
    assertEquals("bar", variableInstance.getValue());
  }

  @Deployment
  @Test
  public void testSignalCatchAfterEventGateway() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    Execution execution = runtimeService.createExecutionQuery()
      .processInstanceId(processInstance.getId())
      .signalEventSubscriptionName("foo")
      .singleResult();

    assertNotNull(execution);

    // when
    runtimeService.signalEventReceived("foo", execution.getId());

    // then
    VariableInstance variableInstance = runtimeService.createVariableInstanceQuery()
      .processInstanceIdIn(processInstance.getId())
      .variableName("foo")
      .singleResult();

    assertNotNull(variableInstance);
    assertEquals("bar", variableInstance.getValue());
  }

  @Deployment
  @Test
  public void testConditionalCatchAfterEventGateway() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    // when
    runtimeService.setVariable(processInstance.getId(), "var", 1);

    // then
    VariableInstance variableInstance = runtimeService.createVariableInstanceQuery()
      .processInstanceIdIn(processInstance.getId())
      .variableName("foo")
      .singleResult();

    assertNotNull(variableInstance);
    assertEquals("bar", variableInstance.getValue());
  }

  @Test
  public void testMessageBoundaryEvent() {
    try {
      repositoryService
        .createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/iomapping/InputOutputEventTest.testMessageBoundaryEvent.bpmn20.xml")
        .deploy();
      fail("expected exception");
    } catch (ParseException e) {
      testRule.assertTextPresent("camunda:inputOutput mapping unsupported for element type 'boundaryEvent'", e.getMessage());
      assertThat(e.getResorceReports().get(0).getErrors().get(0).getMainElementId()).isEqualTo("messageBoundary");
    }
  }

  @After
  public void tearDown() throws Exception {


    VariableLogDelegate.reset();
  }

}
