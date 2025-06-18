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
package org.camunda.bpm.engine.test.api.queries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.impl.HistoricActivityInstanceQueryImpl;
import org.camunda.bpm.engine.impl.HistoricIncidentQueryImpl;
import org.camunda.bpm.engine.impl.HistoricProcessInstanceQueryImpl;
import org.camunda.bpm.engine.impl.HistoricTaskInstanceQueryImpl;
import org.camunda.bpm.engine.impl.HistoricVariableInstanceQueryImpl;
import org.camunda.bpm.engine.impl.ProcessDefinitionQueryImpl;
import org.camunda.bpm.engine.impl.ProcessInstanceQueryImpl;
import org.camunda.bpm.engine.impl.persistence.StrongUuidGenerator;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class QueryByIdAfterTest {

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(config -> config.setIdGenerator(new StrongUuidGenerator()));

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  private RepositoryService repositoryService;
  private HistoryService historyService;
  private RuntimeService runtimeService;

  @Before
  public void init() {
    this.repositoryService = engineRule.getProcessEngine().getRepositoryService();
    this.historyService = engineRule.getProcessEngine().getHistoryService();
    this.runtimeService = engineRule.getRuntimeService();
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void shouldHistoricIncidentApiReturnOnlyAfterGivenId() {
    // given
    startProcessInstancesByKey("oneFailingServiceTaskProcess", 10);
    HistoricIncidentQueryImpl historicIncidentQuery = (HistoricIncidentQueryImpl) historyService.createHistoricIncidentQuery();

    // when querying by idAfter then only expected results are returned
    List<HistoricIncident> historicIncidents = historicIncidentQuery.orderByIncidentId().asc().list();
    String firstId = historicIncidents.get(0).getId();
    String middleId = historicIncidents.get(4).getId();
    String lastId = historicIncidents.get(historicIncidents.size() - 1).getId();
    assertEquals(10, historicIncidents.size());
    assertEquals(9, historicIncidentQuery.idAfter(firstId).list().size());
    assertEquals(0, historicIncidentQuery.idAfter(lastId).list().size());

    List<HistoricIncident> secondHalf = historicIncidentQuery.idAfter(middleId).list();
    assertEquals(5, secondHalf.size());
    assertTrue(secondHalf.stream().allMatch(incident -> isIdGreaterThan(incident.getId(), middleId)));
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml" })
  public void shouldHistoricProcessInstanceApiReturnOnlyAfterGivenId() {
    // given
    startProcessInstancesByKey("oneTaskProcess", 10);

    // when querying by idAfter then only expected results are returned
    HistoricProcessInstanceQueryImpl historicProcessInstanceQuery = (HistoricProcessInstanceQueryImpl) historyService.createHistoricProcessInstanceQuery();
    List<HistoricProcessInstance> historicProcessInstances = historicProcessInstanceQuery.orderByProcessInstanceId().asc().list();
    String firstId = historicProcessInstances.get(0).getId();
    String middleId = historicProcessInstances.get(4).getId();
    String lastId = historicProcessInstances.get(historicProcessInstances.size() - 1).getId();
    assertEquals(10, historicProcessInstances.size());
    assertEquals(9, historicProcessInstanceQuery.idAfter(firstId).list().size());
    assertEquals(0, historicProcessInstanceQuery.idAfter(lastId).list().size());

    List<HistoricProcessInstance> secondHalf = historicProcessInstanceQuery.idAfter(middleId).list();
    assertEquals(5, secondHalf.size());
    assertTrue(secondHalf.stream().allMatch(processInstance -> isIdGreaterThan(processInstance.getId(), middleId)));
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml" })
  public void shouldHistoricTaskInstanceApiReturnOnlyAfterGivenId() {
    // given
    startProcessInstancesByKey("oneTaskProcess", 10);

    // when querying by idAfter then only expected results are returned
    HistoricTaskInstanceQueryImpl historicTaskInstanceQuery = (HistoricTaskInstanceQueryImpl) historyService.createHistoricTaskInstanceQuery();
    List<HistoricTaskInstance> historicTaskInstances = historicTaskInstanceQuery.orderByTaskId().asc().list();
    String firstId = historicTaskInstances.get(0).getId();
    String middleId = historicTaskInstances.get(4).getId();
    String lastId = historicTaskInstances.get(historicTaskInstances.size() - 1).getId();
    assertEquals(10, historicTaskInstances.size());
    assertEquals(9, historicTaskInstanceQuery.idAfter(firstId).list().size());
    assertEquals(0, historicTaskInstanceQuery.idAfter(lastId).list().size());

    List<HistoricTaskInstance> secondHalf = historicTaskInstanceQuery.idAfter(middleId).list();
    assertEquals(5, secondHalf.size());
    assertTrue(secondHalf.stream().allMatch(taskInstance -> isIdGreaterThan(taskInstance.getId(), middleId)));
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/history/HistoricActivityInstanceTest.testHistoricActivityInstanceNoop.bpmn20.xml")
  @Ignore // until comparison for ids (activityName:id) is handled
  public void shouldHistoricActivityInstanceApiReturnOnlyAfterGivenId() {
    // given
    startProcessInstancesByKey("noopProcess", 10);

    // when querying by idAfter then only expected results are returned
    HistoricActivityInstanceQueryImpl historicActivityInstanceQuery = (HistoricActivityInstanceQueryImpl) historyService.createHistoricActivityInstanceQuery();
    List<HistoricActivityInstance> historicActivityInstances = historicActivityInstanceQuery.orderByHistoricActivityInstanceId().asc().list();
    String firstId = historicActivityInstances.get(0).getId();
    String middleId = historicActivityInstances.get(14).getId();
    String lastId = historicActivityInstances.get(historicActivityInstances.size() - 1).getId();
    assertEquals(30, historicActivityInstances.size());
    assertEquals(29, historicActivityInstanceQuery.idAfter(firstId).list().size());
    assertEquals(0, historicActivityInstanceQuery.idAfter(lastId).list().size());

    List<HistoricActivityInstance> secondHalf = historicActivityInstanceQuery.idAfter(middleId).list();
    assertEquals(15, secondHalf.size());
    assertTrue(secondHalf.stream().allMatch(activityInstance -> isIdGreaterThan(activityInstance.getId(), middleId)));
  }

  @Test
  @Ignore // until comparison for ids (processDefinitionKey:version:id) is handled
  public void shouldProcessDefinitionApiReturnOnlyAfterGivenId() {
    // given
    deployProcessDefinitions("org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml", 10);

    // when querying by idAfter then only expected results are returned
    ProcessDefinitionQueryImpl processDefinitionQuery = (ProcessDefinitionQueryImpl) repositoryService.createProcessDefinitionQuery();
    List<ProcessDefinition> processDefinitions = processDefinitionQuery.processDefinitionKey("oneTaskProcess").orderByProcessDefinitionId().asc().list();
    String firstId = processDefinitions.get(0).getId();
    String middleId = processDefinitions.get(4).getId();
    String lastId = processDefinitions.get(processDefinitions.size() - 1).getId();
    assertEquals(10, processDefinitions.size());
    assertEquals(9, processDefinitionQuery.idAfter(firstId).list().size());
    assertEquals(0, processDefinitionQuery.idAfter(lastId).list().size());

    List<ProcessDefinition> secondHalf = processDefinitionQuery.idAfter(middleId).list();
    assertEquals(5, secondHalf.size());
    assertTrue(secondHalf.stream().allMatch(processDefinition -> isIdGreaterThan(processDefinition.getId(), middleId)));
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml" })
  public void shouldProcessInstanceApiReturnOnlyAfterGivenId() {
    // given
    startProcessInstancesByKey("oneTaskProcess", 10);

    // when querying by idAfter then only expected results are returned
    ProcessInstanceQueryImpl processInstanceQuery = (ProcessInstanceQueryImpl) runtimeService.createProcessInstanceQuery();
    List<ProcessInstance> processInstances = processInstanceQuery.orderByProcessInstanceId().asc().list();
    String firstId = processInstances.get(0).getProcessInstanceId();
    String middleId = processInstances.get(4).getProcessInstanceId();
    String lastId = processInstances.get(processInstances.size() - 1).getProcessInstanceId();
    assertEquals(10, processInstances.size());
    assertEquals(9, processInstanceQuery.idAfter(firstId).list().size());
    assertEquals(0, processInstanceQuery.idAfter(lastId).list().size());

    List<ProcessInstance> secondHalf = processInstanceQuery.idAfter(middleId).list();
    assertEquals(5, secondHalf.size());
    assertTrue(secondHalf.stream().allMatch(processInstance -> isIdGreaterThan(processInstance.getProcessInstanceId(), middleId)));
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/history/HistoricVariableInstanceTest.testSimple.bpmn20.xml" })
  public void shouldVariableInstanceApiReturnOnlyAfterGivenId() {
    // given
    startProcessInstancesByKey("myProc", 10);

    // when querying by idAfter then only expected results are returned
    HistoricVariableInstanceQueryImpl historicVariableInstanceQuery = (HistoricVariableInstanceQueryImpl) historyService.createHistoricVariableInstanceQuery();
    List<HistoricVariableInstance> historicVariableInstances = historicVariableInstanceQuery.orderByVariableId().asc().list();
    String firstId = historicVariableInstances.get(0).getId();
    String middleId = historicVariableInstances.get(9).getId();
    String lastId = historicVariableInstances.get(historicVariableInstances.size() - 1).getId();
    assertEquals(20, historicVariableInstances.size());
    assertEquals(19, historicVariableInstanceQuery.idAfter(firstId).list().size());
    assertEquals(0, historicVariableInstanceQuery.idAfter(lastId).list().size());

    List<HistoricVariableInstance> secondHalf = historicVariableInstanceQuery.idAfter(middleId).list();
    assertEquals(10, secondHalf.size());
    assertTrue(secondHalf.stream().allMatch(variable -> isIdGreaterThan(variable.getId(), middleId)));
  }

  private void deployProcessDefinitions(String resource, int numberOfDeployments) {
    for (int i = 0; i < numberOfDeployments; i++) {
      testRule.deploy(resource);
    }
  }

  private void startProcessInstancesByKey(String key, int numberOfInstances) {
    for (int i = 0; i < numberOfInstances; i++) {
      Map<String, Object> variables = Collections.singletonMap("message", "exception" + i);

      runtimeService.startProcessInstanceByKey(key, i + "", variables);
    }
    testRule.executeAvailableJobs();
  }

  /**
   * Compares two ids
   * @param id1
   * @param id2
   * @return true if id1 is greater than id2, false otherwise
   */
  private static boolean isIdGreaterThan(String id1, String id2) {
    return id1.compareTo(id2) > 0;
  }

}
