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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.HistoricIncidentQuery;
import org.camunda.bpm.engine.impl.incident.FailedJobIncidentHandler;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.api.runtime.FailingDelegate;

/**
 *
 * @author Roman Smirnov
 *
 */
public class HistoricIncidentQueryTest extends PluggableProcessEngineTestCase {

  private static String PROCESS_DEFINITION_KEY = "oneFailingServiceTaskProcess";

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testQueryByIncidentId() {
    startProcessInstance(PROCESS_DEFINITION_KEY);

    String incidentId = historyService.createHistoricIncidentQuery().singleResult().getId();

    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery()
        .incidentId(incidentId);

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }

  public void testQueryByInvalidIncidentId() {
    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery();

    assertEquals(0, query.incidentId("invalid").list().size());
    assertEquals(0, query.incidentId("invalid").count());

    try {
      query.incidentId(null);
      fail("It was possible to set a null value as incidentId.");
    } catch (ProcessEngineException e) { }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testQueryByIncidentType() {
    startProcessInstance(PROCESS_DEFINITION_KEY);

    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery()
        .incidentType(FailedJobIncidentHandler.INCIDENT_HANDLER_TYPE);

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }

  public void testQueryByInvalidIncidentType() {
    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery();

    assertEquals(0, query.incidentType("invalid").list().size());
    assertEquals(0, query.incidentType("invalid").count());

    try {
      query.incidentType(null);
      fail("It was possible to set a null value as incidentType.");
    } catch (ProcessEngineException e) { }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testQueryByIncidentMessage() {
    startProcessInstance(PROCESS_DEFINITION_KEY);

    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery()
        .incidentMessage(FailingDelegate.EXCEPTION_MESSAGE);

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }

  public void testQueryByInvalidIncidentMessage() {
    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery();

    assertEquals(0, query.incidentMessage("invalid").list().size());
    assertEquals(0, query.incidentMessage("invalid").count());

    try {
      query.incidentMessage(null);
      fail("It was possible to set a null value as incidentMessage.");
    } catch (ProcessEngineException e) { }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testQueryByProcessDefinitionId() {
    startProcessInstance(PROCESS_DEFINITION_KEY);

    ProcessInstance pi = runtimeService.createProcessInstanceQuery().singleResult();

    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery()
        .processDefinitionId(pi.getProcessDefinitionId());

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }

  public void testQueryByInvalidProcessDefinitionId() {
    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery();

    assertEquals(0, query.processDefinitionId("invalid").list().size());
    assertEquals(0, query.processDefinitionId("invalid").count());

    try {
      query.processDefinitionId(null);
      fail("It was possible to set a null value as processDefinitionId.");
    } catch (ProcessEngineException e) { }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testQueryByProcessInstanceId() {
    startProcessInstance(PROCESS_DEFINITION_KEY);

    ProcessInstance pi = runtimeService.createProcessInstanceQuery().singleResult();

    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery()
        .processInstanceId(pi.getId());

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }

  public void testQueryByInvalidProcessInsanceId() {
    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery();

    assertEquals(0, query.processInstanceId("invalid").list().size());
    assertEquals(0, query.processInstanceId("invalid").count());

    try {
      query.processInstanceId(null);
      fail("It was possible to set a null value as processInstanceId.");
    } catch (ProcessEngineException e) { }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testQueryByExecutionId() {
    startProcessInstance(PROCESS_DEFINITION_KEY);

    ProcessInstance pi = runtimeService.createProcessInstanceQuery().singleResult();

    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery()
        .executionId(pi.getId());

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }

  public void testQueryByInvalidExecutionId() {
    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery();

    assertEquals(0, query.executionId("invalid").list().size());
    assertEquals(0, query.executionId("invalid").count());

    try {
      query.executionId(null);
      fail("It was possible to set a null value as executionId.");
    } catch (ProcessEngineException e) { }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testQueryByActivityId() {
    startProcessInstance(PROCESS_DEFINITION_KEY);

    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery()
        .activityId("theServiceTask");

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }

  public void testQueryByInvalidActivityId() {
    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery();

    assertEquals(0, query.activityId("invalid").list().size());
    assertEquals(0, query.activityId("invalid").count());

    try {
      query.activityId(null);
      fail("It was possible to set a null value as activityId.");
    } catch (ProcessEngineException e) { }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/history/HistoricIncidentQueryTest.testQueryByCauseIncidentId.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testQueryByCauseIncidentId() {
    startProcessInstance("process");

    String processInstanceId = runtimeService.createProcessInstanceQuery()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .singleResult()
        .getId();

    Incident incident = runtimeService.createIncidentQuery()
        .processInstanceId(processInstanceId)
        .singleResult();

    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery()
        .causeIncidentId(incident.getId());

    assertEquals(2, query.list().size());
    assertEquals(2, query.count());
  }

  public void testQueryByInvalidCauseIncidentId() {
    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery();

    assertEquals(0, query.causeIncidentId("invalid").list().size());
    assertEquals(0, query.causeIncidentId("invalid").count());

    try {
      query.causeIncidentId(null);
      fail("It was possible to set a null value as causeIncidentId.");
    } catch (ProcessEngineException e) { }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/history/HistoricIncidentQueryTest.testQueryByCauseIncidentId.bpmn20.xml",
  "org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testQueryByRootCauseIncidentId() {
    startProcessInstance("process");

    String processInstanceId = runtimeService.createProcessInstanceQuery()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .singleResult()
        .getId();

    Incident incident = runtimeService.createIncidentQuery()
        .processInstanceId(processInstanceId)
        .singleResult();

    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery()
        .rootCauseIncidentId(incident.getId());

    assertEquals(2, query.list().size());
    assertEquals(2, query.count());
  }

  public void testQueryByInvalidRootCauseIncidentId() {
    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery();

    assertEquals(0, query.rootCauseIncidentId("invalid").list().size());
    assertEquals(0, query.rootCauseIncidentId("invalid").count());

    try {
      query.rootCauseIncidentId(null);
      fail("It was possible to set a null value as rootCauseIncidentId.");
    } catch (ProcessEngineException e) { }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testQueryByConfiguration() {
    startProcessInstance(PROCESS_DEFINITION_KEY);

    String configuration = managementService.createJobQuery().singleResult().getId();

    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery()
        .configuration(configuration);

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }

  public void testQueryByInvalidConfigurationId() {
    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery();

    assertEquals(0, query.configuration("invalid").list().size());
    assertEquals(0, query.configuration("invalid").count());

    try {
      query.configuration(null);
      fail("It was possible to set a null value as configuration.");
    } catch (ProcessEngineException e) { }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testQueryByOpen() {
    startProcessInstance(PROCESS_DEFINITION_KEY);

    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery()
        .open();

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }

  public void testQueryByInvalidOpen() {
    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery();

    try {
      query.open().open();
      fail("It was possible to set a the open flag twice.");
    } catch (ProcessEngineException e) { }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testQueryByResolved() {
    startProcessInstance(PROCESS_DEFINITION_KEY);

    String jobId = managementService.createJobQuery().singleResult().getId();
    managementService.setJobRetries(jobId, 1);

    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery()
        .resolved();

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }

  public void testQueryByInvalidResolved() {
    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery();

    try {
      query.resolved().resolved();
      fail("It was possible to set a the resolved flag twice.");
    } catch (ProcessEngineException e) { }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testQueryByDeleted() {
    startProcessInstance(PROCESS_DEFINITION_KEY);

    String processInstanceId = runtimeService.createProcessInstanceQuery().singleResult().getId();
    runtimeService.deleteProcessInstance(processInstanceId, null);

    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery()
        .deleted();

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }

  public void testQueryByInvalidDeleted() {
    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery();

    try {
      query.deleted().deleted();
      fail("It was possible to set a the deleted flag twice.");
    } catch (ProcessEngineException e) { }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testQueryPaging() {
    startProcessInstances(PROCESS_DEFINITION_KEY, 4);

    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery();

    assertEquals(4, query.listPage(0, 4).size());
    assertEquals(1, query.listPage(2, 1).size());
    assertEquals(2, query.listPage(1, 2).size());
    assertEquals(3, query.listPage(1, 4).size());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testQuerySorting() {
    startProcessInstances(PROCESS_DEFINITION_KEY, 4);

    assertEquals(4, historyService.createHistoricIncidentQuery().orderByIncidentId().asc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByCreateTime().asc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByEndTime().asc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByIncidentType().asc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByExecutionId().asc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByActivityId().asc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByProcessInstanceId().asc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByProcessDefinitionId().asc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByCauseIncidentId().asc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByRootCauseIncidentId().asc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByConfiguration().asc().list().size());

    assertEquals(4, historyService.createHistoricIncidentQuery().orderByIncidentId().desc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByCreateTime().desc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByEndTime().desc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByIncidentType().desc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByExecutionId().desc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByActivityId().desc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByProcessInstanceId().desc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByProcessDefinitionId().desc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByCauseIncidentId().desc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByRootCauseIncidentId().desc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByConfiguration().desc().list().size());
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
