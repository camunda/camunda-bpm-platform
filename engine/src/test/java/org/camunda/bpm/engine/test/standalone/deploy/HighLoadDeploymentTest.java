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
package org.camunda.bpm.engine.test.standalone.deploy;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
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

public class HighLoadDeploymentTest {

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule();

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testHelper);

  protected RuntimeService runtimeService;
  protected RepositoryService repositoryService;
  protected HistoryService historyService;

  @Before
  public void setUp() {
    runtimeService = engineRule.getRuntimeService();
    repositoryService = engineRule.getRepositoryService();
    historyService = engineRule.getHistoryService();
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void shouldDeleteDeploymentWithCascade() {
    // given
    BpmnModelInstance instance = Bpmn.createExecutableProcess("process")
        .startEvent()
        .userTask("testTask")
        .endEvent()
        .done();

    DeploymentWithDefinitions deployment = repositoryService
        .createDeployment()
        .addModelInstance("foo.bpmn", instance)
        .deployWithResult();

    for (int i = 0; i < 2200; i++) {
      runtimeService.startProcessInstanceByKey("process");
    }

    // when
    assertThat(engineRule.getHistoryService().createHistoricProcessInstanceQuery().count(), is(2200L));
    repositoryService.deleteDeployment(deployment.getId(), true);

    // then
    long hpiCount = historyService.createHistoricProcessInstanceQuery().count();
    long count = repositoryService.createDeploymentQuery().count();
    assertThat(hpiCount, is(0L));
    assertThat(count, is(0L));
  }
}