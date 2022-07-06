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
package org.camunda.bpm.engine.test.concurrency;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.camunda.bpm.engine.CrdbTransactionRetryException;
import org.camunda.bpm.engine.impl.cmd.DeployCmd;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.repository.DeploymentBuilderImpl;
import org.camunda.bpm.engine.impl.test.RequiredDatabase;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.DeploymentQuery;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Test;

/**
 * <p>Tests the deployment from two threads simultaneously.</p>
 *
 * <p><b>Note:</b> the tests are not execute on H2 because it doesn't support the
 * exclusive lock on the deployment table.</p>
 *
 * @author Daniel Meyer
 */
@RequiredDatabase(excludes = DbSqlSessionFactory.H2)
public class ConcurrentDeploymentTest extends ConcurrencyTestCase {

  private static String processResource;

  static {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess().startEvent().done();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Bpmn.writeModelToStream(outputStream, modelInstance);
    processResource = new String(outputStream.toByteArray());
  }

  protected ThreadControl thread1;
  protected ThreadControl thread2;

  /**
   * @see <a href="https://app.camunda.com/jira/browse/CAM-2128">https://app.camunda.com/jira/browse/CAM-2128</a>
   */
  @Test
  public void testDuplicateFiltering() throws InterruptedException {

    deployOnTwoConcurrentThreads(
        createDeploymentBuilder().enableDuplicateFiltering(false),
        createDeploymentBuilder().enableDuplicateFiltering(false));

    // ensure that although both transactions were run concurrently, only one deployment was constructed.
    assertThat(thread1.getException()).isNull();
    DeploymentQuery deploymentQuery = repositoryService.createDeploymentQuery();
    assertThat(deploymentQuery.count()).isEqualTo(1L);

    if (!testRule.isOptimisticLockingExceptionSuppressible()) {
      // on CockroachDB, the deployment pessimistic lock is disabled
      // and concurrent deployments rely on the CRDB optimistic locking mechanism.
      // By default, the `commandRetries` property is set to 0, so retryable commands
      // will still re-throw the `CrdbTransactionRetryException` to the caller and fail.
      assertCockroachDBConcurrentFailure();
    }
  }

  @Test
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

    if (testRule.isOptimisticLockingExceptionSuppressible()) {
      assertThat(processDefinitions.size()).isEqualTo(2);
      assertThat(processDefinitions.get(0).getVersion()).isEqualTo(1);
      assertThat(processDefinitions.get(1).getVersion()).isEqualTo(2);
    } else {
      assertThat(thread1.getException()).isNull();
      assertThat(processDefinitions.size()).isEqualTo(1);
      assertThat(processDefinitions.get(0).getVersion()).isEqualTo(1);
      // on CockroachDB, the deployment pessimistic lock is disabled
      // and concurrent deployments rely on the CRDB optimistic locking mechanism.
      // By default, the `commandRetries` property is set to 0, so retryable commands
      // will still re-throw the `CrdbTransactionRetryException` to the caller and fail.
      assertCockroachDBConcurrentFailure();
    }
  }

  protected DeploymentBuilder createDeploymentBuilder() {
    return new DeploymentBuilderImpl(null)
        .name("some-deployment-name")
        .addString("foo.bpmn", processResource);
  }

  protected void deployOnTwoConcurrentThreads(DeploymentBuilder deploymentOne, DeploymentBuilder deploymentTwo) throws InterruptedException {
    assertThat(deploymentOne)
        .as("you can not use the same deployment builder for both deployments")
        .isNotEqualTo(deploymentTwo);

    // STEP 1: bring two threads to a point where they have
    // 1) started a new transaction
    // 2) are ready to deploy
    thread1 = executeControllableCommand(new ControllableDeployCommand(deploymentOne));
    thread1.reportInterrupts();
    thread1.waitForSync();

    thread2 = executeControllableCommand(new ControllableDeployCommand(deploymentTwo));
    thread2.reportInterrupts();
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

  protected void assertCockroachDBConcurrentFailure() {
    assertThat(thread2.getException()).isInstanceOf(CrdbTransactionRetryException.class);
  }

  @After
  public void tearDown() throws Exception {

    for(Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }

    processEngineConfiguration.getDeploymentCache().purgeCache();
  }

  protected static class ControllableDeployCommand extends ControllableCommand<Void> {

    private final DeploymentBuilder deploymentBuilder;

    public ControllableDeployCommand(DeploymentBuilder deploymentBuilder) {
      this.deploymentBuilder = deploymentBuilder;
    }

    public Void execute(CommandContext commandContext) {
      monitor.sync();  // thread will block here until makeContinue() is called from main thread

      new DeployCmd((DeploymentBuilderImpl) deploymentBuilder).execute(commandContext);

      monitor.sync();  // thread will block here until waitUntilDone() is called form main thread

      return null;
    }

  }

}
