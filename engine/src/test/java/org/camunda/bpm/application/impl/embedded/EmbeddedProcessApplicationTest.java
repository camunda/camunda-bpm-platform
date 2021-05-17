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
package org.camunda.bpm.application.impl.embedded;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Set;

import ch.qos.logback.classic.Level;
import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessApplicationDeployment;
import org.camunda.bpm.engine.repository.Resource;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.commons.testing.ProcessEngineLoggingRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Daniel Meyer
 *
 */
public class EmbeddedProcessApplicationTest extends PluggableProcessEngineTest {

  protected static final String CONFIG_LOGGER = "org.camunda.bpm.application";
  @Rule
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule()
                                                    .watch(CONFIG_LOGGER)
                                                    .level(Level.WARN);

  protected RuntimeContainerDelegate runtimeContainerDelegate = RuntimeContainerDelegate.INSTANCE.get();
  protected boolean defaultEngineRegistered;

  public void registerProcessEngine() {
    runtimeContainerDelegate.registerProcessEngine(processEngine);
    defaultEngineRegistered = true;
  }

  @Before
  public void setUp() throws Exception {
    defaultEngineRegistered = false;
  }

  @After
  public void tearDown() {
    if (defaultEngineRegistered) {
      runtimeContainerDelegate.unregisterProcessEngine(processEngine);
    }
  }

  @Test
  public void testDeployAppWithoutEngine() {

    TestApplicationWithoutEngine processApplication = new TestApplicationWithoutEngine();
    processApplication.deploy();

    processApplication.undeploy();

  }

  @Test
  public void testDeployAppWithoutProcesses() {

    registerProcessEngine();

    TestApplicationWithoutProcesses processApplication = new TestApplicationWithoutProcesses();
    processApplication.deploy();

    ProcessEngine processEngine = BpmPlatform.getProcessEngineService().getDefaultProcessEngine();
    long deployments = processEngine.getRepositoryService().createDeploymentQuery().count();
    assertEquals(0, deployments);

    processApplication.undeploy();

  }

  @Test
  public void testDeployAppWithCustomEngine() {

    TestApplicationWithCustomEngine processApplication = new TestApplicationWithCustomEngine();
    processApplication.deploy();

    ProcessEngine processEngine = BpmPlatform.getProcessEngineService().getProcessEngine("embeddedEngine");
    assertNotNull(processEngine);
    assertEquals("embeddedEngine", processEngine.getName());

    ProcessEngineConfiguration configuration = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration();

    // assert engine properties specified
    assertTrue(configuration.isJobExecutorDeploymentAware());
    assertTrue(configuration.isJobExecutorPreferTimerJobs());
    assertTrue(configuration.isJobExecutorAcquireByDueDate());
    assertEquals(5, configuration.getJdbcMaxActiveConnections());

    processApplication.undeploy();

  }

  @Test
  public void testDeployAppWithoutDmn() {
    // given
    TestApplicationWithoutDmn processApplication = new TestApplicationWithoutDmn();
    processApplication.deploy();

    ProcessEngine processEngine = BpmPlatform.getProcessEngineService().getProcessEngine("embeddedEngine");
    assertNotNull(processEngine);
    assertEquals("embeddedEngine", processEngine.getName());

    ProcessEngineConfigurationImpl configuration = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration();

    // assert engine properties specified
    assertTrue(configuration.isJobExecutorDeploymentAware());
    assertTrue(configuration.isJobExecutorPreferTimerJobs());
    assertTrue(configuration.isJobExecutorAcquireByDueDate());
    assertEquals(5, configuration.getJdbcMaxActiveConnections());
    assertFalse(configuration.isDmnEnabled());

    // when
    processApplication.undeploy();

    // then
    assertThat(loggingRule
        .getFilteredLog("ENGINE-07018 Unregistering process application for deployment but could " +
                        "not remove process definitions from deployment cache."))
        .hasSize(0);
  }

  @Test
  public void testDeployAppWithCustomDefaultEngine() {

    // Test if it's possible to set a custom default engine name.
    // This might happen when the "default" ProcessEngine is not available,
    // but a ProcessApplication doesn't define a ProcessEngine to deploy to.
    String processApplicationName = "test-app";
    String customEngineName = "customDefaultEngine";
    TestApplicationWithCustomDefaultEngine processApplication = new TestApplicationWithCustomDefaultEngine();

    processApplication.deploy();

    String deployedToProcessEngineName = runtimeContainerDelegate.getProcessApplicationService()
      .getProcessApplicationInfo(processApplicationName)
      .getDeploymentInfo()
      .get(0)
      .getProcessEngineName();

    assertEquals(customEngineName, processApplication.getDefaultDeployToEngineName());
    assertEquals(customEngineName, deployedToProcessEngineName);

    processApplication.undeploy();
  }

  @Test
  public void testDeployAppReusingExistingEngine() {

    registerProcessEngine();

    TestApplicationReusingExistingEngine processApplication = new TestApplicationReusingExistingEngine();
    processApplication.deploy();

    assertEquals(1, repositoryService.createDeploymentQuery().count());

    processApplication.undeploy();

    assertEquals(0, repositoryService.createDeploymentQuery().count());

  }

  @Test
  public void testDeployAppWithAdditionalResourceSuffixes() {
    registerProcessEngine();

    TestApplicationWithAdditionalResourceSuffixes processApplication = new TestApplicationWithAdditionalResourceSuffixes();
    processApplication.deploy();


    Deployment deployment = repositoryService.createDeploymentQuery().singleResult();

    assertNotNull(deployment);

    List<Resource> deploymentResources = repositoryService.getDeploymentResources(deployment.getId());
    assertEquals(4, deploymentResources.size());

    processApplication.undeploy();
    assertEquals(0, repositoryService.createDeploymentQuery().count());
  }

  @Test
  public void testDeployAppWithResources() {
    registerProcessEngine();

    TestApplicationWithResources processApplication = new TestApplicationWithResources();
    processApplication.deploy();

    Deployment deployment = repositoryService.createDeploymentQuery().singleResult();

    assertNotNull(deployment);

    List<Resource> deploymentResources = repositoryService.getDeploymentResources(deployment.getId());
    assertEquals(4, deploymentResources.size());

    processApplication.undeploy();
    assertEquals(0, repositoryService.createDeploymentQuery().count());
  }

  @Test
  public void testDeploymentSourceProperty() {
    registerProcessEngine();

    TestApplicationWithResources processApplication = new TestApplicationWithResources();
    processApplication.deploy();

    Deployment deployment = repositoryService.createDeploymentQuery().singleResult();

    assertNotNull(deployment);
    assertEquals(ProcessApplicationDeployment.PROCESS_APPLICATION_DEPLOYMENT_SOURCE, deployment.getSource());

    processApplication.undeploy();
  }

  @Test
  public void testDeployProcessApplicationWithNameAttribute() {
    TestApplicationWithCustomName pa = new TestApplicationWithCustomName();

    pa.deploy();

    Set<String> deployedPAs = runtimeContainerDelegate.getProcessApplicationService().getProcessApplicationNames();
    assertEquals(1, deployedPAs.size());
    assertTrue(deployedPAs.contains(TestApplicationWithCustomName.NAME));

    pa.undeploy();
  }

  @Test
  public void testDeployWithTenantIds() {
    registerProcessEngine();

    TestApplicationWithTenantId processApplication = new TestApplicationWithTenantId();
    processApplication.deploy();

    List<Deployment> deployments = repositoryService
        .createDeploymentQuery()
        .orderByTenantId()
        .asc()
        .list();

    assertEquals(2, deployments.size());
    assertEquals("tenant1", deployments.get(0).getTenantId());
    assertEquals("tenant2", deployments.get(1).getTenantId());

    processApplication.undeploy();
  }

  @Test
  public void testDeployWithoutTenantId() {
    registerProcessEngine();

    TestApplicationWithResources processApplication = new TestApplicationWithResources();
    processApplication.deploy();

    Deployment deployment = repositoryService.createDeploymentQuery().singleResult();

    assertNotNull(deployment);
    assertNull(deployment.getTenantId());

    processApplication.undeploy();
  }

}
