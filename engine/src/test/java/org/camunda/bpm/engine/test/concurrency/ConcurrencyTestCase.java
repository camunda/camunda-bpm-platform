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

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;

/**
 * @author Daniel Meyer
 *
 */
public abstract class ConcurrencyTestCase extends PluggableProcessEngineTestCase {

  protected List<ControllableCommand<?>> controllableCommands;

  @Override
  protected void setUp() throws Exception {
    controllableCommands = new ArrayList<ControllableCommand<?>>();
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {

    // wait for all spawned threads to end
    for (ControllableCommand<?> controllableCommand : controllableCommands) {
      ThreadControl threadControl = controllableCommand.monitor;
      threadControl.executingThread.interrupt();
      threadControl.executingThread.join();
    }

    // clear the test thread's interruption state
    Thread.interrupted();

    super.tearDown();
  }

  protected ThreadControl executeControllableCommand(final ControllableCommand<?> command) {

    final Thread controlThread = Thread.currentThread();

    Thread thread = new Thread(new Runnable() {
      public void run() {
        try {
          processEngineConfiguration.getCommandExecutorTxRequiresNew().execute(command);
        } catch(RuntimeException e) {
          command.monitor.setException(e);
          controlThread.interrupt();
          throw e;
        }
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
        if(exception != null) {
          if (reportFailure) {
            return;
          }
          else {
            fail();
          }
        }
        try {
          if(!syncAvailable) {
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
        if(exception != null) {
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
