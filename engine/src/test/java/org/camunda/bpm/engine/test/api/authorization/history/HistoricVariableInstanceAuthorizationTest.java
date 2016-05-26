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

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.READ_HISTORY;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.authorization.AuthorizationTest;

/**
 * @author Roman Smirnov
 *
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoricVariableInstanceAuthorizationTest extends AuthorizationTest {

  protected static final String PROCESS_KEY = "oneTaskProcess";
  protected static final String MESSAGE_START_PROCESS_KEY = "messageStartProcess";
  protected static final String CASE_KEY = "oneTaskCase";

  protected String deploymentId;

  @Override
  public void setUp() throws Exception {
    deploymentId = createDeployment(null,
        "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/authorization/messageStartEventProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/authorization/oneTaskCase.cmmn").getId();
    super.setUp();
  }

  @Override
  public void tearDown() {
    super.tearDown();
    deleteDeployment(deploymentId);
  }

  // historic variable instance query (standalone task) /////////////////////////////////////////////

  public void testQueryAfterStandaloneTaskVariables() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    disableAuthorization();
    taskService.setVariables(taskId, getVariables());
    enableAuthorization();

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    verifyQueryResults(query, 1);

    deleteTask(taskId, true);
  }

  // historic variable instance query (process variables) ///////////////////////////////////////////

  public void testSimpleQueryWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY, getVariables());

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    verifyQueryResults(query, 0);
  }

  public void testSimpleQueryWithReadHistoryPermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY);

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    verifyQueryResults(query, 1);
  }

  public void testSimpleQueryWithReadHistoryPermissionOnAnyProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    verifyQueryResults(query, 1);
  }

  public void testSimpleQueryWithMultiple() {
    // given
    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY);

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    verifyQueryResults(query, 1);
  }

  // historic variable instance query (multiple process instances) ////////////////////////

  public void testQueryWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    startProcessInstanceByKey(PROCESS_KEY, getVariables());

    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY, getVariables());
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY, getVariables());
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY, getVariables());
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY, getVariables());

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    verifyQueryResults(query, 0);
  }

  public void testQueryWithReadHistoryPermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    startProcessInstanceByKey(PROCESS_KEY, getVariables());

    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY, getVariables());
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY, getVariables());
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY, getVariables());
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY, getVariables());

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY);

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    verifyQueryResults(query, 3);
  }

  public void testQueryWithReadHistoryPermissionOnAnyProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    startProcessInstanceByKey(PROCESS_KEY, getVariables());

    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY, getVariables());
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY, getVariables());
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY, getVariables());
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY, getVariables());

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    verifyQueryResults(query, 7);
  }

  // historic variable instance query (case variables) /////////////////////////////////////////////

  public void testQueryAfterCaseVariables() {
    // given
    createCaseInstanceByKey(CASE_KEY, getVariables());

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    verifyQueryResults(query, 1);
  }

  // historic variable instance query (mixed variables) ////////////////////////////////////

  public void testMixedQueryWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    startProcessInstanceByKey(PROCESS_KEY, getVariables());

    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY, getVariables());
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY, getVariables());
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY, getVariables());
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY, getVariables());

    createTask("one");
    createTask("two");
    createTask("three");
    createTask("four");
    createTask("five");

    disableAuthorization();
    taskService.setVariables("one", getVariables());
    taskService.setVariables("two", getVariables());
    taskService.setVariables("three", getVariables());
    taskService.setVariables("four", getVariables());
    taskService.setVariables("five", getVariables());
    enableAuthorization();

    createCaseInstanceByKey(CASE_KEY, getVariables());
    createCaseInstanceByKey(CASE_KEY, getVariables());

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

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
    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    startProcessInstanceByKey(PROCESS_KEY, getVariables());

    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY, getVariables());
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY, getVariables());
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY, getVariables());
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY, getVariables());

    createTask("one");
    createTask("two");
    createTask("three");
    createTask("four");
    createTask("five");

    disableAuthorization();
    taskService.setVariables("one", getVariables());
    taskService.setVariables("two", getVariables());
    taskService.setVariables("three", getVariables());
    taskService.setVariables("four", getVariables());
    taskService.setVariables("five", getVariables());
    enableAuthorization();

    createCaseInstanceByKey(CASE_KEY, getVariables());
    createCaseInstanceByKey(CASE_KEY, getVariables());

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY);

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

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
    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    startProcessInstanceByKey(PROCESS_KEY, getVariables());

    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY, getVariables());
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY, getVariables());
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY, getVariables());
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY, getVariables());

    createTask("one");
    createTask("two");
    createTask("three");
    createTask("four");
    createTask("five");

    disableAuthorization();
    taskService.setVariables("one", getVariables());
    taskService.setVariables("two", getVariables());
    taskService.setVariables("three", getVariables());
    taskService.setVariables("four", getVariables());
    taskService.setVariables("five", getVariables());
    enableAuthorization();

    createCaseInstanceByKey(CASE_KEY, getVariables());
    createCaseInstanceByKey(CASE_KEY, getVariables());

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

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
    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    startProcessInstanceByKey(PROCESS_KEY, getVariables());
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
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    verifyQueryResults(query, 3);

    disableAuthorization();
    List<HistoricProcessInstance> instances = historyService.createHistoricProcessInstanceQuery().list();
    for (HistoricProcessInstance instance : instances) {
      historyService.deleteHistoricProcessInstance(instance.getId());
    }
    enableAuthorization();
  }

  // helper ////////////////////////////////////////////////////////

  protected void verifyQueryResults(HistoricVariableInstanceQuery query, int countExpected) {
    verifyQueryResults((AbstractQuery<?, ?>) query, countExpected);
  }

}
