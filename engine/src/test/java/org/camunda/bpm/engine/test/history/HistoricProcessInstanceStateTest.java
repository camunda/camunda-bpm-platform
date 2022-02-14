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
package org.camunda.bpm.engine.test.history;

import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.TerminateEventDefinition;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Askar Akhmerov
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
public class HistoricProcessInstanceStateTest {

  public static final String TERMINATION = "termination";
  public static final String PROCESS_ID = "process1";
  public static final String REASON = "very important reason";

  public ProcessEngineRule processEngineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule processEngineTestRule = new ProcessEngineTestRule(processEngineRule);

  @Rule
  public RuleChain ruleChain = RuleChain
      .outerRule(processEngineTestRule)
      .around(processEngineRule);

  @Test
  public void testTerminatedInternalWithGateway() {
    BpmnModelInstance instance = Bpmn.createExecutableProcess(PROCESS_ID)
        .startEvent()
        .parallelGateway()
        .endEvent()
        .moveToLastGateway()
        .endEvent(TERMINATION)
        .done();
    initEndEvent(instance, TERMINATION);
    ProcessDefinition processDefinition = processEngineTestRule.deployAndGetDefinition(instance);
    processEngineRule.getRuntimeService().startProcessInstanceById(processDefinition.getId());
    HistoricProcessInstance entity = getHistoricProcessInstanceWithAssertion(processDefinition);
    assertThat(entity.getState()).isEqualTo(HistoricProcessInstance.STATE_COMPLETED);
  }

  @Test
  public void testCompletedOnEndEvent() {
    BpmnModelInstance instance = Bpmn.createExecutableProcess(PROCESS_ID)
        .startEvent()
        .endEvent()
        .done();
    ProcessDefinition processDefinition = processEngineTestRule.deployAndGetDefinition(instance);
    processEngineRule.getRuntimeService().startProcessInstanceById(processDefinition.getId());
    HistoricProcessInstance entity = getHistoricProcessInstanceWithAssertion(processDefinition);
    assertThat(entity.getState()).isEqualTo(HistoricProcessInstance.STATE_COMPLETED);
  }


  @Test
  public void testCompletionWithSuspension() {
    BpmnModelInstance instance = Bpmn.createExecutableProcess(PROCESS_ID)
        .startEvent()
        .userTask()
        .endEvent()
        .done();
    ProcessDefinition processDefinition = processEngineTestRule.deployAndGetDefinition(instance);
    ProcessInstance processInstance = processEngineRule.getRuntimeService()
        .startProcessInstanceById(processDefinition.getId());
    HistoricProcessInstance entity = getHistoricProcessInstanceWithAssertion(processDefinition);
    assertThat(entity.getState()).isEqualTo(HistoricProcessInstance.STATE_ACTIVE);

    //suspend
    processEngineRule.getRuntimeService().updateProcessInstanceSuspensionState()
        .byProcessInstanceId(processInstance.getId()).suspend();

    entity = getHistoricProcessInstanceWithAssertion(processDefinition);
    assertThat(entity.getState()).isEqualTo(HistoricProcessInstance.STATE_SUSPENDED);

    //activate
    processEngineRule.getRuntimeService().updateProcessInstanceSuspensionState()
        .byProcessInstanceId(processInstance.getId()).activate();

    entity = getHistoricProcessInstanceWithAssertion(processDefinition);
    assertThat(entity.getState()).isEqualTo(HistoricProcessInstance.STATE_ACTIVE);

    //complete task
    processEngineRule.getTaskService().complete(
        processEngineRule.getTaskService().createTaskQuery().active().singleResult().getId());

    //make sure happy path ended
    entity = getHistoricProcessInstanceWithAssertion(processDefinition);
    assertThat(entity.getState()).isEqualTo(HistoricProcessInstance.STATE_COMPLETED);
  }

  @Test
  public void testSuspensionByProcessDefinition() {
    BpmnModelInstance instance = Bpmn.createExecutableProcess(PROCESS_ID)
        .startEvent()
        .userTask()
        .endEvent()
        .done();
    ProcessDefinition processDefinition = processEngineTestRule.deployAndGetDefinition(instance);
    ProcessInstance processInstance1 = processEngineRule.getRuntimeService()
        .startProcessInstanceById(processDefinition.getId());

    ProcessInstance processInstance2 = processEngineRule.getRuntimeService()
        .startProcessInstanceById(processDefinition.getId());

    //suspend all
    processEngineRule.getRuntimeService().updateProcessInstanceSuspensionState()
        .byProcessDefinitionId(processDefinition.getId()).suspend();

    HistoricProcessInstance hpi1 = processEngineRule.getHistoryService().createHistoricProcessInstanceQuery()
        .processInstanceId(processInstance1.getId()).singleResult();

    HistoricProcessInstance hpi2 = processEngineRule.getHistoryService().createHistoricProcessInstanceQuery()
        .processInstanceId(processInstance2.getId()).singleResult();

    assertThat(hpi1.getState()).isEqualTo(HistoricProcessInstance.STATE_SUSPENDED);
    assertThat(hpi2.getState()).isEqualTo(HistoricProcessInstance.STATE_SUSPENDED);
    assertEquals(2, processEngineRule.getHistoryService().createHistoricProcessInstanceQuery().suspended().count());

    //activate all
    processEngineRule.getRuntimeService().updateProcessInstanceSuspensionState()
        .byProcessDefinitionKey(processDefinition.getKey()).activate();

    hpi1 = processEngineRule.getHistoryService().createHistoricProcessInstanceQuery()
        .processInstanceId(processInstance1.getId()).singleResult();

    hpi2 = processEngineRule.getHistoryService().createHistoricProcessInstanceQuery()
        .processInstanceId(processInstance2.getId()).singleResult();

    assertThat(hpi1.getState()).isEqualTo(HistoricProcessInstance.STATE_ACTIVE);
    assertThat(hpi2.getState()).isEqualTo(HistoricProcessInstance.STATE_ACTIVE);
    assertEquals(2, processEngineRule.getHistoryService().createHistoricProcessInstanceQuery().active().count());
  }


  @Test
  public void testCancellationState() {
    BpmnModelInstance instance = Bpmn.createExecutableProcess(PROCESS_ID)
        .startEvent()
        .userTask()
        .endEvent()
        .done();
    ProcessDefinition processDefinition = processEngineTestRule.deployAndGetDefinition(instance);
    ProcessInstance processInstance = processEngineRule.getRuntimeService()
        .startProcessInstanceById(processDefinition.getId());
    HistoricProcessInstance entity = getHistoricProcessInstanceWithAssertion(processDefinition);
    assertThat(entity.getState()).isEqualTo(HistoricProcessInstance.STATE_ACTIVE);

    //same call as in ProcessInstanceResourceImpl
    processEngineRule.getRuntimeService().deleteProcessInstance(processInstance.getId(), REASON, false, true);
    entity = getHistoricProcessInstanceWithAssertion(processDefinition);
    assertThat(entity.getState()).isEqualTo(HistoricProcessInstance.STATE_EXTERNALLY_TERMINATED);
    assertEquals(1, processEngineRule.getHistoryService().createHistoricProcessInstanceQuery().externallyTerminated().count());
  }

  @Test
  public void testSateOfScriptTaskProcessWithTransactionCommitAndException() {
    BpmnModelInstance instance = Bpmn.createExecutableProcess(PROCESS_ID)
        .startEvent()
        //add wait state
        .camundaAsyncAfter()
        .scriptTask()
        .scriptText("throw new RuntimeException()")
        .scriptFormat("groovy")
        .endEvent()
        .done();
    ProcessDefinition processDefinition = processEngineTestRule.deployAndGetDefinition(instance);


    try {
      ProcessInstance pi = processEngineRule.getRuntimeService()
          .startProcessInstanceById(processDefinition.getId());
      processEngineRule.getManagementService().executeJob(
          processEngineRule.getManagementService().createJobQuery().executable().singleResult().getId());
      fail("exception expected");
    } catch (Exception e) {
      //expected
    }

    assertThat(processEngineRule.getRuntimeService().createProcessInstanceQuery().active().list().size()).isEqualTo(1);
    HistoricProcessInstance entity = getHistoricProcessInstanceWithAssertion(processDefinition);
    assertThat(entity.getState()).isEqualTo(HistoricProcessInstance.STATE_ACTIVE);
    assertEquals(1, processEngineRule.getHistoryService().createHistoricProcessInstanceQuery().active().count());
  }

  @Test
  public void testErrorEndEvent() {
    BpmnModelInstance process1 = Bpmn.createExecutableProcess(PROCESS_ID)
        .startEvent()
        .endEvent()
        .error("1")
        .done();

    ProcessDefinition processDefinition = processEngineTestRule.deployAndGetDefinition(process1);
    processEngineRule.getRuntimeService().startProcessInstanceById(processDefinition.getId());
    HistoricProcessInstance entity = getHistoricProcessInstanceWithAssertion(processDefinition);
    assertThat(entity.getState()).isEqualTo(HistoricProcessInstance.STATE_COMPLETED);
    assertEquals(1, processEngineRule.getHistoryService().createHistoricProcessInstanceQuery().completed().count());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricProcessInstanceStateTest.testWithCallActivity.bpmn"})
  public void testWithCallActivity() {
    processEngineRule.getRuntimeService().startProcessInstanceByKey("Main_Process");
    assertThat(processEngineRule.getRuntimeService().createProcessInstanceQuery().active().list().size()).isEqualTo(0);

    HistoricProcessInstance entity1 = processEngineRule.getHistoryService().createHistoricProcessInstanceQuery()
        .processDefinitionKey("Main_Process").singleResult();

    HistoricProcessInstance entity2 = processEngineRule.getHistoryService().createHistoricProcessInstanceQuery()
        .processDefinitionKey("Sub_Process").singleResult();

    assertThat(entity1).isNotNull();
    assertThat(entity2).isNotNull();
    assertThat(entity1.getState()).isEqualTo(HistoricProcessInstance.STATE_COMPLETED);
    assertEquals(1, processEngineRule.getHistoryService().createHistoricProcessInstanceQuery().completed().count());
    assertThat(entity2.getState()).isEqualTo(HistoricProcessInstance.STATE_INTERNALLY_TERMINATED);
    assertEquals(1, processEngineRule.getHistoryService().createHistoricProcessInstanceQuery().internallyTerminated().count());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/CAM-9934.bpmn"})
  public void shouldSetCorrectInstanceStateOnInterruption() {
    // given
    processEngineRule.getRuntimeService().startProcessInstanceByKey("Process_1");

    // when
    processEngineRule.getRuntimeService()
      .correlateMessage("SubProcessTrigger");

    HistoricProcessInstance historicProcessInstance = processEngineRule.getHistoryService()
      .createHistoricProcessInstanceQuery()
      .singleResult();

    // then
    assertThat(historicProcessInstance.getState()).isEqualTo(HistoricProcessInstance.STATE_ACTIVE);
    assertThat(historicProcessInstance.getEndTime()).isNull();
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/CAM-9934.bpmn"})
  public void shouldSetRemovalTimeOnHistoricActivityInstances() {
    // given
    processEngineRule.getProcessEngineConfiguration()
        .setHistoryRemovalTimeStrategy("start");

    processEngineRule.getRuntimeService().startProcessInstanceByKey("Process_1");

    // when
    processEngineRule.getRuntimeService()
      .correlateMessage("SubProcessTrigger");

    HistoricTaskInstance taskInstance = processEngineRule.getHistoryService()
        .createHistoricTaskInstanceQuery()
        .taskDefinitionKey("Task_1eg238f")
        .singleResult();

    // then
    assertThat(taskInstance.getRemovalTime()).isNotNull();

    // clear
    processEngineRule.getProcessEngineConfiguration()
        .setHistoryRemovalTimeStrategy("end");
  }

  private HistoricProcessInstance getHistoricProcessInstanceWithAssertion(ProcessDefinition processDefinition) {
    List<HistoricProcessInstance> entities = processEngineRule.getHistoryService().createHistoricProcessInstanceQuery()
        .processDefinitionId(processDefinition.getId()).list();
    assertThat(entities).isNotNull();
    assertThat(entities.size()).isEqualTo(1);
    return entities.get(0);
  }

  protected static void initEndEvent(BpmnModelInstance modelInstance, String endEventId) {
    EndEvent endEvent = modelInstance.getModelElementById(endEventId);
    TerminateEventDefinition terminateDefinition = modelInstance.newInstance(TerminateEventDefinition.class);
    endEvent.addChildElement(terminateDefinition);
  }
}
