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

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;

/**
 * @author Daniel Meyer
 *
 */
public class EmbeddedProcessApplicationTest extends PluggableProcessEngineTestCase {

  public void testDeployAppWithoutEngine() {

    TestApplicationWithoutEngine processApplication = new TestApplicationWithoutEngine();
    processApplication.deploy();

    processApplication.undeploy();

  }

  public void testDeployAppWithoutProcesses() {

    // register existing process engine with BPM platform
    RuntimeContainerDelegate runtimeContainerDelegate = RuntimeContainerDelegate.INSTANCE.get();
    runtimeContainerDelegate.registerProcessEngine(processEngine);
    try {

      TestApplicationWithoutProcesses processApplication = new TestApplicationWithoutProcesses();
      processApplication.deploy();

      ProcessEngine processEngine = BpmPlatform.getProcessEngineService().getDefaultProcessEngine();
      long deployments = processEngine.getRepositoryService().createDeploymentQuery().count();
      assertEquals(0, deployments);

      processApplication.undeploy();

    } finally {
      // unregister process engine
      runtimeContainerDelegate.unregisterProcessEngine(processEngine);
    }

  }

  public void testDeployAppWithCustomEngine() {

    TestApplicationWithCustomEngine processApplication = new TestApplicationWithCustomEngine();
    processApplication.deploy();

    ProcessEngine processEngine = BpmPlatform.getProcessEngineService().getProcessEngine("embeddedEngine");
    assertNotNull(processEngine);
    assertEquals("embeddedEngine", processEngine.getName());

    ProcessEngineConfiguration configuration = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration();

    // assert engine properties specified
    assertEquals(true, configuration.isJobExecutorDeploymentAware());
    assertEquals(5, configuration.getJdbcMaxActiveConnections());

    processApplication.undeploy();

  }

  public void testDeployAppReusingExistingEngine() {

    // register existing process engine with BPM platform
    RuntimeContainerDelegate runtimeContainerDelegate = RuntimeContainerDelegate.INSTANCE.get();
    runtimeContainerDelegate.registerProcessEngine(processEngine);

    TestApplicationReusingExistingEngine processApplication = new TestApplicationReusingExistingEngine();
    processApplication.deploy();

    assertEquals(1, repositoryService.createDeploymentQuery().count());

    processApplication.undeploy();

    assertEquals(0, repositoryService.createDeploymentQuery().count());

    // unregister process engine
    runtimeContainerDelegate.unregisterProcessEngine(processEngine);

  }



}
