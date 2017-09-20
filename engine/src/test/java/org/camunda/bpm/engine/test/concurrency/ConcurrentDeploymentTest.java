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
package org.camunda.bpm.engine.test.concurrency;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.camunda.bpm.engine.impl.cmd.DeployCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.repository.DeploymentBuilderImpl;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.DeploymentQuery;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.test.util.DatabaseHelper;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

/**
 * <p>Tests the deployment from two threads simultaneously.</p>
 *
 * <p><b>Note:</b> the tests are not execute on H2 because it doesn't support the
 * exclusive lock on the deployment table.</p>
 *
 * @author Daniel Meyer
 */
public class ConcurrentDeploymentTest extends ConcurrencyTestCase {

  private static String processResource;

  static {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess().startEvent().done();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Bpmn.writeModelToStream(outputStream, modelInstance);
    processResource = new String(outputStream.toByteArray());
  }

  /**
   * hook into test method invocation - after the process engine is initialized
   */
  @Override
  protected void runTest() throws Throwable {
    String databaseType = DatabaseHelper.getDatabaseType(processEngineConfiguration);

    if("h2".equals(databaseType)) {
      // skip test method - if database is H2
    } else {
      // invoke the test method
      super.runTest();
    }
  }

  /**
   * @see https://app.camunda.com/jira/browse/CAM-2128
   */
  public void testDuplicateFiltering() throws InterruptedException {

    deployOnTwoConcurrentThreads(
        createDeploymentBuilder().enableDuplicateFiltering(false),
        createDeploymentBuilder().enableDuplicateFiltering(false));

    // ensure that although both transactions were run concurrently, only one deployment was constructed.
    DeploymentQuery deploymentQuery = repositoryService.createDeploymentQuery();
    assertThat(deploymentQuery.count(), is(1L));
  }

  public void testVersioning() throws InterruptedException {

    deployOnTwoConcurrentThreads(
        createDeploymentBuilder(),
        createDeploymentBuilder()
        );

    // ensure that although both transactions were run concurrently, the process definitions have different versions
    List<ProcessDefinition> processDefinitions = repositoryService
        .createProcessDefinitionQuery()
        .orderByProcessDefinitionVersion()
        .asc()
        .list();

    assertThat(processDefinitions.size(), is(2));
    assertThat(processDefinitions.get(0).getVersion(), is(1));
    assertThat(processDefinitions.get(1).getVersion(), is(2));
  }

  protected DeploymentBuilder createDeploymentBuilder() {
    return new DeploymentBuilderImpl(null)
        .name("some-deployment-name")
        .addString("foo.bpmn", processResource);
  }

  protected void deployOnTwoConcurrentThreads(DeploymentBuilder deploymentOne, DeploymentBuilder deploymentTwo) throws InterruptedException {
    assertThat("you can not use the same deployment builder for both deployments", deploymentOne, is(not(deploymentTwo)));

    // STEP 1: bring two threads to a point where they have
    // 1) started a new transaction
    // 2) are ready to deploy
    ThreadControl thread1 = executeControllableCommand(new ControllableDeployCommand(deploymentOne));
    thread1.waitForSync();

    ThreadControl thread2 = executeControllableCommand(new ControllableDeployCommand(deploymentTwo));
    thread2.waitForSync();

    // STEP 2: make Thread 1 proceed and wait until it has deployed but not yet committed
    // -> will still hold the exclusive lock
    thread1.makeContinue();
    thread1.waitForSync();

    // STEP 3: make Thread 2 continue
    // -> it will attempt to acquire the exclusive lock and block on the lock
    thread2.makeContinue();

    // wait for 2 seconds (Thread 2 is blocked on the lock)
    Thread.sleep(2000);

    // STEP 4: allow Thread 1 to terminate
    // -> Thread 1 will commit and release the lock
    thread1.waitUntilDone();

    // STEP 5: wait for Thread 2 to terminate
    thread2.waitForSync();
    thread2.waitUntilDone();
  }

  @Override
  protected void tearDown() throws Exception {

    for(Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

  protected static class ControllableDeployCommand extends ControllableCommand<Void> {

    private final DeploymentBuilder deploymentBuilder;

    public ControllableDeployCommand(DeploymentBuilder deploymentBuilder) {
      this.deploymentBuilder = deploymentBuilder;
    }

    public Void execute(CommandContext commandContext) {
      monitor.sync();  // thread will block here until makeContinue() is called form main thread

      new DeployCmd((DeploymentBuilderImpl) deploymentBuilder).execute(commandContext);

      monitor.sync();  // thread will block here until waitUntilDone() is called form main thread

      return null;
    }

  }

}
