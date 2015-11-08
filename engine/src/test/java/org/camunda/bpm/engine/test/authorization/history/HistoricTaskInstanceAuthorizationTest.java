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
package org.camunda.bpm.engine.test.authorization.history;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.DELETE_HISTORY;
import static org.camunda.bpm.engine.authorization.Permissions.READ_HISTORY;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;

import java.util.List;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstanceQuery;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.authorization.AuthorizationTest;

/**
 * @author Roman Smirnov
 *
 */
public class HistoricTaskInstanceAuthorizationTest extends AuthorizationTest {

  protected static final String PROCESS_KEY = "oneTaskProcess";
  protected static final String MESSAGE_START_PROCESS_KEY = "messageStartProcess";
  protected static final String CASE_KEY = "oneTaskCase";

  protected String deploymentId;

  public void setUp() throws Exception {
    deploymentId = createDeployment(null,
        "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/authorization/messageStartEventProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/authorization/oneTaskCase.cmmn").getId();
    super.setUp();
  }

  public void tearDown() {
    super.tearDown();
    deleteDeployment(deploymentId);
  }

  // historic task instance query (standalone task) ///////////////////////////////////////

  public void testQueryAfterStandaloneTask() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    // when
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    // then
    verifyQueryResults(query, 1);

    deleteTask(taskId, true);
  }

  // historic task instance query (process task) //////////////////////////////////////////

  public void testSimpleQueryWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);

    // when
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    // then
    verifyQueryResults(query, 0);
  }

  public void testSimpleQueryWithReadHistoryPermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY);

    // when
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    // then
    verifyQueryResults(query, 1);
  }

  public void testSimpleQueryWithReadHistoryPermissionOnAnyProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    // then
    verifyQueryResults(query, 1);
  }

  // historic task instance query (multiple process instances) ////////////////////////

  public void testQueryWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);

    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);

    // when
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    // then
    verifyQueryResults(query, 0);
  }

  public void testQueryWithReadHistoryPermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);

    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY);

    // when
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    // then
    verifyQueryResults(query, 3);
  }

  public void testQueryWithReadHistoryPermissionOnAnyProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);

    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    // then
    verifyQueryResults(query, 7);
  }

  // historic task instance query (case task) ///////////////////////////////////////

  public void testQueryAfterCaseTask() {
    // given
    createCaseInstanceByKey(CASE_KEY);

    // when
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    // then
    verifyQueryResults(query, 1);
  }

  // historic task instance query (mixed tasks) ////////////////////////////////////

  public void testMixedQueryWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);

    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);

    createTask("one");
    createTask("two");
    createTask("three");
    createTask("four");
    createTask("five");

    createCaseInstanceByKey(CASE_KEY);
    createCaseInstanceByKey(CASE_KEY);

    // when
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    // then
    verifyQueryResults(query, 7);

    deleteTask("one", true);
    deleteTask("two", true);
    deleteTask("three", true);
    deleteTask("four", true);
    deleteTask("five", true);
  }

  public void testMixedQueryWithReadHistoryPermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);

    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);

    createTask("one");
    createTask("two");
    createTask("three");
    createTask("four");
    createTask("five");

    createCaseInstanceByKey(CASE_KEY);
    createCaseInstanceByKey(CASE_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY);

    // when
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    // then
    verifyQueryResults(query, 10);

    deleteTask("one", true);
    deleteTask("two", true);
    deleteTask("three", true);
    deleteTask("four", true);
    deleteTask("five", true);
  }

  public void testMixedQueryWithReadHistoryPermissionOnAnyProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);

    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);

    createTask("one");
    createTask("two");
    createTask("three");
    createTask("four");
    createTask("five");

    createCaseInstanceByKey(CASE_KEY);
    createCaseInstanceByKey(CASE_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    // then
    verifyQueryResults(query, 14);

    deleteTask("one", true);
    deleteTask("two", true);
    deleteTask("three", true);
    deleteTask("four", true);
    deleteTask("five", true);
  }

  // delete deployment (cascade = false)

  public void testQueryAfterDeletingDeployment() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY);

    disableAuthorization();
    List<Task> tasks = taskService.createTaskQuery().list();
    for (Task task : tasks) {
      taskService.complete(task.getId());
    }
    enableAuthorization();

    disableAuthorization();
    repositoryService.deleteDeployment(deploymentId);
    enableAuthorization();

    // when
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    // then
    verifyQueryResults(query, 3);

    disableAuthorization();
    List<HistoricProcessInstance> instances = historyService.createHistoricProcessInstanceQuery().list();
    for (HistoricProcessInstance instance : instances) {
      historyService.deleteHistoricProcessInstance(instance.getId());
    }
    enableAuthorization();
  }

  // delete historic task (standalone task) ///////////////////////

  public void testDeleteStandaloneTask() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    // when
    historyService.deleteHistoricTaskInstance(taskId);

    // then
    disableAuthorization();
    HistoricTaskInstanceQuery query = historyService
        .createHistoricTaskInstanceQuery()
        .taskId(taskId);
    verifyQueryResults(query, 0);
    enableAuthorization();

    deleteTask(taskId, true);
  }

  // delete historic task (process task) ///////////////////////

  public void testDeleteProcessTaskWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    try {
      // when
      historyService.deleteHistoricTaskInstance(taskId);
      fail("Exception expected: It should not be possible to delete the historic task instance");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(DELETE_HISTORY.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testDeleteProcessTaskWithDeleteHistoryPermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, DELETE_HISTORY);

    // when
    historyService.deleteHistoricTaskInstance(taskId);

    // then
    disableAuthorization();
    HistoricTaskInstanceQuery query = historyService
        .createHistoricTaskInstanceQuery()
        .taskId(taskId);
    verifyQueryResults(query, 0);
    enableAuthorization();
  }

  public void testDeleteProcessTaskWithDeleteHistoryPermissionOnAnyProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, DELETE_HISTORY);

    // when
    historyService.deleteHistoricTaskInstance(taskId);

    // then
    disableAuthorization();
    HistoricTaskInstanceQuery query = historyService
        .createHistoricTaskInstanceQuery()
        .taskId(taskId);
    verifyQueryResults(query, 0);
    enableAuthorization();
  }

  public void testDeleteHistoricTaskInstanceAfterDeletingDeployment() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, DELETE_HISTORY);

    disableAuthorization();
    repositoryService.deleteDeployment(deploymentId);
    enableAuthorization();

    // when
    historyService.deleteHistoricTaskInstance(taskId);

    // then
    disableAuthorization();
    HistoricTaskInstanceQuery query = historyService
        .createHistoricTaskInstanceQuery()
        .taskId(taskId);
    verifyQueryResults(query, 0);
    enableAuthorization();

    disableAuthorization();
    historyService.deleteHistoricProcessInstance(processInstanceId);
    enableAuthorization();
  }

  // helper ////////////////////////////////////////////////////////

  protected void verifyQueryResults(HistoricTaskInstanceQuery query, int countExpected) {
    verifyQueryResults((AbstractQuery<?, ?>) query, countExpected);
  }

}
