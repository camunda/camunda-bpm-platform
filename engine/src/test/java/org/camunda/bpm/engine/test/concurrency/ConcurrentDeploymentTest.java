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

import java.io.ByteArrayOutputStream;

import org.camunda.bpm.engine.impl.cmd.DeployCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.repository.DeploymentBuilderImpl;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.DeploymentQuery;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

/**
 *
 * @author Daniel Meyer
 *
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
   * Create deployment from two threads simultaneously -> make sure that
   * duplicate filtering works as expected.
   *  See: https://app.camunda.com/jira/browse/CAM-2128
   */
  public void testDuplicateFiltering() throws InterruptedException {

    // do not execute on H2
    if("h2".equals(processEngineConfiguration.getDbSqlSessionFactory().getDatabaseType())) {
      return;
    }

    // STEP 1: bring two threads to a point where they have
    // 1) started a new transaction
    // 2) are ready to deploy
    ThreadControl thread1 = executeControllableCommand(new ControllableDeployCommand());
    thread1.waitForSync();

    ThreadControl thread2 = executeControllableCommand(new ControllableDeployCommand());
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

    // ensure that although both transactions were run concurrently, only one deployment was constructed.
    DeploymentQuery deploymentQuery = repositoryService.createDeploymentQuery();
    assertEquals(1, deploymentQuery.count());

    // cleanup
    Deployment deployment = deploymentQuery.singleResult();
    repositoryService.deleteDeployment(deployment.getId(), true);
  }

  protected static class ControllableDeployCommand extends ControllableCommand<Void> {

    public Void execute(CommandContext commandContext) {

      DeploymentBuilder deploymentBuilder = new DeploymentBuilderImpl(null)
        .name("some-deployment-name")
        .enableDuplicateFiltering(false)
        .addString("foo.bpmn", processResource);

      monitor.sync();  // thread will block here until makeContinue() is called form main thread

      new DeployCmd<Deployment>((DeploymentBuilderImpl) deploymentBuilder).execute(commandContext);

      monitor.sync();  // thread will block here until waitUntilDone() is called form main thread

      return null;
    }

  }

}
