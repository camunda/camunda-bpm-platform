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
package org.camunda.bpm.qa.largedata;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.test.TestHelper;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.qa.largedata.util.EngineDataGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class DeleteDeploymentCascadeTest {

  @ClassRule
  public static ProcessEngineRule processEngineRule = new ProcessEngineRule("camunda.cfg.xml");

  protected int GENERATE_PROCESS_INSTANCES_COUNT = 2500;
  protected RepositoryService repositoryService;
  protected HistoryService historyService;

  @Before
  public void init() {
    repositoryService = processEngineRule.getRepositoryService();
    historyService = processEngineRule.getHistoryService();

    // generate data
    EngineDataGenerator generator = new EngineDataGenerator(processEngineRule.getProcessEngine(), GENERATE_PROCESS_INSTANCES_COUNT);
    generator.deployDefinitions();
    generator.generateCompletedProcessInstanceData();
  }

  @After
  public void tearDown() {
    TestHelper.assertAndEnsureCleanDbAndCache(processEngineRule.getProcessEngine(), false);
  }

  @Test
  public void shouldDeleteCascadeWithLargeParameterCount() {
    // given
    Deployment deployment = repositoryService.createDeploymentQuery().deploymentName(EngineDataGenerator.DEPLOYMENT_NAME).singleResult();

    // when
    repositoryService.deleteDeployment(deployment.getId(), true);

    // then
    deployment = repositoryService.createDeploymentQuery().deploymentName(EngineDataGenerator.DEPLOYMENT_NAME).singleResult();
    assertThat(deployment).isNull();
    List<HistoricProcessInstance> instances = historyService.createHistoricProcessInstanceQuery()
        .processDefinitionKey(EngineDataGenerator.AUTO_COMPLETE_PROCESS_KEY).list();
    assertThat(instances).isEmpty();
  }
}
