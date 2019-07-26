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
package org.camunda.bpm.engine.test.api.variables;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.history.HistoricCaseInstance;
import org.camunda.bpm.engine.impl.HistoricCaseInstanceQueryImpl;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.junit.After;
import org.junit.Before;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
public class HistoricCaseInstanceQueryVariableIgnoreCaseTest extends AbstractVariableIgnoreCaseTest<HistoricCaseInstanceQueryImpl, HistoricCaseInstance> {

  RepositoryService repositoryService;

  @Before
  public void init() {
    repositoryService = engineRule.getRepositoryService();

    repositoryService.createDeployment().addClasspathResource("org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn").deploy();
    engineRule.getCaseService().withCaseDefinitionByKey("oneTaskCase").setVariables(VARIABLES).businessKey("oneTaskCase").create();
    instance = engineRule.getHistoryService().createHistoricCaseInstanceQuery().singleResult();
  }

  @After
  public void tearDown() {
    for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

  @Override
  protected HistoricCaseInstanceQueryImpl createQuery() {
    return (HistoricCaseInstanceQueryImpl) engineRule.getHistoryService().createHistoricCaseInstanceQuery();
  }

  @Override
  protected void assertThatTwoInstancesAreEqual(HistoricCaseInstance one, HistoricCaseInstance two) {
    assertThat(one.getId()).isEqualTo(two.getId());
  }

}
