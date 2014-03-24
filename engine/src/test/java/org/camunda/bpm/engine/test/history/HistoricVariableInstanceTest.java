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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.history.HistoricVariableUpdate;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.camunda.bpm.engine.impl.test.AbstractProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.CollectionUtil;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.api.runtime.util.FailingSerializable;


/**
 * @author Christian Lipphardt (camunda)
 */
public class HistoricVariableInstanceTest extends AbstractProcessEngineTestCase {

  @Override
  protected void initializeProcessEngine() {
    processEngineConfiguration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration.createStandaloneProcessEngineConfiguration()
    .setProcessEngineName("HistoricVariableInstanceTest-engine")
    .setJdbcDriver("org.h2.Driver")
    .setJdbcUrl("jdbc:h2:mem:HistoricVariableInstanceTest;DB_CLOSE_DELAY=1000")
    .setJdbcUsername("sa")
    .setJdbcPassword("")
    .setDatabaseSchemaUpdate(ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_TRUE)
    .setJobExecutorActivate(false)
    .setHistory(ProcessEngineConfiguration.HISTORY_FULL);

    processEngine = processEngineConfiguration.buildProcessEngine();
  }

  @Deployment(resources={
    "org/camunda/bpm/engine/test/examples/bpmn/callactivity/orderProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/examples/bpmn/callactivity/checkCreditProcess.bpmn20.xml"
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
    assertEquals(3, historyService.createHistoricDetailQuery().count());
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
    assertEquals(2, historyService.createHistoricDetailQuery().count());
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
    assertEquals(5, historyService.createHistoricDetailQuery().count());
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
    assertEquals(2, historyService.createHistoricDetailQuery().count());
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

    HistoricVariableInstanceEntity historicVariable1 = (HistoricVariableInstanceEntity) variables.get(1);
    assertEquals("myVar1", historicVariable1.getName());
    assertEquals("test789", historicVariable1.getTextValue());
    assertEquals("string", historicVariable1.getVariableTypeName());

    assertEquals(18, historyService.createHistoricActivityInstanceQuery().count());
    assertEquals(7, historyService.createHistoricDetailQuery().count());
  }

  @Deployment(resources={
          "org/camunda/bpm/engine/test/history/HistoricVariableInstanceTest.testCallSimpleSubProcess.bpmn20.xml",
          "org/camunda/bpm/engine/test/history/simpleSubProcess.bpmn20.xml"
  })
  public void testHistoricVariableInstanceQuery() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callSimpleSubProcess");
    assertProcessEnded(processInstance.getId());

    assertEquals(4, historyService.createHistoricVariableInstanceQuery().count());
    assertEquals(4, historyService.createHistoricVariableInstanceQuery().list().size());
    assertEquals(4, historyService.createHistoricVariableInstanceQuery().orderByProcessInstanceId().asc().count());
    assertEquals(4, historyService.createHistoricVariableInstanceQuery().orderByProcessInstanceId().asc().list().size());
    assertEquals(4, historyService.createHistoricVariableInstanceQuery().orderByVariableName().asc().count());
    assertEquals(4, historyService.createHistoricVariableInstanceQuery().orderByVariableName().asc().list().size());

    assertEquals(2, historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).count());
    assertEquals(2, historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).list().size());
    assertEquals(2, historyService.createHistoricVariableInstanceQuery().variableName("myVar").count());
    assertEquals(2, historyService.createHistoricVariableInstanceQuery().variableName("myVar").list().size());
    assertEquals(2, historyService.createHistoricVariableInstanceQuery().variableNameLike("myVar1").count());
    assertEquals(2, historyService.createHistoricVariableInstanceQuery().variableNameLike("myVar1").list().size());

    List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery().list();
    assertEquals(4, variables.size());

    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableValueEquals("myVar", "test123").count());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableValueEquals("myVar", "test123").list().size());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableValueEquals("myVar1", "test456").count());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableValueEquals("myVar1", "test456").list().size());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableValueEquals("myVar", "test666").count());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableValueEquals("myVar", "test666").list().size());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableValueEquals("myVar1", "test666").count());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableValueEquals("myVar1", "test666").list().size());

    assertEquals(8, historyService.createHistoricActivityInstanceQuery().count());
    assertEquals(5, historyService.createHistoricDetailQuery().count());

    // non-existing id:
    assertEquals(0, historyService.createHistoricVariableInstanceQuery().variableId("non-existing").count());

    // existing-id
    List<HistoricVariableInstance> variable = historyService.createHistoricVariableInstanceQuery().listPage(0, 1);
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableId(variable.get(0).getId()).count());

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
    updatesMap.put((String)update.getValue(), update);
    update = (HistoricVariableUpdate) updates.get(1);
    updatesMap.put((String)update.getValue(), update);

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

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testHistoricVariableInstanceQueryByExecutionIds() {
    // given
    Map<String, Object> variables1 = new HashMap<String, Object>();
    variables1.put("stringVar", "test");
    variables1.put("myVar", "test123");
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables1);

    assertEquals(2, historyService.createHistoricVariableInstanceQuery().executionIdIn(processInstance1.getId()).list().size());
    assertEquals(2, historyService.createHistoricVariableInstanceQuery().executionIdIn(processInstance1.getId()).count());

    Map<String, Object> variables2 = new HashMap<String, Object>();
    variables2.put("myVar", "test123");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables2);

    assertEquals(3, historyService.createHistoricVariableInstanceQuery().executionIdIn(processInstance1.getId(), processInstance2.getId()).list().size());
    assertEquals(3, historyService.createHistoricVariableInstanceQuery().executionIdIn(processInstance1.getId(), processInstance2.getId()).count());
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

}
