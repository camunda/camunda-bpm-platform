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
package org.camunda.bpm.engine.test.api.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.incident.IncidentContext;
import org.camunda.bpm.engine.impl.incident.IncidentHandler;
import org.camunda.bpm.engine.impl.persistence.entity.IncidentEntity;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class CreateAndResolveIncidentTest {

  public static final BpmnModelInstance ASYNC_TASK_PROCESS = Bpmn.createExecutableProcess("process")
      .startEvent("start")
      .serviceTask("task")
        .camundaAsyncBefore()
        .camundaExpression("${true}")
      .endEvent("end")
      .done();

  public static final BpmnModelInstance EXTERNAL_TASK_PROCESS = Bpmn.createExecutableProcess("process")
      .startEvent("start")
      .serviceTask("task")
        .camundaExternalTask("topic")
      .endEvent("end")
      .done();

  private static final CustomIncidentHandler CUSTOM_HANDLER = new CustomIncidentHandler("custom");
  private static final CustomIncidentHandler JOB_HANDLER = new CustomIncidentHandler(Incident.FAILED_JOB_HANDLER_TYPE);
  private static final CustomIncidentHandler EXTERNAL_TASK_HANDLER = new CustomIncidentHandler(Incident.EXTERNAL_TASK_HANDLER_TYPE);

  private static final List<IncidentHandler> HANDLERS = new ArrayList<IncidentHandler>();
  static {
    HANDLERS.add(CUSTOM_HANDLER);
    HANDLERS.add(JOB_HANDLER);
    HANDLERS.add(EXTERNAL_TASK_HANDLER);
  }

  @ClassRule
  public static ProcessEngineBootstrapRule processEngineBootstrapRule = new ProcessEngineBootstrapRule(configuration ->
      configuration.setCustomIncidentHandlers(HANDLERS));
  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule(processEngineBootstrapRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  private RuntimeService runtimeService;
  private ManagementService managementService;
  private ExternalTaskService externalTaskService;

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    externalTaskService = engineRule.getExternalTaskService();
    managementService = engineRule.getManagementService();
  }

  @After
  public void resetHandlers() {
    HANDLERS.forEach(h -> ((CustomIncidentHandler) h).reset());
  }

  @Test
  public void createIncident() {
    // given
    testRule.deploy(ProcessModels.TWO_TASKS_PROCESS);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Process");

    // when
    Incident incident = runtimeService.createIncident("foo", processInstance.getId(), "aa", "bar");

    // then
    Incident incident2 = runtimeService.createIncidentQuery().executionId(processInstance.getId()).singleResult();
    assertEquals(incident2.getId(), incident.getId());
    assertEquals("foo", incident2.getIncidentType());
    assertEquals("aa", incident2.getConfiguration());
    assertEquals("bar", incident2.getIncidentMessage());
    assertEquals(processInstance.getId(), incident2.getExecutionId());
  }

  @Test
  public void createIncidentWithNullExecution() {

    try {
      runtimeService.createIncident("foo", null, "userTask1", "bar");
      fail("exception expected");
    } catch (BadUserRequestException e) {
      assertThat(e.getMessage()).contains("Execution id cannot be null");
    }
  }

  @Test
  public void createIncidentWithNullIncidentType() {
    try {
      runtimeService.createIncident(null, "processInstanceId", "foo", "bar");
      fail("Exception expected");
    } catch (BadUserRequestException e) {
      assertThat(e.getMessage()).contains("incidentType is null");
    }
  }

  @Test
  public void createIncidentWithNonExistingExecution() {

    try {
      runtimeService.createIncident("foo", "aaa", "bbb", "bar");
      fail("exception expected");
    } catch (BadUserRequestException e) {
      assertThat(e.getMessage()).contains("Cannot find an execution with executionId 'aaa'");
    }
  }

  @Test
  public void resolveIncident() {
    // given
    testRule.deploy(ProcessModels.TWO_TASKS_PROCESS);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Process");
    Incident incident = runtimeService.createIncident("foo", processInstance.getId(), "userTask1", "bar");

    // when
    runtimeService.resolveIncident(incident.getId());

    // then
    Incident incident2 = runtimeService.createIncidentQuery().executionId(processInstance.getId()).singleResult();
    assertNull(incident2);
  }

  @Test
  public void resolveUnexistingIncident() {
    try {
      runtimeService.resolveIncident("foo");
      fail("Exception expected");
    } catch (NotFoundException e) {
      assertThat(e.getMessage()).contains("Cannot find an incident with id 'foo'");
    }
  }

  @Test
  public void resolveNullIncident() {
    try {
      runtimeService.resolveIncident(null);
      fail("Exception expected");
    } catch (BadUserRequestException e) {
      assertThat(e.getMessage()).contains("incidentId is null");
    }
  }

  @Test
  public void resolveIncidentOfTypeFailedJob() {
    // given
    testRule.deploy("org/camunda/bpm/engine/test/api/mgmt/IncidentTest.testShouldCreateOneIncident.bpmn");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingProcess");

    // when
    List<Job> jobs = engineRule.getManagementService().createJobQuery().withRetriesLeft().list();

    for (Job job : jobs) {
      engineRule.getManagementService().setJobRetries(job.getId(), 1);
      try {
        engineRule.getManagementService().executeJob(job.getId());
      } catch (Exception e) {}
    }

    // then
    Incident incident = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).singleResult();
    try {
      runtimeService.resolveIncident(incident.getId());
      fail("Exception expected");
    } catch (BadUserRequestException e) {
      assertThat(e.getMessage()).contains("Cannot resolve an incident of type failedJob");
    }
  }

  @Test
  public void createIncidentWithIncidentHandler() {
    // given
    testRule.deploy(ProcessModels.TWO_TASKS_PROCESS);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Process");

    // when
    Incident incident = runtimeService.createIncident("custom", processInstance.getId(), "configuration");

    // then
    assertNotNull(incident);

    Incident incident2 = runtimeService.createIncidentQuery().singleResult();
    assertNotNull(incident2);
    assertEquals(incident, incident2);
    assertEquals("custom", incident.getIncidentType());
    assertEquals("configuration", incident.getConfiguration());

    assertThat(CUSTOM_HANDLER.getCreateEvents()).hasSize(1);
    assertThat(CUSTOM_HANDLER.getResolveEvents()).isEmpty();
    assertThat(CUSTOM_HANDLER.getDeleteEvents()).isEmpty();
  }

  @Test
  public void resolveIncidentWithIncidentHandler() {
    // given
    testRule.deploy(ProcessModels.TWO_TASKS_PROCESS);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Process");
    runtimeService.createIncident("custom", processInstance.getId(), "configuration");
    Incident incident = runtimeService.createIncidentQuery().singleResult();

    // when
    runtimeService.resolveIncident(incident.getId());

    // then
    incident = runtimeService.createIncidentQuery().singleResult();
    assertNull(incident);

    assertThat(CUSTOM_HANDLER.getCreateEvents()).hasSize(1);
    assertThat(CUSTOM_HANDLER.getResolveEvents()).hasSize(1);
    assertThat(CUSTOM_HANDLER.getDeleteEvents()).isEmpty();
  }

  @Test
  public void shouldCallDeleteForCustomHandlerOnProcessInstanceCancellation() {
    // given
    testRule.deploy(ProcessModels.TWO_TASKS_PROCESS);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Process");
    runtimeService.createIncident("custom", processInstance.getId(), "configuration");

    CUSTOM_HANDLER.reset();

    // when
    runtimeService.deleteProcessInstance(processInstance.getId(), null);

    // then
    assertThat(CUSTOM_HANDLER.getCreateEvents()).isEmpty();
    assertThat(CUSTOM_HANDLER.getResolveEvents()).isEmpty();
    assertThat(CUSTOM_HANDLER.getDeleteEvents()).hasSize(1);

    IncidentContext deleteContext = CUSTOM_HANDLER.getDeleteEvents().get(0);
    assertThat(deleteContext.getConfiguration()).isEqualTo("configuration");

    long numIncidents = runtimeService.createIncidentQuery().count();
    assertThat(numIncidents).isEqualTo(0);
  }

  @Test
  public void shouldCallDeleteForJobHandlerOnProcessInstanceCancellation() {

    // given
    testRule.deploy(ASYNC_TASK_PROCESS);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    Job job = managementService.createJobQuery().singleResult();
    managementService.setJobRetries(job.getId(), 0);

    JOB_HANDLER.reset();

    // when
    runtimeService.deleteProcessInstance(processInstance.getId(), null);

    // then
    assertThat(JOB_HANDLER.getCreateEvents()).isEmpty();
    assertThat(JOB_HANDLER.getResolveEvents()).isEmpty();
    assertThat(JOB_HANDLER.getDeleteEvents()).hasSize(1);

    IncidentContext deleteContext = JOB_HANDLER.getDeleteEvents().get(0);
    assertThat(deleteContext.getConfiguration()).isEqualTo(job.getId());

    long numIncidents = runtimeService.createIncidentQuery().count();
    assertThat(numIncidents).isEqualTo(0);
  }

  @Test
  public void shouldCallDeleteForExternalTasksHandlerOnProcessInstanceCancellation() {

    // given
    testRule.deploy(EXTERNAL_TASK_PROCESS);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(1, "foo").topic("topic", 1000L).execute();
    LockedExternalTask task = tasks.get(0);

    externalTaskService.setRetries(task.getId(), 0);

    EXTERNAL_TASK_HANDLER.reset();

    // when
    runtimeService.deleteProcessInstance(processInstance.getId(), null);

    // then
    assertThat(EXTERNAL_TASK_HANDLER.getCreateEvents()).isEmpty();
    assertThat(EXTERNAL_TASK_HANDLER.getResolveEvents()).isEmpty();
    assertThat(EXTERNAL_TASK_HANDLER.getDeleteEvents()).hasSize(1);

    IncidentContext deleteContext = EXTERNAL_TASK_HANDLER.getDeleteEvents().get(0);
    assertThat(deleteContext.getConfiguration()).isEqualTo(task.getId());

    long numIncidents = runtimeService.createIncidentQuery().count();
    assertThat(numIncidents).isEqualTo(0);
  }

  @Test
  public void shouldCallResolveForJobHandler() {
    // given
    testRule.deploy(ASYNC_TASK_PROCESS);
    runtimeService.startProcessInstanceByKey("process");
    Job job = managementService.createJobQuery().singleResult();
    managementService.setJobRetries(job.getId(), 0);

    JOB_HANDLER.reset();

    // when
    managementService.setJobRetries(job.getId(), 1);

    // then
    assertThat(JOB_HANDLER.getCreateEvents()).isEmpty();
    assertThat(JOB_HANDLER.getResolveEvents()).hasSize(1);
    assertThat(JOB_HANDLER.getDeleteEvents()).isEmpty();

    IncidentContext deleteContext = JOB_HANDLER.getResolveEvents().get(0);
    assertThat(deleteContext.getConfiguration()).isEqualTo(job.getId());

    long numIncidents = runtimeService.createIncidentQuery().count();
    assertThat(numIncidents).isEqualTo(0);
  }

  @Test
  public void shouldCallResolveForExternalTaskHandler() {
    // given
    testRule.deploy(EXTERNAL_TASK_PROCESS);
    runtimeService.startProcessInstanceByKey("process");
    List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(1, "foo").topic("topic", 1000L).execute();
    LockedExternalTask task = tasks.get(0);

    externalTaskService.setRetries(task.getId(), 0);

    EXTERNAL_TASK_HANDLER.reset();

    // when
    externalTaskService.setRetries(task.getId(), 1);

    // then
    assertThat(EXTERNAL_TASK_HANDLER.getCreateEvents()).isEmpty();
    assertThat(EXTERNAL_TASK_HANDLER.getResolveEvents()).hasSize(1);
    assertThat(EXTERNAL_TASK_HANDLER.getDeleteEvents()).isEmpty();

    IncidentContext resolveContext = EXTERNAL_TASK_HANDLER.getResolveEvents().get(0);
    assertThat(resolveContext.getConfiguration()).isEqualTo(task.getId());

    long numIncidents = runtimeService.createIncidentQuery().count();
    assertThat(numIncidents).isEqualTo(0);
  }

  public static class CustomIncidentHandler implements IncidentHandler {

    private String incidentType;

    private List<IncidentContext> createEvents = new ArrayList<>();
    private List<IncidentContext> resolveEvents = new ArrayList<>();
    private List<IncidentContext> deleteEvents = new ArrayList<>();

    public CustomIncidentHandler(String type) {
      this.incidentType = type;
    }

    @Override
    public String getIncidentHandlerType() {
      return incidentType;
    }

    @Override
    public Incident handleIncident(IncidentContext context, String message) {
      createEvents.add(context);
      return IncidentEntity.createAndInsertIncident(incidentType, context, message);
    }

    @Override
    public void resolveIncident(IncidentContext context) {
      resolveEvents.add(context);
      deleteIncidentEntity(context);
    }

    @Override
    public void deleteIncident(IncidentContext context) {
      deleteEvents.add(context);
      deleteIncidentEntity(context);
    }

    private void deleteIncidentEntity(IncidentContext context) {
      List<Incident> incidents = Context.getCommandContext().getIncidentManager()
          .findIncidentByConfigurationAndIncidentType(context.getConfiguration(), incidentType);

      incidents.forEach(i -> ((IncidentEntity) i).delete());
    }

    public List<IncidentContext> getCreateEvents() {
      return createEvents;
    }

    public List<IncidentContext> getResolveEvents() {
      return resolveEvents;
    }

    public List<IncidentContext> getDeleteEvents() {
      return deleteEvents;
    }

    public void reset() {
      createEvents.clear();
      resolveEvents.clear();
      deleteEvents.clear();
    }

  }
}
