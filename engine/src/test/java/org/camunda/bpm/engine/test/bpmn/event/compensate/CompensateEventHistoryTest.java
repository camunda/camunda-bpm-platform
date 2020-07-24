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
package org.camunda.bpm.engine.test.bpmn.event.compensate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Thorben Lindhauer
 *
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
public class CompensateEventHistoryTest extends PluggableProcessEngineTest {

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/compensate/CompensateEventHistoryTest.testBoundaryCompensationHandlerHistory.bpmn20.xml")
  @Test
  public void testBoundaryCompensationHandlerHistoryActivityInstance() {
    // given a process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("boundaryHandlerProcess");

    // when throwing compensation
    Task beforeCompensationTask = taskService.createTaskQuery().singleResult();
    taskService.complete(beforeCompensationTask.getId());

    String compensationHandlerActivityInstanceId = runtimeService
        .getActivityInstance(processInstance.getId())
        .getActivityInstances("compensationHandler")[0]
        .getId();

    // .. and completing compensation
    Task compensationHandler = taskService.createTaskQuery().singleResult();
    taskService.complete(compensationHandler.getId());

    // then there is a historic activity instance for the compensation handler
    HistoricActivityInstance historicCompensationHandlerInstance = historyService
        .createHistoricActivityInstanceQuery()
        .activityId("compensationHandler")
        .singleResult();

    assertNotNull(historicCompensationHandlerInstance);
    assertEquals(compensationHandlerActivityInstanceId, historicCompensationHandlerInstance.getId());
    assertEquals(processInstance.getId(), historicCompensationHandlerInstance.getParentActivityInstanceId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/compensate/CompensateEventHistoryTest.testBoundaryCompensationHandlerHistory.bpmn20.xml")
  @Ignore("Fix CAM-4351")
  @Test
  public void testBoundaryCompensationHandlerHistoryVariableInstance() {
    // given a process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("boundaryHandlerProcess");

    // when throwing compensation
    Task beforeCompensationTask = taskService.createTaskQuery().singleResult();
    taskService.complete(beforeCompensationTask.getId());

    String compensationHandlerActivityInstanceId = runtimeService
        .getActivityInstance(processInstance.getId())
        .getActivityInstances("compensationHandler")[0]
        .getId();

    // .. setting a variable via task service API
    Task compensationHandler = taskService.createTaskQuery().singleResult();
    runtimeService.setVariableLocal(compensationHandler.getExecutionId(), "apiVariable", "someValue");

    // .. and completing compensation
    taskService.complete(compensationHandler.getId());

    // then there is a historic variable instance for the variable set by API
    HistoricVariableInstance historicVariableInstance = historyService.createHistoricVariableInstanceQuery().singleResult();

    assertNotNull(historicVariableInstance);
    assertEquals(compensationHandlerActivityInstanceId, historicVariableInstance.getActivityInstanceId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/compensate/CompensateEventHistoryTest.testDefaultCompensationHandlerHistory.bpmn20.xml")
  @Test
  public void testDefaultCompensationHandlerHistoryActivityInstance() {
    // given a process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("defaultHandlerProcess");

    // when throwing compensation
    Task beforeCompensationTask = taskService.createTaskQuery().singleResult();
    taskService.complete(beforeCompensationTask.getId());

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    String compensationHandlerActivityInstanceId = tree
        .getActivityInstances("compensationHandler")[0]
        .getId();

    String subProcessActivityInstanceId = tree
        .getActivityInstances("subProcess")[0]
        .getId();

    // .. and completing compensation
    Task compensationHandler = taskService.createTaskQuery().singleResult();
    taskService.complete(compensationHandler.getId());

    // then there is a historic activity instance for the compensation handler
    HistoricActivityInstance historicCompensationHandlerInstance = historyService
        .createHistoricActivityInstanceQuery()
        .activityId("compensationHandler")
        .singleResult();

    assertNotNull(historicCompensationHandlerInstance);
    assertEquals(compensationHandlerActivityInstanceId, historicCompensationHandlerInstance.getId());
    assertEquals(subProcessActivityInstanceId, historicCompensationHandlerInstance.getParentActivityInstanceId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/compensate/CompensateEventHistoryTest.testDefaultCompensationHandlerHistory.bpmn20.xml")
  @Ignore("Fix CAM-4351")
  @Test
  public void testDefaultCompensationHandlerHistoryVariableInstance() {
    // given a process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("defaultHandlerProcess");

    // when throwing compensation
    Task beforeCompensationTask = taskService.createTaskQuery().singleResult();
    taskService.complete(beforeCompensationTask.getId());

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    String compensationHandlerActivityInstanceId = tree
        .getActivityInstances("compensationHandler")[0]
        .getId();

    // .. setting a variable via task service API
    Task compensationHandler = taskService.createTaskQuery().singleResult();
    runtimeService.setVariableLocal(compensationHandler.getExecutionId(), "apiVariable", "someValue");

    // .. and completing compensation
    taskService.complete(compensationHandler.getId());

    // then there is a historic variable instance for the variable set by API
    HistoricVariableInstance historicVariableInstance = historyService.createHistoricVariableInstanceQuery().singleResult();

    assertNotNull(historicVariableInstance);
    assertEquals(compensationHandlerActivityInstanceId, historicVariableInstance.getActivityInstanceId());
  }


}
