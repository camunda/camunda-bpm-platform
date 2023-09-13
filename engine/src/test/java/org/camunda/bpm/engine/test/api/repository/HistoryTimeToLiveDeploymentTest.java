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

import ch.qos.logback.classic.Level;
import org.camunda.bpm.dmn.engine.impl.transform.DmnTransformException;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ParseException;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.commons.testing.ProcessEngineLoggingRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class HistoryTimeToLiveDeploymentTest {

  protected static final String CONFIG_LOGGER = "org.camunda.bpm.engine.cfg";

  protected static final String EXPECTED_DEFAULT_CONFIG_MSG =
      "You are using the default TTL (Time To Live) of 180 days (six months); "
      + "the history clean-up feature will delete your data after six months. "
      + "We recommend adjusting the TTL configuration property aligned with your specific requirements.";

  protected static final String DEFAULT_HTTL_CONFIG_VALUE = "180";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Rule
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule()
      .watch(CONFIG_LOGGER)
      .level(Level.DEBUG);

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected RepositoryService repositoryService;
  protected ManagementService managementService;
  protected ProcessEngine processEngine;

  protected String historyTimeToLive;

  @Before
  public void setUp() throws Exception {
    processEngine = engineRule.getProcessEngine();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    repositoryService = engineRule.getRepositoryService();
    managementService = engineRule.getManagementService();

    historyTimeToLive = processEngineConfiguration.getHistoryTimeToLive();
    processEngineConfiguration.setEnforceHistoryTimeToLive(true);
  }

  @After
  public void tearDown() throws Exception {
    processEngineConfiguration.setHistoryTimeToLive(historyTimeToLive);
    processEngineConfiguration.setEnforceHistoryTimeToLive(false);
    ClockUtil.reset();
  }

  @Test
  public void processWithoutHTTLShouldFail() {
    assertThatThrownBy(() -> {
      // when
      testRule.deploy(repositoryService.createDeployment()
          .addClasspathResource("org/camunda/bpm/engine/test/api/repository/version1.bpmn20.xml"));})

        // then
        .isInstanceOf(ParseException.class)
        .hasMessageContaining("History Time To Live cannot be null");
  }

  @Test
  public void processWithHTTLShouldSucceed() {
    // when
    testRule.deploy(repositoryService
        .createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/version3.bpmn20.xml"));

    Deployment deployment = repositoryService.createDeploymentQuery().singleResult();

    // then
    assertThat(deployment).isNotNull();
  }

  @Test
  public void caseWithHTTLShouldSucceed() {
    // when
    testRule.deploy(repositoryService
        .createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/cmmn/oneTaskCaseWithHistoryTimeToLive.cmmn"));

    Deployment deployment = repositoryService.createDeploymentQuery().singleResult();

    // then
    assertThat(deployment).isNotNull();
  }

  @Test
  public void caseWithoutHTTLShouldFail() {
    assertThatThrownBy(() -> {
      // when
      testRule.deploy(repositoryService.createDeployment()
          .addClasspathResource("org/camunda/bpm/engine/test/api/cmmn/oneTaskCase2.cmmn"));})

        // then
        .isInstanceOf(ProcessEngineException.class)
        .hasCauseInstanceOf(NotValidException.class)
        .hasStackTraceContaining("History Time To Live cannot be null");
  }

  @Test
  public void decisionWithHTTLShouldSucceed() {
    // when
    testRule.deploy(repositoryService
        .createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/dmn/Example.dmn"));

    Deployment deployment = repositoryService.createDeploymentQuery().singleResult();

    // then
    assertThat(deployment).isNotNull();
  }

  @Test
  public void decisionWithoutHTTLShouldFail() {
    assertThatThrownBy(() -> {
      // when
      testRule.deploy(repositoryService
          .createDeployment()
          .addClasspathResource("org/camunda/bpm/engine/test/api/dmn/Another_Example.dmn"));})

        // then
        .isInstanceOf(ProcessEngineException.class)
        .hasCauseInstanceOf(DmnTransformException.class)
        .hasStackTraceContaining("History Time To Live cannot be null");
  }

  @Test
  public void shouldDeploySuccessfullyDueToProcessEngineConfigFallback() {
    // given
    processEngineConfiguration.setHistoryTimeToLive("5");

    // when
    testRule.deploy(repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/version1.bpmn20.xml"));

    Deployment deployment = repositoryService.createDeploymentQuery().singleResult();

    // then
    assertThat(deployment).isNotNull();
  }

  @Test
  public void shouldNotLogMessageOnDefaultConfigOriginatingFromConfig() {
    // given
    processEngineConfiguration.setHistoryTimeToLive(DEFAULT_HTTL_CONFIG_VALUE);

    // when
    testRule.deploy(repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/version1.bpmn20.xml"));

    // then
    assertThat(loggingRule.getFilteredLog(EXPECTED_DEFAULT_CONFIG_MSG)).hasSize(0);
  }

  @Test
  public void shouldLogMessageOnDefaultValueOfModel() {
    // given
    String nonDefaultValue = "179";
    processEngineConfiguration.setHistoryTimeToLive(nonDefaultValue);

    // when
    testRule.deploy(repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/version3.bpmn20.xml"));

    // then
    assertThat(loggingRule.getFilteredLog(EXPECTED_DEFAULT_CONFIG_MSG)).hasSize(1);
  }

  @Test
  public void shouldNotLogAnyMessageOnNonDefaultConfig() {
    String nonDefaultValue = "179";

    // given
    processEngineConfiguration.setHistoryTimeToLive(nonDefaultValue);

    // when
    testRule.deploy(repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/version1.bpmn20.xml"));

    // then
    assertThat(loggingRule.getFilteredLog(EXPECTED_DEFAULT_CONFIG_MSG)).hasSize(0);
  }

}
