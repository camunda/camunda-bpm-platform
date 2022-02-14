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
import java.util.stream.Collectors;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.util.CollectionUtil;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.qa.largedata.util.EngineDataGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class DeleteDeploymentCascadeTest {

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule("camunda.cfg.xml");

  protected static final String DATA_PREFIX = DeleteDeploymentCascadeTest.class.getSimpleName();

  protected int GENERATE_PROCESS_INSTANCES_COUNT = 2500;
  protected RepositoryService repositoryService;
  protected HistoryService historyService;
  protected EngineDataGenerator generator;
  
  @Before
  public void init() {
    repositoryService = processEngineRule.getProcessEngine().getRepositoryService();
    historyService = processEngineRule.getProcessEngine().getHistoryService();

    // generate data
    generator = new EngineDataGenerator(processEngineRule.getProcessEngine(), GENERATE_PROCESS_INSTANCES_COUNT, DATA_PREFIX);
    generator.deployDefinitions();
    generator.generateCompletedProcessInstanceData();
  }

  @After
  public void teardown() {
    Deployment deployment = repositoryService.createDeploymentQuery().deploymentName(generator.getDeploymentName()).singleResult();
    if (deployment != null) {
      List<HistoricProcessInstance> processInstances = historyService.createHistoricProcessInstanceQuery()
          .processDefinitionKey(generator.getAutoCompleteProcessKey()).list();
      if (!processInstances.isEmpty()) {
        List<String> processInstanceIds = processInstances.stream().map(HistoricProcessInstance::getId).collect(Collectors.toList());
        List<List<String>> partitions = CollectionUtil.partition(processInstanceIds, DbSqlSessionFactory.MAXIMUM_NUMBER_PARAMS);
        for (List<String> partition : partitions) {
          historyService.deleteHistoricProcessInstances(partition);
        }
      }
      repositoryService.deleteDeployment(deployment.getId(), false);
    }
  }

  @Test
  public void shouldDeleteCascadeWithLargeParameterCount() {
    // given
    Deployment deployment = repositoryService.createDeploymentQuery().deploymentName(generator.getDeploymentName()).singleResult();

    // when
    repositoryService.deleteDeployment(deployment.getId(), true);

    // then
    deployment = repositoryService.createDeploymentQuery().deploymentName(generator.getDeploymentName()).singleResult();
    assertThat(deployment).isNull();
    List<HistoricProcessInstance> instances = historyService.createHistoricProcessInstanceQuery()
        .processDefinitionKey(generator.getAutoCompleteProcessKey()).list();
    assertThat(instances).isEmpty();
  }
}
