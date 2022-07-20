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

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.junit.After;
import org.junit.Before;

public abstract class ConcurrencyTestHelper {

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected List<ControllableCommand<?>> controllableCommands;

  @Before
  public void init() {
    controllableCommands = new ArrayList<>();
  }

  @After
  public void cleanUp() throws Exception {

    // wait for all spawned threads to end
    for (ControllableCommand<?> controllableCommand : controllableCommands) {
      ThreadControl threadControl = controllableCommand.monitor;
      threadControl.executingThread.interrupt();
      threadControl.executingThread.join();
    }

    // clear the test thread's interruption state
    Thread.interrupted();
  }

  protected ThreadControl executeControllableCommand(final ControllableCommand<?> command) {

    final Thread controlThread = Thread.currentThread();

    Thread thread = new Thread(() -> {
      try {
        processEngineConfiguration.getCommandExecutorTxRequiresNew().execute(command);
      } catch (RuntimeException e) {
        command.monitor.setException(e);
        controlThread.interrupt();
        throw e;
      }
    });

    controllableCommands.add(command);
    command.monitor.executingThread = thread;

    thread.start();

    return command.monitor;
  }


  public static abstract class ControllableCommand<T> implements Command<T> {

    protected final ThreadControl monitor;

    public ControllableCommand() {
      this.monitor = new ThreadControl();
    }

    public ControllableCommand(ThreadControl threadControl) {
      this.monitor = threadControl;
    }

    public ThreadControl getMonitor() {
      return monitor;
    }
  }

  public static class ThreadControl {

    protected volatile boolean syncAvailable = false;

    protected Thread executingThread;

    protected volatile boolean reportFailure;
    protected volatile Exception exception;

    protected boolean ignoreSync = false;

    public ThreadControl() {
    }

    public ThreadControl(Thread executingThread) {
      this.executingThread = executingThread;
    }

    public void waitForSync() {
      waitForSync(Long.MAX_VALUE);
    }

    public void waitForSync(long timeout) {
      synchronized (this) {
        if (exception != null) {
          if (reportFailure) {
            return;
          } else {
            fail();
          }
        }
        try {
          if (!syncAvailable) {
            try {
              wait(timeout);
            } catch (InterruptedException e) {
              if (!reportFailure || exception == null) {
                fail("unexpected interruption");
              }
            }
          }
        } finally {
          syncAvailable = false;
        }
      }
    }

    public void waitUntilDone() {
      waitUntilDone(false);
    }

    public void waitUntilDone(boolean ignoreUpcomingSyncs) {
      ignoreSync = ignoreUpcomingSyncs;
      makeContinue();
      join();
    }

    public void join() {
      try {
        executingThread.join();
      } catch (InterruptedException e) {
        if (!reportFailure || exception == null) {
          fail("Unexpected interruption");
        }
      } finally {
        // clear our interruption state; the controlled thread may have interrupted us
        // in case the controlled command failed (see ConcurrencyTestCase#executeControllableCommand).
        //
        // If the controlled thread finished before we entered the #join method, #join returns
        // immediately and does not clear our interruption status. If we do not clear the
        // interruption status here, any subsequent call of interrupt-sensitive
        // methods may fail (e.g. monitors, IO operations)
        Thread.interrupted();
      }
    }

    public void sync() {
      synchronized (this) {
        if (ignoreSync) {
          return;
        }

        syncAvailable = true;
        try {
          notifyAll();
          wait();
        } catch (InterruptedException e) {
          if (!reportFailure || exception == null) {
            fail("Unexpected interruption");
          }
        }
      }
    }

    public void makeContinue() {
      synchronized (this) {
        if (exception != null) {
          fail();
        }
        notifyAll();
      }
    }

    public void makeContinueAndWaitForSync() {
      makeContinue();
      waitForSync();
    }

    public void reportInterrupts() {
      this.reportFailure = true;
    }

    public void ignoreFutureSyncs() {
      this.ignoreSync = true;
    }

    public synchronized void setException(Exception e) {
      this.exception = e;
    }

    public Throwable getException() {
      return exception;
    }
  }


}
