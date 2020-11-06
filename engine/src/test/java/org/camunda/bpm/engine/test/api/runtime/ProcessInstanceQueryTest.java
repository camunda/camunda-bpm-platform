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
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.processInstanceByBusinessKey;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.processInstanceByProcessDefinitionId;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.processInstanceByProcessInstanceId;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.verifySorting;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.impl.ProcessInstanceQueryImpl;
import org.camunda.bpm.engine.impl.util.ImmutablePair;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.CompensationModels;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Joram Barrez
 * @author Frederik Heremans
 * @author Falko Menge
 */
public class ProcessInstanceQueryTest {

  public static final BpmnModelInstance FORK_JOIN_SUB_PROCESS_MODEL = ProcessModels.newModel()
    .startEvent()
    .subProcess("subProcess")
    .embeddedSubProcess()
      .startEvent()
      .parallelGateway("fork")
        .userTask("userTask1")
        .name("completeMe")
      .parallelGateway("join")
      .endEvent()
      .moveToNode("fork")
        .userTask("userTask2")
      .connectTo("join")
    .subProcessDone()
    .endEvent()
    .done();

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testHelper);

  private static String PROCESS_DEFINITION_KEY = "oneTaskProcess";
  private static String PROCESS_DEFINITION_KEY_2 = "otherOneTaskProcess";

  protected RuntimeService runtimeService;
  protected RepositoryService repositoryService;
  protected ManagementService managementService;
  protected CaseService caseService;

  protected List<String> processInstanceIds;

  @Before
  public void initServices() {
    runtimeService = engineRule.getRuntimeService();
    repositoryService = engineRule.getRepositoryService();
    managementService = engineRule.getManagementService();
    caseService = engineRule.getCaseService();
  }


  /**
   * Setup starts 4 process instances of oneTaskProcess
   * and 1 instance of otherOneTaskProcess
   */
  @Before
  public void deployTestProcesses() throws Exception {
    org.camunda.bpm.engine.repository.Deployment deployment = engineRule.getRepositoryService().createDeployment()
      .addClasspathResource("org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml")
      .addClasspathResource("org/camunda/bpm/engine/test/api/runtime/otherOneTaskProcess.bpmn20.xml")
      .deploy();

    engineRule.manageDeployment(deployment);

    RuntimeService runtimeService = engineRule.getRuntimeService();
    processInstanceIds = new ArrayList<>();
    for (int i = 0; i < 4; i++) {
      processInstanceIds.add(runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, i + "").getId());
    }
    processInstanceIds.add(runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY_2, "businessKey_123").getId());
  }


  @Test
  public void testQueryNoSpecificsList() {
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertEquals(5, query.count());
    assertEquals(5, query.list().size());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testQueryNoSpecificsDeploymentIdMappings() {
    // given
    String deploymentId = repositoryService.createDeploymentQuery().singleResult().getId();
    ImmutablePair<String, String>[] expectedMappings = processInstanceIds.stream()
        .map(id -> new ImmutablePair<>(deploymentId, id))
        .collect(Collectors.toList())
        .toArray(new ImmutablePair[0]);
    // when
    List<ImmutablePair<String, String>> mappings = engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired().execute((c) -> {
      ProcessInstanceQuery query = c.getProcessEngineConfiguration().getRuntimeService().createProcessInstanceQuery();
      return ((ProcessInstanceQueryImpl) query).listDeploymentIdMappings();
    });
    // then
    assertEquals(5, mappings.size());
    assertThat(mappings).containsExactlyInAnyOrder(expectedMappings);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testQueryNoSpecificsDeploymentIdMappingsInDifferentDeployments() {
    // given
    List<ImmutablePair<String, String>> expectedMappings = new ArrayList<>();
    String deploymentIdOne = repositoryService.createDeploymentQuery().singleResult().getId();
    processInstanceIds.stream()
      .map(id -> new ImmutablePair<>(deploymentIdOne, id))
      .forEach(expectedMappings::add);
    org.camunda.bpm.engine.repository.Deployment deploymentTwo = repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml")
        .deploy();
    engineRule.manageDeployment(deploymentTwo);
    ImmutablePair<String, String> newMapping = new ImmutablePair<>(deploymentTwo.getId(),
        runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY).getId());
    expectedMappings.add(newMapping);
    // when
    List<ImmutablePair<String, String>> mappings = engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired().execute((c) -> {
      ProcessInstanceQuery query = c.getProcessEngineConfiguration().getRuntimeService().createProcessInstanceQuery();
      return ((ProcessInstanceQueryImpl) query).listDeploymentIdMappings();
    });
    // then
    assertEquals(6, mappings.size());
    assertThat(mappings).containsExactlyInAnyOrder(expectedMappings.toArray(new ImmutablePair[0]));
    // ... and the items are sorted in ascending order by deployment id
    assertThat(mappings.get(mappings.size() - 1)).isEqualTo(newMapping);
  }

  @Test
  public void testQueryNoSpecificsSingleResult() {
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    try {
      query.singleResult();
      fail();
    } catch (ProcessEngineException e) {
      // Exception is expected
    }
  }

  @Test
  public void testQueryByProcessDefinitionKeySingleResult() {
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY_2);
    assertEquals(1, query.count());
    assertEquals(1, query.list().size());
    assertNotNull(query.singleResult());
  }

  @Test
  public void testQueryByInvalidProcessDefinitionKey() {
    assertNull(runtimeService.createProcessInstanceQuery().processDefinitionKey("invalid").singleResult());
    assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey("invalid").list().size());
  }

  @Test
  public void testQueryByProcessDefinitionKeyMultipleResults() {
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY);
    assertEquals(4, query.count());
    assertEquals(4, query.list().size());

    try {
      query.singleResult();
      fail();
    } catch (ProcessEngineException e) {
      // Exception is expected
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testQueryByProcessDefinitionKeyDeploymentIdMappings() {
    // given
    String deploymentId = repositoryService.createDeploymentQuery().singleResult().getId();
    List<String> relevantIds = engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired().execute((c) -> {
      ProcessInstanceQuery query = c.getProcessEngineConfiguration().getRuntimeService().createProcessInstanceQuery()
          .processDefinitionKey(PROCESS_DEFINITION_KEY);
      return ((ProcessInstanceQueryImpl) query).listIds();
    });
    List<ImmutablePair<String, String>> expectedMappings = relevantIds.stream()
        .map(id -> new ImmutablePair<>(deploymentId, id))
        .collect(Collectors.toList());
    // when
    List<ImmutablePair<String, String>> mappings = engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired().execute((c) -> {
      ProcessInstanceQuery query = c.getProcessEngineConfiguration().getRuntimeService().createProcessInstanceQuery()
          .processDefinitionKey(PROCESS_DEFINITION_KEY);
      return ((ProcessInstanceQueryImpl)query).listDeploymentIdMappings();
    });
    // then
    assertEquals(4, mappings.size());
    assertThat(mappings).containsExactlyInAnyOrder(expectedMappings.toArray(new ImmutablePair[0]));
  }

  @Test
  public void testQueryByProcessDefinitionKeyIn() {
    // given (deploy another process)
    ProcessDefinition oneTaskProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    String oneTaskProcessDefinitionId = oneTaskProcessDefinition.getId();
    runtimeService.startProcessInstanceById(oneTaskProcessDefinitionId);
    runtimeService.startProcessInstanceById(oneTaskProcessDefinitionId);

    // assume
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(7l);

    // when
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery()
      .processDefinitionKeyIn(PROCESS_DEFINITION_KEY, PROCESS_DEFINITION_KEY_2);

    // then
    assertThat(query.count()).isEqualTo(5l);
    assertThat(query.list().size()).isEqualTo(5);
  }

  @Test
  public void testQueryByNonExistingProcessDefinitionKeyIn() {
    // when
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery()
      .processDefinitionKeyIn("not-existing-key");

    // then
    assertThat(query.count()).isEqualTo(0l);
    assertThat(query.list().size()).isEqualTo(0);
  }

  @Test
  public void testQueryByOneInvalidProcessDefinitionKeyIn() {
    try {
      // when
      runtimeService.createProcessInstanceQuery()
        .processDefinitionKeyIn((String) null);
      fail();
    } catch(ProcessEngineException expected) {
      // then Exception is expected
    }
  }

  @Test
  public void testQueryByMultipleInvalidProcessDefinitionKeyIn() {
    try {
      // when
      runtimeService.createProcessInstanceQuery()
        .processDefinitionKeyIn(PROCESS_DEFINITION_KEY, null);
      fail();
    } catch(ProcessEngineException expected) {
      // Exception is expected
    }
  }

  @Test
  public void testQueryByProcessDefinitionKeyNotIn() {
    // given (deploy another process)
    ProcessDefinition oneTaskProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    String oneTaskProcessDefinitionId = oneTaskProcessDefinition.getId();
    runtimeService.startProcessInstanceById(oneTaskProcessDefinitionId);
    runtimeService.startProcessInstanceById(oneTaskProcessDefinitionId);

    // assume
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(7l);

    // when
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery()
      .processDefinitionKeyNotIn(PROCESS_DEFINITION_KEY, PROCESS_DEFINITION_KEY_2);

    // then
    assertThat(query.count()).isEqualTo(2l);
    assertThat(query.list().size()).isEqualTo(2);
  }

  @Test
  public void testQueryByNonExistingProcessDefinitionKeyNotIn() {
    // when
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery()
      .processDefinitionKeyNotIn("not-existing-key");

    // then
    assertThat(query.count()).isEqualTo(5l);
    assertThat(query.list().size()).isEqualTo(5);
  }

  @Test
  public void testQueryByOneInvalidProcessDefinitionKeyNotIn() {
    try {
      // when
      runtimeService.createProcessInstanceQuery()
        .processDefinitionKeyNotIn((String) null);
      fail();
    } catch(ProcessEngineException expected) {
      // then Exception is expected
    }
  }

  @Test
  public void testQueryByMultipleInvalidProcessDefinitionKeyNotIn() {
    try {
      // when
      runtimeService.createProcessInstanceQuery()
        .processDefinitionKeyNotIn(PROCESS_DEFINITION_KEY, null);
      fail();
    } catch(ProcessEngineException expected) {
      // then Exception is expected
    }
  }

  @Test
  public void testQueryByProcessInstanceId() {
    for (String processInstanceId : processInstanceIds) {
      assertNotNull(runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult());
      assertEquals(1, runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).list().size());
    }
  }

  @Test
  public void testQueryByBusinessKeyAndProcessDefinitionKey() {
    assertEquals(1, runtimeService.createProcessInstanceQuery().processInstanceBusinessKey("0", PROCESS_DEFINITION_KEY).count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().processInstanceBusinessKey("1", PROCESS_DEFINITION_KEY).count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().processInstanceBusinessKey("2", PROCESS_DEFINITION_KEY).count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().processInstanceBusinessKey("3", PROCESS_DEFINITION_KEY).count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().processInstanceBusinessKey("businessKey_123", PROCESS_DEFINITION_KEY_2).count());
  }

  @Test
  public void testQueryByBusinessKey() {
    assertEquals(1, runtimeService.createProcessInstanceQuery().processInstanceBusinessKey("0").count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().processInstanceBusinessKey("1").count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().processInstanceBusinessKey("businessKey_123").count());
  }

  @Test
  public void testQueryByBusinessKeyLike(){
    assertEquals(1, runtimeService.createProcessInstanceQuery().processInstanceBusinessKeyLike("business%").count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().processInstanceBusinessKeyLike("%sinessKey\\_123").count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().processInstanceBusinessKeyLike("%siness%").count());
  }

  @Test
  public void testQueryByInvalidBusinessKey() {
    assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceBusinessKey("invalid").count());

    try {
      runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(null).count();
      fail();
    } catch(ProcessEngineException ignored) {

    }
  }

  @Test
  public void testQueryByInvalidProcessInstanceId() {
    assertNull(runtimeService.createProcessInstanceQuery().processInstanceId("I do not exist").singleResult());
    assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId("I do not exist").list().size());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/superProcess.bpmn20.xml",
                           "org/camunda/bpm/engine/test/api/runtime/subProcess.bpmn20.xml"})
  public void testQueryBySuperProcessInstanceId() {
    ProcessInstance superProcessInstance = runtimeService.startProcessInstanceByKey("subProcessQueryTest");

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().superProcessInstanceId(superProcessInstance.getId());
    ProcessInstance subProcessInstance = query.singleResult();
    assertNotNull(subProcessInstance);
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }

  @Test
  public void testQueryByInvalidSuperProcessInstanceId() {
    assertNull(runtimeService.createProcessInstanceQuery().superProcessInstanceId("invalid").singleResult());
    assertEquals(0, runtimeService.createProcessInstanceQuery().superProcessInstanceId("invalid").list().size());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/superProcess.bpmn20.xml",
                           "org/camunda/bpm/engine/test/api/runtime/subProcess.bpmn20.xml"})
  @Test
  public void testQueryBySubProcessInstanceId() {
    ProcessInstance superProcessInstance = runtimeService.startProcessInstanceByKey("subProcessQueryTest");

    ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(superProcessInstance.getId()).singleResult();
    assertNotNull(subProcessInstance);
    assertEquals(superProcessInstance.getId(), runtimeService.createProcessInstanceQuery().subProcessInstanceId(subProcessInstance.getId()).singleResult().getId());
  }

  @Test
  public void testQueryByInvalidSubProcessInstanceId() {
    assertNull(runtimeService.createProcessInstanceQuery().subProcessInstanceId("invalid").singleResult());
    assertEquals(0, runtimeService.createProcessInstanceQuery().subProcessInstanceId("invalid").list().size());
  }

  // Nested subprocess make the query complexer, hence this test
  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/superProcessWithNestedSubProcess.bpmn20.xml",
                           "org/camunda/bpm/engine/test/api/runtime/nestedSubProcess.bpmn20.xml",
                           "org/camunda/bpm/engine/test/api/runtime/subProcess.bpmn20.xml"})
  public void testQueryBySuperProcessInstanceIdNested() {
    ProcessInstance superProcessInstance = runtimeService.startProcessInstanceByKey("nestedSubProcessQueryTest");

    ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(superProcessInstance.getId()).singleResult();
    assertNotNull(subProcessInstance);

    ProcessInstance nestedSubProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(subProcessInstance.getId()).singleResult();
    assertNotNull(nestedSubProcessInstance);
  }

  //Nested subprocess make the query complexer, hence this test
  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/superProcessWithNestedSubProcess.bpmn20.xml",
          "org/camunda/bpm/engine/test/api/runtime/nestedSubProcess.bpmn20.xml",
          "org/camunda/bpm/engine/test/api/runtime/subProcess.bpmn20.xml"})
  public void testQueryBySubProcessInstanceIdNested() {
    ProcessInstance superProcessInstance = runtimeService.startProcessInstanceByKey("nestedSubProcessQueryTest");

    ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(superProcessInstance.getId()).singleResult();
    assertEquals(superProcessInstance.getId(), runtimeService.createProcessInstanceQuery().subProcessInstanceId(subProcessInstance.getId()).singleResult().getId());

    ProcessInstance nestedSubProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(subProcessInstance.getId()).singleResult();
    assertEquals(subProcessInstance.getId(), runtimeService.createProcessInstanceQuery().subProcessInstanceId(nestedSubProcessInstance.getId()).singleResult().getId());
  }

  @Test
  public void testQueryPaging() {
    assertEquals(4, runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).count());
    assertEquals(2, runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).listPage(0, 2).size());
    assertEquals(3, runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).listPage(1, 3).size());
  }

  @Test
  public void testQuerySorting() {
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().orderByProcessInstanceId().asc().list();
    assertEquals(5, processInstances.size());
    verifySorting(processInstances, processInstanceByProcessInstanceId());

    processInstances = runtimeService.createProcessInstanceQuery().orderByProcessDefinitionId().asc().list();
    assertEquals(5, processInstances.size());
    verifySorting(processInstances, processInstanceByProcessDefinitionId());

    processInstances = runtimeService.createProcessInstanceQuery().orderByBusinessKey().asc().list();
    assertEquals(5, processInstances.size());
    verifySorting(processInstances, processInstanceByBusinessKey());

    assertEquals(5, runtimeService.createProcessInstanceQuery().orderByProcessDefinitionKey().asc().list().size());

    assertEquals(5, runtimeService.createProcessInstanceQuery().orderByProcessInstanceId().desc().list().size());
    assertEquals(5, runtimeService.createProcessInstanceQuery().orderByProcessDefinitionId().desc().list().size());
    assertEquals(5, runtimeService.createProcessInstanceQuery().orderByProcessDefinitionKey().desc().list().size());
    assertEquals(5, runtimeService.createProcessInstanceQuery().orderByBusinessKey().desc().list().size());

    assertEquals(4, runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).orderByProcessInstanceId().asc().list().size());
    assertEquals(4, runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).orderByProcessInstanceId().desc().list().size());
  }

  @Test
  public void testQueryInvalidSorting() {
    try {
      runtimeService.createProcessInstanceQuery().orderByProcessDefinitionId().list(); // asc - desc not called -> exception
      fail();
    }catch (ProcessEngineException ignored) {}
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryStringVariable() {
    Map<String, Object> vars = new HashMap<>();
    vars.put("stringVar", "abcdef");
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<>();
    vars.put("stringVar", "abcdef");
    vars.put("stringVar2", "ghijkl");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<>();
    vars.put("stringVar", "azerty");
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    // Test EQUAL on single string variable, should result in 2 matches
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().variableValueEquals("stringVar", "abcdef");
    List<ProcessInstance> processInstances = query.list();
    assertNotNull(processInstances);
    Assert.assertEquals(2, processInstances.size());

    // Test EQUAL on two string variables, should result in single match
    query = runtimeService.createProcessInstanceQuery().variableValueEquals("stringVar", "abcdef").variableValueEquals("stringVar2", "ghijkl");
    ProcessInstance resultInstance = query.singleResult();
    assertNotNull(resultInstance);
    Assert.assertEquals(processInstance2.getId(), resultInstance.getId());

    // Test NOT_EQUAL, should return only 1 resultInstance
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueNotEquals("stringVar", "abcdef").singleResult();
    assertNotNull(resultInstance);
    Assert.assertEquals(processInstance3.getId(), resultInstance.getId());

    // Test GREATER_THAN, should return only matching 'azerty'
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThan("stringVar", "abcdef").singleResult();
    assertNotNull(resultInstance);
    Assert.assertEquals(processInstance3.getId(), resultInstance.getId());

    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThan("stringVar", "z").singleResult();
    assertNull(resultInstance);

    // Test GREATER_THAN_OR_EQUAL, should return 3 results
    assertEquals(3, runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("stringVar", "abcdef").count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("stringVar", "z").count());

    // Test LESS_THAN, should return 2 results
    processInstances = runtimeService.createProcessInstanceQuery().variableValueLessThan("stringVar", "abcdeg").list();
    Assert.assertEquals(2, processInstances.size());
    List<String> expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    List<String> ids = new ArrayList<>(Arrays.asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());

    assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueLessThan("stringVar", "abcdef").count());
    assertEquals(3, runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("stringVar", "z").count());

    // Test LESS_THAN_OR_EQUAL
    processInstances = runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("stringVar", "abcdef").list();
    Assert.assertEquals(2, processInstances.size());
    expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    ids = new ArrayList<>(Arrays.asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());

    assertEquals(3, runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("stringVar", "z").count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("stringVar", "aa").count());

    // Test LIKE
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueLike("stringVar", "azert%").singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());

    resultInstance = runtimeService.createProcessInstanceQuery().variableValueLike("stringVar", "%y").singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());

    resultInstance = runtimeService.createProcessInstanceQuery().variableValueLike("stringVar", "%zer%").singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());

    assertEquals(3, runtimeService.createProcessInstanceQuery().variableValueLike("stringVar", "a%").count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueLike("stringVar", "%x%").count());

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance3.getId(), "test");
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryLongVariable() {
    Map<String, Object> vars = new HashMap<>();
    vars.put("longVar", 12345L);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<>();
    vars.put("longVar", 12345L);
    vars.put("longVar2", 67890L);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<>();
    vars.put("longVar", 55555L);
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    // Query on single long variable, should result in 2 matches
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().variableValueEquals("longVar", 12345L);
    List<ProcessInstance> processInstances = query.list();
    assertNotNull(processInstances);
    Assert.assertEquals(2, processInstances.size());

    // Query on two long variables, should result in single match
    query = runtimeService.createProcessInstanceQuery().variableValueEquals("longVar", 12345L).variableValueEquals("longVar2", 67890L);
    ProcessInstance resultInstance = query.singleResult();
    assertNotNull(resultInstance);
    Assert.assertEquals(processInstance2.getId(), resultInstance.getId());

    // Query with unexisting variable value
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueEquals("longVar", 999L).singleResult();
    assertNull(resultInstance);

    // Test NOT_EQUALS
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueNotEquals("longVar", 12345L).singleResult();
    assertNotNull(resultInstance);
    Assert.assertEquals(processInstance3.getId(), resultInstance.getId());

    // Test GREATER_THAN
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThan("longVar", 44444L).singleResult();
    assertNotNull(resultInstance);
    Assert.assertEquals(processInstance3.getId(), resultInstance.getId());

    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueGreaterThan("longVar", 55555L).count());
    Assert.assertEquals(3, runtimeService.createProcessInstanceQuery().variableValueGreaterThan("longVar",1L).count());

    // Test GREATER_THAN_OR_EQUAL
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("longVar", 44444L).singleResult();
    assertNotNull(resultInstance);
    Assert.assertEquals(processInstance3.getId(), resultInstance.getId());

    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("longVar", 55555L).singleResult();
    assertNotNull(resultInstance);
    Assert.assertEquals(processInstance3.getId(), resultInstance.getId());

    Assert.assertEquals(3, runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("longVar",1L).count());

    // Test LESS_THAN
    processInstances = runtimeService.createProcessInstanceQuery().variableValueLessThan("longVar", 55555L).list();
    Assert.assertEquals(2, processInstances.size());

    List<String> expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    List<String> ids = new ArrayList<>(Arrays.asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());

    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueLessThan("longVar", 12345L).count());
    Assert.assertEquals(3, runtimeService.createProcessInstanceQuery().variableValueLessThan("longVar",66666L).count());

    // Test LESS_THAN_OR_EQUAL
    processInstances = runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("longVar", 55555L).list();
    Assert.assertEquals(3, processInstances.size());

    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("longVar", 12344L).count());

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance3.getId(), "test");
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryDoubleVariable() {
    Map<String, Object> vars = new HashMap<>();
    vars.put("doubleVar", 12345.6789);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<>();
    vars.put("doubleVar", 12345.6789);
    vars.put("doubleVar2", 9876.54321);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<>();
    vars.put("doubleVar", 55555.5555);
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    // Query on single double variable, should result in 2 matches
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().variableValueEquals("doubleVar", 12345.6789);
    List<ProcessInstance> processInstances = query.list();
    assertNotNull(processInstances);
    Assert.assertEquals(2, processInstances.size());

    // Query on two double variables, should result in single value
    query = runtimeService.createProcessInstanceQuery().variableValueEquals("doubleVar", 12345.6789).variableValueEquals("doubleVar2", 9876.54321);
    ProcessInstance resultInstance = query.singleResult();
    assertNotNull(resultInstance);
    Assert.assertEquals(processInstance2.getId(), resultInstance.getId());

    // Query with unexisting variable value
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueEquals("doubleVar", 9999.99).singleResult();
    assertNull(resultInstance);

    // Test NOT_EQUALS
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueNotEquals("doubleVar", 12345.6789).singleResult();
    assertNotNull(resultInstance);
    Assert.assertEquals(processInstance3.getId(), resultInstance.getId());

    // Test GREATER_THAN
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThan("doubleVar", 44444.4444).singleResult();
    assertNotNull(resultInstance);
    Assert.assertEquals(processInstance3.getId(), resultInstance.getId());

    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueGreaterThan("doubleVar", 55555.5555).count());
    Assert.assertEquals(3, runtimeService.createProcessInstanceQuery().variableValueGreaterThan("doubleVar",1.234).count());

    // Test GREATER_THAN_OR_EQUAL
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("doubleVar", 44444.4444).singleResult();
    assertNotNull(resultInstance);
    Assert.assertEquals(processInstance3.getId(), resultInstance.getId());

    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("doubleVar", 55555.5555).singleResult();
    assertNotNull(resultInstance);
    Assert.assertEquals(processInstance3.getId(), resultInstance.getId());

    Assert.assertEquals(3, runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("doubleVar",1.234).count());

    // Test LESS_THAN
    processInstances = runtimeService.createProcessInstanceQuery().variableValueLessThan("doubleVar", 55555.5555).list();
    Assert.assertEquals(2, processInstances.size());

    List<String> expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    List<String> ids = new ArrayList<>(Arrays.asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());

    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueLessThan("doubleVar", 12345.6789).count());
    Assert.assertEquals(3, runtimeService.createProcessInstanceQuery().variableValueLessThan("doubleVar",66666.6666).count());

    // Test LESS_THAN_OR_EQUAL
    processInstances = runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("doubleVar", 55555.5555).list();
    Assert.assertEquals(3, processInstances.size());

    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("doubleVar", 12344.6789).count());

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance3.getId(), "test");
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryIntegerVariable() {
    Map<String, Object> vars = new HashMap<>();
    vars.put("integerVar", 12345);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<>();
    vars.put("integerVar", 12345);
    vars.put("integerVar2", 67890);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<>();
    vars.put("integerVar", 55555);
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    // Query on single integer variable, should result in 2 matches
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().variableValueEquals("integerVar", 12345);
    List<ProcessInstance> processInstances = query.list();
    assertNotNull(processInstances);
    Assert.assertEquals(2, processInstances.size());

    // Query on two integer variables, should result in single value
    query = runtimeService.createProcessInstanceQuery().variableValueEquals("integerVar", 12345).variableValueEquals("integerVar2", 67890);
    ProcessInstance resultInstance = query.singleResult();
    assertNotNull(resultInstance);
    Assert.assertEquals(processInstance2.getId(), resultInstance.getId());

    // Query with unexisting variable value
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueEquals("integerVar", 9999).singleResult();
    assertNull(resultInstance);

    // Test NOT_EQUALS
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueNotEquals("integerVar", 12345).singleResult();
    assertNotNull(resultInstance);
    Assert.assertEquals(processInstance3.getId(), resultInstance.getId());

    // Test GREATER_THAN
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThan("integerVar", 44444).singleResult();
    assertNotNull(resultInstance);
    Assert.assertEquals(processInstance3.getId(), resultInstance.getId());

    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueGreaterThan("integerVar", 55555).count());
    Assert.assertEquals(3, runtimeService.createProcessInstanceQuery().variableValueGreaterThan("integerVar",1).count());

    // Test GREATER_THAN_OR_EQUAL
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("integerVar", 44444).singleResult();
    assertNotNull(resultInstance);
    Assert.assertEquals(processInstance3.getId(), resultInstance.getId());

    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("integerVar", 55555).singleResult();
    assertNotNull(resultInstance);
    Assert.assertEquals(processInstance3.getId(), resultInstance.getId());

    Assert.assertEquals(3, runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("integerVar",1).count());

    // Test LESS_THAN
    processInstances = runtimeService.createProcessInstanceQuery().variableValueLessThan("integerVar", 55555).list();
    Assert.assertEquals(2, processInstances.size());

    List<String> expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    List<String> ids = new ArrayList<>(Arrays.asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());

    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueLessThan("integerVar", 12345).count());
    Assert.assertEquals(3, runtimeService.createProcessInstanceQuery().variableValueLessThan("integerVar",66666).count());

    // Test LESS_THAN_OR_EQUAL
    processInstances = runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("integerVar", 55555).list();
    Assert.assertEquals(3, processInstances.size());

    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("integerVar", 12344).count());

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance3.getId(), "test");
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryShortVariable() {
    Map<String, Object> vars = new HashMap<>();
    short shortVar = 1234;
    vars.put("shortVar", shortVar);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    short shortVar2 = 6789;
    vars = new HashMap<>();
    vars.put("shortVar", shortVar);
    vars.put("shortVar2", shortVar2);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<>();
    vars.put("shortVar", (short)5555);
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    // Query on single short variable, should result in 2 matches
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().variableValueEquals("shortVar", shortVar);
    List<ProcessInstance> processInstances = query.list();
    assertNotNull(processInstances);
    Assert.assertEquals(2, processInstances.size());

    // Query on two short variables, should result in single value
    query = runtimeService.createProcessInstanceQuery().variableValueEquals("shortVar", shortVar).variableValueEquals("shortVar2", shortVar2);
    ProcessInstance resultInstance = query.singleResult();
    assertNotNull(resultInstance);
    Assert.assertEquals(processInstance2.getId(), resultInstance.getId());

    // Query with unexisting variable value
    short unexistingValue = (short)9999;
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueEquals("shortVar", unexistingValue).singleResult();
    assertNull(resultInstance);

    // Test NOT_EQUALS
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueNotEquals("shortVar", (short)1234).singleResult();
    assertNotNull(resultInstance);
    Assert.assertEquals(processInstance3.getId(), resultInstance.getId());

    // Test GREATER_THAN
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThan("shortVar", (short)4444).singleResult();
    assertNotNull(resultInstance);
    Assert.assertEquals(processInstance3.getId(), resultInstance.getId());

    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueGreaterThan("shortVar", (short)5555).count());
    Assert.assertEquals(3, runtimeService.createProcessInstanceQuery().variableValueGreaterThan("shortVar",(short)1).count());

    // Test GREATER_THAN_OR_EQUAL
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("shortVar", (short)4444).singleResult();
    assertNotNull(resultInstance);
    Assert.assertEquals(processInstance3.getId(), resultInstance.getId());

    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("shortVar", (short)5555).singleResult();
    assertNotNull(resultInstance);
    Assert.assertEquals(processInstance3.getId(), resultInstance.getId());

    Assert.assertEquals(3, runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("shortVar",(short)1).count());

    // Test LESS_THAN
    processInstances = runtimeService.createProcessInstanceQuery().variableValueLessThan("shortVar", (short)5555).list();
    Assert.assertEquals(2, processInstances.size());

    List<String> expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    List<String> ids = new ArrayList<>(Arrays.asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());

    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueLessThan("shortVar", (short)1234).count());
    Assert.assertEquals(3, runtimeService.createProcessInstanceQuery().variableValueLessThan("shortVar",(short)6666).count());

    // Test LESS_THAN_OR_EQUAL
    processInstances = runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("shortVar", (short)5555).list();
    Assert.assertEquals(3, processInstances.size());

    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("shortVar", (short)1233).count());

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance3.getId(), "test");
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryDateVariable() throws Exception {
    Map<String, Object> vars = new HashMap<>();
    Date date1 = Calendar.getInstance().getTime();
    vars.put("dateVar", date1);

    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    Date date2 = Calendar.getInstance().getTime();
    vars = new HashMap<>();
    vars.put("dateVar", date1);
    vars.put("dateVar2", date2);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    Calendar nextYear = Calendar.getInstance();
    nextYear.add(Calendar.YEAR, 1);
    vars = new HashMap<>();
    vars.put("dateVar",nextYear.getTime());
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    Calendar nextMonth = Calendar.getInstance();
    nextMonth.add(Calendar.MONTH, 1);

    Calendar twoYearsLater = Calendar.getInstance();
    twoYearsLater.add(Calendar.YEAR, 2);

    Calendar oneYearAgo = Calendar.getInstance();
    oneYearAgo.add(Calendar.YEAR, -1);

    // Query on single short variable, should result in 2 matches
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().variableValueEquals("dateVar", date1);
    List<ProcessInstance> processInstances = query.list();
    assertNotNull(processInstances);
    Assert.assertEquals(2, processInstances.size());

    // Query on two short variables, should result in single value
    query = runtimeService.createProcessInstanceQuery().variableValueEquals("dateVar", date1).variableValueEquals("dateVar2", date2);
    ProcessInstance resultInstance = query.singleResult();
    assertNotNull(resultInstance);
    Assert.assertEquals(processInstance2.getId(), resultInstance.getId());

    // Query with unexisting variable value
    Date unexistingDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse("01/01/1989 12:00:00");
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueEquals("dateVar", unexistingDate).singleResult();
    assertNull(resultInstance);

    // Test NOT_EQUALS
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueNotEquals("dateVar", date1).singleResult();
    assertNotNull(resultInstance);
    Assert.assertEquals(processInstance3.getId(), resultInstance.getId());

    // Test GREATER_THAN
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThan("dateVar", nextMonth.getTime()).singleResult();
    assertNotNull(resultInstance);
    Assert.assertEquals(processInstance3.getId(), resultInstance.getId());

    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueGreaterThan("dateVar", nextYear.getTime()).count());
    Assert.assertEquals(3, runtimeService.createProcessInstanceQuery().variableValueGreaterThan("dateVar", oneYearAgo.getTime()).count());

    // Test GREATER_THAN_OR_EQUAL
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("dateVar", nextMonth.getTime()).singleResult();
    assertNotNull(resultInstance);
    Assert.assertEquals(processInstance3.getId(), resultInstance.getId());

    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("dateVar", nextYear.getTime()).singleResult();
    assertNotNull(resultInstance);
    Assert.assertEquals(processInstance3.getId(), resultInstance.getId());

    Assert.assertEquals(3, runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("dateVar",oneYearAgo.getTime()).count());

    // Test LESS_THAN
    processInstances = runtimeService.createProcessInstanceQuery().variableValueLessThan("dateVar", nextYear.getTime()).list();
    Assert.assertEquals(2, processInstances.size());

    List<String> expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    List<String> ids = new ArrayList<>(Arrays.asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());

    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueLessThan("dateVar", date1).count());
    Assert.assertEquals(3, runtimeService.createProcessInstanceQuery().variableValueLessThan("dateVar", twoYearsLater.getTime()).count());

    // Test LESS_THAN_OR_EQUAL
    processInstances = runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("dateVar", nextYear.getTime()).list();
    Assert.assertEquals(3, processInstances.size());

    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("dateVar", oneYearAgo.getTime()).count());

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance3.getId(), "test");
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testBooleanVariable() throws Exception {

    // TEST EQUALS
    HashMap<String, Object> vars = new HashMap<>();
    vars.put("booleanVar", true);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<>();
    vars.put("booleanVar", false);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery().variableValueEquals("booleanVar", true).list();

    assertNotNull(instances);
    assertEquals(1, instances.size());
    assertEquals(processInstance1.getId(), instances.get(0).getId());

    instances = runtimeService.createProcessInstanceQuery().variableValueEquals("booleanVar", false).list();

    assertNotNull(instances);
    assertEquals(1, instances.size());
    assertEquals(processInstance2.getId(), instances.get(0).getId());

    // TEST NOT_EQUALS
    instances = runtimeService.createProcessInstanceQuery().variableValueNotEquals("booleanVar", true).list();

    assertNotNull(instances);
    assertEquals(1, instances.size());
    assertEquals(processInstance2.getId(), instances.get(0).getId());

    instances = runtimeService.createProcessInstanceQuery().variableValueNotEquals("booleanVar", false).list();

    assertNotNull(instances);
    assertEquals(1, instances.size());
    assertEquals(processInstance1.getId(), instances.get(0).getId());

    // Test unsupported operations
    try {
      runtimeService.createProcessInstanceQuery().variableValueGreaterThan("booleanVar", true);
      fail("Excetion expected");
    } catch(ProcessEngineException ae) {
      assertThat(ae.getMessage()).contains("Booleans and null cannot be used in 'greater than' condition");
    }

    try {
      runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("booleanVar", true);
      fail("Excetion expected");
    } catch(ProcessEngineException ae) {
      assertThat(ae.getMessage()).contains("Booleans and null cannot be used in 'greater than or equal' condition");
    }

    try {
      runtimeService.createProcessInstanceQuery().variableValueLessThan("booleanVar", true);
      fail("Excetion expected");
    } catch(ProcessEngineException ae) {
      assertThat(ae.getMessage()).contains("Booleans and null cannot be used in 'less than' condition");
    }

    try {
      runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("booleanVar", true);
      fail("Excetion expected");
    } catch(ProcessEngineException ae) {
      assertThat(ae.getMessage()).contains("Booleans and null cannot be used in 'less than or equal' condition");
    }

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryVariablesUpdatedToNullValue() {
    // Start process instance with different types of variables
    Map<String, Object> variables = new HashMap<>();
    variables.put("longVar", 928374L);
    variables.put("shortVar", (short) 123);
    variables.put("integerVar", 1234);
    variables.put("stringVar", "coca-cola");
    variables.put("dateVar", new Date());
    variables.put("booleanVar", true);
    variables.put("nullVar", null);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery()
      .variableValueEquals("longVar", null)
      .variableValueEquals("shortVar", null)
      .variableValueEquals("integerVar", null)
      .variableValueEquals("stringVar", null)
      .variableValueEquals("booleanVar", null)
      .variableValueEquals("dateVar", null);

    ProcessInstanceQuery notQuery = runtimeService.createProcessInstanceQuery()
      .variableValueNotEquals("longVar", null)
      .variableValueNotEquals("shortVar", null)
      .variableValueNotEquals("integerVar", null)
      .variableValueNotEquals("stringVar", null)
      .variableValueNotEquals("booleanVar", null)
      .variableValueNotEquals("dateVar", null);

    assertNull(query.singleResult());
    assertNotNull(notQuery.singleResult());

    // Set all existing variables values to null
    runtimeService.setVariable(processInstance.getId(), "longVar", null);
    runtimeService.setVariable(processInstance.getId(), "shortVar", null);
    runtimeService.setVariable(processInstance.getId(), "integerVar", null);
    runtimeService.setVariable(processInstance.getId(), "stringVar", null);
    runtimeService.setVariable(processInstance.getId(), "dateVar", null);
    runtimeService.setVariable(processInstance.getId(), "nullVar", null);
    runtimeService.setVariable(processInstance.getId(), "booleanVar", null);

    Execution queryResult = query.singleResult();
    assertNotNull(queryResult);
    assertEquals(processInstance.getId(), queryResult.getId());
    assertNull(notQuery.singleResult());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryNullVariable() throws Exception {
    Map<String, Object> vars = new HashMap<>();
    vars.put("nullVar", null);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<>();
    vars.put("nullVar", "notnull");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<>();
    vars.put("nullVarLong", "notnull");
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<>();
    vars.put("nullVarDouble", "notnull");
    ProcessInstance processInstance4 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<>();
    vars.put("nullVarByte", "testbytes".getBytes());
    ProcessInstance processInstance5 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    // Query on null value, should return one value
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().variableValueEquals("nullVar", null);
    List<ProcessInstance> processInstances = query.list();
    assertNotNull(processInstances);
    Assert.assertEquals(1, processInstances.size());
    Assert.assertEquals(processInstance1.getId(), processInstances.get(0).getId());

    // Test NOT_EQUALS null
    Assert.assertEquals(1, runtimeService.createProcessInstanceQuery().variableValueNotEquals("nullVar", null).count());
    Assert.assertEquals(1, runtimeService.createProcessInstanceQuery().variableValueNotEquals("nullVarLong", null).count());
    Assert.assertEquals(1, runtimeService.createProcessInstanceQuery().variableValueNotEquals("nullVarDouble", null).count());
    // When a byte-array refrence is present, the variable is not considered null
    Assert.assertEquals(1, runtimeService.createProcessInstanceQuery().variableValueNotEquals("nullVarByte", null).count());

    // All other variable queries with null should throw exception
    try {
      runtimeService.createProcessInstanceQuery().variableValueGreaterThan("nullVar", null);
      fail("Excetion expected");
    } catch(ProcessEngineException ae) {
      assertThat(ae.getMessage()).contains("Booleans and null cannot be used in 'greater than' condition");
    }

    try {
      runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("nullVar", null);
      fail("Excetion expected");
    } catch(ProcessEngineException ae) {
      assertThat(ae.getMessage()).contains("Booleans and null cannot be used in 'greater than or equal' condition");
    }

    try {
      runtimeService.createProcessInstanceQuery().variableValueLessThan("nullVar", null);
      fail("Excetion expected");
    } catch(ProcessEngineException ae) {
      assertThat(ae.getMessage()).contains("Booleans and null cannot be used in 'less than' condition");
    }

    try {
      runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("nullVar", null);
      fail("Excetion expected");
    } catch(ProcessEngineException ae) {
      assertThat(ae.getMessage()).contains("Booleans and null cannot be used in 'less than or equal' condition");
    }

    try {
      runtimeService.createProcessInstanceQuery().variableValueLike("nullVar", null);
      fail("Excetion expected");
    } catch(ProcessEngineException ae) {
      assertThat(ae.getMessage()).contains("Booleans and null cannot be used in 'like' condition");
    }

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance3.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance4.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance5.getId(), "test");
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryInvalidTypes() throws Exception {
    Map<String, Object> vars = new HashMap<>();
    vars.put("bytesVar", "test".getBytes());
    vars.put("serializableVar",new DummySerializable());

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    try {
      runtimeService.createProcessInstanceQuery()
        .variableValueEquals("bytesVar", "test".getBytes())
        .list();
      fail("Expected exception");
    } catch(ProcessEngineException ae) {
      assertThat(ae.getMessage()).contains("Variables of type ByteArray cannot be used to query");
    }

    try {
      runtimeService.createProcessInstanceQuery()
        .variableValueEquals("serializableVar", new DummySerializable())
        .list();
      fail("Expected exception");
    } catch(ProcessEngineException ae) {
      assertThat(ae.getMessage()).contains("Object values cannot be used to query");
    }

    runtimeService.deleteProcessInstance(processInstance.getId(), "test");
  }

  @Test
  public void testQueryVariablesNullNameArgument() {
    try {
      runtimeService.createProcessInstanceQuery().variableValueEquals(null, "value");
      fail("Expected exception");
    } catch(ProcessEngineException ae) {
      assertThat(ae.getMessage()).contains("name is null");
    }
    try {
      runtimeService.createProcessInstanceQuery().variableValueNotEquals(null, "value");
      fail("Expected exception");
    } catch(ProcessEngineException ae) {
      assertThat(ae.getMessage()).contains("name is null");
    }
    try {
      runtimeService.createProcessInstanceQuery().variableValueGreaterThan(null, "value");
      fail("Expected exception");
    } catch(ProcessEngineException ae) {
      assertThat(ae.getMessage()).contains("name is null");
    }
    try {
      runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual(null, "value");
      fail("Expected exception");
    } catch(ProcessEngineException ae) {
      assertThat(ae.getMessage()).contains("name is null");
    }
    try {
      runtimeService.createProcessInstanceQuery().variableValueLessThan(null, "value");
      fail("Expected exception");
    } catch(ProcessEngineException ae) {
      assertThat(ae.getMessage()).contains("name is null");
    }
    try {
      runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual(null, "value");
      fail("Expected exception");
    } catch(ProcessEngineException ae) {
      assertThat(ae.getMessage()).contains("name is null");
    }
    try {
      runtimeService.createProcessInstanceQuery().variableValueLike(null, "value");
      fail("Expected exception");
    } catch(ProcessEngineException ae) {
      assertThat(ae.getMessage()).contains("name is null");
    }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryAllVariableTypes() throws Exception {
    Map<String, Object> vars = new HashMap<>();
    vars.put("nullVar", null);
    vars.put("stringVar", "string");
    vars.put("longVar", 10L);
    vars.put("doubleVar", 1.2);
    vars.put("integerVar", 1234);
    vars.put("booleanVar", true);
    vars.put("shortVar", (short) 123);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery()
      .variableValueEquals("nullVar", null)
      .variableValueEquals("stringVar", "string")
      .variableValueEquals("longVar", 10L)
      .variableValueEquals("doubleVar", 1.2)
      .variableValueEquals("integerVar", 1234)
      .variableValueEquals("booleanVar", true)
      .variableValueEquals("shortVar", (short) 123);

    List<ProcessInstance> processInstances = query.list();
    assertNotNull(processInstances);
    Assert.assertEquals(1, processInstances.size());
    Assert.assertEquals(processInstance.getId(), processInstances.get(0).getId());

    runtimeService.deleteProcessInstance(processInstance.getId(), "test");
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testClashingValues() throws Exception {
      Map<String, Object> vars = new HashMap<>();
      vars.put("var", 1234L);

      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

      Map<String, Object> vars2 = new HashMap<>();
      vars2.put("var", 1234);

      ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars2);

      List<ProcessInstance> foundInstances = runtimeService.createProcessInstanceQuery()
      .processDefinitionKey("oneTaskProcess")
      .variableValueEquals("var", 1234L)
      .list();

      assertEquals(1, foundInstances.size());
      assertEquals(processInstance.getId(), foundInstances.get(0).getId());

      runtimeService.deleteProcessInstance(processInstance.getId(), "test");
      runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
  }

  @Test
  public void testQueryByProcessInstanceIds() {
    Set<String> processInstanceIds = new HashSet<>(this.processInstanceIds);

    // start an instance that will not be part of the query
    runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY_2, "2");

    ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery().processInstanceIds(processInstanceIds);
    assertEquals(5, processInstanceQuery.count());

    List<ProcessInstance> processInstances = processInstanceQuery.list();
    assertNotNull(processInstances);
    assertEquals(5, processInstances.size());

    for (ProcessInstance processInstance : processInstances) {
      assertTrue(processInstanceIds.contains(processInstance.getId()));
    }
  }

  @Test
  public void testQueryByProcessInstanceIdsEmpty() {
    try {
      runtimeService.createProcessInstanceQuery().processInstanceIds(new HashSet<String>());
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      assertThat(re.getMessage()).contains("Set of process instance ids is empty");
    }
  }

  @Test
  public void testQueryByProcessInstanceIdsNull() {
    try {
      runtimeService.createProcessInstanceQuery().processInstanceIds(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      assertThat(re.getMessage()).contains("Set of process instance ids is null");
    }
  }

  @Test
  public void testQueryByActive() throws Exception {
    ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery();

    assertEquals(5, processInstanceQuery.active().count());

    repositoryService.suspendProcessDefinitionByKey(PROCESS_DEFINITION_KEY);

    assertEquals(5, processInstanceQuery.active().count());

    repositoryService.suspendProcessDefinitionByKey(PROCESS_DEFINITION_KEY, true, null);

    assertEquals(1, processInstanceQuery.active().count());
  }

  @Test
  public void testQueryBySuspended() throws Exception {
    ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery();

    assertEquals(0, processInstanceQuery.suspended().count());

    repositoryService.suspendProcessDefinitionByKey(PROCESS_DEFINITION_KEY);

    assertEquals(0, processInstanceQuery.suspended().count());

    repositoryService.suspendProcessDefinitionByKey(PROCESS_DEFINITION_KEY, true, null);

    assertEquals(4, processInstanceQuery.suspended().count());
  }

  @Test
  public void testNativeQuery() {
    String tablePrefix = engineRule.getProcessEngineConfiguration().getDatabaseTablePrefix();
    // just test that the query will be constructed and executed, details are tested in the TaskQueryTest
    assertEquals(tablePrefix + "ACT_RU_EXECUTION", managementService.getTableName(ProcessInstance.class));

    long piCount = runtimeService.createProcessInstanceQuery().count();

    assertEquals(piCount, runtimeService.createNativeProcessInstanceQuery().sql("SELECT * FROM " + managementService.getTableName(ProcessInstance.class)).list().size());
    assertEquals(piCount, runtimeService.createNativeProcessInstanceQuery().sql("SELECT count(*) FROM " + managementService.getTableName(ProcessInstance.class)).count());
  }

  @Test
  public void testNativeQueryPaging() {
    assertEquals(5, runtimeService.createNativeProcessInstanceQuery().sql("SELECT * FROM " + managementService.getTableName(ProcessInstance.class)).listPage(0, 5).size());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/failingProcessCreateOneIncident.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testQueryWithIncident() {
    ProcessInstance instanceWithIncident = runtimeService.startProcessInstanceByKey("failingProcess");
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    testHelper.executeAvailableJobs();

    List<Incident> incidentList = runtimeService.createIncidentQuery().list();
    assertEquals(1, incidentList.size());

    ProcessInstance instance = runtimeService.createProcessInstanceQuery().withIncident().singleResult();
    assertThat(instance.getId()).isEqualTo(instanceWithIncident.getId());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/failingProcessCreateOneIncident.bpmn20.xml"})
  public void testQueryByIncidentId() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingProcess");

    testHelper.executeAvailableJobs();

    List<Incident> incidentList = runtimeService.createIncidentQuery().list();
    assertEquals(1, incidentList.size());

    Incident incident = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).singleResult();

    List<ProcessInstance> processInstanceList = runtimeService
        .createProcessInstanceQuery()
        .incidentId(incident.getId()).list();

    assertEquals(1, processInstanceList.size());
  }

  @Test
  public void testQueryByInvalidIncidentId() {
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();

    assertEquals(0, query.incidentId("invalid").count());

    try {
      query.incidentId(null);
      fail();
    } catch (ProcessEngineException ignored) {}
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/failingProcessCreateOneIncident.bpmn20.xml"})
  public void testQueryByIncidentType() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingProcess");

    testHelper.executeAvailableJobs();

    List<Incident> incidentList = runtimeService.createIncidentQuery().list();
    assertEquals(1, incidentList.size());

    Incident incident = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).singleResult();

    List<ProcessInstance> processInstanceList = runtimeService
        .createProcessInstanceQuery()
        .incidentType(incident.getIncidentType()).list();

    assertEquals(1, processInstanceList.size());
  }

  @Test
  public void testQueryByInvalidIncidentType() {
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();

    assertEquals(0, query.incidentType("invalid").count());

    try {
      query.incidentType(null);
      fail();
    } catch (ProcessEngineException ignored) {}
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/failingProcessCreateOneIncident.bpmn20.xml"})
  public void testQueryByIncidentMessage() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingProcess");

    testHelper.executeAvailableJobs();

    List<Incident> incidentList = runtimeService.createIncidentQuery().list();
    assertEquals(1, incidentList.size());

    Incident incident = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).singleResult();

    List<ProcessInstance> processInstanceList = runtimeService
        .createProcessInstanceQuery()
        .incidentMessage(incident.getIncidentMessage()).list();

    assertEquals(1, processInstanceList.size());
  }

  @Test
  public void testQueryByInvalidIncidentMessage() {
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();

    assertEquals(0, query.incidentMessage("invalid").count());

    try {
      query.incidentMessage(null);
      fail();
    } catch (ProcessEngineException ignored) {}
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/failingProcessCreateOneIncident.bpmn20.xml"})
  public void testQueryByIncidentMessageLike() {
    runtimeService.startProcessInstanceByKey("failingProcess");

    testHelper.executeAvailableJobs();

    List<Incident> incidentList = runtimeService.createIncidentQuery().list();
    assertEquals(1, incidentList.size());

    List<ProcessInstance> processInstanceList = runtimeService
        .createProcessInstanceQuery()
        .incidentMessageLike("%\\_exception%").list();

    assertEquals(1, processInstanceList.size());
  }

  @Test
  public void testQueryByInvalidIncidentMessageLike() {
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();

    assertEquals(0, query.incidentMessageLike("invalid").count());

    try {
      query.incidentMessageLike(null);
      fail();
    } catch (ProcessEngineException ignored) {}
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/failingSubProcessCreateOneIncident.bpmn20.xml"})
  public void testQueryByIncidentIdInSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingSubProcess");

    testHelper.executeAvailableJobs();

    List<Incident> incidentList = runtimeService.createIncidentQuery().list();
    assertEquals(1, incidentList.size());

    Incident incident = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).singleResult();

    List<ProcessInstance> processInstanceList = runtimeService
        .createProcessInstanceQuery()
        .incidentId(incident.getId()).list();

    assertEquals(1, processInstanceList.size());
    assertEquals(processInstance.getId(), processInstanceList.get(0).getId());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/failingSubProcessCreateOneIncident.bpmn20.xml"})
  public void testQueryByIncidentTypeInSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingSubProcess");

    testHelper.executeAvailableJobs();

    List<Incident> incidentList = runtimeService.createIncidentQuery().list();
    assertEquals(1, incidentList.size());

    Incident incident = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).singleResult();

    List<ProcessInstance> processInstanceList = runtimeService
        .createProcessInstanceQuery()
        .incidentType(incident.getIncidentType()).list();

    assertEquals(1, processInstanceList.size());
    assertEquals(processInstance.getId(), processInstanceList.get(0).getId());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/failingSubProcessCreateOneIncident.bpmn20.xml"})
  public void testQueryByIncidentMessageInSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingSubProcess");

    testHelper.executeAvailableJobs();

    List<Incident> incidentList = runtimeService.createIncidentQuery().list();
    assertEquals(1, incidentList.size());

    Incident incident = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).singleResult();

    List<ProcessInstance> processInstanceList = runtimeService
        .createProcessInstanceQuery()
        .incidentMessage(incident.getIncidentMessage()).list();

    assertEquals(1, processInstanceList.size());
    assertEquals(processInstance.getId(), processInstanceList.get(0).getId());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/failingSubProcessCreateOneIncident.bpmn20.xml"})
  public void testQueryByIncidentMessageLikeInSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingSubProcess");

    testHelper.executeAvailableJobs();

    List<Incident> incidentList = runtimeService.createIncidentQuery().list();
    assertEquals(1, incidentList.size());

    List<ProcessInstance> processInstanceList = runtimeService
        .createProcessInstanceQuery()
        .incidentMessageLike("%exception%").list();

    assertEquals(1, processInstanceList.size());
    assertEquals(processInstance.getId(), processInstanceList.get(0).getId());
  }

  @Test
  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/cmmn/oneProcessTaskCase.cmmn",
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
    })
  public void testQueryByCaseInstanceId() {
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("oneProcessTaskCase")
      .create()
      .getId();

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();

    query.caseInstanceId(caseInstanceId);

    assertEquals(1, query.count());

    List<ProcessInstance> result = query.list();
    assertEquals(1, result.size());

    ProcessInstance processInstance = result.get(0);
    assertEquals(caseInstanceId, processInstance.getCaseInstanceId());
  }

  @Test
  public void testQueryByInvalidCaseInstanceId() {
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();

    query.caseInstanceId("invalid");

    assertEquals(0, query.count());

    try {
      query.caseInstanceId(null);
      fail("The passed case instance should not be null.");
    } catch (Exception ignored) {}

  }

  @Test
  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/runtime/superCase.cmmn",
      "org/camunda/bpm/engine/test/api/runtime/superProcessWithCallActivityInsideSubProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/runtime/subProcess.bpmn20.xml"
    })
  public void testQueryByCaseInstanceIdHierarchy() {
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("oneProcessTaskCase")
      .businessKey("aBusinessKey")
      .create()
      .getId();

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();

    query.caseInstanceId(caseInstanceId);

    assertEquals(2, query.count());

    List<ProcessInstance> result = query.list();
    assertEquals(2, result.size());

    ProcessInstance firstProcessInstance = result.get(0);
    assertEquals(caseInstanceId, firstProcessInstance.getCaseInstanceId());

    ProcessInstance secondProcessInstance = result.get(1);
    assertEquals(caseInstanceId, secondProcessInstance.getCaseInstanceId());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testProcessVariableValueEqualsNumber() throws Exception {
    // long
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 123L));

    // non-matching long
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 12345L));

    // short
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", (short) 123));

    // double
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 123.0d));

    // integer
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 123));

    // untyped null (should not match)
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.singletonMap("var", null));

    // typed null (should not match)
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", Variables.longValue(null)));

    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", "123"));

    assertEquals(4, runtimeService.createProcessInstanceQuery().variableValueEquals("var", Variables.numberValue(123)).count());
    assertEquals(4, runtimeService.createProcessInstanceQuery().variableValueEquals("var", Variables.numberValue(123L)).count());
    assertEquals(4, runtimeService.createProcessInstanceQuery().variableValueEquals("var", Variables.numberValue(123.0d)).count());
    assertEquals(4, runtimeService.createProcessInstanceQuery().variableValueEquals("var", Variables.numberValue((short) 123)).count());

    assertEquals(1, runtimeService.createProcessInstanceQuery().variableValueEquals("var", Variables.numberValue(null)).count());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testProcessVariableValueNumberComparison() throws Exception {
    // long
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 123L));

    // non-matching long
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 12345L));

    // short
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", (short) 123));

    // double
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 123.0d));

    // integer
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 123));

    // untyped null
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.singletonMap("var", null));

    // typed null
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", Variables.longValue(null)));

    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", "123"));

    assertEquals(4, runtimeService.createProcessInstanceQuery().variableValueNotEquals("var", Variables.numberValue(123)).count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().variableValueGreaterThan("var", Variables.numberValue(123)).count());
    assertEquals(5, runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("var", Variables.numberValue(123)).count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueLessThan("var", Variables.numberValue(123)).count());
    assertEquals(4, runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("var", Variables.numberValue(123)).count());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/cmmn/oneProcessTaskCase.cmmn"})
  public void testQueryBySuperCaseInstanceId() {
    String superCaseInstanceId = caseService.createCaseInstanceByKey("oneProcessTaskCase").getId();

    ProcessInstanceQuery query = runtimeService
        .createProcessInstanceQuery()
        .superCaseInstanceId(superCaseInstanceId);

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());

    ProcessInstance subProcessInstance = query.singleResult();
    assertNotNull(subProcessInstance);
  }

  @Test
  public void testQueryByInvalidSuperCaseInstanceId() {
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();

    assertNull(query.superProcessInstanceId("invalid").singleResult());
    assertEquals(0, query.superProcessInstanceId("invalid").list().size());

    try {
      query.superCaseInstanceId(null);
      fail();
    } catch (NullValueException e) {
      // expected
    }
  }

  @Test
  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/runtime/superProcessWithCaseCallActivity.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn" })
  public void testQueryBySubCaseInstanceId() {
    String superProcessInstanceId = runtimeService.startProcessInstanceByKey("subProcessQueryTest").getId();

    String subCaseInstanceId = caseService
        .createCaseInstanceQuery()
        .superProcessInstanceId(superProcessInstanceId)
        .singleResult()
        .getId();

    ProcessInstanceQuery query = runtimeService
        .createProcessInstanceQuery()
        .subCaseInstanceId(subCaseInstanceId);

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());

    ProcessInstance superProcessInstance = query.singleResult();
    assertNotNull(superProcessInstance);
    assertEquals(superProcessInstanceId, superProcessInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/runtime/superProcessWithCaseCallActivityInsideSubProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn" })
  public void testQueryBySubCaseInstanceIdNested() {
    String superProcessInstanceId = runtimeService.startProcessInstanceByKey("subProcessQueryTest").getId();

    String subCaseInstanceId = caseService
        .createCaseInstanceQuery()
        .superProcessInstanceId(superProcessInstanceId)
        .singleResult()
        .getId();

    ProcessInstanceQuery query = runtimeService
        .createProcessInstanceQuery()
        .subCaseInstanceId(subCaseInstanceId);

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());

    ProcessInstance superProcessInstance = query.singleResult();
    assertNotNull(superProcessInstance);
    assertEquals(superProcessInstanceId, superProcessInstance.getId());
  }

  @Test
  public void testQueryByInvalidSubCaseInstanceId() {
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();

    assertNull(query.subProcessInstanceId("invalid").singleResult());
    assertEquals(0, query.subProcessInstanceId("invalid").list().size());

    try {
      query.subCaseInstanceId(null);
      fail();
    } catch (NullValueException e) {
      // expected
    }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryNullValue() {
    // typed null
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.createVariables().putValueTyped("var", Variables.stringValue(null)));

    // untyped null
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.createVariables().putValueTyped("var", null));

    // non-null String value
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.createVariables().putValue("var", "a String Value"));

    ProcessInstance processInstance4 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.createVariables().putValue("var", "another String Value"));

    // (1) query for untyped null: should return typed and untyped null (notEquals: the opposite)
    List<ProcessInstance> instances =
        runtimeService.createProcessInstanceQuery().variableValueEquals("var", null).list();
    verifyResultContainsExactly(instances, asSet(processInstance1.getId(), processInstance2.getId()));
    instances = runtimeService.createProcessInstanceQuery().variableValueNotEquals("var", null).list();
    verifyResultContainsExactly(instances, asSet(processInstance3.getId(), processInstance4.getId()));

    // (2) query for typed null: should return typed null only (notEquals: the opposite)
    instances = runtimeService.createProcessInstanceQuery()
        .variableValueEquals("var", Variables.stringValue(null)).list();
    verifyResultContainsExactly(instances, asSet(processInstance1.getId()));
    instances = runtimeService.createProcessInstanceQuery()
        .variableValueNotEquals("var", Variables.stringValue(null)).list();
    verifyResultContainsExactly(instances, asSet(processInstance2.getId(), processInstance3.getId(), processInstance4.getId()));

    // (3) query for typed value: should return typed value only (notEquals: the opposite)
    instances = runtimeService.createProcessInstanceQuery()
        .variableValueEquals("var", "a String Value").list();
    verifyResultContainsExactly(instances, asSet(processInstance3.getId()));
    instances = runtimeService.createProcessInstanceQuery()
        .variableValueNotEquals("var", "a String Value").list();
    verifyResultContainsExactly(instances, asSet(processInstance1.getId(), processInstance2.getId(), processInstance4.getId()));
  }

  @Test
  public void testQueryByDeploymentId() {
    // given
    String firstDeploymentId = repositoryService
        .createDeploymentQuery()
        .singleResult()
        .getId();

    // make a second deployment and start an instance
    org.camunda.bpm.engine.repository.Deployment secondDeployment = repositoryService.createDeployment()
      .addClasspathResource("org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml")
      .deploy();

    ProcessInstance secondProcessInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    ProcessInstanceQuery query = runtimeService
        .createProcessInstanceQuery()
        .deploymentId(firstDeploymentId);

    // then the instance belonging to the second deployment is not returned
    assertEquals(5, query.count());

    List<ProcessInstance> instances = query.list();
    assertEquals(5, instances.size());

    for (ProcessInstance returnedInstance : instances) {
      assertTrue(!returnedInstance.getId().equals(secondProcessInstance.getId()));
    }

    // cleanup
    repositoryService.deleteDeployment(secondDeployment.getId(), true);

  }

  @Test
  public void testQueryByInvalidDeploymentId() {
    assertEquals(0, runtimeService.createProcessInstanceQuery().deploymentId("invalid").count());

    try {
      runtimeService.createProcessInstanceQuery().deploymentId(null).count();
      fail();
    } catch(ProcessEngineException e) {
      // expected
    }
  }

  @Test
  public void testQueryByNullActivityId() {
    try {
      runtimeService.createProcessInstanceQuery()
        .activityIdIn((String) null);
      fail("exception expected");
    }
    catch (NullValueException e) {
        assertThat(e.getMessage()).contains("activity ids contains null value");
    }
  }

  @Test
  public void testQueryByNullActivityIds() {
    try {
      runtimeService.createProcessInstanceQuery()
        .activityIdIn((String[]) null);
      fail("exception expected");
    }
    catch (NullValueException e) {
      assertThat(e.getMessage()).contains("activity ids is null");
    }
  }

  @Test
  public void testQueryByUnknownActivityId() {
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery()
      .activityIdIn("unknown");

    assertNoProcessInstancesReturned(query);
  }

  @Test
  public void testQueryByLeafActivityId() {
    // given
    ProcessDefinition oneTaskDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition gatewaySubProcessDefinition = testHelper.deployAndGetDefinition(FORK_JOIN_SUB_PROCESS_MODEL);

    // when
    ProcessInstance oneTaskInstance1 = runtimeService.startProcessInstanceById(oneTaskDefinition.getId());
    ProcessInstance oneTaskInstance2 = runtimeService.startProcessInstanceById(oneTaskDefinition.getId());
    ProcessInstance gatewaySubProcessInstance1 = runtimeService.startProcessInstanceById(gatewaySubProcessDefinition.getId());
    ProcessInstance gatewaySubProcessInstance2 = runtimeService.startProcessInstanceById(gatewaySubProcessDefinition.getId());

    Task task = engineRule.getTaskService().createTaskQuery()
      .processInstanceId(gatewaySubProcessInstance2.getId())
      .taskName("completeMe")
      .singleResult();
    engineRule.getTaskService().complete(task.getId());

    // then
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().activityIdIn("userTask");
    assertReturnedProcessInstances(query, oneTaskInstance1, oneTaskInstance2);

    query = runtimeService.createProcessInstanceQuery().activityIdIn("userTask1", "userTask2");
    assertReturnedProcessInstances(query, gatewaySubProcessInstance1, gatewaySubProcessInstance2);

    query = runtimeService.createProcessInstanceQuery().activityIdIn("userTask", "userTask1");
    assertReturnedProcessInstances(query, oneTaskInstance1, oneTaskInstance2, gatewaySubProcessInstance1);

    query = runtimeService.createProcessInstanceQuery().activityIdIn("userTask", "userTask1", "userTask2");
    assertReturnedProcessInstances(query, oneTaskInstance1, oneTaskInstance2, gatewaySubProcessInstance1, gatewaySubProcessInstance2);

    query = runtimeService.createProcessInstanceQuery().activityIdIn("join");
    assertReturnedProcessInstances(query, gatewaySubProcessInstance2);
  }

  @Test
  public void testQueryByNonLeafActivityId() {
    // given
    ProcessDefinition processDefinition = testHelper.deployAndGetDefinition(FORK_JOIN_SUB_PROCESS_MODEL);

    // when
    runtimeService.startProcessInstanceById(processDefinition.getId());

    // then
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().activityIdIn("subProcess", "fork");
    assertNoProcessInstancesReturned(query);
  }

  @Test
  public void testQueryByAsyncBeforeActivityId() {
    // given
    ProcessDefinition testProcess = testHelper.deployAndGetDefinition(ProcessModels.newModel()
      .startEvent("start").camundaAsyncBefore()
      .subProcess("subProcess").camundaAsyncBefore()
      .embeddedSubProcess()
        .startEvent()
        .serviceTask("task").camundaAsyncBefore().camundaExpression("${true}")
        .endEvent()
      .subProcessDone()
      .endEvent("end").camundaAsyncBefore()
      .done()
    );

    // when
    ProcessInstance instanceBeforeStart = runtimeService.startProcessInstanceById(testProcess.getId());
    ProcessInstance instanceBeforeSubProcess = runtimeService.startProcessInstanceById(testProcess.getId());
    executeJobForProcessInstance(instanceBeforeSubProcess);
    ProcessInstance instanceBeforeTask = runtimeService.startProcessInstanceById(testProcess.getId());
    executeJobForProcessInstance(instanceBeforeTask);
    executeJobForProcessInstance(instanceBeforeTask);
    ProcessInstance instanceBeforeEnd = runtimeService.startProcessInstanceById(testProcess.getId());
    executeJobForProcessInstance(instanceBeforeEnd);
    executeJobForProcessInstance(instanceBeforeEnd);
    executeJobForProcessInstance(instanceBeforeEnd);

    // then
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().activityIdIn("start");
    assertReturnedProcessInstances(query, instanceBeforeStart);

    query = runtimeService.createProcessInstanceQuery().activityIdIn("subProcess");
    assertReturnedProcessInstances(query, instanceBeforeSubProcess);

    query = runtimeService.createProcessInstanceQuery().activityIdIn("task");
    assertReturnedProcessInstances(query, instanceBeforeTask);

    query = runtimeService.createProcessInstanceQuery().activityIdIn("end");
    assertReturnedProcessInstances(query, instanceBeforeEnd);
  }

  @Test
  public void testQueryByAsyncAfterActivityId() {
    // given
    ProcessDefinition testProcess = testHelper.deployAndGetDefinition(ProcessModels.newModel()
      .startEvent("start").camundaAsyncAfter()
      .subProcess("subProcess").camundaAsyncAfter()
      .embeddedSubProcess()
        .startEvent()
        .serviceTask("task").camundaAsyncAfter().camundaExpression("${true}")
        .endEvent()
      .subProcessDone()
      .endEvent("end").camundaAsyncAfter()
      .done()
    );

    // when
    ProcessInstance instanceAfterStart = runtimeService.startProcessInstanceById(testProcess.getId());
    ProcessInstance instanceAfterTask = runtimeService.startProcessInstanceById(testProcess.getId());
    executeJobForProcessInstance(instanceAfterTask);
    ProcessInstance instanceAfterSubProcess = runtimeService.startProcessInstanceById(testProcess.getId());
    executeJobForProcessInstance(instanceAfterSubProcess);
    executeJobForProcessInstance(instanceAfterSubProcess);
    ProcessInstance instanceAfterEnd = runtimeService.startProcessInstanceById(testProcess.getId());
    executeJobForProcessInstance(instanceAfterEnd);
    executeJobForProcessInstance(instanceAfterEnd);
    executeJobForProcessInstance(instanceAfterEnd);

    // then
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().activityIdIn("start");
    assertReturnedProcessInstances(query, instanceAfterStart);

    query = runtimeService.createProcessInstanceQuery().activityIdIn("task");
    assertReturnedProcessInstances(query, instanceAfterTask);

    query = runtimeService.createProcessInstanceQuery().activityIdIn("subProcess");
    assertReturnedProcessInstances(query, instanceAfterSubProcess);

    query = runtimeService.createProcessInstanceQuery().activityIdIn("end");
    assertReturnedProcessInstances(query, instanceAfterEnd);
  }

  @Test
  public void testQueryByActivityIdBeforeCompensation() {
    // given
    ProcessDefinition testProcess = testHelper.deployAndGetDefinition(CompensationModels.COMPENSATION_ONE_TASK_SUBPROCESS_MODEL);

    // when
    runtimeService.startProcessInstanceById(testProcess.getId());
    testHelper.completeTask("userTask1");

    // then
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().activityIdIn("subProcess");
    assertNoProcessInstancesReturned(query);
  }

  @Test
  public void testQueryByActivityIdDuringCompensation() {
    // given
    ProcessDefinition testProcess = testHelper.deployAndGetDefinition(CompensationModels.COMPENSATION_ONE_TASK_SUBPROCESS_MODEL);

    // when
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(testProcess.getId());
    testHelper.completeTask("userTask1");
    testHelper.completeTask("userTask2");

    // then
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().activityIdIn("subProcess");
    assertReturnedProcessInstances(query, processInstance);

    query = runtimeService.createProcessInstanceQuery().activityIdIn("compensationEvent");
    assertReturnedProcessInstances(query, processInstance);

    query = runtimeService.createProcessInstanceQuery().activityIdIn("compensationHandler");
    assertReturnedProcessInstances(query, processInstance);
  }

  @Test
  public void testQueryByRootProcessInstances() {
    // given
    String superProcess = "calling";
    String subProcess = "called";
    BpmnModelInstance callingInstance = ProcessModels.newModel(superProcess)
      .startEvent()
      .callActivity()
      .calledElement(subProcess)
      .endEvent()
      .done();

    BpmnModelInstance calledInstance = ProcessModels.newModel(subProcess)
      .startEvent()
      .userTask()
      .endEvent()
      .done();

    testHelper.deploy(callingInstance, calledInstance);
    String businessKey = "theOne";
    String processInstanceId = runtimeService.startProcessInstanceByKey(superProcess, businessKey).getProcessInstanceId();

    // when
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery()
      .processInstanceBusinessKey(businessKey)
      .rootProcessInstances();

    // then
    assertEquals(1, query.count());
    List<ProcessInstance> list = query.list();
    assertEquals(1, list.size());
    assertEquals(processInstanceId, list.get(0).getId());
  }

  @Test
  public void testQueryByRootProcessInstancesAndSuperProcess() {
    // when
    try {
      runtimeService.createProcessInstanceQuery()
        .rootProcessInstances()
        .superProcessInstanceId("processInstanceId");

      fail("expected exception");
    } catch (ProcessEngineException e) {
      // then
      assertTrue(e.getMessage().contains("Invalid query usage: cannot set both rootProcessInstances and superProcessInstanceId"));
    }

    // when
    try {
      runtimeService.createProcessInstanceQuery()
        .superProcessInstanceId("processInstanceId")
        .rootProcessInstances();

      fail("expected exception");
    } catch (ProcessEngineException e) {
      // then
      assertTrue(e.getMessage().contains("Invalid query usage: cannot set both rootProcessInstances and superProcessInstanceId"));
    }
  }

  protected void executeJobForProcessInstance(ProcessInstance processInstance) {
    Job job = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
    managementService.executeJob(job.getId());
  }

  @SuppressWarnings("unchecked")
  protected <T> Set<T> asSet(T... elements) {
    return new HashSet<>(Arrays.asList(elements));
  }

  protected void assertNoProcessInstancesReturned(ProcessInstanceQuery query) {
    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
  }

  protected void assertReturnedProcessInstances(ProcessInstanceQuery query, ProcessInstance... processInstances) {
    int expectedSize = processInstances.length;
    assertEquals(expectedSize, query.count());
    assertEquals(expectedSize, query.list().size());

    verifyResultContainsExactly(query.list(), collectProcessInstanceIds(Arrays.asList(processInstances)));
  }

  protected void verifyResultContainsExactly(List<ProcessInstance> instances, Set<String> processInstanceIds) {
    Set<String> retrievedInstanceIds = collectProcessInstanceIds(instances);
    assertEquals(processInstanceIds, retrievedInstanceIds);
  }

  protected Set<String> collectProcessInstanceIds(List<ProcessInstance> instances) {
    Set<String> retrievedInstanceIds = new HashSet<>();
    for (ProcessInstance instance : instances) {
      retrievedInstanceIds.add(instance.getId());
    }
    return retrievedInstanceIds;
  }

}
