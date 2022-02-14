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

import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.impl.cmd.LockExternalTaskCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExternalTaskEntity;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;

/**
 * This test covers the use-case where two competing transactions
 * attempt to lock (by ID) the same external task. The test steps are:
 *
 * 0. External Task is created.
 * 1. TX1 validates that there is no lock on the task, and waits for sync;
 * 2. TX2 validates that there is no lock on the task, and waits for sync;
 * 3. TX1 updates the lock, and waits to flush (waits for sync).
 * 4. TX2 updates the lock, and waits to flush (waits for sync).
 * 5. TX1 flushes the result and commits.
 * 6. TX2 attempts to flush the result and receives
 *    an OLE since the lock was already updated by TX1.
 */
public class CompetingExternalTaskLockingTest extends ConcurrencyTestCase {

  protected static final String WORKER_ID_1 = "WORKER_1";
  protected static final String WORKER_ID_2 = "WORKER_2";
  protected static final long LOCK_DURATION = 10000L;

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml"})
  @Test
  public void shouldThrowOleOnConcurrentLockingAttempt() throws InterruptedException {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    String externalTaskId = externalTaskService.createExternalTaskQuery()
        .notLocked()
        .singleResult()
        .getId();

    // TX1: start, READ external task, and verify that there is no lock
    ThreadControl lockThread1 = executeControllableCommand(
        new ControllableExternalTaskLockCmd(externalTaskId, WORKER_ID_1, LOCK_DURATION));
    lockThread1.reportInterrupts();
    lockThread1.waitForSync();

    // TX2: start, READ external task, and verify that there is no lock
    ThreadControl lockThread2 = executeControllableCommand(
        new ControllableExternalTaskLockCmd(externalTaskId, WORKER_ID_2, LOCK_DURATION));
    lockThread2.reportInterrupts();
    lockThread2.waitForSync();

    // TX1: lock external task and wait to flush
    lockThread1.makeContinue();
    lockThread1.waitForSync();

    // TX2: lock external task and wait to flush
    lockThread2.makeContinue();

    // introduce a delay to avoid race conditions between the threads
    Thread.sleep(2000);

    // TX1: flush & commit
    lockThread1.waitUntilDone();

    // when
    // TX2: attempt to flush
    lockThread2.waitForSync();
    lockThread2.waitUntilDone();

    // then
    ExternalTask lockedExternalTask = externalTaskService.createExternalTaskQuery().locked().singleResult();
    assertThat(lockThread1.getException()).isNull();
    assertThat(lockThread2.getException()).isNotNull();
    assertThat(lockThread2.getException()).isInstanceOf(OptimisticLockingException.class);
    assertThat(lockedExternalTask.getWorkerId()).isEqualToIgnoringCase(WORKER_ID_1);
  }

  private static class ControllableExternalTaskLockCmd extends ControllableCommand<Void> {

    protected LockExternalTaskCmd lockExternalTaskCmd;

    public ControllableExternalTaskLockCmd(String externalTaskId, String workerId, long lockDuration) {
      this.lockExternalTaskCmd = new ControllableLockExternalTaskCmd(externalTaskId, workerId, lockDuration, monitor);
    }

    @Override
    public Void execute(CommandContext commandContext) {

      lockExternalTaskCmd.execute(commandContext);

      // second sync
      // block thread after task lock is set
      monitor.sync();

      return null;
    }
  }

  private static class ControllableLockExternalTaskCmd extends LockExternalTaskCmd {

    protected final ThreadControl monitor;

    public ControllableLockExternalTaskCmd(String externalTaskId, String workerId, long lockDuration, ThreadControl threadControl) {
      super(externalTaskId, workerId, lockDuration);
      this.monitor = threadControl;
    }

    @Override
    protected boolean validateWorkerViolation(ExternalTaskEntity externalTask) {
      boolean result = super.validateWorkerViolation(externalTask);

      // first sync
      // block thread after the task is validated
      monitor.sync();

      return result;
    }
  }
}