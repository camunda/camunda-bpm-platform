package org.camunda.bpm.engine.test.api.mgmt;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.incident.CompositeIncidentHandler;
import org.camunda.bpm.engine.impl.incident.IncidentContext;
import org.camunda.bpm.engine.impl.incident.IncidentHandler;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class IncidentMultipleProcessingTest {

  private static final StubIncidentHandler JOB_HANDLER = new StubIncidentHandler(Incident.FAILED_JOB_HANDLER_TYPE);

  private static final List<IncidentHandler> HANDLERS = new ArrayList<>();

  static {
    HANDLERS.add(JOB_HANDLER);
  }

  @ClassRule
  public static ProcessEngineBootstrapRule processEngineBootstrapRule = new ProcessEngineBootstrapRule(
      configuration -> {
        configuration.setCompositeIncidentHandlersEnabled(true);
        configuration.setCustomIncidentHandlers(HANDLERS);
      });
  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule(processEngineBootstrapRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  private RuntimeService runtimeService;
  private ManagementService managementService;

  @Before
  public void init() {
    HANDLERS.forEach(h -> ((StubIncidentHandler) h).reset());

    runtimeService = engineRule.getRuntimeService();
    managementService = engineRule.getManagementService();
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/mgmt/IncidentTest.testShouldCreateOneIncident.bpmn" })
  @Test
  public void shouldCreateOneIncident() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingProcess");

    testRule.executeAvailableJobs();

    List<Incident> incidents = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).list();

    assertFalse(incidents.isEmpty());
    assertEquals(1, incidents.size());

    assertThat(JOB_HANDLER.getCreateEvents()).hasSize(1);
    assertThat(JOB_HANDLER.getResolveEvents()).isEmpty();
    assertThat(JOB_HANDLER.getDeleteEvents()).isEmpty();

    IncidentHandler incidentHandler = engineRule.getProcessEngineConfiguration().getIncidentHandler(Incident.FAILED_JOB_HANDLER_TYPE);
    assertNotNull(incidentHandler);
    assertTrue(incidentHandler instanceof CompositeIncidentHandler);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/mgmt/IncidentTest.testShouldCreateOneIncident.bpmn" })
  @Test
  public void shouldResolveIncidentAfterJobWasSuccessfully() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingProcess");

    testRule.executeAvailableJobs();

    // there exists one incident to failed
    Incident incident = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(incident);

    engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired().execute((Command<Void>) commandContext -> {
      ProcessInstance innerProcessInstance = runtimeService.createProcessInstanceQuery()
          .processInstanceId(processInstance.getId())
          .singleResult();
      assertTrue(innerProcessInstance instanceof ExecutionEntity);
      ExecutionEntity exec = (ExecutionEntity) innerProcessInstance;

      exec.resolveIncident(incident.getId());
      return null;
    });

    assertThat(JOB_HANDLER.getCreateEvents()).hasSize(1);
    assertThat(JOB_HANDLER.getResolveEvents()).hasSize(1);
    assertThat(JOB_HANDLER.getDeleteEvents()).isEmpty();

    IncidentHandler incidentHandler = engineRule.getProcessEngineConfiguration().getIncidentHandler(Incident.FAILED_JOB_HANDLER_TYPE);
    assertNotNull(incidentHandler);
    assertTrue(incidentHandler instanceof CompositeIncidentHandler);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/mgmt/IncidentTest.testShouldCreateOneIncident.bpmn" })
  @Test
  public void shouldDeleteIncidentAfterJobHasBeenDeleted() {
    // start failing process
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingProcess");

    testRule.executeAvailableJobs();

    // get the job
    Job job = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(job);

    // there exists one incident to failed
    Incident incident = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(incident);

    // delete the job
    managementService.deleteJob(job.getId());

    // the incident has been deleted too.
    incident = runtimeService.createIncidentQuery().incidentId(incident.getId()).singleResult();
    assertNull(incident);

    assertThat(JOB_HANDLER.getCreateEvents()).hasSize(1);
    assertThat(JOB_HANDLER.getResolveEvents()).isEmpty();
    assertThat(JOB_HANDLER.getDeleteEvents()).hasSize(1);

    IncidentHandler incidentHandler = engineRule.getProcessEngineConfiguration().getIncidentHandler(Incident.FAILED_JOB_HANDLER_TYPE);
    assertNotNull(incidentHandler);
    assertTrue(incidentHandler instanceof CompositeIncidentHandler);
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
