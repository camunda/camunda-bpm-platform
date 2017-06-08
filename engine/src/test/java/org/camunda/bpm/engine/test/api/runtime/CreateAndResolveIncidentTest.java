package org.camunda.bpm.engine.test.api.runtime;

import static org.junit.Assert.*;

import org.apache.tools.ant.filters.TokenFilter.ContainsString;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.impl.RuntimeServiceImpl;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import static org.hamcrest.CoreMatchers.containsString;

public class CreateAndResolveIncidentTest {

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected RuntimeService runtimeService;
  protected HistoryService historyService;

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    historyService = engineRule.getHistoryService();
  }

  @Test
  public void createIncident() {
    // given
    testRule.deploy(ProcessModels.TWO_TASKS_PROCESS);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Process");

    // when
    Incident incident = runtimeService.createIncident("foo", processInstance.getId(), "userTask1", "bar");

    // then
    Incident incident2 = runtimeService.createIncidentQuery().executionId(processInstance.getId()).singleResult();
    assertEquals(incident2.getId(), incident.getId());
    assertEquals("userTask1", incident2.getActivityId());
    assertEquals("foo", incident2.getIncidentType());
    assertEquals("bar", incident2.getConfiguration());
    assertEquals(processInstance.getId(), incident2.getExecutionId());
  }

  @Test
  public void createIncidentWithNullExecution() {

    try {
      runtimeService.createIncident("foo", null, "userTask1", "bar");
      fail("exception expected");
    } catch (BadUserRequestException e) {
      assertThat(e.getMessage(), containsString("Execution id cannot be null"));
    }
  }

  @Test
  public void createIncidentWithNullIncidentType() {
    try {
      runtimeService.createIncident(null, "processInstanceId", "foo", "bar");
      fail("Exception expected");
    } catch (BadUserRequestException e) {
      assertThat(e.getMessage(), containsString("incidentType is null"));
    }
  }

  @Test
  public void createIncidentWithNonExistingExecution() {

    try {
      runtimeService.createIncident("foo", "aaa", "userTask1", "bar");
      fail("exception expected");
    } catch (BadUserRequestException e) {
      assertThat(e.getMessage(), containsString("Cannot find an execution with executionId 'aaa' and activityId 'userTask1'"));
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
    } catch (BadUserRequestException e) {
      assertThat(e.getMessage(), containsString("Cannot find an incident with id 'foo'"));
    }
  }
}
