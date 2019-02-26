/*
 * Copyright © 2012 - 2018 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import static org.junit.Assert.assertEquals;

/**
 * @author Tassilo Weidner
 */
public class ProcessInstanceQueryOrTest {

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  protected RuntimeService runtimeService;
  protected RepositoryService repositoryService;

  @Before
  public void init() {
    runtimeService = processEngineRule.getRuntimeService();
    repositoryService = processEngineRule.getRepositoryService();
  }

  @Test
  public void shouldThrowExceptionByMissingStartOr() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set endOr() before or()");

    runtimeService.createProcessInstanceQuery()
      .or()
      .endOr()
      .endOr();
  }

  @Test
  public void shouldThrowExceptionByNesting() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set or() within 'or' query");

    runtimeService.createProcessInstanceQuery()
      .or()
        .or()
        .endOr()
      .endOr()
      .or()
      .endOr();
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void shouldReturnProcInstWithEmptyOrQuery() {
    // given
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery()
      .or()
      .endOr()
      .list();

    // then
    assertEquals(2, processInstances.size());

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void shouldReturnProcInstWithVarValue1OrVarValue2() {
    // given
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("stringVar", "abcdef");
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("stringVar", "ghijkl");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    // when
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery()
      .or()
        .variableValueEquals("stringVar", "abcdef")
        .variableValueEquals("stringVar", "ghijkl")
      .endOr()
      .list();

    // then
    assertEquals(2, processInstances.size());

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void shouldReturnProcInstWithMultipleOrCriteria() {
    // given
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("stringVar", "abcdef");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    runtimeService.setVariable(processInstance2.getProcessInstanceId(), "aVarName", "varValue");

    vars = new HashMap<String, Object>();
    vars.put("stringVar2", "aaabbbaaa");
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    runtimeService.setVariable(processInstance3.getProcessInstanceId(), "bVarName", "bTestb");

    vars = new HashMap<String, Object>();
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

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance3.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance4.getId(), "test");
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void shouldReturnProcInstFilteredByMultipleOrAndCriteria() {
    // given
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("stringVar", "abcdef");
    vars.put("longVar", 12345L);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("stringVar", "ghijkl");
    vars.put("longVar", 56789L);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("stringVar", "abcdef");
    vars.put("longVar", 56789L);
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("stringVar", "ghijkl");
    vars.put("longVar", 12345L);
    ProcessInstance processInstance4 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

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

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance3.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance4.getId(), "test");
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void shouldReturnProcInstFilteredByMultipleOrQueries() {
    // given
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("stringVar", "abcdef");
    vars.put("longVar", 12345L);
    vars.put("boolVar", true);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("stringVar", "ghijkl");
    vars.put("longVar", 56789L);
    vars.put("boolVar", true);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("stringVar", "abcdef");
    vars.put("longVar", 56789L);
    vars.put("boolVar", true);
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("stringVar", "ghijkl");
    vars.put("longVar", 12345L);
    vars.put("boolVar", true);
    ProcessInstance processInstance4 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("stringVar", "ghijkl");
    vars.put("longVar", 56789L);
    vars.put("boolVar", false);
    ProcessInstance processInstance5 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("stringVar", "abcdef");
    vars.put("longVar", 12345L);
    vars.put("boolVar", false);
    ProcessInstance processInstance6 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

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

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance3.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance4.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance5.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance6.getId(), "test");
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

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance3.getId(), "test");
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void shouldReturnProcInstWithVariableValueEqualsOrVariableValueGreaterThan() {
    // given
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("longVar", 12345L);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("longerVar", 56789L);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("longerVar", 56789L);
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    // when
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery()
      .or()
        .variableValueEquals("longVar", 12345L)
        .variableValueGreaterThan("longerVar", 20000L)
      .endOr();

    // then
    assertEquals(3, query.count());

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance3.getId(), "test");
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

    repositoryService
      .createDeployment()
      .addModelInstance("foo.bpmn", aProcessDefinition)
      .deploy();

    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("aProcessDefinition");

    BpmnModelInstance anotherProcessDefinition = Bpmn.createExecutableProcess("anotherProcessDefinition")
      .startEvent()
        .userTask()
      .endEvent()
      .done();

     repositoryService
       .createDeployment()
       .addModelInstance("foo.bpmn", anotherProcessDefinition)
       .deploy();

    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("anotherProcessDefinition");

    // when
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery()
      .or()
        .processDefinitionId(processInstance1.getProcessDefinitionId())
        .processDefinitionKey("anotherProcessDefinition")
      .endOr()
      .list();

    // then
    assertEquals(2, processInstances.size());

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
  }

  @Test
  public void shouldReturnProcInstWithBusinessKeyOrBusinessKeyLike() {
    // given
    BpmnModelInstance aProcessDefinition = Bpmn.createExecutableProcess("aProcessDefinition")
      .startEvent()
        .userTask()
      .endEvent()
      .done();

    repositoryService
      .createDeployment()
      .addModelInstance("foo.bpmn", aProcessDefinition)
      .deploy();

    ProcessInstance processInstance1 = runtimeService
      .startProcessInstanceByKey("aProcessDefinition", "aBusinessKey");

    BpmnModelInstance anotherProcessDefinition = Bpmn.createExecutableProcess("anotherProcessDefinition")
      .startEvent()
        .userTask()
      .endEvent()
      .done();

     repositoryService
       .createDeployment()
       .addModelInstance("foo.bpmn", anotherProcessDefinition)
       .deploy();

    ProcessInstance processInstance2 = runtimeService
      .startProcessInstanceByKey("anotherProcessDefinition", "anotherBusinessKey");

    // when
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery()
      .or()
        .processInstanceBusinessKey("aBusinessKey")
        .processInstanceBusinessKeyLike("anotherBusinessKey")
      .endOr()
      .list();

    // then
    assertEquals(2, processInstances.size());

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
  }

  @Test
  public void shouldReturnProcInstWithActivityIdInOrProcInstId() {
    // given
    BpmnModelInstance aProcessDefinition = Bpmn.createExecutableProcess("aProcessDefinition")
      .startEvent()
        .userTask()
      .endEvent()
      .done();

    repositoryService
      .createDeployment()
      .addModelInstance("foo.bpmn", aProcessDefinition)
      .deploy();

    ProcessInstance processInstance1 = runtimeService
      .startProcessInstanceByKey("aProcessDefinition");

    String activityInstanceId = runtimeService.getActivityInstance(processInstance1.getId())
      .getChildActivityInstances()[0].getActivityId();

    ProcessInstance processInstance2 = runtimeService
      .startProcessInstanceByKey("aProcessDefinition");

    // when
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery()
      .or()
        .activityIdIn(activityInstanceId)
        .processInstanceId(processInstance2.getId())
      .endOr()
      .list();

    // then
    assertEquals(2, processInstances.size());

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
  }

  @Test
  public void shouldReturnProcInstByVariableAndActiveProcesses() throws Exception {
    // given
    BpmnModelInstance aProcessDefinition = Bpmn.createExecutableProcess("oneTaskProcess")
        .startEvent()
          .userTask("testQuerySuspensionStateTask")
        .endEvent()
        .done();

      repositoryService
        .createDeployment()
        .addModelInstance("foo.bpmn", aProcessDefinition)
        .deploy();

    // start two process instance and leave them active
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // start one process instance and suspend it
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("foo", 0);
    ProcessInstance suspendedProcessInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    runtimeService.suspendProcessInstanceById(suspendedProcessInstance.getProcessInstanceId());

    // assume
    assertEquals(2, runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").active().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").suspended().count());

    // then
    assertEquals(3, runtimeService.createProcessInstanceQuery().or().active().variableValueEquals("foo", 0).endOr().list().size());

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    runtimeService.deleteProcessInstance(suspendedProcessInstance.getId(), "test");
  }
}
