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

package org.camunda.bpm.engine.test.history;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.history.*;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.CollectionUtil;
import org.camunda.bpm.engine.runtime.*;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.runtime.util.CustomSerializable;
import org.camunda.bpm.engine.test.api.runtime.util.FailingSerializable;
import org.camunda.bpm.engine.test.cmmn.decisiontask.TestPojo;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Assert;

import java.io.Serializable;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


/**
 * @author Christian Lipphardt (camunda)
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
public class HistoricVariableInstanceTest extends PluggableProcessEngineTestCase {

  @Deployment(resources={
    "org/camunda/bpm/engine/test/history/orderProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/history/checkCreditProcess.bpmn20.xml"
  })
  public void testOrderProcessWithCallActivity() {
    // After the process has started, the 'verify credit history' task should be active
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("orderProcess");
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task verifyCreditTask = taskQuery.singleResult();
    assertEquals("Verify credit history", verifyCreditTask.getName());

    // Verify with Query API
    ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(pi.getId()).singleResult();
    assertNotNull(subProcessInstance);
    assertEquals(pi.getId(), runtimeService.createProcessInstanceQuery().subProcessInstanceId(subProcessInstance.getId()).singleResult().getId());

    // Completing the task with approval, will end the subprocess and continue the original process
    taskService.complete(verifyCreditTask.getId(), CollectionUtil.singletonMap("creditApproved", true));
    Task prepareAndShipTask = taskQuery.singleResult();
    assertEquals("Prepare and Ship", prepareAndShipTask.getName());
  }

  @Deployment
  public void testSimple() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("myProc");
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task userTask = taskQuery.singleResult();
    assertEquals("userTask1", userTask.getName());

    taskService.complete(userTask.getId(), CollectionUtil.singletonMap("myVar", "test789"));

    assertProcessEnded(processInstance.getId());

    List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery().list();
    assertEquals(1, variables.size());

    HistoricVariableInstanceEntity historicVariable = (HistoricVariableInstanceEntity) variables.get(0);
    assertEquals("test456", historicVariable.getTextValue());

    assertEquals(5, historyService.createHistoricActivityInstanceQuery().count());

    if (isFullHistoryEnabled()) {
      assertEquals(3, historyService.createHistoricDetailQuery().count());
    }
  }

  @Deployment
  public void testSimpleNoWaitState() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("myProc");
    assertProcessEnded(processInstance.getId());

    List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery().list();
    assertEquals(1, variables.size());

    HistoricVariableInstanceEntity historicVariable = (HistoricVariableInstanceEntity) variables.get(0);
    assertEquals("test456", historicVariable.getTextValue());

    assertEquals(4, historyService.createHistoricActivityInstanceQuery().count());

    if (isFullHistoryEnabled()) {
      assertEquals(2, historyService.createHistoricDetailQuery().count());
    }
  }

  @Deployment
  public void testParallel() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("myProc");
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task userTask = taskQuery.singleResult();
    assertEquals("userTask1", userTask.getName());

    taskService.complete(userTask.getId(), CollectionUtil.singletonMap("myVar", "test789"));

    assertProcessEnded(processInstance.getId());

    List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery().orderByVariableName().asc().list();
    assertEquals(2, variables.size());

    HistoricVariableInstanceEntity historicVariable = (HistoricVariableInstanceEntity) variables.get(0);
    assertEquals("myVar", historicVariable.getName());
    assertEquals("test789", historicVariable.getTextValue());

    HistoricVariableInstanceEntity historicVariable1 = (HistoricVariableInstanceEntity) variables.get(1);
    assertEquals("myVar1", historicVariable1.getName());
    assertEquals("test456", historicVariable1.getTextValue());

    assertEquals(8, historyService.createHistoricActivityInstanceQuery().count());

    if (isFullHistoryEnabled()) {
      assertEquals(5, historyService.createHistoricDetailQuery().count());
    }
  }

  @Deployment
  public void testParallelNoWaitState() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("myProc");
    assertProcessEnded(processInstance.getId());

    List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery().list();
    assertEquals(1, variables.size());

    HistoricVariableInstanceEntity historicVariable = (HistoricVariableInstanceEntity) variables.get(0);
    assertEquals("test456", historicVariable.getTextValue());

    assertEquals(7, historyService.createHistoricActivityInstanceQuery().count());

    if (isFullHistoryEnabled()) {
      assertEquals(2, historyService.createHistoricDetailQuery().count());
    }
  }

  @Deployment
  public void testTwoSubProcessInParallelWithinSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoSubProcessInParallelWithinSubProcess");
    assertProcessEnded(processInstance.getId());

    List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery().orderByVariableName().asc().list();
    assertEquals(2, variables.size());

    HistoricVariableInstanceEntity historicVariable = (HistoricVariableInstanceEntity) variables.get(0);
    assertEquals("myVar", historicVariable.getName());
    assertEquals("test101112", historicVariable.getTextValue());
    assertEquals("string", historicVariable.getVariableTypeName());
    assertEquals("string", historicVariable.getTypeName());

    HistoricVariableInstanceEntity historicVariable1 = (HistoricVariableInstanceEntity) variables.get(1);
    assertEquals("myVar1", historicVariable1.getName());
    assertEquals("test789", historicVariable1.getTextValue());
    assertEquals("string", historicVariable1.getVariableTypeName());
    assertEquals("string", historicVariable1.getTypeName());

    assertEquals(18, historyService.createHistoricActivityInstanceQuery().count());

    if (isFullHistoryEnabled()) {
      assertEquals(7, historyService.createHistoricDetailQuery().count());
    }
  }

  @Deployment(resources={
          "org/camunda/bpm/engine/test/history/HistoricVariableInstanceTest.testCallSimpleSubProcess.bpmn20.xml",
          "org/camunda/bpm/engine/test/history/simpleSubProcess.bpmn20.xml"
  })
  public void testHistoricVariableInstanceQuery() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callSimpleSubProcess");
    assertProcessEnded(processInstance.getId());

    assertEquals(5, historyService.createHistoricVariableInstanceQuery().count());
    assertEquals(5, historyService.createHistoricVariableInstanceQuery().list().size());
    assertEquals(5, historyService.createHistoricVariableInstanceQuery().orderByProcessInstanceId().asc().count());
    assertEquals(5, historyService.createHistoricVariableInstanceQuery().orderByProcessInstanceId().asc().list().size());
    assertEquals(5, historyService.createHistoricVariableInstanceQuery().orderByVariableName().asc().count());
    assertEquals(5, historyService.createHistoricVariableInstanceQuery().orderByVariableName().asc().list().size());

    assertEquals(2, historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).count());
    assertEquals(2, historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).list().size());
    assertEquals(2, historyService.createHistoricVariableInstanceQuery().variableName("myVar").count());
    assertEquals(2, historyService.createHistoricVariableInstanceQuery().variableName("myVar").list().size());
    assertEquals(2, historyService.createHistoricVariableInstanceQuery().variableNameLike("myVar1").count());
    assertEquals(2, historyService.createHistoricVariableInstanceQuery().variableNameLike("myVar1").list().size());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableNameLike("my\\_Var%").count());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableNameLike("my\\_Var%").list().size());

    List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery().list();
    assertEquals(5, variables.size());

    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableValueEquals("myVar", "test123").count());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableValueEquals("myVar", "test123").list().size());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableValueEquals("myVar1", "test456").count());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableValueEquals("myVar1", "test456").list().size());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableValueEquals("myVar", "test666").count());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableValueEquals("myVar", "test666").list().size());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableValueEquals("myVar1", "test666").count());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableValueEquals("myVar1", "test666").list().size());

    assertEquals(8, historyService.createHistoricActivityInstanceQuery().count());

    if (isFullHistoryEnabled()) {
      assertEquals(6, historyService.createHistoricDetailQuery().count());
    }

    // non-existing id:
    assertEquals(0, historyService.createHistoricVariableInstanceQuery().variableId("non-existing").count());

    // existing-id
    List<HistoricVariableInstance> variable = historyService.createHistoricVariableInstanceQuery().listPage(0, 1);
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableId(variable.get(0).getId()).count());

  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/history/HistoricVariableInstanceTest.testCallSubProcessSettingVariableOnStart.bpmn20.xml",
      "org/camunda/bpm/engine/test/history/subProcessSetVariableOnStart.bpmn20.xml"
  })
  public void testCallSubProcessSettingVariableOnStart() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callSubProcess");
    assertProcessEnded(processInstance.getId());

    assertEquals(1, historyService.createHistoricVariableInstanceQuery().count());

    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableValueEquals("aVariable", "aValue").count());
  }

  @Deployment(resources={
          "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
  })
  public void testHistoricProcessVariableOnDeletion() {
    HashMap<String, Object> variables = new HashMap<String,  Object>();
    variables.put("testVar", "Hallo Christian");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    runtimeService.deleteProcessInstance(processInstance.getId(), "deleted");
    assertProcessEnded(processInstance.getId());

    // check that process variable is set even if the process is canceled and not ended normally
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).variableValueEquals("testVar", "Hallo Christian").count());
  }


  @Deployment(resources={"org/camunda/bpm/engine/test/standalone/history/FullHistoryTest.testVariableUpdatesAreLinkedToActivity.bpmn20.xml"})
  public void testVariableUpdatesLinkedToActivity() throws Exception {
    if (isFullHistoryEnabled()) {
      ProcessInstance pi = runtimeService.startProcessInstanceByKey("ProcessWithSubProcess");

      Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
      Map<String, Object> variables = new HashMap<String, Object>();
      variables.put("test", "1");
      taskService.complete(task.getId(), variables);

      // now we are in the subprocess
      task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
      variables.clear();
      variables.put("test", "2");
      taskService.complete(task.getId(), variables);

      // now we are ended
      assertProcessEnded(pi.getId());

      // check history
      List<HistoricDetail> updates = historyService.createHistoricDetailQuery().variableUpdates().list();
      assertEquals(2, updates.size());

      Map<String, HistoricVariableUpdate> updatesMap = new HashMap<String, HistoricVariableUpdate>();
      HistoricVariableUpdate update = (HistoricVariableUpdate) updates.get(0);
      updatesMap.put((String) update.getValue(), update);
      update = (HistoricVariableUpdate) updates.get(1);
      updatesMap.put((String) update.getValue(), update);

      HistoricVariableUpdate update1 = updatesMap.get("1");
      HistoricVariableUpdate update2 = updatesMap.get("2");

      assertNotNull(update1.getActivityInstanceId());
      assertNotNull(update1.getExecutionId());
      HistoricActivityInstance historicActivityInstance1 = historyService.createHistoricActivityInstanceQuery().activityInstanceId(update1.getActivityInstanceId()).singleResult();
      assertEquals(historicActivityInstance1.getExecutionId(), update1.getExecutionId());
      assertEquals("usertask1", historicActivityInstance1.getActivityId());

      // TODO http://jira.codehaus.org/browse/ACT-1083
      assertNotNull(update2.getActivityInstanceId());
      HistoricActivityInstance historicActivityInstance2 = historyService.createHistoricActivityInstanceQuery().activityInstanceId(update2.getActivityInstanceId()).singleResult();
      assertEquals("usertask2", historicActivityInstance2.getActivityId());

    /*
     * This is OK! The variable is set on the root execution, on a execution never run through the activity, where the process instances
     * stands when calling the set Variable. But the ActivityId of this flow node is used. So the execution id's doesn't have to be equal.
     *
     * execution id: On which execution it was set
     * activity id: in which activity was the process instance when setting the variable
     */
      assertFalse(historicActivityInstance2.getExecutionId().equals(update2.getExecutionId()));
    }
  }

  // Test for ACT-1528, which (correctly) reported that deleting any
  // historic process instance would remove ALL historic variables.
  // Yes. Real serious bug.
  @Deployment
  public void testHistoricProcessInstanceDeleteCascadesCorrectly() {

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("var1", "value1");
    variables.put("var2", "value2");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("myProcess", variables);
    assertNotNull(processInstance);

    variables = new HashMap<String, Object>();
    variables.put("var3", "value3");
    variables.put("var4", "value4");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("myProcess", variables);
    assertNotNull(processInstance2);

    // check variables
    long count = historyService.createHistoricVariableInstanceQuery().count();
    assertEquals(4, count);

    // delete runtime execution of ONE process instance
    runtimeService.deleteProcessInstance(processInstance.getId(), "reason 1");
    historyService.deleteHistoricProcessInstance(processInstance.getId());

    // recheck variables
    // this is a bug: all variables was deleted after delete a history processinstance
    count = historyService.createHistoricVariableInstanceQuery().count();
    assertEquals(2, count);

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/history/HistoricVariableInstanceTest.testParallel.bpmn20.xml"})
  public void testHistoricVariableInstanceQueryByTaskIds() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("myProc");

    TaskQuery taskQuery = taskService.createTaskQuery();
    Task userTask = taskQuery.singleResult();
    assertEquals("userTask1", userTask.getName());

    // set local variable on user task
    taskService.setVariableLocal(userTask.getId(), "taskVariable", "aCustomValue");

    // complete user task to finish process instance
    taskService.complete(userTask.getId());

    assertProcessEnded(processInstance.getId());

    List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstance.getProcessInstanceId()).list();
    assertEquals(1, tasks.size());

    // check existing variables
    assertEquals(3, historyService.createHistoricVariableInstanceQuery().count());

    // check existing variables for task ID
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().taskIdIn(tasks.get(0).getId()).list().size());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().taskIdIn(tasks.get(0).getId()).count());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/history/HistoricVariableInstanceTest.testParallel.bpmn20.xml"})
  public void testHistoricVariableInstanceQueryByProcessIdIn() {
    // given
    Map<String, Object> vars = new HashMap<String, Object>();
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("myProc",vars);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("myProc",vars);

    // check existing variables for process instance ID
    assertEquals(4, historyService.createHistoricVariableInstanceQuery().processInstanceIdIn(processInstance.getProcessInstanceId(),processInstance2.getProcessInstanceId()).count());
    assertEquals(4, historyService.createHistoricVariableInstanceQuery().processInstanceIdIn(processInstance.getProcessInstanceId(),processInstance2.getProcessInstanceId()).list().size());

    //add check with not existing search
    String notExistingSearch = processInstance.getProcessInstanceId() + "-notExisting";
    assertEquals(2, historyService.createHistoricVariableInstanceQuery().processInstanceIdIn(notExistingSearch,processInstance2.getProcessInstanceId()).count());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/history/HistoricVariableInstanceTest.testParallel.bpmn20.xml"})
  public void testHistoricVariableInstanceQueryByInvalidProcessIdIn() {
    // given
    Map<String, Object> vars = new HashMap<String, Object>();
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("myProc",vars);

    // check existing variables for task ID
    try {
      historyService.createHistoricVariableInstanceQuery().processInstanceIdIn(processInstance.getProcessInstanceId(),null);
      fail("Search by process instance ID was finished");
    } catch (ProcessEngineException e) { }

    try {
      historyService.createHistoricVariableInstanceQuery().processInstanceIdIn(null,processInstance.getProcessInstanceId());
      fail("Search by process instance ID was finished");
    } catch (ProcessEngineException e) { }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testHistoricVariableInstanceQueryByExecutionIds() {
    // given
    Map<String, Object> variables1 = new HashMap<String, Object>();
    variables1.put("stringVar", "test");
    variables1.put("myVar", "test123");
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables1);

    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery().executionIdIn(processInstance1.getId());
    assertEquals(2, query.count());
    List<HistoricVariableInstance> variableInstances = query.list();
    assertEquals(2, variableInstances.size());
    for (HistoricVariableInstance variableInstance : variableInstances) {
      assertEquals(processInstance1.getId(), variableInstance.getExecutionId());
    }

    Map<String, Object> variables2 = new HashMap<String, Object>();
    variables2.put("myVar", "test123");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables2);

    query = historyService.createHistoricVariableInstanceQuery().executionIdIn(processInstance1.getId(), processInstance2.getId());
    assertEquals(3, query.list().size());
    assertEquals(3, query.count());
  }

  public void testQueryByInvalidExecutionIdIn() {
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery().executionIdIn("invalid");
    assertEquals(0, query.count());

    try {
      historyService.createHistoricVariableInstanceQuery().executionIdIn(null);
      fail("A ProcessEngineExcpetion was expected.");
    } catch (ProcessEngineException e) {}

    try {
      historyService.createHistoricVariableInstanceQuery().executionIdIn((String)null);
      fail("A ProcessEngineExcpetion was expected.");
    } catch (ProcessEngineException e) {}
  }

  public void testQueryByInvalidTaskIdIn() {
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery().taskIdIn("invalid");
    assertEquals(0, query.count());

    try {
      historyService.createHistoricVariableInstanceQuery().taskIdIn(null);
      fail("A ProcessEngineExcpetion was expected.");
    } catch (ProcessEngineException e) {}

    try {
      historyService.createHistoricVariableInstanceQuery().taskIdIn((String)null);
      fail("A ProcessEngineExcpetion was expected.");
    } catch (ProcessEngineException e) {}
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByActivityInstanceIdIn() {
    // given
    Map<String, Object> variables1 = new HashMap<String, Object>();
    variables1.put("stringVar", "test");
    variables1.put("myVar", "test123");
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables1);

    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    query.activityInstanceIdIn(processInstance1.getId());

    assertEquals(2, query.list().size());
    assertEquals(2, query.count());

    Map<String, Object> variables2 = new HashMap<String, Object>();
    variables2.put("myVar", "test123");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables2);

    query.activityInstanceIdIn(processInstance1.getId(), processInstance2.getId());

    assertEquals(3, query.list().size());
    assertEquals(3, query.count());
  }

  public void testQueryByInvalidActivityInstanceIdIn() {
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    query.taskIdIn("invalid");
    assertEquals(0, query.count());

    try {
      query.taskIdIn(null);
      fail("A ProcessEngineExcpetion was expected.");
    } catch (ProcessEngineException e) {}

    try {
      query.taskIdIn((String)null);
      fail("A ProcessEngineExcpetion was expected.");
    } catch (ProcessEngineException e) {}
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByVariableTypeIn() {
    // given
    Map<String, Object> variables1 = new HashMap<String, Object>();
    variables1.put("stringVar", "test");
    variables1.put("boolVar", true);
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables1);

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery().variableTypeIn("string");

    // then
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
    assertEquals(query.list().get(0).getName(), "stringVar");
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByVariableTypeInWithCapitalLetter() {
    // given
    Map<String, Object> variables1 = new HashMap<String, Object>();
    variables1.put("stringVar", "test");
    variables1.put("boolVar", true);
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables1);

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();
    query.variableTypeIn("Boolean");

    // then
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
    assertEquals(query.list().get(0).getName(), "boolVar");
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByVariableTypeInWithSeveralTypes() {
    // given
    Map<String, Object> variables1 = new HashMap<String, Object>();
    variables1.put("stringVar", "test");
    variables1.put("boolVar", true);
    variables1.put("intVar", 5);
    variables1.put("nullVar", null);
    variables1.put("pojoVar", new TestPojo("str", .0));
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables1);

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();
    query.variableTypeIn("BooLEAN", "string", "Serializable");

    // then
    assertEquals(3, query.list().size());
    assertEquals(3, query.count());
  }

  public void testQueryByInvalidVariableTypeIn() {
    // given
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // when
    query.variableTypeIn("invalid");

    // then
    assertEquals(0, query.count());

    try {
      // when
      query.variableTypeIn(null);
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
      // then fails
    }

    try {
      // when
      query.variableTypeIn((String)null);
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
      // then fails
    }
  }

  public void testBinaryFetchingEnabled() {

    // by default, binary fetching is enabled

    Task newTask = taskService.newTask();
    taskService.saveTask(newTask);

    String variableName = "binaryVariableName";
    taskService.setVariable(newTask.getId(), variableName, "some bytes".getBytes());

    HistoricVariableInstance variableInstance = historyService.createHistoricVariableInstanceQuery()
      .variableName(variableName)
      .singleResult();

    assertNotNull(variableInstance.getValue());

    taskService.deleteTask(newTask.getId(), true);
  }

  public void testBinaryFetchingDisabled() {

    Task newTask = taskService.newTask();
    taskService.saveTask(newTask);

    String variableName = "binaryVariableName";
    taskService.setVariable(newTask.getId(), variableName, "some bytes".getBytes());

    HistoricVariableInstance variableInstance = historyService.createHistoricVariableInstanceQuery()
      .variableName(variableName)
      .disableBinaryFetching()
      .singleResult();

    assertNull(variableInstance.getValue());

    taskService.deleteTask(newTask.getId(), true);
  }

  @Deployment(resources= "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml")
  public void testDisableBinaryFetchingForFileValues() {
    // given
    String fileName = "text.txt";
    String encoding = "crazy-encoding";
    String mimeType = "martini/dry";

    FileValue fileValue = Variables
        .fileValue(fileName)
        .file("ABC".getBytes())
        .encoding(encoding)
        .mimeType(mimeType)
        .create();

    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.createVariables().putValueTyped("fileVar", fileValue));

    // when enabling binary fetching
    HistoricVariableInstance fileVariableInstance =
        historyService.createHistoricVariableInstanceQuery().singleResult();

    // then the binary value is accessible
    assertNotNull(fileVariableInstance.getValue());

    // when disabling binary fetching
    fileVariableInstance =
        historyService.createHistoricVariableInstanceQuery().disableBinaryFetching().singleResult();

    // then the byte value is not fetched
    assertNotNull(fileVariableInstance);
    assertEquals("fileVar", fileVariableInstance.getName());

    assertNull(fileVariableInstance.getValue());

    FileValue typedValue = (FileValue) fileVariableInstance.getTypedValue();
    assertNull(typedValue.getValue());

    // but typed value metadata is accessible
    assertEquals(ValueType.FILE, typedValue.getType());
    assertEquals(fileName, typedValue.getFilename());
    assertEquals(encoding, typedValue.getEncoding());
    assertEquals(mimeType, typedValue.getMimeType());

  }

  public void testDisableCustomObjectDeserialization() {
    // given
    Task newTask = taskService.newTask();
    taskService.saveTask(newTask);

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("customSerializable", new CustomSerializable());
    variables.put("failingSerializable", new FailingSerializable());
    taskService.setVariables(newTask.getId(), variables);

    // when
    List<HistoricVariableInstance> variableInstances = historyService.createHistoricVariableInstanceQuery()
      .disableCustomObjectDeserialization()
      .list();

    // then
    assertEquals(2, variableInstances.size());

    for (HistoricVariableInstance variableInstance : variableInstances) {
      assertNull(variableInstance.getErrorMessage());

      ObjectValue typedValue = (ObjectValue) variableInstance.getTypedValue();
      assertNotNull(typedValue);
      assertFalse(typedValue.isDeserialized());
      // cannot access the deserialized value
      try {
        typedValue.getValue();
      }
      catch(IllegalStateException e) {
        assertTextPresent("Object is not deserialized", e.getMessage());
      }
      assertNotNull(typedValue.getValueSerialized());
    }

    taskService.deleteTask(newTask.getId(), true);

  }

  public void testDisableCustomObjectDeserializationNativeQuery() {
    // given
    Task newTask = taskService.newTask();
    taskService.saveTask(newTask);

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("customSerializable", new CustomSerializable());
    variables.put("failingSerializable", new FailingSerializable());
    taskService.setVariables(newTask.getId(), variables);

    // when
    List<HistoricVariableInstance> variableInstances = historyService.createNativeHistoricVariableInstanceQuery()
      .sql("SELECT * from " + managementService.getTableName(HistoricVariableInstance.class))
      .disableCustomObjectDeserialization()
      .list();

    // then
    assertEquals(2, variableInstances.size());

    for (HistoricVariableInstance variableInstance : variableInstances) {
      assertNull(variableInstance.getErrorMessage());

      ObjectValue typedValue = (ObjectValue) variableInstance.getTypedValue();
      assertNotNull(typedValue);
      assertFalse(typedValue.isDeserialized());
      // cannot access the deserialized value
      try {
        typedValue.getValue();
      }
      catch(IllegalStateException e) {
        assertTextPresent("Object is not deserialized", e.getMessage());
      }
      assertNotNull(typedValue.getValueSerialized());
    }

    taskService.deleteTask(newTask.getId(), true);
  }

  public void testErrorMessage() {

    Task newTask = taskService.newTask();
    taskService.saveTask(newTask);

    String variableName = "failingSerializable";
    taskService.setVariable(newTask.getId(), variableName, new FailingSerializable());

    HistoricVariableInstance variableInstance = historyService.createHistoricVariableInstanceQuery()
      .variableName(variableName)
      .singleResult();

    assertNull(variableInstance.getValue());
    assertNotNull(variableInstance.getErrorMessage());

    taskService.deleteTask(newTask.getId(), true);
  }

  @Deployment
  public void testHistoricVariableInstanceRevision() {
    // given:
    // a finished process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    assertProcessEnded(processInstance.getId());

    // when

    // then
    HistoricVariableInstance variable = historyService
      .createHistoricVariableInstanceQuery()
      .singleResult();

    assertNotNull(variable);

    HistoricVariableInstanceEntity variableEntity = (HistoricVariableInstanceEntity) variable;

    // the revision has to be 0
    assertEquals(0, variableEntity.getRevision());

    if (isFullHistoryEnabled()) {
      List<HistoricDetail> details = historyService
        .createHistoricDetailQuery()
        .orderByVariableRevision()
        .asc()
        .list();

      for (HistoricDetail detail : details) {
        HistoricVariableUpdate variableDetail = (HistoricVariableUpdate) detail;
        assertEquals(0, variableDetail.getRevision());
      }
    }
  }

  @Deployment
  public void testHistoricVariableInstanceRevisionAsync() {
    // given:
    // a finished process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    // when
    executeAvailableJobs();

    // then
    assertProcessEnded(processInstance.getId());

    HistoricVariableInstance variable = historyService
      .createHistoricVariableInstanceQuery()
      .singleResult();

    assertNotNull(variable);

    HistoricVariableInstanceEntity variableEntity = (HistoricVariableInstanceEntity) variable;

    // the revision has to be 2
    assertEquals(2, variableEntity.getRevision());

    if (isFullHistoryEnabled()) {
      List<HistoricDetail> details = historyService
        .createHistoricDetailQuery()
        .orderByVariableRevision()
        .asc()
        .list();

      int i = 0;
      for (HistoricDetail detail : details) {
        HistoricVariableUpdate variableDetail = (HistoricVariableUpdate) detail;
        assertEquals(i, variableDetail.getRevision());
        i++;
      }
    }

  }

  /**
   * CAM-3442
   */
  @Deployment
  @SuppressWarnings("unchecked")
  public void testImplicitVariableUpdate() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("serviceTaskProcess",
        Variables.createVariables()
          .putValue("listVar", new ArrayList<String>())
          .putValue("delegate", new UpdateValueDelegate()));

    List<String> list = (List<String>) runtimeService.getVariable(instance.getId(), "listVar");
    assertNotNull(list);
    assertEquals(1, list.size());
    assertEquals(UpdateValueDelegate.NEW_ELEMENT, list.get(0));

    HistoricVariableInstance historicVariableInstance = historyService
        .createHistoricVariableInstanceQuery()
        .variableName("listVar").singleResult();

    List<String> historicList = (List<String>) historicVariableInstance.getValue();
    assertNotNull(historicList);
    assertEquals(1, historicList.size());
    assertEquals(UpdateValueDelegate.NEW_ELEMENT, historicList.get(0));

    if (isFullHistoryEnabled()) {
      List<HistoricDetail> historicDetails = historyService
          .createHistoricDetailQuery()
          .variableUpdates()
          .variableInstanceId(historicVariableInstance.getId())
          .orderPartiallyByOccurrence().asc()
          .list();

      assertEquals(2, historicDetails.size());

      HistoricVariableUpdate update1 = (HistoricVariableUpdate) historicDetails.get(0);
      HistoricVariableUpdate update2 = (HistoricVariableUpdate) historicDetails.get(1);

      List<String> value1 = (List<String>) update1.getValue();

      assertNotNull(value1);
      assertTrue(value1.isEmpty());

      List<String> value2 = (List<String>) update2.getValue();

      assertNotNull(value2);
      assertEquals(1, value2.size());
      assertEquals(UpdateValueDelegate.NEW_ELEMENT, value2.get(0));
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/history/HistoricVariableInstanceTest.testImplicitVariableUpdate.bpmn20.xml")
  public void FAILING_testImplicitVariableUpdateActivityInstanceId() {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("serviceTaskProcess",
        Variables.createVariables()
          .putValue("listVar", new ArrayList<String>())
          .putValue("delegate", new UpdateValueDelegate()));

    HistoricActivityInstance historicServiceTask = historyService
        .createHistoricActivityInstanceQuery()
        .activityId("task")
        .singleResult();

    List<String> list = (List<String>) runtimeService.getVariable(instance.getId(), "listVar");
    assertNotNull(list);
    assertEquals(1, list.size());
    assertEquals(UpdateValueDelegate.NEW_ELEMENT, list.get(0));

    // when
    HistoricVariableInstance historicVariableInstance = historyService
        .createHistoricVariableInstanceQuery()
        .variableName("listVar").singleResult();

    // then
    assertEquals(historicServiceTask.getId(), historicVariableInstance.getActivityInstanceId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/history/HistoricVariableInstanceTest.testImplicitVariableUpdate.bpmn20.xml")
  public void FAILING_testImplicitVariableUpdateAndReplacementInOneTransaction() {
    // given
    runtimeService.startProcessInstanceByKey("serviceTaskProcess",
        Variables.createVariables()
          .putValue("listVar", new ArrayList<String>())
          .putValue("delegate", new UpdateAndReplaceValueDelegate()));

    HistoricVariableInstance historicVariableInstance = historyService
        .createHistoricVariableInstanceQuery()
        .variableName("listVar").singleResult();

    List<String> historicList = (List<String>) historicVariableInstance.getValue();
    assertNotNull(historicList);
    assertEquals(0, historicList.size());

    if (isFullHistoryEnabled()) {
      List<HistoricDetail> historicDetails = historyService
          .createHistoricDetailQuery()
          .variableUpdates()
          .variableInstanceId(historicVariableInstance.getId())
          .orderPartiallyByOccurrence().asc()
          .list();

      assertEquals(3, historicDetails.size());

      HistoricVariableUpdate update1 = (HistoricVariableUpdate) historicDetails.get(0);
      HistoricVariableUpdate update2 = (HistoricVariableUpdate) historicDetails.get(1);
      HistoricVariableUpdate update3 = (HistoricVariableUpdate) historicDetails.get(2);

      List<String> value1 = (List<String>) update1.getValue();

      assertNotNull(value1);
      assertTrue(value1.isEmpty());

      List<String> value2 = (List<String>) update2.getValue();

      assertNotNull(value2);
      assertEquals(1, value2.size());
      assertEquals(UpdateValueDelegate.NEW_ELEMENT, value2.get(0));

      List<String> value3 = (List<String>) update3.getValue();

      assertNotNull(value3);
      assertTrue(value3.isEmpty());
    }
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testImplicitVariableUpdateAndScopeDestroyedInOneTransaction() {
    deployment(Bpmn.createExecutableProcess("process1")
      .startEvent("start")
      .serviceTask("task1").camundaExpression("${var.setValue(\"newValue\")}")
      .endEvent("end")
      .done());

    processEngine.getRuntimeService().startProcessInstanceByKey("process1", Variables.createVariables().putValue("var", new CustomVar("initialValue")));

    final HistoricVariableInstance historicVariableInstance = processEngine.getHistoryService().createHistoricVariableInstanceQuery().list().get(0);
    CustomVar var = (CustomVar) historicVariableInstance.getTypedValue().getValue();

    assertEquals("newValue", var.getValue());

    final List<HistoricDetail> historicDetails = processEngine.getHistoryService().createHistoricDetailQuery().orderPartiallyByOccurrence().desc().list();
    HistoricDetail historicDetail = historicDetails.get(0);
    final CustomVar typedValue = (CustomVar) ((HistoricVariableUpdate) historicDetail).getTypedValue().getValue();
    assertEquals("newValue", typedValue.getValue());
  }

  public static class CustomVar implements Serializable {
    private String value;

    public CustomVar(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }
  }

  @Deployment
  public void testNoImplicitUpdateOnHistoricValues() {
    //given
    runtimeService.startProcessInstanceByKey("serviceTaskProcess",
        Variables.createVariables()
          .putValue("listVar", new ArrayList<String>())
          .putValue("delegate", new UpdateHistoricValueDelegate()));

    // a task before the delegate ensures that the variables have actually been persisted
    // and can be fetched by querying
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    // then
    HistoricVariableInstance historicVariableInstance = historyService
        .createHistoricVariableInstanceQuery()
        .variableName("listVar").singleResult();

    List<String> historicList = (List<String>) historicVariableInstance.getValue();
    assertNotNull(historicList);
    assertEquals(0, historicList.size());

    if (isFullHistoryEnabled()) {
      assertEquals(2, historyService.createHistoricDetailQuery().count());

      List<HistoricDetail> historicDetails = historyService
          .createHistoricDetailQuery()
          .variableUpdates()
          .variableInstanceId(historicVariableInstance.getId())
          .list();

      assertEquals(1, historicDetails.size());

      HistoricVariableUpdate update1 = (HistoricVariableUpdate) historicDetails.get(0);

      List<String> value1 = (List<String>) update1.getValue();

      assertNotNull(value1);
      assertTrue(value1.isEmpty());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/history/HistoricVariableInstanceTest.testImplicitVariableUpdate.bpmn20.xml")
  public void testImplicitVariableRemoveAndUpdateInOneTransaction() {
    // given
    runtimeService.startProcessInstanceByKey("serviceTaskProcess",
        Variables.createVariables()
          .putValue("listVar", new ArrayList<String>())
          .putValue("delegate", new RemoveAndUpdateValueDelegate()));

    if (isFullHistoryEnabled()) {
      List<HistoricDetail> historicDetails = historyService
          .createHistoricDetailQuery()
          .variableUpdates()
          .orderPartiallyByOccurrence().asc()
          .list();

      Iterator<HistoricDetail> detailsIt = historicDetails.iterator();
      while(detailsIt.hasNext()) {
        if (!"listVar".equals(((HistoricVariableUpdate) detailsIt.next()).getVariableName())) {
          detailsIt.remove();
        }
      }

      // one for creation, one for deletion, none for update
      assertEquals(2, historicDetails.size());

      HistoricVariableUpdate update1 = (HistoricVariableUpdate) historicDetails.get(0);

      List<String> value1 = (List<String>) update1.getValue();

      assertNotNull(value1);
      assertTrue(value1.isEmpty());

      HistoricVariableUpdate update2 = (HistoricVariableUpdate) historicDetails.get(1);
      assertNull(update2.getValue());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/history/HistoricVariableInstanceTest.testNoImplicitUpdateOnHistoricValues.bpmn20.xml")
  public void testNoImplicitUpdateOnHistoricDetailValues() {
    if (!isFullHistoryEnabled()) {
      return;
    }

    // given
    runtimeService.startProcessInstanceByKey("serviceTaskProcess",
        Variables.createVariables()
          .putValue("listVar", new ArrayList<String>())
          .putValue("delegate", new UpdateHistoricDetailValueDelegate()));

    // a task before the delegate ensures that the variables have actually been persisted
    // and can be fetched by querying
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    // then
    HistoricVariableInstance historicVariableInstance = historyService
        .createHistoricVariableInstanceQuery()
        .variableName("listVar").singleResult();

    // One for "listvar", one for "delegate"
    assertEquals(2, historyService.createHistoricDetailQuery().count());

    List<HistoricDetail> historicDetails = historyService
        .createHistoricDetailQuery()
        .variableUpdates()
        .variableInstanceId(historicVariableInstance.getId())
        .list();

    assertEquals(1, historicDetails.size());

    HistoricVariableUpdate update1 = (HistoricVariableUpdate) historicDetails.get(0);

    List<String> value1 = (List<String>) update1.getValue();

    assertNotNull(value1);
    assertTrue(value1.isEmpty());
  }

  protected boolean isFullHistoryEnabled() {
    return processEngineConfiguration.getHistoryLevel().equals(HistoryLevel.HISTORY_LEVEL_FULL);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricVariableInstanceTest.testHistoricVariableInstanceRevision.bpmn20.xml"})
  public void testVariableUpdateOrder() {
    // given:
    // a finished process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    assertProcessEnded(processInstance.getId());

    // when

    // then
    HistoricVariableInstance variable = historyService
      .createHistoricVariableInstanceQuery()
      .singleResult();
    assertNotNull(variable);

    if (isFullHistoryEnabled()) {

      List<HistoricDetail> details = historyService
        .createHistoricDetailQuery()
        .variableInstanceId(variable.getId())
        .orderPartiallyByOccurrence()
        .asc()
        .list();

      assertEquals(3, details.size());

      HistoricVariableUpdate firstUpdate = (HistoricVariableUpdate) details.get(0);
      assertEquals(1, firstUpdate.getValue());

      HistoricVariableUpdate secondUpdate = (HistoricVariableUpdate) details.get(1);
      assertEquals(2, secondUpdate.getValue());
      assertTrue(((HistoryEvent)secondUpdate).getSequenceCounter() > ((HistoryEvent)firstUpdate).getSequenceCounter());

      HistoricVariableUpdate thirdUpdate = (HistoricVariableUpdate) details.get(2);
      assertEquals(3, thirdUpdate.getValue());
      assertTrue(((HistoryEvent)thirdUpdate).getSequenceCounter() > ((HistoryEvent)secondUpdate).getSequenceCounter());
    }

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricVariableInstanceTest.testHistoricVariableInstanceRevisionAsync.bpmn20.xml"})
  public void testVariableUpdateOrderAsync() {
    // given:
    // a finished process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    // when
    executeAvailableJobs();

    // then
    assertProcessEnded(processInstance.getId());

    HistoricVariableInstance variable = historyService
      .createHistoricVariableInstanceQuery()
      .singleResult();
    assertNotNull(variable);

    if (isFullHistoryEnabled()) {

      List<HistoricDetail> details = historyService
        .createHistoricDetailQuery()
        .variableInstanceId(variable.getId())
        .orderPartiallyByOccurrence()
        .asc()
        .list();

      assertEquals(3, details.size());

      HistoricVariableUpdate firstUpdate = (HistoricVariableUpdate) details.get(0);
      assertEquals(1, firstUpdate.getValue());

      HistoricVariableUpdate secondUpdate = (HistoricVariableUpdate) details.get(1);
      assertEquals(2, secondUpdate.getValue());
      assertTrue(((HistoryEvent)secondUpdate).getSequenceCounter() > ((HistoryEvent)firstUpdate).getSequenceCounter());

      HistoricVariableUpdate thirdUpdate = (HistoricVariableUpdate) details.get(2);
      assertEquals(3, thirdUpdate.getValue());
      assertTrue(((HistoryEvent)thirdUpdate).getSequenceCounter() > ((HistoryEvent)secondUpdate).getSequenceCounter());
    }

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testTaskVariableUpdateOrder() {
    // given:
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when (1)
    taskService.setVariableLocal(taskId, "myVariable", 1);
    taskService.setVariableLocal(taskId, "myVariable", 2);
    taskService.setVariableLocal(taskId, "myVariable", 3);

    // then (1)
    HistoricVariableInstance variable = historyService
      .createHistoricVariableInstanceQuery()
      .singleResult();
    assertNotNull(variable);

    String variableInstanceId = variable.getId();

    if (isFullHistoryEnabled()) {

      List<HistoricDetail> details = historyService
        .createHistoricDetailQuery()
        .variableInstanceId(variableInstanceId)
        .orderPartiallyByOccurrence()
        .asc()
        .list();

      assertEquals(3, details.size());

      HistoricVariableUpdate firstUpdate = (HistoricVariableUpdate) details.get(0);
      assertEquals(1, firstUpdate.getValue());

      HistoricVariableUpdate secondUpdate = (HistoricVariableUpdate) details.get(1);
      assertEquals(2, secondUpdate.getValue());
      assertTrue(((HistoryEvent)secondUpdate).getSequenceCounter() > ((HistoryEvent)firstUpdate).getSequenceCounter());

      HistoricVariableUpdate thirdUpdate = (HistoricVariableUpdate) details.get(2);
      assertEquals(3, thirdUpdate.getValue());
      assertTrue(((HistoryEvent)thirdUpdate).getSequenceCounter() > ((HistoryEvent)secondUpdate).getSequenceCounter());
    }

    // when (2)
    taskService.setVariableLocal(taskId, "myVariable", "abc");

    // then (2)
    variable = historyService
      .createHistoricVariableInstanceQuery()
      .singleResult();
    assertNotNull(variable);

    if (isFullHistoryEnabled()) {

      List<HistoricDetail> details = historyService
        .createHistoricDetailQuery()
        .variableInstanceId(variableInstanceId)
        .orderPartiallyByOccurrence()
        .asc()
        .list();

      assertEquals(4, details.size());

      HistoricVariableUpdate firstUpdate = (HistoricVariableUpdate) details.get(0);
      assertEquals(1, firstUpdate.getValue());

      HistoricVariableUpdate secondUpdate = (HistoricVariableUpdate) details.get(1);
      assertEquals(2, secondUpdate.getValue());
      assertTrue(((HistoryEvent)secondUpdate).getSequenceCounter() > ((HistoryEvent)firstUpdate).getSequenceCounter());

      HistoricVariableUpdate thirdUpdate = (HistoricVariableUpdate) details.get(2);
      assertEquals(3, thirdUpdate.getValue());
      assertTrue(((HistoryEvent)thirdUpdate).getSequenceCounter() > ((HistoryEvent)secondUpdate).getSequenceCounter());

      HistoricVariableUpdate fourthUpdate = (HistoricVariableUpdate) details.get(3);
      assertEquals("abc", fourthUpdate.getValue());
      assertTrue(((HistoryEvent)fourthUpdate).getSequenceCounter() > ((HistoryEvent)thirdUpdate).getSequenceCounter());
    }

    // when (3)
    taskService.removeVariable(taskId, "myVariable");

    // then (3)
    variable = historyService
      .createHistoricVariableInstanceQuery()
      .singleResult();
    assertNull(variable);

    if (isFullHistoryEnabled()) {

      List<HistoricDetail> details = historyService
        .createHistoricDetailQuery()
        .variableInstanceId(variableInstanceId)
        .orderPartiallyByOccurrence()
        .asc()
        .list();

      assertEquals(5, details.size());

      HistoricVariableUpdate firstUpdate = (HistoricVariableUpdate) details.get(0);
      assertEquals(1, firstUpdate.getValue());

      HistoricVariableUpdate secondUpdate = (HistoricVariableUpdate) details.get(1);
      assertEquals(2, secondUpdate.getValue());
      assertTrue(((HistoryEvent)secondUpdate).getSequenceCounter() > ((HistoryEvent)firstUpdate).getSequenceCounter());

      HistoricVariableUpdate thirdUpdate = (HistoricVariableUpdate) details.get(2);
      assertEquals(3, thirdUpdate.getValue());
      assertTrue(((HistoryEvent)thirdUpdate).getSequenceCounter() > ((HistoryEvent)secondUpdate).getSequenceCounter());

      HistoricVariableUpdate fourthUpdate = (HistoricVariableUpdate) details.get(3);
      assertEquals("abc", fourthUpdate.getValue());
      assertTrue(((HistoryEvent)fourthUpdate).getSequenceCounter() > ((HistoryEvent)thirdUpdate).getSequenceCounter());

      HistoricVariableUpdate fifthUpdate = (HistoricVariableUpdate) details.get(4);
      assertNull(fifthUpdate.getValue());
      assertTrue(((HistoryEvent)fifthUpdate).getSequenceCounter() > ((HistoryEvent)fourthUpdate).getSequenceCounter());
    }

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testCaseVariableUpdateOrder() {
    // given:
    String caseInstanceId = caseService.createCaseInstanceByKey("oneTaskCase").getId();

    // when (1)
    caseService.setVariable(caseInstanceId, "myVariable", 1);
    caseService.setVariable(caseInstanceId, "myVariable", 2);
    caseService.setVariable(caseInstanceId, "myVariable", 3);

    // then (1)
    HistoricVariableInstance variable = historyService
      .createHistoricVariableInstanceQuery()
      .singleResult();
    assertNotNull(variable);

    String variableInstanceId = variable.getId();

    if (isFullHistoryEnabled()) {

      List<HistoricDetail> details = historyService
        .createHistoricDetailQuery()
        .variableInstanceId(variableInstanceId)
        .orderPartiallyByOccurrence()
        .asc()
        .list();

      assertEquals(3, details.size());

      HistoricVariableUpdate firstUpdate = (HistoricVariableUpdate) details.get(0);
      assertEquals(1, firstUpdate.getValue());

      HistoricVariableUpdate secondUpdate = (HistoricVariableUpdate) details.get(1);
      assertEquals(2, secondUpdate.getValue());
      assertTrue(((HistoryEvent)secondUpdate).getSequenceCounter() > ((HistoryEvent)firstUpdate).getSequenceCounter());

      HistoricVariableUpdate thirdUpdate = (HistoricVariableUpdate) details.get(2);
      assertEquals(3, thirdUpdate.getValue());
      assertTrue(((HistoryEvent)thirdUpdate).getSequenceCounter() > ((HistoryEvent)secondUpdate).getSequenceCounter());
    }

    // when (2)
    caseService.setVariable(caseInstanceId, "myVariable", "abc");

    // then (2)
    variable = historyService
      .createHistoricVariableInstanceQuery()
      .singleResult();
    assertNotNull(variable);

    if (isFullHistoryEnabled()) {

      List<HistoricDetail> details = historyService
        .createHistoricDetailQuery()
        .variableInstanceId(variableInstanceId)
        .orderPartiallyByOccurrence()
        .asc()
        .list();

      assertEquals(4, details.size());

      HistoricVariableUpdate firstUpdate = (HistoricVariableUpdate) details.get(0);
      assertEquals(1, firstUpdate.getValue());

      HistoricVariableUpdate secondUpdate = (HistoricVariableUpdate) details.get(1);
      assertEquals(2, secondUpdate.getValue());
      assertTrue(((HistoryEvent)secondUpdate).getSequenceCounter() > ((HistoryEvent)firstUpdate).getSequenceCounter());

      HistoricVariableUpdate thirdUpdate = (HistoricVariableUpdate) details.get(2);
      assertEquals(3, thirdUpdate.getValue());
      assertTrue(((HistoryEvent)thirdUpdate).getSequenceCounter() > ((HistoryEvent)secondUpdate).getSequenceCounter());

      HistoricVariableUpdate fourthUpdate = (HistoricVariableUpdate) details.get(3);
      assertEquals("abc", fourthUpdate.getValue());
      assertTrue(((HistoryEvent)fourthUpdate).getSequenceCounter() > ((HistoryEvent)thirdUpdate).getSequenceCounter());
    }

    // when (3)
    caseService.removeVariable(caseInstanceId, "myVariable");

    // then (3)
    variable = historyService
      .createHistoricVariableInstanceQuery()
      .singleResult();
    assertNull(variable);

    if (isFullHistoryEnabled()) {

      List<HistoricDetail> details = historyService
        .createHistoricDetailQuery()
        .variableInstanceId(variableInstanceId)
        .orderPartiallyByOccurrence()
        .asc()
        .list();

      assertEquals(5, details.size());

      HistoricVariableUpdate firstUpdate = (HistoricVariableUpdate) details.get(0);
      assertEquals(1, firstUpdate.getValue());

      HistoricVariableUpdate secondUpdate = (HistoricVariableUpdate) details.get(1);
      assertEquals(2, secondUpdate.getValue());
      assertTrue(((HistoryEvent)secondUpdate).getSequenceCounter() > ((HistoryEvent)firstUpdate).getSequenceCounter());

      HistoricVariableUpdate thirdUpdate = (HistoricVariableUpdate) details.get(2);
      assertEquals(3, thirdUpdate.getValue());
      assertTrue(((HistoryEvent)thirdUpdate).getSequenceCounter() > ((HistoryEvent)secondUpdate).getSequenceCounter());

      HistoricVariableUpdate fourthUpdate = (HistoricVariableUpdate) details.get(3);
      assertEquals("abc", fourthUpdate.getValue());
      assertTrue(((HistoryEvent)fourthUpdate).getSequenceCounter() > ((HistoryEvent)thirdUpdate).getSequenceCounter());

      HistoricVariableUpdate fifthUpdate = (HistoricVariableUpdate) details.get(4);
      assertNull(fifthUpdate.getValue());
      assertTrue(((HistoryEvent)fifthUpdate).getSequenceCounter() > ((HistoryEvent)fourthUpdate).getSequenceCounter());
    }

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testSetSameVariableUpdateOrder() {
    // given:
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.setVariable(taskId, "myVariable", 1);
    taskService.setVariable(taskId, "myVariable", 1);
    taskService.setVariable(taskId, "myVariable", 2);

    // then
    HistoricVariableInstance variable = historyService
      .createHistoricVariableInstanceQuery()
      .singleResult();
    assertNotNull(variable);

    String variableInstanceId = variable.getId();

    if (isFullHistoryEnabled()) {

      List<HistoricDetail> details = historyService
        .createHistoricDetailQuery()
        .variableInstanceId(variableInstanceId)
        .orderPartiallyByOccurrence()
        .asc()
        .list();

      assertEquals(3, details.size());

      HistoricVariableUpdate firstUpdate = (HistoricVariableUpdate) details.get(0);
      assertEquals(1, firstUpdate.getValue());

      HistoricVariableUpdate secondUpdate = (HistoricVariableUpdate) details.get(1);
      assertEquals(1, secondUpdate.getValue());
      assertTrue(((HistoryEvent)secondUpdate).getSequenceCounter() > ((HistoryEvent)firstUpdate).getSequenceCounter());

      HistoricVariableUpdate thirdUpdate = (HistoricVariableUpdate) details.get(2);
      assertEquals(2, thirdUpdate.getValue());
      assertTrue(((HistoryEvent)thirdUpdate).getSequenceCounter() > ((HistoryEvent)secondUpdate).getSequenceCounter());
    }

  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testProcessDefinitionProperty() {
    // given
    String key = "oneTaskProcess";
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(key);

    String processInstanceId = processInstance.getId();
    String taskId = taskService.createTaskQuery().singleResult().getId();

    runtimeService.setVariable(processInstanceId, "aVariable", "aValue");
    taskService.setVariableLocal(taskId, "aLocalVariable", "anotherValue");

    // when (1)
    HistoricVariableInstance instance = historyService
        .createHistoricVariableInstanceQuery()
        .processInstanceId(processInstanceId)
        .variableName("aVariable")
        .singleResult();

    // then (1)
    assertNotNull(instance.getProcessDefinitionKey());
    assertEquals(key, instance.getProcessDefinitionKey());

    assertNotNull(instance.getProcessDefinitionId());
    assertEquals(processInstance.getProcessDefinitionId(), instance.getProcessDefinitionId());

    assertNull(instance.getCaseDefinitionKey());
    assertNull(instance.getCaseDefinitionId());

    // when (2)
    instance = historyService
        .createHistoricVariableInstanceQuery()
        .processInstanceId(processInstanceId)
        .variableName("aLocalVariable")
        .singleResult();

    // then (2)
    assertNotNull(instance.getProcessDefinitionKey());
    assertEquals(key, instance.getProcessDefinitionKey());

    assertNotNull(instance.getProcessDefinitionId());
    assertEquals(processInstance.getProcessDefinitionId(), instance.getProcessDefinitionId());

    assertNull(instance.getCaseDefinitionKey());
    assertNull(instance.getCaseDefinitionId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn")
  public void testCaseDefinitionProperty() {
    // given
    String key = "oneTaskCase";
    CaseInstance caseInstance = caseService.createCaseInstanceByKey(key);

    String caseInstanceId = caseInstance.getId();

    caseService.createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();
    String taskId = taskService.createTaskQuery().singleResult().getId();

    caseService.setVariable(caseInstanceId, "aVariable", "aValue");
    taskService.setVariableLocal(taskId, "aLocalVariable", "anotherValue");

    VariableInstance variable = runtimeService.createVariableInstanceQuery().caseInstanceIdIn(caseInstanceId).variableName("aVariable").singleResult();
    assertNotNull(variable);

    // when (1)
    HistoricVariableInstance instance = historyService
        .createHistoricVariableInstanceQuery()
        .caseInstanceId(caseInstanceId)
        .variableName("aVariable")
        .singleResult();

    // then (1)
    assertCaseVariable(key, caseInstance, instance);

    // when (2)
    instance = historyService
        .createHistoricVariableInstanceQuery()
        .caseInstanceId(caseInstanceId)
        .variableName("aLocalVariable")
        .singleResult();

    // then (2)
    assertCaseVariable(key, caseInstance, instance);

    // when (3)
    instance = historyService
        .createHistoricVariableInstanceQuery()
        .caseInstanceId(caseInstanceId)
        .variableId(variable.getId())
        .singleResult();

    // then (4)
    assertNotNull(instance);
    assertCaseVariable(key, caseInstance, instance);
  }

  protected void assertCaseVariable(String key, CaseInstance caseInstance, HistoricVariableInstance instance) {
    assertNotNull(instance.getCaseDefinitionKey());
    assertEquals(key, instance.getCaseDefinitionKey());

    assertNotNull(instance.getCaseDefinitionId());
    assertEquals(caseInstance.getCaseDefinitionId(), instance.getCaseDefinitionId());

    assertNull(instance.getProcessDefinitionKey());
    assertNull(instance.getProcessDefinitionId());
  }

  public void testStandaloneTaskDefinitionProperties() {
    // given
    String taskId = "myTask";
    Task task = taskService.newTask(taskId);
    taskService.saveTask(task);

    taskService.setVariable(taskId, "aVariable", "anotherValue");

    // when (1)
    HistoricVariableInstance instance = historyService
        .createHistoricVariableInstanceQuery()
        .taskIdIn(taskId)
        .variableName("aVariable")
        .singleResult();

    // then (1)
    assertNull(instance.getProcessDefinitionKey());
    assertNull(instance.getProcessDefinitionId());
    assertNull(instance.getCaseDefinitionKey());
    assertNull(instance.getCaseDefinitionId());

    taskService.deleteTask(taskId, true);
  }

  public void testTaskIdProperty() {
    // given
    String taskId = "myTask";
    Task task = taskService.newTask(taskId);
    taskService.saveTask(task);

    taskService.setVariable(taskId, "aVariable", "anotherValue");

    // when
    HistoricVariableInstance instance = historyService
        .createHistoricVariableInstanceQuery()
        .taskIdIn(taskId)
        .variableName("aVariable")
        .singleResult();

    // then
    assertEquals(taskId, instance.getTaskId());

    taskService.deleteTask(taskId, true);
  }

  @Deployment
  public void testJoinParallelGatewayLocalVariableOnLastJoiningExecution() {
    // when
    runtimeService.startProcessInstanceByKey("process");

    // then
    assertEquals(0, runtimeService.createVariableInstanceQuery().count());

    HistoricVariableInstance historicVariable = historyService.createHistoricVariableInstanceQuery().singleResult();
    assertNotNull(historicVariable);
    assertEquals("testVar", historicVariable.getName());
  }

  @Deployment
  public void testNestedJoinParallelGatewayLocalVariableOnLastJoiningExecution() {
    // when
    runtimeService.startProcessInstanceByKey("process");

    // then
    assertEquals(0, runtimeService.createVariableInstanceQuery().count());

    HistoricVariableInstance historicVariable = historyService.createHistoricVariableInstanceQuery().singleResult();
    assertNotNull(historicVariable);
    assertEquals("testVar", historicVariable.getName());
  }

  @Deployment
  public void testJoinInclusiveGatewayLocalVariableOnLastJoiningExecution() {
    // when
    runtimeService.startProcessInstanceByKey("process");

    // then
    assertEquals(0, runtimeService.createVariableInstanceQuery().count());

    HistoricVariableInstance historicVariable = historyService.createHistoricVariableInstanceQuery().singleResult();
    assertNotNull(historicVariable);
    assertEquals("testVar", historicVariable.getName());
  }

  @Deployment
  public void testNestedJoinInclusiveGatewayLocalVariableOnLastJoiningExecution() {
    // when
    runtimeService.startProcessInstanceByKey("process");

    // then
    assertEquals(0, runtimeService.createVariableInstanceQuery().count());

    HistoricVariableInstance historicVariable = historyService.createHistoricVariableInstanceQuery().singleResult();
    assertNotNull(historicVariable);
    assertEquals("testVar", historicVariable.getName());
  }

  @Deployment
  public void testForkParallelGatewayTreeCompaction() {
    // given
    runtimeService.startProcessInstanceByKey("process");

    Task task1 = taskService
        .createTaskQuery()
        .taskDefinitionKey("task1")
        .singleResult();

    Execution task2Execution = runtimeService
        .createExecutionQuery()
        .activityId("task2")
        .singleResult();

    // when
    runtimeService.setVariableLocal(task2Execution.getId(), "foo", "bar");
    taskService.complete(task1.getId());

    // then
    assertEquals(1, runtimeService.createVariableInstanceQuery().count());

    HistoricVariableInstance historicVariable = historyService.createHistoricVariableInstanceQuery().singleResult();
    assertNotNull(historicVariable);
    assertEquals("foo", historicVariable.getName());
  }

  @Deployment
  public void testNestedForkParallelGatewayTreeCompaction() {
    // given
    runtimeService.startProcessInstanceByKey("process");

    Task task1 = taskService
        .createTaskQuery()
        .taskDefinitionKey("task1")
        .singleResult();

    Execution task2Execution = runtimeService
        .createExecutionQuery()
        .activityId("task2")
        .singleResult();

    // when
    runtimeService.setVariableLocal(task2Execution.getId(), "foo", "bar");
    taskService.complete(task1.getId());

    // then
    assertEquals(1, runtimeService.createVariableInstanceQuery().count());

    HistoricVariableInstance historicVariable = historyService.createHistoricVariableInstanceQuery().singleResult();
    assertNotNull(historicVariable);
    assertEquals("foo", historicVariable.getName());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn")
  public void testQueryByCaseActivityId() {
    // given
    caseService.createCaseInstanceByKey("oneTaskCase", Variables.putValue("foo", "bar"));

    CaseExecution caseExecution = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult();
    caseService.setVariableLocal(caseExecution.getId(), "bar", "foo");

    // when
    HistoricVariableInstanceQuery query = historyService
        .createHistoricVariableInstanceQuery()
        .caseActivityIdIn("PI_HumanTask_1");

    // then
    assertEquals(1, query.count());
    assertEquals("bar", query.singleResult().getName());
    assertEquals("foo", query.singleResult().getValue());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/cmmn/twoTaskCase.cmmn")
  public void testQueryByCaseActivityIds() {
    // given
    caseService.createCaseInstanceByKey("twoTaskCase");

    CaseExecution caseExecution1 = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult();
    caseService.setVariableLocal(caseExecution1.getId(), "foo", "bar");

    CaseExecution caseExecution2 = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_2")
        .singleResult();
    caseService.setVariableLocal(caseExecution2.getId(), "bar", "foo");

    // when
    HistoricVariableInstanceQuery query = historyService
        .createHistoricVariableInstanceQuery()
        .caseActivityIdIn("PI_HumanTask_1", "PI_HumanTask_2");

    // then
    assertEquals(2, query.count());
  }

  public void testQueryByInvalidCaseActivityIds() {
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    query.caseActivityIdIn("invalid");
    assertEquals(0, query.count());

    try {
      query.caseActivityIdIn(null);
      fail("A ProcessEngineExcpetion was expected.");
    } catch (NullValueException e) {}

    try {
      query.caseActivityIdIn((String)null);
      fail("A ProcessEngineExcpetion was expected.");
    } catch (NullValueException e) {}

    try {
      String[] values = { "a", null, "b" };
      query.caseActivityIdIn(values);
      fail("A ProcessEngineExcpetion was expected.");
    } catch (NullValueException e) {}
  }

  public void testSetVariableInSubProcessStartEventWithEndListener () throws Exception {
    //given
    BpmnModelInstance topProcess = Bpmn.createExecutableProcess("topProcess")
        .startEvent()
        .callActivity()
        .calledElement("subProcess")
        .camundaIn("executionListenerCounter","executionListenerCounter")
        .endEvent()
        .done();

    BpmnModelInstance subProcess = Bpmn.createExecutableProcess("subProcess")
        .startEvent()
        .camundaAsyncBefore()
        .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_END, "org.camunda.bpm.engine.test.history.SubProcessActivityStartListener")
        .endEvent()
        .done();
    org.camunda.bpm.engine.repository.Deployment deployment = repositoryService.createDeployment()
        .addModelInstance("process.bpmn", topProcess)
        .addModelInstance("subProcess.bpmn", subProcess)
        .deploy();

    //when
    runtimeService.startProcessInstanceByKey("topProcess", Variables.createVariables().putValue("executionListenerCounter",1));
    managementService.executeJob(managementService.createJobQuery().active().singleResult().getId());

    //then
    assertThat(historyService.createHistoricVariableInstanceQuery().count(), is (3L));
    repositoryService.deleteDeployment(deployment.getId(),true);
  }

  public void testSetVariableInEndListenerOfAsyncStartEvent () throws Exception {
    //given
    BpmnModelInstance subProcess = Bpmn.createExecutableProcess("process")
      .startEvent()
      .camundaAsyncBefore()
      .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_END, SubProcessActivityStartListener.class.getName())
      .endEvent()
      .done();

    org.camunda.bpm.engine.repository.Deployment deployment = repositoryService.createDeployment()
      .addModelInstance("process.bpmn", subProcess)
      .deploy();

    //when
    runtimeService.startProcessInstanceByKey("process", Variables.createVariables().putValue("executionListenerCounter",1));
    managementService.executeJob(managementService.createJobQuery().active().singleResult().getId());

    //then
    assertThat(historyService.createHistoricVariableInstanceQuery().count(), is (2L));
    repositoryService.deleteDeployment(deployment.getId(),true);
  }

  public void testSetVariableInStartListenerOfAsyncStartEvent () throws Exception {
    //given
    BpmnModelInstance subProcess = Bpmn.createExecutableProcess("process")
      .startEvent()
      .camundaAsyncBefore()
      .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_START, SubProcessActivityStartListener.class.getName())
      .endEvent()
      .done();

    org.camunda.bpm.engine.repository.Deployment deployment = repositoryService.createDeployment()
      .addModelInstance("process.bpmn", subProcess)
      .deploy();

    //when
    runtimeService.startProcessInstanceByKey("process", Variables.createVariables().putValue("executionListenerCounter",1));
    managementService.executeJob(managementService.createJobQuery().active().singleResult().getId());

    //then
    assertThat(historyService.createHistoricVariableInstanceQuery().count(), is (2L));
    repositoryService.deleteDeployment(deployment.getId(),true);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/async/AsyncStartEventTest.testAsyncStartEvent.bpmn20.xml")
  public void testAsyncStartEventHistory() {
    if(processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      runtimeService.startProcessInstanceByKey("asyncStartEvent");

      HistoricProcessInstance historicInstance = historyService.createHistoricProcessInstanceQuery().singleResult();
      Assert.assertNotNull(historicInstance);
      Assert.assertNotNull(historicInstance.getStartTime());

      HistoricActivityInstance historicStartEvent = historyService.createHistoricActivityInstanceQuery().singleResult();
      Assert.assertNull(historicStartEvent);
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/async/AsyncStartEventTest.testAsyncStartEvent.bpmn20.xml")
  public void testAsyncStartEventVariableHistory() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("foo", "bar");
    String processInstanceId = runtimeService.startProcessInstanceByKey("asyncStartEvent", variables).getId();

    VariableInstance variableFoo = runtimeService.createVariableInstanceQuery().singleResult();
    assertNotNull(variableFoo);
    assertEquals("foo", variableFoo.getName());
    assertEquals("bar", variableFoo.getValue());

    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    executeAvailableJobs();

    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    taskService.complete(task.getId());

    // assert process instance is ended
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());

    if(processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {
      HistoricVariableInstance variable = historyService.createHistoricVariableInstanceQuery().singleResult();
      assertNotNull(variable);
      assertEquals("foo", variable.getName());
      assertEquals("bar", variable.getValue());
      assertEquals(processInstanceId, variable.getActivityInstanceId());

      if(processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {

        String startEventId = historyService
          .createHistoricActivityInstanceQuery()
          .activityId("startEvent")
          .singleResult()
          .getId();

        HistoricDetail historicDetail = historyService
          .createHistoricDetailQuery()
          .singleResult();

        assertEquals(startEventId, historicDetail.getActivityInstanceId());
      }
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/async/AsyncStartEventTest.testMultipleAsyncStartEvents.bpmn20.xml"})
  public void testMultipleAsyncStartEventsVariableHistory() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("foo", "bar");
    runtimeService.correlateMessage("newInvoiceMessage", new HashMap<String, Object>(), variables);

    VariableInstance variableFoo = runtimeService.createVariableInstanceQuery().singleResult();
    assertNotNull(variableFoo);
    assertEquals("foo", variableFoo.getName());
    assertEquals("bar", variableFoo.getValue());

    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    executeAvailableJobs();

    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    taskService.complete(task.getId());

    // assert process instance is ended
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());

    if(processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {

      String processInstanceId = historyService
        .createHistoricProcessInstanceQuery()
        .singleResult()
        .getId();

      HistoricVariableInstance variable = historyService.createHistoricVariableInstanceQuery().singleResult();
      assertNotNull(variable);
      assertEquals("foo", variable.getName());
      assertEquals("bar", variable.getValue());
      assertEquals(processInstanceId, variable.getActivityInstanceId());

      if(processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {

        String theStartActivityInstanceId = historyService
          .createHistoricActivityInstanceQuery()
          .activityId("messageStartEvent")
          .singleResult()
          .getId();

        HistoricDetail historicDetail = historyService
          .createHistoricDetailQuery()
          .singleResult();

        assertEquals(theStartActivityInstanceId, historicDetail.getActivityInstanceId());

      }
    }
  }

  public void testAsyncStartEventWithAddedVariable() {
    // given a process definition with asynchronous start event
    deployment(Bpmn.createExecutableProcess("testProcess")
      .startEvent()
      .camundaAsyncBefore()
      .endEvent()
      .done());

    // when create an instance with a variable
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess",
      Variables.putValue("var1", "foo"));

    // and add a variable before the instance is created
    runtimeService.setVariable(processInstance.getId(), "var2", "bar");

    executeAvailableJobs();

    assertProcessEnded(processInstance.getId());

    if (processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {

      // then the history contains one entry for each variable
      HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();
      assertEquals(query.count(), 2);

      HistoricVariableInstance firstVariable = query.variableName("var1").singleResult();
      assertNotNull(firstVariable);
      assertEquals(firstVariable.getValue(), "foo");
      assertNotNull(firstVariable.getActivityInstanceId());

      HistoricVariableInstance secondVariable = query.variableName("var2").singleResult();
      assertNotNull(secondVariable);
      assertEquals(secondVariable.getValue(), "bar");
      assertNotNull(secondVariable.getActivityInstanceId());
    }
  }


  public void testAsyncStartEventWithChangedVariable() {
    // given a process definition with asynchronous start event
    deployment(Bpmn.createExecutableProcess("testProcess")
      .startEvent()
      .camundaAsyncBefore()
      .endEvent()
      .done());

    // when create an instance with a variable
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess",
      Variables.putValue("var", "foo"));

    // and update this variable before the instance is created
    runtimeService.setVariable(processInstance.getId(), "var", "bar");

    executeAvailableJobs();

    assertProcessEnded(processInstance.getId());

    if (processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {

      // then the history contains only one entry for the latest update (value = "bar")
      // - the entry for the initial value (value = "foo") is lost because of current limitations
      HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();
      assertEquals(query.count(), 1);

      HistoricVariableInstance variable = query.singleResult();
      assertEquals(variable.getValue(), "bar");
      assertNotNull(variable.getActivityInstanceId());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/async/AsyncStartEventTest.testAsyncStartEvent.bpmn20.xml")
  public void testSubmitForm() {

    String processDefinitionId = repositoryService
      .createProcessDefinitionQuery()
      .processDefinitionKey("asyncStartEvent")
      .singleResult()
      .getId();

    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("foo", "bar");

    formService.submitStartForm(processDefinitionId, properties);

    VariableInstance variableFoo = runtimeService.createVariableInstanceQuery().singleResult();
    assertNotNull(variableFoo);
    assertEquals("foo", variableFoo.getName());
    assertEquals("bar", variableFoo.getValue());

    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    executeAvailableJobs();

    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    taskService.complete(task.getId());

    // assert process instance is ended
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());

    if(processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {

      String processInstanceId = historyService
        .createHistoricProcessInstanceQuery()
        .singleResult()
        .getId();

      HistoricVariableInstance variable = historyService.createHistoricVariableInstanceQuery().singleResult();
      assertNotNull(variable);
      assertEquals("foo", variable.getName());
      assertEquals("bar", variable.getValue());
      assertEquals(processInstanceId, variable.getActivityInstanceId());

      if(processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {

        String theStartActivityInstanceId = historyService
          .createHistoricActivityInstanceQuery()
          .activityId("startEvent")
          .singleResult()
          .getId();

        HistoricFormField historicFormUpdate = (HistoricFormField) historyService
          .createHistoricDetailQuery()
          .formFields()
          .singleResult();

        assertNotNull(historicFormUpdate);
        assertEquals("bar", historicFormUpdate.getFieldValue());

        HistoricVariableUpdate historicVariableUpdate = (HistoricVariableUpdate) historyService
          .createHistoricDetailQuery()
          .variableUpdates()
          .singleResult();

        assertNotNull(historicVariableUpdate);
        assertEquals(theStartActivityInstanceId, historicVariableUpdate.getActivityInstanceId());
        assertEquals("bar", historicVariableUpdate.getValue());

      }
    }
  }

  /**
   * CAM-2828
   */
  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/async/AsyncStartEventTest.testAsyncStartEvent.bpmn20.xml")
  public void FAILING_testSubmitFormHistoricUpdates() {

    String processDefinitionId = repositoryService
      .createProcessDefinitionQuery()
      .processDefinitionKey("asyncStartEvent")
      .singleResult()
      .getId();

    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("foo", "bar");

    formService.submitStartForm(processDefinitionId, properties);
    executeAvailableJobs();

    if(processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {

      String theStartActivityInstanceId = historyService
        .createHistoricActivityInstanceQuery()
        .activityId("startEvent")
        .singleResult()
        .getId();

      HistoricDetail historicFormUpdate = historyService
        .createHistoricDetailQuery()
        .formFields()
        .singleResult();

      assertNotNull(historicFormUpdate);
      assertEquals(theStartActivityInstanceId, historicFormUpdate.getActivityInstanceId());

    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/twoTasksProcess.bpmn20.xml"})
  public void testSetDifferentStates() {
    //given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoTasksProcess", Variables.createVariables().putValue("initial", "foo"));
    Task task = taskService.createTaskQuery().singleResult();
    taskService.setVariables(task.getId(), Variables.createVariables().putValue("bar", "abc"));
    taskService.complete(task.getId());

    //when
    runtimeService.removeVariable(processInstance.getId(), "bar");

    //then
    List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery().includeDeleted().list();
    Assert.assertEquals(2, variables.size());

    int createdCounter = 0;
    int deletedCounter = 0;

    for (HistoricVariableInstance variable : variables) {
      if (variable.getName().equals("initial")) {
        Assert.assertEquals(HistoricVariableInstance.STATE_CREATED, variable.getState());
        createdCounter += 1;
      } else if (variable.getName().equals("bar")) {
        Assert.assertEquals(HistoricVariableInstance.STATE_DELETED, variable.getState());
        deletedCounter += 1;
      }
    }

    Assert.assertEquals(1, createdCounter);
    Assert.assertEquals(1, deletedCounter);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/twoTasksProcess.bpmn20.xml"})
  public void testQueryNotIncludeDeleted() {
    //given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoTasksProcess", Variables.createVariables().putValue("initial", "foo"));
    Task task = taskService.createTaskQuery().singleResult();
    taskService.setVariables(task.getId(), Variables.createVariables().putValue("bar", "abc"));
    taskService.complete(task.getId());

    //when
    runtimeService.removeVariable(processInstance.getId(), "bar");

    //then
    HistoricVariableInstance variable = historyService.createHistoricVariableInstanceQuery().singleResult();
    assertEquals(HistoricVariableInstance.STATE_CREATED, variable.getState());
    assertEquals("initial", variable.getName());
    assertEquals("foo", variable.getValue());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/twoTasksProcess.bpmn20.xml"})
  public void testQueryByProcessDefinitionId() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoTasksProcess",
        Variables.createVariables().putValue("initial", "foo"));

    // when
    HistoricVariableInstance variable = historyService.createHistoricVariableInstanceQuery()
        .processDefinitionId(processInstance.getProcessDefinitionId()).singleResult();

    // then
    assertNotNull(variable);
    assertEquals("initial", variable.getName());
    assertEquals("foo", variable.getValue());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/twoTasksProcess.bpmn20.xml"})
  public void testQueryByProcessDefinitionKey() {
    // given
    runtimeService.startProcessInstanceByKey("twoTasksProcess", Variables.createVariables().putValue("initial", "foo"));

    // when
    HistoricVariableInstance variable = historyService.createHistoricVariableInstanceQuery()
        .processDefinitionKey("twoTasksProcess").singleResult();

    // then
    assertNotNull(variable);
    assertEquals("initial", variable.getName());
    assertEquals("foo", variable.getValue());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/twoTasksProcess.bpmn20.xml"})
  public void testQueryByProcessDefinitionKeyTwoInstances() {
    // given
    runtimeService.startProcessInstanceByKey("twoTasksProcess", Variables.createVariables().putValue("initial", "foo").putValue("vegie", "cucumber"));
    runtimeService.startProcessInstanceByKey("twoTasksProcess", Variables.createVariables().putValue("initial", "bar").putValue("fruit", "marakuia"));

    // when
    List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery()
    .processDefinitionKey("twoTasksProcess").list();

    // then
    assertNotNull(variables);
    assertEquals(4, variables.size());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml", "org/camunda/bpm/engine/test/api/twoTasksProcess.bpmn20.xml"})
  public void testQueryByProcessDefinitionKeyTwoDefinitions() {
    // given
    runtimeService.startProcessInstanceByKey("oneTaskProcess", Variables.createVariables().putValue("initial", "bar"));
    runtimeService.startProcessInstanceByKey("twoTasksProcess", Variables.createVariables().putValue("initial", "foo"));

    // when
    HistoricVariableInstance variable = historyService.createHistoricVariableInstanceQuery()
        .processDefinitionKey("twoTasksProcess").singleResult();

    // then
    assertNotNull(variable);
    assertEquals("initial", variable.getName());
    assertEquals("foo", variable.getValue());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryByProcessInstanceIdAndVariableId() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", Variables.createVariables().putValue("initial", "bar"));

    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("initial").singleResult();
    assertNotNull(variable);

    // when
    HistoricVariableInstance historyVariable = historyService.createHistoricVariableInstanceQuery()
        .processInstanceId(processInstance.getId())
        .variableId(variable.getId())
        .singleResult();

    // then
    assertNotNull(historyVariable);
    assertEquals("initial", historyVariable.getName());
    assertEquals("bar", historyVariable.getValue());
  }
}
