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

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.READ_INSTANCE;
import static org.camunda.bpm.engine.authorization.Resources.DEPLOYMENT;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_INSTANCE;

import java.util.List;

import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.management.DeploymentStatistics;
import org.camunda.bpm.engine.management.DeploymentStatisticsQuery;
import org.camunda.bpm.engine.management.IncidentStatistics;


/**
 * @author Roman Smirnov
 *
 */
public class DeploymentStatisticsAuthorizationTest extends AuthorizationTest {

  protected static final String ONE_INCIDENT_PROCESS_KEY = "process";
  protected static final String TIMER_START_PROCESS_KEY = "timerStartProcess";
  protected static final String TIMER_BOUNDARY_PROCESS_KEY = "timerBoundaryProcess";

  protected String firstDeploymentId;
  protected String secondDeploymentId;
  protected String thirdDeploymentId;

  @Override
  public void setUp() throws Exception {
    firstDeploymentId = createDeployment("first", "org/camunda/bpm/engine/test/api/authorization/oneIncidentProcess.bpmn20.xml").getId();
    secondDeploymentId = createDeployment("second", "org/camunda/bpm/engine/test/api/authorization/timerStartEventProcess.bpmn20.xml").getId();
    thirdDeploymentId = createDeployment("third", "org/camunda/bpm/engine/test/api/authorization/timerBoundaryEventProcess.bpmn20.xml").getId();
    super.setUp();
  }

  @Override
  public void tearDown() {
    super.tearDown();
    deleteDeployment(firstDeploymentId);
    deleteDeployment(secondDeploymentId);
    deleteDeployment(thirdDeploymentId);
  }

  // deployment statistics query without process instance authorizations /////////////////////////////////////////////

  public void testQueryWithoutAuthorization() {
    // given

    // when
    DeploymentStatisticsQuery query = managementService.createDeploymentStatisticsQuery();

    // then
    verifyQueryResults(query, 0);
  }

  public void testQueryWithReadPermissionOnDeployment() {
    // given
    createGrantAuthorization(DEPLOYMENT, firstDeploymentId, userId, READ);

    // when
    DeploymentStatisticsQuery query = managementService.createDeploymentStatisticsQuery();

    // then
    verifyQueryResults(query, 1);

    DeploymentStatistics statistics = query.singleResult();
    verifyStatisticsResult(statistics, 0, 0, 0);
  }

  public void testQueryWithMultiple() {
    // given
    createGrantAuthorization(DEPLOYMENT, firstDeploymentId, userId, READ);
    createGrantAuthorization(DEPLOYMENT, ANY, userId, READ);

    // when
    DeploymentStatisticsQuery query = managementService.createDeploymentStatisticsQuery();

    // then
    verifyQueryResults(query, 3);
  }

  public void testQueryWithReadPermissionOnAnyDeployment() {
    // given
    createGrantAuthorization(DEPLOYMENT, ANY, userId, READ);

    // when
    DeploymentStatisticsQuery query = managementService.createDeploymentStatisticsQuery();

    // then
    verifyQueryResults(query, 3);

    List<DeploymentStatistics> result = query.list();
    for (DeploymentStatistics statistics : result) {
      verifyStatisticsResult(statistics, 0, 0, 0);
    }
  }

  // deployment statistics query (including process instances) /////////////////////////////////////////////

  public void testQueryWithReadPermissionOnProcessInstance() {
    // given
    createGrantAuthorization(DEPLOYMENT, ANY, userId, READ);

    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);

    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    // when
    DeploymentStatisticsQuery query = managementService.createDeploymentStatisticsQuery();

    // then
    List<DeploymentStatistics> statistics = query.list();

    for (DeploymentStatistics deploymentStatistics : statistics) {
      String id = deploymentStatistics.getId();
      if (id.equals(firstDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 0, 0, 0);
      } else if (id.equals(secondDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 0, 0, 0);
      } else if (id.equals(thirdDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 1, 0, 0);
      } else {
        fail("Unexpected deployment");
      }
    }
  }

  public void testQueryWithReadPermissionOnAnyProcessInstance() {
    // given
    createGrantAuthorization(DEPLOYMENT, ANY, userId, READ);

    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);

    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);

    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    DeploymentStatisticsQuery query = managementService.createDeploymentStatisticsQuery();

    // then
    List<DeploymentStatistics> statistics = query.list();

    for (DeploymentStatistics deploymentStatistics : statistics) {
      String id = deploymentStatistics.getId();
      if (id.equals(firstDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 3, 0, 0);
      } else if (id.equals(secondDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 3, 0, 0);
      } else if (id.equals(thirdDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 3, 0, 0);
      } else {
        fail("Unexpected deployment");
      }
    }
  }

  public void testQueryWithReadInstancePermissionOnProcessDefinition() {
    // given
    createGrantAuthorization(DEPLOYMENT, ANY, userId, READ);

    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);

    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, TIMER_BOUNDARY_PROCESS_KEY, userId, READ_INSTANCE);

    // when
    DeploymentStatisticsQuery query = managementService.createDeploymentStatisticsQuery();

    // then
    List<DeploymentStatistics> statistics = query.list();

    for (DeploymentStatistics deploymentStatistics : statistics) {
      String id = deploymentStatistics.getId();
      if (id.equals(firstDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 0, 0, 0);
      } else if (id.equals(secondDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 0, 0, 0);
      } else if (id.equals(thirdDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 3, 0, 0);
      } else {
        fail("Unexpected deployment");
      }
    }
  }

  public void testQueryWithReadInstancePermissionOnAnyProcessDefinition() {
    // given
    createGrantAuthorization(DEPLOYMENT, ANY, userId, READ);

    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);

    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);

    // when
    DeploymentStatisticsQuery query = managementService.createDeploymentStatisticsQuery();

    // then
    List<DeploymentStatistics> statistics = query.list();

    for (DeploymentStatistics deploymentStatistics : statistics) {
      String id = deploymentStatistics.getId();
      if (id.equals(firstDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 3, 0, 0);
      } else if (id.equals(secondDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 3, 0, 0);
      } else if (id.equals(thirdDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 3, 0, 0);
      } else {
        fail("Unexpected deployment");
      }
    }
  }

  // deployment statistics query (including failed jobs) /////////////////////////////////////////////

  public void testQueryIncludingFailedJobsWithReadPermissionOnProcessInstance() {
    // given
    createGrantAuthorization(DEPLOYMENT, ANY, userId, READ);

    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    String processInstanceId = startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);

    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);

    // when
    DeploymentStatisticsQuery query = managementService
        .createDeploymentStatisticsQuery()
        .includeFailedJobs();

    // then
    List<DeploymentStatistics> statistics = query.list();

    for (DeploymentStatistics deploymentStatistics : statistics) {
      String id = deploymentStatistics.getId();
      if (id.equals(firstDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 1, 1, 0);
      } else if (id.equals(secondDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 0, 0, 0);
      } else if (id.equals(thirdDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 0, 0, 0);
      } else {
        fail("Unexpected deployment");
      }
    }
  }

  public void testQueryIncludingFailedJobsWithReadPermissionOnAnyProcessInstance() {
    // given
    createGrantAuthorization(DEPLOYMENT, ANY, userId, READ);

    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);

    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);

    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    DeploymentStatisticsQuery query = managementService
        .createDeploymentStatisticsQuery()
        .includeFailedJobs();

    // then
    List<DeploymentStatistics> statistics = query.list();

    for (DeploymentStatistics deploymentStatistics : statistics) {
      String id = deploymentStatistics.getId();
      if (id.equals(firstDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 3, 3, 0);
      } else if (id.equals(secondDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 3, 0, 0);
      } else if (id.equals(thirdDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 3, 0, 0);
      } else {
        fail("Unexpected deployment");
      }
    }
  }

  public void testQueryIncludingFailedJobsWithReadInstancePermissionOnProcessDefinition() {
    // given
    createGrantAuthorization(DEPLOYMENT, ANY, userId, READ);

    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);

    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ONE_INCIDENT_PROCESS_KEY, userId, READ_INSTANCE);

    // when
    DeploymentStatisticsQuery query = managementService
        .createDeploymentStatisticsQuery()
        .includeFailedJobs();

    // then
    List<DeploymentStatistics> statistics = query.list();

    for (DeploymentStatistics deploymentStatistics : statistics) {
      String id = deploymentStatistics.getId();
      if (id.equals(firstDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 3, 3, 0);
      } else if (id.equals(secondDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 0, 0, 0);
      } else if (id.equals(thirdDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 0, 0, 0);
      } else {
        fail("Unexpected deployment");
      }
    }
  }

  public void testQueryIncludingFailedJobsWithReadInstancePermissionOnAnyProcessDefinition() {
    // given
    createGrantAuthorization(DEPLOYMENT, ANY, userId, READ);

    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);

    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);

    // when
    DeploymentStatisticsQuery query = managementService
        .createDeploymentStatisticsQuery()
        .includeFailedJobs();

    // then
    List<DeploymentStatistics> statistics = query.list();

    for (DeploymentStatistics deploymentStatistics : statistics) {
      String id = deploymentStatistics.getId();
      if (id.equals(firstDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 3, 3, 0);
      } else if (id.equals(secondDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 3, 0, 0);
      } else if (id.equals(thirdDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 3, 0, 0);
      } else {
        fail("Unexpected deployment");
      }
    }
  }

  // deployment statistics query (including incidents) /////////////////////////////////////////////

  public void testQueryIncludingIncidentsWithReadPermissionOnProcessInstance() {
    // given
    createGrantAuthorization(DEPLOYMENT, ANY, userId, READ);

    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    String processInstanceId = startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);

    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);

    // when
    DeploymentStatisticsQuery query = managementService
        .createDeploymentStatisticsQuery()
        .includeIncidents();

    // then
    List<DeploymentStatistics> statistics = query.list();

    for (DeploymentStatistics deploymentStatistics : statistics) {
      String id = deploymentStatistics.getId();
      if (id.equals(firstDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 1, 0, 1);
      } else if (id.equals(secondDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 0, 0, 0);
      } else if (id.equals(thirdDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 0, 0, 0);
      } else {
        fail("Unexpected deployment");
      }
    }
  }

  public void testQueryIncludingIncidentsWithReadPermissionOnAnyProcessInstance() {
    // given
    createGrantAuthorization(DEPLOYMENT, ANY, userId, READ);

    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);

    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);

    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    DeploymentStatisticsQuery query = managementService
        .createDeploymentStatisticsQuery()
        .includeIncidents();

    // then
    List<DeploymentStatistics> statistics = query.list();

    for (DeploymentStatistics deploymentStatistics : statistics) {
      String id = deploymentStatistics.getId();
      if (id.equals(firstDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 3, 0, 3);
      } else if (id.equals(secondDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 3, 0, 0);
      } else if (id.equals(thirdDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 3, 0, 0);
      } else {
        fail("Unexpected deployment");
      }
    }
  }

  public void testQueryIncludingIncidentsWithReadInstancePermissionOnProcessDefinition() {
    // given
    createGrantAuthorization(DEPLOYMENT, ANY, userId, READ);

    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);

    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ONE_INCIDENT_PROCESS_KEY, userId, READ_INSTANCE);

    // when
    DeploymentStatisticsQuery query = managementService
        .createDeploymentStatisticsQuery()
        .includeIncidents();

    // then
    List<DeploymentStatistics> statistics = query.list();

    for (DeploymentStatistics deploymentStatistics : statistics) {
      String id = deploymentStatistics.getId();
      if (id.equals(firstDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 3, 0, 3);
      } else if (id.equals(secondDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 0, 0, 0);
      } else if (id.equals(thirdDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 0, 0, 0);
      } else {
        fail("Unexpected deployment");
      }
    }
  }

  public void testQueryIncludingIncidentsWithReadInstancePermissionOnAnyProcessDefinition() {
    // given
    createGrantAuthorization(DEPLOYMENT, ANY, userId, READ);

    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);

    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);

    // when
    DeploymentStatisticsQuery query = managementService
        .createDeploymentStatisticsQuery()
        .includeIncidents();

    // then
    List<DeploymentStatistics> statistics = query.list();

    for (DeploymentStatistics deploymentStatistics : statistics) {
      String id = deploymentStatistics.getId();
      if (id.equals(firstDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 3, 0, 3);
      } else if (id.equals(secondDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 3, 0, 0);
      } else if (id.equals(thirdDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 3, 0, 0);
      } else {
        fail("Unexpected deployment");
      }
    }
  }

  // deployment statistics query (including failed jobs and incidents) /////////////////////////////////////////////

  public void testQueryIncludingFailedJobsAndIncidentsWithReadPermissionOnProcessInstance() {
    // given
    createGrantAuthorization(DEPLOYMENT, ANY, userId, READ);

    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    String processInstanceId = startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);

    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);

    // when
    DeploymentStatisticsQuery query = managementService
        .createDeploymentStatisticsQuery()
        .includeFailedJobs()
        .includeIncidents();

    // then
    List<DeploymentStatistics> statistics = query.list();

    for (DeploymentStatistics deploymentStatistics : statistics) {
      String id = deploymentStatistics.getId();
      if (id.equals(firstDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 1, 1, 1);
      } else if (id.equals(secondDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 0, 0, 0);
      } else if (id.equals(thirdDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 0, 0, 0);
      } else {
        fail("Unexpected deployment");
      }
    }
  }

  public void testQueryIncludingFailedJobsAndIncidentsWithReadPermissionOnAnyProcessInstance() {
    // given
    createGrantAuthorization(DEPLOYMENT, ANY, userId, READ);

    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);

    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);

    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    DeploymentStatisticsQuery query = managementService
        .createDeploymentStatisticsQuery()
        .includeFailedJobs()
        .includeIncidents();

    // then
    List<DeploymentStatistics> statistics = query.list();

    for (DeploymentStatistics deploymentStatistics : statistics) {
      String id = deploymentStatistics.getId();
      if (id.equals(firstDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 3, 3, 3);
      } else if (id.equals(secondDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 3, 0, 0);
      } else if (id.equals(thirdDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 3, 0, 0);
      } else {
        fail("Unexpected deployment");
      }
    }
  }

  public void testQueryIncludingFailedJobsAndIncidentsWithReadInstancePermissionOnProcessDefinition() {
    // given
    createGrantAuthorization(DEPLOYMENT, ANY, userId, READ);

    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);

    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ONE_INCIDENT_PROCESS_KEY, userId, READ_INSTANCE);

    // when
    DeploymentStatisticsQuery query = managementService
        .createDeploymentStatisticsQuery()
        .includeFailedJobs()
        .includeIncidents();

    // then
    List<DeploymentStatistics> statistics = query.list();

    for (DeploymentStatistics deploymentStatistics : statistics) {
      String id = deploymentStatistics.getId();
      if (id.equals(firstDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 3, 3, 3);
      } else if (id.equals(secondDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 0, 0, 0);
      } else if (id.equals(thirdDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 0, 0, 0);
      } else {
        fail("Unexpected deployment");
      }
    }
  }

  public void testQueryIncludingFailedJobsAndIncidentsWithReadInstancePermissionOnAnyProcessDefinition() {
    // given
    createGrantAuthorization(DEPLOYMENT, ANY, userId, READ);

    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_START_PROCESS_KEY);

    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);

    // when
    DeploymentStatisticsQuery query = managementService
        .createDeploymentStatisticsQuery()
        .includeFailedJobs()
        .includeIncidents();

    // then
    List<DeploymentStatistics> statistics = query.list();

    for (DeploymentStatistics deploymentStatistics : statistics) {
      String id = deploymentStatistics.getId();
      if (id.equals(firstDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 3, 3, 3);
      } else if (id.equals(secondDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 3, 0, 0);
      } else if (id.equals(thirdDeploymentId)) {
        verifyStatisticsResult(deploymentStatistics, 3, 0, 0);
      } else {
        fail("Unexpected deployment");
      }
    }
  }

  // helper ///////////////////////////////////////////////////////////////////////////

  protected void verifyQueryResults(DeploymentStatisticsQuery query, int countExpected) {
    verifyQueryResults((AbstractQuery<?, ?>) query, countExpected);
  }

  protected void verifyStatisticsResult(DeploymentStatistics statistics, int instances, int failedJobs, int incidents) {
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

}
