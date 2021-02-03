/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.api.authorization;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.READ_INSTANCE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE_INSTANCE;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_INSTANCE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.TimerSuspendProcessDefinitionHandler;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricIncidentEntity;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.IncidentQuery;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class IncidentAuthorizationTest extends AuthorizationTest {

  protected static final String TIMER_START_PROCESS_KEY = "timerStartProcess";
  protected static final String ONE_INCIDENT_PROCESS_KEY = "process";
  protected static final String ANOTHER_ONE_INCIDENT_PROCESS_KEY = "anotherOneIncidentProcess";

  @Before
  public void setUp() throws Exception {
    testRule.deploy(
        "org/camunda/bpm/engine/test/api/authorization/timerStartEventProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/authorization/oneIncidentProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/authorization/anotherOneIncidentProcess.bpmn20.xml");
    super.setUp();
  }

  @Test
  public void testQueryForStandaloneIncidents() {
    // given
    String jobId = createStandaloneIncident();

    // when
    IncidentQuery query = runtimeService.createIncidentQuery();

    // then
    verifyQueryResults(query, 1);

    // cleanup
    cleanupStandalonIncident(jobId);
  }

  @Test
  public void testStartTimerJobIncidentQueryWithoutAuthorization() {
    // given
    disableAuthorization();
    String jobId = managementService.createJobQuery().singleResult().getId();
    managementService.setJobRetries(jobId, 0);
    enableAuthorization();

    // when
    IncidentQuery query = runtimeService.createIncidentQuery();

    // then
    verifyQueryResults(query, 0);
  }

  @Test
  public void testStartTimerJobIncidentQueryWithReadPermissionOnAnyProcessInstance() {
    // given
    disableAuthorization();
    String jobId = managementService.createJobQuery().singleResult().getId();
    managementService.setJobRetries(jobId, 0);
    enableAuthorization();

    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    IncidentQuery query = runtimeService.createIncidentQuery();

    // then
    verifyQueryResults(query, 1);
  }

  @Test
  public void testStartTimerJobIncidentQueryWithReadInstancePermissionOnProcessDefinition() {
    // given
    disableAuthorization();
    String jobId = managementService.createJobQuery().singleResult().getId();
    managementService.setJobRetries(jobId, 0);
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, TIMER_START_PROCESS_KEY, userId, READ_INSTANCE);

    // when
    IncidentQuery query = runtimeService.createIncidentQuery();

    // then
    verifyQueryResults(query, 1);
  }

  @Test
  public void testStartTimerJobIncidentQueryWithReadInstancePermissionOnAnyProcessDefinition() {
    // given
    disableAuthorization();
    String jobId = managementService.createJobQuery().singleResult().getId();
    managementService.setJobRetries(jobId, 0);
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);

    // when
    IncidentQuery query = runtimeService.createIncidentQuery();

    // then
    verifyQueryResults(query, 1);
  }

  @Test
  public void testSimpleQueryWithoutAuthorization() {
    // given
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    // when
    IncidentQuery query = runtimeService.createIncidentQuery();

    // then
    verifyQueryResults(query, 0);
  }

  @Test
  public void testSimpleQueryWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    // when
    IncidentQuery query = runtimeService.createIncidentQuery();

    // then
    verifyQueryResults(query, 1);

    Incident incident = query.singleResult();
    assertNotNull(incident);
    assertEquals(processInstanceId, incident.getProcessInstanceId());
  }

  @Test
  public void testSimpleQueryWithReadPermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    IncidentQuery query = runtimeService.createIncidentQuery();

    // then
    verifyQueryResults(query, 1);

    Incident incident = query.singleResult();
    assertNotNull(incident);
    assertEquals(processInstanceId, incident.getProcessInstanceId());
  }

  @Test
  public void testSimpleQueryWithMultiple() {
    // given
    String processInstanceId = startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    IncidentQuery query = runtimeService.createIncidentQuery();

    // then
    verifyQueryResults(query, 1);

    Incident incident = query.singleResult();
    assertNotNull(incident);
    assertEquals(processInstanceId, incident.getProcessInstanceId());
  }

  @Test
  public void testSimpleQueryWithReadInstancesPermissionOnOneTaskProcess() {
    // given
    String processInstanceId = startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ONE_INCIDENT_PROCESS_KEY, userId, READ_INSTANCE);

    // when
    IncidentQuery query = runtimeService.createIncidentQuery();

    // then
    verifyQueryResults(query, 1);

    Incident incident = query.singleResult();
    assertNotNull(incident);
    assertEquals(processInstanceId, incident.getProcessInstanceId());
  }

  @Test
  public void testSimpleQueryWithReadInstancesPermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);

    // when
    IncidentQuery query = runtimeService.createIncidentQuery();

    // then
    verifyQueryResults(query, 1);

    Incident incident = query.singleResult();
    assertNotNull(incident);
    assertEquals(processInstanceId, incident.getProcessInstanceId());
  }

  @Test
  public void testQueryWithoutAuthorization() {
    // given
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);

    // when
    IncidentQuery query = runtimeService.createIncidentQuery();

    // then
    verifyQueryResults(query, 0);
  }

  @Test
  public void testQueryWithReadPermissionOnProcessInstance() {
    // given
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    String processInstanceId = startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY).getId();

    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);

    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    // when
    IncidentQuery query = runtimeService.createIncidentQuery();

    // then
    verifyQueryResults(query, 1);

    Incident incident = query.singleResult();
    assertNotNull(incident);
    assertEquals(processInstanceId, incident.getProcessInstanceId());
  }

  @Test
  public void testQueryWithReadPermissionOnAnyProcessInstance() {
    // given
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);

    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    IncidentQuery query = runtimeService.createIncidentQuery();

    // then
    verifyQueryResults(query, 7);
  }

  @Test
  public void testQueryWithReadInstancesPermissionOnOneTaskProcess() {
    // given
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ONE_INCIDENT_PROCESS_KEY, userId, READ_INSTANCE);

    // when
    IncidentQuery query = runtimeService.createIncidentQuery();

    // then
    verifyQueryResults(query, 3);
  }

  @Test
  public void testQueryWithReadInstancesPermissionOnAnyProcessDefinition() {
    // given
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);

    // when
    IncidentQuery query = runtimeService.createIncidentQuery();

    // then
    verifyQueryResults(query, 7);
  }


  @Test
  public void shouldDenySetAnnotationWithoutAuthorization() {
    // given
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    disableAuthorization();
    Incident incident = runtimeService.createIncidentQuery().singleResult();
    enableAuthorization();

    // when & then
    assertThatThrownBy(() -> runtimeService.setAnnotationForIncidentById(incident.getId(), "my annotation"))
      .isInstanceOf(AuthorizationException.class)
      .hasMessageMatching(getMissingPermissionMessageRegex(UPDATE, PROCESS_INSTANCE))
      .hasMessageMatching(getMissingPermissionMessageRegex(UPDATE_INSTANCE, PROCESS_DEFINITION));
  }

  @Test
  public void shouldAllowSetAnnotationWithUpdatePermissionOnAnyInstance() {
    // given
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    disableAuthorization();
    Incident incident = runtimeService.createIncidentQuery().singleResult();
    enableAuthorization();

    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    // when
    runtimeService.setAnnotationForIncidentById(incident.getId(), "my annotation");

    // then no error is thrown
  }

  @Test
  public void shouldAllowSetAnnotationWithUpdatePermissionOnInstance() {
    // given
    ProcessInstance instance = startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    disableAuthorization();
    Incident incident = runtimeService.createIncidentQuery().singleResult();
    enableAuthorization();

    createGrantAuthorization(PROCESS_INSTANCE, instance.getId(), userId, UPDATE);

    // when
    runtimeService.setAnnotationForIncidentById(incident.getId(), "my annotation");

    // then no error is thrown
  }

  @Test
  public void shouldAllowSetAnnotationWithUpdateInstancePermissionOnAnyDefinition() {
    // given
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    disableAuthorization();
    Incident incident = runtimeService.createIncidentQuery().singleResult();
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.setAnnotationForIncidentById(incident.getId(), "my annotation");

    // then no error is thrown
  }

  @Test
  public void shouldAllowSetAnnotationWithUpdateInstancePermissionOnOneTaskDefinition() {
    // given
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    disableAuthorization();
    Incident incident = runtimeService.createIncidentQuery().singleResult();
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, ONE_INCIDENT_PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.setAnnotationForIncidentById(incident.getId(), "my annotation");

    // then no error is thrown
  }

  @Test
  public void shouldDenyClearAnnotationWithoutAuthorization() {
    // given
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    disableAuthorization();
    Incident incident = runtimeService.createIncidentQuery().singleResult();
    enableAuthorization();

    // when & then
    assertThatThrownBy(() -> runtimeService.clearAnnotationForIncidentById(incident.getId()))
      .isInstanceOf(AuthorizationException.class)
      .hasMessageMatching(getMissingPermissionMessageRegex(UPDATE, PROCESS_INSTANCE))
      .hasMessageMatching(getMissingPermissionMessageRegex(UPDATE_INSTANCE, PROCESS_DEFINITION));
  }

  @Test
  public void shouldAllowClearAnnotationWithUpdatePermissionOnAnyInstance() {
    // given
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    disableAuthorization();
    Incident incident = runtimeService.createIncidentQuery().singleResult();
    enableAuthorization();

    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    // when
    runtimeService.clearAnnotationForIncidentById(incident.getId());

    // then no error is thrown
  }

  @Test
  public void shouldAllowClearAnnotationWithUpdatePermissionOnInstance() {
    // given
    ProcessInstance instance = startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    disableAuthorization();
    Incident incident = runtimeService.createIncidentQuery().singleResult();
    enableAuthorization();

    createGrantAuthorization(PROCESS_INSTANCE, instance.getId(), userId, UPDATE);

    // when
    runtimeService.clearAnnotationForIncidentById(incident.getId());

    // then no error is thrown
  }

  @Test
  public void shouldAllowClearAnnotationWithUpdateInstancePermissionOnAnyDefinition() {
    // given
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    disableAuthorization();
    Incident incident = runtimeService.createIncidentQuery().singleResult();
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.clearAnnotationForIncidentById(incident.getId());

    // then no error is thrown
  }

  @Test
  public void shouldAllowClearAnnotationWithUpdateInstancePermissionOnOneTaskDefinition() {
    // given
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    disableAuthorization();
    Incident incident = runtimeService.createIncidentQuery().singleResult();
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, ONE_INCIDENT_PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.clearAnnotationForIncidentById(incident.getId());

    // then no error is thrown
  }

  @Test
  public void shouldAllowSetAnnotationOnStandaloneIncidentWithoutAuthorization() {
    // given
    String jobId = createStandaloneIncident();
    disableAuthorization();
    Incident incident = runtimeService.createIncidentQuery().singleResult();
    enableAuthorization();

    // when
    runtimeService.setAnnotationForIncidentById(incident.getId(), "my annotation");

    // then no error is thrown

    // cleanup
    cleanupStandalonIncident(jobId);
  }

  @Test
  public void shouldAllowClearAnnotationOnStandaloneIncidentWithoutAuthorization() {
    // given
    String jobId = createStandaloneIncident();
    disableAuthorization();
    Incident incident = runtimeService.createIncidentQuery().singleResult();
    enableAuthorization();

    // when
    runtimeService.clearAnnotationForIncidentById(incident.getId());

    // then no error is thrown

    // cleanup
    cleanupStandalonIncident(jobId);
  }

  protected String createStandaloneIncident() {
    disableAuthorization();
    repositoryService.suspendProcessDefinitionByKey(ONE_INCIDENT_PROCESS_KEY, true, new Date());
    String jobId = null;
    List<Job> jobs = managementService.createJobQuery().list();
    for (Job job : jobs) {
      if (job.getProcessDefinitionKey() == null) {
        jobId = job.getId();
        break;
      }
    }
    managementService.setJobRetries(jobId, 0);
    enableAuthorization();
    return jobId;
  }

  protected void verifyQueryResults(IncidentQuery query, int countExpected) {
    verifyQueryResults((AbstractQuery<?, ?>) query, countExpected);
  }

  protected void cleanupStandalonIncident(String jobId) {
    disableAuthorization();
    managementService.deleteJob(jobId);
    enableAuthorization();
    clearDatabase();
  }

  protected void clearDatabase() {
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        HistoryLevel historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
        if (historyLevel.equals(HistoryLevel.HISTORY_LEVEL_FULL)) {
          commandContext.getHistoricJobLogManager().deleteHistoricJobLogsByHandlerType(TimerSuspendProcessDefinitionHandler.TYPE);
          List<HistoricIncident> incidents = Context.getProcessEngineConfiguration().getHistoryService().createHistoricIncidentQuery().list();
          for (HistoricIncident incident : incidents) {
            commandContext.getHistoricIncidentManager().delete((HistoricIncidentEntity) incident);
          }
        }

        return null;
      }
    });
  }

}
