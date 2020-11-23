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
package org.camunda.bpm.engine.test.api.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.query.Query;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.RuleChain;

public abstract class AbstractDefinitionQueryTest {

  protected final static String FIRST_DEPLOYMENT_NAME = "firstDeployment";
  protected static final String SECOND_DEPLOYMENT_NAME = "secondDeployment";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);
  @Rule
  public RuleChain chain = RuleChain.outerRule(engineRule).around(testRule);

  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;

  protected String deploymentOneId;
  protected String deploymentTwoId;

  @Before
  public void before() throws Exception {
    repositoryService = engineRule.getRepositoryService();
    runtimeService = engineRule.getRuntimeService();

    deploymentOneId = repositoryService
      .createDeployment()
      .name(FIRST_DEPLOYMENT_NAME)
      .addClasspathResource(getResourceOnePath())
      .addClasspathResource(getResourceTwoPath())
      .deploy()
      .getId();

    deploymentTwoId = repositoryService
      .createDeployment()
      .name(SECOND_DEPLOYMENT_NAME)
      .addClasspathResource(getResourceOnePath())
      .deploy()
      .getId();
  }

  protected abstract String getResourceOnePath();

  protected abstract String getResourceTwoPath();

  @After
  public void after() throws Exception {
    repositoryService.deleteDeployment(deploymentOneId, true);
    repositoryService.deleteDeployment(deploymentTwoId, true);
  }

  protected void verifyQueryResults(Query query, int countExpected) {
    assertThat(query.list()).hasSize(countExpected);
    assertThat(query.count()).isEqualTo(new Long(countExpected));

    if (countExpected == 1) {
      assertThat(query.singleResult()).isNotNull();
    } else if (countExpected > 1){
      verifySingleResultFails(query);
    } else if (countExpected == 0) {
      assertThat(query.singleResult()).isNull();
    }
  }

  private void verifySingleResultFails(Query query) {

    // when/then
    assertThatThrownBy(() -> query.singleResult())
      .isInstanceOf(ProcessEngineException.class);
  }
}
