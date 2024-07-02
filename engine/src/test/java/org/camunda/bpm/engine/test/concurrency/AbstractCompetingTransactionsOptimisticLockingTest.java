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

import java.util.List;
import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.CompleteTaskCmd;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.errorcode.BuiltinExceptionCode;
import org.camunda.bpm.engine.impl.test.RequiredDatabase;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;

/**
 * Test cases for ensuring that an INSERTs/UPDATEs on entities with referenced
 * entities that have already been removed in a concurrent transaction lead to
 * {@link OptimisticLockingException}s.
 */
public abstract class AbstractCompetingTransactionsOptimisticLockingTest {

  private static Logger LOG = ProcessEngineLogger.TEST_LOGGER.getLogger();

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected RuntimeService runtimeService;
  protected TaskService taskService;

  protected static ControllableThread activeThread;

  protected abstract ProcessEngineTestRule getTestRule();

  @After
  public void resetConfiguration() {
    processEngineConfiguration.setEnableOptimisticLockingOnForeignKeyViolation(true);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/concurrency/AbstractCompetingTransactionsOptimisticLockingTest.shouldDetectConcurrentDeletionOfExecutionForTaskInsert.bpmn20.xml")
  @Test
  public void shouldDetectConcurrentDeletionOfExecutionForTaskInsert() {
    // given
    runtimeService.startProcessInstanceByKey("competingTransactionsProcess");
    List<Task> tasks = taskService.createTaskQuery().list();

    Task firstTask = "task1-1".equals(tasks.get(0).getTaskDefinitionKey()) ? tasks.get(0) : tasks.get(1);
    Task secondTask = "task2-1".equals(tasks.get(0).getTaskDefinitionKey()) ? tasks.get(0) : tasks.get(1);

    CompleteTaskThread thread1 = new CompleteTaskThread(firstTask);
    thread1.startAndWaitUntilControlIsReturned();
    CompleteTaskThread thread2 = new CompleteTaskThread(secondTask);
    thread2.startAndWaitUntilControlIsReturned();

    thread2.proceedAndWaitTillDone();

    // assume
    assertThat(thread2.exception).isNull();

    // when
    thread1.proceedAndWaitTillDone();

    // then
    assertThat(thread1.exception).isNotNull();
    assertThat(thread1.exception).isInstanceOf(OptimisticLockingException.class);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/concurrency/AbstractCompetingTransactionsOptimisticLockingTest.shouldDetectConcurrentDeletionOfExecutionForTaskInsert.bpmn20.xml")
  @Test
  @RequiredDatabase(includes = DbSqlSessionFactory.POSTGRES)
  public void shouldTreatConcurrentDeletionOfExecutionForTaskInsertAsForeignKeyException() {
    // given
    processEngineConfiguration.setEnableOptimisticLockingOnForeignKeyViolation(false);

    runtimeService.startProcessInstanceByKey("competingTransactionsProcess");
    List<Task> tasks = taskService.createTaskQuery().list();

    Task firstTask = "task1-1".equals(tasks.get(0).getTaskDefinitionKey()) ? tasks.get(0) : tasks.get(1);
    Task secondTask = "task2-1".equals(tasks.get(0).getTaskDefinitionKey()) ? tasks.get(0) : tasks.get(1);

    CompleteTaskThread thread1 = new CompleteTaskThread(firstTask);
    thread1.startAndWaitUntilControlIsReturned();
    CompleteTaskThread thread2 = new CompleteTaskThread(secondTask);
    thread2.startAndWaitUntilControlIsReturned();

    thread2.proceedAndWaitTillDone();

    // assume
    assertThat(thread2.exception).isNull();

    // when
    thread1.proceedAndWaitTillDone();

    // then
    assertThat(thread1.exception)
      .isInstanceOf(ProcessEngineException.class)
      .extracting("code")
      .contains(BuiltinExceptionCode.FOREIGN_KEY_CONSTRAINT_VIOLATION.getCode());
  }

  public class CompleteTaskThread extends ControllableThread {
    Task task;
    ProcessEngineException exception;

    public CompleteTaskThread(Task task) {
      this.task = task;
    }

    @Override
    public synchronized void startAndWaitUntilControlIsReturned() {
      activeThread = this;
      super.startAndWaitUntilControlIsReturned();
    }

    @Override
    public void run() {
      try {
        processEngineConfiguration.getCommandExecutorTxRequired().execute(
            new ControlledCommand<VariableMap>(activeThread, new CompleteTaskCmd(task.getId(), null)));
      } catch (ProcessEngineException e) {
        this.exception = e;
      }
      LOG.debug(getName() + " ends.");
    }
  }
}
