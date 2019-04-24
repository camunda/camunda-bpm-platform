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
package org.camunda.bpm.engine.test.api.history;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.HistoricDetailQuery;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.CollectionUtil;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstanceQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.runtime.ProcessInstanceQueryTest;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.slf4j.Logger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @author Frederik Heremans
 * @author Falko Menge
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
public class HistoryServiceTest extends PluggableProcessEngineTestCase {

  public static final String ONE_TASK_PROCESS = "oneTaskProcess";
  protected static Logger LOG = ProcessEngineLogger.TEST_LOGGER.getLogger();

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testHistoricProcessInstanceQuery() {
    // With a clean ProcessEngine, no instances should be available
    assertTrue(historyService.createHistoricProcessInstanceQuery().count() == 0);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS);
    assertTrue(historyService.createHistoricProcessInstanceQuery().count() == 1);

    // Complete the task and check if the size is count 1
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    assertEquals(1, tasks.size());
    taskService.complete(tasks.get(0).getId());
    assertTrue(historyService.createHistoricProcessInstanceQuery().count() == 1);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testHistoricProcessInstanceQueryOrderBy() {
    // With a clean ProcessEngine, no instances should be available
    assertTrue(historyService.createHistoricProcessInstanceQuery().count() == 0);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS);

    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    assertEquals(1, tasks.size());
    taskService.complete(tasks.get(0).getId());

    historyService.createHistoricTaskInstanceQuery().orderByDeleteReason().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByExecutionId().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByHistoricActivityInstanceId().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByHistoricActivityInstanceStartTime().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByHistoricTaskInstanceDuration().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByHistoricTaskInstanceEndTime().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByProcessDefinitionId().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByProcessInstanceId().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByTaskAssignee().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByTaskDefinitionKey().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByTaskDescription().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByTaskId().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByTaskName().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByTaskOwner().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByTaskPriority().asc().list();
  }

  @SuppressWarnings("deprecation") // deprecated method is tested here
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testHistoricProcessInstanceUserIdAndActivityId() {
    identityService.setAuthenticatedUserId("johndoe");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS);
    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();
    assertEquals("johndoe", historicProcessInstance.getStartUserId());
    assertEquals("theStart", historicProcessInstance.getStartActivityId());

    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    assertEquals(1, tasks.size());
    taskService.complete(tasks.get(0).getId());

    historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();
    assertEquals("theEnd", historicProcessInstance.getEndActivityId());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/history/orderProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/history/checkCreditProcess.bpmn20.xml"})
  public void testOrderProcessWithCallActivity() {
    // After the process has started, the 'verify credit history' task should be
    // active
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("orderProcess");
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task verifyCreditTask = taskQuery.singleResult();

    // Completing the task with approval, will end the subprocess and continue
    // the original process
    taskService.complete(verifyCreditTask.getId(), CollectionUtil.singletonMap("creditApproved", true));
    Task prepareAndShipTask = taskQuery.singleResult();
    assertEquals("Prepare and Ship", prepareAndShipTask.getName());

    // verify
    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().superProcessInstanceId(pi.getId()).singleResult();
    assertNotNull(historicProcessInstance);
    assertTrue(historicProcessInstance.getProcessDefinitionId().contains("checkCreditProcess"));
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/history/orderProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/history/checkCreditProcess.bpmn20.xml"})
  public void testHistoricProcessInstanceQueryByProcessDefinitionKey() {

    String processDefinitionKey = ONE_TASK_PROCESS;
    runtimeService.startProcessInstanceByKey(processDefinitionKey);
    runtimeService.startProcessInstanceByKey("orderProcess");
    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processDefinitionKey(processDefinitionKey)
        .singleResult();
    assertNotNull(historicProcessInstance);
    assertTrue(historicProcessInstance.getProcessDefinitionId().startsWith(processDefinitionKey));
    assertEquals("theStart", historicProcessInstance.getStartActivityId());

    // now complete the task to end the process instance
    Task task = taskService.createTaskQuery().processDefinitionKey("checkCreditProcess").singleResult();
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("creditApproved", true);
    taskService.complete(task.getId(), map);

    // and make sure the super process instance is set correctly on the
    // HistoricProcessInstance
    HistoricProcessInstance historicProcessInstanceSub = historyService.createHistoricProcessInstanceQuery().processDefinitionKey("checkCreditProcess")
        .singleResult();
    HistoricProcessInstance historicProcessInstanceSuper = historyService.createHistoricProcessInstanceQuery().processDefinitionKey("orderProcess")
        .singleResult();
    assertEquals(historicProcessInstanceSuper.getId(), historicProcessInstanceSub.getSuperProcessInstanceId());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/runtime/otherOneTaskProcess.bpmn20.xml" })
  public void testHistoricProcessInstanceQueryByProcessInstanceIds() {
    HashSet<String> processInstanceIds = new HashSet<String>();
    for (int i = 0; i < 4; i++) {
      processInstanceIds.add(runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS, i + "").getId());
    }
    processInstanceIds.add(runtimeService.startProcessInstanceByKey("otherOneTaskProcess", "1").getId());

    // start an instance that will not be part of the query
    runtimeService.startProcessInstanceByKey("otherOneTaskProcess", "2");

    HistoricProcessInstanceQuery processInstanceQuery = historyService.createHistoricProcessInstanceQuery().processInstanceIds(processInstanceIds);
    assertEquals(5, processInstanceQuery.count());

    List<HistoricProcessInstance> processInstances = processInstanceQuery.list();
    assertNotNull(processInstances);
    assertEquals(5, processInstances.size());

    for (HistoricProcessInstance historicProcessInstance : processInstances) {
      assertTrue(processInstanceIds.contains(historicProcessInstance.getId()));
    }

    // making a query that has contradicting conditions should succeed
    assertEquals(0, processInstanceQuery.processInstanceId("dummy").count());
  }

  public void testHistoricProcessInstanceQueryByProcessInstanceIdsEmpty() {
    try {
      historyService.createHistoricProcessInstanceQuery().processInstanceIds(new HashSet<String>());
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      assertTextPresent("Set of process instance ids is empty", re.getMessage());
    }
  }

  public void testHistoricProcessInstanceQueryByProcessInstanceIdsNull() {
    try {
      historyService.createHistoricProcessInstanceQuery().processInstanceIds(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      assertTextPresent("Set of process instance ids is null", re.getMessage());
    }
  }

  public void testQueryByRootProcessInstances() {
    // given
    String superProcess = "calling";
    String subProcess = "called";
    BpmnModelInstance callingInstance = ProcessModels.newModel(superProcess)
      .startEvent()
      .callActivity()
      .calledElement(subProcess)
      .endEvent()
      .done();

    BpmnModelInstance calledInstance = ProcessModels.newModel(subProcess)
      .startEvent()
      .userTask()
      .endEvent()
      .done();

    deployment(callingInstance, calledInstance);
    String processInstanceId1 = runtimeService.startProcessInstanceByKey(superProcess).getProcessInstanceId();

    // when
    List<HistoricProcessInstance> list = historyService
        .createHistoricProcessInstanceQuery()
        .rootProcessInstances()
        .list();

    // then
    assertEquals(1, list.size());
    assertEquals(processInstanceId1, list.get(0).getId());
  }

  public void testQueryByRootProcessInstancesAndSuperProcess() {
    // when
    try {
      historyService.createHistoricProcessInstanceQuery()
        .rootProcessInstances()
        .superProcessInstanceId("processInstanceId");

      fail("expected exception");
    } catch (BadUserRequestException e) {
      // then
      assertTrue(e.getMessage().contains("Invalid query usage: cannot set both rootProcessInstances and superProcessInstanceId"));
    }

    // when
    try {
      historyService.createHistoricProcessInstanceQuery()
        .superProcessInstanceId("processInstanceId")
        .rootProcessInstances();

      fail("expected exception");
    } catch (BadUserRequestException e) {
      // then
      assertTrue(e.getMessage().contains("Invalid query usage: cannot set both rootProcessInstances and superProcessInstanceId"));
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/concurrentExecution.bpmn20.xml"})
  public void testHistoricVariableInstancesOnParallelExecution() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("rootValue", "test");
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("concurrent", vars);

    List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
    for (Task task : tasks) {
      Map<String, Object> variables = new HashMap<String, Object>();
      // set token local variable
      LOG.debug("setting variables on task " + task.getId() + ", execution " + task.getExecutionId());
      runtimeService.setVariableLocal(task.getExecutionId(), "parallelValue1", task.getName());
      runtimeService.setVariableLocal(task.getExecutionId(), "parallelValue2", "test");
      taskService.complete(task.getId(), variables);
    }
    taskService.complete(taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult().getId());

    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueEquals("rootValue", "test").count());

    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueEquals("parallelValue1", "Receive Payment").count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueEquals("parallelValue1", "Ship Order").count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueEquals("parallelValue2", "test").count());
  }

  /**
   * basically copied from {@link ProcessInstanceQueryTest}
   */
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryStringVariable() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("stringVar", "abcdef");
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS, vars);
    taskService.complete(taskService.createTaskQuery().processInstanceId(processInstance1.getId()).singleResult().getId());

    vars = new HashMap<String, Object>();
    vars.put("stringVar", "abcdef");
    vars.put("stringVar2", "ghijkl");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS, vars);
    taskService.complete(taskService.createTaskQuery().processInstanceId(processInstance2.getId()).singleResult().getId());

    vars = new HashMap<String, Object>();
    vars.put("stringVar", "azerty");
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS, vars);
    taskService.complete(taskService.createTaskQuery().processInstanceId(processInstance3.getId()).singleResult().getId());

    // Test EQUAL on single string variable, should result in 2 matches
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().variableValueEquals("stringVar", "abcdef");
    List<HistoricProcessInstance> processInstances = query.list();
    Assert.assertNotNull(processInstances);
    Assert.assertEquals(2, processInstances.size());

    // Test EQUAL on two string variables, should result in single match
    query = historyService.createHistoricProcessInstanceQuery().variableValueEquals("stringVar", "abcdef").variableValueEquals("stringVar2", "ghijkl");
    HistoricProcessInstance resultInstance = query.singleResult();
    Assert.assertNotNull(resultInstance);
    Assert.assertEquals(processInstance2.getId(), resultInstance.getId());

    // Test NOT_EQUAL, should return only 1 resultInstance
    resultInstance = historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("stringVar", "abcdef").singleResult();
    Assert.assertNotNull(resultInstance);
    Assert.assertEquals(processInstance3.getId(), resultInstance.getId());

    // Test GREATER_THAN, should return only matching 'azerty'
    resultInstance = historyService.createHistoricProcessInstanceQuery().variableValueGreaterThan("stringVar", "abcdef").singleResult();
    Assert.assertNotNull(resultInstance);
    Assert.assertEquals(processInstance3.getId(), resultInstance.getId());

    resultInstance = historyService.createHistoricProcessInstanceQuery().variableValueGreaterThan("stringVar", "z").singleResult();
    Assert.assertNull(resultInstance);

    // Test GREATER_THAN_OR_EQUAL, should return 3 results
    assertEquals(3, historyService.createHistoricProcessInstanceQuery().variableValueGreaterThanOrEqual("stringVar", "abcdef").count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueGreaterThanOrEqual("stringVar", "z").count());

    // Test LESS_THAN, should return 2 results
    processInstances = historyService.createHistoricProcessInstanceQuery().variableValueLessThan("stringVar", "abcdeg").list();
    Assert.assertEquals(2, processInstances.size());
    List<String> expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    List<String> ids = new ArrayList<String>(Arrays.asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());

    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueLessThan("stringVar", "abcdef").count());
    assertEquals(3, historyService.createHistoricProcessInstanceQuery().variableValueLessThanOrEqual("stringVar", "z").count());

    // Test LESS_THAN_OR_EQUAL
    processInstances = historyService.createHistoricProcessInstanceQuery().variableValueLessThanOrEqual("stringVar", "abcdef").list();
    Assert.assertEquals(2, processInstances.size());
    expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    ids = new ArrayList<String>(Arrays.asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());

    assertEquals(3, historyService.createHistoricProcessInstanceQuery().variableValueLessThanOrEqual("stringVar", "z").count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueLessThanOrEqual("stringVar", "aa").count());

    // Test LIKE
    resultInstance = historyService.createHistoricProcessInstanceQuery().variableValueLike("stringVar", "azert%").singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());

    resultInstance = historyService.createHistoricProcessInstanceQuery().variableValueLike("stringVar", "%y").singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());

    resultInstance = historyService.createHistoricProcessInstanceQuery().variableValueLike("stringVar", "%zer%").singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());

    assertEquals(3, historyService.createHistoricProcessInstanceQuery().variableValueLike("stringVar", "a%").count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueLike("stringVar", "%x%").count());

    historyService.deleteHistoricProcessInstance(processInstance1.getId());
    historyService.deleteHistoricProcessInstance(processInstance2.getId());
    historyService.deleteHistoricProcessInstance(processInstance3.getId());
  }

  /**
   * Only do one second type, as the logic is same as in {@link ProcessInstanceQueryTest} and I do not want to duplicate
   * all test case logic here.
   * Basically copied from {@link ProcessInstanceQueryTest}
   */
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryDateVariable() throws Exception {
    Map<String, Object> vars = new HashMap<String, Object>();
    Date date1 = Calendar.getInstance().getTime();
    vars.put("dateVar", date1);

    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS, vars);
    taskService.complete(taskService.createTaskQuery().processInstanceId(processInstance1.getId()).singleResult().getId());

    Date date2 = Calendar.getInstance().getTime();
    vars = new HashMap<String, Object>();
    vars.put("dateVar", date1);
    vars.put("dateVar2", date2);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS, vars);
    taskService.complete(taskService.createTaskQuery().processInstanceId(processInstance2.getId()).singleResult().getId());

    Calendar nextYear = Calendar.getInstance();
    nextYear.add(Calendar.YEAR, 1);
    vars = new HashMap<String, Object>();
    vars.put("dateVar", nextYear.getTime());
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS, vars);
    taskService.complete(taskService.createTaskQuery().processInstanceId(processInstance3.getId()).singleResult().getId());

    Calendar nextMonth = Calendar.getInstance();
    nextMonth.add(Calendar.MONTH, 1);

    Calendar twoYearsLater = Calendar.getInstance();
    twoYearsLater.add(Calendar.YEAR, 2);

    Calendar oneYearAgo = Calendar.getInstance();
    oneYearAgo.add(Calendar.YEAR, -1);

    // Query on single short variable, should result in 2 matches
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().variableValueEquals("dateVar", date1);
    List<HistoricProcessInstance> processInstances = query.list();
    Assert.assertNotNull(processInstances);
    Assert.assertEquals(2, processInstances.size());

    // Query on two short variables, should result in single value
    query = historyService.createHistoricProcessInstanceQuery().variableValueEquals("dateVar", date1).variableValueEquals("dateVar2", date2);
    HistoricProcessInstance resultInstance = query.singleResult();
    Assert.assertNotNull(resultInstance);
    Assert.assertEquals(processInstance2.getId(), resultInstance.getId());

    // Query with unexisting variable value
    Date unexistingDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse("01/01/1989 12:00:00");
    resultInstance = historyService.createHistoricProcessInstanceQuery().variableValueEquals("dateVar", unexistingDate).singleResult();
    Assert.assertNull(resultInstance);

    // Test NOT_EQUALS
    resultInstance = historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("dateVar", date1).singleResult();
    Assert.assertNotNull(resultInstance);
    Assert.assertEquals(processInstance3.getId(), resultInstance.getId());

    // Test GREATER_THAN
    resultInstance = historyService.createHistoricProcessInstanceQuery().variableValueGreaterThan("dateVar", nextMonth.getTime()).singleResult();
    Assert.assertNotNull(resultInstance);
    Assert.assertEquals(processInstance3.getId(), resultInstance.getId());

    Assert.assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueGreaterThan("dateVar", nextYear.getTime()).count());
    Assert.assertEquals(3, historyService.createHistoricProcessInstanceQuery().variableValueGreaterThan("dateVar", oneYearAgo.getTime()).count());

    // Test GREATER_THAN_OR_EQUAL
    resultInstance = historyService.createHistoricProcessInstanceQuery().variableValueGreaterThanOrEqual("dateVar", nextMonth.getTime()).singleResult();
    Assert.assertNotNull(resultInstance);
    Assert.assertEquals(processInstance3.getId(), resultInstance.getId());

    resultInstance = historyService.createHistoricProcessInstanceQuery().variableValueGreaterThanOrEqual("dateVar", nextYear.getTime()).singleResult();
    Assert.assertNotNull(resultInstance);
    Assert.assertEquals(processInstance3.getId(), resultInstance.getId());

    Assert.assertEquals(3, historyService.createHistoricProcessInstanceQuery().variableValueGreaterThanOrEqual("dateVar", oneYearAgo.getTime()).count());

    // Test LESS_THAN
    processInstances = historyService.createHistoricProcessInstanceQuery().variableValueLessThan("dateVar", nextYear.getTime()).list();
    Assert.assertEquals(2, processInstances.size());

    List<String> expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    List<String> ids = new ArrayList<String>(Arrays.asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());

    Assert.assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueLessThan("dateVar", date1).count());
    Assert.assertEquals(3, historyService.createHistoricProcessInstanceQuery().variableValueLessThan("dateVar", twoYearsLater.getTime()).count());

    // Test LESS_THAN_OR_EQUAL
    processInstances = historyService.createHistoricProcessInstanceQuery().variableValueLessThanOrEqual("dateVar", nextYear.getTime()).list();
    Assert.assertEquals(3, processInstances.size());

    Assert.assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueLessThanOrEqual("dateVar", oneYearAgo.getTime()).count());

    historyService.deleteHistoricProcessInstance(processInstance1.getId());
    historyService.deleteHistoricProcessInstance(processInstance2.getId());
    historyService.deleteHistoricProcessInstance(processInstance3.getId());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testNativeHistoricProcessInstanceTest() {
    // just test that the query will be constructed and executed, details are tested in the TaskQueryTest
    runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS);
    assertEquals(1, historyService.createNativeHistoricProcessInstanceQuery().sql("SELECT count(*) FROM " + managementService.getTableName(HistoricProcessInstance.class)).count());
    assertEquals(1, historyService.createNativeHistoricProcessInstanceQuery().sql("SELECT * FROM " + managementService.getTableName(HistoricProcessInstance.class)).list().size());
//  assertEquals(1, historyService.createNativeHistoricProcessInstanceQuery().sql("SELECT * FROM " + managementService.getTableName(HistoricProcessInstance.class)).listPage(0, 1).size());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testNativeHistoricTaskInstanceTest() {
    runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS);
    assertEquals(1, historyService.createNativeHistoricTaskInstanceQuery().sql("SELECT count(*) FROM " + managementService.getTableName(HistoricProcessInstance.class)).count());
    assertEquals(1, historyService.createNativeHistoricTaskInstanceQuery().sql("SELECT * FROM " + managementService.getTableName(HistoricProcessInstance.class)).list().size());
    assertEquals(1, historyService.createNativeHistoricTaskInstanceQuery().sql("SELECT * FROM " + managementService.getTableName(HistoricProcessInstance.class)).listPage(0, 1).size());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testNativeHistoricActivityInstanceTest() {
    runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS);
    assertEquals(1, historyService.createNativeHistoricActivityInstanceQuery().sql("SELECT count(*) FROM " + managementService.getTableName(HistoricProcessInstance.class)).count());
    assertEquals(1, historyService.createNativeHistoricActivityInstanceQuery().sql("SELECT * FROM " + managementService.getTableName(HistoricProcessInstance.class)).list().size());
    assertEquals(1, historyService.createNativeHistoricActivityInstanceQuery().sql("SELECT * FROM " + managementService.getTableName(HistoricProcessInstance.class)).listPage(0, 1).size());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testNativeHistoricVariableInstanceTest() {
    Date date = Calendar.getInstance().getTime();
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("stringVar", "abcdef");
    vars.put("dateVar", date);
    runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS, vars);

    assertEquals(2, historyService.createNativeHistoricVariableInstanceQuery().sql("SELECT count(*) FROM " + managementService.getTableName(HistoricVariableInstance.class)).count());
    assertEquals(1, historyService.createNativeHistoricVariableInstanceQuery().sql("SELECT * FROM " + managementService.getTableName(HistoricVariableInstance.class)).listPage(0, 1).size());

    List<HistoricVariableInstance> variables = historyService.createNativeHistoricVariableInstanceQuery().sql("SELECT * FROM " + managementService.getTableName(HistoricVariableInstance.class)).list();
    assertEquals(2, variables.size());
    for (HistoricVariableInstance variable : variables) {
      assertTrue(vars.containsKey(variable.getName()));
      assertEquals(vars.get(variable.getName()), variable.getValue());
      vars.remove(variable.getName());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testProcessVariableValueEqualsNumber() throws Exception {
    // long
    runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS,
        Collections.<String, Object>singletonMap("var", 123L));

    // non-matching long
    runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS,
        Collections.<String, Object>singletonMap("var", 12345L));

    // short
    runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS,
        Collections.<String, Object>singletonMap("var", (short) 123));

    // double
    runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS,
        Collections.<String, Object>singletonMap("var", 123.0d));

    // integer
    runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS,
        Collections.<String, Object>singletonMap("var", 123));

    // untyped null (should not match)
    runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS,
        Collections.<String, Object>singletonMap("var", null));

    // typed null (should not match)
    runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS,
        Collections.<String, Object>singletonMap("var", Variables.longValue(null)));

    runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS,
        Collections.<String, Object>singletonMap("var", "123"));

    assertEquals(4, historyService.createHistoricProcessInstanceQuery().variableValueEquals("var", Variables.numberValue(123)).count());
    assertEquals(4, historyService.createHistoricProcessInstanceQuery().variableValueEquals("var", Variables.numberValue(123L)).count());
    assertEquals(4, historyService.createHistoricProcessInstanceQuery().variableValueEquals("var", Variables.numberValue(123.0d)).count());
    assertEquals(4, historyService.createHistoricProcessInstanceQuery().variableValueEquals("var", Variables.numberValue((short) 123)).count());

    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueEquals("var", Variables.numberValue(null)).count());
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testDeleteProcessInstance() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS);
    assertEquals(1, runtimeService.createProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());

    runtimeService.deleteProcessInstance(processInstance.getId(), null);
    assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());

    historyService.deleteHistoricProcessInstance(processInstance.getId());
    assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testDeleteRunningProcessInstance() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS);
    assertEquals(1, runtimeService.createProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
    try {
      historyService.deleteHistoricProcessInstance(processInstance.getId());
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("Process instance is still running, cannot delete historic process instance", ae.getMessage());
    }
  }

  public void testDeleteProcessInstanceWithFake() {
    try {
      historyService.deleteHistoricProcessInstance("aFake");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("No historic process instance found with id", ae.getMessage());
    }
  }

  public void testDeleteProcessInstanceIfExistsWithFake() {
      historyService.deleteHistoricProcessInstanceIfExists("aFake");
      //don't expect exception
  }

  public void testDeleteProcessInstanceNullId() {
    try {
      historyService.deleteHistoricProcessInstance(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("processInstanceId is null", ae.getMessage());
    }
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testDeleteProcessInstances() {
    //given
    List<String> ids = prepareHistoricProcesses();

    //when
    historyService.deleteHistoricProcessInstances(ids);

    //then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testDeleteProcessInstancesWithFake() {
    //given
    List<String> ids = prepareHistoricProcesses();
    ids.add("aFake");

    try {
      //when
      historyService.deleteHistoricProcessInstances(ids);
      fail("Exception expected");
    } catch (ProcessEngineException e) {
      //expected
      Assert.assertThat(e.getMessage(), CoreMatchers.containsString("No historic process instance found with id: [aFake]" ));
    }

    //then expect no instance is deleted
    assertEquals(2, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
  }

  @Deployment(resources = {
  "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testDeleteProcessInstancesIfExistsWithFake() {
    //given
    List<String> ids = prepareHistoricProcesses();
    ids.add("aFake");

    //when
    historyService.deleteHistoricProcessInstancesIfExists(ids);

    //then expect no exception and all instances are deleted
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testDeleteProcessInstancesWithNull() {
    try {
      //when
      historyService.deleteHistoricProcessInstances(null);
      fail("Exception expected");
    } catch (ProcessEngineException e) {
      //expected
    }
  }
  
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources = { 
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testDeleteHistoricVariableAndDetails() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS);
    String executionId = processInstance.getId();
    assertEquals(1, runtimeService.createProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
    runtimeService.setVariable(executionId, "myVariable", "testValue1");
    runtimeService.setVariable(executionId, "myVariable", "testValue2");
    runtimeService.setVariable(executionId, "myVariable", "testValue3");
    runtimeService.setVariable(executionId, "mySecondVariable", 5L);
    
    runtimeService.deleteProcessInstance(executionId, null);
    assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());

    HistoricVariableInstanceQuery histVariableQuery = historyService.createHistoricVariableInstanceQuery()
        .processInstanceId(executionId)
        .variableName("myVariable");
    HistoricVariableInstanceQuery secondHistVariableQuery = historyService.createHistoricVariableInstanceQuery()
        .processInstanceId(executionId)
        .variableName("mySecondVariable");
    assertEquals(1, histVariableQuery.count());
    assertEquals(1, secondHistVariableQuery.count());
    
    String variableInstanceId = histVariableQuery.singleResult().getId();
    String secondVariableInstanceId = secondHistVariableQuery.singleResult().getId();
    HistoricDetailQuery detailsQuery = historyService.createHistoricDetailQuery()
        .processInstanceId(executionId)
        .variableInstanceId(variableInstanceId);
    HistoricDetailQuery secondDetailsQuery = historyService.createHistoricDetailQuery()
        .processInstanceId(executionId)
        .variableInstanceId(secondVariableInstanceId);
    assertEquals(3, detailsQuery.count());
    assertEquals(1, secondDetailsQuery.count());
    
    // when
    historyService.deleteHistoricVariableInstance(variableInstanceId);
    
    // then
    assertEquals(0, histVariableQuery.count());
    assertEquals(1, secondHistVariableQuery.count());
    assertEquals(0, detailsQuery.count());
    assertEquals(1, secondDetailsQuery.count());
  }
  
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testDeleteHistoricVariableAndDetailsOnRunningInstance() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS);
    String executionId = processInstance.getId();
    assertEquals(1, runtimeService.createProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
    runtimeService.setVariable(executionId, "myVariable", "testValue1");
    runtimeService.setVariable(executionId, "myVariable", "testValue2");
    runtimeService.setVariable(executionId, "myVariable", "testValue3");
    
    VariableInstanceQuery variableQuery = runtimeService.createVariableInstanceQuery()
        .processInstanceIdIn(executionId)
        .variableName("myVariable");
    assertEquals(1, variableQuery.count());
    assertEquals("testValue3", variableQuery.singleResult().getValue());

    HistoricVariableInstanceQuery histVariableQuery = historyService.createHistoricVariableInstanceQuery()
        .processInstanceId(executionId)
        .variableName("myVariable");
    assertEquals(1, histVariableQuery.count());

    String variableInstanceId = histVariableQuery.singleResult().getId();
    HistoricDetailQuery detailsQuery = historyService.createHistoricDetailQuery()
        .processInstanceId(executionId)
        .variableInstanceId(variableInstanceId);
    assertEquals(3, detailsQuery.count());

    // when
    historyService.deleteHistoricVariableInstance(variableInstanceId);

    // then
    assertEquals(0, histVariableQuery.count());
    assertEquals(0, detailsQuery.count());
    assertEquals(1, variableQuery.count());
    assertEquals("testValue3", variableQuery.singleResult().getValue());
  }
  
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testDeleteHistoricVariableAndDetailsOnRunningInstanceAndSetAgain() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS);
    String executionId = processInstance.getId();
    runtimeService.setVariable(executionId, "myVariable", "testValue1");
    runtimeService.setVariable(executionId, "myVariable", "testValue2");
    runtimeService.setVariable(executionId, "myVariable", "testValue3");
    
    VariableInstanceQuery variableQuery = runtimeService.createVariableInstanceQuery()
        .processInstanceIdIn(executionId)
        .variableName("myVariable");
    HistoricVariableInstanceQuery histVariableQuery = historyService.createHistoricVariableInstanceQuery()
        .processInstanceId(executionId)
        .variableName("myVariable");
    
    String variableInstanceId = histVariableQuery.singleResult().getId();

    HistoricDetailQuery detailsQuery = historyService.createHistoricDetailQuery()
        .processInstanceId(executionId)
        .variableInstanceId(variableInstanceId);
    
    
    historyService.deleteHistoricVariableInstance(variableInstanceId);
    
    assertEquals(0, histVariableQuery.count());
    assertEquals(0, detailsQuery.count());
    assertEquals(1, variableQuery.count());
    assertEquals("testValue3", variableQuery.singleResult().getValue());
    
    // when
    runtimeService.setVariable(executionId, "myVariable", "testValue4");
    
    // then
    assertEquals(1, histVariableQuery.count());
    assertEquals(1, detailsQuery.count());
    assertEquals(1, variableQuery.count());
    assertEquals("testValue4", variableQuery.singleResult().getValue());
  }
  
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testDeleteHistoricVariableAndDetailsFromCase() {
    // given
    String caseInstanceId = caseService.createCaseInstanceByKey("oneTaskCase").getId();
    caseService.setVariable(caseInstanceId, "myVariable", 1);
    caseService.setVariable(caseInstanceId, "myVariable", 2);
    caseService.setVariable(caseInstanceId, "myVariable", 3);
    
    HistoricVariableInstance variableInstance = historyService.createHistoricVariableInstanceQuery().singleResult();
    HistoricDetailQuery detailsQuery = historyService.createHistoricDetailQuery()
        .caseInstanceId(caseInstanceId)
        .variableInstanceId(variableInstance.getId());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().count());
    assertEquals(3, detailsQuery.count());

    // when
    historyService.deleteHistoricVariableInstance(variableInstance.getId());

    // then
    assertEquals(0, historyService.createHistoricVariableInstanceQuery().count());
    assertEquals(0, detailsQuery.count());
  }
  
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testDeleteHistoricVariableAndDetailsFromCaseAndSetAgain() {
    // given
    String caseInstanceId = caseService.createCaseInstanceByKey("oneTaskCase").getId();
    caseService.setVariable(caseInstanceId, "myVariable", 1);
    caseService.setVariable(caseInstanceId, "myVariable", 2);
    caseService.setVariable(caseInstanceId, "myVariable", 3);
    
    HistoricVariableInstance variableInstance = historyService.createHistoricVariableInstanceQuery().singleResult();
    HistoricDetailQuery detailsQuery = historyService.createHistoricDetailQuery()
        .caseInstanceId(caseInstanceId)
        .variableInstanceId(variableInstance.getId());
    historyService.deleteHistoricVariableInstance(variableInstance.getId());
    assertEquals(0, historyService.createHistoricVariableInstanceQuery().count());
    assertEquals(0, detailsQuery.count());

    // when
    caseService.setVariable(caseInstanceId, "myVariable", 4);

    // then
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().count());
    assertEquals(1, detailsQuery.count());
  }
  
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testDeleteHistoricVariableAndDetailsFromStandaloneTask() {
    // given
    Task task = taskService.newTask();
    taskService.saveTask(task);
    taskService.setVariable(task.getId(), "testVariable", "testValue");
    taskService.setVariable(task.getId(), "testVariable", "testValue2");
    HistoricVariableInstance variableInstance = historyService.createHistoricVariableInstanceQuery().singleResult();
    HistoricDetailQuery detailsQuery = historyService.createHistoricDetailQuery()
        .taskId(task.getId())
        .variableInstanceId(variableInstance.getId());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().count());
    assertEquals(2, detailsQuery.count());
    
    // when
    historyService.deleteHistoricVariableInstance(variableInstance.getId());
    
    // then
    assertEquals(0, historyService.createHistoricVariableInstanceQuery().count());
    assertEquals(0, detailsQuery.count());
    
    taskService.deleteTask(task.getId(), true);
  }
  
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testDeleteHistoricVariableAndDetailsFromStandaloneTaskAndSetAgain() {
    // given
    Task task = taskService.newTask();
    taskService.saveTask(task);
    taskService.setVariable(task.getId(), "testVariable", "testValue");
    taskService.setVariable(task.getId(), "testVariable", "testValue2");
    
    HistoricVariableInstance variableInstance = historyService.createHistoricVariableInstanceQuery().singleResult();
    HistoricDetailQuery detailsQuery = historyService.createHistoricDetailQuery()
        .taskId(task.getId())
        .variableInstanceId(variableInstance.getId());
    
    historyService.deleteHistoricVariableInstance(variableInstance.getId());
    assertEquals(0, historyService.createHistoricVariableInstanceQuery().count());
    assertEquals(0, detailsQuery.count());
    
    // when
    taskService.setVariable(task.getId(), "testVariable", "testValue3");
    
    // then
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().count());
    assertEquals(1, detailsQuery.count());
    
    taskService.deleteTask(task.getId(), true);
  }
  
  public void testDeleteUnknownHistoricVariable() {
    try {
      // when
      historyService.deleteHistoricVariableInstance("fakeID");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      // then
      assertTextPresent("No historic variable instance found with id: fakeID", ae.getMessage());
    }
  }
  
  public void testDeleteHistoricVariableWithNull() {
    try{
      // when
      historyService.deleteHistoricVariableInstance(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      // then
      assertTextPresent("variableInstanceId is null", ae.getMessage());
    }
  }
  
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testDeleteAllHistoricVariablesAndDetails() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS);
    String executionId = processInstance.getId();
    assertEquals(1, runtimeService.createProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
    runtimeService.setVariable(executionId, "myVariable", "testValue1");
    runtimeService.setVariable(executionId, "myVariable", "testValue2");
    runtimeService.setVariable(executionId, "myVariable", "testValue3");
    runtimeService.setVariable(executionId, "mySecondVariable", 5L);
    runtimeService.setVariable(executionId, "mySecondVariable", 7L);

    runtimeService.deleteProcessInstance(executionId, null);
    assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());

    HistoricVariableInstanceQuery histVariableQuery = historyService.createHistoricVariableInstanceQuery().processInstanceId(executionId)
        .variableName("myVariable");
    HistoricVariableInstanceQuery secondHistVariableQuery = historyService.createHistoricVariableInstanceQuery().processInstanceId(executionId)
        .variableName("mySecondVariable");
    assertEquals(1, histVariableQuery.count());
    assertEquals(1, secondHistVariableQuery.count());

    String variableInstanceId = histVariableQuery.singleResult().getId();
    String secondVariableInstanceId = secondHistVariableQuery.singleResult().getId();
    HistoricDetailQuery detailsQuery = historyService.createHistoricDetailQuery().processInstanceId(executionId).variableInstanceId(variableInstanceId);
    HistoricDetailQuery secondDetailsQuery = historyService.createHistoricDetailQuery().processInstanceId(executionId)
        .variableInstanceId(secondVariableInstanceId);
    assertEquals(3, detailsQuery.count());
    assertEquals(2, secondDetailsQuery.count());

    // when
    historyService.deleteHistoricVariableInstancesByProcessInstanceId(executionId);

    // then
    assertEquals(0, histVariableQuery.count());
    assertEquals(0, secondHistVariableQuery.count());
    assertEquals(0, detailsQuery.count());
    assertEquals(0, secondDetailsQuery.count());
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testDeleteAllHistoricVariablesAndDetailsOnRunningInstance() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS);
    String executionId = processInstance.getId();
    assertEquals(1, runtimeService.createProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
    runtimeService.setVariable(executionId, "myVariable", "testValue1");
    runtimeService.setVariable(executionId, "myVariable", "testValue2");
    runtimeService.setVariable(executionId, "myVariable", "testValue3");
    runtimeService.setVariable(executionId, "mySecondVariable", "testValue1");
    runtimeService.setVariable(executionId, "mySecondVariable", "testValue2");

    VariableInstanceQuery variableQuery = runtimeService.createVariableInstanceQuery().processInstanceIdIn(executionId).variableName("myVariable");
    VariableInstanceQuery secondVariableQuery = runtimeService.createVariableInstanceQuery().processInstanceIdIn(executionId).variableName("mySecondVariable");
    assertEquals(1L, variableQuery.count());
    assertEquals(1L, secondVariableQuery.count());
    assertEquals("testValue3", variableQuery.singleResult().getValue());
    assertEquals("testValue2", secondVariableQuery.singleResult().getValue());

    HistoricVariableInstanceQuery histVariableQuery = historyService.createHistoricVariableInstanceQuery().processInstanceId(executionId)
        .variableName("myVariable");
    HistoricVariableInstanceQuery secondHistVariableQuery = historyService.createHistoricVariableInstanceQuery().processInstanceId(executionId)
        .variableName("mySecondVariable");
    assertEquals(1L, histVariableQuery.count());
    assertEquals(1L, secondHistVariableQuery.count());

    String variableInstanceId = histVariableQuery.singleResult().getId();
    String secondVariableInstanceId = secondHistVariableQuery.singleResult().getId();
    HistoricDetailQuery detailsQuery = historyService.createHistoricDetailQuery().processInstanceId(executionId).variableInstanceId(variableInstanceId);
    HistoricDetailQuery secondDetailsQuery = historyService.createHistoricDetailQuery().processInstanceId(executionId).variableInstanceId(secondVariableInstanceId);
    assertEquals(3L, detailsQuery.count());
    assertEquals(2L, secondDetailsQuery.count());

    // when
    historyService.deleteHistoricVariableInstancesByProcessInstanceId(executionId);

    // then
    HistoricVariableInstanceQuery allHistVariableQuery = historyService.createHistoricVariableInstanceQuery().processInstanceId(executionId);
    HistoricDetailQuery allDetailsQuery = historyService.createHistoricDetailQuery().processInstanceId(executionId);
    assertEquals(0L, histVariableQuery.count());
    assertEquals(0L, secondHistVariableQuery.count());
    assertEquals(0L, allHistVariableQuery.count());
    assertEquals(0L, detailsQuery.count());
    assertEquals(0L, secondDetailsQuery.count());
    assertEquals(0L, allDetailsQuery.count());
    assertEquals(1L, variableQuery.count());
    assertEquals("testValue3", variableQuery.singleResult().getValue());
    assertEquals("testValue2", secondVariableQuery.singleResult().getValue());
  }
  
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testDeleteAllHistoricVariablesAndDetailsOnRunningInstanceAndSetAgain() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS);
    String executionId = processInstance.getId();
    runtimeService.setVariable(executionId, "myVariable", "testValue1");
    runtimeService.setVariable(executionId, "myVariable", "testValue2");
    runtimeService.setVariable(executionId, "mySecondVariable", "testValue1");
    runtimeService.setVariable(executionId, "mySecondVariable", "testValue2");

    historyService.deleteHistoricVariableInstancesByProcessInstanceId(executionId);

    VariableInstanceQuery variableQuery = runtimeService.createVariableInstanceQuery().processInstanceIdIn(executionId).variableName("myVariable");
    VariableInstanceQuery secondVariableQuery = runtimeService.createVariableInstanceQuery().processInstanceIdIn(executionId).variableName("mySecondVariable");
    HistoricVariableInstanceQuery allHistVariableQuery = historyService.createHistoricVariableInstanceQuery().processInstanceId(executionId);
    HistoricDetailQuery allDetailsQuery = historyService.createHistoricDetailQuery().processInstanceId(executionId);
    assertEquals(0L, allHistVariableQuery.count());
    assertEquals(0L, allDetailsQuery.count());
    assertEquals(1L, variableQuery.count());
    assertEquals(1L, secondVariableQuery.count());
    assertEquals("testValue2", variableQuery.singleResult().getValue());
    assertEquals("testValue2", secondVariableQuery.singleResult().getValue());

    // when
    runtimeService.setVariable(executionId, "myVariable", "testValue3");
    runtimeService.setVariable(executionId, "mySecondVariable", "testValue3");
    
    // then
    HistoricVariableInstanceQuery histVariableQuery = historyService.createHistoricVariableInstanceQuery().processInstanceId(executionId).variableName("myVariable");
    HistoricVariableInstanceQuery secondHistVariableQuery = historyService.createHistoricVariableInstanceQuery().processInstanceId(executionId).variableName("mySecondVariable");
    HistoricDetailQuery detailsQuery = historyService.createHistoricDetailQuery().processInstanceId(executionId).variableInstanceId(histVariableQuery.singleResult().getId());
    HistoricDetailQuery secondDetailsQuery = historyService.createHistoricDetailQuery().processInstanceId(executionId).variableInstanceId(secondHistVariableQuery.singleResult().getId());
    assertEquals(1L, histVariableQuery.count());
    assertEquals(1L, secondHistVariableQuery.count());
    assertEquals(2L, allHistVariableQuery.count());
    assertEquals(1L, detailsQuery.count());
    assertEquals(1L, secondDetailsQuery.count());
    assertEquals(2L, allDetailsQuery.count());
    assertEquals(1L, variableQuery.count());
    assertEquals(1L, secondVariableQuery.count());
    assertEquals("testValue3", variableQuery.singleResult().getValue());
    assertEquals("testValue3", secondVariableQuery.singleResult().getValue());
  }
  
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testDeleteAllHistoricVariablesOnEmpty() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS);
    String executionId = processInstance.getId();
    assertEquals(1L, runtimeService.createProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());

    runtimeService.deleteProcessInstance(executionId, null);
    assertEquals(0L, runtimeService.createProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
    assertEquals(1L, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());

    HistoricVariableInstanceQuery histVariableQuery = historyService.createHistoricVariableInstanceQuery().processInstanceId(executionId);
    assertEquals(0L, histVariableQuery.count());

    HistoricDetailQuery detailsQuery = historyService.createHistoricDetailQuery().processInstanceId(executionId);
    assertEquals(0L, detailsQuery.count());

    // when
    historyService.deleteHistoricVariableInstancesByProcessInstanceId(executionId);

    // then
    assertEquals(0, histVariableQuery.count());
    assertEquals(0, detailsQuery.count());
  }

  public void testDeleteAllHistoricVariablesOnUnkownProcessInstance() {
    try {
      // when
      historyService.deleteHistoricVariableInstancesByProcessInstanceId("fakeID");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      // then
      assertTextPresent("No historic process instance found with id: fakeID", ae.getMessage());
    }
  }

  public void testDeleteAllHistoricVariablesWithNull() {
    try {
      // when
      historyService.deleteHistoricVariableInstancesByProcessInstanceId(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      // then
      assertTextPresent("processInstanceId is null", ae.getMessage());
    }
  }

  protected List<String> prepareHistoricProcesses() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS);

    List<String> processInstanceIds = new ArrayList<String>(Arrays.asList(
        new String[]{processInstance.getId(), processInstance2.getId()}));
    runtimeService.deleteProcessInstances(processInstanceIds, null, true, true);

    return processInstanceIds;
  }
}
