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

import java.util.logging.Logger;

import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.impl.cmd.CompleteTaskCmd;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Roman Smirnov
 *
 */
public class CompetingForkTest extends PluggableProcessEngineTestCase {

  private static Logger log = Logger.getLogger(CompetingForkTest.class.getName());

  Thread testThread = Thread.currentThread();
  static ControllableThread activeThread;
  static String jobId;

  public class CompleteTaskThread extends ControllableThread {

    String taskId;
    OptimisticLockingException exception;

    public CompleteTaskThread(String taskId) {
      this.taskId = taskId;
    }
    public synchronized void startAndWaitUntilControlIsReturned() {
      activeThread = this;
      super.startAndWaitUntilControlIsReturned();
    }

    public void run() {
      try {
        processEngineConfiguration
          .getCommandExecutorTxRequired()
          .execute(new ControlledCommand(activeThread, new CompleteTaskCmd(taskId, null)));

      } catch (OptimisticLockingException e) {
        this.exception = e;
      }
      log.fine(getName()+" ends");
    }
  }

  @Deployment
  public void FAILING_testCompetingFork() throws Exception {
    runtimeService.startProcessInstanceByKey("process");

    TaskQuery query = taskService.createTaskQuery();

    String task1 = query
        .taskDefinitionKey("task1")
        .singleResult()
        .getId();

    String task2 = query
        .taskDefinitionKey("task2")
        .singleResult()
        .getId();

    String task3 = query
        .taskDefinitionKey("task3")
        .singleResult()
        .getId();

    log.fine("test thread starts thread one");
    CompleteTaskThread threadOne = new CompleteTaskThread(task1);
    threadOne.startAndWaitUntilControlIsReturned();

    log.fine("test thread thread two");
    CompleteTaskThread threadTwo = new CompleteTaskThread(task2);
    threadTwo.startAndWaitUntilControlIsReturned();

    log.fine("test thread continues to start thread three");
    CompleteTaskThread threadThree = new CompleteTaskThread(task3);
    threadThree.startAndWaitUntilControlIsReturned();

    log.fine("test thread notifies thread 1");
    threadOne.proceedAndWaitTillDone();
    assertNull(threadOne.exception);

    log.fine("test thread notifies thread 2");
    threadTwo.proceedAndWaitTillDone();
    assertNull(threadTwo.exception);

    log.fine("test thread notifies thread 3");
    threadThree.proceedAndWaitTillDone();
    assertNotNull(threadThree.exception);
    assertTextPresent("was updated by another transaction concurrently", threadThree.exception.getMessage());
  }
}
