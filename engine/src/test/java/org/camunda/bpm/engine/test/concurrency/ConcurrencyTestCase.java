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

import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;

/**
 * @author Daniel Meyer
 *
 */
public abstract class ConcurrencyTestCase extends PluggableProcessEngineTestCase {

  protected ThreadControl executeControllableCommand(final ControllableCommand<?> command) {

    final Thread controlThread = Thread.currentThread();

    Thread thread = new Thread(new Runnable() {
      public void run() {
        try {
          processEngineConfiguration.getCommandExecutorTxRequired().execute(command);
        } catch(RuntimeException e) {
          controlThread.interrupt();
          throw e;
        }
      }
    });

    command.monitor.executingThread = thread;

    thread.start();

    return command.monitor;
  }


  static abstract class ControllableCommand<T> implements Command<T> {

    protected final ThreadControl monitor;

    public ControllableCommand() {
      this.monitor = new ThreadControl();
    }

  }

  static class ThreadControl {

    protected boolean syncAvailable = false;

    protected Thread executingThread;

    public void waitForSync() {
      synchronized (this) {
        if(Thread.interrupted()) {
          fail();
        }
        if(!syncAvailable) {
          try {
            wait();
          } catch (InterruptedException e) {
            fail();
          }
        }
        syncAvailable = false;
      }
    }

    public void waitUntilDone() {
      makeContinue();
      try {
        executingThread.join();
      } catch (InterruptedException e) {
        fail();
      }
    }

    public void sync() {
      synchronized (this) {
        syncAvailable = true;
        try {
          notifyAll();
          wait();
        } catch (InterruptedException e) {
          fail();
        }
      }
    }

    public void makeContinue() {
      synchronized (this) {
        if(Thread.interrupted()) {
          fail();
        }
        notifyAll();
      }
    }

  }

}
