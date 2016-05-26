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
import org.camunda.bpm.engine.history.HistoricActivityStatistics;
import org.camunda.bpm.engine.history.HistoricActivityStatisticsQuery;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.authorization.AuthorizationTest;

/**
 * @author Roman Smirnov
 *
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
public class HistoricActivityStatisticsAuthorizationTest extends AuthorizationTest {

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

  // historic activity statistics query //////////////////////////////////

  public void testQueryWithoutAuthorization() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(PROCESS_KEY).getId();

    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);

    // when
    HistoricActivityStatisticsQuery query = historyService.createHistoricActivityStatisticsQuery(processDefinitionId);

    // then
    verifyQueryResults(query, 0);
  }

  public void testQueryWithReadHistoryPermissionOnProcessDefinition() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(PROCESS_KEY).getId();

    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY);

    // when
    HistoricActivityStatisticsQuery query = historyService.createHistoricActivityStatisticsQuery(processDefinitionId);

    // then
    verifyQueryResults(query, 1);
    verifyStatisticsResult(query.singleResult(), 3, 0, 0, 0);
  }

  public void testQueryWithReadHistoryPermissionOnAnyProcessDefinition() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(PROCESS_KEY).getId();

    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    HistoricActivityStatisticsQuery query = historyService.createHistoricActivityStatisticsQuery(processDefinitionId);

    // then
    verifyQueryResults(query, 1);
    verifyStatisticsResult(query.singleResult(), 3, 0, 0, 0);
  }

  public void testQueryMultiple() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(PROCESS_KEY).getId();

    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY);

    // when
    HistoricActivityStatisticsQuery query = historyService.createHistoricActivityStatisticsQuery(processDefinitionId);

    // then
    verifyQueryResults(query, 1);
    verifyStatisticsResult(query.singleResult(), 3, 0, 0, 0);
  }

  // historic activity statistics query (including finished) //////////////////////////////////

  public void testQueryIncludingFinishedWithoutAuthorization() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(PROCESS_KEY).getId();

    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);

    String taskId = selectAnyTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    // when
    HistoricActivityStatisticsQuery query = historyService
        .createHistoricActivityStatisticsQuery(processDefinitionId)
        .includeFinished();

    // then
    verifyQueryResults(query, 0);
  }

  public void testQueryIncludingFinishedWithReadHistoryPermissionOnProcessDefinition() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(PROCESS_KEY).getId();

    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);

    String taskId = selectAnyTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY);

    // when
    HistoricActivityStatisticsQuery query = historyService
        .createHistoricActivityStatisticsQuery(processDefinitionId)
        .includeFinished();

    // then
    verifyQueryResults(query, 3);
    List<HistoricActivityStatistics> statistics = query.list();

    HistoricActivityStatistics start = getStatisticsByKey(statistics, "theStart");
    verifyStatisticsResult(start, 0, 3, 0, 0);

    HistoricActivityStatistics task = getStatisticsByKey(statistics, "theTask");
    verifyStatisticsResult(task, 2, 1, 0, 0);

    HistoricActivityStatistics end = getStatisticsByKey(statistics, "theEnd");
    verifyStatisticsResult(end, 0, 1, 0, 0);
  }

  public void testQueryIncludingFinishedWithReadHistoryPermissionOnAnyProcessDefinition() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(PROCESS_KEY).getId();

    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);

    String taskId = selectAnyTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    HistoricActivityStatisticsQuery query = historyService
        .createHistoricActivityStatisticsQuery(processDefinitionId)
        .includeFinished();

    // then
    verifyQueryResults(query, 3);
    List<HistoricActivityStatistics> statistics = query.list();

    HistoricActivityStatistics start = getStatisticsByKey(statistics, "theStart");
    verifyStatisticsResult(start, 0, 3, 0, 0);

    HistoricActivityStatistics task = getStatisticsByKey(statistics, "theTask");
    verifyStatisticsResult(task, 2, 1, 0, 0);

    HistoricActivityStatistics end = getStatisticsByKey(statistics, "theEnd");
    verifyStatisticsResult(end, 0, 1, 0, 0);
  }

  // historic activity statistics query (including canceled) //////////////////////////////////

  public void testQueryIncludingCanceledWithoutAuthorization() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(PROCESS_KEY).getId();

    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();

    disableAuthorization();
    runtimeService.deleteProcessInstance(processInstanceId, null);
    enableAuthorization();

    // when
    HistoricActivityStatisticsQuery query = historyService
        .createHistoricActivityStatisticsQuery(processDefinitionId)
        .includeCanceled();

    // then
    verifyQueryResults(query, 0);
  }

  public void testQueryIncludingCanceledWithReadHistoryPermissionOnProcessDefinition() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(PROCESS_KEY).getId();

    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();

    disableAuthorization();
    runtimeService.deleteProcessInstance(processInstanceId, null);
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY);

    // when
    HistoricActivityStatisticsQuery query = historyService
        .createHistoricActivityStatisticsQuery(processDefinitionId)
        .includeCanceled();

    // then
    verifyQueryResults(query, 1);
    List<HistoricActivityStatistics> statistics = query.list();

    HistoricActivityStatistics task = getStatisticsByKey(statistics, "theTask");
    verifyStatisticsResult(task, 2, 0, 1, 0);
  }

  public void testQueryIncludingCanceledWithReadHistoryPermissionOnAnyProcessDefinition() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(PROCESS_KEY).getId();

    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();

    disableAuthorization();
    runtimeService.deleteProcessInstance(processInstanceId, null);
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    HistoricActivityStatisticsQuery query = historyService
        .createHistoricActivityStatisticsQuery(processDefinitionId)
        .includeCanceled();

    // then
    verifyQueryResults(query, 1);
    List<HistoricActivityStatistics> statistics = query.list();

    HistoricActivityStatistics task = getStatisticsByKey(statistics, "theTask");
    verifyStatisticsResult(task, 2, 0, 1, 0);
  }

  // historic activity statistics query (including complete scope) //////////////////////////////////

  public void testQueryIncludingCompleteScopeWithoutAuthorization() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(PROCESS_KEY).getId();

    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);

    String taskId = selectAnyTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    // when
    HistoricActivityStatisticsQuery query = historyService
        .createHistoricActivityStatisticsQuery(processDefinitionId)
        .includeCompleteScope();

    // then
    verifyQueryResults(query, 0);
  }

  public void testQueryIncludingCompleteScopeWithReadHistoryPermissionOnProcessDefinition() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(PROCESS_KEY).getId();

    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);

    String taskId = selectAnyTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY);

    // when
    HistoricActivityStatisticsQuery query = historyService
        .createHistoricActivityStatisticsQuery(processDefinitionId)
        .includeCompleteScope();

    // then
    verifyQueryResults(query, 2);
    List<HistoricActivityStatistics> statistics = query.list();

    HistoricActivityStatistics task = getStatisticsByKey(statistics, "theTask");
    verifyStatisticsResult(task, 2, 0, 0, 0);

    HistoricActivityStatistics end = getStatisticsByKey(statistics, "theEnd");
    verifyStatisticsResult(end, 0, 0, 0, 1);
  }

  public void testQueryIncludingCompleteScopeWithReadHistoryPermissionOnAnyProcessDefinition() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(PROCESS_KEY).getId();

    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);

    String taskId = selectAnyTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    HistoricActivityStatisticsQuery query = historyService
        .createHistoricActivityStatisticsQuery(processDefinitionId)
        .includeCompleteScope();

    // then
    verifyQueryResults(query, 2);
    List<HistoricActivityStatistics> statistics = query.list();

    HistoricActivityStatistics task = getStatisticsByKey(statistics, "theTask");
    verifyStatisticsResult(task, 2, 0, 0, 0);

    HistoricActivityStatistics end = getStatisticsByKey(statistics, "theEnd");
    verifyStatisticsResult(end, 0, 0, 0, 1);
  }

  // historic activity statistics query (including all) //////////////////////////////////

  public void testQueryIncludingAllWithoutAuthorization() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(PROCESS_KEY).getId();

    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();

    disableAuthorization();
    runtimeService.deleteProcessInstance(processInstanceId, null);
    enableAuthorization();

    String taskId = selectAnyTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    // when
    HistoricActivityStatisticsQuery query = historyService
        .createHistoricActivityStatisticsQuery(processDefinitionId)
        .includeFinished()
        .includeCanceled()
        .includeCompleteScope();

    // then
    verifyQueryResults(query, 0);
  }

  public void testQueryIncludingAllWithReadHistoryPermissionOnProcessDefinition() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(PROCESS_KEY).getId();

    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();

    disableAuthorization();
    runtimeService.deleteProcessInstance(processInstanceId, null);
    enableAuthorization();

    String taskId = selectAnyTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY);

    // when
    HistoricActivityStatisticsQuery query = historyService
        .createHistoricActivityStatisticsQuery(processDefinitionId)
        .includeFinished()
        .includeCanceled()
        .includeCompleteScope();

    // then
    verifyQueryResults(query, 3);
    List<HistoricActivityStatistics> statistics = query.list();

    HistoricActivityStatistics start = getStatisticsByKey(statistics, "theStart");
    verifyStatisticsResult(start, 0, 3, 0, 0);

    HistoricActivityStatistics task = getStatisticsByKey(statistics, "theTask");
    verifyStatisticsResult(task, 1, 2, 1, 0);

    HistoricActivityStatistics end = getStatisticsByKey(statistics, "theEnd");
    verifyStatisticsResult(end, 0, 1, 0, 1);
  }

  public void testQueryIncludingAllWithReadHistoryPermissionOnAnyProcessDefinition() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(PROCESS_KEY).getId();

    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();

    disableAuthorization();
    runtimeService.deleteProcessInstance(processInstanceId, null);
    enableAuthorization();

    String taskId = selectAnyTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    HistoricActivityStatisticsQuery query = historyService
        .createHistoricActivityStatisticsQuery(processDefinitionId)
        .includeFinished()
        .includeCanceled()
        .includeCompleteScope();

    // then
    verifyQueryResults(query, 3);
    List<HistoricActivityStatistics> statistics = query.list();

    HistoricActivityStatistics start = getStatisticsByKey(statistics, "theStart");
    verifyStatisticsResult(start, 0, 3, 0, 0);

    HistoricActivityStatistics task = getStatisticsByKey(statistics, "theTask");
    verifyStatisticsResult(task, 1, 2, 1, 0);

    HistoricActivityStatistics end = getStatisticsByKey(statistics, "theEnd");
    verifyStatisticsResult(end, 0, 1, 0, 1);
  }

  // helper ////////////////////////////////////////////////////////

  protected void verifyQueryResults(HistoricActivityStatisticsQuery query, int countExpected) {
    verifyQueryResults((AbstractQuery<?, ?>) query, countExpected);
  }

  protected void verifyStatisticsResult(HistoricActivityStatistics statistics, int instances, int finished, int canceled, int completeScope) {
    assertEquals("Instances", instances, statistics.getInstances());
    assertEquals("Finished", finished, statistics.getFinished());
    assertEquals("Canceled", canceled, statistics.getCanceled());
    assertEquals("Complete Scope", completeScope, statistics.getCompleteScope());
  }

  protected HistoricActivityStatistics getStatisticsByKey(List<HistoricActivityStatistics> statistics, String key) {
    for (HistoricActivityStatistics result : statistics) {
      if (key.equals(result.getId())) {
        return result;
      }
    }
    fail("No statistics found for key '" + key + "'.");
    return null;
  }

  protected Task selectAnyTask() {
    disableAuthorization();
    Task task = taskService.createTaskQuery().listPage(0, 1).get(0);
    enableAuthorization();
    return task;
  }

}
