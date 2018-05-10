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

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricDetailQuery;
import org.camunda.bpm.engine.history.HistoricVariableUpdate;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.cmmn.decisiontask.TestPojo;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 * @author Svetlana Dorokhova
 *
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoricDetailQueryTest {

  protected static final String PROCESS_KEY = "oneTaskProcess";

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain chain = RuleChain.outerRule(engineRule).around(testHelper);


  protected RuntimeService runtimeService;
  protected ManagementService managementService;
  protected HistoryService historyService;
  protected TaskService taskService;
  private IdentityService identityService;

  @Before
  public void initServices() {
    runtimeService = engineRule.getRuntimeService();
    managementService = engineRule.getManagementService();
    historyService = engineRule.getHistoryService();
    taskService = engineRule.getTaskService();
    identityService = engineRule.getIdentityService();
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryByUserOperationId() {
    startProcessInstance(PROCESS_KEY);

    identityService.setAuthenticatedUserId("demo");

    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.resolveTask(taskId, getVariables());

    //then
    String userOperationId = historyService.createHistoricDetailQuery().singleResult().getUserOperationId();

    HistoricDetailQuery query = historyService.createHistoricDetailQuery()
            .userOperationId(userOperationId);

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryByInvalidUserOperationId() {
    startProcessInstance(PROCESS_KEY);

    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.resolveTask(taskId, getVariables());

    //then
    HistoricDetailQuery query = historyService.createHistoricDetailQuery()
            .userOperationId("invalid");

    assertEquals(0, query.list().size());
    assertEquals(0, query.count());

    try {
      query.userOperationId(null);
      fail("It was possible to set a null value as userOperationId.");
    } catch (ProcessEngineException e) { }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryByExecutionId() {
    startProcessInstance(PROCESS_KEY);

    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.resolveTask(taskId, getVariables());

    //then
    String executionId = historyService.createHistoricDetailQuery().singleResult().getExecutionId();

    HistoricDetailQuery query = historyService.createHistoricDetailQuery()
            .executionId(executionId);

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryByInvalidExecutionId() {
    startProcessInstance(PROCESS_KEY);

    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.resolveTask(taskId, getVariables());

    //then
    HistoricDetailQuery query = historyService.createHistoricDetailQuery()
            .executionId("invalid");

    assertEquals(0, query.list().size());
    assertEquals(0, query.count());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByVariableTypeIn() {
    // given
    Map<String, Object> variables1 = new HashMap<String, Object>();
    variables1.put("stringVar", "test");
    variables1.put("boolVar", true);
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables1);

    // when
    HistoricDetailQuery query =
      historyService.createHistoricDetailQuery()
        .variableTypeIn("string");

    // then
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
    HistoricDetail historicDetail = query.list().get(0);
    if (historicDetail instanceof HistoricVariableUpdate) {
      HistoricVariableUpdate variableUpdate = (HistoricVariableUpdate) historicDetail;
      assertEquals(variableUpdate.getVariableName(), "stringVar");
      assertEquals(variableUpdate.getTypeName(), "string");
    } else {
      fail("Historic detail should be a variable update!");
    }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByVariableTypeInWithCapitalLetter() {
    // given
    Map<String, Object> variables1 = new HashMap<String, Object>();
    variables1.put("stringVar", "test");
    variables1.put("boolVar", true);
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables1);

    // when
    HistoricDetailQuery query =
      historyService.createHistoricDetailQuery()
        .variableTypeIn("Boolean");

    // then
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
    HistoricDetail historicDetail = query.list().get(0);
    if (historicDetail instanceof HistoricVariableUpdate) {
      HistoricVariableUpdate variableUpdate = (HistoricVariableUpdate) historicDetail;
      assertEquals(variableUpdate.getVariableName(), "boolVar");
      assertEquals(variableUpdate.getTypeName(), "boolean");
    } else {
      fail("Historic detail should be a variable update!");
    }
  }

  @Test
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
    HistoricDetailQuery query =
      historyService.createHistoricDetailQuery()
        .variableTypeIn("boolean", "integer", "Serializable");

    // then
    assertEquals(3, query.list().size());
    assertEquals(3, query.count());
    Set<String> allowedVariableTypes = new HashSet<String>();
    allowedVariableTypes.add("boolean");
    allowedVariableTypes.add("integer");
    allowedVariableTypes.add("object");
    for (HistoricDetail detail : query.list()) {
      if (detail instanceof HistoricVariableUpdate) {
        HistoricVariableUpdate variableUpdate = (HistoricVariableUpdate) detail;
        assertTrue(allowedVariableTypes.contains(variableUpdate.getTypeName()));
      } else {
        fail("Historic detail should be a variable update!");
      }
    }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByInvalidVariableTypeIn() {
    // given
    Map<String, Object> variables1 = new HashMap<String, Object>();
    variables1.put("stringVar", "test");
    variables1.put("boolVar", true);
    variables1.put("intVar", 5);
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables1);

    // when
    HistoricDetailQuery query =
      historyService.createHistoricDetailQuery()
        .variableTypeIn("invalid");

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

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryBySingleProcessInstanceId() {
    // given
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("stringVar", "test");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_KEY, variables);

    // when
    HistoricDetailQuery query =
      historyService.createHistoricDetailQuery()
        .variableUpdates()
        .processInstanceIdIn(processInstance.getProcessInstanceId());

    // then
    assertEquals(1, query.count());
    assertEquals(query.list().get(0).getProcessInstanceId(), processInstance.getId());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryBySeveralProcessInstanceIds() {
    // given
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("stringVar", "test");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_KEY, variables);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey(PROCESS_KEY, variables);
    runtimeService.startProcessInstanceByKey(PROCESS_KEY, variables);

    // when
    HistoricDetailQuery query =
      historyService.createHistoricDetailQuery()
        .variableUpdates()
        .processInstanceIdIn(processInstance.getProcessInstanceId(), processInstance2.getProcessInstanceId());

    // then
    Set<String> expectedProcessInstanceIds = new HashSet<String>();
    expectedProcessInstanceIds.add(processInstance.getId());
    expectedProcessInstanceIds.add(processInstance2.getId());
    assertEquals(2, query.count());
    assertTrue(expectedProcessInstanceIds.contains(query.list().get(0).getProcessInstanceId()));
    assertTrue(expectedProcessInstanceIds.contains(query.list().get(1).getProcessInstanceId()));
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNonExistingProcessInstanceId() {
    // given
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("stringVar", "test");
    runtimeService.startProcessInstanceByKey(PROCESS_KEY, variables);

    // when
    HistoricDetailQuery query =
      historyService.createHistoricDetailQuery()
        .processInstanceIdIn("foo");

    // then
    assertEquals(0, query.count());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByInvalidProcessInstanceIds() {
    // given
    Map<String, Object> variables1 = new HashMap<String, Object>();
    variables1.put("stringVar", "test");
    runtimeService.startProcessInstanceByKey(PROCESS_KEY, variables1);

    // when
    HistoricDetailQuery query =
      historyService.createHistoricDetailQuery();

    try {
      // when
      query.processInstanceIdIn(null);
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
      // then fails
    }

    try {
      // when
      query.processInstanceIdIn((String)null);
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
      // then fails
    }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryByOccurredBefore() {
    // given
    Calendar startTime = Calendar.getInstance();
    ClockUtil.setCurrentTime(startTime.getTime());

    Calendar hourAgo = Calendar.getInstance();
    hourAgo.add(Calendar.HOUR_OF_DAY, -1);
    Calendar hourFromNow = Calendar.getInstance();
    hourFromNow.add(Calendar.HOUR_OF_DAY, 1);

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("stringVar", "test");
    runtimeService.startProcessInstanceByKey(PROCESS_KEY, variables);

    // when
    HistoricDetailQuery query =
      historyService.createHistoricDetailQuery();

    // then
    assertEquals(1, query.occurredBefore(hourFromNow.getTime()).count());
    assertEquals(0, query.occurredBefore(hourAgo.getTime()).count());

  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryByOccurredAfter() {
    // given
    Calendar startTime = Calendar.getInstance();
    ClockUtil.setCurrentTime(startTime.getTime());

    Calendar hourAgo = Calendar.getInstance();
    hourAgo.add(Calendar.HOUR_OF_DAY, -1);
    Calendar hourFromNow = Calendar.getInstance();
    hourFromNow.add(Calendar.HOUR_OF_DAY, 1);

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("stringVar", "test");
    runtimeService.startProcessInstanceByKey(PROCESS_KEY, variables);

    // when
    HistoricDetailQuery query =
      historyService.createHistoricDetailQuery();

    // then
    assertEquals(0, query.occurredAfter(hourFromNow.getTime()).count());
    assertEquals(1, query.occurredAfter(hourAgo.getTime()).count());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryByOccurredAfterAndOccurredBefore() {
    // given
    Calendar startTime = Calendar.getInstance();
    ClockUtil.setCurrentTime(startTime.getTime());

    Calendar hourAgo = Calendar.getInstance();
    hourAgo.add(Calendar.HOUR_OF_DAY, -1);
    Calendar hourFromNow = Calendar.getInstance();
    hourFromNow.add(Calendar.HOUR_OF_DAY, 1);

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("stringVar", "test");
    runtimeService.startProcessInstanceByKey(PROCESS_KEY, variables);

    // when
    HistoricDetailQuery query =
      historyService.createHistoricDetailQuery();

    // then
    assertEquals(0, query.occurredAfter(hourFromNow.getTime()).occurredBefore(hourFromNow.getTime()).count());
    assertEquals(1, query.occurredAfter(hourAgo.getTime()).occurredBefore(hourFromNow.getTime()).count());
    assertEquals(0, query.occurredAfter(hourFromNow.getTime()).occurredBefore(hourAgo.getTime()).count());
    assertEquals(0, query.occurredAfter(hourAgo.getTime()).occurredBefore(hourAgo.getTime()).count());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByInvalidOccurredBeforeDate() {
    // given
    Map<String, Object> variables1 = new HashMap<String, Object>();
    variables1.put("stringVar", "test");
    runtimeService.startProcessInstanceByKey(PROCESS_KEY, variables1);

    // when
    HistoricDetailQuery query =
      historyService.createHistoricDetailQuery();

    try {
      // when
      query.occurredBefore(null);
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
      // then fails
    }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByInvalidOccurredAfterDate() {
    // given
    Map<String, Object> variables1 = new HashMap<String, Object>();
    variables1.put("stringVar", "test");
    runtimeService.startProcessInstanceByKey(PROCESS_KEY, variables1);

    // when
    HistoricDetailQuery query =
      historyService.createHistoricDetailQuery();

    try {
      // when
      query.occurredAfter(null);
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
      // then fails
    }
  }

  protected VariableMap getVariables() {
    return Variables.createVariables()
            .putValue("aVariableName", "aVariableValue");
  }

  protected void startProcessInstance(String key) {
    startProcessInstances(key, 1);
  }

  protected void startProcessInstances(String key, int numberOfInstances) {
    for (int i = 0; i < numberOfInstances; i++) {
      runtimeService.startProcessInstanceByKey(key);
    }

    testHelper.executeAvailableJobs();
  }
}
