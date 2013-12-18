/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.test.bpmn.event.compensate;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.bpmn.event.compensate.helper.SetVariablesDelegate;


/**
 * @author Daniel Meyer
 */
public class CompensateEventTest extends PluggableProcessEngineTestCase {

  @Deployment
  public void testCompensateSubprocess() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

    assertEquals(5, runtimeService.getVariable(processInstance.getId(), "undoBookHotel"));

    runtimeService.signal(processInstance.getId());
    assertProcessEnded(processInstance.getId());

  }

  @Deployment
  public void testCompensateParallelSubprocess() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

    assertEquals(5, runtimeService.getVariable(processInstance.getId(), "undoBookHotel"));

    Task singleResult = taskService.createTaskQuery().singleResult();
    taskService.complete(singleResult.getId());

    runtimeService.signal(processInstance.getId());
    assertProcessEnded(processInstance.getId());

  }

  @Deployment
  public void testCompensateParallelSubprocessCompHandlerWaitstate() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

    List<Task> compensationHandlerTasks = taskService.createTaskQuery().taskDefinitionKey("undoBookHotel").list();
    assertEquals(5, compensationHandlerTasks.size());

    ActivityInstance rootActivityInstance = runtimeService.getActivityInstance(processInstance.getId());
    List<ActivityInstance> compensationHandlerInstances = getInstancesForActivitiyId(rootActivityInstance, "undoBookHotel");
    assertEquals(5, compensationHandlerInstances.size());

    for (Task task : compensationHandlerTasks) {
      taskService.complete(task.getId());
    }

    Task singleResult = taskService.createTaskQuery().singleResult();
    taskService.complete(singleResult.getId());

    runtimeService.signal(processInstance.getId());
    assertProcessEnded(processInstance.getId());

  }

  @Deployment
  public void testCompensateMiSubprocess() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

    assertEquals(5, runtimeService.getVariable(processInstance.getId(), "undoBookHotel"));

    runtimeService.signal(processInstance.getId());
    assertProcessEnded(processInstance.getId());

  }

  @Deployment
  public void testCompensateScope() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

    assertEquals(5, runtimeService.getVariable(processInstance.getId(), "undoBookHotel"));
    assertEquals(5, runtimeService.getVariable(processInstance.getId(), "undoBookFlight"));

    runtimeService.signal(processInstance.getId());
    assertProcessEnded(processInstance.getId());

  }

  // See: https://app.camunda.com/jira/browse/CAM-1410
  @Deployment
  public void testCompensateActivityRef() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

    assertEquals(5, runtimeService.getVariable(processInstance.getId(), "undoBookHotel"));
    assertNull(runtimeService.getVariable(processInstance.getId(), "undoBookFlight"));

    runtimeService.signal(processInstance.getId());
    assertProcessEnded(processInstance.getId());

  }

  @Deployment(resources={
          "org/camunda/bpm/engine/test/bpmn/event/compensate/CompensateEventTest.testCallActivityCompensationHandler.bpmn20.xml",
          "org/camunda/bpm/engine/test/bpmn/event/compensate/CompensationHandler.bpmn20.xml"
  })
  public void testCallActivityCompensationHandler() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

    if(!processEngineConfiguration.getHistory().equals(ProcessEngineConfiguration.HISTORY_NONE)) {
      assertEquals(5, historyService.createHistoricActivityInstanceQuery()
              .activityId("undoBookHotel")
              .count());
    }

    runtimeService.signal(processInstance.getId());
    assertProcessEnded(processInstance.getId());

    assertEquals(0, runtimeService.createProcessInstanceQuery().count());

    if(!processEngineConfiguration.getHistory().equals(ProcessEngineConfiguration.HISTORY_NONE)) {
      assertEquals(6, historyService.createHistoricProcessInstanceQuery()
              .count());
    }

  }

  @Deployment
  public void testCompensateMiSubprocessVariableSnapshots() {

    // see referenced java delegates in the process definition.

    SetVariablesDelegate.variablesMap.clear();

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

    if(!processEngineConfiguration.getHistory().equals(ProcessEngineConfiguration.HISTORY_NONE)) {
      assertEquals(5, historyService.createHistoricActivityInstanceQuery().activityId("undoBookHotel").count());
    }

    assertProcessEnded(processInstance.getId());

  }

  public void testMultipleCompensationCatchEventsFails() {
    try {
      repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/compensate/CompensateEventTest.testMultipleCompensationCatchEventsFails.bpmn20.xml")
        .deploy();
      fail("exception expected");
    } catch (Exception e) {
      if(!e.getMessage().contains("multiple boundary events with compensateEventDefinition not supported on same activity")) {
        fail("different exception expected");
      }
    }
  }

  public void testMultipleCompensationCatchEventsCompensationAttributeMissingFails() {
    try {
      repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/compensate/CompensateEventTest.testMultipleCompensationCatchEventsCompensationAttributeMissingFails.bpmn20.xml")
        .deploy();
      fail("exception expected");
    } catch (Exception e) {
      if(!e.getMessage().contains("compensation boundary catch must be connected to element with isForCompensation=true")) {
        fail("different exception expected");
      }
    }
  }

  public void testInvalidActivityRefFails() {
    try {
      repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/compensate/CompensateEventTest.testInvalidActivityRefFails.bpmn20.xml")
        .deploy();
      fail("exception expected");
    } catch (Exception e) {
      if(!e.getMessage().contains("Invalid attribute value for 'activityRef':")) {
        fail("different exception expected");
      }
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/bpmn/event/compensate/CompensateEventTest.testCompensationTriggeredByEventSubProcessActivityRef.bpmn20.xml"})
  public void testCompensateActivityRefTriggeredByEventSubprocess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");
    assertProcessEnded(processInstance.getId());

    HistoricVariableInstanceQuery historicVariableInstanceQuery = historyService.createHistoricVariableInstanceQuery()
      .processInstanceId(processInstance.getId())
      .variableName("undoBookHotel");

    if(!processEngineConfiguration.getHistory().equals(ProcessEngineConfiguration.HISTORY_NONE)) {
      assertEquals(1, historicVariableInstanceQuery.count());
      assertEquals("undoBookHotel", historicVariableInstanceQuery.list().get(0).getVariableName());
      assertEquals(5, historicVariableInstanceQuery.list().get(0).getValue());

      assertEquals(0, historyService.createHistoricVariableInstanceQuery()
       .processInstanceId(processInstance.getId())
       .variableName("undoBookFlight")
       .count());
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/bpmn/event/compensate/CompensateEventTest.testCompensationTriggeredByEventSubProcessInSubProcessActivityRef.bpmn20.xml"})
  public void testCompensateActivityRefTriggeredByEventSubprocessInSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");
    assertProcessEnded(processInstance.getId());

    HistoricVariableInstanceQuery historicVariableInstanceQuery = historyService.createHistoricVariableInstanceQuery()
      .processInstanceId(processInstance.getId())
      .variableName("undoBookHotel");

    if(!processEngineConfiguration.getHistory().equals(ProcessEngineConfiguration.HISTORY_NONE)) {
      assertEquals(1, historicVariableInstanceQuery.count());
      assertEquals("undoBookHotel", historicVariableInstanceQuery.list().get(0).getVariableName());
      assertEquals(5, historicVariableInstanceQuery.list().get(0).getValue());

      assertEquals(0, historyService.createHistoricVariableInstanceQuery()
        .processInstanceId(processInstance.getId())
        .variableName("undoBookFlight")
        .count());
    }
  }

  public void testIllegalCompensateActivityRefParentScope() {

    try {
      String id = repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/compensate/CompensateEventTest.testIllegalCompensateActivityRefParentScope.bpmn20.xml")
        .deploy()
        .getId();
      repositoryService.deleteDeployment(id, true);
      fail("Exception expected!");

    } catch (ProcessEngineException e) {
      if(!e.getMessage().contains("Invalid attribute value for 'activityRef': no activity with id 'someServiceInMainProcess' in scope 'subProcess'")) {
        fail("different exception expected");
      }
    }

  }

  public void testIllegalCompensateActivityRefNestedScope() {

    try {
      String id = repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/compensate/CompensateEventTest.testIllegalCompensateActivityRefNestedScope.bpmn20.xml")
        .deploy()
        .getId();
      repositoryService.deleteDeployment(id, true);
      fail("Exception expected!");

    } catch (ProcessEngineException e) {
      if(!e.getMessage().contains("Invalid attribute value for 'activityRef': no activity with id 'someServiceInNestedScope' in scope 'subProcess'")) {
        fail("different exception expected");
      }
    }

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/bpmn/event/compensate/CompensateEventTest.testCompensationInEventSubProcessActivityRef.bpmn20.xml"})
  public void testCompensateActivityRefInEventSubprocess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");
    assertProcessEnded(processInstance.getId());


    HistoricVariableInstanceQuery historicVariableInstanceQuery = historyService.createHistoricVariableInstanceQuery()
      .variableName("undoBookSecondHotel");

    if(!processEngineConfiguration.getHistory().equals(ProcessEngineConfiguration.HISTORY_NONE)) {
      assertEquals(1, historicVariableInstanceQuery.count());
      assertEquals("undoBookSecondHotel", historicVariableInstanceQuery.list().get(0).getVariableName());
      assertEquals(5, historicVariableInstanceQuery.list().get(0).getValue());

      assertEquals(0, historyService.createHistoricVariableInstanceQuery()
        .processInstanceId(processInstance.getId())
        .variableName("undoBookFlight")
        .count());

      assertEquals(0, historyService.createHistoricVariableInstanceQuery()
        .processInstanceId(processInstance.getId())
        .variableName("undoBookHotel")
        .count());
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/bpmn/event/compensate/CompensateEventTest.testCompensationInEventSubProcess.bpmn20.xml"})
  public void testCompensateInEventSubprocess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");
    assertProcessEnded(processInstance.getId());

    HistoricVariableInstanceQuery historicVariableInstanceQuery = historyService.createHistoricVariableInstanceQuery()
        .variableName("undoBookSecondHotel");

    if(!processEngineConfiguration.getHistory().equals(ProcessEngineConfiguration.HISTORY_NONE)) {
      assertEquals(1, historicVariableInstanceQuery.count());
      assertEquals("undoBookSecondHotel", historicVariableInstanceQuery.list().get(0).getVariableName());
      assertEquals(5, historicVariableInstanceQuery.list().get(0).getValue());

      historicVariableInstanceQuery = historyService.createHistoricVariableInstanceQuery()
          .variableName("undoBookFlight");

      assertEquals(1, historicVariableInstanceQuery.count());
      assertEquals(5, historicVariableInstanceQuery.list().get(0).getValue());

      historicVariableInstanceQuery = historyService.createHistoricVariableInstanceQuery()
          .variableName("undoBookHotel");

      assertEquals(1, historicVariableInstanceQuery.count());
      assertEquals(5, historicVariableInstanceQuery.list().get(0).getValue());
    }
  }

}
