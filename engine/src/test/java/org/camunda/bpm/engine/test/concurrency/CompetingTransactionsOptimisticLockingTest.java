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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.camunda.bpm.engine.CrdbTransactionRetryException;
import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.CompleteTaskCmd;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.test.RequiredDatabase;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.slf4j.Logger;

/**
 * @author Nikola Koevski
 */
public class CompetingTransactionsOptimisticLockingTest {

  private static Logger LOG = ProcessEngineLogger.TEST_LOGGER.getLogger();

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected RuntimeService runtimeService;
  protected TaskService taskService;

  protected static ControllableThread activeThread;

  @Before
  public void initializeServices() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    runtimeService = engineRule.getRuntimeService();
    taskService = engineRule.getTaskService();
  }

  public class TransactionThread extends ControllableThread {
    String taskId;
    ProcessEngineException exception;

    public TransactionThread(String taskId) {
      this.taskId = taskId;
    }

    @Override
    public synchronized void startAndWaitUntilControlIsReturned() {
      activeThread = this;
      super.startAndWaitUntilControlIsReturned();
    }

    @Override
    public void run() {
      try {
        processEngineConfiguration
          .getCommandExecutorTxRequired()
          .execute(new ControlledCommand(activeThread, new CompleteTaskCmd(taskId, null)));

      } catch (ProcessEngineException e) {
        this.exception = e;
      }
      LOG.debug(getName() + " ends.");
    }
  }

  @Deployment
  @Test
  @RequiredDatabase(excludes = DbSqlSessionFactory.POSTGRES)
  public void testCompetingTransactionsOptimisticLocking() {
    // given
    runtimeService.startProcessInstanceByKey("competingTransactionsProcess");
    List<Task> tasks = taskService.createTaskQuery().list();

    assertEquals(2, tasks.size());

    Task firstTask = "task1-1".equals(tasks.get(0).getTaskDefinitionKey()) ? tasks.get(0) : tasks.get(1);
    Task secondTask = "task2-1".equals(tasks.get(0).getTaskDefinitionKey()) ? tasks.get(0) : tasks.get(1);

    TransactionThread thread1 = new TransactionThread(firstTask.getId());
    thread1.startAndWaitUntilControlIsReturned();
    TransactionThread thread2 = new TransactionThread(secondTask.getId());
    thread2.startAndWaitUntilControlIsReturned();

    thread2.proceedAndWaitTillDone();
    assertNull(thread2.exception);

    thread1.proceedAndWaitTillDone();
    assertNotNull(thread1.exception);

    if (testRule.isOptimisticLockingExceptionSuppressible()) {
      assertEquals(OptimisticLockingException.class, thread1.exception.getClass());
    } else {
      // on CRDB, the transaction needs to be rolled back and retried,
      // so a CrdbTransactionRetryException is thrown.
      assertEquals(CrdbTransactionRetryException.class, thread1.exception.getClass());
    }
  }
}
