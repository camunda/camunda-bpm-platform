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
package org.camunda.bpm.engine.test.api.runtime;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.incident.FailedJobIncidentHandler;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.IncidentQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author roman.smirnov
 */
public class IncidentQueryTest extends PluggableProcessEngineTestCase {

  private static String PROCESS_DEFINITION_KEY = "oneFailingServiceTaskProcess";

  private List<String> processInstanceIds;

  /**
   * Setup starts 4 process instances of oneFailingServiceTaskProcess.
   */
  protected void setUp() throws Exception {
    super.setUp();
    repositoryService.createDeployment()
      .addClasspathResource("org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml")
      .deploy();

    processInstanceIds = new ArrayList<String>();
    for (int i = 0; i < 4; i++) {
      processInstanceIds.add(runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, i + "").getId());
    }

    executeAvailableJobs();
  }

  protected void tearDown() throws Exception {
    for (org.camunda.bpm.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
    super.tearDown();
  }

  public void testQuery() {
    IncidentQuery query = runtimeService.createIncidentQuery();
    assertEquals(4, query.count());

    List<Incident> incidents = query.list();
    assertFalse(incidents.isEmpty());
    assertEquals(4, incidents.size());
  }

  public void testQueryByIncidentType() {
    IncidentQuery query = runtimeService.createIncidentQuery().incidentType(FailedJobIncidentHandler.INCIDENT_HANDLER_TYPE);
    assertEquals(4, query.count());

    List<Incident> incidents = query.list();
    assertFalse(incidents.isEmpty());
    assertEquals(4, incidents.size());
  }

  public void testQueryByInvalidIncidentType() {
    IncidentQuery query = runtimeService.createIncidentQuery().incidentType("invalid");

    assertEquals(0, query.count());

    List<Incident> incidents = query.list();
    assertTrue(incidents.isEmpty());

    Incident incident = query.singleResult();
    assertNull(incident);
  }

  public void testQueryByIncidentMessage() {
    IncidentQuery query = runtimeService.createIncidentQuery().incidentMessage("Expected exception.");
    assertEquals(4, query.count());

    List<Incident> incidents = query.list();
    assertFalse(incidents.isEmpty());
    assertEquals(4, incidents.size());
  }

  public void testQueryByInvalidIncidentMessage() {
    IncidentQuery query = runtimeService.createIncidentQuery().incidentMessage("invalid");

    assertEquals(0, query.count());

    List<Incident> incidents = query.list();
    assertTrue(incidents.isEmpty());

    Incident incident = query.singleResult();
    assertNull(incident);
  }

  public void testQueryByProcessDefinitionId() {
    String processDefinitionId = repositoryService.createProcessDefinitionQuery().singleResult().getId();

    IncidentQuery query = runtimeService.createIncidentQuery().processDefinitionId(processDefinitionId);
    assertEquals(4, query.count());

    List<Incident> incidents = query.list();
    assertFalse(incidents.isEmpty());
    assertEquals(4, incidents.size());
  }

  public void testQueryByInvalidProcessDefinitionId() {
    IncidentQuery query = runtimeService.createIncidentQuery().processDefinitionId("invalid");

    assertEquals(0, query.count());

    List<Incident> incidents = query.list();
    assertTrue(incidents.isEmpty());

    Incident incident = query.singleResult();
    assertNull(incident);
  }

  public void testQueryByProcessInstanceId() {
    IncidentQuery query = runtimeService.createIncidentQuery().processInstanceId(processInstanceIds.get(0));

    assertEquals(1, query.count());

    List<Incident> incidents = query.list();
    assertFalse(incidents.isEmpty());
    assertEquals(1, incidents.size());

    Incident incident = query.singleResult();
    assertNotNull(incident);
  }

  public void testQueryByInvalidProcessInstanceId() {
    IncidentQuery query = runtimeService.createIncidentQuery().processInstanceId("invalid");

    assertEquals(0, query.count());

    List<Incident> incidents = query.list();
    assertTrue(incidents.isEmpty());

    Incident incident = query.singleResult();
    assertNull(incident);
  }

  public void testQueryByIncidentId() {
    Incident incident= runtimeService.createIncidentQuery().processInstanceId(processInstanceIds.get(0)).singleResult();
    assertNotNull(incident);

    IncidentQuery query = runtimeService.createIncidentQuery().incidentId(incident.getId());

    assertEquals(1, query.count());

    List<Incident> incidents = query.list();
    assertFalse(incidents.isEmpty());
    assertEquals(1, incidents.size());
  }

  public void testQueryByInvalidIncidentId() {
    IncidentQuery query = runtimeService.createIncidentQuery().incidentId("invalid");

    assertEquals(0, query.count());

    List<Incident> incidents = query.list();
    assertTrue(incidents.isEmpty());

    Incident incident = query.singleResult();
    assertNull(incident);
  }

  public void testQueryByExecutionId() {
    Execution execution = runtimeService.createExecutionQuery().processInstanceId(processInstanceIds.get(0)).singleResult();
    assertNotNull(execution);

    IncidentQuery query = runtimeService.createIncidentQuery().executionId(execution.getId());

    assertEquals(1, query.count());

    List<Incident> incidents = query.list();
    assertFalse(incidents.isEmpty());
    assertEquals(1, incidents.size());
  }

  public void testQueryByInvalidExecutionId() {
    IncidentQuery query = runtimeService.createIncidentQuery().executionId("invalid");

    assertEquals(0, query.count());

    List<Incident> incidents = query.list();
    assertTrue(incidents.isEmpty());

    Incident incident = query.singleResult();
    assertNull(incident);
  }

  public void testQueryByActivityId() {
    IncidentQuery query = runtimeService.createIncidentQuery().activityId("theServiceTask");
    assertEquals(4, query.count());

    List<Incident> incidents = query.list();
    assertFalse(incidents.isEmpty());
    assertEquals(4, incidents.size());
  }

  public void testQueryByInvalidActivityId() {
    IncidentQuery query = runtimeService.createIncidentQuery().activityId("invalid");

    assertEquals(0, query.count());

    List<Incident> incidents = query.list();
    assertTrue(incidents.isEmpty());

    Incident incident = query.singleResult();
    assertNull(incident);
  }

  public void testQueryByConfiguration() {
    String jobId = managementService.createJobQuery().processInstanceId(processInstanceIds.get(0)).singleResult().getId();

    IncidentQuery query = runtimeService.createIncidentQuery().configuration(jobId);
    assertEquals(1, query.count());

    List<Incident> incidents = query.list();
    assertFalse(incidents.isEmpty());
    assertEquals(1, incidents.size());
  }

  public void testQueryByInvalidConfiguration() {
    IncidentQuery query = runtimeService.createIncidentQuery().configuration("invalid");

    assertEquals(0, query.count());

    List<Incident> incidents = query.list();
    assertTrue(incidents.isEmpty());

    Incident incident = query.singleResult();
    assertNull(incident);
  }

  public void testQueryByCauseIncidentIdEqualsNull() {
    IncidentQuery query = runtimeService.createIncidentQuery().causeIncidentId(null);
    assertEquals(4, query.count());

    List<Incident> incidents = query.list();
    assertFalse(incidents.isEmpty());
    assertEquals(4, incidents.size());
  }

  public void testQueryByInvalidCauseIncidentId() {
    IncidentQuery query = runtimeService.createIncidentQuery().causeIncidentId("invalid");
    assertEquals(0, query.count());

    List<Incident> incidents = query.list();
    assertTrue(incidents.isEmpty());
    assertEquals(0, incidents.size());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/IncidentQueryTest.testQueryByCauseIncidentId.bpmn"})
  public void testQueryByCauseIncidentId() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callFailingProcess");

    executeAvailableJobs();

    ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(processInstance.getId()).singleResult();
    assertNotNull(subProcessInstance);

    Incident causeIncident = runtimeService.createIncidentQuery().processInstanceId(subProcessInstance.getId()).singleResult();
    assertNotNull(causeIncident);

    IncidentQuery query = runtimeService.createIncidentQuery().causeIncidentId(causeIncident.getId());
    assertEquals(2, query.count());

    List<Incident> incidents = query.list();
    assertFalse(incidents.isEmpty());
    assertEquals(2, incidents.size());
  }

  public void testQueryByRootCauseIncidentIdEqualsNull() {
    IncidentQuery query = runtimeService.createIncidentQuery().rootCauseIncidentId(null);
    assertEquals(4, query.count());

    List<Incident> incidents = query.list();
    assertFalse(incidents.isEmpty());
    assertEquals(4, incidents.size());
  }

  public void testQueryByRootInvalidCauseIncidentId() {
    IncidentQuery query = runtimeService.createIncidentQuery().rootCauseIncidentId("invalid");
    assertEquals(0, query.count());

    List<Incident> incidents = query.list();
    assertTrue(incidents.isEmpty());
    assertEquals(0, incidents.size());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/IncidentQueryTest.testQueryByRootCauseIncidentId.bpmn",
      "org/camunda/bpm/engine/test/api/runtime/IncidentQueryTest.testQueryByCauseIncidentId.bpmn"})
  public void testQueryByRootCauseIncidentId() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callFailingCallActivity");

    executeAvailableJobs();

    ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(processInstance.getId()).singleResult();
    assertNotNull(subProcessInstance);

    ProcessInstance failingSubProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(subProcessInstance.getId()).singleResult();
    assertNotNull(subProcessInstance);

    Incident incident = runtimeService.createIncidentQuery().processInstanceId(failingSubProcessInstance.getId()).singleResult();
    assertNotNull(incident);

    IncidentQuery query = runtimeService.createIncidentQuery().rootCauseIncidentId(incident.getId());
    assertEquals(3, query.count());

    List<Incident> incidents = query.list();
    assertFalse(incidents.isEmpty());
    assertEquals(3, incidents.size());

    try {
      query.singleResult();
      fail();
    } catch (ProcessEngineException e) {
      // Exception is expected
    }

  }

  public void testQueryPaging() {
    assertEquals(4, runtimeService.createIncidentQuery().listPage(0, 4).size());
    assertEquals(1, runtimeService.createIncidentQuery().listPage(2, 1).size());
    assertEquals(2, runtimeService.createIncidentQuery().listPage(1, 2).size());
    assertEquals(3, runtimeService.createIncidentQuery().listPage(1, 4).size());
  }

  public void testQuerySorting() {
    assertEquals(4, runtimeService.createIncidentQuery().orderByIncidentId().asc().list().size());
    assertEquals(4, runtimeService.createIncidentQuery().orderByIncidentTimestamp().asc().list().size());
    assertEquals(4, runtimeService.createIncidentQuery().orderByIncidentType().asc().list().size());
    assertEquals(4, runtimeService.createIncidentQuery().orderByExecutionId().asc().list().size());
    assertEquals(4, runtimeService.createIncidentQuery().orderByActivityId().asc().list().size());
    assertEquals(4, runtimeService.createIncidentQuery().orderByProcessInstanceId().asc().list().size());
    assertEquals(4, runtimeService.createIncidentQuery().orderByProcessDefinitionId().asc().list().size());
    assertEquals(4, runtimeService.createIncidentQuery().orderByCauseIncidentId().asc().list().size());
    assertEquals(4, runtimeService.createIncidentQuery().orderByRootCauseIncidentId().asc().list().size());
    assertEquals(4, runtimeService.createIncidentQuery().orderByConfiguration().asc().list().size());

    assertEquals(4, runtimeService.createIncidentQuery().orderByIncidentId().desc().list().size());
    assertEquals(4, runtimeService.createIncidentQuery().orderByIncidentTimestamp().desc().list().size());
    assertEquals(4, runtimeService.createIncidentQuery().orderByIncidentType().desc().list().size());
    assertEquals(4, runtimeService.createIncidentQuery().orderByExecutionId().desc().list().size());
    assertEquals(4, runtimeService.createIncidentQuery().orderByActivityId().desc().list().size());
    assertEquals(4, runtimeService.createIncidentQuery().orderByProcessInstanceId().desc().list().size());
    assertEquals(4, runtimeService.createIncidentQuery().orderByProcessDefinitionId().desc().list().size());
    assertEquals(4, runtimeService.createIncidentQuery().orderByCauseIncidentId().desc().list().size());
    assertEquals(4, runtimeService.createIncidentQuery().orderByRootCauseIncidentId().desc().list().size());
    assertEquals(4, runtimeService.createIncidentQuery().orderByConfiguration().desc().list().size());

  }
}
