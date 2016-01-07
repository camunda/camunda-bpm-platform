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
import static org.camunda.bpm.engine.authorization.Permissions.DELETE_HISTORY;
import static org.camunda.bpm.engine.authorization.Permissions.READ_HISTORY;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;

import java.util.List;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.authorization.MissingAuthorization;
import org.camunda.bpm.engine.history.DurationReportResult;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.query.PeriodUnit;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.api.authorization.AuthorizationTest;

/**
 * @author Roman Smirnov
 *
 */
public class HistoricProcessInstanceAuthorizationTest extends AuthorizationTest {

  protected static final String PROCESS_KEY = "oneTaskProcess";
  protected static final String MESSAGE_START_PROCESS_KEY = "messageStartProcess";

  protected String deploymentId;

  public void setUp() throws Exception {
    deploymentId = createDeployment(null,
        "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/authorization/messageStartEventProcess.bpmn20.xml").getId();
    super.setUp();
  }

  public void tearDown() {
    super.tearDown();
    deleteDeployment(deploymentId);
  }

  // historic process instance query //////////////////////////////////////////////////////////

  public void testSimpleQueryWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);

    // when
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // then
    verifyQueryResults(query, 0);
  }

  public void testSimpleQueryWithReadHistoryPermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY);

    // when
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // then
    verifyQueryResults(query, 1);

    HistoricProcessInstance instance = query.singleResult();
    assertNotNull(instance);
    assertEquals(processInstanceId, instance.getId());
  }

  public void testSimpleQueryWithReadHistoryPermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // then
    verifyQueryResults(query, 1);

    HistoricProcessInstance instance = query.singleResult();
    assertNotNull(instance);
    assertEquals(processInstanceId, instance.getId());
  }

  // historic process instance query (multiple process instances) ////////////////////////

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
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

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
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

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
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // then
    verifyQueryResults(query, 7);
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
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // then
    verifyQueryResults(query, 3);

    disableAuthorization();
    List<HistoricProcessInstance> instances = historyService.createHistoricProcessInstanceQuery().list();
    for (HistoricProcessInstance instance : instances) {
      historyService.deleteHistoricProcessInstance(instance.getId());
    }
    enableAuthorization();
  }

  // delete historic process instance //////////////////////////////

  public void testDeleteHistoricProcessInstanceWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    try {
      // when
      historyService.deleteHistoricProcessInstance(processInstanceId);
      fail("Exception expected: It should not be possible to delete the historic process instance");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(DELETE_HISTORY.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testDeleteHistoricProcessInstanceWithDeleteHistoryPermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, DELETE_HISTORY);

    // when
    historyService.deleteHistoricProcessInstance(processInstanceId);

    // then
    disableAuthorization();
    long count = historyService
        .createHistoricProcessInstanceQuery()
        .processInstanceId(processInstanceId)
        .count();
    assertEquals(0, count);
    enableAuthorization();
  }

  public void testDeleteHistoricProcessInstanceWithDeleteHistoryPermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, DELETE_HISTORY);

    // when
    historyService.deleteHistoricProcessInstance(processInstanceId);

    // then
    disableAuthorization();
    long count = historyService
        .createHistoricProcessInstanceQuery()
        .processInstanceId(processInstanceId)
        .count();
    assertEquals(0, count);
    enableAuthorization();
  }

  public void testDeleteHistoricProcessInstanceAfterDeletingDeployment() {
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
    historyService.deleteHistoricProcessInstance(processInstanceId);

    // then
    disableAuthorization();
    long count = historyService
        .createHistoricProcessInstanceQuery()
        .processInstanceId(processInstanceId)
        .count();
    assertEquals(0, count);
    enableAuthorization();
  }

  // create historic process instance report

  public void testHistoricProcessInstanceReportWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    try {
      // when
      historyService
          .createHistoricProcessInstanceReport()
          .duration(PeriodUnit.MONTH);
      fail("Exception expected: It should not be possible to create a historic process instance report");
    } catch (AuthorizationException e) {
      // then
      List<MissingAuthorization> missingAuthorizations = e.getMissingAuthorizations();
      assertEquals(1, missingAuthorizations.size());

      MissingAuthorization missingAuthorization = missingAuthorizations.get(0);
      assertEquals(READ_HISTORY.toString(), missingAuthorization.getViolatedPermissionName());
      assertEquals(PROCESS_DEFINITION.resourceName(), missingAuthorization.getResourceType());
      assertEquals(ANY, missingAuthorization.getResourceId());
    }
  }

  public void testHistoricProcessInstanceReportWithHistoryReadPermissionOnAny() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    List<DurationReportResult> result = historyService
        .createHistoricProcessInstanceReport()
        .duration(PeriodUnit.MONTH);

    // then
    assertEquals(1, result.size());
  }

  // helper ////////////////////////////////////////////////////////

  protected void verifyQueryResults(HistoricProcessInstanceQuery query, int countExpected) {
    verifyQueryResults((AbstractQuery<?, ?>) query, countExpected);
  }

}
