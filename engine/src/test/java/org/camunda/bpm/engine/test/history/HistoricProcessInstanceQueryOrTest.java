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
package org.camunda.bpm.engine.test.history;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
public class HistoricProcessInstanceQueryOrTest {

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  protected HistoryService historyService;
  protected RuntimeService runtimeService;
  protected RepositoryService repositoryService;
  protected ManagementService managementService;

  protected List<String> deploymentIds = new ArrayList<>();

  @Before
  public void init() {
    historyService = processEngineRule.getHistoryService();
    runtimeService = processEngineRule.getRuntimeService();
    repositoryService = processEngineRule.getRepositoryService();
    managementService = processEngineRule.getManagementService();
  }

  @After
  public void deleteDeployments() {
    for (String deploymentId : deploymentIds) {
      repositoryService.deleteDeployment(deploymentId, true);
    }
  }

  @Test
  public void shouldThrowExceptionByMissingStartOr() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set endOr() before or()");

    historyService.createHistoricProcessInstanceQuery()
      .or()
      .endOr()
      .endOr();
  }

  @Test
  public void shouldThrowExceptionByNesting() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set or() within 'or' query");

    historyService.createHistoricProcessInstanceQuery()
      .or()
        .or()
        .endOr()
      .endOr()
      .or()
      .endOr();
  }

  @Test
  public void shouldThrowExceptionOnOrderByProcessInstanceId() {
    // given

    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set orderByProcessInstanceId() within 'or' query");

    // when
    historyService.createHistoricProcessInstanceQuery()
        .or()
          .orderByProcessInstanceId()
        .endOr();
  }

  @Test
  public void shouldThrowExceptionOnOrderByProcessDefinitionId() {
    // given

    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set orderByProcessDefinitionId() within 'or' query");

    // when
    historyService.createHistoricProcessInstanceQuery()
        .or()
          .orderByProcessDefinitionId()
        .endOr();
  }

  @Test
  public void shouldThrowExceptionOnOrderByProcessDefinitionKey() {
    // given

    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set orderByProcessDefinitionKey() within 'or' query");

    // when
    historyService.createHistoricProcessInstanceQuery()
        .or()
          .orderByProcessDefinitionKey()
        .endOr();
  }

  @Test
  public void shouldThrowExceptionOnOrderByTenantId() {
    // given

    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set orderByTenantId() within 'or' query");

    // when
    historyService.createHistoricProcessInstanceQuery()
        .or()
          .orderByTenantId()
        .endOr();
  }

  @Test
  public void shouldThrowExceptionOnOrderByProcessInstanceDuration() {
    // given

    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set orderByProcessInstanceDuration() within 'or' query");

    // when
    historyService.createHistoricProcessInstanceQuery()
        .or()
          .orderByProcessInstanceDuration()
        .endOr();
  }

  @Test
  public void shouldThrowExceptionOnOrderByProcessInstanceStartTime() {
    // given

    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set orderByProcessInstanceStartTime() within 'or' query");

    // when
    historyService.createHistoricProcessInstanceQuery()
        .or()
          .orderByProcessInstanceStartTime()
        .endOr();
  }

  @Test
  public void shouldThrowExceptionOnOrderByProcessInstanceEndTime() {
    // given

    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set orderByProcessInstanceEndTime() within 'or' query");

    // then
    historyService.createHistoricProcessInstanceQuery()
        .or()
          .orderByProcessInstanceEndTime()
        .endOr();
  }

  @Test
  public void shouldThrowExceptionOnOrderByProcessInstanceBusinessKey() {
    // given

    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set orderByProcessInstanceBusinessKey() within 'or' query");

    // then
    historyService.createHistoricProcessInstanceQuery()
        .or()
          .orderByProcessInstanceBusinessKey()
        .endOr();
  }

  @Test
  public void shouldThrowExceptionOnOrderByProcessDefinitionName() {
    // given

    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set orderByProcessDefinitionName() within 'or' query");

    // then
    historyService.createHistoricProcessInstanceQuery()
        .or()
          .orderByProcessDefinitionName()
        .endOr();
  }

  @Test
  public void shouldThrowExceptionOnOrderByProcessDefinitionVersion() {
    // given

    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set orderByProcessDefinitionVersion() within 'or' query");

    // then
    historyService.createHistoricProcessInstanceQuery()
        .or()
          .orderByProcessDefinitionVersion()
        .endOr();
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void shouldReturnHistoricProcInstWithEmptyOrQuery() {
    // given
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");

    // when
    List<HistoricProcessInstance> processInstances = historyService.createHistoricProcessInstanceQuery()
      .or()
      .endOr()
      .list();

    // then
    assertEquals(2, processInstances.size());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void shouldReturnHistoricProcInstWithVarValue1OrVarValue2() {
    // given
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("stringVar", "abcdef");
    runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("stringVar", "ghijkl");
    runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    // when
    List<HistoricProcessInstance> processInstances = historyService.createHistoricProcessInstanceQuery()
      .or()
        .variableValueEquals("stringVar", "abcdef")
        .variableValueEquals("stringVar", "ghijkl")
      .endOr()
      .list();

    // then
    assertEquals(2, processInstances.size());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void shouldReturnHistoricProcInstWithMultipleOrCriteria() {
    // given
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("stringVar", "abcdef");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    runtimeService.setVariable(processInstance2.getProcessInstanceId(), "aVarName", "varValue");

    vars = new HashMap<>();
    vars.put("stringVar2", "aaabbbaaa");
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    runtimeService.setVariable(processInstance3.getProcessInstanceId(), "bVarName", "bTestb");

    vars = new HashMap<>();
    vars.put("stringVar2", "cccbbbccc");
    ProcessInstance processInstance4 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    runtimeService.setVariable(processInstance4.getProcessInstanceId(), "bVarName", "aTesta");

    // when
    List<HistoricProcessInstance> processInstances = historyService.createHistoricProcessInstanceQuery()
      .or()
        .variableValueEquals("stringVar", "abcdef")
        .variableValueLike("stringVar2", "%bbb%")
        .processInstanceId(processInstance1.getId())
      .endOr()
      .list();

    // then
    assertEquals(4, processInstances.size());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void shouldReturnHistoricProcInstFilteredByMultipleOrAndCriteria() {
    // given
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("stringVar", "abcdef");
    vars.put("longVar", 12345L);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<>();
    vars.put("stringVar", "ghijkl");
    vars.put("longVar", 56789L);
    runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<>();
    vars.put("stringVar", "abcdef");
    vars.put("longVar", 56789L);
    runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<>();
    vars.put("stringVar", "ghijkl");
    vars.put("longVar", 12345L);
    runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    // when
    List<HistoricProcessInstance> processInstances = historyService.createHistoricProcessInstanceQuery()
      .or()
        .variableValueEquals("longVar", 56789L)
        .processInstanceId(processInstance1.getId())
      .endOr()
      .variableValueEquals("stringVar", "abcdef")
      .list();

    // then
    assertEquals(2, processInstances.size());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void shouldReturnHistoricProcInstFilteredByMultipleOrQueries() {
    // given
    Map<String, Object> vars = new HashMap<>();
    vars.put("stringVar", "abcdef");
    vars.put("longVar", 12345L);
    vars.put("boolVar", true);
    runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<>();
    vars.put("stringVar", "ghijkl");
    vars.put("longVar", 56789L);
    vars.put("boolVar", true);
    runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<>();
    vars.put("stringVar", "abcdef");
    vars.put("longVar", 56789L);
    vars.put("boolVar", true);
    runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<>();
    vars.put("stringVar", "ghijkl");
    vars.put("longVar", 12345L);
    vars.put("boolVar", true);
    runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<>();
    vars.put("stringVar", "ghijkl");
    vars.put("longVar", 56789L);
    vars.put("boolVar", false);
    runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<>();
    vars.put("stringVar", "abcdef");
    vars.put("longVar", 12345L);
    vars.put("boolVar", false);
    runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    // when
    List<HistoricProcessInstance> processInstances = historyService.createHistoricProcessInstanceQuery()
      .or()
        .variableValueEquals("stringVar", "abcdef")
        .variableValueEquals("longVar", 12345L)
      .endOr()
      .or()
        .variableValueEquals("boolVar", true)
        .variableValueEquals("longVar", 12345L)
      .endOr()
      .or()
        .variableValueEquals("stringVar", "ghijkl")
        .variableValueEquals("longVar", 56789L)
      .endOr()
      .or()
        .variableValueEquals("stringVar", "ghijkl")
        .variableValueEquals("boolVar", false)
        .variableValueEquals("longVar", 56789L)
      .endOr()
      .list();

    // then
    assertEquals(2, processInstances.size());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void shouldReturnHistoricProcInstWhereSameCriterionWasAppliedThreeTimesInOneQuery() {
    // given
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    List<HistoricProcessInstance> processInstances = historyService.createHistoricProcessInstanceQuery()
      .or()
        .processInstanceId(processInstance1.getId())
        .processInstanceId(processInstance2.getId())
        .processInstanceId(processInstance3.getId())
      .endOr()
      .list();

    // then
    assertEquals(1, processInstances.size());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void shouldReturnHistoricProcInstWithVariableValueEqualsOrVariableValueGreaterThan() {
    // given
    Map<String, Object> vars = new HashMap<>();
    vars.put("longVar", 12345L);
    runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<>();
    vars.put("longerVar", 56789L);
    runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<>();
    vars.put("longerVar", 56789L);
    runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    // when
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery()
      .or()
        .variableValueEquals("longVar", 12345L)
        .variableValueGreaterThan("longerVar", 20000L)
      .endOr();

    // then
    assertEquals(3, query.count());
  }

  @Test
  public void shouldReturnHistoricProcInstWithProcessDefinitionNameOrProcessDefinitionKey() {
    // given
    BpmnModelInstance aProcessDefinition = Bpmn.createExecutableProcess("aProcessDefinition")
      .name("process1")
      .startEvent()
        .userTask()
      .endEvent()
      .done();

    String deploymentId = repositoryService
      .createDeployment()
      .addModelInstance("foo.bpmn", aProcessDefinition)
      .deploy()
      .getId();

    deploymentIds.add(deploymentId);

    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("aProcessDefinition");

    BpmnModelInstance anotherProcessDefinition = Bpmn.createExecutableProcess("anotherProcessDefinition")
      .startEvent()
        .userTask()
      .endEvent()
      .done();

    deploymentId = repositoryService
       .createDeployment()
       .addModelInstance("foo.bpmn", anotherProcessDefinition)
       .deploy()
       .getId();

    deploymentIds.add(deploymentId);

    runtimeService.startProcessInstanceByKey("anotherProcessDefinition");

    // when
    List<HistoricProcessInstance> processInstances = historyService.createHistoricProcessInstanceQuery()
      .or()
        .processDefinitionId(processInstance1.getProcessDefinitionId())
        .processDefinitionKey("anotherProcessDefinition")
      .endOr()
      .list();

    // then
    assertEquals(2, processInstances.size());
  }

  @Test
  public void shouldReturnHistoricProcInstWithBusinessKeyOrBusinessKeyLike() {
    // given
    BpmnModelInstance aProcessDefinition = Bpmn.createExecutableProcess("aProcessDefinition")
      .startEvent()
        .userTask()
      .endEvent()
      .done();

    String deploymentId = repositoryService
      .createDeployment()
      .addModelInstance("foo.bpmn", aProcessDefinition)
      .deploy()
      .getId();

    deploymentIds.add(deploymentId);

    runtimeService.startProcessInstanceByKey("aProcessDefinition", "aBusinessKey");

    BpmnModelInstance anotherProcessDefinition = Bpmn.createExecutableProcess("anotherProcessDefinition")
      .startEvent()
        .userTask()
      .endEvent()
      .done();

     deploymentId = repositoryService
      .createDeployment()
      .addModelInstance("foo.bpmn", anotherProcessDefinition)
      .deploy()
      .getId();

     deploymentIds.add(deploymentId);

    runtimeService.startProcessInstanceByKey("anotherProcessDefinition", "anotherBusinessKey");

    // when
    List<HistoricProcessInstance> processInstances = historyService.createHistoricProcessInstanceQuery()
      .or()
        .processInstanceBusinessKey("aBusinessKey")
        .processInstanceBusinessKeyLike("anotherBusinessKey")
      .endOr()
      .list();

    // then
    assertEquals(2, processInstances.size());
  }

  @Test
  public void shouldReturnHistoricProcInstWithActivityIdInOrProcInstId() {
    // given
    BpmnModelInstance aProcessDefinition = Bpmn.createExecutableProcess("aProcessDefinition")
      .startEvent()
        .userTask()
      .endEvent()
      .done();

    String deploymentId = repositoryService
      .createDeployment()
      .addModelInstance("foo.bpmn", aProcessDefinition)
      .deploy()
      .getId();

    deploymentIds.add(deploymentId);

    ProcessInstance processInstance1 = runtimeService
      .startProcessInstanceByKey("aProcessDefinition");

    String activityInstanceId = runtimeService.getActivityInstance(processInstance1.getId())
      .getChildActivityInstances()[0].getActivityId();

    ProcessInstance processInstance2 = runtimeService
      .startProcessInstanceByKey("aProcessDefinition");

    // when
    List<HistoricProcessInstance> tasks = historyService.createHistoricProcessInstanceQuery()
      .or()
        .activeActivityIdIn(activityInstanceId)
        .processInstanceId(processInstance2.getId())
      .endOr()
      .list();

    // then
    assertEquals(2, tasks.size());
  }

  @Test
  public void shouldReturnHistoricProcInstByVariableAndActiveProcesses() {
    // given
    BpmnModelInstance aProcessDefinition = Bpmn.createExecutableProcess("oneTaskProcess")
        .startEvent()
          .userTask("testQuerySuspensionStateTask")
        .endEvent()
        .done();

      String deploymentId = repositoryService
        .createDeployment()
        .addModelInstance("foo.bpmn", aProcessDefinition)
        .deploy()
        .getId();

      deploymentIds.add(deploymentId);

    // start two process instance and leave them active
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // start one process instance and suspend it
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("foo", 0);
    ProcessInstance suspendedProcessInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    runtimeService.suspendProcessInstanceById(suspendedProcessInstance.getProcessInstanceId());

    // assume
    assertEquals(2, historyService.createHistoricProcessInstanceQuery().processDefinitionKey("oneTaskProcess").active().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processDefinitionKey("oneTaskProcess").suspended().count());

    // then
    assertEquals(3, historyService.createHistoricProcessInstanceQuery().or().active().variableValueEquals("foo", 0).endOr().list().size());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void shouldReturnByProcessDefinitionKeyOrActivityId() {
    // given
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    BpmnModelInstance aProcessDefinition = Bpmn.createExecutableProcess("process")
        .startEvent()
          .userTask("aUserTask")
        .endEvent()
        .done();

    String deploymentId = repositoryService
        .createDeployment()
        .addModelInstance("foo.bpmn", aProcessDefinition)
        .deploy()
        .getId();

    deploymentIds.add(deploymentId);

    runtimeService.startProcessInstanceByKey("process");

    // when
    List<HistoricProcessInstance> processInstances = historyService.createHistoricProcessInstanceQuery()
        .or()
          .activeActivityIdIn("theTask")
          .processDefinitionKey("process")
        .endOr()
        .list();

    // then
    assertThat(processInstances.size()).isEqualTo(2);
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void shouldReturnByProcessDefinitionIdOrIncidentType() {
    // given
    String processDefinitionId = runtimeService.startProcessInstanceByKey("oneTaskProcess")
        .getProcessDefinitionId();

    BpmnModelInstance aProcessDefinition = Bpmn.createExecutableProcess("process")
        .startEvent().camundaAsyncBefore()
          .userTask("aUserTask")
        .endEvent()
        .done();

    String deploymentId = repositoryService
        .createDeployment()
        .addModelInstance("foo.bpmn", aProcessDefinition)
        .deploy()
        .getId();

    deploymentIds.add(deploymentId);

    runtimeService.startProcessInstanceByKey("process");

    String jobId = managementService.createJobQuery().singleResult().getId();

    managementService.setJobRetries(jobId, 0);

    // when
    List<HistoricProcessInstance> processInstances = historyService.createHistoricProcessInstanceQuery()
        .or()
          .incidentType("failedJob")
          .processDefinitionId(processDefinitionId)
        .endOr()
        .list();

    // then
    assertThat(processInstances.size()).isEqualTo(2);
  }

}
