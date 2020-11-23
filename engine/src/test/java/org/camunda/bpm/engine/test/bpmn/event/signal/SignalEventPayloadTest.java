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
package org.camunda.bpm.engine.test.bpmn.event.signal;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Nikola Koevski
 */
public class SignalEventPayloadTest {

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(configuration ->
      configuration.setJavaSerializationFormatEnabled(true));

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected RuntimeService runtimeService;
  protected TaskService taskService;
  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    taskService = engineRule.getTaskService();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
  }


  /**
   * Test case for CAM-8820 with a catching Start Signal event.
   * Using Source and Target Variable name mapping attributes.
   */
  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventPayloadTests.throwSignalWithPayload.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventPayloadTests.catchSignalWithPayloadStart.bpmn20.xml" })
  public void testSignalPayloadStart() {
    // given
    Map<String, Object> variables = new HashMap<>();
    variables.put("payloadVar1", "payloadVal1");
    variables.put("payloadVar2", "payloadVal2");

    // when
    runtimeService.startProcessInstanceByKey("throwPayloadSignal", variables);

    // then
    Task catchingPiUserTask = taskService.createTaskQuery().singleResult();

    List<VariableInstance> catchingPiVariables = runtimeService.createVariableInstanceQuery()
      .processInstanceIdIn(catchingPiUserTask.getProcessInstanceId())
      .list();
    assertEquals(2, catchingPiVariables.size());

    for(VariableInstance variable : catchingPiVariables) {
      if(variable.getName().equals("payloadVar1Target")) {
        assertEquals("payloadVal1", variable.getValue());
      } else {
        assertEquals("payloadVal2", variable.getValue());
      }
    }
  }

  /**
   * Test case for CAM-8820 with a catching Intermediate Signal event.
   * Using Source and Target Variable name mapping attributes.
   */
  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventPayloadTests.throwSignalWithPayload.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventPayloadTests.catchSignalWithPayloadIntermediate.bpmn20.xml" })
  public void testSignalPayloadIntermediate() {
    // given
    Map<String, Object> variables = new HashMap<>();
    variables.put("payloadVar1", "payloadVal1");
    variables.put("payloadVar2", "payloadVal2");
    ProcessInstance catchingPI = runtimeService.startProcessInstanceByKey("catchIntermediatePayloadSignal");

    // when
    runtimeService.startProcessInstanceByKey("throwPayloadSignal", variables);

    // then
    List<VariableInstance> catchingPiVariables = runtimeService
      .createVariableInstanceQuery()
      .processInstanceIdIn(catchingPI.getId())
      .list();
    assertEquals(2, catchingPiVariables.size());

    for(VariableInstance variable : catchingPiVariables) {
      if(variable.getName().equals("payloadVar1Target")) {
        assertEquals("payloadVal1", variable.getValue());
      } else {
        assertEquals("payloadVal2", variable.getValue());
      }
    }
  }

  /**
   * Test case for CAM-8820 with an expression as a source.
   */
  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventPayloadTests.throwSignalWithExpressionPayload.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventPayloadTests.catchSignalWithPayloadIntermediate.bpmn20.xml" })
  public void testSignalSourceExpressionPayload() {
    // given
    Map<String, Object> variables = new HashMap<>();
    variables.put("payloadVar", "Val");
    ProcessInstance catchingPI = runtimeService.startProcessInstanceByKey("catchIntermediatePayloadSignal");

    // when
    runtimeService.startProcessInstanceByKey("throwExpressionPayloadSignal", variables);

    // then
    List<VariableInstance> catchingPiVariables = runtimeService
      .createVariableInstanceQuery()
      .processInstanceIdIn(catchingPI.getId())
      .list();
    assertEquals(1, catchingPiVariables.size());

    assertEquals("srcExpressionResVal", catchingPiVariables.get(0).getName());
    assertEquals("sourceVal", catchingPiVariables.get(0).getValue());
  }

  /**
   * Test case for CAM-8820 with all the (global) source variables
   * as the signal payload.
   */
  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventPayloadTests.throwSignalWithAllVariablesPayload.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventPayloadTests.catchSignalWithPayloadIntermediate.bpmn20.xml" })
  public void testSignalAllSourceVariablesPayload() {
    // given
    Map<String, Object> variables = new HashMap<>();
    variables.put("payloadVar1", "payloadVal1");
    variables.put("payloadVar2", "payloadVal2");
    ProcessInstance catchingPI = runtimeService.startProcessInstanceByKey("catchIntermediatePayloadSignal");

    // when
    runtimeService.startProcessInstanceByKey("throwPayloadSignal", variables);

    // then
    List<VariableInstance> catchingPiVariables = runtimeService
      .createVariableInstanceQuery()
      .processInstanceIdIn(catchingPI.getId())
      .list();
    assertEquals(2, catchingPiVariables.size());

    for(VariableInstance variable : catchingPiVariables) {
      if(variable.getName().equals("payloadVar1")) {
        assertEquals("payloadVal1", variable.getValue());
      } else {
        assertEquals("payloadVal2", variable.getValue());
      }
    }
  }

  /**
   * Test case for CAM-8820 with all the (local) source variables
   * as the signal payload.
   */
  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventPayloadTests.throwEndSignalEventWithAllLocalVariablesPayload.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventPayloadTests.catchSignalWithPayloadIntermediate.bpmn20.xml" })
  public void testSignalAllLocalSourceVariablesPayload() {
    // given
    Map<String, Object> variables = new HashMap<>();
    variables.put("payloadVar1", "payloadVal1");
    String localVar1 = "localVar1";
    String localVal1 = "localVal1";
    String localVar2 = "localVar2";
    String localVal2 = "localVal2";
    ProcessInstance catchingPI = runtimeService.startProcessInstanceByKey("catchIntermediatePayloadSignal");

    // when
    runtimeService.startProcessInstanceByKey("throwPayloadSignal", variables);

    // then
    List<VariableInstance> catchingPiVariables = runtimeService
      .createVariableInstanceQuery()
      .processInstanceIdIn(catchingPI.getId())
      .list();
    assertEquals(2, catchingPiVariables.size());

    for(VariableInstance variable : catchingPiVariables) {
      if(variable.getName().equals(localVar1)) {
        assertEquals(localVal1, variable.getValue());
      } else {
        assertEquals(localVal2, variable.getValue());
      }
    }
  }

  /**
   * Test case for CAM-8820 with a Business Key
   * as signal payload.
   */
  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventPayloadTests.throwSignalWithBusinessKeyPayload.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventPayloadTests.catchSignalWithPayloadStart.bpmn20.xml" })
  public void testSignalBusinessKeyPayload() {
    // given
    String businessKey = "aBusinessKey";

    // when
    runtimeService.startProcessInstanceByKey("throwBusinessKeyPayloadSignal", businessKey);

    // then
    ProcessInstance catchingPI = runtimeService.createProcessInstanceQuery().singleResult();
    assertEquals(businessKey, catchingPI.getBusinessKey());
  }

  /**
   * Test case for CAM-8820 with all possible options for a signal payload.
   */
  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventPayloadTests.throwSignalWithAllOptions.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventPayloadTests.catchSignalWithPayloadStart.bpmn20.xml"})
  public void testSignalPayloadWithAllOptions() {
    // given
    Map<String, Object> variables = new HashMap<>();
    String globalVar1 = "payloadVar1";
    String globalVal1 = "payloadVar1";
    String globalVar2 = "payloadVar2";
    String globalVal2 = "payloadVal2";
    variables.put(globalVar1, globalVal1);
    variables.put(globalVar2, globalVal2);
    String localVar1 = "localVar1";
    String localVal1 = "localVal1";;
    String localVar2 = "localVar2";
    String localVal2 = "localVal2";
    String businessKey = "aBusinessKey";

    // when
    runtimeService.startProcessInstanceByKey("throwCompletePayloadSignal", businessKey, variables);

    // then
    Task catchingPiUserTask = taskService.createTaskQuery().singleResult();
    ProcessInstance catchingPI = runtimeService.createProcessInstanceQuery().processInstanceId(catchingPiUserTask.getProcessInstanceId()).singleResult();
    assertEquals(businessKey, catchingPI.getBusinessKey());

    List<VariableInstance> targetVariables = runtimeService.createVariableInstanceQuery().processInstanceIdIn(catchingPiUserTask.getProcessInstanceId()).list();
    assertEquals(4, targetVariables.size());

    for (VariableInstance variable : targetVariables) {
      if (variable.getName().equals(globalVar1 + "Target")) {
        assertEquals(globalVal1, variable.getValue());
      } else if (variable.getName().equals(globalVar2 + "Target")) {
        assertEquals(globalVal2 + "Source", variable.getValue());
      } else if (variable.getName().equals(localVar1)) {
        assertEquals(localVal1, variable.getValue());
      } else if (variable.getName().equals(localVar2)) {
        assertEquals(localVal2, variable.getValue());
      }
    }
  }
}
