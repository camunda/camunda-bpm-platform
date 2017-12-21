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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.hamcrest.CoreMatchers.containsString;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.management.IncidentStatistics;
import org.camunda.bpm.engine.management.ProcessDefinitionStatistics;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Test;

public class ProcessDefinitionStatisticsQueryTest extends PluggableProcessEngineTestCase {

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testStatisticsQueryWithFailedJobs.bpmn20.xml")
  public void testProcessDefinitionStatisticsQueryWithFailedJobs() {
    runtimeService.startProcessInstanceByKey("ExampleProcess");

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("fail", true);
    runtimeService.startProcessInstanceByKey("ExampleProcess", parameters);

    executeAvailableJobs();

    List<ProcessDefinitionStatistics> statistics =
        managementService
        .createProcessDefinitionStatisticsQuery()
        .includeFailedJobs()
        .list();

    Assert.assertEquals(1, statistics.size());

    ProcessDefinitionStatistics definitionResult = statistics.get(0);
    Assert.assertEquals(2, definitionResult.getInstances());
    Assert.assertEquals(1, definitionResult.getFailedJobs());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testStatisticsQueryWithFailedJobs.bpmn20.xml")
  public void testProcessDefinitionStatisticsQueryWithIncidents() {
    runtimeService.startProcessInstanceByKey("ExampleProcess");

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("fail", true);
    runtimeService.startProcessInstanceByKey("ExampleProcess", parameters);

    executeAvailableJobs();

    List<ProcessDefinitionStatistics> statistics =
        managementService
        .createProcessDefinitionStatisticsQuery()
        .includeIncidents()
        .list();

    Assert.assertEquals(1, statistics.size());

    ProcessDefinitionStatistics definitionResult = statistics.get(0);
    Assert.assertEquals(2, definitionResult.getInstances());

    assertFalse(definitionResult.getIncidentStatistics().isEmpty());
    assertEquals(1, definitionResult.getIncidentStatistics().size());

    IncidentStatistics incidentStatistics = definitionResult.getIncidentStatistics().get(0);
    Assert.assertEquals(Incident.FAILED_JOB_HANDLER_TYPE, incidentStatistics.getIncidentType());
    Assert.assertEquals(1, incidentStatistics.getIncidentCount());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testStatisticsQueryWithFailedJobs.bpmn20.xml")
  public void testProcessDefinitionStatisticsQueryWithIncidentType() {
    runtimeService.startProcessInstanceByKey("ExampleProcess");

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("fail", true);
    runtimeService.startProcessInstanceByKey("ExampleProcess", parameters);

    executeAvailableJobs();

    List<ProcessDefinitionStatistics> statistics =
        managementService
        .createProcessDefinitionStatisticsQuery()
        .includeIncidentsForType("failedJob")
        .list();

    Assert.assertEquals(1, statistics.size());

    ProcessDefinitionStatistics definitionResult = statistics.get(0);
    Assert.assertEquals(2, definitionResult.getInstances());

    assertFalse(definitionResult.getIncidentStatistics().isEmpty());
    assertEquals(1, definitionResult.getIncidentStatistics().size());

    IncidentStatistics incidentStatistics = definitionResult.getIncidentStatistics().get(0);
    Assert.assertEquals(Incident.FAILED_JOB_HANDLER_TYPE, incidentStatistics.getIncidentType());
    Assert.assertEquals(1, incidentStatistics.getIncidentCount());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testStatisticsQueryWithFailedJobs.bpmn20.xml")
  public void testProcessDefinitionStatisticsQueryWithInvalidIncidentType() {
    runtimeService.startProcessInstanceByKey("ExampleProcess");

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("fail", true);
    runtimeService.startProcessInstanceByKey("ExampleProcess", parameters);

    executeAvailableJobs();

    List<ProcessDefinitionStatistics> statistics =
        managementService
        .createProcessDefinitionStatisticsQuery()
        .includeIncidentsForType("invalid")
        .list();

    Assert.assertEquals(1, statistics.size());

    ProcessDefinitionStatistics definitionResult = statistics.get(0);
    Assert.assertEquals(2, definitionResult.getInstances());

    assertTrue(definitionResult.getIncidentStatistics().isEmpty());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testStatisticsQueryWithFailedJobs.bpmn20.xml")
  public void testProcessDefinitionStatisticsQueryWithIncidentsAndFailedJobs() {
    runtimeService.startProcessInstanceByKey("ExampleProcess");

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("fail", true);
    runtimeService.startProcessInstanceByKey("ExampleProcess", parameters);

    executeAvailableJobs();

    List<ProcessDefinitionStatistics> statistics =
        managementService
        .createProcessDefinitionStatisticsQuery()
        .includeIncidents()
        .includeFailedJobs()
        .list();

    Assert.assertEquals(1, statistics.size());

    ProcessDefinitionStatistics definitionResult = statistics.get(0);
    Assert.assertEquals(2, definitionResult.getInstances());
    Assert.assertEquals(1, definitionResult.getFailedJobs());

    assertFalse(definitionResult.getIncidentStatistics().isEmpty());
    assertEquals(1, definitionResult.getIncidentStatistics().size());

    IncidentStatistics incidentStatistics = definitionResult.getIncidentStatistics().get(0);
    Assert.assertEquals(Incident.FAILED_JOB_HANDLER_TYPE, incidentStatistics.getIncidentType());
    Assert.assertEquals(1, incidentStatistics.getIncidentCount());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testStatisticsQueryWithFailedJobs.bpmn20.xml")
  public void testProcessDefinitionStatisticsQueryWithoutRunningInstances() {
    List<ProcessDefinitionStatistics> statistics =
        managementService
        .createProcessDefinitionStatisticsQuery()
        .includeFailedJobs()
        .includeIncidents()
        .list();

    Assert.assertEquals(1, statistics.size());

    ProcessDefinitionStatistics definitionResult = statistics.get(0);
    Assert.assertEquals(0, definitionResult.getInstances());
    Assert.assertEquals(0, definitionResult.getFailedJobs());

    statistics =
        managementService.createProcessDefinitionStatisticsQuery().includeIncidents().list();

    assertTrue(definitionResult.getIncidentStatistics().isEmpty());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testStatisticsQueryWithFailedJobs.bpmn20.xml")
  public void testProcessDefinitionStatisticsQueryCount() {
    runtimeService.startProcessInstanceByKey("ExampleProcess");

    executeAvailableJobs();

    long count =
        managementService
        .createProcessDefinitionStatisticsQuery()
        .includeFailedJobs()
        .includeIncidents()
        .count();

    Assert.assertEquals(1, count);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testMultiInstanceStatisticsQuery.bpmn20.xml")
  public void testMultiInstanceProcessDefinitionStatisticsQuery() {
    runtimeService.startProcessInstanceByKey("MIExampleProcess");

    List<ProcessDefinitionStatistics> statistics =
        managementService
        .createProcessDefinitionStatisticsQuery()
        .list();

    Assert.assertEquals(1, statistics.size());

    ProcessDefinitionStatistics result = statistics.get(0);
    Assert.assertEquals(1, result.getInstances());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testSubprocessStatisticsQuery.bpmn20.xml")
  public void testSubprocessProcessDefinitionStatisticsQuery() {
    runtimeService.startProcessInstanceByKey("ExampleProcess");

    List<ProcessDefinitionStatistics> statistics =
        managementService
        .createProcessDefinitionStatisticsQuery()
        .list();

    Assert.assertEquals(1, statistics.size());

    ProcessDefinitionStatistics result = statistics.get(0);
    Assert.assertEquals(1, result.getInstances());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testCallActivityWithIncidentsWithoutFailedJobs.bpmn20.xml")
  public void testCallActivityProcessDefinitionStatisticsQuery() {
    runtimeService.startProcessInstanceByKey("callExampleSubProcess");

    executeAvailableJobs();

    List<ProcessDefinitionStatistics> statistics =
        managementService
        .createProcessDefinitionStatisticsQuery()
        .includeFailedJobs()
        .list();

    Assert.assertEquals(2, statistics.size());

    for (ProcessDefinitionStatistics result : statistics) {
      if (result.getKey().equals("ExampleProcess")) {
        Assert.assertEquals(1, result.getInstances());
        Assert.assertEquals(1, result.getFailedJobs());
      } else if (result.getKey().equals("callExampleSubProcess")) {
        Assert.assertEquals(1, result.getInstances());
        Assert.assertEquals(0, result.getFailedJobs());
      } else {
        fail(result + " was not expected.");
      }
    }
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testStatisticsQueryWithFailedJobs.bpmn20.xml")
  public void testProcessDefinitionStatisticsQueryForMultipleVersions() {
    org.camunda.bpm.engine.repository.Deployment deployment =
        repositoryService.createDeployment()
          .addClasspathResource("org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testStatisticsQueryWithFailedJobs.bpmn20.xml")
          .deploy();

    List<ProcessDefinition> definitions =
        repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("ExampleProcess")
        .list();

    for (ProcessDefinition definition : definitions) {
      runtimeService.startProcessInstanceById(definition.getId());
    }

    executeAvailableJobs();

    List<ProcessDefinitionStatistics> statistics =
        managementService
        .createProcessDefinitionStatisticsQuery()
        .includeFailedJobs()
        .includeIncidents()
        .list();

    Assert.assertEquals(2, statistics.size());

    ProcessDefinitionStatistics definitionResult = statistics.get(0);
    Assert.assertEquals(1, definitionResult.getInstances());
    Assert.assertEquals(0, definitionResult.getFailedJobs());

    assertTrue(definitionResult.getIncidentStatistics().isEmpty());

    definitionResult = statistics.get(1);
    Assert.assertEquals(1, definitionResult.getInstances());
    Assert.assertEquals(0, definitionResult.getFailedJobs());

    assertTrue(definitionResult.getIncidentStatistics().isEmpty());

    repositoryService.deleteDeployment(deployment.getId(), true);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testStatisticsQueryWithFailedJobs.bpmn20.xml")
  public void testProcessDefinitionStatisticsQueryForMultipleVersionsWithFailedJobsAndIncidents() {
    org.camunda.bpm.engine.repository.Deployment deployment =
        repositoryService.createDeployment()
          .addClasspathResource("org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testStatisticsQueryWithFailedJobs.bpmn20.xml")
          .deploy();

    List<ProcessDefinition> definitions =
        repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("ExampleProcess")
        .list();

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("fail", true);

    for (ProcessDefinition definition : definitions) {
      runtimeService.startProcessInstanceById(definition.getId(), parameters);
    }

    executeAvailableJobs();

    List<ProcessDefinitionStatistics> statistics =
        managementService
        .createProcessDefinitionStatisticsQuery()
        .includeFailedJobs()
        .includeIncidents()
        .list();

    Assert.assertEquals(2, statistics.size());

    ProcessDefinitionStatistics definitionResult = statistics.get(0);
    Assert.assertEquals(1, definitionResult.getInstances());
    Assert.assertEquals(1, definitionResult.getFailedJobs());

    List<IncidentStatistics> incidentStatistics = definitionResult.getIncidentStatistics();
    assertFalse(incidentStatistics.isEmpty());
    assertEquals(1, incidentStatistics.size());

    IncidentStatistics incident = incidentStatistics.get(0);

    assertEquals(Incident.FAILED_JOB_HANDLER_TYPE, incident.getIncidentType());
    assertEquals(1, incident.getIncidentCount());

    definitionResult = statistics.get(1);
    Assert.assertEquals(1, definitionResult.getInstances());
    Assert.assertEquals(1, definitionResult.getFailedJobs());

    incidentStatistics = definitionResult.getIncidentStatistics();
    assertFalse(incidentStatistics.isEmpty());
    assertEquals(1, incidentStatistics.size());

    incident = incidentStatistics.get(0);

    assertEquals(Incident.FAILED_JOB_HANDLER_TYPE, incident.getIncidentType());
    assertEquals(1, incident.getIncidentCount());

    repositoryService.deleteDeployment(deployment.getId(), true);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testStatisticsQueryWithFailedJobs.bpmn20.xml")
  public void testProcessDefinitionStatisticsQueryForMultipleVersionsWithIncidentType() {
    org.camunda.bpm.engine.repository.Deployment deployment =
        repositoryService.createDeployment()
          .addClasspathResource("org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testStatisticsQueryWithFailedJobs.bpmn20.xml")
          .deploy();

    List<ProcessDefinition> definitions =
        repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("ExampleProcess")
        .list();

    for (ProcessDefinition definition : definitions) {
      runtimeService.startProcessInstanceById(definition.getId());
    }

    executeAvailableJobs();

    List<ProcessDefinitionStatistics> statistics =
        managementService
        .createProcessDefinitionStatisticsQuery()
        .includeFailedJobs()
        .includeIncidentsForType("failedJob")
        .list();

    Assert.assertEquals(2, statistics.size());

    ProcessDefinitionStatistics definitionResult = statistics.get(0);
    Assert.assertEquals(1, definitionResult.getInstances());
    Assert.assertEquals(0, definitionResult.getFailedJobs());

    assertTrue(definitionResult.getIncidentStatistics().isEmpty());

    definitionResult = statistics.get(1);
    Assert.assertEquals(1, definitionResult.getInstances());
    Assert.assertEquals(0, definitionResult.getFailedJobs());

    assertTrue(definitionResult.getIncidentStatistics().isEmpty());

    repositoryService.deleteDeployment(deployment.getId(), true);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testStatisticsQueryWithFailedJobs.bpmn20.xml")
  public void testProcessDefinitionStatisticsQueryPagination() {
    org.camunda.bpm.engine.repository.Deployment deployment =
        repositoryService.createDeployment()
          .addClasspathResource("org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testStatisticsQuery.bpmn20.xml")
          .deploy();

    List<ProcessDefinition> definitions =
        repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("ExampleProcess")
        .list();

    for (ProcessDefinition definition : definitions) {
      runtimeService.startProcessInstanceById(definition.getId());
    }

    List<ProcessDefinitionStatistics> statistics =
        managementService.createProcessDefinitionStatisticsQuery().includeFailedJobs().listPage(0, 1);

    Assert.assertEquals(1, statistics.size());

    repositoryService.deleteDeployment(deployment.getId(), true);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testCallActivityWithIncidentsWithoutFailedJobs.bpmn20.xml")
  public void testProcessDefinitionStatisticsQueryWithIncidentsWithoutFailedJobs() {
    runtimeService.startProcessInstanceByKey("callExampleSubProcess");

    executeAvailableJobs();

    List<ProcessDefinitionStatistics> statistics =
        managementService
        .createProcessDefinitionStatisticsQuery()
        .includeIncidents()
        .includeFailedJobs()
        .list();

    Assert.assertEquals(2, statistics.size());

    ProcessDefinitionStatistics callExampleSubProcessStaticstics = null;
    ProcessDefinitionStatistics exampleSubProcessStaticstics = null;

    for (ProcessDefinitionStatistics current : statistics) {
      if (current.getKey().equals("callExampleSubProcess")) {
        callExampleSubProcessStaticstics = current;
      } else if (current.getKey().equals("ExampleProcess")) {
        exampleSubProcessStaticstics = current;
      } else {
        fail(current.getKey() + " was not expected.");
      }
    }

    assertNotNull(callExampleSubProcessStaticstics);
    assertNotNull(exampleSubProcessStaticstics);

    // "super" process definition
    assertEquals(1, callExampleSubProcessStaticstics.getInstances());
    assertEquals(0, callExampleSubProcessStaticstics.getFailedJobs());

    assertFalse(callExampleSubProcessStaticstics.getIncidentStatistics().isEmpty());
    assertEquals(1, callExampleSubProcessStaticstics.getIncidentStatistics().size());

    IncidentStatistics incidentStatistics = callExampleSubProcessStaticstics.getIncidentStatistics().get(0);
    assertEquals(Incident.FAILED_JOB_HANDLER_TYPE, incidentStatistics.getIncidentType());
    assertEquals(1, incidentStatistics.getIncidentCount());

    // "called" process definition
    assertEquals(1, exampleSubProcessStaticstics.getInstances());
    assertEquals(1, exampleSubProcessStaticstics.getFailedJobs());

    assertFalse(exampleSubProcessStaticstics.getIncidentStatistics().isEmpty());
    assertEquals(1, exampleSubProcessStaticstics.getIncidentStatistics().size());

    incidentStatistics = exampleSubProcessStaticstics.getIncidentStatistics().get(0);
    assertEquals(Incident.FAILED_JOB_HANDLER_TYPE, incidentStatistics.getIncidentType());
    assertEquals(1, incidentStatistics.getIncidentCount());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testFailedTimerStartEvent.bpmn20.xml")
  public void testQueryByIncidentsWithFailedTimerStartEvent() {

    executeAvailableJobs();

    List<ProcessDefinitionStatistics> statistics =
        managementService
        .createProcessDefinitionStatisticsQuery()
        .includeIncidents()
        .list();

    assertEquals(1, statistics.size());

    ProcessDefinitionStatistics result = statistics.get(0);

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

    List<ProcessDefinitionStatistics> statistics =
        managementService
        .createProcessDefinitionStatisticsQuery()
        .includeIncidentsForType(Incident.FAILED_JOB_HANDLER_TYPE)
        .list();

    assertEquals(1, statistics.size());

    ProcessDefinitionStatistics result = statistics.get(0);

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

    List<ProcessDefinitionStatistics> statistics =
        managementService
        .createProcessDefinitionStatisticsQuery()
        .includeFailedJobs()
        .list();

    assertEquals(1, statistics.size());

    ProcessDefinitionStatistics result = statistics.get(0);

    // there is no running instance
    assertEquals(0, result.getInstances());
    // but there is one failed timer job
    assertEquals(1, result.getFailedJobs());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testFailedTimerStartEvent.bpmn20.xml")
  public void testQueryByFailedJobsAndIncidentsWithFailedTimerStartEvent() {

    executeAvailableJobs();

    List<ProcessDefinitionStatistics> statistics =
        managementService
        .createProcessDefinitionStatisticsQuery()
        .includeFailedJobs()
        .includeIncidents()
        .list();

    assertEquals(1, statistics.size());

    ProcessDefinitionStatistics result = statistics.get(0);

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

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testCallActivityWithIncidentsWithoutFailedJobs.bpmn20.xml")
  public void testIncludeRootIncidentsOnly() {
    runtimeService.startProcessInstanceByKey("callExampleSubProcess");

    executeAvailableJobs();

    List<ProcessDefinitionStatistics> statistics =
        managementService
        .createProcessDefinitionStatisticsQuery()
        .includeRootIncidents()
        .list();

    // two process definitions
    assertEquals(2, statistics.size());

    for (ProcessDefinitionStatistics definitionResult : statistics) {

      if (definitionResult.getKey().equals("callExampleSubProcess")) {
        // there is no root incidents
        assertTrue(definitionResult.getIncidentStatistics().isEmpty());

      } else if (definitionResult.getKey().equals("ExampleProcess")) {
        // there is one root incident
        assertFalse(definitionResult.getIncidentStatistics().isEmpty());
        assertEquals(1, definitionResult.getIncidentStatistics().size());
        assertEquals(1, definitionResult.getIncidentStatistics().get(0).getIncidentCount());

      } else {
        // fail if the process definition key does not match
        fail();
      }
    }
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testCallActivityWithIncidentsWithoutFailedJobs.bpmn20.xml")
  public void testIncludeRootIncidentsFails() {
    runtimeService.startProcessInstanceByKey("callExampleSubProcess");

    executeAvailableJobs();

    try {
        managementService
        .createProcessDefinitionStatisticsQuery()
        .includeIncidents()
        .includeRootIncidents()
        .list();
    } catch (ProcessEngineException e) {
      Assert.assertThat(e.getMessage(), containsString("It is not possible to use includeIncident() and includeRootIncidents() to execute one query"));
    }
  }

  public void testProcessDefinitionStatisticsProperties() {
    String resourceName = "org/camunda/bpm/engine/test/api/mgmt/ProcessDefinitionStatisticsQueryTest.testProcessDefinitionStatisticsProperties.bpmn20.xml";
    String deploymentId = deploymentForTenant("tenant1", resourceName);

    ProcessDefinitionStatistics processDefinitionStatistics = managementService.createProcessDefinitionStatisticsQuery().singleResult();

    assertEquals("testProcess", processDefinitionStatistics.getKey());
    assertEquals("process name", processDefinitionStatistics.getName());
    assertEquals("Examples", processDefinitionStatistics.getCategory());
    assertEquals(null, processDefinitionStatistics.getDescription()); // it is not parsed for the statistics query
    assertEquals("tenant1", processDefinitionStatistics.getTenantId());
    assertEquals("v0.1.0", processDefinitionStatistics.getVersionTag());
    assertEquals(deploymentId, processDefinitionStatistics.getDeploymentId());
    assertEquals(resourceName, processDefinitionStatistics.getResourceName());
    assertEquals(null, processDefinitionStatistics.getDiagramResourceName());
    assertEquals(1, processDefinitionStatistics.getVersion());
    assertEquals(0, processDefinitionStatistics.getInstances());
    assertEquals(0, processDefinitionStatistics.getFailedJobs());
    assertTrue(processDefinitionStatistics.getIncidentStatistics().isEmpty());
  }

}
