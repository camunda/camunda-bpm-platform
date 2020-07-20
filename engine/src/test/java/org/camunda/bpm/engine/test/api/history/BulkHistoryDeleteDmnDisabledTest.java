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
package org.camunda.bpm.engine.test.api.history;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Svetlana Dorokhova
 *
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class BulkHistoryDeleteDmnDisabledTest {

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(configuration ->
    configuration.setDmnEnabled(false));

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  private RuntimeService runtimeService;
  private HistoryService historyService;

  @Before
  public void createProcessEngine() {
    runtimeService = engineRule.getRuntimeService();
    historyService = engineRule.getHistoryService();

  }

  @Test
  public void bulkHistoryDeleteWithDisabledDmn() {
    BpmnModelInstance model = Bpmn.createExecutableProcess("someProcess")
        .startEvent()
          .userTask("userTask")
        .endEvent()
        .done();
    testRule.deploy(model);
    List<String> ids = prepareHistoricProcesses("someProcess");
    runtimeService.deleteProcessInstances(ids, null, true, true);

    //when
    historyService.deleteHistoricProcessInstancesBulk(ids);

    //then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey("someProcess").count());
  }

  private List<String> prepareHistoricProcesses(String businessKey) {
    List<String> processInstanceIds = new ArrayList<>();

    for (int i = 0; i < 5; i++) {
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(businessKey);
      processInstanceIds.add(processInstance.getId());
    }

    return processInstanceIds;
  }

}
