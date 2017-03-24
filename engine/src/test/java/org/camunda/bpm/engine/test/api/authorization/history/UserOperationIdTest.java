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

import java.util.List;

import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.authorization.AuthorizationTest;
import org.camunda.bpm.engine.test.history.DecisionServiceDelegate;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.camunda.bpm.engine.authorization.Permissions.READ_HISTORY;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE_TASK;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.TASK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

    identityService.setAuthenticatedUserId("demo");
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testResolveTaskOperationId() {
    // given
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
