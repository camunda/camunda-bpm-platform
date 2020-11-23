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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ProcessInstanceQueryOrTest {

  @Rule
  public ProcessEngineRule processEngineRule = new ProvidedProcessEngineRule();

  protected RuntimeService runtimeService;
  protected RepositoryService repositoryService;
  protected ManagementService managementService;

  protected List<String> deploymentIds = new ArrayList<>();

  @Before
  public void init() {
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

    // when/then
    assertThatThrownBy(() -> runtimeService.createProcessInstanceQuery()
        .or()
        .endOr()
        .endOr())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage: cannot set endOr() before or()");
  }

  @Test
  public void shouldThrowExceptionByNesting() {

    // when/then
    assertThatThrownBy(() -> runtimeService.createProcessInstanceQuery()
        .or()
        .or()
        .endOr()
        .endOr()
        .or()
        .endOr())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage: cannot set or() within 'or' query");
  }

  @Test
  public void shouldThrowExceptionOnOrderByProcessInstanceId() {

    // when/then
    assertThatThrownBy(() -> runtimeService.createProcessInstanceQuery()
        .or()
        .orderByProcessInstanceId()
        .endOr())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage: cannot set orderByProcessInstanceId() within 'or' query");
  }

  @Test
  public void shouldThrowExceptionOnOrderByProcessDefinitionId() {

    // when/then
    assertThatThrownBy(() -> runtimeService.createProcessInstanceQuery()
        .or()
        .orderByProcessDefinitionId()
      .endOr())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage: cannot set orderByProcessDefinitionId() within 'or' query");
  }

  @Test
  public void shouldThrowExceptionOnOrderByProcessDefinitionKey() {

    // when/then
    assertThatThrownBy(() -> runtimeService.createProcessInstanceQuery()
        .or()
          .orderByProcessDefinitionKey()
        .endOr())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage: cannot set orderByProcessDefinitionKey() within 'or' query");

  }

  @Test
  public void shouldThrowExceptionOnOorderByTenantIdd() {

    // when/then
    assertThatThrownBy(() -> runtimeService.createProcessInstanceQuery()
        .or()
          .orderByTenantId()
        .endOr())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage: cannot set orderByTenantId() within 'or' query");
  }

  @Test
  public void shouldThrowExceptionOnOrderByBusinessKey() {

    // when/then
    assertThatThrownBy(() -> runtimeService.createProcessInstanceQuery()
        .or()
          .orderByBusinessKey()
        .endOr())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage: cannot set orderByBusinessKey() within 'or' query");

  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void shouldReturnProcInstWithEmptyOrQuery() {
    // given
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery()
        .or()
        .endOr()
        .list();

    // then
    assertEquals(2, processInstances.size());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void shouldReturnProcInstWithVarValue1OrVarValue2() {
    // given
    Map<String, Object> vars = new HashMap<>();
    vars.put("stringVar", "abcdef");
    runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<>();
    vars.put("stringVar", "ghijkl");
    runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    // when
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery()
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
  public void shouldReturnProcInstWithMultipleOrCriteria() {
    // given
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    Map<String, Object> vars = new HashMap<>();
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
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery()
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
  public void shouldReturnProcInstFilteredByMultipleOrAndCriteria() {
    // given
    Map<String, Object> vars = new HashMap<>();
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
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery()
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
  public void shouldReturnProcInstFilteredByMultipleOrQueries() {
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
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery()
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
  public void shouldReturnProcInstWhereSameCriterionWasAppliedThreeTimesInOneQuery() {
    // given
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery()
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
  public void shouldReturnProcInstWithVariableValueEqualsOrVariableValueGreaterThan() {
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
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery()
        .or()
          .variableValueEquals("longVar", 12345L)
          .variableValueGreaterThan("longerVar", 20000L)
        .endOr();

    // then
    assertEquals(3, query.count());
  }

  @Test
  public void shouldReturnProcInstWithProcessDefinitionNameOrProcessDefinitionKey() {
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
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery()
        .or()
          .processDefinitionId(processInstance1.getProcessDefinitionId())
          .processDefinitionKey("anotherProcessDefinition")
        .endOr()
        .list();

    // then
    assertEquals(2, processInstances.size());
  }

  @Test
  public void shouldReturnProcInstWithBusinessKeyOrBusinessKeyLike() {
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
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery()
        .or()
          .processInstanceBusinessKey("aBusinessKey")
          .processInstanceBusinessKeyLike("anotherBusinessKey")
        .endOr()
        .list();

    // then
    assertEquals(2, processInstances.size());
  }

  @Test
  public void shouldReturnProcInstByVariableAndActiveProcesses() {
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
    Map<String, Object> variables = new HashMap<>();
    variables.put("foo", 0);
    ProcessInstance suspendedProcessInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    runtimeService.suspendProcessInstanceById(suspendedProcessInstance.getProcessInstanceId());

    List<ProcessInstance> activeProcessInstances = runtimeService.createProcessInstanceQuery()
        .processDefinitionKey("oneTaskProcess")
        .active()
        .list();

    List<ProcessInstance> suspendedProcessInstances = runtimeService.createProcessInstanceQuery()
        .processDefinitionKey("oneTaskProcess")
        .suspended()
        .list();

    // assume
    assertEquals(2, activeProcessInstances.size());
    assertEquals(1, suspendedProcessInstances.size());

    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery()
        .or()
          .active()
          .variableValueEquals("foo", 0)
        .endOr()
        .list();

    // then
    assertEquals(3, processInstances.size());
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
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery()
        .or()
          .activityIdIn("theTask")
          .processDefinitionKey("process")
        .endOr()
        .list();

    // then
    assertThat(processInstances.size()).isEqualTo(2);
  }

  @Test
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
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery()
        .or()
          .incidentType("failedJob")
          .processDefinitionId(processDefinitionId)
        .endOr()
        .list();

    // then
    assertThat(processInstances.size()).isEqualTo(2);
  }

}