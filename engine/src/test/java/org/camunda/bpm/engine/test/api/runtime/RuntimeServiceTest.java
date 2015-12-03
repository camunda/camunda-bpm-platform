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

package org.camunda.bpm.engine.test.api.runtime;

import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.describeActivityInstanceTree;
import static org.camunda.bpm.engine.variable.Variables.createVariables;
import static org.camunda.bpm.engine.variable.Variables.objectValue;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.impl.RuntimeServiceImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricDetailVariableInstanceUpdateEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.CollectionUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.TransitionInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.runtime.VariableInstanceQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.bpmn.executionlistener.RecorderExecutionListener;
import org.camunda.bpm.engine.test.bpmn.executionlistener.RecorderExecutionListener.RecordedEvent;
import org.camunda.bpm.engine.test.bpmn.tasklistener.util.RecorderTaskListener;
import org.camunda.bpm.engine.test.api.runtime.util.SimpleSerializableBean;
import org.camunda.bpm.engine.test.history.SerializableVariable;
import org.camunda.bpm.engine.test.util.TestExecutionListener;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.ValueType;

/**
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class RuntimeServiceTest extends PluggableProcessEngineTestCase {

  public void testStartProcessInstanceByKeyNullKey() {
    try {
      runtimeService.startProcessInstanceByKey(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException e) {
      // Expected exception
    }
  }

  public void testStartProcessInstanceByKeyUnexistingKey() {
    try {
      runtimeService.startProcessInstanceByKey("unexistingkey");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("no processes deployed with key", ae.getMessage());
    }
  }

  public void testStartProcessInstanceByIdNullId() {
    try {
      runtimeService.startProcessInstanceById(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException e) {
      // Expected exception
    }
  }

  public void testStartProcessInstanceByIdUnexistingId() {
    try {
      runtimeService.startProcessInstanceById("unexistingId");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("no deployed process definition found with id", ae.getMessage());
    }
  }

  @Deployment(resources={
    "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testStartProcessInstanceByIdNullVariables() {
    runtimeService.startProcessInstanceByKey("oneTaskProcess", (Map<String, Object>) null);
    assertEquals(1, runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").count());
  }

  @Deployment(resources={
    "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void startProcessInstanceWithBusinessKey() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // by key
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", "123");
    assertNotNull(processInstance);
    assertEquals("123", processInstance.getBusinessKey());
    assertEquals(1, runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").count());

    // by key with variables
    processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", "456", CollectionUtil.singletonMap("var", "value"));
    assertNotNull(processInstance);
    assertEquals(2, runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").count());
    assertEquals("var", runtimeService.getVariable(processInstance.getId(), "var"));

    // by id
    processInstance = runtimeService.startProcessInstanceById(processDefinition.getId(), "789");
    assertNotNull(processInstance);
    assertEquals(3, runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").count());

    // by id with variables
    processInstance = runtimeService.startProcessInstanceById(processDefinition.getId(), "101123", CollectionUtil.singletonMap("var", "value2"));
    assertNotNull(processInstance);
    assertEquals(4, runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").count());
    assertEquals("var", runtimeService.getVariable(processInstance.getId(), "var"));
  }

  @Deployment(resources={
    "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testDeleteProcessInstance() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    assertEquals(1, runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").count());

    String deleteReason = "testing instance deletion";
    runtimeService.deleteProcessInstance(processInstance.getId(), deleteReason);
    assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").count());

    // test that the delete reason of the process instance shows up as delete reason of the task in history
    // ACT-848
    if(!ProcessEngineConfiguration.HISTORY_NONE.equals(processEngineConfiguration.getHistory())) {

      HistoricTaskInstance historicTaskInstance = historyService
              .createHistoricTaskInstanceQuery()
              .processInstanceId(processInstance.getId())
              .singleResult();

      assertEquals(deleteReason, historicTaskInstance.getDeleteReason());
    }
  }

  @Deployment
  public void testDeleteProcessInstanceWithListeners() {
    RecorderExecutionListener.clear();

    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nestedParallelGatewayScopeTasks");

    // when
    runtimeService.deleteProcessInstance(processInstance.getId(), "");

    // then
    List<RecordedEvent> recordedEvents = RecorderExecutionListener.getRecordedEvents();
    assertEquals(10, recordedEvents.size());

    Set<RecordedEvent> startEvents = new HashSet<RecordedEvent>();
    Set<RecordedEvent> endEvents = new HashSet<RecordedEvent>();
    for (RecordedEvent event : recordedEvents) {
      if(event.getEventName().equals(ExecutionListener.EVENTNAME_START)){
        startEvents.add(event);
      } else if(event.getEventName().equals(ExecutionListener.EVENTNAME_END)){
        endEvents.add(event);
      }
    }

    assertThat(startEvents, hasSize(5));
    assertThat(endEvents, hasSize(5));
    for (RecordedEvent startEvent : startEvents) {
      assertThat(startEvent.getActivityId(), is(anyOf(equalTo("innerTask1"),
          equalTo("innerTask2"), equalTo("outerTask"), equalTo("subProcess"), equalTo("theStart"))));
      for (RecordedEvent endEvent : endEvents) {
        if(startEvent.getActivityId().equals(endEvent.getActivityId())){
          assertThat(startEvent.getActivityInstanceId(), is(endEvent.getActivityInstanceId()));
          assertThat(startEvent.getExecutionId(), is(endEvent.getExecutionId()));
        }
      }
    }
    for (RecordedEvent recordedEvent : endEvents) {
      assertThat(recordedEvent.getActivityId(), is(anyOf(equalTo("innerTask1"),
          equalTo("innerTask2"), equalTo("outerTask"), equalTo("subProcess"), nullValue())));
    }
  }

  @Deployment(resources={
  "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testDeleteProcessInstanceSkipCustomListenersEnsureHistoryWritten() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // if we skip the custom listeners,
    runtimeService.deleteProcessInstance(processInstance.getId(), null, true);

    // buit-in listeners are still invoked and thus history is written
    if(!ProcessEngineConfiguration.HISTORY_NONE.equals(processEngineConfiguration.getHistory())) {
      // verify that all historic activity instances are ended
      List<HistoricActivityInstance> hais = historyService.createHistoricActivityInstanceQuery().list();
      for (HistoricActivityInstance hai : hais) {
        assertNotNull(hai.getEndTime());
      }
    }
  }

  @Deployment
  public void testDeleteProcessInstanceSkipCustomListeners() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    // if we do not skip the custom listeners,
    runtimeService.deleteProcessInstance(processInstance.getId(), null, false);
    // the custom listener is invoked
    assertTrue(TestExecutionListener.collectedEvents.size() == 1);
    TestExecutionListener.reset();

    processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    // if we DO skip the custom listeners,
    runtimeService.deleteProcessInstance(processInstance.getId(), null, true);
    // the custom listener is not invoked
    assertTrue(TestExecutionListener.collectedEvents.size() == 0);
    TestExecutionListener.reset();
  }

  @Deployment
  public void testDeleteProcessInstanceSkipCustomListenersScope() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    // if we do not skip the custom listeners,
    runtimeService.deleteProcessInstance(processInstance.getId(), null, false);
    // the custom listener is invoked
    assertTrue(TestExecutionListener.collectedEvents.size() == 1);
    TestExecutionListener.reset();

    processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    // if we DO skip the custom listeners,
    runtimeService.deleteProcessInstance(processInstance.getId(), null, true);
    // the custom listener is not invoked
    assertTrue(TestExecutionListener.collectedEvents.size() == 0);
    TestExecutionListener.reset();
  }

  @Deployment
  public void testDeleteProcessInstanceSkipCustomTaskListeners() {

    // given a process instance
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // and an empty task listener invocation storage
    RecorderTaskListener.clear();

    // if we do not skip the custom listeners
    runtimeService.deleteProcessInstance(instance.getId(), null, false);

    // then the the custom listener is invoked
    assertEquals(1, RecorderTaskListener.getRecordedEvents().size());
    assertEquals(TaskListener.EVENTNAME_DELETE, RecorderTaskListener.getRecordedEvents().get(0).getEvent());

    // if we do skip the custom listeners
    instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    RecorderTaskListener.clear();

    runtimeService.deleteProcessInstance(instance.getId(), null, true);

    // then the the custom listener is not invoked
    assertTrue(RecorderTaskListener.getRecordedEvents().isEmpty());
  }

  @Deployment(resources={
    "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testDeleteProcessInstanceNullReason() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    assertEquals(1, runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").count());

    // Deleting without a reason should be possible
    runtimeService.deleteProcessInstance(processInstance.getId(), null);
    assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").count());
  }

  public void testDeleteProcessInstanceUnexistingId() {
    try {
      runtimeService.deleteProcessInstance("enexistingInstanceId", null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("No process instance found for id", ae.getMessage());
      assertTrue(ae instanceof BadUserRequestException);
    }
  }


  public void testDeleteProcessInstanceNullId() {
    try {
      runtimeService.deleteProcessInstance(null, "test null id delete");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("processInstanceId is null", ae.getMessage());
      assertTrue(ae instanceof BadUserRequestException);
    }
  }

  @Deployment
  public void testDeleteProcessInstanceWithActiveCompensation() {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("compensationProcess");

    Task innerTask = taskService.createTaskQuery().singleResult();
    taskService.complete(innerTask.getId());

    Task afterSubProcessTask = taskService.createTaskQuery().singleResult();
    assertEquals("taskAfterSubprocess", afterSubProcessTask.getTaskDefinitionKey());
    taskService.complete(afterSubProcessTask.getId());

    // when
    // there are two compensation tasks
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey("outerAfterBoundaryTask").count());
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey("innerAfterBoundaryTask").count());

    // when the process instance is deleted
    runtimeService.deleteProcessInstance(instance.getId(), "");

    // then
    assertProcessEnded(instance.getId());
  }

  @Deployment(resources={
    "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testFindActiveActivityIds() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    List<String> activities = runtimeService.getActiveActivityIds(processInstance.getId());
    assertNotNull(activities);
    assertEquals(1, activities.size());
  }

  public void testFindActiveActivityIdsUnexistingExecututionId() {
    try {
      runtimeService.getActiveActivityIds("unexistingExecutionId");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("execution unexistingExecutionId doesn't exist", ae.getMessage());
    }
  }

  public void testFindActiveActivityIdsNullExecututionId() {
    try {
      runtimeService.getActiveActivityIds(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("executionId is null", ae.getMessage());
    }
  }

  /**
   * Testcase to reproduce ACT-950 (https://jira.codehaus.org/browse/ACT-950)
   */
  @Deployment
  public void testFindActiveActivityIdProcessWithErrorEventAndSubProcess() {
    ProcessInstance processInstance = processEngine.getRuntimeService().startProcessInstanceByKey("errorEventSubprocess");

    List<String> activeActivities = runtimeService.getActiveActivityIds(processInstance.getId());
    assertEquals(3, activeActivities.size());

    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(2, tasks.size());

    Task parallelUserTask = null;
    for (Task task : tasks) {
      if (!task.getName().equals("ParallelUserTask") && !task.getName().equals("MainUserTask")) {
        fail("Expected: <ParallelUserTask> or <MainUserTask> but was <" + task.getName() + ">.");
      }
      if (task.getName().equals("ParallelUserTask")) {
        parallelUserTask = task;
      }
    }
    assertNotNull(parallelUserTask);

    taskService.complete(parallelUserTask.getId());

    Execution execution = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).activityId("subprocess1WaitBeforeError").singleResult();
    runtimeService.signal(execution.getId());

    activeActivities = runtimeService.getActiveActivityIds(processInstance.getId());
    assertEquals(2, activeActivities.size());

    tasks = taskService.createTaskQuery().list();
    assertEquals(2, tasks.size());

    Task beforeErrorUserTask = null;
    for (Task task : tasks) {
      if (!task.getName().equals("BeforeError") && !task.getName().equals("MainUserTask")) {
        fail("Expected: <BeforeError> or <MainUserTask> but was <" + task.getName() + ">.");
      }
      if (task.getName().equals("BeforeError")) {
        beforeErrorUserTask = task;
      }
    }
    assertNotNull(beforeErrorUserTask);

    taskService.complete(beforeErrorUserTask.getId());

    activeActivities = runtimeService.getActiveActivityIds(processInstance.getId());
    assertEquals(2, activeActivities.size());

    tasks = taskService.createTaskQuery().list();
    assertEquals(2, tasks.size());

    Task afterErrorUserTask = null;
    for (Task task : tasks) {
      if (!task.getName().equals("AfterError") && !task.getName().equals("MainUserTask")) {
        fail("Expected: <AfterError> or <MainUserTask> but was <" + task.getName() + ">.");
      }
      if (task.getName().equals("AfterError")) {
        afterErrorUserTask = task;
      }
    }
    assertNotNull(afterErrorUserTask);

    taskService.complete(afterErrorUserTask.getId());

    tasks = taskService.createTaskQuery().list();
    assertEquals(1, tasks.size());
    assertEquals("MainUserTask", tasks.get(0).getName());

    activeActivities = runtimeService.getActiveActivityIds(processInstance.getId());
    assertEquals(1, activeActivities.size());
    assertEquals("MainUserTask", activeActivities.get(0));

    taskService.complete(tasks.get(0).getId());

    assertProcessEnded(processInstance.getId());
  }

  public void testSignalUnexistingExecututionId() {
    try {
      runtimeService.signal("unexistingExecutionId");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("execution unexistingExecutionId doesn't exist", ae.getMessage());
      assertTrue(ae instanceof BadUserRequestException);
    }
  }

  public void testSignalNullExecutionId() {
    try {
      runtimeService.signal(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("executionId is null", ae.getMessage());
      assertTrue(ae instanceof BadUserRequestException);
    }
  }

  @Deployment
  public void testSignalWithProcessVariables() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testSignalWithProcessVariables");
    Map<String, Object> processVariables = new HashMap<String, Object>();
    processVariables.put("variable", "value");

    // signal the execution while passing in the variables
    runtimeService.signal(processInstance.getId(), processVariables);

    Map<String, Object> variables = runtimeService.getVariables(processInstance.getId());
    assertEquals(variables, processVariables);

  }

  @Deployment(resources={
    "org/camunda/bpm/engine/test/api/runtime/RuntimeServiceTest.testSignalWithProcessVariables.bpmn20.xml"})
  public void testSignalWithSignalNameAndData() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testSignalWithProcessVariables");
    Map<String, Object> processVariables = new HashMap<String, Object>();
    processVariables.put("variable", "value");

    // signal the execution while passing in the variables
    runtimeService.signal(processInstance.getId(), "dummySignalName", new String("SignalData"), processVariables);

    Map<String, Object> variables = runtimeService.getVariables(processInstance.getId());
    assertEquals(variables, processVariables);

  }

  @Deployment(resources={
    "org/camunda/bpm/engine/test/api/runtime/RuntimeServiceTest.testSignalWithProcessVariables.bpmn20.xml"})
  public void testSignalWithoutSignalNameAndData() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testSignalWithProcessVariables");
    Map<String, Object> processVariables = new HashMap<String, Object>();
    processVariables.put("variable", "value");

    // signal the execution while passing in the variables
    runtimeService.signal(processInstance.getId(), null, null, processVariables);

    Map<String, Object> variables = runtimeService.getVariables(processInstance.getId());
    assertEquals(processVariables, variables);

  }

  @Deployment
  public void testSignalInactiveExecution() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("testSignalInactiveExecution");

    // there exist two executions: the inactive parent (the process instance) and the child that actually waits in the receive task
    try {
      runtimeService.signal(instance.getId());
      fail();
    } catch(ProcessEngineException e) {
      // happy path
      assertTextPresent("cannot signal execution " + instance.getId() + ": it has no current activity", e.getMessage());
    } catch (Exception e) {
      fail("Signalling an inactive execution that has no activity should result in a ProcessEngineException");
    }

  }

  public void testGetVariablesUnexistingExecutionId() {
    try {
      runtimeService.getVariables("unexistingExecutionId");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("execution unexistingExecutionId doesn't exist", ae.getMessage());
    }
  }

  public void testGetVariablesNullExecutionId() {
    try {
      runtimeService.getVariables(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("executionId is null", ae.getMessage());
    }
  }

  public void testGetVariableUnexistingExecutionId() {
    try {
      runtimeService.getVariables("unexistingExecutionId");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("execution unexistingExecutionId doesn't exist", ae.getMessage());
    }
  }

  public void testGetVariableNullExecutionId() {
    try {
      runtimeService.getVariables(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("executionId is null", ae.getMessage());
    }
  }

  @Deployment(resources={
    "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testGetVariableUnexistingVariableName() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Object variableValue = runtimeService.getVariable(processInstance.getId(), "unexistingVariable");
    assertNull(variableValue);
  }

  public void testSetVariableUnexistingExecutionId() {
    try {
      runtimeService.setVariable("unexistingExecutionId", "variableName", "value");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("execution unexistingExecutionId doesn't exist", ae.getMessage());
    }
  }

  public void testSetVariableNullExecutionId() {
    try {
      runtimeService.setVariable(null, "variableName", "variableValue");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("executionId is null", ae.getMessage());
    }
  }

  @Deployment(resources={
    "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testSetVariableNullVariableName() {
    try {
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
      runtimeService.setVariable(processInstance.getId(), null, "variableValue");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("variableName is null", ae.getMessage());
    }
  }

  @Deployment(resources={
  "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testSetVariables() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("variable1", "value1");
    vars.put("variable2", "value2");

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.setVariables(processInstance.getId(), vars);

    assertEquals("value1", runtimeService.getVariable(processInstance.getId(), "variable1"));
    assertEquals("value2", runtimeService.getVariable(processInstance.getId(), "variable2"));
  }

  @Deployment(resources={
  "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testGetVariablesTyped() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("variable1", "value1");
    vars.put("variable2", "value2");

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    VariableMap variablesTyped = runtimeService.getVariablesTyped(processInstance.getId());
    assertEquals(vars, variablesTyped);
  }

  @Deployment(resources={
  "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testGetVariablesTypedDeserialize() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.createVariables()
          .putValue("broken", Variables.serializedObjectValue("broken")
              .serializationDataFormat(Variables.SerializationDataFormats.JAVA)
              .objectTypeName("unexisting").create()));

    // this works
    VariableMap variablesTyped = runtimeService.getVariablesTyped(processInstance.getId(), false);
    assertNotNull(variablesTyped.getValueTyped("broken"));
    variablesTyped = runtimeService.getVariablesTyped(processInstance.getId(), Arrays.asList("broken"), false);
    assertNotNull(variablesTyped.getValueTyped("broken"));

    // this does not
    try {
      runtimeService.getVariablesTyped(processInstance.getId());
    } catch(ProcessEngineException e) {
      assertTextPresent("Cannot deserialize object", e.getMessage());
    }

    // this does not
    try {
      runtimeService.getVariablesTyped(processInstance.getId(), Arrays.asList("broken"), true);
    } catch(ProcessEngineException e) {
      assertTextPresent("Cannot deserialize object", e.getMessage());
    }
  }

  @Deployment(resources={
  "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testGetVariablesLocalTyped() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("variable1", "value1");
    vars.put("variable2", "value2");

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    VariableMap variablesTyped = runtimeService.getVariablesLocalTyped(processInstance.getId());
    assertEquals(vars, variablesTyped);
  }

  @Deployment(resources={
  "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testGetVariablesLocalTypedDeserialize() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.createVariables()
          .putValue("broken", Variables.serializedObjectValue("broken")
              .serializationDataFormat(Variables.SerializationDataFormats.JAVA)
              .objectTypeName("unexisting").create()));

    // this works
    VariableMap variablesTyped = runtimeService.getVariablesLocalTyped(processInstance.getId(), false);
    assertNotNull(variablesTyped.getValueTyped("broken"));
    variablesTyped = runtimeService.getVariablesLocalTyped(processInstance.getId(), Arrays.asList("broken"), false);
    assertNotNull(variablesTyped.getValueTyped("broken"));

    // this does not
    try {
      runtimeService.getVariablesLocalTyped(processInstance.getId());
    } catch(ProcessEngineException e) {
      assertTextPresent("Cannot deserialize object", e.getMessage());
    }

    // this does not
    try {
      runtimeService.getVariablesLocalTyped(processInstance.getId(), Arrays.asList("broken"), true);
    } catch(ProcessEngineException e) {
      assertTextPresent("Cannot deserialize object", e.getMessage());
    }

  }

  @SuppressWarnings("unchecked")
  public void testSetVariablesUnexistingExecutionId() {
    try {
      runtimeService.setVariables("unexistingexecution", Collections.EMPTY_MAP);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("execution unexistingexecution doesn't exist", ae.getMessage());
    }
  }

  @SuppressWarnings("unchecked")
  public void testSetVariablesNullExecutionId() {
    try {
      runtimeService.setVariables(null, Collections.EMPTY_MAP);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("executionId is null", ae.getMessage());
    }
  }

  private void checkHistoricVariableUpdateEntity(String variableName, String processInstanceId) {
    if (processEngineConfiguration.getHistoryLevel().equals(HistoryLevel.HISTORY_LEVEL_FULL)) {
      boolean deletedVariableUpdateFound = false;

      List<HistoricDetail> resultSet = historyService.createHistoricDetailQuery().processInstanceId(processInstanceId).list();
      for (HistoricDetail currentHistoricDetail : resultSet) {
        assertTrue(currentHistoricDetail instanceof HistoricDetailVariableInstanceUpdateEntity);
        HistoricDetailVariableInstanceUpdateEntity historicVariableUpdate = (HistoricDetailVariableInstanceUpdateEntity) currentHistoricDetail;

        if (historicVariableUpdate.getName().equals(variableName)) {
          if (historicVariableUpdate.getValue() == null) {
            if (deletedVariableUpdateFound) {
              fail("Mismatch: A HistoricVariableUpdateEntity with a null value already found");
            } else {
              deletedVariableUpdateFound = true;
            }
          }
        }
      }

      assertTrue(deletedVariableUpdateFound);
    }
  }

  @Deployment(resources={
  "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testRemoveVariable() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("variable1", "value1");
    vars.put("variable2", "value2");

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.setVariables(processInstance.getId(), vars);

    runtimeService.removeVariable(processInstance.getId(), "variable1");

    assertNull(runtimeService.getVariable(processInstance.getId(), "variable1"));
    assertNull(runtimeService.getVariableLocal(processInstance.getId(), "variable1"));
    assertEquals("value2", runtimeService.getVariable(processInstance.getId(), "variable2"));

    checkHistoricVariableUpdateEntity("variable1", processInstance.getId());
  }

  @Deployment(resources={
  "org/camunda/bpm/engine/test/api/oneSubProcess.bpmn20.xml"})
  public void testRemoveVariableInParentScope() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("variable1", "value1");
    vars.put("variable2", "value2");

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startSimpleSubProcess", vars);
    Task currentTask = taskService.createTaskQuery().singleResult();

    runtimeService.removeVariable(currentTask.getExecutionId(), "variable1");

    assertNull(runtimeService.getVariable(processInstance.getId(), "variable1"));
    assertEquals("value2", runtimeService.getVariable(processInstance.getId(), "variable2"));

    checkHistoricVariableUpdateEntity("variable1", processInstance.getId());
  }


  public void testRemoveVariableNullExecutionId() {
    try {
      runtimeService.removeVariable(null, "variable");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("executionId is null", ae.getMessage());
    }
  }

  @Deployment(resources={
  "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testRemoveVariableLocal() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("variable1", "value1");
    vars.put("variable2", "value2");

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    runtimeService.removeVariableLocal(processInstance.getId(), "variable1");

    assertNull(runtimeService.getVariable(processInstance.getId(), "variable1"));
    assertNull(runtimeService.getVariableLocal(processInstance.getId(), "variable1"));
    assertEquals("value2", runtimeService.getVariable(processInstance.getId(), "variable2"));

    checkHistoricVariableUpdateEntity("variable1", processInstance.getId());
  }

  @Deployment(resources={
  "org/camunda/bpm/engine/test/api/oneSubProcess.bpmn20.xml"})
  public void testRemoveVariableLocalWithParentScope() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("variable1", "value1");
    vars.put("variable2", "value2");

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startSimpleSubProcess", vars);
    Task currentTask = taskService.createTaskQuery().singleResult();
    runtimeService.setVariableLocal(currentTask.getExecutionId(), "localVariable", "local value");

    assertEquals("local value", runtimeService.getVariableLocal(currentTask.getExecutionId(), "localVariable"));

    runtimeService.removeVariableLocal(currentTask.getExecutionId(), "localVariable");

    assertNull(runtimeService.getVariable(currentTask.getExecutionId(), "localVariable"));
    assertNull(runtimeService.getVariableLocal(currentTask.getExecutionId(), "localVariable"));

    assertEquals("value1", runtimeService.getVariable(processInstance.getId(), "variable1"));
    assertEquals("value2", runtimeService.getVariable(processInstance.getId(), "variable2"));

    assertEquals("value1", runtimeService.getVariable(currentTask.getExecutionId(), "variable1"));
    assertEquals("value2", runtimeService.getVariable(currentTask.getExecutionId(), "variable2"));

    checkHistoricVariableUpdateEntity("localVariable", processInstance.getId());
  }


  public void testRemoveLocalVariableNullExecutionId() {
    try {
      runtimeService.removeVariableLocal(null, "variable");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("executionId is null", ae.getMessage());
    }
  }

  @Deployment(resources={
  "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testRemoveVariables() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("variable1", "value1");
    vars.put("variable2", "value2");

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    runtimeService.setVariable(processInstance.getId(), "variable3", "value3");

    runtimeService.removeVariables(processInstance.getId(), vars.keySet());

    assertNull(runtimeService.getVariable(processInstance.getId(), "variable1"));
    assertNull(runtimeService.getVariableLocal(processInstance.getId(), "variable1"));
    assertNull(runtimeService.getVariable(processInstance.getId(), "variable2"));
    assertNull(runtimeService.getVariableLocal(processInstance.getId(), "variable2"));

    assertEquals("value3", runtimeService.getVariable(processInstance.getId(), "variable3"));
    assertEquals("value3", runtimeService.getVariableLocal(processInstance.getId(), "variable3"));

    checkHistoricVariableUpdateEntity("variable1", processInstance.getId());
    checkHistoricVariableUpdateEntity("variable2", processInstance.getId());
  }

  @Deployment(resources={
  "org/camunda/bpm/engine/test/api/oneSubProcess.bpmn20.xml"})
  public void testRemoveVariablesWithParentScope() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("variable1", "value1");
    vars.put("variable2", "value2");

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startSimpleSubProcess", vars);
    runtimeService.setVariable(processInstance.getId(), "variable3", "value3");

    Task currentTask = taskService.createTaskQuery().singleResult();

    runtimeService.removeVariables(currentTask.getExecutionId(), vars.keySet());

    assertNull(runtimeService.getVariable(processInstance.getId(), "variable1"));
    assertNull(runtimeService.getVariableLocal(processInstance.getId(), "variable1"));
    assertNull(runtimeService.getVariable(processInstance.getId(), "variable2"));
    assertNull(runtimeService.getVariableLocal(processInstance.getId(), "variable2"));

    assertEquals("value3", runtimeService.getVariable(processInstance.getId(), "variable3"));
    assertEquals("value3", runtimeService.getVariableLocal(processInstance.getId(), "variable3"));

    assertNull(runtimeService.getVariable(currentTask.getExecutionId(), "variable1"));
    assertNull(runtimeService.getVariable(currentTask.getExecutionId(), "variable2"));

    assertEquals("value3", runtimeService.getVariable(currentTask.getExecutionId(), "variable3"));

    checkHistoricVariableUpdateEntity("variable1", processInstance.getId());
    checkHistoricVariableUpdateEntity("variable2", processInstance.getId());
  }

  @SuppressWarnings("unchecked")
  public void testRemoveVariablesNullExecutionId() {
    try {
      runtimeService.removeVariables(null, Collections.EMPTY_LIST);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("executionId is null", ae.getMessage());
    }
  }

  @Deployment(resources={
  "org/camunda/bpm/engine/test/api/oneSubProcess.bpmn20.xml"})
  public void testRemoveVariablesLocalWithParentScope() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("variable1", "value1");
    vars.put("variable2", "value2");

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startSimpleSubProcess", vars);

    Task currentTask = taskService.createTaskQuery().singleResult();
    Map<String, Object> varsToDelete = new HashMap<String, Object>();
    varsToDelete.put("variable3", "value3");
    varsToDelete.put("variable4", "value4");
    varsToDelete.put("variable5", "value5");
    runtimeService.setVariablesLocal(currentTask.getExecutionId(), varsToDelete);
    runtimeService.setVariableLocal(currentTask.getExecutionId(), "variable6", "value6");

    assertEquals("value3", runtimeService.getVariable(currentTask.getExecutionId(), "variable3"));
    assertEquals("value3", runtimeService.getVariableLocal(currentTask.getExecutionId(), "variable3"));
    assertEquals("value4", runtimeService.getVariable(currentTask.getExecutionId(), "variable4"));
    assertEquals("value4", runtimeService.getVariableLocal(currentTask.getExecutionId(), "variable4"));
    assertEquals("value5", runtimeService.getVariable(currentTask.getExecutionId(), "variable5"));
    assertEquals("value5", runtimeService.getVariableLocal(currentTask.getExecutionId(), "variable5"));
    assertEquals("value6", runtimeService.getVariable(currentTask.getExecutionId(), "variable6"));
    assertEquals("value6", runtimeService.getVariableLocal(currentTask.getExecutionId(), "variable6"));

    runtimeService.removeVariablesLocal(currentTask.getExecutionId(), varsToDelete.keySet());

    assertEquals("value1", runtimeService.getVariable(currentTask.getExecutionId(), "variable1"));
    assertEquals("value2", runtimeService.getVariable(currentTask.getExecutionId(), "variable2"));

    assertNull(runtimeService.getVariable(currentTask.getExecutionId(), "variable3"));
    assertNull(runtimeService.getVariableLocal(currentTask.getExecutionId(), "variable3"));
    assertNull(runtimeService.getVariable(currentTask.getExecutionId(), "variable4"));
    assertNull(runtimeService.getVariableLocal(currentTask.getExecutionId(), "variable4"));
    assertNull(runtimeService.getVariable(currentTask.getExecutionId(), "variable5"));
    assertNull(runtimeService.getVariableLocal(currentTask.getExecutionId(), "variable5"));

    assertEquals("value6", runtimeService.getVariable(currentTask.getExecutionId(), "variable6"));
    assertEquals("value6", runtimeService.getVariableLocal(currentTask.getExecutionId(), "variable6"));

    checkHistoricVariableUpdateEntity("variable3", processInstance.getId());
    checkHistoricVariableUpdateEntity("variable4", processInstance.getId());
    checkHistoricVariableUpdateEntity("variable5", processInstance.getId());
  }

  @SuppressWarnings("unchecked")
  public void testRemoveVariablesLocalNullExecutionId() {
    try {
      runtimeService.removeVariablesLocal(null, Collections.EMPTY_LIST);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("executionId is null", ae.getMessage());
    }
  }

  @Deployment(resources={
  "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testUpdateVariables() {
    Map<String, Object> modifications = new HashMap<String, Object>();
    modifications.put("variable1", "value1");
    modifications.put("variable2", "value2");

    List<String> deletions = new ArrayList<String>();
    deletions.add("variable1");

    Map<String, Object> initialVariables = new HashMap<String, Object>();
    initialVariables.put("variable1", "initialValue");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", initialVariables);
    ((RuntimeServiceImpl) runtimeService).updateVariables(processInstance.getId(), modifications, deletions);

    assertNull(runtimeService.getVariable(processInstance.getId(), "variable1"));
    assertEquals("value2", runtimeService.getVariable(processInstance.getId(), "variable2"));
  }

  @Deployment(resources={
  "org/camunda/bpm/engine/test/api/oneSubProcess.bpmn20.xml"})
  public void testUpdateVariablesLocal() {
    Map<String, Object> globalVars = new HashMap<String, Object>();
    globalVars.put("variable4", "value4");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startSimpleSubProcess", globalVars);

    Task currentTask = taskService.createTaskQuery().singleResult();
    Map<String, Object> localVars = new HashMap<String, Object>();
    localVars.put("variable1", "value1");
    localVars.put("variable2", "value2");
    localVars.put("variable3", "value3");
    runtimeService.setVariablesLocal(currentTask.getExecutionId(), localVars);

    Map<String, Object> modifications = new HashMap<String, Object>();
    modifications.put("variable1", "anotherValue1");
    modifications.put("variable2", "anotherValue2");

    List<String> deletions = new ArrayList<String>();
    deletions.add("variable2");
    deletions.add("variable3");
    deletions.add("variable4");

    ((RuntimeServiceImpl) runtimeService).updateVariablesLocal(currentTask.getExecutionId(), modifications, deletions);

    assertEquals("anotherValue1", runtimeService.getVariable(currentTask.getExecutionId(), "variable1"));
    assertNull(runtimeService.getVariable(currentTask.getExecutionId(), "variable2"));
    assertNull(runtimeService.getVariable(currentTask.getExecutionId(), "variable3"));
    assertEquals("value4", runtimeService.getVariable(processInstance.getId(), "variable4"));
  }

  @Deployment(resources={
          "org/camunda/bpm/engine/test/api/runtime/RuntimeServiceTest.catchAlertSignal.bpmn20.xml",
          "org/camunda/bpm/engine/test/api/runtime/RuntimeServiceTest.catchPanicSignal.bpmn20.xml"
  })
  public void testSignalEventReceived() {

    //////  test  signalEventReceived(String)

    startSignalCatchProcesses();
    // 12, because the signal catch is a scope
    assertEquals(12, runtimeService.createExecutionQuery().count());
    runtimeService.signalEventReceived("alert");
    assertEquals(6, runtimeService.createExecutionQuery().count());
    runtimeService.signalEventReceived("panic");
    assertEquals(0, runtimeService.createExecutionQuery().count());

    //////  test  signalEventReceived(String, String)
    startSignalCatchProcesses();

    // signal the executions one at a time:
    for (int executions = 3; executions > 0; executions--) {
      List<Execution> page = runtimeService.createExecutionQuery()
        .signalEventSubscriptionName("alert")
        .listPage(0, 1);
      runtimeService.signalEventReceived("alert", page.get(0).getId());

      assertEquals(executions-1, runtimeService.createExecutionQuery().signalEventSubscriptionName("alert").count());
    }

    for (int executions = 3; executions > 0; executions-- ) {
      List<Execution> page = runtimeService.createExecutionQuery()
        .signalEventSubscriptionName("panic")
        .listPage(0, 1);
      runtimeService.signalEventReceived("panic", page.get(0).getId());

      assertEquals(executions-1, runtimeService.createExecutionQuery().signalEventSubscriptionName("panic").count());
    }

  }

  @Deployment(resources={
          "org/camunda/bpm/engine/test/api/runtime/RuntimeServiceTest.catchAlertMessage.bpmn20.xml",
          "org/camunda/bpm/engine/test/api/runtime/RuntimeServiceTest.catchPanicMessage.bpmn20.xml"
  })
  public void testMessageEventReceived() {

    startMessageCatchProcesses();
    // 12, because the signal catch is a scope
    assertEquals(12, runtimeService.createExecutionQuery().count());

    // signal the executions one at a time:
    for (int executions = 3; executions > 0; executions--) {
      List<Execution> page = runtimeService.createExecutionQuery()
        .messageEventSubscriptionName("alert")
        .listPage(0, 1);
      runtimeService.messageEventReceived("alert", page.get(0).getId());

      assertEquals(executions-1, runtimeService.createExecutionQuery().messageEventSubscriptionName("alert").count());
    }

    for (int executions = 3; executions > 0; executions-- ) {
      List<Execution> page = runtimeService.createExecutionQuery()
        .messageEventSubscriptionName("panic")
        .listPage(0, 1);
      runtimeService.messageEventReceived("panic", page.get(0).getId());

      assertEquals(executions-1, runtimeService.createExecutionQuery().messageEventSubscriptionName("panic").count());
    }

  }

 public void testSignalEventReceivedNonExistingExecution() {
   try {
     runtimeService.signalEventReceived("alert", "nonexistingExecution");
     fail("exeception expected");
   }catch (ProcessEngineException e) {
     // this is good
     assertTrue(e.getMessage().contains("Cannot find execution with id 'nonexistingExecution'"));
   }
  }

 public void testMessageEventReceivedNonExistingExecution() {
   try {
     runtimeService.messageEventReceived("alert", "nonexistingExecution");
     fail("exeception expected");
   }catch (ProcessEngineException e) {
     // this is good
     assertTrue(e.getMessage().contains("Execution with id 'nonexistingExecution' does not have a subscription to a message event with name 'alert'"));
   }
  }

 @Deployment(resources={
         "org/camunda/bpm/engine/test/api/runtime/RuntimeServiceTest.catchAlertSignal.bpmn20.xml"
 })
 public void testExecutionWaitingForDifferentSignal() {
   runtimeService.startProcessInstanceByKey("catchAlertSignal");
   Execution execution = runtimeService.createExecutionQuery()
     .signalEventSubscriptionName("alert")
     .singleResult();
   try {
     runtimeService.signalEventReceived("bogusSignal", execution.getId());
     fail("exeception expected");
   }catch (ProcessEngineException e) {
     // this is good
     assertTrue(e.getMessage().contains("has not subscribed to a signal event with name 'bogusSignal'"));
   }
  }

  private void startSignalCatchProcesses() {
    for (int i = 0; i < 3; i++) {
      runtimeService.startProcessInstanceByKey("catchAlertSignal");
      runtimeService.startProcessInstanceByKey("catchPanicSignal");
    }
  }

  private void startMessageCatchProcesses() {
    for (int i = 0; i < 3; i++) {
      runtimeService.startProcessInstanceByKey("catchAlertMessage");
      runtimeService.startProcessInstanceByKey("catchPanicMessage");
    }
  }

  // getActivityInstance Tests //////////////////////////////////

  public void testActivityInstanceForNonExistingProcessInstanceId() {
    assertNull(runtimeService.getActivityInstance("some-nonexisting-id"));
  }

  public void testActivityInstanceForNullProcessInstanceId() {
    try {
      runtimeService.getActivityInstance(null);
      fail("PEE expected!");
    } catch (ProcessEngineException engineException) {
      assertTrue(engineException.getMessage().contains("processInstanceId is null"));
    }
  }

  @Deployment(resources={
  "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testActivityInstancePopulated() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", "business-key");

    // validate properties of root
    ActivityInstance rootActInstance = runtimeService.getActivityInstance(processInstance.getId());
    assertEquals(processInstance.getId(), rootActInstance.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), rootActInstance.getProcessDefinitionId());
    assertEquals(processInstance.getId(), rootActInstance.getProcessInstanceId());
    assertTrue(rootActInstance.getExecutionIds()[0].equals(processInstance.getId()));
    assertEquals(rootActInstance.getProcessDefinitionId(), rootActInstance.getActivityId());
    assertNull(rootActInstance.getParentActivityInstanceId());
    assertEquals("processDefinition", rootActInstance.getActivityType());

    // validate properties of child:
    Task task = taskService.createTaskQuery().singleResult();
    ActivityInstance childActivityInstance = rootActInstance.getChildActivityInstances()[0];
    assertEquals(processInstance.getId(), childActivityInstance.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), childActivityInstance.getProcessDefinitionId());
    assertEquals(processInstance.getId(), childActivityInstance.getProcessInstanceId());
    assertTrue(childActivityInstance.getExecutionIds()[0].equals(task.getExecutionId()));
    assertEquals("theTask", childActivityInstance.getActivityId());
    assertEquals(rootActInstance.getId(), childActivityInstance.getParentActivityInstanceId());
    assertEquals("userTask", childActivityInstance.getActivityType());
    assertNotNull(childActivityInstance.getChildActivityInstances());
    assertNotNull(childActivityInstance.getChildTransitionInstances());
    assertEquals(0, childActivityInstance.getChildActivityInstances().length);
    assertEquals(0, childActivityInstance.getChildTransitionInstances().length);

  }

  @Deployment
  public void testActivityInstanceTreeForAsyncBeforeTask() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .transition("theTask")
        .done());

    TransitionInstance asyncBeforeTransitionInstance = tree.getChildTransitionInstances()[0];
    assertEquals(processInstance.getId(), asyncBeforeTransitionInstance.getExecutionId());
  }

  @Deployment
  public void testActivityInstanceTreeForConcurrentAsyncBeforeTask() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("concurrentTasksProcess");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .activity("theTask")
          .transition("asyncTask")
        .done());

    TransitionInstance asyncBeforeTransitionInstance = tree.getChildTransitionInstances()[0];
    String asyncExecutionId = managementService.createJobQuery().singleResult().getExecutionId();
    assertEquals(asyncExecutionId, asyncBeforeTransitionInstance.getExecutionId());
  }

  @Deployment
  public void testActivityInstanceTreeForAsyncBeforeStartEvent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .transition("theStart")
        .done());

    TransitionInstance asyncBeforeTransitionInstance = tree.getChildTransitionInstances()[0];
    assertEquals(processInstance.getId(), asyncBeforeTransitionInstance.getExecutionId());
  }

  @Deployment
  public void testActivityInstanceTreeForAsyncAfterTask() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());


    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .transition("theTask")
        .done());

    TransitionInstance asyncAfterTransitionInstance = tree.getChildTransitionInstances()[0];
    assertEquals(processInstance.getId(), asyncAfterTransitionInstance.getExecutionId());
  }

  @Deployment
  public void testActivityInstanceTreeForConcurrentAsyncAfterTask() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("concurrentTasksProcess");

    Task asyncTask = taskService.createTaskQuery().taskDefinitionKey("asyncTask").singleResult();
    assertNotNull(asyncTask);
    taskService.complete(asyncTask.getId());

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .activity("theTask")
          .transition("asyncTask")
        .done());

    TransitionInstance asyncBeforeTransitionInstance = tree.getChildTransitionInstances()[0];
    String asyncExecutionId = managementService.createJobQuery().singleResult().getExecutionId();
    assertEquals(asyncExecutionId, asyncBeforeTransitionInstance.getExecutionId());
  }

  @Deployment
  public void testActivityInstanceTreeForAsyncAfterEndEvent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("asyncEndEventProcess");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .transition("theEnd")
        .done());

    TransitionInstance asyncAfterTransitionInstance = tree.getChildTransitionInstances()[0];
    assertEquals(processInstance.getId(), asyncAfterTransitionInstance.getExecutionId());
  }

  @Deployment
  public void testActivityInstanceTreeForNestedAsyncBeforeTask() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .beginScope("subProcess")
            .transition("theTask")
        .done());

    TransitionInstance asyncBeforeTransitionInstance = tree.getChildActivityInstances()[0]
        .getChildTransitionInstances()[0];
    String asyncExecutionId = managementService.createJobQuery().singleResult().getExecutionId();
    assertEquals(asyncExecutionId, asyncBeforeTransitionInstance.getExecutionId());
  }

  /**
   * requires fix for CAM-3662
   */
  @Deployment
  public void FAILING_testActivityInstanceTreeForNestedAsyncBeforeStartEvent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .beginScope("subProcess")
            .transition("theSubProcessStart")
        .done());

    TransitionInstance asyncBeforeTransitionInstance = tree.getChildTransitionInstances()[0];
    assertEquals(processInstance.getId(), asyncBeforeTransitionInstance.getExecutionId());
  }

  @Deployment
  public void testActivityInstanceTreeForNestedAsyncAfterTask() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());


    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .beginScope("subProcess")
            .transition("theTask")
        .done());

    TransitionInstance asyncAfterTransitionInstance = tree.getChildActivityInstances()[0]
        .getChildTransitionInstances()[0];
    String asyncExecutionId = managementService.createJobQuery().singleResult().getExecutionId();
    assertEquals(asyncExecutionId, asyncAfterTransitionInstance.getExecutionId());
  }

  @Deployment
  public void testActivityInstanceTreeForNestedAsyncAfterEndEvent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("asyncEndEventProcess");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .beginScope("subProcess")
            .transition("theSubProcessEnd")
        .done());

    TransitionInstance asyncAfterTransitionInstance = tree.getChildActivityInstances()[0]
        .getChildTransitionInstances()[0];
    String asyncExecutionId = managementService.createJobQuery().singleResult().getExecutionId();
    assertEquals(asyncExecutionId, asyncAfterTransitionInstance.getExecutionId());
  }

  /**
   * Test for CAM-3572
   */
  @Deployment
  public void testActivityInstanceForConcurrentSubprocess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("concurrentSubProcess");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertNotNull(tree);

    assertThat(tree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .activity("outerTask")
          .beginScope("subProcess")
            .activity("innerTask")
        .done());
  }

  @Deployment
  public void testGetActivityInstancesForActivity() {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("miSubprocess");
    ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().singleResult();

    // when
    ActivityInstance tree = runtimeService.getActivityInstance(instance.getId());

    // then
    ActivityInstance[] processActivityInstances = tree.getActivityInstances(definition.getId());
    assertEquals(1, processActivityInstances.length);
    assertEquals(tree.getId(), processActivityInstances[0].getId());
    assertEquals(definition.getId(), processActivityInstances[0].getActivityId());

    assertActivityInstances(tree.getActivityInstances("subProcess#multiInstanceBody"), 1, "subProcess#multiInstanceBody");
    assertActivityInstances(tree.getActivityInstances("subProcess"), 3, "subProcess");
    assertActivityInstances(tree.getActivityInstances("innerTask"), 3, "innerTask");

    ActivityInstance subProcessInstance = tree.getChildActivityInstances()[0].getChildActivityInstances()[0];
    assertActivityInstances(subProcessInstance.getActivityInstances("subProcess"), 1, "subProcess");

    ActivityInstance[] childInstances = subProcessInstance.getActivityInstances("innerTask");
    assertEquals(1, childInstances.length);
    assertEquals(subProcessInstance.getChildActivityInstances()[0].getId(), childInstances[0].getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/RuntimeServiceTest.testGetActivityInstancesForActivity.bpmn20.xml")
  public void testGetInvalidActivityInstancesForActivity() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("miSubprocess");

    ActivityInstance tree = runtimeService.getActivityInstance(instance.getId());

    try {
      tree.getActivityInstances(null);
      fail("exception expected");
    } catch (NullValueException e) {
      // happy path
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/RuntimeServiceTest.testGetActivityInstancesForActivity.bpmn20.xml")
  public void testGetActivityInstancesForNonExistingActivity() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("miSubprocess");

    ActivityInstance tree = runtimeService.getActivityInstance(instance.getId());

    ActivityInstance[] instances = tree.getActivityInstances("aNonExistingActivityId");
    assertNotNull(instances);
    assertEquals(0, instances.length);
  }

  @Deployment
  public void testGetTransitionInstancesForActivity() {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("miSubprocess");

    // complete one async task
    Job job = managementService.createJobQuery().listPage(0, 1).get(0);
    managementService.executeJob(job.getId());
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    // when
    ActivityInstance tree = runtimeService.getActivityInstance(instance.getId());

    // then
    assertEquals(0, tree.getTransitionInstances("subProcess").length);
    TransitionInstance[] asyncBeforeInstances = tree.getTransitionInstances("innerTask");
    assertEquals(2, asyncBeforeInstances.length);

    assertEquals("innerTask", asyncBeforeInstances[0].getActivityId());
    assertEquals("innerTask", asyncBeforeInstances[1].getActivityId());
    assertFalse(asyncBeforeInstances[0].getId().equals(asyncBeforeInstances[1].getId()));

    TransitionInstance[] asyncEndEventInstances = tree.getTransitionInstances("theSubProcessEnd");
    assertEquals(1, asyncEndEventInstances.length);
    assertEquals("theSubProcessEnd", asyncEndEventInstances[0].getActivityId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/RuntimeServiceTest.testGetTransitionInstancesForActivity.bpmn20.xml")
  public void testGetInvalidTransitionInstancesForActivity() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("miSubprocess");

    ActivityInstance tree = runtimeService.getActivityInstance(instance.getId());

    try {
      tree.getTransitionInstances(null);
      fail("exception expected");
    } catch (NullValueException e) {
      // happy path
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/RuntimeServiceTest.testGetTransitionInstancesForActivity.bpmn20.xml")
  public void testGetTransitionInstancesForNonExistingActivity() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("miSubprocess");

    ActivityInstance tree = runtimeService.getActivityInstance(instance.getId());

    TransitionInstance[] instances = tree.getTransitionInstances("aNonExistingActivityId");
    assertNotNull(instances);
    assertEquals(0, instances.length);
  }


  protected void assertActivityInstances(ActivityInstance[] instances, int expectedAmount, String expectedActivityId) {
    assertEquals(expectedAmount, instances.length);

    Set<String> instanceIds = new HashSet<String>();

    for (ActivityInstance instance : instances) {
      assertEquals(expectedActivityId, instance.getActivityId());
      instanceIds.add(instance.getId());
    }

    // ensure that all instances are unique
    assertEquals(expectedAmount, instanceIds.size());
  }


  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testChangeVariableType() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    DummySerializable dummy = new DummySerializable();
    runtimeService.setVariable(instance.getId(), "dummy", dummy);

    runtimeService.setVariable(instance.getId(), "dummy", 47);

    VariableInstance variableInstance = runtimeService.createVariableInstanceQuery().singleResult();

    assertEquals(47, variableInstance.getValue());
    assertEquals(ValueType.INTEGER.getName(), variableInstance.getTypeName());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testStartByKeyWithCaseInstanceId() {
    String caseInstanceId = "aCaseInstanceId";

    ProcessInstance firstInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", null, caseInstanceId);

    assertEquals(caseInstanceId, firstInstance.getCaseInstanceId());

    // load process instance from db
    firstInstance = runtimeService
        .createProcessInstanceQuery()
        .processInstanceId(firstInstance.getId())
        .singleResult();

    assertNotNull(firstInstance);

    assertEquals(caseInstanceId, firstInstance.getCaseInstanceId());

    // the second possibility to start a process instance /////////////////////////////////////////////

    ProcessInstance secondInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", null, caseInstanceId, null);

    assertEquals(caseInstanceId, secondInstance.getCaseInstanceId());

    // load process instance from db
    secondInstance = runtimeService
        .createProcessInstanceQuery()
        .processInstanceId(secondInstance.getId())
        .singleResult();

    assertNotNull(secondInstance);

    assertEquals(caseInstanceId, secondInstance.getCaseInstanceId());

  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testStartByIdWithCaseInstanceId() {
    String processDefinitionId = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("oneTaskProcess")
        .singleResult()
        .getId();

    String caseInstanceId = "aCaseInstanceId";
    ProcessInstance firstInstance = runtimeService.startProcessInstanceById(processDefinitionId, null, caseInstanceId);

    assertEquals(caseInstanceId, firstInstance.getCaseInstanceId());

    // load process instance from db
    firstInstance = runtimeService
        .createProcessInstanceQuery()
        .processInstanceId(firstInstance.getId())
        .singleResult();

    assertNotNull(firstInstance);

    assertEquals(caseInstanceId, firstInstance.getCaseInstanceId());

    // the second possibility to start a process instance /////////////////////////////////////////////

    ProcessInstance secondInstance = runtimeService.startProcessInstanceById(processDefinitionId, null, caseInstanceId, null);

    assertEquals(caseInstanceId, secondInstance.getCaseInstanceId());

    // load process instance from db
    secondInstance = runtimeService
        .createProcessInstanceQuery()
        .processInstanceId(secondInstance.getId())
        .singleResult();

    assertNotNull(secondInstance);

    assertEquals(caseInstanceId, secondInstance.getCaseInstanceId());

  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testSetAbstractNumberValueFails() {
    try {
      runtimeService.startProcessInstanceByKey("oneTaskProcess",
          Variables.createVariables().putValueTyped("var", Variables.numberValue(42)));
      fail("exception expected");
    } catch (ProcessEngineException e) {
      // happy path
      assertTextPresentIgnoreCase("cannot serialize value of abstract type number", e.getMessage());
    }

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    try {
      runtimeService.setVariable(processInstance.getId(), "var", Variables.numberValue(42));
      fail("exception expected");
    } catch (ProcessEngineException e) {
      // happy path
      assertTextPresentIgnoreCase("cannot serialize value of abstract type number", e.getMessage());
    }
  }


  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/messageStartEvent.bpmn20.xml")
  public void testStartProcessInstanceByMessageWithEarlierVersionOfProcessDefinition() {
	  String deploymentId = repositoryService.createDeployment().addClasspathResource("org/camunda/bpm/engine/test/api/runtime/messageStartEvent_version2.bpmn20.xml").deploy().getId();
	  ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionVersion(1).singleResult();

	  ProcessInstance processInstance = runtimeService.startProcessInstanceByMessageAndProcessDefinitionId("startMessage", processDefinition.getId());

	  assertThat(processInstance, is(notNullValue()));
	  assertThat(processInstance.getProcessDefinitionId(), is(processDefinition.getId()));

	  // clean up
	  repositoryService.deleteDeployment(deploymentId, true);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/messageStartEvent.bpmn20.xml")
  public void testStartProcessInstanceByMessageWithLastVersionOfProcessDefinition() {
	  String deploymentId = repositoryService.createDeployment().addClasspathResource("org/camunda/bpm/engine/test/api/runtime/messageStartEvent_version2.bpmn20.xml").deploy().getId();
	  ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().latestVersion().singleResult();

	  ProcessInstance processInstance = runtimeService.startProcessInstanceByMessageAndProcessDefinitionId("newStartMessage", processDefinition.getId());

	  assertThat(processInstance, is(notNullValue()));
	  assertThat(processInstance.getProcessDefinitionId(), is(processDefinition.getId()));

	  // clean up
	  repositoryService.deleteDeployment(deploymentId, true);
   }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/messageStartEvent.bpmn20.xml")
  public void testStartProcessInstanceByMessageWithNonExistingMessageStartEvent() {
	  String deploymentId = null;
	  try {
		 deploymentId = repositoryService.createDeployment().addClasspathResource("org/camunda/bpm/engine/test/api/runtime/messageStartEvent_version2.bpmn20.xml").deploy().getId();
		 ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionVersion(1).singleResult();

		 runtimeService.startProcessInstanceByMessageAndProcessDefinitionId("newStartMessage", processDefinition.getId());

		 fail("exeception expected");
	 } catch(ProcessEngineException e) {
		 assertThat(e.getMessage(), containsString("no message start event with name 'newStartMessage' found"));
	 }
	 finally {
		 // clean up
		 if(deploymentId != null){
			 repositoryService.deleteDeployment(deploymentId, true);
		 }
	 }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testActivityInstanceActivityNameProperty() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("oneTaskProcess").getId();

    // when
    ActivityInstance tree = runtimeService.getActivityInstance(processInstanceId);

    // then
    ActivityInstance[] activityInstances = tree.getActivityInstances("theTask");
    assertEquals(1, activityInstances.length);

    ActivityInstance task = activityInstances[0];
    assertNotNull(task);
    assertNotNull(task.getActivityName());
    assertEquals("my task", task.getActivityName());
  }

  @Deployment
  public void testTransitionInstanceActivityNamePropertyBeforeTask() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // when
    ActivityInstance tree = runtimeService.getActivityInstance(processInstanceId);

    // then
    TransitionInstance[] instances = tree.getTransitionInstances("firstServiceTask");
    TransitionInstance task = instances[0];
    assertNotNull(task);
    assertNotNull(task.getActivityName());
    assertEquals("First Service Task", task.getActivityName());

    instances = tree.getTransitionInstances("secondServiceTask");
    task = instances[0];
    assertNotNull(task);
    assertNotNull(task.getActivityName());
    assertEquals("Second Service Task", task.getActivityName());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/RuntimeServiceTest.testTransitionInstanceActivityNamePropertyBeforeTask.bpmn20.xml")
  public void testTransitionInstanceActivityTypePropertyBeforeTask() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // when
    ActivityInstance tree = runtimeService.getActivityInstance(processInstanceId);

    // then
    TransitionInstance[] instances = tree.getTransitionInstances("firstServiceTask");
    TransitionInstance task = instances[0];
    assertNotNull(task);
    assertNotNull(task.getActivityType());
    assertEquals("serviceTask", task.getActivityType());

    instances = tree.getTransitionInstances("secondServiceTask");
    task = instances[0];
    assertNotNull(task);
    assertNotNull(task.getActivityType());
    assertEquals("serviceTask", task.getActivityType());
  }

  @Deployment
  public void testTransitionInstanceActivityNamePropertyAfterTask() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // when
    ActivityInstance tree = runtimeService.getActivityInstance(processInstanceId);

    // then
    TransitionInstance[] instances = tree.getTransitionInstances("firstServiceTask");
    TransitionInstance task = instances[0];
    assertNotNull(task);
    assertNotNull(task.getActivityName());
    assertEquals("First Service Task", task.getActivityName());

    instances = tree.getTransitionInstances("secondServiceTask");
    task = instances[0];
    assertNotNull(task);
    assertNotNull(task.getActivityName());
    assertEquals("Second Service Task", task.getActivityName());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/RuntimeServiceTest.testTransitionInstanceActivityNamePropertyAfterTask.bpmn20.xml")
  public void testTransitionInstanceActivityTypePropertyAfterTask() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // when
    ActivityInstance tree = runtimeService.getActivityInstance(processInstanceId);

    // then
    TransitionInstance[] instances = tree.getTransitionInstances("firstServiceTask");
    TransitionInstance task = instances[0];
    assertNotNull(task);
    assertNotNull(task.getActivityType());
    assertEquals("serviceTask", task.getActivityType());

    instances = tree.getTransitionInstances("secondServiceTask");
    task = instances[0];
    assertNotNull(task);
    assertNotNull(task.getActivityType());
    assertEquals("serviceTask", task.getActivityType());
  }

  @Deployment
  public void testTransitionInstanceActivityNamePropertyBeforeStartEvent() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // when
    ActivityInstance tree = runtimeService.getActivityInstance(processInstanceId);

    // then
    TransitionInstance[] instances = tree.getTransitionInstances("start");
    TransitionInstance task = instances[0];
    assertNotNull(task);
    assertNotNull(task.getActivityName());
    assertEquals("The Start Event", task.getActivityName());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/RuntimeServiceTest.testTransitionInstanceActivityNamePropertyBeforeStartEvent.bpmn20.xml")
  public void testTransitionInstanceActivityTypePropertyBeforeStartEvent() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // when
    ActivityInstance tree = runtimeService.getActivityInstance(processInstanceId);

    // then
    TransitionInstance[] instances = tree.getTransitionInstances("start");
    TransitionInstance task = instances[0];
    assertNotNull(task);
    assertNotNull(task.getActivityType());
    assertEquals("startEvent", task.getActivityType());
  }

  @Deployment
  public void testTransitionInstanceActivityNamePropertyAfterStartEvent() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // when
    ActivityInstance tree = runtimeService.getActivityInstance(processInstanceId);

    // then
    TransitionInstance[] instances = tree.getTransitionInstances("start");
    TransitionInstance task = instances[0];
    assertNotNull(task);
    assertNotNull(task.getActivityName());
    assertEquals("The Start Event", task.getActivityName());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/RuntimeServiceTest.testTransitionInstanceActivityNamePropertyAfterStartEvent.bpmn20.xml")
  public void testTransitionInstanceActivityTypePropertyAfterStartEvent() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // when
    ActivityInstance tree = runtimeService.getActivityInstance(processInstanceId);

    // then
    TransitionInstance[] instances = tree.getTransitionInstances("start");
    TransitionInstance task = instances[0];
    assertNotNull(task);
    assertNotNull(task.getActivityType());
    assertEquals("startEvent", task.getActivityType());
  }

  //Test for a bug: when the process engine is rebooted the
  // cache is cleaned and the deployed process definition is
  // removed from the process cache. This led to problems because
  // the id wasnt fetched from the DB after a redeploy.
  public void testStartProcessInstanceByIdAfterReboot() {

    // In case this test is run in a test suite, previous engines might
    // have been initialized and cached.  First we close the
    // existing process engines to make sure that the db is clean
    // and that there are no existing process engines involved.
    ProcessEngines.destroy();

    // Creating the DB schema (without building a process engine)
    ProcessEngineConfigurationImpl processEngineConfiguration = new StandaloneInMemProcessEngineConfiguration();
    processEngineConfiguration.setProcessEngineName("reboot-test-schema");
    processEngineConfiguration.setJdbcUrl("jdbc:h2:mem:activiti-reboot-test;DB_CLOSE_DELAY=1000");
    ProcessEngine schemaProcessEngine = processEngineConfiguration.buildProcessEngine();

    // Create process engine and deploy test process
    ProcessEngine processEngine = new StandaloneProcessEngineConfiguration()
      .setProcessEngineName("reboot-test")
      .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE)
      .setJdbcUrl("jdbc:h2:mem:activiti-reboot-test;DB_CLOSE_DELAY=1000")
      .setJobExecutorActivate(false)
      .buildProcessEngine();

    processEngine.getRepositoryService()
      .createDeployment()
      .addClasspathResource("org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
      .deploy();
      // verify existence of process definition
    List<ProcessDefinition> processDefinitions = processEngine
      .getRepositoryService()
      .createProcessDefinitionQuery()
      .list();

    assertEquals(1, processDefinitions.size());

    // Start a new Process instance
    ProcessInstance processInstance = processEngine.getRuntimeService().startProcessInstanceById(processDefinitions.get(0).getId());
    String processInstanceId = processInstance.getId();
    assertNotNull(processInstance);

    // Close the process engine
    processEngine.close();
    assertNotNull(processEngine.getRuntimeService());

    // Reboot the process engine
    processEngine = new StandaloneProcessEngineConfiguration()
      .setProcessEngineName("reboot-test")
      .setDatabaseSchemaUpdate(org.camunda.bpm.engine.ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE)
      .setJdbcUrl("jdbc:h2:mem:activiti-reboot-test;DB_CLOSE_DELAY=1000")
      .setJobExecutorActivate(false)
      .buildProcessEngine();

    // Check if the existing process instance is still alive
    processInstance = processEngine
      .getRuntimeService()
      .createProcessInstanceQuery()
      .processInstanceId(processInstanceId)
      .singleResult();

    assertNotNull(processInstance);

    // Complete the task.  That will end the process instance
    TaskService taskService = processEngine.getTaskService();
    Task task = taskService
      .createTaskQuery()
      .list()
      .get(0);
    taskService.complete(task.getId());

    // Check if the process instance has really ended.  This means that the process definition has
    // re-loaded into the process definition cache
    processInstance = processEngine
      .getRuntimeService()
      .createProcessInstanceQuery()
      .processInstanceId(processInstanceId)
      .singleResult();
    assertNull(processInstance);

    // Extra check to see if a new process instance can be started as well
    processInstance = processEngine.getRuntimeService().startProcessInstanceById(processDefinitions.get(0).getId());
    assertNotNull(processInstance);

    // close the process engine
    processEngine.close();

    // Cleanup schema
    schemaProcessEngine.close();
  }

  @Deployment
  public void testVariableScope() {

    // After starting the process, the task in the subprocess should be active
    Map<String, Object> varMap = new HashMap<String, Object>();
    varMap.put("test", "test");
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleSubProcess", varMap);
    Task subProcessTask = taskService.createTaskQuery()
        .processInstanceId(pi.getId())
        .singleResult();
    assertEquals("Task in subprocess", subProcessTask.getName());

    // get variables for execution id user task, should return the new value of variable test --> test2
    assertEquals("test2", runtimeService.getVariable(subProcessTask.getExecutionId(), "test"));
    assertEquals("test2", runtimeService.getVariables(subProcessTask.getExecutionId()).get("test"));

    // get variables for process instance id, should return the initial value of variable test --> test
    assertEquals("test", runtimeService.getVariable(pi.getId(), "test"));
    assertEquals("test", runtimeService.getVariables(pi.getId()).get("test"));

    runtimeService.setVariableLocal(subProcessTask.getExecutionId(), "test", "test3");

    // get variables for execution id user task, should return the new value of variable test --> test3
    assertEquals("test3", runtimeService.getVariable(subProcessTask.getExecutionId(), "test"));
    assertEquals("test3", runtimeService.getVariables(subProcessTask.getExecutionId()).get("test"));

    // get variables for process instance id, should still return the initial value of variable test --> test
    assertEquals("test", runtimeService.getVariable(pi.getId(), "test"));
    assertEquals("test", runtimeService.getVariables(pi.getId()).get("test"));

    runtimeService.setVariable(pi.getId(), "test", "test4");

    // get variables for execution id user task, should return the old value of variable test --> test3
    assertEquals("test3", runtimeService.getVariable(subProcessTask.getExecutionId(), "test"));
    assertEquals("test3", runtimeService.getVariables(subProcessTask.getExecutionId()).get("test"));

    // get variables for process instance id, should also return the initial value of variable test --> test4
    assertEquals("test4", runtimeService.getVariable(pi.getId(), "test"));
    assertEquals("test4", runtimeService.getVariables(pi.getId()).get("test"));

    // After completing the task in the subprocess,
    // the subprocess scope is destroyed and the complete process ends
    taskService.complete(subProcessTask.getId());
  }

  @Deployment
  public void testBasicVariableOperations() {

    Date now = new Date();
    List<String> serializable = new ArrayList<String>();
    serializable.add("one");
    serializable.add("two");
    serializable.add("three");
    byte[] bytes = "somebytes".getBytes();
    byte[] streamBytes = "morebytes".getBytes();

    // Start process instance with different types of variables
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("longVar", 928374L);
    variables.put("shortVar", (short) 123);
    variables.put("integerVar", 1234);
    variables.put("stringVar", "coca-cola");
    variables.put("dateVar", now);
    variables.put("nullVar", null);
    variables.put("serializableVar", serializable);
    variables.put("bytesVar", bytes);
    variables.put("byteStreamVar", new ByteArrayInputStream(streamBytes));
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskAssigneeProcess", variables);

    variables = runtimeService.getVariables(processInstance.getId());
    assertEquals("coca-cola", variables.get("stringVar"));
    assertEquals(928374L, variables.get("longVar"));
    assertEquals((short) 123, variables.get("shortVar"));
    assertEquals(1234, variables.get("integerVar"));
    assertEquals(now, variables.get("dateVar"));
    assertEquals(null, variables.get("nullVar"));
    assertEquals(serializable, variables.get("serializableVar"));
    assertTrue(Arrays.equals(bytes, (byte[]) variables.get("bytesVar")));
    assertTrue(Arrays.equals(streamBytes, (byte[]) variables.get("byteStreamVar")));
    assertEquals(9, variables.size());

    // Set all existing variables values to null
    runtimeService.setVariable(processInstance.getId(), "longVar", null);
    runtimeService.setVariable(processInstance.getId(), "shortVar", null);
    runtimeService.setVariable(processInstance.getId(), "integerVar", null);
    runtimeService.setVariable(processInstance.getId(), "stringVar", null);
    runtimeService.setVariable(processInstance.getId(), "dateVar", null);
    runtimeService.setVariable(processInstance.getId(), "nullVar", null);
    runtimeService.setVariable(processInstance.getId(), "serializableVar", null);
    runtimeService.setVariable(processInstance.getId(), "bytesVar", null);
    runtimeService.setVariable(processInstance.getId(), "byteStreamVar", null);

    variables = runtimeService.getVariables(processInstance.getId());
    assertEquals(null, variables.get("longVar"));
    assertEquals(null, variables.get("shortVar"));
    assertEquals(null, variables.get("integerVar"));
    assertEquals(null, variables.get("stringVar"));
    assertEquals(null, variables.get("dateVar"));
    assertEquals(null, variables.get("nullVar"));
    assertEquals(null, variables.get("serializableVar"));
    assertEquals(null, variables.get("bytesVar"));
    assertEquals(null, variables.get("byteStreamVar"));
    assertEquals(9, variables.size());

    // Update existing variable values again, and add a new variable
    runtimeService.setVariable(processInstance.getId(), "new var", "hi");
    runtimeService.setVariable(processInstance.getId(), "longVar", 9987L);
    runtimeService.setVariable(processInstance.getId(), "shortVar", (short) 456);
    runtimeService.setVariable(processInstance.getId(), "integerVar", 4567);
    runtimeService.setVariable(processInstance.getId(), "stringVar", "colgate");
    runtimeService.setVariable(processInstance.getId(), "dateVar", now);
    runtimeService.setVariable(processInstance.getId(), "serializableVar", serializable);
    runtimeService.setVariable(processInstance.getId(), "bytesVar", bytes);
    runtimeService.setVariable(processInstance.getId(), "byteStreamVar", new ByteArrayInputStream(streamBytes));

    variables = runtimeService.getVariables(processInstance.getId());
    assertEquals("hi", variables.get("new var"));
    assertEquals(9987L, variables.get("longVar"));
    assertEquals((short)456, variables.get("shortVar"));
    assertEquals(4567, variables.get("integerVar"));
    assertEquals("colgate", variables.get("stringVar"));
    assertEquals(now, variables.get("dateVar"));
    assertEquals(null, variables.get("nullVar"));
    assertEquals(serializable, variables.get("serializableVar"));
    assertTrue(Arrays.equals(bytes, (byte[]) variables.get("bytesVar")));
    assertTrue(Arrays.equals(streamBytes, (byte[]) variables.get("byteStreamVar")));
    assertEquals(10, variables.size());

    Collection<String> varFilter = new ArrayList<String>(2);
    varFilter.add("stringVar");
    varFilter.add("integerVar");

    Map<String, Object> filteredVariables = runtimeService.getVariables(processInstance.getId(), varFilter);
    assertEquals(2, filteredVariables.size());
    assertTrue(filteredVariables.containsKey("stringVar"));
    assertTrue(filteredVariables.containsKey("integerVar"));

    // Try setting the value of the variable that was initially created with value 'null'
    runtimeService.setVariable(processInstance.getId(), "nullVar", "a value");
    Object newValue = runtimeService.getVariable(processInstance.getId(), "nullVar");
    assertNotNull(newValue);
    assertEquals("a value", newValue);

    // Try setting the value of the serializableVar to an integer value
    runtimeService.setVariable(processInstance.getId(), "serializableVar", 100);
    variables = runtimeService.getVariables(processInstance.getId());
    assertEquals(100, variables.get("serializableVar"));

    // Try setting the value of the serializableVar back to a serializable value
    runtimeService.setVariable(processInstance.getId(), "serializableVar", serializable);
    variables = runtimeService.getVariables(processInstance.getId());
    assertEquals(serializable, variables.get("serializableVar"));
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/RuntimeServiceTest.testBasicVariableOperations.bpmn20.xml"})
  public void testOnlyChangeType() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aVariable", 1234);
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("taskAssigneeProcess", variables);

    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableName("aVariable");

    VariableInstance variable = query.singleResult();
    assertEquals(ValueType.INTEGER.getName(), variable.getTypeName());

    runtimeService.setVariable(pi.getId(), "aVariable", 1234L);
    variable = query.singleResult();
    assertEquals(ValueType.LONG.getName(), variable.getTypeName());

    runtimeService.setVariable(pi.getId(), "aVariable", (short)1234);
    variable = query.singleResult();
    assertEquals(ValueType.SHORT.getName(), variable.getTypeName());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/RuntimeServiceTest.testBasicVariableOperations.bpmn20.xml"})
  public void testChangeTypeFromSerializableUsingApi() {

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aVariable", new SerializableVariable("foo"));
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("taskAssigneeProcess", variables);

    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableName("aVariable");

    VariableInstance variable = query.singleResult();
    assertEquals(ValueType.OBJECT.getName(), variable.getTypeName());

    runtimeService.setVariable(pi.getId(), "aVariable", null);
    variable = query.singleResult();
    assertEquals(ValueType.NULL.getName(), variable.getTypeName());

  }

  @Deployment
  public void testChangeSerializableInsideEngine() {

    runtimeService.startProcessInstanceByKey("testProcess");

    Task task = taskService.createTaskQuery().singleResult();

    SerializableVariable var = (SerializableVariable) taskService.getVariable(task.getId(), "variableName");
    assertNotNull(var);

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/RuntimeServiceTest.testBasicVariableOperations.bpmn20.xml"})
  public void testChangeToSerializableUsingApi() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aVariable", "test");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskAssigneeProcess", variables);

    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableName("aVariable");

    VariableInstance variable = query.singleResult();
    assertEquals(ValueType.STRING.getName(), variable.getTypeName());

    runtimeService.setVariable(processInstance.getId(), "aVariable", new SerializableVariable("foo"));
    variable = query.singleResult();
    assertEquals(ValueType.OBJECT.getName(), variable.getTypeName());

  }

  @Deployment
  public void testGetVariableInstancesFromVariableScope() {

    VariableMap variables = createVariables()
      .putValue("anIntegerVariable", 1234)
      .putValue("anObjectValue", objectValue(new SimpleSerializableBean(10)).serializationDataFormat(Variables.SerializationDataFormats.JAVA))
      .putValue("anUntypedObjectValue", new SimpleSerializableBean(30));

    runtimeService.startProcessInstanceByKey("testProcess", variables);

    // assertions are part of the java delegate AssertVariableInstancesDelegate
    // only there we can access the VariableScope methods
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/RuntimeServiceTest.testSetVariableInScope.bpmn20.xml")
  public void testSetVariableInScopeExplicitUpdate() {
    // when a process instance is started and the task after the subprocess reached
    runtimeService.startProcessInstanceByKey("testProcess",
        Collections.<String, Object>singletonMap("shouldExplicitlyUpdateVariable", true));

    // then there should be only the "shouldExplicitlyUpdateVariable" variable
    VariableInstance variableInstance = runtimeService.createVariableInstanceQuery().singleResult();
    assertNotNull(variableInstance);
    assertEquals("shouldExplicitlyUpdateVariable", variableInstance.getName());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/RuntimeServiceTest.testSetVariableInScope.bpmn20.xml")
  public void testSetVariableInScopeImplicitUpdate() {
    // when a process instance is started and the task after the subprocess reached
    runtimeService.startProcessInstanceByKey("testProcess",
        Collections.<String, Object>singletonMap("shouldExplicitlyUpdateVariable", true));

    // then there should be only the "shouldExplicitlyUpdateVariable" variable
    VariableInstance variableInstance = runtimeService.createVariableInstanceQuery().singleResult();
    assertNotNull(variableInstance);
    assertEquals("shouldExplicitlyUpdateVariable", variableInstance.getName());
  }

  @Deployment
  public void testUpdateVariableInProcessWithoutWaitstate() {
    // when a process instance is started
    runtimeService.startProcessInstanceByKey("oneScriptTaskProcess",
        Collections.<String, Object>singletonMap("var", new SimpleSerializableBean(10)));

    // then it should succeeds successfully
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertNull(processInstance);
  }

  @Deployment
  public void testSetUpdateAndDeleteComplexVariable() {
    // when a process instance is started
    runtimeService.startProcessInstanceByKey("oneUserTaskProcess",
        Collections.<String, Object>singletonMap("var", new SimpleSerializableBean(10)));

    // then it should wait at the user task
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertNotNull(processInstance);
  }

  @Deployment
  public void testRollback() {
    try {
      runtimeService.startProcessInstanceByKey("RollbackProcess");

      fail("Starting the process instance should throw an exception");

    } catch (Exception e) {
      assertEquals("Buzzz", e.getMessage());
    }

    assertEquals(0, runtimeService.createExecutionQuery().count());
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/runtime/trivial.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/runtime/rollbackAfterSubProcess.bpmn20.xml"})
  public void testRollbackAfterSubProcess() {
    try {
      runtimeService.startProcessInstanceByKey("RollbackAfterSubProcess");

      fail("Starting the process instance should throw an exception");

    } catch (Exception e) {
      assertEquals("Buzzz", e.getMessage());
    }

    assertEquals(0, runtimeService.createExecutionQuery().count());

  }
}
