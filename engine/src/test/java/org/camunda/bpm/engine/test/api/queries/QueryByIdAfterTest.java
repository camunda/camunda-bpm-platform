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

  private HistoryService historyService;
  private RuntimeService runtimeService;

  @Before
  public void init() {
    this.historyService = engineRule.getProcessEngine().getHistoryService();
    this.runtimeService = engineRule.getRuntimeService();
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
