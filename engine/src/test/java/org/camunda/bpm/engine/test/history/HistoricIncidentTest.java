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
package org.camunda.bpm.engine.test.history;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.history.HistoricIncidentQuery;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;

/**
 *
 * @author Roman Smirnov
 *
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoricIncidentTest extends PluggableProcessEngineTestCase {

  private static String PROCESS_DEFINITION_KEY = "oneFailingServiceTaskProcess";

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testPropertiesOfHistoricIncident() {
    startProcessInstance(PROCESS_DEFINITION_KEY);

    Incident incident = runtimeService.createIncidentQuery().singleResult();
    assertNotNull(incident);

    HistoricIncident historicIncident = historyService.createHistoricIncidentQuery().singleResult();
    assertNotNull(historicIncident);

    assertEquals(incident.getId(), historicIncident.getId());
    assertEquals(incident.getIncidentTimestamp(), historicIncident.getCreateTime());
    assertNull(historicIncident.getEndTime());
    assertEquals(incident.getIncidentType(), historicIncident.getIncidentType());
    assertEquals(incident.getIncidentMessage(), historicIncident.getIncidentMessage());
    assertEquals(incident.getExecutionId(), historicIncident.getExecutionId());
    assertEquals(incident.getActivityId(), historicIncident.getActivityId());
    assertEquals(incident.getProcessInstanceId(), historicIncident.getProcessInstanceId());
    assertEquals(incident.getProcessDefinitionId(), historicIncident.getProcessDefinitionId());
    assertEquals(PROCESS_DEFINITION_KEY, historicIncident.getProcessDefinitionKey());
    assertEquals(incident.getCauseIncidentId(), historicIncident.getCauseIncidentId());
    assertEquals(incident.getRootCauseIncidentId(), historicIncident.getRootCauseIncidentId());
    assertEquals(incident.getConfiguration(), historicIncident.getConfiguration());
    assertEquals(incident.getJobDefinitionId(), historicIncident.getJobDefinitionId());

    assertTrue(historicIncident.isOpen());
    assertFalse(historicIncident.isDeleted());
    assertFalse(historicIncident.isResolved());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testCreateSecondHistoricIncident() {
    startProcessInstance(PROCESS_DEFINITION_KEY);

    String jobId = managementService.createJobQuery().singleResult().getId();
    managementService.setJobRetries(jobId, 1);

    executeAvailableJobs();

    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery();
    assertEquals(2, query.count());

    // the first historic incident has been resolved
    assertEquals(1, query.resolved().count());

    query = historyService.createHistoricIncidentQuery();
    // a new historic incident exists which is open
    assertEquals(1, query.open().count());
  }


  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testSetHistoricIncidentToResolved() {
    startProcessInstance(PROCESS_DEFINITION_KEY);

    String jobId = managementService.createJobQuery().singleResult().getId();
    managementService.setJobRetries(jobId, 1);

    HistoricIncident historicIncident = historyService.createHistoricIncidentQuery().singleResult();
    assertNotNull(historicIncident);

    assertNotNull(historicIncident.getEndTime());

    assertFalse(historicIncident.isOpen());
    assertFalse(historicIncident.isDeleted());
    assertTrue(historicIncident.isResolved());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/history/HistoricIncidentQueryTest.testQueryByCauseIncidentId.bpmn20.xml",
  "org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testSetHistoricIncidentToResolvedRecursive() {
    startProcessInstance("process");

    String jobId = managementService.createJobQuery().singleResult().getId();
    managementService.setJobRetries(jobId, 1);

    List<HistoricIncident> historicIncidents = historyService.createHistoricIncidentQuery().list();

    for (HistoricIncident historicIncident : historicIncidents) {
      assertNotNull(historicIncident.getEndTime());

      assertFalse(historicIncident.isOpen());
      assertFalse(historicIncident.isDeleted());
      assertTrue(historicIncident.isResolved());
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testSetHistoricIncidentToDeleted() {
    startProcessInstance(PROCESS_DEFINITION_KEY);

    String processInstanceId = runtimeService.createProcessInstanceQuery().singleResult().getId();
    runtimeService.deleteProcessInstance(processInstanceId, null);

    HistoricIncident historicIncident = historyService.createHistoricIncidentQuery().singleResult();
    assertNotNull(historicIncident);

    assertNotNull(historicIncident.getEndTime());

    assertFalse(historicIncident.isOpen());
    assertTrue(historicIncident.isDeleted());
    assertFalse(historicIncident.isResolved());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/history/HistoricIncidentQueryTest.testQueryByCauseIncidentId.bpmn20.xml",
  "org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testSetHistoricIncidentToDeletedRecursive() {
    startProcessInstance("process");

    String processInstanceId = runtimeService.createProcessInstanceQuery()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .singleResult()
        .getId();
    runtimeService.deleteProcessInstance(processInstanceId, null);

    List<HistoricIncident> historicIncidents = historyService.createHistoricIncidentQuery().list();

    for (HistoricIncident historicIncident : historicIncidents) {
      assertNotNull(historicIncident.getEndTime());

      assertFalse(historicIncident.isOpen());
      assertTrue(historicIncident.isDeleted());
      assertFalse(historicIncident.isResolved());
    }
  }

  @Deployment
  public void testCreateHistoricIncidentForNestedExecution () {
    startProcessInstance("process");

    Execution execution = runtimeService.createExecutionQuery()
        .activityId("serviceTask")
        .singleResult();
    assertNotNull(execution);

    HistoricIncident historicIncident = historyService.createHistoricIncidentQuery().singleResult();
    assertNotNull(historicIncident);

    assertEquals(execution.getId(), historicIncident.getExecutionId());
    assertEquals("serviceTask", historicIncident.getActivityId());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/history/HistoricIncidentQueryTest.testQueryByCauseIncidentId.bpmn20.xml",
  "org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testCreateRecursiveHistoricIncidents() {
    startProcessInstance("process");

    ProcessInstance pi1 = runtimeService.createProcessInstanceQuery()
        .processDefinitionKey("process")
        .singleResult();
    assertNotNull(pi1);

    ProcessInstance pi2 = runtimeService.createProcessInstanceQuery()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .singleResult();
    assertNotNull(pi2);

    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery();

    HistoricIncident rootCauseHistoricIncident = query.processInstanceId(pi2.getId()).singleResult();
    assertNotNull(rootCauseHistoricIncident);

    // cause and root cause id is equal to the id of the root incident
    assertEquals(rootCauseHistoricIncident.getId(), rootCauseHistoricIncident.getCauseIncidentId());
    assertEquals(rootCauseHistoricIncident.getId(), rootCauseHistoricIncident.getRootCauseIncidentId());

    HistoricIncident historicIncident = query.processInstanceId(pi1.getId()).singleResult();
    assertNotNull(historicIncident);

    // cause and root cause id is equal to the id of the root incident
    assertEquals(rootCauseHistoricIncident.getId(), historicIncident.getCauseIncidentId());
    assertEquals(rootCauseHistoricIncident.getId(), historicIncident.getRootCauseIncidentId());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/history/HistoricIncidentTest.testCreateRecursiveHistoricIncidentsForNestedCallActivities.bpmn20.xml",
      "org/camunda/bpm/engine/test/history/HistoricIncidentQueryTest.testQueryByCauseIncidentId.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testCreateRecursiveHistoricIncidentsForNestedCallActivities() {
    startProcessInstance("process1");

    ProcessInstance pi1 = runtimeService.createProcessInstanceQuery()
        .processDefinitionKey("process1")
        .singleResult();
    assertNotNull(pi1);

    ProcessInstance pi2 = runtimeService.createProcessInstanceQuery()
        .processDefinitionKey("process")
        .singleResult();
    assertNotNull(pi2);

    ProcessInstance pi3 = runtimeService.createProcessInstanceQuery()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .singleResult();
    assertNotNull(pi3);

    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery();

    HistoricIncident rootCauseHistoricIncident = query.processInstanceId(pi3.getId()).singleResult();
    assertNotNull(rootCauseHistoricIncident);

    // cause and root cause id is equal to the id of the root incident
    assertEquals(rootCauseHistoricIncident.getId(), rootCauseHistoricIncident.getCauseIncidentId());
    assertEquals(rootCauseHistoricIncident.getId(), rootCauseHistoricIncident.getRootCauseIncidentId());

    HistoricIncident causeHistoricIncident = query.processInstanceId(pi2.getId()).singleResult();
    assertNotNull(causeHistoricIncident);

    // cause and root cause id is equal to the id of the root incident
    assertEquals(rootCauseHistoricIncident.getId(), causeHistoricIncident.getCauseIncidentId());
    assertEquals(rootCauseHistoricIncident.getId(), causeHistoricIncident.getRootCauseIncidentId());

    HistoricIncident historicIncident = query.processInstanceId(pi1.getId()).singleResult();
    assertNotNull(historicIncident);

    // cause and root cause id is equal to the id of the root incident
    assertEquals(causeHistoricIncident.getId(), historicIncident.getCauseIncidentId());
    assertEquals(rootCauseHistoricIncident.getId(), historicIncident.getRootCauseIncidentId());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testDoNotCreateNewIncident() {
    startProcessInstance(PROCESS_DEFINITION_KEY);

    ProcessInstance pi = runtimeService.createProcessInstanceQuery().singleResult();

    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery().processInstanceId(pi.getId());
    HistoricIncident incident = query.singleResult();
    assertNotNull(incident);

    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // set retries to 1 by job definition id
    managementService.setJobRetriesByJobDefinitionId(jobDefinition.getId(), 1);

    // the incident still exists
    HistoricIncident tmp = query.singleResult();
    assertEquals(incident.getId(), tmp.getId());
    assertNull(tmp.getEndTime());
    assertTrue(tmp.isOpen());

    // execute the available job (should fail again)
    executeAvailableJobs();

    // the incident still exists and there
    // should be not a new incident
    assertEquals(1, query.count());
    tmp = query.singleResult();
    assertEquals(incident.getId(), tmp.getId());
    assertNull(tmp.getEndTime());
    assertTrue(tmp.isOpen());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testSetRetriesByJobDefinitionIdResolveIncident() {
    startProcessInstance(PROCESS_DEFINITION_KEY);

    ProcessInstance pi = runtimeService.createProcessInstanceQuery().singleResult();

    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery().processInstanceId(pi.getId());
    HistoricIncident incident = query.singleResult();
    assertNotNull(incident);

    runtimeService.setVariable(pi.getId(), "fail", false);

    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // set retries to 1 by job definition id
    managementService.setJobRetriesByJobDefinitionId(jobDefinition.getId(), 1);

    // the incident still exists
    HistoricIncident tmp = query.singleResult();
    assertEquals(incident.getId(), tmp.getId());
    assertNull(tmp.getEndTime());
    assertTrue(tmp.isOpen());

    // execute the available job (should fail again)
    executeAvailableJobs();

    // the incident still exists and there
    // should be not a new incident
    assertEquals(1, query.count());
    tmp = query.singleResult();
    assertEquals(incident.getId(), tmp.getId());
    assertNotNull(tmp.getEndTime());
    assertTrue(tmp.isResolved());

    assertProcessEnded(pi.getId());
  }

  protected void startProcessInstance(String key) {
    startProcessInstances(key, 1);
  }

  protected void startProcessInstances(String key, int numberOfInstances) {
    for (int i = 0; i < numberOfInstances; i++) {
      runtimeService.startProcessInstanceByKey(key);
    }

    executeAvailableJobs();
  }

}
