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
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.authorization.AuthorizationTest;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import static org.camunda.bpm.engine.authorization.Permissions.READ_HISTORY;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE_TASK;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.TASK;

/**
 * Tests the operationId field in historic tables, which helps to correlate records from different tables.
 *
 * @author Svetlana Dorokhova
 *
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class OperationIdAuthorizationTest extends AuthorizationTest {

  protected static final String PROCESS_KEY = "oneTaskProcess";
  protected String deploymentId;

  @Override
  public void setUp() throws Exception {
    deploymentId = createDeployment(null,
        "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml").getId();
    super.setUp();
  }

  @Override
  public void tearDown() {
    super.tearDown();
    deleteDeployment(deploymentId);
  }

  public void testResolveTaskOperationId() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, taskId, userId, UPDATE);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_TASK, READ_HISTORY);

    setAssignee(taskId, userId);
    delegateTask(taskId, "demo");

    String delegateOperationId = historyService.createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_DELEGATE).taskId(taskId)
        .list().get(0).getOperationId();

    // when
    taskService.resolveTask(taskId, getVariables());

    //then
    List<UserOperationLogEntry> userOperationLogEntries = historyService.createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_RESOLVE)
        .taskId(taskId)
        .list();
    List<HistoricDetail> historicDetails = historyService.createHistoricDetailQuery().taskId(taskId).list();
    String resolveOperationId = verifySameOperationId(userOperationLogEntries, historicDetails);
    assertNotSame("Different operations must have different operationId in log.", resolveOperationId, delegateOperationId);
  }

  public void testSubmitTaskFormOperationId() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, taskId, userId, UPDATE);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_TASK, READ_HISTORY);

    setAssignee(taskId, userId);
    delegateTask(taskId, "demo");

    String delegateOperationId = historyService.createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_DELEGATE).taskId(taskId)
        .list().get(0).getOperationId();

    // when
    formService.submitTaskForm(taskId, getVariables());

    //then
    List<UserOperationLogEntry> userOperationLogEntries = historyService.createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_RESOLVE)
        .taskId(taskId)
        .list();
    List<HistoricDetail> historicDetails = historyService.createHistoricDetailQuery().taskId(taskId).list();
    String updateOperationId = verifySameOperationId(userOperationLogEntries, historicDetails);
    assertNotSame("Different operations must have different operationId in log.", updateOperationId, delegateOperationId);
  }

  public void testSetTaskVariablesOperationId() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, taskId, userId, UPDATE);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_TASK, READ_HISTORY);

    setAssignee(taskId, userId);
    delegateTask(taskId, "demo");

    String delegateOperationId = historyService.createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_DELEGATE).taskId(taskId)
        .list().get(0).getOperationId();

    // when
    taskService.setVariables(taskId, getVariables());

    //then
    List<UserOperationLogEntry> userOperationLogEntries = historyService.createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_SET_VARIABLE)
        .taskId(taskId)
        .list();
    List<HistoricDetail> historicDetails = historyService.createHistoricDetailQuery().taskId(taskId).list();
    String updateOperationId = verifySameOperationId(userOperationLogEntries, historicDetails);
    assertNotSame("Different operations must have different operationId in log.", updateOperationId, delegateOperationId);
  }

  private String verifySameOperationId(List<UserOperationLogEntry> userOperationLogEntries, List<HistoricDetail> historicDetails) {
    assertTrue("Operation log entry must exist", userOperationLogEntries.size() > 0);
    String operationId = userOperationLogEntries.get(0).getOperationId();
    assertNotNull(operationId);
    for (UserOperationLogEntry userOperationLogEntry: userOperationLogEntries) {
      assertEquals("OperationIds must be the same", operationId, userOperationLogEntry.getOperationId());
    }
    for (HistoricDetail historicDetail : historicDetails) {
      assertEquals("OperationIds must be the same", operationId, historicDetail.getOperationId());
    }
    return operationId;
  }

  protected VariableMap getVariables() {
    return Variables.createVariables()
        .putValue(VARIABLE_NAME, VARIABLE_VALUE)
        .putValue("anotherVariableName", "anotherVariableValue");
  }

}
