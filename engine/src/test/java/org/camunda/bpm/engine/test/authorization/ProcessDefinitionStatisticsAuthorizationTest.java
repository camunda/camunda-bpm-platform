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
package org.camunda.bpm.engine.test.authorization;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.READ_INSTANCE;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_INSTANCE;

import java.util.List;

import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.management.IncidentStatistics;
import org.camunda.bpm.engine.management.ProcessDefinitionStatistics;
import org.camunda.bpm.engine.management.ProcessDefinitionStatisticsQuery;

/**
 * @author Roman Smirnov
 *
 */
public class ProcessDefinitionStatisticsAuthorizationTest extends AuthorizationTest {

  protected static final String ONE_TASK_PROCESS_KEY = "oneTaskProcess";
  protected static final String ONE_INCIDENT_PROCESS_KEY = "process";

  protected String deploymentId;

  public void setUp() throws Exception {
    deploymentId = createDeployment(null,
        "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/authorization/oneIncidentProcess.bpmn20.xml").getId();
    super.setUp();
  }

  public void tearDown() {
    super.tearDown();
    deleteDeployment(deploymentId);
  }

  // without running instances //////////////////////////////////////////////////////////

  public void testQueryWithoutAuthorizations() {
    // given

    // when
    ProcessDefinitionStatisticsQuery query = managementService.createProcessDefinitionStatisticsQuery();

    // then
    verifyQueryResults(query, 0);
  }

  public void testQueryWithReadPermissionOnOneTaskProcess() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, READ);

    // when
    ProcessDefinitionStatisticsQuery query = managementService.createProcessDefinitionStatisticsQuery();

    // then
    verifyQueryResults(query, 1);

    ProcessDefinitionStatistics statistics = query.singleResult();
    assertEquals(ONE_TASK_PROCESS_KEY, statistics.getKey());
    assertEquals(0, statistics.getInstances());
    assertEquals(0, statistics.getFailedJobs());
    assertTrue(statistics.getIncidentStatistics().isEmpty());
  }

  public void testQueryWithReadPermissionOnAnyProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ);

    // when
    ProcessDefinitionStatisticsQuery query = managementService.createProcessDefinitionStatisticsQuery();

    // then
    verifyQueryResults(query, 2);

    List<ProcessDefinitionStatistics> statistics = query.list();
    for (ProcessDefinitionStatistics result : statistics) {
      verifyStatisticsResult(result, 0, 0, 0);
    }
  }

  // including instances //////////////////////////////////////////////////////////////

  public void testQueryIncludingInstancesWithoutProcessInstanceAuthorizations() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ);

    // when
    List<ProcessDefinitionStatistics> statistics = managementService.createProcessDefinitionStatisticsQuery().list();

    // then
    assertEquals(2, statistics.size());

    for (ProcessDefinitionStatistics result : statistics) {
      verifyStatisticsResult(result, 0, 0, 0);
    }
  }

  public void testQueryIncludingInstancesWithReadPermissionOnFirstProcessInstance() {
    // given
    String firstProcessInstanceId = startProcessInstanceByKey(ONE_TASK_PROCESS_KEY).getId();
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ);
    createGrantAuthorization(PROCESS_INSTANCE, firstProcessInstanceId, userId, READ);

    // when
    List<ProcessDefinitionStatistics> statistics = managementService.createProcessDefinitionStatisticsQuery().list();

    // then
    assertEquals(2, statistics.size());

    ProcessDefinitionStatistics oneTaskProcessStatistics = getStatisticsByKey(statistics, ONE_TASK_PROCESS_KEY);
    verifyStatisticsResult(oneTaskProcessStatistics, 1, 0, 0);

    ProcessDefinitionStatistics oneIncidentProcessStatistics = getStatisticsByKey(statistics, ONE_INCIDENT_PROCESS_KEY);
    verifyStatisticsResult(oneIncidentProcessStatistics, 0, 0, 0);
  }

  public void testQueryIncludingInstancesWithReadPermissionOnAnyProcessInstance() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    List<ProcessDefinitionStatistics> statistics = managementService.createProcessDefinitionStatisticsQuery().list();

    // then
    assertEquals(2, statistics.size());

    ProcessDefinitionStatistics oneTaskProcessStatistics = getStatisticsByKey(statistics, ONE_TASK_PROCESS_KEY);
    verifyStatisticsResult(oneTaskProcessStatistics, 2, 0, 0);

    ProcessDefinitionStatistics oneIncidentProcessStatistics = getStatisticsByKey(statistics, ONE_INCIDENT_PROCESS_KEY);
    verifyStatisticsResult(oneIncidentProcessStatistics, 3, 0, 0);
  }

  public void testQueryIncludingInstancesWithReadInstancePermissionOnOneTaskProcessDefinition() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ);
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, READ_INSTANCE);

    // when
    List<ProcessDefinitionStatistics> statistics = managementService.createProcessDefinitionStatisticsQuery().list();

    // then
    assertEquals(2, statistics.size());

    ProcessDefinitionStatistics oneTaskProcessStatistics = getStatisticsByKey(statistics, ONE_TASK_PROCESS_KEY);
    verifyStatisticsResult(oneTaskProcessStatistics, 2, 0, 0);

    ProcessDefinitionStatistics oneIncidentProcessStatistics = getStatisticsByKey(statistics, ONE_INCIDENT_PROCESS_KEY);
    verifyStatisticsResult(oneIncidentProcessStatistics, 0, 0, 0);
  }

  public void testQueryIncludingInstancesWithReadInstancePermissionOnAnyProcessDefinition() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ, READ_INSTANCE);

    // when
    List<ProcessDefinitionStatistics> statistics = managementService.createProcessDefinitionStatisticsQuery().list();

    // then
    assertEquals(2, statistics.size());

    ProcessDefinitionStatistics oneTaskProcessStatistics = getStatisticsByKey(statistics, ONE_TASK_PROCESS_KEY);
    verifyStatisticsResult(oneTaskProcessStatistics, 2, 0, 0);

    ProcessDefinitionStatistics oneIncidentProcessStatistics = getStatisticsByKey(statistics, ONE_INCIDENT_PROCESS_KEY);
    verifyStatisticsResult(oneIncidentProcessStatistics, 3, 0, 0);
  }

  // including failed jobs ////////////////////////////////////////////////////////////

  public void testQueryIncludingFailedJobsWithoutProcessInstanceAuthorizations() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ);

    // when
    List<ProcessDefinitionStatistics> statistics = managementService
        .createProcessDefinitionStatisticsQuery()
        .includeFailedJobs()
        .list();

    // then
    assertEquals(2, statistics.size());

    for (ProcessDefinitionStatistics result : statistics) {
      verifyStatisticsResult(result, 0, 0, 0);
    }
  }

  public void testQueryIncludingFailedJobsWithReadPermissionOnFirstProcessInstance() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY).getId();
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    String firstProcessInstanceId = startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY).getId();
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ);
    createGrantAuthorization(PROCESS_INSTANCE, firstProcessInstanceId, userId, READ);

    // when
    List<ProcessDefinitionStatistics> statistics = managementService
        .createProcessDefinitionStatisticsQuery()
        .includeFailedJobs()
        .list();

    // then
    assertEquals(2, statistics.size());

    ProcessDefinitionStatistics oneTaskProcessStatistics = getStatisticsByKey(statistics, ONE_TASK_PROCESS_KEY);
    verifyStatisticsResult(oneTaskProcessStatistics, 0, 0, 0);

    ProcessDefinitionStatistics oneIncidentProcessStatistics = getStatisticsByKey(statistics, ONE_INCIDENT_PROCESS_KEY);
    verifyStatisticsResult(oneIncidentProcessStatistics, 1, 1, 0);
  }

  public void testQueryIncludingFailedJobsWithReadPermissionOnAnyProcessInstance() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    List<ProcessDefinitionStatistics> statistics = managementService
        .createProcessDefinitionStatisticsQuery()
        .includeFailedJobs()
        .list();

    // then
    assertEquals(2, statistics.size());

    ProcessDefinitionStatistics oneTaskProcessStatistics = getStatisticsByKey(statistics, ONE_TASK_PROCESS_KEY);
    verifyStatisticsResult(oneTaskProcessStatistics, 2, 0, 0);

    ProcessDefinitionStatistics oneIncidentProcessStatistics = getStatisticsByKey(statistics, ONE_INCIDENT_PROCESS_KEY);
    verifyStatisticsResult(oneIncidentProcessStatistics, 3, 3, 0);
  }

  public void testQueryIncludingFailedJobsWithReadInstancePermissionOnOneIncidentProcessDefinition() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ);
    createGrantAuthorization(PROCESS_DEFINITION, ONE_INCIDENT_PROCESS_KEY, userId, READ_INSTANCE);

    // when
    List<ProcessDefinitionStatistics> statistics = managementService
        .createProcessDefinitionStatisticsQuery()
        .includeFailedJobs()
        .list();

    // then
    assertEquals(2, statistics.size());

    ProcessDefinitionStatistics oneTaskProcessStatistics = getStatisticsByKey(statistics, ONE_TASK_PROCESS_KEY);
    verifyStatisticsResult(oneTaskProcessStatistics, 0, 0, 0);

    ProcessDefinitionStatistics oneIncidentProcessStatistics = getStatisticsByKey(statistics, ONE_INCIDENT_PROCESS_KEY);
    verifyStatisticsResult(oneIncidentProcessStatistics, 3, 3, 0);
  }

  public void testQueryIncludingFailedJobsWithReadInstancePermissionOnAnyProcessDefinition() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ, READ_INSTANCE);

    // when
    List<ProcessDefinitionStatistics> statistics = managementService
        .createProcessDefinitionStatisticsQuery()
        .includeFailedJobs()
        .list();

    // then
    assertEquals(2, statistics.size());

    ProcessDefinitionStatistics oneTaskProcessStatistics = getStatisticsByKey(statistics, ONE_TASK_PROCESS_KEY);
    verifyStatisticsResult(oneTaskProcessStatistics, 2, 0, 0);

    ProcessDefinitionStatistics oneIncidentProcessStatistics = getStatisticsByKey(statistics, ONE_INCIDENT_PROCESS_KEY);
    verifyStatisticsResult(oneIncidentProcessStatistics, 3, 3, 0);
  }

  // including incidents //////////////////////////////////////////////////////////////////////////

  public void testQueryIncludingIncidentsWithoutProcessInstanceAuthorizations() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ);

    // when
    List<ProcessDefinitionStatistics> statistics = managementService
        .createProcessDefinitionStatisticsQuery()
        .includeIncidents()
        .list();

    // then
    assertEquals(2, statistics.size());

    for (ProcessDefinitionStatistics result : statistics) {
      verifyStatisticsResult(result, 0, 0, 0);
    }
  }

  public void testQueryIncludingIncidentsWithReadPermissionOnFirstProcessInstance() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY).getId();
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    String firstProcessInstanceId = startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY).getId();
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ);
    createGrantAuthorization(PROCESS_INSTANCE, firstProcessInstanceId, userId, READ);

    // when
    List<ProcessDefinitionStatistics> statistics = managementService
        .createProcessDefinitionStatisticsQuery()
        .includeIncidents()
        .list();

    // then
    assertEquals(2, statistics.size());

    ProcessDefinitionStatistics oneTaskProcessStatistics = getStatisticsByKey(statistics, ONE_TASK_PROCESS_KEY);
    verifyStatisticsResult(oneTaskProcessStatistics, 0, 0, 0);

    ProcessDefinitionStatistics oneIncidentProcessStatistics = getStatisticsByKey(statistics, ONE_INCIDENT_PROCESS_KEY);
    verifyStatisticsResult(oneIncidentProcessStatistics, 1, 0, 1);
  }

  public void testQueryIncludingIncidentsWithReadPermissionOnAnyProcessInstance() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    List<ProcessDefinitionStatistics> statistics = managementService
        .createProcessDefinitionStatisticsQuery()
        .includeIncidents()
        .list();

    // then
    assertEquals(2, statistics.size());

    ProcessDefinitionStatistics oneTaskProcessStatistics = getStatisticsByKey(statistics, ONE_TASK_PROCESS_KEY);
    verifyStatisticsResult(oneTaskProcessStatistics, 2, 0, 0);

    ProcessDefinitionStatistics oneIncidentProcessStatistics = getStatisticsByKey(statistics, ONE_INCIDENT_PROCESS_KEY);
    verifyStatisticsResult(oneIncidentProcessStatistics, 3, 0, 3);
  }

  public void testQueryIncludingIncidentsWithReadInstancePermissionOnOneIncidentProcessDefinition() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ);
    createGrantAuthorization(PROCESS_DEFINITION, ONE_INCIDENT_PROCESS_KEY, userId, READ_INSTANCE);

    // when
    List<ProcessDefinitionStatistics> statistics = managementService
        .createProcessDefinitionStatisticsQuery()
        .includeIncidents()
        .list();

    // then
    assertEquals(2, statistics.size());

    ProcessDefinitionStatistics oneTaskProcessStatistics = getStatisticsByKey(statistics, ONE_TASK_PROCESS_KEY);
    verifyStatisticsResult(oneTaskProcessStatistics, 0, 0, 0);

    ProcessDefinitionStatistics oneIncidentProcessStatistics = getStatisticsByKey(statistics, ONE_INCIDENT_PROCESS_KEY);
    verifyStatisticsResult(oneIncidentProcessStatistics, 3, 0, 3);
  }

  public void testQueryIncludingIncidentsWithReadInstancePermissionOnAnyProcessDefinition() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ, READ_INSTANCE);

    // when
    List<ProcessDefinitionStatistics> statistics = managementService
        .createProcessDefinitionStatisticsQuery()
        .includeIncidents()
        .list();

    // then
    assertEquals(2, statistics.size());

    ProcessDefinitionStatistics oneTaskProcessStatistics = getStatisticsByKey(statistics, ONE_TASK_PROCESS_KEY);
    verifyStatisticsResult(oneTaskProcessStatistics, 2, 0, 0);

    ProcessDefinitionStatistics oneIncidentProcessStatistics = getStatisticsByKey(statistics, ONE_INCIDENT_PROCESS_KEY);
    verifyStatisticsResult(oneIncidentProcessStatistics, 3, 0, 3);
  }

  // including incidents and failed jobs ///////////////////////////////////////////////////////////////

  public void testQueryIncludingIncidentsAndFailedJobsWithoutProcessInstanceAuthorizations() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ);

    // when
    List<ProcessDefinitionStatistics> statistics = managementService
        .createProcessDefinitionStatisticsQuery()
        .includeIncidents()
        .includeFailedJobs()
        .list();

    // then
    assertEquals(2, statistics.size());

    for (ProcessDefinitionStatistics result : statistics) {
      verifyStatisticsResult(result, 0, 0, 0);
    }
  }

  public void testQueryIncludingIncidentsAndFailedJobsWithReadPermissionOnFirstProcessInstance() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY).getId();
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    String firstProcessInstanceId = startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY).getId();
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ);
    createGrantAuthorization(PROCESS_INSTANCE, firstProcessInstanceId, userId, READ);

    // when
    List<ProcessDefinitionStatistics> statistics = managementService
        .createProcessDefinitionStatisticsQuery()
        .includeIncidents()
        .includeFailedJobs()
        .list();

    // then
    assertEquals(2, statistics.size());

    ProcessDefinitionStatistics oneTaskProcessStatistics = getStatisticsByKey(statistics, ONE_TASK_PROCESS_KEY);
    verifyStatisticsResult(oneTaskProcessStatistics, 0, 0, 0);

    ProcessDefinitionStatistics oneIncidentProcessStatistics = getStatisticsByKey(statistics, ONE_INCIDENT_PROCESS_KEY);
    verifyStatisticsResult(oneIncidentProcessStatistics, 1, 1, 1);
  }

  public void testQueryIncludingIncidentsAndFailedJobsWithReadPermissionOnAnyProcessInstance() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    List<ProcessDefinitionStatistics> statistics = managementService
        .createProcessDefinitionStatisticsQuery()
        .includeIncidents()
        .includeFailedJobs()
        .list();

    // then
    assertEquals(2, statistics.size());

    ProcessDefinitionStatistics oneTaskProcessStatistics = getStatisticsByKey(statistics, ONE_TASK_PROCESS_KEY);
    verifyStatisticsResult(oneTaskProcessStatistics, 2, 0, 0);

    ProcessDefinitionStatistics oneIncidentProcessStatistics = getStatisticsByKey(statistics, ONE_INCIDENT_PROCESS_KEY);
    verifyStatisticsResult(oneIncidentProcessStatistics, 3, 3, 3);
  }

  public void testQueryIncludingIncidentsAndFailedJobsWithReadInstancePermissionOnOneIncidentProcessDefinition() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ);
    createGrantAuthorization(PROCESS_DEFINITION, ONE_INCIDENT_PROCESS_KEY, userId, READ_INSTANCE);

    // when
    List<ProcessDefinitionStatistics> statistics = managementService
        .createProcessDefinitionStatisticsQuery()
        .includeIncidents()
        .includeFailedJobs()
        .list();

    // then
    assertEquals(2, statistics.size());

    ProcessDefinitionStatistics oneTaskProcessStatistics = getStatisticsByKey(statistics, ONE_TASK_PROCESS_KEY);
    verifyStatisticsResult(oneTaskProcessStatistics, 0, 0, 0);

    ProcessDefinitionStatistics oneIncidentProcessStatistics = getStatisticsByKey(statistics, ONE_INCIDENT_PROCESS_KEY);
    verifyStatisticsResult(oneIncidentProcessStatistics, 3, 3, 3);
  }

  public void testQueryIncludingIncidentsAndFailedJobsWithReadInstancePermissionOnAnyProcessDefinition() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ, READ_INSTANCE);

    // when
    List<ProcessDefinitionStatistics> statistics = managementService
        .createProcessDefinitionStatisticsQuery()
        .includeIncidents()
        .includeFailedJobs()
        .list();

    // then
    assertEquals(2, statistics.size());

    ProcessDefinitionStatistics oneTaskProcessStatistics = getStatisticsByKey(statistics, ONE_TASK_PROCESS_KEY);
    verifyStatisticsResult(oneTaskProcessStatistics, 2, 0, 0);

    ProcessDefinitionStatistics oneIncidentProcessStatistics = getStatisticsByKey(statistics, ONE_INCIDENT_PROCESS_KEY);
    verifyStatisticsResult(oneIncidentProcessStatistics, 3, 3, 3);
  }

  // helper ///////////////////////////////////////////////////////////////////////////

  protected void verifyQueryResults(ProcessDefinitionStatisticsQuery query, int countExpected) {
    verifyQueryResults((AbstractQuery<?, ?>) query, countExpected);
  }

  protected void verifyStatisticsResult(ProcessDefinitionStatistics statistics, int instances, int failedJobs, int incidents) {
    assertEquals("Instances", instances, statistics.getInstances());
    assertEquals("Failed Jobs", failedJobs, statistics.getFailedJobs());

    List<IncidentStatistics> incidentStatistics = statistics.getIncidentStatistics();
    if (incidents == 0) {
      assertTrue("Incidents supposed to be empty", incidentStatistics.isEmpty());
    }
    else {
      // the test does have only one type of incidents
      assertEquals("Incidents", incidents, incidentStatistics.get(0).getIncidentCount());
    }
  }

  protected ProcessDefinitionStatistics getStatisticsByKey(List<ProcessDefinitionStatistics> statistics, String key) {
    for (ProcessDefinitionStatistics result : statistics) {
      if (key.equals(result.getKey())) {
        return result;
      }
    }
    fail("No statistics found for key '" + key + "'.");
    return null;
  }
}
