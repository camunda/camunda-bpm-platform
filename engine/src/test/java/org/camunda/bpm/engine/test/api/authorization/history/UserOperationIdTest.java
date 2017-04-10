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
package org.camunda.bpm.engine.test.api.authorization.history;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

/**
 * Tests the operationId field in historic tables, which helps to correlate records from different tables.
 *
 * @author Svetlana Dorokhova
 *
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class UserOperationIdTest {

  @Rule
  public ProcessEngineRule engineRule = new ProcessEngineRule();

  @Rule
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  protected static final String PROCESS_KEY = "oneTaskProcess";
  protected String deploymentId;

  protected RuntimeService runtimeService;
  protected RepositoryService repositoryService;
  protected HistoryService historyService;
  protected TaskService taskService;
  protected FormService formService;
  protected IdentityService identityService;

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    repositoryService = engineRule.getRepositoryService();
    historyService = engineRule.getHistoryService();
    taskService = engineRule.getTaskService();
    formService = engineRule.getFormService();
    identityService = engineRule.getIdentityService();

  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testResolveTaskOperationId() {
    // given
    identityService.setAuthenticatedUserId("demo");
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.resolveTask(taskId, getVariables());

    //then
    List<UserOperationLogEntry> userOperationLogEntries = historyService.createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_RESOLVE)
        .taskId(taskId)
        .list();
    List<HistoricDetail> historicDetails = historyService.createHistoricDetailQuery().list();
    verifySameOperationId(userOperationLogEntries, historicDetails);
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testSubmitTaskFormOperationId() {
    // given
    identityService.setAuthenticatedUserId("demo");
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    formService.submitTaskForm(taskId, getVariables());

    //then
    List<UserOperationLogEntry> userOperationLogEntries = historyService.createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_COMPLETE)
        .taskId(taskId)
        .list();
    List<HistoricDetail> historicDetails = historyService.createHistoricDetailQuery().list();
    verifySameOperationId(userOperationLogEntries, historicDetails);
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testSetTaskVariablesOperationId() {
    // given
    identityService.setAuthenticatedUserId("demo");
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.setVariables(taskId, getVariables());

    //then
    List<UserOperationLogEntry> userOperationLogEntries = historyService.createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_SET_VARIABLE)
        .taskId(taskId)
        .list();
    List<HistoricDetail> historicDetails = historyService.createHistoricDetailQuery().list();
    verifySameOperationId(userOperationLogEntries, historicDetails);
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testWithoutAuthentication() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.resolveTask(taskId, getVariables());

    //then
    List<UserOperationLogEntry> userOperationLogEntries = historyService.createUserOperationLogQuery()
        .taskId(taskId)
        .list();
    assertEquals(0, userOperationLogEntries.size());
    List<HistoricDetail> historicDetails = historyService.createHistoricDetailQuery().list();
    assertTrue(historicDetails.size() > 0);
    //history detail records must have null userOperationId as user operation log was not created
    for (HistoricDetail historicDetail: historicDetails) {
      assertNull(historicDetail.getUserOperationId());
    }
  }

  @Test
  public void testSetTaskVariablesInServiceTask() {
    // given
    BpmnModelInstance bpmnModelInstance = Bpmn.createExecutableProcess(PROCESS_KEY)
        .startEvent()
        .userTask()
        .serviceTask()
          .camundaExpression("${execution.setVariable('foo', 'bar')}")
        .endEvent()
        .done();
    testRule.deploy(bpmnModelInstance);

    identityService.setAuthenticatedUserId("demo");
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().singleResult();

    // when
    taskService.complete(task.getId());

    //then
    HistoricDetail historicDetail = historyService.createHistoricDetailQuery().singleResult();
    // no user operation log id is set for this update, as it is not written as part of the user operation
    assertNull(historicDetail.getUserOperationId());
  }

  private void verifySameOperationId(List<UserOperationLogEntry> userOperationLogEntries, List<HistoricDetail> historicDetails) {
    assertTrue("Operation log entry must exist", userOperationLogEntries.size() > 0);
    String operationId = userOperationLogEntries.get(0).getOperationId();
    assertNotNull(operationId);
    assertTrue("Some historic details are expected to be present", historicDetails.size() > 0);
    for (UserOperationLogEntry userOperationLogEntry: userOperationLogEntries) {
      assertEquals("OperationIds must be the same", operationId, userOperationLogEntry.getOperationId());
    }
    for (HistoricDetail historicDetail : historicDetails) {
      assertEquals("OperationIds must be the same", operationId, historicDetail.getUserOperationId());
    }
  }

  protected VariableMap getVariables() {
    return Variables.createVariables()
        .putValue("aVariableName", "aVariableValue")
        .putValue("anotherVariableName", "anotherVariableValue");
  }

}
