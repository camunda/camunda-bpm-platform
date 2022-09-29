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
package org.camunda.bpm.engine.test.bpmn.el;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Test;

/**
 * @author Frederik Heremans
 */
public class ExpressionManagerTest extends PluggableProcessEngineTest {

  protected String deploymentId;

  @After
  public void clear() {
    if (deploymentId != null) {
      repositoryService.deleteDeployment(deploymentId, true);
      deploymentId = null;
    }
  }

  @Deployment
  @Test
  public void testMethodExpressions() {
    // Process contains 2 service tasks. one containing a method with no params, the other
    // contains a method with 2 params. When the process completes without exception,
    // test passed.
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("aString", "abcdefgh");
    runtimeService.startProcessInstanceByKey("methodExpressionProcess", vars);

    assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey("methodExpressionProcess").count());
  }

  @Deployment
  @Test
  public void testExecutionAvailable() {
    Map<String, Object> vars = new HashMap<String, Object>();

    vars.put("myVar", new ExecutionTestVariable());
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testExecutionAvailableProcess", vars);

    // Check of the testMethod has been called with the current execution
    String value = (String) runtimeService.getVariable(processInstance.getId(), "testVar");
    assertNotNull(value);
    assertEquals("myValue", value);
  }

  @Deployment
  @Test
  public void testAuthenticatedUserIdAvailable() {
    try {
      // Setup authentication
      identityService.setAuthenticatedUserId("frederik");
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testAuthenticatedUserIdAvailableProcess");

      // Check if the variable that has been set in service-task is the authenticated user
      String value = (String) runtimeService.getVariable(processInstance.getId(), "theUser");
      assertNotNull(value);
      assertEquals("frederik", value);
    } finally {
      // Cleanup
      identityService.clearAuthentication();
    }
  }

  @Deployment
  @Test
  public void testResolvesVariablesFromDifferentScopes() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("assignee", "michael");

    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("michael", task.getAssignee());

    variables.put("assignee", "johnny");
    ProcessInstance secondInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    task = taskService.createTaskQuery().processInstanceId(secondInstance.getId()).singleResult();
    assertEquals("johnny", task.getAssignee());
  }

  @Deployment
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Test
  public void testSetVariableByExpressionFromListener() {
    // given
    runtimeService.startProcessInstanceByKey("fieldInjectionTest", Variables.putValue("myCounter", 5));
    // when
    taskService.complete(taskService.createTaskQuery().singleResult().getId());
    // then
    assertEquals(1L, historyService.createHistoricVariableInstanceQuery().variableValueEquals("myCounter", 6).count());
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testJuelExpressionWithNonPublicClass() {
    final BpmnModelInstance process = Bpmn.createExecutableProcess("testProcess")
        .startEvent()
          .exclusiveGateway()
            .condition("true", "${list.contains('foo')}")
            .userTask("userTask")
          .moveToLastGateway()
            .condition("false", "${!list.contains('foo')}")
            .endEvent()
        .done();

    deploymentId = repositoryService.createDeployment()
        .addModelInstance("testProcess.bpmn", process)
        .deploy()
        .getId();

    runtimeService.startProcessInstanceByKey("testProcess",
        Variables.createVariables().putValue("list", Arrays.asList("foo", "bar")));

    HistoricActivityInstance userTask = historyService.createHistoricActivityInstanceQuery()
        .activityId("userTask")
        .singleResult();
    assertThat(userTask).isNotNull();
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void shouldCompareWithBigDecimal() {
    // given
    BpmnModelInstance process = Bpmn.createExecutableProcess("testProcess")
        .startEvent()
          .exclusiveGateway()
            .condition("true", "${total.compareTo(myValue) >= 0}")
            .userTask("userTask")
          .moveToLastGateway()
            .condition("false", "${total.compareTo(myValue) < 0}")
            .endEvent()
        .done();

    deploymentId = repositoryService.createDeployment()
        .addModelInstance("testProcess.bpmn", process)
        .deploy()
        .getId();

    // when
    runtimeService.startProcessInstanceByKey("testProcess",
        Variables.createVariables()
            .putValue("total", new BigDecimal(123))
            .putValue("myValue", new BigDecimal(0)));

    // then
    HistoricActivityInstance userTask = historyService.createHistoricActivityInstanceQuery()
        .activityId("userTask")
        .singleResult();
    assertThat(userTask).isNotNull();
  }

  @Deployment
  @Test
  public void shouldResolveMethodExpressionTwoParametersSameType() {
    Map<String, Object> vars = new HashMap<String, Object>();

    vars.put("myVar", new ExpressionTestParameter());
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process", vars);

    // Check of the testMethod has been called with the current execution
    boolean value = (boolean) runtimeService.getVariable(processInstance.getId(), "resultVar");
    assertThat(value).isTrue();
  }
}
