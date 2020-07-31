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
package org.camunda.bpm.engine.test.bpmn.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ParseException;
import org.camunda.bpm.engine.Problem;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.util.CollectionUtil;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Test;

/**
 * @author Joram Barrez
 */
public class ExclusiveGatewayTest extends PluggableProcessEngineTest {

  @Deployment
  @Test
  public void testDivergingExclusiveGateway() {
    for (int i = 1; i <= 3; i++) {
      ProcessInstance pi = runtimeService.startProcessInstanceByKey("exclusiveGwDiverging", CollectionUtil.singletonMap("input", i));
      assertEquals("Task " + i, taskService.createTaskQuery().singleResult().getName());
      runtimeService.deleteProcessInstance(pi.getId(), "testing deletion");
    }
  }

  @Deployment
  @Test
  public void testMergingExclusiveGateway() {
    runtimeService.startProcessInstanceByKey("exclusiveGwMerging");
    assertEquals(3, taskService.createTaskQuery().count());
  }

  // If there are multiple outgoing seqFlow with valid conditions, the first
  // defined one should be chosen.
  @Deployment
  @Test
  public void testMultipleValidConditions() {
    runtimeService.startProcessInstanceByKey("exclusiveGwMultipleValidConditions", CollectionUtil.singletonMap("input", 5));
    assertEquals("Task 2", taskService.createTaskQuery().singleResult().getName());
  }

  @Deployment
  @Test
  public void testNoSequenceFlowSelected() {
    try {
      runtimeService.startProcessInstanceByKey("exclusiveGwNoSeqFlowSelected", CollectionUtil.singletonMap("input", 4));
      fail();
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("ENGINE-02004 No outgoing sequence flow for the element with id 'exclusiveGw' could be selected for continuing the process.", e.getMessage());
    }
  }

  /**
   * Test for bug ACT-10: whitespaces/newlines in expressions lead to exceptions
   */
  @Deployment
  @Test
  public void testWhitespaceInExpression() {
    // Starting a process instance will lead to an exception if whitespace are incorrectly handled
    runtimeService.startProcessInstanceByKey("whiteSpaceInExpression",
            CollectionUtil.singletonMap("input", 1));
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/gateway/ExclusiveGatewayTest.testDivergingExclusiveGateway.bpmn20.xml"})
  @Test
  public void testUnknownVariableInExpression() {
    // Instead of 'input' we're starting a process instance with the name 'iinput' (ie. a typo)
    try {
      runtimeService.startProcessInstanceByKey(
            "exclusiveGwDiverging", CollectionUtil.singletonMap("iinput", 1));
      fail();
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("Unknown property used in expression", e.getMessage());
    }
  }

  @Deployment
  @Test
  public void testDecideBasedOnBeanProperty() {
    runtimeService.startProcessInstanceByKey("decisionBasedOnBeanProperty",
            CollectionUtil.singletonMap("order", new ExclusiveGatewayTestOrder(150)));

    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    assertEquals("Standard service", task.getName());
  }

  @Deployment
  @Test
  public void testDecideBasedOnListOrArrayOfBeans() {
    List<ExclusiveGatewayTestOrder> orders = new ArrayList<ExclusiveGatewayTestOrder>();
    orders.add(new ExclusiveGatewayTestOrder(50));
    orders.add(new ExclusiveGatewayTestOrder(300));
    orders.add(new ExclusiveGatewayTestOrder(175));

    ProcessInstance pi = runtimeService.startProcessInstanceByKey(
            "decisionBasedOnListOrArrayOfBeans", CollectionUtil.singletonMap("orders", orders));

    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertNotNull(task);
    assertEquals("Gold Member service", task.getName());


    // Arrays are usable in exactly the same way
    ExclusiveGatewayTestOrder[] orderArray = orders.toArray(new ExclusiveGatewayTestOrder[orders.size()]);
    orderArray[1].setPrice(10);
    pi = runtimeService.startProcessInstanceByKey(
            "decisionBasedOnListOrArrayOfBeans", CollectionUtil.singletonMap("orders", orderArray));

    task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertNotNull(task);
    assertEquals("Basic service", task.getName());
  }

  @Deployment
  @Test
  public void testDecideBasedOnBeanMethod() {
    runtimeService.startProcessInstanceByKey("decisionBasedOnBeanMethod",
            CollectionUtil.singletonMap("order", new ExclusiveGatewayTestOrder(300)));

    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    assertEquals("Gold Member service", task.getName());
  }

  @Deployment
  @Test
  public void testInvalidMethodExpression() {
    try {
      runtimeService.startProcessInstanceByKey("invalidMethodExpression",
            CollectionUtil.singletonMap("order", new ExclusiveGatewayTestOrder(50)));
      fail();
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("Unknown method used in expression", e.getMessage());
    }
  }

  @Deployment
  @Test
  public void testDefaultSequenceFlow() {

    // Input == 1 -> default is not selected
    String procId = runtimeService.startProcessInstanceByKey("exclusiveGwDefaultSequenceFlow",
            CollectionUtil.singletonMap("input", 1)).getId();
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("Input is one", task.getName());
    runtimeService.deleteProcessInstance(procId, null);

    procId = runtimeService.startProcessInstanceByKey("exclusiveGwDefaultSequenceFlow",
            CollectionUtil.singletonMap("input", 5)).getId();
    task = taskService.createTaskQuery().singleResult();
    assertEquals("Default input", task.getName());
  }

  @Deployment
  @Test
  public void testNoIdOnSequenceFlow() {
    runtimeService.startProcessInstanceByKey("noIdOnSequenceFlow", CollectionUtil.singletonMap("input", 3));
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("Input is more than one", task.getName());
  }

  @Test
  public void testFlowWithoutConditionNoDefaultFlow() {
    String flowWithoutConditionNoDefaultFlow = "<?xml version='1.0' encoding='UTF-8'?>" +
            "<definitions id='definitions' xmlns='http://www.omg.org/spec/BPMN/20100524/MODEL' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:activiti='http://activiti.org/bpmn' targetNamespace='Examples'>" +
            "  <process id='exclusiveGwDefaultSequenceFlow' isExecutable='true'> " +
            "    <startEvent id='theStart' /> " +
            "    <sequenceFlow id='flow1' sourceRef='theStart' targetRef='exclusiveGw' /> " +

            "    <exclusiveGateway id='exclusiveGw' name='Exclusive Gateway' /> " + // no default = "flow3" !!
            "    <sequenceFlow id='flow2' sourceRef='exclusiveGw' targetRef='theTask1'> " +
            "      <conditionExpression xsi:type='tFormalExpression'>${input == 1}</conditionExpression> " +
            "    </sequenceFlow> " +
            "    <sequenceFlow id='flow3' sourceRef='exclusiveGw' targetRef='theTask2'/> " +  // one would be OK
            "    <sequenceFlow id='flow4' sourceRef='exclusiveGw' targetRef='theTask2'/> " +  // but two unconditional not!

            "    <userTask id='theTask1' name='Input is one' /> " +
            "    <userTask id='theTask2' name='Default input' /> " +
            "  </process>" +
            "</definitions>";
    try {
      repositoryService.createDeployment().addString("myprocess.bpmn20.xml", flowWithoutConditionNoDefaultFlow).deploy();
      fail("Could deploy a process definition with a sequence flow out of a XOR Gateway without condition with is not the default flow.");
    }
    catch (ParseException e) {
      assertTrue(e.getMessage().contains("Exclusive Gateway 'exclusiveGw' has outgoing sequence flow 'flow3' without condition which is not the default flow."));
      Problem error = e.getResorceReports().get(0).getErrors().get(0);
      assertThat(error.getMainElementId()).isEqualTo("exclusiveGw");
      assertThat(error.getElementIds()).containsExactlyInAnyOrder("exclusiveGw", "flow3");
    }
  }

  @Test
  public void testDefaultFlowWithCondition() {
    String defaultFlowWithCondition = "<?xml version='1.0' encoding='UTF-8'?>" +
            "<definitions id='definitions' xmlns='http://www.omg.org/spec/BPMN/20100524/MODEL' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:activiti='http://activiti.org/bpmn' targetNamespace='Examples'>" +
            "  <process id='exclusiveGwDefaultSequenceFlow' isExecutable='true'> " +
            "    <startEvent id='theStart' /> " +
            "    <sequenceFlow id='flow1' sourceRef='theStart' targetRef='exclusiveGw' /> " +

            "    <exclusiveGateway id='exclusiveGw' name='Exclusive Gateway' default='flow3' /> " +
            "    <sequenceFlow id='flow2' sourceRef='exclusiveGw' targetRef='theTask1'> " +
            "      <conditionExpression xsi:type='tFormalExpression'>${input == 1}</conditionExpression> " +
            "    </sequenceFlow> " +
            "    <sequenceFlow id='flow3' sourceRef='exclusiveGw' targetRef='theTask2'> " +
            "      <conditionExpression xsi:type='tFormalExpression'>${input == 3}</conditionExpression> " +
            "    </sequenceFlow> " +

            "    <userTask id='theTask1' name='Input is one' /> " +
            "    <userTask id='theTask2' name='Default input' /> " +
            "  </process>" +
            "</definitions>";
    try {
      repositoryService.createDeployment().addString("myprocess.bpmn20.xml", defaultFlowWithCondition).deploy();
      fail("Could deploy a process definition with a sequence flow out of a XOR Gateway without condition with is not the default flow.");
    }
    catch (ParseException e) {
      assertTrue(e.getMessage().contains("Exclusive Gateway 'exclusiveGw' has outgoing sequence flow 'flow3' which is the default flow but has a condition too."));
      Problem error = e.getResorceReports().get(0).getErrors().get(0);
      assertThat(error.getMainElementId()).isEqualTo("exclusiveGw");
      assertThat(error.getElementIds()).containsExactlyInAnyOrder("exclusiveGw", "flow3");
    }
  }

  @Test
  public void testNoOutgoingFlow() {
    String noOutgoingFlow = "<?xml version='1.0' encoding='UTF-8'?>" +
            "<definitions id='definitions' xmlns='http://www.omg.org/spec/BPMN/20100524/MODEL' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:activiti='http://activiti.org/bpmn' targetNamespace='Examples'>" +
            "  <process id='exclusiveGwDefaultSequenceFlow' isExecutable='true'> " +
            "    <startEvent id='theStart' /> " +
            "    <sequenceFlow id='flow1' sourceRef='theStart' targetRef='exclusiveGw' /> " +
            "    <exclusiveGateway id='exclusiveGw' name='Exclusive Gateway' /> " +
            "  </process>" +
            "</definitions>";
    try {
      repositoryService.createDeployment().addString("myprocess.bpmn20.xml", noOutgoingFlow).deploy();
      fail("Could deploy a process definition with a sequence flow out of a XOR Gateway without condition with is not the default flow.");
    }
    catch (ParseException e) {
      assertTrue(e.getMessage().contains("Exclusive Gateway 'exclusiveGw' has no outgoing sequence flows."));
      assertThat(e.getResorceReports().get(0).getErrors().get(0).getMainElementId()).isEqualTo("exclusiveGw");
    }

  }

  // see CAM-4172
  @Deployment
  @Test
  public void testLoopWithManyIterations() {
    int numOfIterations = 1000;

    // this should not fail
    runtimeService.startProcessInstanceByKey("testProcess", Variables.createVariables().putValue("numOfIterations", numOfIterations));
  }

  /**
   * The test process has an XOR gateway where, the 'input' variable is used to
   * select one of the outgoing sequence flow. Every one of those sequence flow
   * goes to another task, allowing us to test the decision very easily.
   */
  @Deployment
  @Test
  public void testDecisionFunctionality() {

    Map<String, Object> variables = new HashMap<String, Object>();

    // Test with input == 1
    variables.put("input", 1);
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("exclusiveGateway", variables);
    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("Send e-mail for more information", task.getName());

    // Test with input == 2
    variables.put("input", 2);
    pi = runtimeService.startProcessInstanceByKey("exclusiveGateway", variables);
    task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("Check account balance", task.getName());

    // Test with input == 3
    variables.put("input", 3);
    pi = runtimeService.startProcessInstanceByKey("exclusiveGateway", variables);
    task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("Call customer", task.getName());

    // Test with input == 4
    variables.put("input", 4);
    try {
      runtimeService.startProcessInstanceByKey("exclusiveGateway", variables);
      fail();
    } catch (ProcessEngineException e) {
      // Exception is expected since no outgoing sequence flow matches
    }

  }
}
