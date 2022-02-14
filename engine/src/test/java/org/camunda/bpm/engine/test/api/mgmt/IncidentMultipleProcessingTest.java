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
 package org.camunda.bpm.engine.test.api.mgmt;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.incident.CompositeIncidentHandler;
import org.camunda.bpm.engine.impl.incident.IncidentContext;
import org.camunda.bpm.engine.impl.incident.IncidentHandler;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class IncidentMultipleProcessingTest {

  private static final StubIncidentHandler JOB_HANDLER = new StubIncidentHandler(Incident.FAILED_JOB_HANDLER_TYPE);

  @ClassRule
  public static ProcessEngineBootstrapRule processEngineBootstrapRule = new ProcessEngineBootstrapRule(
      configuration -> {
        configuration.setCompositeIncidentHandlersEnabled(true);
        configuration.setCustomIncidentHandlers(Collections.singletonList(JOB_HANDLER));
      });
  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule(processEngineBootstrapRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  private RuntimeService runtimeService;
  private ManagementService managementService;

  @Before
  public void init() {
    JOB_HANDLER.reset();

    runtimeService = engineRule.getRuntimeService();
    managementService = engineRule.getManagementService();
  }

  @Test
  public void jobHandlerShouldBeCompositeHandler() {
    IncidentHandler incidentHandler = engineRule.getProcessEngineConfiguration().getIncidentHandler(Incident.FAILED_JOB_HANDLER_TYPE);

    assertThat(incidentHandler).isNotNull();
    assertThat(incidentHandler).isInstanceOf(CompositeIncidentHandler.class);
  }

  @Test
  public void externalTaskHandlerShouldBeCompositeHandler() {
    IncidentHandler incidentHandler = engineRule.getProcessEngineConfiguration().getIncidentHandler(Incident.EXTERNAL_TASK_HANDLER_TYPE);

    assertThat(incidentHandler).isNotNull();
    assertThat(incidentHandler).isInstanceOf(CompositeIncidentHandler.class);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/mgmt/IncidentTest.testShouldCreateOneIncident.bpmn" })
  @Test
  public void shouldCreateOneIncident() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingProcess");

    testRule.executeAvailableJobs();

    List<Incident> incidents = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).list();

    assertThat(incidents).hasSize(1);

    assertThat(JOB_HANDLER.getCreateEvents()).hasSize(1);
    assertThat(JOB_HANDLER.getResolveEvents()).isEmpty();
    assertThat(JOB_HANDLER.getDeleteEvents()).isEmpty();
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/mgmt/IncidentTest.testShouldCreateOneIncident.bpmn" })
  @Test
  public void shouldResolveIncidentAfterJobRetriesRefresh() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingProcess");

    testRule.executeAvailableJobs();

    Job job = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(job).isNotNull();

    Incident incident = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(incident).isNotNull();

    managementService.setJobRetries(job.getId(), 1);

    incident = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(incident).isNull();

    assertThat(JOB_HANDLER.getCreateEvents()).hasSize(1);
    // incidents resolved when job retries update
    assertThat(JOB_HANDLER.getResolveEvents()).hasSize(1);
    assertThat(JOB_HANDLER.getDeleteEvents()).isEmpty();
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/mgmt/IncidentTest.testShouldCreateOneIncident.bpmn" })
  @Test
  public void shouldDeleteIncidentAfterJobHasBeenDeleted() {
    // start failing process
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingProcess");

    testRule.executeAvailableJobs();

    // get the job
    Job job = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(job).isNotNull();

    // there exists one incident to failed
    Incident incident = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(incident).isNotNull();

    // delete the job
    managementService.deleteJob(job.getId());

    // the incident has been deleted too.
    incident = runtimeService.createIncidentQuery().incidentId(incident.getId()).singleResult();
    assertThat(incident).isNull();

    assertThat(JOB_HANDLER.getCreateEvents()).hasSize(1);
    assertThat(JOB_HANDLER.getResolveEvents()).isEmpty();
    assertThat(JOB_HANDLER.getDeleteEvents()).hasSize(1);
  }

  public static class StubIncidentHandler implements IncidentHandler {

    private String incidentType;

    private List<IncidentContext> createEvents = new ArrayList<>();
    private List<IncidentContext> resolveEvents = new ArrayList<>();
    private List<IncidentContext> deleteEvents = new ArrayList<>();

    public StubIncidentHandler(String type) {
      this.incidentType = type;
    }

    @Override
    public String getIncidentHandlerType() {
      return incidentType;
    }

    @Override
    public Incident handleIncident(IncidentContext context, String message) {
      createEvents.add(context);
      return null;
    }

    @Override
    public void resolveIncident(IncidentContext context) {
      resolveEvents.add(context);
    }

    @Override
    public void deleteIncident(IncidentContext context) {
      deleteEvents.add(context);
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
