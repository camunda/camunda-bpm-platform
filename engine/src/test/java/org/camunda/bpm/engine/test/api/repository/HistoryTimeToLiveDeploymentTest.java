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

import java.util.Set;
import org.camunda.bpm.application.impl.EmbeddedProcessApplication;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ParseException;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.application.ProcessApplicationManager;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.persistence.deploy.cache.DeploymentCache;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.DeploymentHandlerFactory;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.bpmn.deployment.VersionedDeploymentHandlerFactory;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class HistoryTimeToLiveDeploymentTest {

  @ClassRule
  public static ProcessEngineBootstrapRule DEFAULT_CONFIG_RULE = new ProcessEngineBootstrapRule(configuration -> {
    // given
    configuration.setEnforceHistoryTimeToLive(true);
  });

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule(DEFAULT_CONFIG_RULE);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected RepositoryService repositoryService;
  protected ManagementService managementService;
  protected ProcessEngine processEngine;

  private EmbeddedProcessApplication processApplication;
  protected DeploymentHandlerFactory defaultDeploymentHandlerFactory;
  protected DeploymentHandlerFactory customDeploymentHandlerFactory;

  protected ProcessApplicationManager processApplicationManager;
  protected DeploymentCache deploymentCache;
  Set<String> registeredDeployments;

  @Before
  public void setUp() throws Exception {
    processEngine = engineRule.getProcessEngine();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    repositoryService = engineRule.getRepositoryService();
    managementService = engineRule.getManagementService();

    defaultDeploymentHandlerFactory = processEngineConfiguration.getDeploymentHandlerFactory();
    customDeploymentHandlerFactory = new VersionedDeploymentHandlerFactory();
    processApplication = new EmbeddedProcessApplication();

    processApplicationManager = processEngineConfiguration.getProcessApplicationManager();
    deploymentCache = processEngineConfiguration.getDeploymentCache();
    registeredDeployments = processEngineConfiguration.getRegisteredDeployments();
  }

  @After
  public void tearDown() throws Exception {
    clearProcessApplicationDeployments();
    processApplication.undeploy();
    processEngineConfiguration.setDeploymentHandlerFactory(defaultDeploymentHandlerFactory);
    ClockUtil.reset();
  }

  // then
  @Test(expected = ParseException.class)
  public void createDeploymentShouldFail() {
    // when
    testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/version1.bpmn20.xml"));
  }

  /*
   * Clears the deployment caches to simulate a stop of the process engine.
   */
  protected void clearProcessApplicationDeployments() {
    processApplicationManager.clearRegistrations();
    registeredDeployments.clear();
    deploymentCache.discardProcessDefinitionCache();
  }

}
