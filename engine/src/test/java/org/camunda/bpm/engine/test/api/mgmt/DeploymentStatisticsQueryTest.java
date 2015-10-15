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

package org.camunda.bpm.engine.test.api.mgmt;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.management.DeploymentStatistics;
import org.camunda.bpm.engine.management.IncidentStatistics;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Test;

public class DeploymentStatisticsQueryTest extends PluggableProcessEngineTestCase {

  @Test
  public void testDeploymentStatisticsQuery() {
    String deploymentName = "my deployment";

    org.camunda.bpm.engine.repository.Deployment deployment = repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testMultiInstanceStatisticsQuery.bpmn20.xml")
        .addClasspathResource("org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testParallelGatewayStatisticsQuery.bpmn20.xml")
        .name(deploymentName)
        .deploy();
    runtimeService.startProcessInstanceByKey("MIExampleProcess");
    runtimeService.startProcessInstanceByKey("ParGatewayExampleProcess");

    List<DeploymentStatistics> statistics =
        managementService.createDeploymentStatisticsQuery().includeFailedJobs().list();

    Assert.assertEquals(1, statistics.size());

    DeploymentStatistics result = statistics.get(0);
    Assert.assertEquals(2, result.getInstances());
    Assert.assertEquals(0, result.getFailedJobs());

    Assert.assertEquals(deployment.getId(), result.getId());
    Assert.assertEquals(deploymentName, result.getName());

    // only compare time on second level (i.e. drop milliseconds)
    Calendar cal1 = Calendar.getInstance();
    cal1.setTime(deployment.getDeploymentTime());
    cal1.set(Calendar.MILLISECOND, 0);

    Calendar cal2 = Calendar.getInstance();
    cal2.setTime(result.getDeploymentTime());
    cal2.set(Calendar.MILLISECOND, 0);

    Assert.assertTrue(cal1.equals(cal2));

    repositoryService.deleteDeployment(deployment.getId(), true);
  }

  @Test
  public void testDeploymentStatisticsQueryCountAndPaging() {
    org.camunda.bpm.engine.repository.Deployment deployment = repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testMultiInstanceStatisticsQuery.bpmn20.xml")
        .addClasspathResource("org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testParallelGatewayStatisticsQuery.bpmn20.xml")
        .deploy();

    runtimeService.startProcessInstanceByKey("MIExampleProcess");
    runtimeService.startProcessInstanceByKey("ParGatewayExampleProcess");

    org.camunda.bpm.engine.repository.Deployment anotherDeployment = repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testMultiInstanceStatisticsQuery.bpmn20.xml")
        .addClasspathResource("org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testParallelGatewayStatisticsQuery.bpmn20.xml")
        .deploy();

    runtimeService.startProcessInstanceByKey("MIExampleProcess");
    runtimeService.startProcessInstanceByKey("ParGatewayExampleProcess");

    long count = managementService.createDeploymentStatisticsQuery().includeFailedJobs().count();

    Assert.assertEquals(2, count);

    List<DeploymentStatistics> statistics = managementService.createDeploymentStatisticsQuery().includeFailedJobs().listPage(0, 1);
    Assert.assertEquals(1, statistics.size());

    repositoryService.deleteDeployment(deployment.getId(), true);
    repositoryService.deleteDeployment(anotherDeployment.getId(), true);
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testMultiInstanceStatisticsQuery.bpmn20.xml",
  "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testStatisticsQueryWithFailedJobs.bpmn20.xml"})
  public void testDeploymentStatisticsQueryWithFailedJobs() {

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("fail", true);

    runtimeService.startProcessInstanceByKey("MIExampleProcess");
    runtimeService.startProcessInstanceByKey("ExampleProcess", parameters);

    executeAvailableJobs();

    List<DeploymentStatistics> statistics =
        managementService.createDeploymentStatisticsQuery().includeFailedJobs().list();

    DeploymentStatistics result = statistics.get(0);
    Assert.assertEquals(1, result.getFailedJobs());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testMultiInstanceStatisticsQuery.bpmn20.xml",
  "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testStatisticsQueryWithFailedJobs.bpmn20.xml"})
  public void testDeploymentStatisticsQueryWithIncidents() {

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("fail", true);

    runtimeService.startProcessInstanceByKey("MIExampleProcess");
    runtimeService.startProcessInstanceByKey("ExampleProcess", parameters);

    executeAvailableJobs();

    List<DeploymentStatistics> statistics =
        managementService.createDeploymentStatisticsQuery().includeIncidents().list();

    assertFalse(statistics.isEmpty());
    assertEquals(1, statistics.size());

    DeploymentStatistics result = statistics.get(0);

    List<IncidentStatistics> incidentStatistics = result.getIncidentStatistics();
    assertFalse(incidentStatistics.isEmpty());
    assertEquals(1, incidentStatistics.size());

    IncidentStatistics incident = incidentStatistics.get(0);
    assertEquals(Incident.FAILED_JOB_HANDLER_TYPE, incident.getIncidentType());
    assertEquals(1, incident.getIncidentCount());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testMultiInstanceStatisticsQuery.bpmn20.xml",
  "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testStatisticsQueryWithFailedJobs.bpmn20.xml"})
  public void testDeploymentStatisticsQueryWithIncidentType() {

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("fail", true);

    runtimeService.startProcessInstanceByKey("MIExampleProcess");
    runtimeService.startProcessInstanceByKey("ExampleProcess", parameters);

    executeAvailableJobs();

    List<DeploymentStatistics> statistics =
        managementService
        .createDeploymentStatisticsQuery()
        .includeIncidentsForType("failedJob")
        .list();

    assertFalse(statistics.isEmpty());
    assertEquals(1, statistics.size());

    DeploymentStatistics result = statistics.get(0);

    List<IncidentStatistics> incidentStatistics = result.getIncidentStatistics();
    assertFalse(incidentStatistics.isEmpty());
    assertEquals(1, incidentStatistics.size());

    IncidentStatistics incident = incidentStatistics.get(0);
    assertEquals(Incident.FAILED_JOB_HANDLER_TYPE, incident.getIncidentType());
    assertEquals(1, incident.getIncidentCount());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testMultiInstanceStatisticsQuery.bpmn20.xml",
  "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testStatisticsQueryWithFailedJobs.bpmn20.xml"})
  public void testDeploymentStatisticsQueryWithInvalidIncidentType() {

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("fail", true);

    runtimeService.startProcessInstanceByKey("MIExampleProcess");
    runtimeService.startProcessInstanceByKey("ExampleProcess", parameters);

    executeAvailableJobs();

    List<DeploymentStatistics> statistics =
        managementService
        .createDeploymentStatisticsQuery()
        .includeIncidentsForType("invalid")
        .list();

    assertFalse(statistics.isEmpty());
    assertEquals(1, statistics.size());

    DeploymentStatistics result = statistics.get(0);

    List<IncidentStatistics> incidentStatistics = result.getIncidentStatistics();
    assertTrue(incidentStatistics.isEmpty());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testMultiInstanceStatisticsQuery.bpmn20.xml",
  "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testStatisticsQueryWithFailedJobs.bpmn20.xml"})
  public void testDeploymentStatisticsQueryWithIncidentsAndFailedJobs() {

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("fail", true);

    runtimeService.startProcessInstanceByKey("MIExampleProcess");
    runtimeService.startProcessInstanceByKey("ExampleProcess", parameters);

    executeAvailableJobs();

    List<DeploymentStatistics> statistics =
        managementService
        .createDeploymentStatisticsQuery()
        .includeIncidents()
        .includeFailedJobs()
        .list();

    assertFalse(statistics.isEmpty());
    assertEquals(1, statistics.size());

    DeploymentStatistics result = statistics.get(0);

    Assert.assertEquals(1, result.getFailedJobs());

    List<IncidentStatistics> incidentStatistics = result.getIncidentStatistics();
    assertFalse(incidentStatistics.isEmpty());
    assertEquals(1, incidentStatistics.size());

    IncidentStatistics incident = incidentStatistics.get(0);
    assertEquals(Incident.FAILED_JOB_HANDLER_TYPE, incident.getIncidentType());
    assertEquals(1, incident.getIncidentCount());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testCallActivityWithIncidentsWithoutFailedJobs.bpmn20.xml")
  public void testDeploymentStatisticsQueryWithTwoIncidentsAndOneFailedJobs() {
    runtimeService.startProcessInstanceByKey("callExampleSubProcess");

    executeAvailableJobs();

    List<DeploymentStatistics> statistics =
        managementService
        .createDeploymentStatisticsQuery()
        .includeIncidents()
        .includeFailedJobs()
        .list();

    assertFalse(statistics.isEmpty());
    assertEquals(1, statistics.size());

    DeploymentStatistics result = statistics.get(0);

    // has one failed job
    Assert.assertEquals(1, result.getFailedJobs());

    List<IncidentStatistics> incidentStatistics = result.getIncidentStatistics();
    assertFalse(incidentStatistics.isEmpty());
    assertEquals(1, incidentStatistics.size());

    IncidentStatistics incident = incidentStatistics.get(0);
    assertEquals(Incident.FAILED_JOB_HANDLER_TYPE, incident.getIncidentType());
    assertEquals(2, incident.getIncidentCount()); // ...but two incidents
  }


  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testMultiInstanceStatisticsQuery.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testParallelGatewayStatisticsQuery.bpmn20.xml"})
  public void testDeploymentStatisticsQueryWithoutRunningInstances() {
    List<DeploymentStatistics> statistics =
        managementService.createDeploymentStatisticsQuery().includeFailedJobs().list();

    Assert.assertEquals(1, statistics.size());

    DeploymentStatistics result = statistics.get(0);
    Assert.assertEquals(0, result.getInstances());
    Assert.assertEquals(0, result.getFailedJobs());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testFailedTimerStartEvent.bpmn20.xml")
  public void testQueryByIncidentsWithFailedTimerStartEvent() {

    executeAvailableJobs();

    List<DeploymentStatistics> statistics =
        managementService
        .createDeploymentStatisticsQuery()
        .includeIncidents()
        .list();

    assertEquals(1, statistics.size());

    DeploymentStatistics result = statistics.get(0);

    // there is no running instance
    assertEquals(0, result.getInstances());

    List<IncidentStatistics> incidentStatistics = result.getIncidentStatistics();

    // but there is one incident for the failed timer job
    assertEquals(1, incidentStatistics.size());

    IncidentStatistics incidentStatistic = incidentStatistics.get(0);
    assertEquals(1, incidentStatistic.getIncidentCount());
    assertEquals(Incident.FAILED_JOB_HANDLER_TYPE, incidentStatistic.getIncidentType());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testFailedTimerStartEvent.bpmn20.xml")
  public void testQueryByIncidentTypeWithFailedTimerStartEvent() {

    executeAvailableJobs();

    List<DeploymentStatistics> statistics =
        managementService
        .createDeploymentStatisticsQuery()
        .includeIncidentsForType(Incident.FAILED_JOB_HANDLER_TYPE)
        .list();

    assertEquals(1, statistics.size());

    DeploymentStatistics result = statistics.get(0);

    // there is no running instance
    assertEquals(0, result.getInstances());

    List<IncidentStatistics> incidentStatistics = result.getIncidentStatistics();

    // but there is one incident for the failed timer job
    assertEquals(1, incidentStatistics.size());

    IncidentStatistics incidentStatistic = incidentStatistics.get(0);
    assertEquals(1, incidentStatistic.getIncidentCount());
    assertEquals(Incident.FAILED_JOB_HANDLER_TYPE, incidentStatistic.getIncidentType());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testFailedTimerStartEvent.bpmn20.xml")
  public void testQueryByFailedJobsWithFailedTimerStartEvent() {

    executeAvailableJobs();

    List<DeploymentStatistics> statistics =
        managementService
        .createDeploymentStatisticsQuery()
        .includeFailedJobs()
        .list();

    assertEquals(1, statistics.size());

    DeploymentStatistics result = statistics.get(0);

    // there is no running instance
    assertEquals(0, result.getInstances());
    // but there is one failed timer job
    assertEquals(1, result.getFailedJobs());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testFailedTimerStartEvent.bpmn20.xml")
  public void testQueryByFailedJobsAndIncidentsWithFailedTimerStartEvent() {

    executeAvailableJobs();

    List<DeploymentStatistics> statistics =
        managementService
        .createDeploymentStatisticsQuery()
        .includeFailedJobs()
        .includeIncidents()
        .list();

    assertEquals(1, statistics.size());

    DeploymentStatistics result = statistics.get(0);

    // there is no running instance
    assertEquals(0, result.getInstances());
    // but there is one failed timer job
    assertEquals(1, result.getFailedJobs());

    List<IncidentStatistics> incidentStatistics = result.getIncidentStatistics();

    // and there is one incident for the failed timer job
    assertEquals(1, incidentStatistics.size());

    IncidentStatistics incidentStatistic = incidentStatistics.get(0);
    assertEquals(1, incidentStatistic.getIncidentCount());
    assertEquals(Incident.FAILED_JOB_HANDLER_TYPE, incidentStatistic.getIncidentType());
  }
}
