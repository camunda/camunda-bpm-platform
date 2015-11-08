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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.management.ActivityStatistics;
import org.camunda.bpm.engine.management.IncidentStatistics;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Test;

public class ActivityStatisticsQueryTest extends PluggableProcessEngineTestCase {

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testStatisticsQueryWithFailedJobs.bpmn20.xml")
  public void testActivityStatisticsQueryWithoutFailedJobs() {

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("fail", true);

    runtimeService.startProcessInstanceByKey("ExampleProcess", parameters);

    executeAvailableJobs();

    ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("ExampleProcess").singleResult();

    List<ActivityStatistics> statistics =
        managementService.createActivityStatisticsQuery(definition.getId()).list();

    Assert.assertEquals(1, statistics.size());

    ActivityStatistics activityResult = statistics.get(0);
    Assert.assertEquals(1, activityResult.getInstances());
    Assert.assertEquals("theServiceTask", activityResult.getId());
    Assert.assertEquals(0, activityResult.getFailedJobs());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testStatisticsQueryWithFailedJobs.bpmn20.xml")
  public void testActivityStatisticsQueryWithIncidents() {

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("fail", true);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("ExampleProcess", parameters);

    executeAvailableJobs();

    List<ActivityStatistics> statistics =
        managementService
        .createActivityStatisticsQuery(processInstance.getProcessDefinitionId())
        .includeIncidents()
        .list();

    Assert.assertEquals(1, statistics.size());

    ActivityStatistics activityResult = statistics.get(0);

    List<IncidentStatistics> incidentStatistics = activityResult.getIncidentStatistics();
    assertFalse(incidentStatistics.isEmpty());
    assertEquals(1, incidentStatistics.size());

    IncidentStatistics incident = incidentStatistics.get(0);
    assertEquals(Incident.FAILED_JOB_HANDLER_TYPE, incident.getIncidentType());
    assertEquals(1, incident.getIncidentCount());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testStatisticsQueryWithFailedJobs.bpmn20.xml")
  public void testActivityStatisticsQueryWithIncidentType() {

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("fail", true);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("ExampleProcess", parameters);

    executeAvailableJobs();

    List<ActivityStatistics> statistics =
        managementService
        .createActivityStatisticsQuery(processInstance.getProcessDefinitionId())
        .includeIncidentsForType("failedJob")
        .list();

    Assert.assertEquals(1, statistics.size());

    ActivityStatistics activityResult = statistics.get(0);

    List<IncidentStatistics> incidentStatistics = activityResult.getIncidentStatistics();
    assertFalse(incidentStatistics.isEmpty());
    assertEquals(1, incidentStatistics.size());

    IncidentStatistics incident = incidentStatistics.get(0);
    assertEquals(Incident.FAILED_JOB_HANDLER_TYPE, incident.getIncidentType());
    assertEquals(1, incident.getIncidentCount());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testStatisticsQueryWithFailedJobs.bpmn20.xml")
  public void testActivityStatisticsQueryWithInvalidIncidentType() {

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("fail", true);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("ExampleProcess", parameters);

    executeAvailableJobs();

    List<ActivityStatistics> statistics =
        managementService
        .createActivityStatisticsQuery(processInstance.getProcessDefinitionId())
        .includeIncidentsForType("invalid")
        .list();

    Assert.assertEquals(1, statistics.size());

    ActivityStatistics activityResult = statistics.get(0);

    List<IncidentStatistics> incidentStatistics = activityResult.getIncidentStatistics();
    assertTrue(incidentStatistics.isEmpty());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testCallActivityWithIncidentsWithoutFailedJobs.bpmn20.xml")
  public void testActivityStatisticsQueryWithIncidentsWithoutFailedJobs() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callExampleSubProcess");

    executeAvailableJobs();

    List<ActivityStatistics> statistics =
        managementService
        .createActivityStatisticsQuery(processInstance.getProcessDefinitionId())
        .includeIncidents()
        .includeFailedJobs()
        .list();

    Assert.assertEquals(1, statistics.size());

    ActivityStatistics activityResult = statistics.get(0);

    Assert.assertEquals("callSubProcess", activityResult.getId());
    Assert.assertEquals(0, activityResult.getFailedJobs()); // has no failed jobs

    List<IncidentStatistics> incidentStatistics = activityResult.getIncidentStatistics();
    assertFalse(incidentStatistics.isEmpty());
    assertEquals(1, incidentStatistics.size());

    IncidentStatistics incident = incidentStatistics.get(0);
    assertEquals(Incident.FAILED_JOB_HANDLER_TYPE, incident.getIncidentType());
    assertEquals(1, incident.getIncidentCount()); //... but has one incident
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testStatisticsQuery.bpmn20.xml")
  public void testActivityStatisticsQuery() {
    runtimeService.startProcessInstanceByKey("ExampleProcess");
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
    Assert.assertEquals(1, activityResult.getInstances());
    Assert.assertEquals("theTask", activityResult.getId());
    Assert.assertEquals(0, activityResult.getFailedJobs());
    assertTrue(activityResult.getIncidentStatistics().isEmpty());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testStatisticsQuery.bpmn20.xml")
  public void testActivityStatisticsQueryCount() {
    runtimeService.startProcessInstanceByKey("ExampleProcess");
    ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("ExampleProcess").singleResult();

    long count =
        managementService
        .createActivityStatisticsQuery(definition.getId())
        .includeFailedJobs()
        .includeIncidents()
        .count();

    Assert.assertEquals(1, count);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testStatisticsQuery.bpmn20.xml")
  public void testManyInstancesActivityStatisticsQuery() {
    runtimeService.startProcessInstanceByKey("ExampleProcess");
    runtimeService.startProcessInstanceByKey("ExampleProcess");
    runtimeService.startProcessInstanceByKey("ExampleProcess");

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

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testMultiInstanceStatisticsQuery.bpmn20.xml")
  public void testParallelMultiInstanceActivityStatisticsQuery() {
    runtimeService.startProcessInstanceByKey("MIExampleProcess");
    ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("MIExampleProcess").singleResult();

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

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testSubprocessStatisticsQuery.bpmn20.xml")
  public void testSubprocessActivityStatisticsQuery() {
    runtimeService.startProcessInstanceByKey("ExampleProcess");

    ProcessDefinition definition = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("ExampleProcess")
        .singleResult();

    List<ActivityStatistics> statistics =
        managementService
        .createActivityStatisticsQuery(definition.getId())
        .list();

    Assert.assertEquals(1, statistics.size());

    ActivityStatistics result = statistics.get(0);
    Assert.assertEquals(1, result.getInstances());
    Assert.assertEquals("subProcessTask", result.getId());
  }

  @Test
  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testCallActivityStatisticsQuery.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testStatisticsQueryWithFailedJobs.bpmn20.xml"})
  public void testCallActivityActivityStatisticsQuery() {
    runtimeService.startProcessInstanceByKey("callExampleSubProcess");

    executeAvailableJobs();

    ProcessDefinition definition = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("ExampleProcess")
        .singleResult();

    List<ActivityStatistics> statistics =
        managementService
        .createActivityStatisticsQuery(definition.getId())
        .includeFailedJobs()
        .includeIncidents()
        .list();

    Assert.assertEquals(1, statistics.size());

    ActivityStatistics result = statistics.get(0);
    Assert.assertEquals(1, result.getInstances());
    Assert.assertEquals(0, result.getFailedJobs());
    assertTrue(result.getIncidentStatistics().isEmpty());

    ProcessDefinition callSubProcessDefinition = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("callExampleSubProcess")
        .singleResult();

    List<ActivityStatistics> callSubProcessStatistics =
        managementService
        .createActivityStatisticsQuery(callSubProcessDefinition.getId())
        .includeFailedJobs()
        .includeIncidents()
        .list();

    Assert.assertEquals(1, callSubProcessStatistics.size());

    result = callSubProcessStatistics.get(0);
    Assert.assertEquals(1, result.getInstances());
    Assert.assertEquals(0, result.getFailedJobs());
    assertTrue(result.getIncidentStatistics().isEmpty());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testActivityStatisticsQueryWithIntermediateTimer.bpmn20.xml")
  public void testActivityStatisticsQueryWithIntermediateTimer() {
    runtimeService.startProcessInstanceByKey("ExampleProcess");
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
    Assert.assertEquals(1, activityResult.getInstances());
    Assert.assertEquals("theTimer", activityResult.getId());
    Assert.assertEquals(0, activityResult.getFailedJobs());
    assertTrue(activityResult.getIncidentStatistics().isEmpty());
  }

  @Test
  public void testNullProcessDefinitionParameter() {
    try {
      managementService.createActivityStatisticsQuery(null).list();
      Assert.fail();
    } catch (ProcessEngineException e) {
      // expected
    }
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testParallelGatewayStatisticsQuery.bpmn20.xml")
  public void testActivityStatisticsQueryPagination() {

    ProcessDefinition definition =
        repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("ParGatewayExampleProcess")
        .singleResult();

    runtimeService.startProcessInstanceById(definition.getId());

    List<ActivityStatistics> statistics =
        managementService
        .createActivityStatisticsQuery(definition.getId())
        .includeFailedJobs()
        .includeIncidents()
        .listPage(0, 1);

    Assert.assertEquals(1, statistics.size());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testParallelGatewayStatisticsQuery.bpmn20.xml")
  public void testParallelGatewayActivityStatisticsQuery() {

    ProcessDefinition definition =
        repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("ParGatewayExampleProcess")
        .singleResult();

    runtimeService.startProcessInstanceById(definition.getId());

    List<ActivityStatistics> statistics =
        managementService
        .createActivityStatisticsQuery(definition.getId())
        .list();

    Assert.assertEquals(2, statistics.size());

    for (ActivityStatistics result : statistics) {
      Assert.assertEquals(1, result.getInstances());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testFailedTimerStartEvent.bpmn20.xml")
  public void testQueryByIncidentsWithFailedTimerStartEvent() {

    ProcessDefinition definition =
        repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("process")
        .singleResult();

    executeAvailableJobs();

    List<ActivityStatistics> statistics =
        managementService
        .createActivityStatisticsQuery(definition.getId())
        .includeIncidents()
        .list();

    assertEquals(1, statistics.size());

    ActivityStatistics result = statistics.get(0);

    assertEquals("theStart", result.getId());

    // there is no running activity instance
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

    ProcessDefinition definition =
        repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("process")
        .singleResult();

    executeAvailableJobs();

    List<ActivityStatistics> statistics =
        managementService
        .createActivityStatisticsQuery(definition.getId())
        .includeIncidentsForType(Incident.FAILED_JOB_HANDLER_TYPE)
        .list();

    assertEquals(1, statistics.size());

    ActivityStatistics result = statistics.get(0);

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

    ProcessDefinition definition =
        repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("process")
        .singleResult();

    executeAvailableJobs();

    List<ActivityStatistics> statistics =
        managementService
        .createActivityStatisticsQuery(definition.getId())
        .includeFailedJobs()
        .list();

    assertEquals(1, statistics.size());

    ActivityStatistics result = statistics.get(0);

    // there is no running instance
    assertEquals(0, result.getInstances());
    // but there is one failed timer job
    assertEquals(1, result.getFailedJobs());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testFailedTimerStartEvent.bpmn20.xml")
  public void testQueryByFailedJobsAndIncidentsWithFailedTimerStartEvent() {

    ProcessDefinition definition =
        repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("process")
        .singleResult();

    executeAvailableJobs();

    List<ActivityStatistics> statistics =
        managementService
        .createActivityStatisticsQuery(definition.getId())
        .includeFailedJobs()
        .includeIncidents()
        .list();

    assertEquals(1, statistics.size());

    ActivityStatistics result = statistics.get(0);

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
  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testStatisticsQuery.bpmn20.xml")
  public void FAILING_testActivityStatisticsQueryWithNoInstances() {

    ProcessDefinition definition =
        repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("ExampleProcess")
        .singleResult();

    List<ActivityStatistics> statistics =
        managementService
        .createActivityStatisticsQuery(definition.getId())
        .list();

    Assert.assertEquals(1, statistics.size());
    ActivityStatistics result = statistics.get(0);
    Assert.assertEquals("theTask", result.getId());
    Assert.assertEquals(0, result.getInstances());
    Assert.assertEquals(0, result.getFailedJobs());

  }
}
