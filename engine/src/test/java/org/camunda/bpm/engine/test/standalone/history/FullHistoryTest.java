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
package org.camunda.bpm.engine.test.standalone.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricDetailQuery;
import org.camunda.bpm.engine.history.HistoricFormField;
import org.camunda.bpm.engine.history.HistoricFormProperty;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.history.HistoricVariableUpdate;
import org.camunda.bpm.engine.impl.cmd.SubmitStartFormCmd;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.runtime.DummySerializable;
import org.camunda.bpm.engine.test.api.runtime.util.CustomSerializable;
import org.camunda.bpm.engine.test.api.runtime.util.FailingSerializable;
import org.camunda.bpm.engine.test.history.SerializableVariable;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;


/**
 * @author Tom Baeyens
 * @author Frederik Heremans
 * @author Joram Barrez
 * @author Christian Lipphardt (camunda)
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class FullHistoryTest {

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testHelper);

  protected RuntimeService runtimeService;
  protected HistoryService historyService;
  protected TaskService taskService;
  protected FormService formService;
  protected RepositoryService repositoryService;
  protected CaseService caseService;

  @Before
  public void initServices() {
    runtimeService = engineRule.getRuntimeService();
    historyService = engineRule.getHistoryService();
    taskService = engineRule.getTaskService();
    formService = engineRule.getFormService();
    repositoryService = engineRule.getRepositoryService();
    caseService = engineRule.getCaseService();
  }

  @Test
  @Deployment
  public void testVariableUpdates() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("number", "one");
    variables.put("character", "a");
    variables.put("bytes", ":-(".getBytes());
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("receiveTask", variables);
    runtimeService.setVariable(processInstance.getId(), "number", "two");
    runtimeService.setVariable(processInstance.getId(), "bytes", ":-)".getBytes());

    // Start-task should be added to history
    HistoricActivityInstance historicStartEvent = historyService.createHistoricActivityInstanceQuery()
      .processInstanceId(processInstance.getId())
      .activityId("theStart")
      .singleResult();
    assertNotNull(historicStartEvent);

    HistoricActivityInstance waitStateActivity = historyService.createHistoricActivityInstanceQuery()
      .processInstanceId(processInstance.getId())
      .activityId("waitState")
      .singleResult();
    assertNotNull(waitStateActivity);

    HistoricActivityInstance serviceTaskActivity = historyService.createHistoricActivityInstanceQuery()
      .processInstanceId(processInstance.getId())
      .activityId("serviceTask")
      .singleResult();
    assertNotNull(serviceTaskActivity);

    List<HistoricDetail> historicDetails = historyService
      .createHistoricDetailQuery()
      .orderByVariableName().asc()
      .orderByVariableRevision().asc()
      .list();

    assertEquals(10, historicDetails.size());

    HistoricVariableUpdate historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(0);
    assertEquals("bytes", historicVariableUpdate.getVariableName());
    assertEquals(":-(", new String((byte[])historicVariableUpdate.getValue()));
    assertEquals(0, historicVariableUpdate.getRevision());
    assertEquals(processInstance.getProcessInstanceId(), historicVariableUpdate.getActivityInstanceId());

    // Variable is updated when process was in waitstate
    historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(1);
    assertEquals("bytes", historicVariableUpdate.getVariableName());
    assertEquals(":-)", new String((byte[])historicVariableUpdate.getValue()));
    assertEquals(1, historicVariableUpdate.getRevision());
    assertEquals(waitStateActivity.getId(), historicVariableUpdate.getActivityInstanceId());

    historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(2);
    assertEquals("character", historicVariableUpdate.getVariableName());
    assertEquals("a", historicVariableUpdate.getValue());
    assertEquals(0, historicVariableUpdate.getRevision());
    assertEquals(processInstance.getProcessInstanceId(), historicVariableUpdate.getActivityInstanceId());

    historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(3);
    assertEquals("number", historicVariableUpdate.getVariableName());
    assertEquals("one", historicVariableUpdate.getValue());
    assertEquals(0, historicVariableUpdate.getRevision());
    assertEquals(processInstance.getProcessInstanceId(), historicVariableUpdate.getActivityInstanceId());

    // Variable is updated when process was in waitstate
    historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(4);
    assertEquals("number", historicVariableUpdate.getVariableName());
    assertEquals("two", historicVariableUpdate.getValue());
    assertEquals(1, historicVariableUpdate.getRevision());
    assertEquals(waitStateActivity.getId(), historicVariableUpdate.getActivityInstanceId());

    // Variable set from process-start execution listener
    historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(5);
    assertEquals("zVar1", historicVariableUpdate.getVariableName());
    assertEquals("Event: start", historicVariableUpdate.getValue());
    assertEquals(0, historicVariableUpdate.getRevision());
    assertEquals(processInstance.getProcessInstanceId(), historicVariableUpdate.getActivityInstanceId());

    // Variable set from transition take execution listener
    historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(6);
    assertEquals("zVar2", historicVariableUpdate.getVariableName());
    assertEquals("Event: take", historicVariableUpdate.getValue());
    assertEquals(0, historicVariableUpdate.getRevision());
    assertNull(historicVariableUpdate.getActivityInstanceId());

    // Variable set from activity start execution listener on the servicetask
    historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(7);
    assertEquals("zVar3", historicVariableUpdate.getVariableName());
    assertEquals("Event: start", historicVariableUpdate.getValue());
    assertEquals(0, historicVariableUpdate.getRevision());
    assertEquals(serviceTaskActivity.getId(), historicVariableUpdate.getActivityInstanceId());

    // Variable set from activity end execution listener on the servicetask
    historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(8);
    assertEquals("zVar4", historicVariableUpdate.getVariableName());
    assertEquals("Event: end", historicVariableUpdate.getValue());
    assertEquals(0, historicVariableUpdate.getRevision());
    assertEquals(serviceTaskActivity.getId(), historicVariableUpdate.getActivityInstanceId());

    // Variable set from service-task
    historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(9);
    assertEquals("zzz", historicVariableUpdate.getVariableName());
    assertEquals(123456789L, historicVariableUpdate.getValue());
    assertEquals(0, historicVariableUpdate.getRevision());
    assertEquals(serviceTaskActivity.getId(), historicVariableUpdate.getActivityInstanceId());

    // trigger receive task
    runtimeService.signal(processInstance.getId());
    testHelper.assertProcessEnded(processInstance.getId());

    // check for historic process variables set
    HistoricVariableInstanceQuery historicProcessVariableQuery = historyService
            .createHistoricVariableInstanceQuery()
            .orderByVariableName().asc();

    assertEquals(8, historicProcessVariableQuery.count());

    List<HistoricVariableInstance> historicVariables = historicProcessVariableQuery.list();

    // Variable status when process is finished
    HistoricVariableInstance historicVariable = historicVariables.get(0);
    assertEquals("bytes", historicVariable.getVariableName());
    assertEquals(":-)", new String((byte[])historicVariable.getValue()));

    historicVariable = historicVariables.get(1);
    assertEquals("character", historicVariable.getVariableName());
    assertEquals("a", historicVariable.getValue());

    historicVariable = historicVariables.get(2);
    assertEquals("number", historicVariable.getVariableName());
    assertEquals("two", historicVariable.getValue());

    historicVariable = historicVariables.get(3);
    assertEquals("zVar1", historicVariable.getVariableName());
    assertEquals("Event: start", historicVariable.getValue());

    historicVariable = historicVariables.get(4);
    assertEquals("zVar2", historicVariable.getVariableName());
    assertEquals("Event: take", historicVariable.getValue());

    historicVariable = historicVariables.get(5);
    assertEquals("zVar3", historicVariable.getVariableName());
    assertEquals("Event: start", historicVariable.getValue());

    historicVariable = historicVariables.get(6);
    assertEquals("zVar4", historicVariable.getVariableName());
    assertEquals("Event: end", historicVariable.getValue());

    historicVariable = historicVariables.get(7);
    assertEquals("zzz", historicVariable.getVariableName());
    assertEquals(123456789L, historicVariable.getValue());
  }

  @Test
  @Deployment(resources="org/camunda/bpm/engine/test/standalone/history/FullHistoryTest.testVariableUpdates.bpmn20.xml")
  public void testHistoricVariableInstanceQuery() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("process", "one");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("receiveTask", variables);
    runtimeService.signal(processInstance.getProcessInstanceId());

    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableName("process").count());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableValueEquals("process", "one").count());

    Map<String, Object> variables2 = new HashMap<String, Object>();
    variables2.put("process", "two");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("receiveTask", variables2);
    runtimeService.signal(processInstance2.getProcessInstanceId());

    assertEquals(2, historyService.createHistoricVariableInstanceQuery().variableName("process").count());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableValueEquals("process", "one").count());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableValueEquals("process", "two").count());

    HistoricVariableInstance historicProcessVariable = historyService.createHistoricVariableInstanceQuery().variableValueEquals("process", "one").singleResult();
    assertEquals("process", historicProcessVariable.getVariableName());
    assertEquals("one", historicProcessVariable.getValue());
    assertEquals(ValueType.STRING.getName(), historicProcessVariable.getVariableTypeName());
    assertEquals(ValueType.STRING.getName(), historicProcessVariable.getTypeName());
    assertEquals(historicProcessVariable.getValue(), historicProcessVariable.getTypedValue().getValue());
    assertEquals(historicProcessVariable.getTypeName(), historicProcessVariable.getTypedValue().getType().getName());

    Map<String, Object> variables3 = new HashMap<String, Object>();
    variables3.put("long", 1000l);
    variables3.put("double", 25.43d);
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("receiveTask", variables3);
    runtimeService.signal(processInstance3.getProcessInstanceId());

    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableName("long").count());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableValueEquals("long", 1000l).count());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableName("double").count());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableValueEquals("double",  25.43d).count());

  }

  @Test
  @Deployment
  public void testHistoricVariableUpdatesAllTypes() throws Exception {

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss SSS");
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aVariable", "initial value");

    Date startedDate = sdf.parse("01/01/2001 01:23:45 000");

    // In the javaDelegate, the current time is manipulated
    Date updatedDate = sdf.parse("01/01/2001 01:23:46 000");

    ClockUtil.setCurrentTime(startedDate);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("HistoricVariableUpdateProcess", variables);

    List<HistoricDetail> details = historyService.createHistoricDetailQuery()
      .variableUpdates()
      .processInstanceId(processInstance.getId())
      .orderByVariableName().asc()
      .orderByTime().asc()
      .list();

    // 8 variable updates should be present, one performed when starting process
    // the other 7 are set in VariableSetter serviceTask
    assertEquals(9, details.size());

    // Since we order by varName, first entry should be aVariable update from startTask
    HistoricVariableUpdate startVarUpdate = (HistoricVariableUpdate) details.get(0);
    assertEquals("aVariable", startVarUpdate.getVariableName());
    assertEquals("initial value", startVarUpdate.getValue());
    assertEquals(0, startVarUpdate.getRevision());
    assertEquals(processInstance.getId(), startVarUpdate.getProcessInstanceId());
    // Date should the the one set when starting
    assertEquals(startedDate, startVarUpdate.getTime());

    HistoricVariableUpdate updatedStringVariable = (HistoricVariableUpdate) details.get(1);
    assertEquals("aVariable", updatedStringVariable.getVariableName());
    assertEquals("updated value", updatedStringVariable.getValue());
    assertEquals(processInstance.getId(), updatedStringVariable.getProcessInstanceId());
    // Date should be the updated date
    assertEquals(updatedDate, updatedStringVariable.getTime());

    HistoricVariableUpdate intVariable = (HistoricVariableUpdate) details.get(2);
    assertEquals("bVariable", intVariable.getVariableName());
    assertEquals(123, intVariable.getValue());
    assertEquals(processInstance.getId(), intVariable.getProcessInstanceId());
    assertEquals(updatedDate, intVariable.getTime());

    HistoricVariableUpdate longVariable = (HistoricVariableUpdate) details.get(3);
    assertEquals("cVariable", longVariable.getVariableName());
    assertEquals(12345L, longVariable.getValue());
    assertEquals(processInstance.getId(), longVariable.getProcessInstanceId());
    assertEquals(updatedDate, longVariable.getTime());

    HistoricVariableUpdate doubleVariable = (HistoricVariableUpdate) details.get(4);
    assertEquals("dVariable", doubleVariable.getVariableName());
    assertEquals(1234.567, doubleVariable.getValue());
    assertEquals(processInstance.getId(), doubleVariable.getProcessInstanceId());
    assertEquals(updatedDate, doubleVariable.getTime());

    HistoricVariableUpdate shortVariable = (HistoricVariableUpdate) details.get(5);
    assertEquals("eVariable", shortVariable.getVariableName());
    assertEquals((short)12, shortVariable.getValue());
    assertEquals(processInstance.getId(), shortVariable.getProcessInstanceId());
    assertEquals(updatedDate, shortVariable.getTime());

    HistoricVariableUpdate dateVariable = (HistoricVariableUpdate) details.get(6);
    assertEquals("fVariable", dateVariable.getVariableName());
    assertEquals(sdf.parse("01/01/2001 01:23:45 678"), dateVariable.getValue());
    assertEquals(processInstance.getId(), dateVariable.getProcessInstanceId());
    assertEquals(updatedDate, dateVariable.getTime());

    HistoricVariableUpdate serializableVariable = (HistoricVariableUpdate) details.get(7);
    assertEquals("gVariable", serializableVariable.getVariableName());
    assertEquals(new SerializableVariable("hello hello"), serializableVariable.getValue());
    assertEquals(processInstance.getId(), serializableVariable.getProcessInstanceId());
    assertEquals(updatedDate, serializableVariable.getTime());

    HistoricVariableUpdate byteArrayVariable = (HistoricVariableUpdate) details.get(8);
    assertEquals("hVariable", byteArrayVariable.getVariableName());
    assertEquals(";-)", new String((byte[])byteArrayVariable.getValue()));
    assertEquals(processInstance.getId(), byteArrayVariable.getProcessInstanceId());
    assertEquals(updatedDate, byteArrayVariable.getTime());

    // end process instance
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(1, tasks.size());
    taskService.complete(tasks.get(0).getId());
    testHelper.assertProcessEnded(processInstance.getId());

    // check for historic process variables set
    HistoricVariableInstanceQuery historicProcessVariableQuery = historyService
            .createHistoricVariableInstanceQuery()
            .orderByVariableName().asc();

    assertEquals(8, historicProcessVariableQuery.count());

    List<HistoricVariableInstance> historicVariables = historicProcessVariableQuery.list();

 // Variable status when process is finished
    HistoricVariableInstance historicVariable = historicVariables.get(0);
    assertEquals("aVariable", historicVariable.getVariableName());
    assertEquals("updated value", historicVariable.getValue());
    assertEquals(processInstance.getId(), historicVariable.getProcessInstanceId());

    historicVariable = historicVariables.get(1);
    assertEquals("bVariable", historicVariable.getVariableName());
    assertEquals(123, historicVariable.getValue());
    assertEquals(processInstance.getId(), historicVariable.getProcessInstanceId());

    historicVariable = historicVariables.get(2);
    assertEquals("cVariable", historicVariable.getVariableName());
    assertEquals(12345L, historicVariable.getValue());
    assertEquals(processInstance.getId(), historicVariable.getProcessInstanceId());

    historicVariable = historicVariables.get(3);
    assertEquals("dVariable", historicVariable.getVariableName());
    assertEquals(1234.567, historicVariable.getValue());
    assertEquals(processInstance.getId(), historicVariable.getProcessInstanceId());

    historicVariable = historicVariables.get(4);
    assertEquals("eVariable", historicVariable.getVariableName());
    assertEquals((short) 12, historicVariable.getValue());
    assertEquals(processInstance.getId(), historicVariable.getProcessInstanceId());

    historicVariable = historicVariables.get(5);
    assertEquals("fVariable", historicVariable.getVariableName());
    assertEquals(sdf.parse("01/01/2001 01:23:45 678"), historicVariable.getValue());
    assertEquals(processInstance.getId(), historicVariable.getProcessInstanceId());

    historicVariable = historicVariables.get(6);
    assertEquals("gVariable", historicVariable.getVariableName());
    assertEquals(new SerializableVariable("hello hello"), historicVariable.getValue());
    assertEquals(processInstance.getId(), historicVariable.getProcessInstanceId());

    historicVariable = historicVariables.get(7);
    assertEquals("hVariable", historicVariable.getVariableName());
    assertEquals(";-)", ";-)", new String((byte[])historicVariable.getValue()));
    assertEquals(processInstance.getId(), historicVariable.getProcessInstanceId());
  }

  @Test
  @Deployment
  public void testHistoricFormProperties() throws Exception {
    Date startedDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss SSS").parse("01/01/2001 01:23:46 000");

    ClockUtil.setCurrentTime(startedDate);

    Map<String, String> formProperties = new HashMap<String, String>();
    formProperties.put("formProp1", "Activiti rocks");
    formProperties.put("formProp2", "12345");

    ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery().processDefinitionKey("historicFormPropertiesProcess").singleResult();

    ProcessInstance processInstance = formService.submitStartFormData(procDef.getId() , formProperties);

    // Submit form-properties on the created task
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(task);

    // Out execution only has a single activity waiting, the task
    List<String> activityIds = runtimeService.getActiveActivityIds(task.getExecutionId());
    assertNotNull(activityIds);
    assertEquals(1, activityIds.size());

    String taskActivityId = activityIds.get(0);

    // Submit form properties
    formProperties = new HashMap<String, String>();
    formProperties.put("formProp3", "Activiti still rocks!!!");
    formProperties.put("formProp4", "54321");
    formService.submitTaskFormData(task.getId(), formProperties);

    // 4 historic form properties should be created. 2 when process started, 2 when task completed
    List<HistoricDetail> props = historyService.createHistoricDetailQuery()
      .formProperties()
      .processInstanceId(processInstance.getId())
      .orderByFormPropertyId().asc()
      .list();

    HistoricFormProperty historicProperty1 = (HistoricFormProperty) props.get(0);
    assertEquals("formProp1", historicProperty1.getPropertyId());
    assertEquals("Activiti rocks", historicProperty1.getPropertyValue());
    assertEquals(startedDate, historicProperty1.getTime());
    assertEquals(processInstance.getId(), historicProperty1.getProcessInstanceId());
    assertNull(historicProperty1.getTaskId());

    assertEquals(processInstance.getId(), historicProperty1.getActivityInstanceId());

    HistoricFormProperty historicProperty2 = (HistoricFormProperty) props.get(1);
    assertEquals("formProp2", historicProperty2.getPropertyId());
    assertEquals("12345", historicProperty2.getPropertyValue());
    assertEquals(startedDate, historicProperty2.getTime());
    assertEquals(processInstance.getId(), historicProperty2.getProcessInstanceId());
    assertNull(historicProperty2.getTaskId());

    assertEquals(processInstance.getId(), historicProperty2.getActivityInstanceId());

    HistoricFormProperty historicProperty3 = (HistoricFormProperty) props.get(2);
    assertEquals("formProp3", historicProperty3.getPropertyId());
    assertEquals("Activiti still rocks!!!", historicProperty3.getPropertyValue());
    assertEquals(startedDate, historicProperty3.getTime());
    assertEquals(processInstance.getId(), historicProperty3.getProcessInstanceId());
    String activityInstanceId = historicProperty3.getActivityInstanceId();
    HistoricActivityInstance historicActivityInstance = historyService.createHistoricActivityInstanceQuery().activityInstanceId(activityInstanceId).singleResult();
    assertNotNull(historicActivityInstance);
    assertEquals(taskActivityId, historicActivityInstance.getActivityId());
    assertNotNull(historicProperty3.getTaskId());

    HistoricFormProperty historicProperty4 = (HistoricFormProperty) props.get(3);
    assertEquals("formProp4", historicProperty4.getPropertyId());
    assertEquals("54321", historicProperty4.getPropertyValue());
    assertEquals(startedDate, historicProperty4.getTime());
    assertEquals(processInstance.getId(), historicProperty4.getProcessInstanceId());
    activityInstanceId = historicProperty4.getActivityInstanceId();
    historicActivityInstance = historyService.createHistoricActivityInstanceQuery().activityInstanceId(activityInstanceId).singleResult();
    assertNotNull(historicActivityInstance);
    assertEquals(taskActivityId, historicActivityInstance.getActivityId());
    assertNotNull(historicProperty4.getTaskId());

    assertEquals(4, props.size());
  }

  @Test
  @Deployment(
    resources={"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testHistoricVariableQuery() throws Exception {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("stringVar", "activiti rocks!");
    variables.put("longVar", 12345L);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);

    // Query on activity-instance, activity instance null will return all vars set when starting process
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().activityInstanceId(null).count());
    assertEquals(0, historyService.createHistoricDetailQuery().variableUpdates().activityInstanceId("unexisting").count());

    // Query on process-instance
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().processInstanceId(processInstance.getId()).count());
    assertEquals(0, historyService.createHistoricDetailQuery().variableUpdates().processInstanceId("unexisting").count());

    // Query both process-instance and activity-instance
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates()
            .activityInstanceId(null)
            .processInstanceId(processInstance.getId()).count());

    // end process instance
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(1, tasks.size());
    taskService.complete(tasks.get(0).getId());
    testHelper.assertProcessEnded(processInstance.getId());

    assertEquals(2, historyService.createHistoricVariableInstanceQuery().count());

    // Query on process-instance
    assertEquals(2, historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).count());
    assertEquals(0, historyService.createHistoricVariableInstanceQuery().processInstanceId("unexisting").count());
  }

  @Test
  @Deployment(
    resources={"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testHistoricVariableQueryExcludeTaskRelatedDetails() throws Exception {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("stringVar", "activiti rocks!");
    variables.put("longVar", 12345L);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);

    // Set a local task-variable
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(task);
    taskService.setVariableLocal(task.getId(), "taskVar", "It is I, le Variable");

    // Query on process-instance
    assertEquals(3, historyService.createHistoricDetailQuery().variableUpdates().processInstanceId(processInstance.getId()).count());

    // Query on process-instance, excluding task-details
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().processInstanceId(processInstance.getId())
      .excludeTaskDetails().count());

    // Check task-id precedence on excluding task-details
    assertEquals(1, historyService.createHistoricDetailQuery().variableUpdates().processInstanceId(processInstance.getId())
            .excludeTaskDetails().taskId(task.getId()).count());
  }

  @Test
  @Deployment(
    resources={"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testHistoricFormPropertiesQuery() throws Exception {
    Map<String, String> formProperties = new HashMap<String, String>();
    formProperties.put("stringVar", "activiti rocks!");
    formProperties.put("longVar", "12345");

    ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess").singleResult();
    ProcessInstance processInstance = formService.submitStartFormData(procDef.getId() , formProperties);

    // Query on activity-instance, activity instance null will return all vars set when starting process
    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().activityInstanceId(null).count());
    assertEquals(0, historyService.createHistoricDetailQuery().formProperties().activityInstanceId("unexisting").count());

    // Query on process-instance
    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().processInstanceId(processInstance.getId()).count());
    assertEquals(0, historyService.createHistoricDetailQuery().formProperties().processInstanceId("unexisting").count());

    // Complete the task by submitting the task properties
    Task task = taskService.createTaskQuery().singleResult();
    formProperties = new HashMap<String, String>();
    formProperties.put("taskVar", "task form property");
    formService.submitTaskFormData(task.getId(), formProperties);

    assertEquals(3, historyService.createHistoricDetailQuery().formProperties().processInstanceId(processInstance.getId()).count());
    assertEquals(0, historyService.createHistoricDetailQuery().formProperties().processInstanceId("unexisting").count());
  }

  @Test
  @Deployment(
    resources={"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testHistoricVariableQuerySorting() throws Exception {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("stringVar", "activiti rocks!");
    variables.put("longVar", 12345L);

    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);

    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByProcessInstanceId().asc().count());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByTime().asc().count());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByVariableName().asc().count());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByVariableRevision().asc().count());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByVariableType().asc().count());

    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByProcessInstanceId().desc().count());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByTime().desc().count());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByVariableName().desc().count());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByVariableRevision().desc().count());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByVariableType().desc().count());

    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByProcessInstanceId().asc().list().size());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByTime().asc().list().size());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByVariableName().asc().list().size());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByVariableRevision().asc().list().size());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByVariableType().asc().list().size());

    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByProcessInstanceId().desc().list().size());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByTime().desc().list().size());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByVariableName().desc().list().size());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByVariableRevision().desc().list().size());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByVariableType().desc().list().size());
  }

  @Test
  @Deployment(
    resources={"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testHistoricFormPropertySorting() throws Exception {

    Map<String, String> formProperties = new HashMap<String, String>();
    formProperties.put("stringVar", "activiti rocks!");
    formProperties.put("longVar", "12345");

    ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess").singleResult();
    formService.submitStartFormData(procDef.getId() , formProperties);

    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().orderByProcessInstanceId().asc().count());
    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().orderByTime().asc().count());
    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().orderByFormPropertyId().asc().count());

    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().orderByProcessInstanceId().desc().count());
    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().orderByTime().desc().count());
    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().orderByFormPropertyId().desc().count());

    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().orderByProcessInstanceId().asc().list().size());
    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().orderByTime().asc().list().size());
    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().orderByFormPropertyId().asc().list().size());

    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().orderByProcessInstanceId().desc().list().size());
    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().orderByTime().desc().list().size());
    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().orderByFormPropertyId().desc().list().size());
  }

  @Test
  @Deployment
  public void testHistoricDetailQueryMixed() throws Exception {

    Map<String, String> formProperties = new HashMap<String, String>();
    formProperties.put("formProp1", "activiti rocks!");
    formProperties.put("formProp2", "12345");

    ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery().processDefinitionKey("historicDetailMixed").singleResult();
    ProcessInstance processInstance = formService.submitStartFormData(procDef.getId() , formProperties);

    List<HistoricDetail> details = historyService
      .createHistoricDetailQuery()
      .processInstanceId(processInstance.getId())
      .orderByVariableName().asc()
      .list();

    assertEquals(4, details.size());

    assertTrue(details.get(0) instanceof HistoricFormProperty);
    HistoricFormProperty formProp1 = (HistoricFormProperty) details.get(0);
    assertEquals("formProp1", formProp1.getPropertyId());
    assertEquals("activiti rocks!", formProp1.getPropertyValue());

    assertTrue(details.get(1) instanceof HistoricFormProperty);
    HistoricFormProperty formProp2 = (HistoricFormProperty) details.get(1);
    assertEquals("formProp2", formProp2.getPropertyId());
    assertEquals("12345", formProp2.getPropertyValue());


    assertTrue(details.get(2) instanceof HistoricVariableUpdate);
    HistoricVariableUpdate varUpdate1 = (HistoricVariableUpdate) details.get(2);
    assertEquals("variable1", varUpdate1.getVariableName());
    assertEquals("activiti rocks!", varUpdate1.getValue());


    // This variable should be of type LONG since this is defined in the process-definition
    assertTrue(details.get(3) instanceof HistoricVariableUpdate);
    HistoricVariableUpdate varUpdate2 = (HistoricVariableUpdate) details.get(3);
    assertEquals("variable2", varUpdate2.getVariableName());
    assertEquals(12345L, varUpdate2.getValue());
  }

  @Test
  public void testHistoricDetailQueryInvalidSorting() throws Exception {
    try {
      historyService.createHistoricDetailQuery().asc().list();
      fail();
    } catch (ProcessEngineException e) {

    }

    try {
      historyService.createHistoricDetailQuery().desc().list();
      fail();
    } catch (ProcessEngineException e) {

    }

    try {
      historyService.createHistoricDetailQuery().orderByProcessInstanceId().list();
      fail();
    } catch (ProcessEngineException e) {

    }

    try {
      historyService.createHistoricDetailQuery().orderByTime().list();
      fail();
    } catch (ProcessEngineException e) {

    }

    try {
      historyService.createHistoricDetailQuery().orderByVariableName().list();
      fail();
    } catch (ProcessEngineException e) {

    }

    try {
      historyService.createHistoricDetailQuery().orderByVariableRevision().list();
      fail();
    } catch (ProcessEngineException e) {

    }

    try {
      historyService.createHistoricDetailQuery().orderByVariableType().list();
      fail();
    } catch (ProcessEngineException e) {

    }
  }

  @Test
  @Deployment
  public void testHistoricTaskInstanceVariableUpdates() {
    String processInstanceId = runtimeService.startProcessInstanceByKey("HistoricTaskInstanceTest").getId();

    String taskId = taskService.createTaskQuery().singleResult().getId();

    runtimeService.setVariable(processInstanceId, "deadline", "yesterday");

    taskService.setVariableLocal(taskId, "bucket", "23c");
    taskService.setVariableLocal(taskId, "mop", "37i");

    taskService.complete(taskId);

    assertEquals(1, historyService.createHistoricTaskInstanceQuery().count());

    List<HistoricDetail> historicTaskVariableUpdates = historyService.createHistoricDetailQuery()
      .taskId(taskId)
      .variableUpdates()
      .orderByVariableName().asc()
      .list();

    assertEquals(2, historicTaskVariableUpdates.size());

    historyService.deleteHistoricTaskInstance(taskId);

    // Check if the variable updates have been removed as well
    historicTaskVariableUpdates = historyService.createHistoricDetailQuery()
      .taskId(taskId)
      .variableUpdates()
      .orderByVariableName().asc()
      .list();

     assertEquals(0, historicTaskVariableUpdates.size());
  }

  // ACT-592
  @Test
  @Deployment
  public void testSetVariableOnProcessInstanceWithTimer() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("timerVariablesProcess");
    runtimeService.setVariable(processInstance.getId(), "myVar", 123456L);
    assertEquals(123456L, runtimeService.getVariable(processInstance.getId(), "myVar"));
  }

  @Test
  @Deployment
  public void testDeleteHistoricProcessInstance() {
    // Start process-instance with some variables set
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("processVar", 123L);
    vars.put("anotherProcessVar", new DummySerializable());

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("HistoricTaskInstanceTest", vars);
    assertNotNull(processInstance);

    // Set 2 task properties
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.setVariableLocal(task.getId(), "taskVar", 45678);
    taskService.setVariableLocal(task.getId(), "anotherTaskVar", "value");

    // Finish the task, this end the process-instance
    taskService.complete(task.getId());

    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).count());
    assertEquals(3, historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstance.getId()).count());
    assertEquals(4, historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).count());
    assertEquals(4, historyService.createHistoricDetailQuery().processInstanceId(processInstance.getId()).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstance.getId()).count());

    // Delete the historic process-instance
    historyService.deleteHistoricProcessInstance(processInstance.getId());

    // Verify no traces are left in the history tables
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).count());
    assertEquals(0, historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstance.getId()).count());
    assertEquals(0, historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).count());
    assertEquals(0, historyService.createHistoricDetailQuery().processInstanceId(processInstance.getId()).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstance.getId()).count());

    try {
      // Delete the historic process-instance, which is still running
      historyService.deleteHistoricProcessInstance("unexisting");
      fail("Exception expected when deleting process-instance that is still running");
    } catch(ProcessEngineException ae) {
      // Expected exception
      assertThat(ae.getMessage()).contains("No historic process instance found with id: unexisting");
    }
  }

  @Test
  @Deployment
  public void testDeleteRunningHistoricProcessInstance() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("HistoricTaskInstanceTest");
    assertNotNull(processInstance);

    try {
      // Delete the historic process-instance, which is still running
      historyService.deleteHistoricProcessInstance(processInstance.getId());
      fail("Exception expected when deleting process-instance that is still running");
    } catch(ProcessEngineException ae) {
      // Expected exception
      assertThat(ae.getMessage()).contains("Process instance is still running, cannot delete historic process instance");
    }
  }

  @Test
  @Deployment
  public void testDeleteCachedHistoricDetails() {
    final String processDefinitionId = repositoryService.createProcessDefinitionQuery().singleResult().getId();


    engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired().execute(new Command<Void>() {

      public Void execute(CommandContext commandContext) {
        Map<String, Object> formProperties = new HashMap<String, Object>();
        formProperties.put("formProp1", "value1");

        ProcessInstance processInstance = new SubmitStartFormCmd(processDefinitionId, null, formProperties).execute(commandContext);

        // two historic details should be in cache: one form property and one variable update
        commandContext.getHistoricDetailManager().deleteHistoricDetailsByProcessInstanceIds(Arrays.asList(processInstance.getId()));
        return null;
      }
    });

    // the historic process instance should still be there
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().count());

    // the historic details should be deleted
    assertEquals(0, historyService.createHistoricDetailQuery().count());
  }

  /**
   * Test created to validate ACT-621 fix.
   */
  @Test
  @Deployment
  public void testHistoricFormPropertiesOnReEnteringActivity() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("comeBack", Boolean.TRUE);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("HistoricFormPropertiesProcess", variables);
    assertNotNull(processInstance);

    // Submit form on task
    Map<String, String> data = new HashMap<String, String>();
    data.put("formProp1", "Property value");

    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    formService.submitTaskFormData(task.getId(), data);

    // Historic property should be available
    List<HistoricDetail> details = historyService.createHistoricDetailQuery()
      .formProperties()
      .processInstanceId(processInstance.getId())
      .list();
    assertNotNull(details);
    assertEquals(1, details.size());

    // Task should be active in the same activity as the previous one
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    formService.submitTaskFormData(task.getId(), data);

    details = historyService.createHistoricDetailQuery()
      .formProperties()
      .processInstanceId(processInstance.getId())
      .list();
    assertNotNull(details);
    assertEquals(2, details.size());

    // Should have 2 different historic activity instance ID's, with the same activityId
    Assert.assertNotSame(details.get(0).getActivityInstanceId(), details.get(1).getActivityInstanceId());

    HistoricActivityInstance historicActInst1 = historyService.createHistoricActivityInstanceQuery()
      .activityInstanceId(details.get(0).getActivityInstanceId())
      .singleResult();

    HistoricActivityInstance historicActInst2 = historyService.createHistoricActivityInstanceQuery()
      .activityInstanceId(details.get(1).getActivityInstanceId())
      .singleResult();

    assertEquals(historicActInst1.getActivityId(), historicActInst2.getActivityId());
  }

  @Test
  @Deployment
  public void testHistoricTaskInstanceQueryTaskVariableValueEquals() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("HistoricTaskInstanceTest");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    // Set some variables on the task
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("longVar", 12345L);
    variables.put("shortVar", (short) 123);
    variables.put("integerVar", 1234);
    variables.put("stringVar", "stringValue");
    variables.put("booleanVar", true);
    Date date = Calendar.getInstance().getTime();
    variables.put("dateVar", date);
    variables.put("nullVar", null);

    taskService.setVariablesLocal(task.getId(), variables);

    // Validate all variable-updates are present in DB
    assertEquals(7, historyService.createHistoricDetailQuery().variableUpdates().taskId(task.getId()).count());

    // Query Historic task instances based on variable
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("longVar", 12345L).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("shortVar", (short) 123).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("integerVar",1234).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("stringVar","stringValue").count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("booleanVar", true).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("dateVar", date).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("nullVar", null).count());

    // Update the variables
    variables.put("longVar", 67890L);
    variables.put("shortVar", (short) 456);
    variables.put("integerVar", 5678);
    variables.put("stringVar", "updatedStringValue");
    variables.put("booleanVar", false);
    Calendar otherCal = Calendar.getInstance();
    otherCal.add(Calendar.DAY_OF_MONTH, 1);
    Date otherDate = otherCal.getTime();
    variables.put("dateVar", otherDate);
    variables.put("nullVar", null);

    taskService.setVariablesLocal(task.getId(), variables);

    // Validate all variable-updates are present in DB
    assertEquals(14, historyService.createHistoricDetailQuery().variableUpdates().taskId(task.getId()).count());

    // Previous values should NOT match
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("longVar", 12345L).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("shortVar", (short) 123).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("integerVar",1234).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("stringVar","stringValue").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("booleanVar", true).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("dateVar", date).count());

    // New values should match
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("longVar", 67890L).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("shortVar", (short) 456).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("integerVar",5678).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("stringVar","updatedStringValue").count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("booleanVar", false).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("dateVar", otherDate).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("nullVar", null).count());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testHistoricTaskInstanceQueryTaskVariableValueEqualsOverwriteType() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    // Set a long variable on a task
    taskService.setVariableLocal(task.getId(), "var", 12345L);

    // Validate all variable-updates are present in DB
    assertEquals(1, historyService.createHistoricDetailQuery().variableUpdates().taskId(task.getId()).count());

    // Query Historic task instances based on variable
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("var", 12345L).count());

    // Update the variables to an int variable
    taskService.setVariableLocal(task.getId(), "var", 12345);

    // Validate all variable-updates are present in DB
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().taskId(task.getId()).count());

    // The previous long value should not match
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("var", 12345L).count());

    // The previous int value should not match
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("var", 12345).count());
  }

  @Test
  @Deployment
  public void testHistoricTaskInstanceQueryVariableInParallelBranch() throws Exception {
    runtimeService.startProcessInstanceByKey("parallelGateway");

    // when there are two process variables of the same name but different types
    Execution task1Execution = runtimeService.createExecutionQuery().activityId("task1").singleResult();
    runtimeService.setVariableLocal(task1Execution.getId(), "var", 12345L);
    Execution task2Execution = runtimeService.createExecutionQuery().activityId("task2").singleResult();
    runtimeService.setVariableLocal(task2Execution.getId(), "var", 12345);

    // then the task query should be able to filter by both variables and return both tasks
    assertEquals(2, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("var", 12345).count());
    assertEquals(2, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("var", 12345L).count());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/standalone/history/FullHistoryTest.testHistoricTaskInstanceQueryVariableInParallelBranch.bpmn20.xml")
  public void testHistoricTaskInstanceQueryVariableOfSameTypeInParallelBranch() throws Exception {
    runtimeService.startProcessInstanceByKey("parallelGateway");

    // when there are two process variables of the same name but different types
    Execution task1Execution = runtimeService.createExecutionQuery().activityId("task1").singleResult();
    runtimeService.setVariableLocal(task1Execution.getId(), "var", 12345L);
    Execution task2Execution = runtimeService.createExecutionQuery().activityId("task2").singleResult();
    runtimeService.setVariableLocal(task2Execution.getId(), "var", 45678L);

    // then the task query should be able to filter by both variables and return both tasks
    assertEquals(2, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("var", 12345L).count());
    assertEquals(2, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("var", 45678L).count());
  }

  @Test
  @Deployment
  public void testHistoricTaskInstanceQueryProcessVariableValueEquals() throws Exception {
    // Set some variables on the process instance
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("longVar", 12345L);
    variables.put("shortVar", (short) 123);
    variables.put("integerVar", 1234);
    variables.put("stringVar", "stringValue");
    variables.put("booleanVar", true);
    Date date = Calendar.getInstance().getTime();
    variables.put("dateVar", date);
    variables.put("nullVar", null);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("HistoricTaskInstanceTest", variables);
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    // Validate all variable-updates are present in DB
    assertEquals(7, historyService.createHistoricDetailQuery().variableUpdates().processInstanceId(processInstance.getId()).count());

    // Query Historic task instances based on process variable
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("longVar", 12345L).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("shortVar", (short) 123).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("integerVar",1234).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("stringVar","stringValue").count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("booleanVar", true).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("dateVar", date).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("nullVar", null).count());

    // Update the variables
    variables.put("longVar", 67890L);
    variables.put("shortVar", (short) 456);
    variables.put("integerVar", 5678);
    variables.put("stringVar", "updatedStringValue");
    variables.put("booleanVar", false);
    Calendar otherCal = Calendar.getInstance();
    otherCal.add(Calendar.DAY_OF_MONTH, 1);
    Date otherDate = otherCal.getTime();
    variables.put("dateVar", otherDate);
    variables.put("nullVar", null);

    runtimeService.setVariables(processInstance.getId(), variables);

    // Validate all variable-updates are present in DB
    assertEquals(14, historyService.createHistoricDetailQuery().variableUpdates().processInstanceId(processInstance.getId()).count());

    // Previous values should NOT match
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("longVar", 12345L).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("shortVar", (short) 123).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("integerVar",1234).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("stringVar","stringValue").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("booleanVar", true).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("dateVar", date).count());

    // New values should match
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("longVar", 67890L).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("shortVar", (short) 456).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("integerVar",5678).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("stringVar","updatedStringValue").count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("booleanVar", false).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("dateVar", otherDate).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("nullVar", null).count());

    // Set a task-variables, shouldn't affect the process-variable matches
    taskService.setVariableLocal(task.getId(), "longVar", 9999L);
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("longVar", 9999L).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("longVar", 67890L).count());
  }

  @Test
  @Deployment
  public void testHistoricProcessInstanceVariableValueEquals() throws Exception {
    // Set some variables on the process instance
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("longVar", 12345L);
    variables.put("shortVar", (short) 123);
    variables.put("integerVar", 1234);
    variables.put("stringVar", "stringValue");
    variables.put("booleanVar", true);
    Date date = Calendar.getInstance().getTime();
    variables.put("dateVar", date);
    variables.put("nullVar", null);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("HistoricProcessInstanceTest", variables);

    // Validate all variable-updates are present in DB
    assertEquals(7, historyService.createHistoricDetailQuery().variableUpdates().processInstanceId(processInstance.getId()).count());

  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/standalone/history/FullHistoryTest.testHistoricProcessInstanceVariableValueEquals.bpmn20.xml"})
  public void testHistoricProcessInstanceVariableValueNotEquals() throws Exception {
    // Set some variables on the process instance
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("longVar", 12345L);
    variables.put("shortVar", (short) 123);
    variables.put("integerVar", 1234);
    variables.put("stringVar", "stringValue");
    variables.put("booleanVar", true);
    Date date = Calendar.getInstance().getTime();
    Calendar otherCal = Calendar.getInstance();
    otherCal.add(Calendar.DAY_OF_MONTH, 1);
    Date otherDate = otherCal.getTime();
    variables.put("dateVar", date);
    variables.put("nullVar", null);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("HistoricProcessInstanceTest", variables);

    // Validate all variable-updates are present in DB
    assertEquals(7, historyService.createHistoricDetailQuery().variableUpdates().processInstanceId(processInstance.getId()).count());

    // Query Historic process instances based on process variable, shouldn't match
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("longVar", 12345L).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("shortVar", (short) 123).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("integerVar",1234).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("stringVar","stringValue").count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("booleanVar", true).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("dateVar", date).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("nullVar", null).count());

  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/standalone/history/FullHistoryTest.testHistoricProcessInstanceVariableValueEquals.bpmn20.xml"})
  public void testHistoricProcessInstanceVariableValueLessThanAndGreaterThan() throws Exception {
    // Set some variables on the process instance
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("longVar", 12345L);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("HistoricProcessInstanceTest", variables);

    // Validate all variable-updates are present in DB
    assertEquals(1, historyService.createHistoricDetailQuery().variableUpdates().processInstanceId(processInstance.getId()).count());

    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueGreaterThan("longVar", 12345L).count());
  }

  @Test
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
    testHelper.assertProcessEnded(pi.getId());

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

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testHistoricDetailQueryByVariableInstanceId() throws Exception {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("testVar", "testValue");
    runtimeService.startProcessInstanceByKey("oneTaskProcess", params);

    HistoricVariableInstance testVariable = historyService.createHistoricVariableInstanceQuery()
      .variableName("testVar")
      .singleResult();

    HistoricDetailQuery query = historyService.createHistoricDetailQuery();

    query.variableInstanceId(testVariable.getId());

    assertEquals(1, query.count());
    assertEquals(1, query.list().size());
  }

  @Test
  public void testHistoricDetailQueryByInvalidVariableInstanceId() {
    HistoricDetailQuery query = historyService.createHistoricDetailQuery();

    query.variableInstanceId("invalid");
    assertEquals(0, query.count());

    try {
      query.variableInstanceId(null);
      fail("A ProcessEngineExcpetion was expected.");
    } catch (ProcessEngineException e) {}

    try {
      query.variableInstanceId((String)null);
      fail("A ProcessEngineExcpetion was expected.");
    } catch (ProcessEngineException e) {}
  }

  @Test
  @Deployment
  public void testHistoricDetailActivityInstanceIdForInactiveScopeExecution() {

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    runtimeService.setVariable(pi.getId(), "foo", "bar");

    HistoricDetail historicDetail = historyService.createHistoricDetailQuery().singleResult();
    assertNotNull(historicDetail.getActivityInstanceId());
    assertNotEquals(pi.getId(), historicDetail.getActivityInstanceId());
  }

  @Test
  @Deployment
  public void testHistoricDetailActivityInstanceIdForInactiveScopeExecutionAsyncBefore() {

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    runtimeService.setVariable(pi.getId(), "foo", "bar");

    HistoricDetail historicDetail = historyService.createHistoricDetailQuery().singleResult();
    assertNotNull(historicDetail.getActivityInstanceId());
    assertNotEquals(pi.getId(), historicDetail.getActivityInstanceId());
  }

  @Test
  public void testHistoricDetailQueryById() {

    Task newTask = taskService.newTask();
    taskService.saveTask(newTask);

    String variableName = "someName";
    String variableValue = "someValue";
    taskService.setVariable(newTask.getId(), variableName, variableValue);

    HistoricDetail result = historyService.createHistoricDetailQuery()
      .singleResult();

    HistoricDetail resultById = historyService.createHistoricDetailQuery().detailId(result.getId()).singleResult();
    assertNotNull(resultById);
    assertEquals(result.getId(), resultById.getId());
    assertEquals(variableName, ((HistoricVariableUpdate)resultById).getVariableName());
    assertEquals(variableValue, ((HistoricVariableUpdate)resultById).getValue());
    assertEquals(ValueType.STRING.getName(), ((HistoricVariableUpdate)resultById).getVariableTypeName());
    assertEquals(ValueType.STRING.getName(), ((HistoricVariableUpdate)resultById).getTypeName());

    taskService.deleteTask(newTask.getId(), true);
  }

  @Test
  public void testHistoricDetailQueryByNonExistingId() {

    Task newTask = taskService.newTask();
    taskService.saveTask(newTask);

    String variableName = "someName";
    String variableValue = "someValue";
    taskService.setVariable(newTask.getId(), variableName, variableValue);

    HistoricDetail result = historyService.createHistoricDetailQuery().detailId("non-existing").singleResult();
    assertNull(result);

    taskService.deleteTask(newTask.getId(), true);
  }

  @Test
  public void testBinaryFetchingEnabled() {

    // by default, binary fetching is enabled

    Task newTask = taskService.newTask();
    taskService.saveTask(newTask);

    String variableName = "binaryVariableName";
    taskService.setVariable(newTask.getId(), variableName, "some bytes".getBytes());

    HistoricDetail result = historyService.createHistoricDetailQuery()
      .variableUpdates()
      .singleResult();

    assertNotNull(((HistoricVariableUpdate)result).getValue());

    taskService.deleteTask(newTask.getId(), true);
  }

  @Test
  public void testBinaryFetchingDisabled() {

    Task newTask = taskService.newTask();
    taskService.saveTask(newTask);

    String variableName = "binaryVariableName";
    taskService.setVariable(newTask.getId(), variableName, "some bytes".getBytes());

    HistoricDetail result = historyService.createHistoricDetailQuery()
      .disableBinaryFetching()
      .variableUpdates()
      .singleResult();

    assertNull(((HistoricVariableUpdate)result).getValue());

    taskService.deleteTask(newTask.getId(), true);
  }

  @Test
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
    HistoricVariableUpdate fileVariableInstance =
        (HistoricVariableUpdate) historyService.createHistoricDetailQuery().singleResult();

    // then the binary value is accessible
    assertNotNull(fileVariableInstance.getValue());

    // when disabling binary fetching
    fileVariableInstance =
        (HistoricVariableUpdate) historyService.createHistoricDetailQuery().disableBinaryFetching().singleResult();

    // then the byte value is not fetched
    assertNotNull(fileVariableInstance);
    assertEquals("fileVar", fileVariableInstance.getVariableName());

    assertNull(fileVariableInstance.getValue());

    FileValue typedValue = (FileValue) fileVariableInstance.getTypedValue();
    assertNull(typedValue.getValue());

    // but typed value metadata is accessible
    assertEquals(ValueType.FILE, typedValue.getType());
    assertEquals(fileName, typedValue.getFilename());
    assertEquals(encoding, typedValue.getEncoding());
    assertEquals(mimeType, typedValue.getMimeType());

  }

  @Test
  public void testDisableCustomObjectDeserialization() {

    Task newTask = taskService.newTask();
    taskService.saveTask(newTask);

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("customSerializable", new CustomSerializable());
    variables.put("failingSerializable", new FailingSerializable());
    taskService.setVariables(newTask.getId(), variables);

    List<HistoricDetail> results = historyService.createHistoricDetailQuery()
        .disableBinaryFetching()
        .disableCustomObjectDeserialization()
        .variableUpdates()
        .list();

    // both variables are not deserialized, but their serialized values are available
    assertEquals(2, results.size());

    for (HistoricDetail update : results) {
      HistoricVariableUpdate variableUpdate = (HistoricVariableUpdate) update;
      assertNull(variableUpdate.getErrorMessage());

      ObjectValue typedValue = (ObjectValue) variableUpdate.getTypedValue();
      assertNotNull(typedValue);
      assertFalse(typedValue.isDeserialized());
      // cannot access the deserialized value
      try {
        typedValue.getValue();
      }
      catch(IllegalStateException e) {
        assertThat(e.getMessage()).contains("Object is not deserialized");
      }
      assertNotNull(typedValue.getValueSerialized());
    }

    taskService.deleteTask(newTask.getId(), true);
  }

  @Test
  public void testErrorMessage() {

    Task newTask = taskService.newTask();
    taskService.saveTask(newTask);

    String variableName = "failingSerializable";
    taskService.setVariable(newTask.getId(), variableName, new FailingSerializable());

    HistoricDetail result = historyService.createHistoricDetailQuery()
        .disableBinaryFetching()
        .variableUpdates()
        .singleResult();

    assertNull(((HistoricVariableUpdate)result).getValue());
    assertNotNull(((HistoricVariableUpdate)result).getErrorMessage());

    taskService.deleteTask(newTask.getId(), true);

  }

  @Test
  public void testVariableInstance() {

    Task newTask = taskService.newTask();
    taskService.saveTask(newTask);

    String variableName = "someName";
    String variableValue = "someValue";
    taskService.setVariable(newTask.getId(), variableName, variableValue);

    VariableInstance variable = runtimeService
        .createVariableInstanceQuery()
        .singleResult();
    assertNotNull(variable);

    HistoricVariableUpdate result = (HistoricVariableUpdate) historyService
        .createHistoricDetailQuery()
        .variableUpdates()
        .singleResult();
    assertNotNull(result);

    assertEquals(variable.getId(), result.getVariableInstanceId());

    taskService.deleteTask(newTask.getId(), true);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testHistoricVariableUpdateProcessDefinitionProperty() {
    // given
    String key = "oneTaskProcess";
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(key);

    String processInstanceId = processInstance.getId();
    String taskId = taskService.createTaskQuery().singleResult().getId();

    runtimeService.setVariable(processInstanceId, "aVariable", "aValue");
    taskService.setVariableLocal(taskId, "aLocalVariable", "anotherValue");

    String firstVariable = runtimeService
        .createVariableInstanceQuery()
        .variableName("aVariable")
        .singleResult()
        .getId();

    String secondVariable = runtimeService
        .createVariableInstanceQuery()
        .variableName("aLocalVariable")
        .singleResult()
        .getId();

    // when (1)
    HistoricVariableUpdate instance = (HistoricVariableUpdate) historyService
        .createHistoricDetailQuery()
        .variableUpdates()
        .variableInstanceId(firstVariable)
        .singleResult();

    // then (1)
    assertNotNull(instance.getProcessDefinitionKey());
    assertEquals(key, instance.getProcessDefinitionKey());

    assertNotNull(instance.getProcessDefinitionId());
    assertEquals(processInstance.getProcessDefinitionId(), instance.getProcessDefinitionId());

    assertNull(instance.getCaseDefinitionKey());
    assertNull(instance.getCaseDefinitionId());

    // when (2)
    instance = (HistoricVariableUpdate) historyService
        .createHistoricDetailQuery()
        .variableUpdates()
        .variableInstanceId(secondVariable)
        .singleResult();

    // then (2)
    assertNotNull(instance.getProcessDefinitionKey());
    assertEquals(key, instance.getProcessDefinitionKey());

    assertNotNull(instance.getProcessDefinitionId());
    assertEquals(processInstance.getProcessDefinitionId(), instance.getProcessDefinitionId());

    assertNull(instance.getCaseDefinitionKey());
    assertNull(instance.getCaseDefinitionId());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn")
  public void testHistoricVariableUpdateCaseDefinitionProperty() {
    // given
    String key = "oneTaskCase";
    CaseInstance caseInstance = caseService.createCaseInstanceByKey(key);

    String caseInstanceId = caseInstance.getId();

    String humanTask = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();
    String taskId = taskService.createTaskQuery().singleResult().getId();

    caseService.setVariable(caseInstanceId, "aVariable", "aValue");
    taskService.setVariableLocal(taskId, "aLocalVariable", "anotherValue");

    String firstVariable = runtimeService
        .createVariableInstanceQuery()
        .variableName("aVariable")
        .singleResult()
        .getId();

    String secondVariable = runtimeService
        .createVariableInstanceQuery()
        .variableName("aLocalVariable")
        .singleResult()
        .getId();


    // when (1)
    HistoricVariableUpdate instance = (HistoricVariableUpdate) historyService
        .createHistoricDetailQuery()
        .variableUpdates()
        .variableInstanceId(firstVariable)
        .singleResult();

    // then (1)
    assertNotNull(instance.getCaseDefinitionKey());
    assertEquals(key, instance.getCaseDefinitionKey());

    assertNotNull(instance.getCaseDefinitionId());
    assertEquals(caseInstance.getCaseDefinitionId(), instance.getCaseDefinitionId());

    assertNull(instance.getProcessDefinitionKey());
    assertNull(instance.getProcessDefinitionId());

    // when (2)
    instance = (HistoricVariableUpdate) historyService
        .createHistoricDetailQuery()
        .variableUpdates()
        .variableInstanceId(secondVariable)
        .singleResult();

    // then (2)
    assertNotNull(instance.getCaseDefinitionKey());
    assertEquals(key, instance.getCaseDefinitionKey());

    assertNotNull(instance.getCaseDefinitionId());
    assertEquals(caseInstance.getCaseDefinitionId(), instance.getCaseDefinitionId());

    assertNull(instance.getProcessDefinitionKey());
    assertNull(instance.getProcessDefinitionId());
  }

  @Test
  public void testHistoricVariableUpdateStandaloneTaskDefinitionProperties() {
    // given
    String taskId = "myTask";
    Task task = taskService.newTask(taskId);
    taskService.saveTask(task);

    taskService.setVariable(taskId, "aVariable", "anotherValue");

    String firstVariable = runtimeService
        .createVariableInstanceQuery()
        .variableName("aVariable")
        .singleResult()
        .getId();

    // when
    HistoricVariableUpdate instance = (HistoricVariableUpdate) historyService
        .createHistoricDetailQuery()
        .variableUpdates()
        .variableInstanceId(firstVariable)
        .singleResult();

    // then
    assertNull(instance.getProcessDefinitionKey());
    assertNull(instance.getProcessDefinitionId());
    assertNull(instance.getCaseDefinitionKey());
    assertNull(instance.getCaseDefinitionId());

    taskService.deleteTask(taskId, true);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testHistoricFormFieldProcessDefinitionProperty() {
    // given
    String key = "oneTaskProcess";
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(key);

    String taskId = taskService.createTaskQuery().singleResult().getId();

    formService.submitTaskForm(taskId, Variables.createVariables().putValue("aVariable", "aValue"));

    // when
    HistoricFormField instance = (HistoricFormField) historyService
        .createHistoricDetailQuery()
        .formFields()
        .singleResult();

    // then
    assertNotNull(instance.getProcessDefinitionKey());
    assertEquals(key, instance.getProcessDefinitionKey());

    assertNotNull(instance.getProcessDefinitionId());
    assertEquals(processInstance.getProcessDefinitionId(), instance.getProcessDefinitionId());

    assertNull(instance.getCaseDefinitionKey());
    assertNull(instance.getCaseDefinitionId());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testDeleteProcessInstanceSkipCustomListener() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("oneTaskProcess").getId();

    // when
    runtimeService.deleteProcessInstance(processInstanceId, null, true);

    // then
    HistoricProcessInstance instance = historyService
        .createHistoricProcessInstanceQuery()
        .processInstanceId(processInstanceId)
        .singleResult();
    assertNotNull(instance);

    assertEquals(processInstanceId, instance.getId());
    assertNotNull(instance.getEndTime());
  }

}
