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
package org.camunda.bpm.engine.test.api.authorization;

import java.util.List;
import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.ALL;
import static org.camunda.bpm.engine.authorization.Permissions.CREATE;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.READ_INSTANCE;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_INSTANCE;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.management.ActivityStatistics;
import org.camunda.bpm.engine.management.ActivityStatisticsQuery;
import org.camunda.bpm.engine.management.IncidentStatistics;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class ActivityStatisticsAuthorizationTest extends AuthorizationTest {

  protected static final String ONE_INCIDENT_PROCESS_KEY = "process";

  protected String deploymentId;

  @Override
  public void setUp() throws Exception {
    deploymentId = createDeployment(null, "org/camunda/bpm/engine/test/api/authorization/oneIncidentProcess.bpmn20.xml").getId();
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    super.setUp();
  }

  @Override
  public void tearDown() {
    super.tearDown();
    deleteDeployment(deploymentId);
  }

  // without any authorization

  public void testQueryWithoutAuthorizations() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_INCIDENT_PROCESS_KEY).getId();

    try {
      // when
      managementService.createActivityStatisticsQuery(processDefinitionId).list();
      fail("Exception expected: It should not be possible to execute the activity statistics query");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(ONE_INCIDENT_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  // including instances //////////////////////////////////////////////////////////////

  public void testQueryIncludingInstancesWithoutAuthorizationOnProcessInstance() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_INCIDENT_PROCESS_KEY).getId();

    createGrantAuthorization(PROCESS_DEFINITION, ONE_INCIDENT_PROCESS_KEY, userId, READ);

    // when
    ActivityStatisticsQuery query = managementService.createActivityStatisticsQuery(processDefinitionId);

    // then
    verifyQueryResults(query, 0);
  }

  public void testQueryIncludingInstancesWithReadPermissionOnOneProcessInstance() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_INCIDENT_PROCESS_KEY).getId();

    disableAuthorization();
    String processInstanceId = runtimeService.createProcessInstanceQuery().list().get(0).getId();
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, ONE_INCIDENT_PROCESS_KEY, userId, READ);
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    // when
    ActivityStatistics statistics = managementService.createActivityStatisticsQuery(processDefinitionId).singleResult();

    // then
    assertNotNull(statistics);
    assertEquals("scriptTask", statistics.getId());
    assertEquals(1, statistics.getInstances());
    assertEquals(0, statistics.getFailedJobs());
    assertTrue(statistics.getIncidentStatistics().isEmpty());
  }

  public void testQueryIncludingInstancesWithMany() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_INCIDENT_PROCESS_KEY).getId();

    disableAuthorization();
    String processInstanceId = runtimeService.createProcessInstanceQuery().list().get(0).getId();
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, ONE_INCIDENT_PROCESS_KEY, userId, READ);
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ);
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    // when
    ActivityStatistics statistics = managementService.createActivityStatisticsQuery(processDefinitionId).singleResult();

    // then
    assertNotNull(statistics);
    assertEquals("scriptTask", statistics.getId());
    assertEquals(1, statistics.getInstances());
    assertEquals(0, statistics.getFailedJobs());
    assertTrue(statistics.getIncidentStatistics().isEmpty());
  }

  public void testQueryIncludingInstancesWithReadPermissionOnAnyProcessInstance() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_INCIDENT_PROCESS_KEY).getId();

    createGrantAuthorization(PROCESS_DEFINITION, ONE_INCIDENT_PROCESS_KEY, userId, READ);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    ActivityStatistics statistics = managementService.createActivityStatisticsQuery(processDefinitionId).singleResult();

    // then
    assertNotNull(statistics);
    assertEquals("scriptTask", statistics.getId());
    assertEquals(3, statistics.getInstances());
    assertEquals(0, statistics.getFailedJobs());
    assertTrue(statistics.getIncidentStatistics().isEmpty());
  }

  public void testQueryIncludingInstancesWithReadInstancePermissionOnProcessDefinition() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_INCIDENT_PROCESS_KEY).getId();

    createGrantAuthorization(PROCESS_DEFINITION, ONE_INCIDENT_PROCESS_KEY, userId, READ, READ_INSTANCE);

    // when
    ActivityStatistics statistics = managementService.createActivityStatisticsQuery(processDefinitionId).singleResult();

    // then
    assertNotNull(statistics);
    assertEquals("scriptTask", statistics.getId());
    assertEquals(3, statistics.getInstances());
    assertEquals(0, statistics.getFailedJobs());
    assertTrue(statistics.getIncidentStatistics().isEmpty());
  }

  // including failed jobs //////////////////////////////////////////////////////////////

  public void testQueryIncludingFailedJobsWithoutAuthorizationOnProcessInstance() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_INCIDENT_PROCESS_KEY).getId();

    createGrantAuthorization(PROCESS_DEFINITION, ONE_INCIDENT_PROCESS_KEY, userId, READ);

    // when
    ActivityStatistics statistics = managementService
        .createActivityStatisticsQuery(processDefinitionId)
        .includeFailedJobs()
        .singleResult();

    // then
    assertNull(statistics);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testStatisticsQuery.bpmn20.xml")
  public void testManyInstancesManyAuthorizationsActivityStatisticsQuery() {

    startProcessAndExecuteJob("ExampleProcess");
    startProcessAndExecuteJob("ExampleProcess");
    startProcessAndExecuteJob("ExampleProcess");

    createGrantAuthorization(PROCESS_DEFINITION, "ExampleProcess", userId, READ, READ_INSTANCE);
    createGrantAuthorizationGroup(PROCESS_DEFINITION, "ExampleProcess", groupId, READ, READ_INSTANCE);

    ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
      .processDefinitionKey("ExampleProcess").singleResult();

    List<ActivityStatistics> statistics =
      managementService
        .createActivityStatisticsQuery(definition.getId())
        .includeFailedJobs()
        .includeIncidents()
        .list();

    Assert.assertEquals(1, statistics.size());

    ActivityStatistics activityResult = statistics.get(0);
    Assert.assertEquals(3, activityResult.getInstances());
    Assert.assertEquals("theTask", activityResult.getId());
    Assert.assertEquals(0, activityResult.getFailedJobs());
    assertTrue(activityResult.getIncidentStatistics().isEmpty());
  }

  public void testQueryIncludingFailedJobsWithReadPermissionOnOneProcessInstance() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_INCIDENT_PROCESS_KEY).getId();

    disableAuthorization();
    String processInstanceId = runtimeService.createProcessInstanceQuery().list().get(0).getId();
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, ONE_INCIDENT_PROCESS_KEY, userId, READ);
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    // when
    ActivityStatistics statistics = managementService
        .createActivityStatisticsQuery(processDefinitionId)
        .includeFailedJobs()
        .singleResult();

    // then
    assertNotNull(statistics);
    assertEquals("scriptTask", statistics.getId());
    assertEquals(1, statistics.getInstances());
    assertEquals(1, statistics.getFailedJobs());
    assertTrue(statistics.getIncidentStatistics().isEmpty());
  }

  public void testQueryIncludingFailedJobsWithReadPermissionOnAnyProcessInstance() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_INCIDENT_PROCESS_KEY).getId();

    createGrantAuthorization(PROCESS_DEFINITION, ONE_INCIDENT_PROCESS_KEY, userId, READ);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    ActivityStatistics statistics = managementService
        .createActivityStatisticsQuery(processDefinitionId)
        .includeFailedJobs()
        .singleResult();

    // then
    assertNotNull(statistics);
    assertEquals("scriptTask", statistics.getId());
    assertEquals(3, statistics.getInstances());
    assertEquals(3, statistics.getFailedJobs());
    assertTrue(statistics.getIncidentStatistics().isEmpty());
  }

  public void testQueryIncludingFailedJobsWithReadInstancePermissionOnProcessDefinition() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_INCIDENT_PROCESS_KEY).getId();

    createGrantAuthorization(PROCESS_DEFINITION, ONE_INCIDENT_PROCESS_KEY, userId, READ, READ_INSTANCE);

    // when
    ActivityStatistics statistics = managementService
        .createActivityStatisticsQuery(processDefinitionId)
        .includeFailedJobs()
        .singleResult();

    // then
    assertNotNull(statistics);
    assertEquals("scriptTask", statistics.getId());
    assertEquals(3, statistics.getInstances());
    assertEquals(3, statistics.getFailedJobs());
    assertTrue(statistics.getIncidentStatistics().isEmpty());
  }

  // including incidents //////////////////////////////////////////////////////////////

  public void testQueryIncludingIncidentsWithoutAuthorizationOnProcessInstance() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_INCIDENT_PROCESS_KEY).getId();

    createGrantAuthorization(PROCESS_DEFINITION, ONE_INCIDENT_PROCESS_KEY, userId, READ);

    // when
    ActivityStatistics statistics = managementService
        .createActivityStatisticsQuery(processDefinitionId)
        .includeIncidents()
        .singleResult();

    // then
    assertNull(statistics);
  }

  public void testQueryIncludingIncidentsWithReadPermissionOnOneProcessInstance() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_INCIDENT_PROCESS_KEY).getId();

    disableAuthorization();
    String processInstanceId = runtimeService.createProcessInstanceQuery().list().get(0).getId();
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, ONE_INCIDENT_PROCESS_KEY, userId, READ);
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    // when
    ActivityStatistics statistics = managementService
        .createActivityStatisticsQuery(processDefinitionId)
        .includeIncidents()
        .singleResult();

    // then
    assertNotNull(statistics);
    assertEquals("scriptTask", statistics.getId());
    assertEquals(1, statistics.getInstances());
    assertEquals(0, statistics.getFailedJobs());
    assertFalse(statistics.getIncidentStatistics().isEmpty());
    IncidentStatistics incidentStatistics = statistics.getIncidentStatistics().get(0);
    assertEquals(1, incidentStatistics.getIncidentCount());
  }

  public void testQueryIncludingIncidentsWithReadPermissionOnAnyProcessInstance() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_INCIDENT_PROCESS_KEY).getId();

    createGrantAuthorization(PROCESS_DEFINITION, ONE_INCIDENT_PROCESS_KEY, userId, READ);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    ActivityStatistics statistics = managementService
        .createActivityStatisticsQuery(processDefinitionId)
        .includeIncidents()
        .singleResult();

    // then
    assertNotNull(statistics);
    assertEquals("scriptTask", statistics.getId());
    assertEquals(3, statistics.getInstances());
    assertEquals(0, statistics.getFailedJobs());
    assertFalse(statistics.getIncidentStatistics().isEmpty());
    IncidentStatistics incidentStatistics = statistics.getIncidentStatistics().get(0);
    assertEquals(3, incidentStatistics.getIncidentCount());
  }

  public void testQueryIncludingIncidentsWithReadInstancePermissionOnProcessDefinition() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_INCIDENT_PROCESS_KEY).getId();

    createGrantAuthorization(PROCESS_DEFINITION, ONE_INCIDENT_PROCESS_KEY, userId, READ, READ_INSTANCE);

    // when
    ActivityStatistics statistics = managementService
        .createActivityStatisticsQuery(processDefinitionId)
        .includeIncidents()
        .singleResult();

    // then
    assertNotNull(statistics);
    assertEquals("scriptTask", statistics.getId());
    assertEquals(3, statistics.getInstances());
    assertEquals(0, statistics.getFailedJobs());
    assertFalse(statistics.getIncidentStatistics().isEmpty());
    IncidentStatistics incidentStatistics = statistics.getIncidentStatistics().get(0);
    assertEquals(3, incidentStatistics.getIncidentCount());
  }

  // including incidents and failed jobs //////////////////////////////////////////////////////////

  public void testQueryIncludingIncidentsAndFailedJobsWithoutAuthorizationOnProcessInstance() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_INCIDENT_PROCESS_KEY).getId();

    createGrantAuthorization(PROCESS_DEFINITION, ONE_INCIDENT_PROCESS_KEY, userId, READ);

    // when
    ActivityStatistics statistics = managementService
        .createActivityStatisticsQuery(processDefinitionId)
        .includeIncidents()
        .includeFailedJobs()
        .singleResult();

    // then
    assertNull(statistics);
  }

  public void testQueryIncludingIncidentsAndFailedJobsWithReadPermissionOnOneProcessInstance() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_INCIDENT_PROCESS_KEY).getId();

    disableAuthorization();
    String processInstanceId = runtimeService.createProcessInstanceQuery().list().get(0).getId();
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, ONE_INCIDENT_PROCESS_KEY, userId, READ);
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    // when
    ActivityStatistics statistics = managementService
        .createActivityStatisticsQuery(processDefinitionId)
        .includeIncidents()
        .includeFailedJobs()
        .singleResult();

    // then
    assertNotNull(statistics);
    assertEquals("scriptTask", statistics.getId());
    assertEquals(1, statistics.getInstances());
    assertEquals(1, statistics.getFailedJobs());
    assertFalse(statistics.getIncidentStatistics().isEmpty());
    IncidentStatistics incidentStatistics = statistics.getIncidentStatistics().get(0);
    assertEquals(1, incidentStatistics.getIncidentCount());
  }

  public void testQueryIncludingIncidentsAndFailedJobsWithReadPermissionOnAnyProcessInstance() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_INCIDENT_PROCESS_KEY).getId();

    createGrantAuthorization(PROCESS_DEFINITION, ONE_INCIDENT_PROCESS_KEY, userId, READ);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    ActivityStatistics statistics = managementService
        .createActivityStatisticsQuery(processDefinitionId)
        .includeIncidents()
        .includeFailedJobs()
        .singleResult();

    // then
    assertNotNull(statistics);
    assertEquals("scriptTask", statistics.getId());
    assertEquals(3, statistics.getInstances());
    assertEquals(3, statistics.getFailedJobs());
    assertFalse(statistics.getIncidentStatistics().isEmpty());
    IncidentStatistics incidentStatistics = statistics.getIncidentStatistics().get(0);
    assertEquals(3, incidentStatistics.getIncidentCount());
  }

  public void testQueryIncludingIncidentsAndFailedJobsWithReadInstancePermissionOnProcessDefinition() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_INCIDENT_PROCESS_KEY).getId();

    createGrantAuthorization(PROCESS_DEFINITION, ONE_INCIDENT_PROCESS_KEY, userId, READ, READ_INSTANCE);

    // when
    ActivityStatistics statistics = managementService
        .createActivityStatisticsQuery(processDefinitionId)
        .includeIncidents()
        .includeFailedJobs()
        .singleResult();

    // then
    assertNotNull(statistics);
    assertEquals("scriptTask", statistics.getId());
    assertEquals(3, statistics.getInstances());
    assertEquals(3, statistics.getFailedJobs());
    assertFalse(statistics.getIncidentStatistics().isEmpty());
    IncidentStatistics incidentStatistics = statistics.getIncidentStatistics().get(0);
    assertEquals(3, incidentStatistics.getIncidentCount());
  }

  // helper ///////////////////////////////////////////////////////////////////////////

  protected void verifyQueryResults(ActivityStatisticsQuery query, int countExpected) {
    verifyQueryResults((AbstractQuery<?, ?>) query, countExpected);
  }

}
