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

import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cmd.CompleteTaskCmd;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Ignore;
import org.slf4j.Logger;

/**
 * @author Roman Smirnov
 *
 */
@Ignore
public class CompetingForkTest extends PluggableProcessEngineTestCase {

private static Logger LOG = ProcessEngineLogger.TEST_LOGGER.getLogger();

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
      LOG.debug(getName()+" ends");
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

    LOG.debug("test thread starts thread one");
    CompleteTaskThread threadOne = new CompleteTaskThread(task1);
    threadOne.startAndWaitUntilControlIsReturned();

    LOG.debug("test thread thread two");
    CompleteTaskThread threadTwo = new CompleteTaskThread(task2);
    threadTwo.startAndWaitUntilControlIsReturned();

    LOG.debug("test thread continues to start thread three");
    CompleteTaskThread threadThree = new CompleteTaskThread(task3);
    threadThree.startAndWaitUntilControlIsReturned();

    LOG.debug("test thread notifies thread 1");
    threadOne.proceedAndWaitTillDone();
    assertNull(threadOne.exception);

    LOG.debug("test thread notifies thread 2");
    threadTwo.proceedAndWaitTillDone();
    assertNotNull(threadTwo.exception);
    assertTextPresent("was updated by another transaction concurrently", threadTwo.exception.getMessage());

    LOG.debug("test thread notifies thread 3");
    threadThree.proceedAndWaitTillDone();
    assertNotNull(threadThree.exception);
    assertTextPresent("was updated by another transaction concurrently", threadThree.exception.getMessage());
  }
}
