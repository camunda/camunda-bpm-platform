/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.application.impl.embedded;

import java.util.List;
import java.util.Set;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessApplicationDeployment;
import org.camunda.bpm.engine.repository.Resource;

/**
 * @author Daniel Meyer
 *
 */
public class EmbeddedProcessApplicationTest extends PluggableProcessEngineTestCase {

  protected RuntimeContainerDelegate runtimeContainerDelegate = RuntimeContainerDelegate.INSTANCE.get();
  protected boolean defaultEngineRegistered;

  public void registerProcessEngine() {
    runtimeContainerDelegate.registerProcessEngine(processEngine);
    defaultEngineRegistered = true;
  }

  @Override
  protected void setUp() throws Exception {
    defaultEngineRegistered = false;
  }

  @Override
  public void tearDown() {
    if (defaultEngineRegistered) {
      runtimeContainerDelegate.unregisterProcessEngine(processEngine);
    }
  }

  public void testDeployAppWithoutEngine() {

    TestApplicationWithoutEngine processApplication = new TestApplicationWithoutEngine();
    processApplication.deploy();

    processApplication.undeploy();

  }

  public void testDeployAppWithoutProcesses() {

    registerProcessEngine();

    TestApplicationWithoutProcesses processApplication = new TestApplicationWithoutProcesses();
    processApplication.deploy();

    ProcessEngine processEngine = BpmPlatform.getProcessEngineService().getDefaultProcessEngine();
    long deployments = processEngine.getRepositoryService().createDeploymentQuery().count();
    assertEquals(0, deployments);

    processApplication.undeploy();

  }

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

  public void testDeployAppReusingExistingEngine() {

    registerProcessEngine();

    TestApplicationReusingExistingEngine processApplication = new TestApplicationReusingExistingEngine();
    processApplication.deploy();

    assertEquals(1, repositoryService.createDeploymentQuery().count());

    processApplication.undeploy();

    assertEquals(0, repositoryService.createDeploymentQuery().count());

  }

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

  public void testDeploymentSourceProperty() {
    registerProcessEngine();

    TestApplicationWithResources processApplication = new TestApplicationWithResources();
    processApplication.deploy();

    Deployment deployment = repositoryService.createDeploymentQuery().singleResult();

    assertNotNull(deployment);
    assertEquals(ProcessApplicationDeployment.PROCESS_APPLICATION_DEPLOYMENT_SOURCE, deployment.getSource());

    processApplication.undeploy();
  }

  public void testDeployProcessApplicationWithNameAttribute() {
    TestApplicationWithCustomName pa = new TestApplicationWithCustomName();

    pa.deploy();

    Set<String> deployedPAs = runtimeContainerDelegate.getProcessApplicationService().getProcessApplicationNames();
    assertEquals(1, deployedPAs.size());
    assertTrue(deployedPAs.contains(TestApplicationWithCustomName.NAME));

    pa.undeploy();
  }

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
