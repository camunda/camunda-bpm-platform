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
import org.camunda.bpm.engine.impl.cmd.ActivityInstanceCancellationCmd;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.slf4j.Logger;

/**
 * @author Roman Smirnov
 *
 */
public class CompetingActivityInstanceCancellationTest extends PluggableProcessEngineTestCase {

private static Logger LOG = ProcessEngineLogger.TEST_LOGGER.getLogger();

  Thread testThread = Thread.currentThread();
  static ControllableThread activeThread;
  static String jobId;

  public class CancelActivityInstance extends ControllableThread {

    String processInstanceId;
    String activityInstanceId;
    OptimisticLockingException exception;

    public CancelActivityInstance(String processInstanceId, String activityInstanceId) {
      this.processInstanceId = processInstanceId;
      this.activityInstanceId = activityInstanceId;
    }
    public synchronized void startAndWaitUntilControlIsReturned() {
      activeThread = this;
      super.startAndWaitUntilControlIsReturned();
    }

    public void run() {
      try {
        processEngineConfiguration
          .getCommandExecutorTxRequired()
          .execute(new ControlledCommand(activeThread, new ActivityInstanceCancellationCmd(processInstanceId, activityInstanceId)));

      } catch (OptimisticLockingException e) {
        this.exception = e;
      }
      LOG.debug(getName()+" ends");
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/concurrency/CompetingForkTest.testCompetingFork.bpmn20.xml"})
  public void testCompetingCancellation() throws Exception {
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    ActivityInstance activityInstance = runtimeService.getActivityInstance(processInstanceId);
    ActivityInstance[] children = activityInstance.getChildActivityInstances();

    String task1ActivityInstanceId = null;
    String task2ActivityInstanceId = null;
    String task3ActivityInstanceId = null;

    for (ActivityInstance currentInstance : children) {

      String id = currentInstance.getId();
      String activityId = currentInstance.getActivityId();

      if ("task1".equals(activityId)) {
        task1ActivityInstanceId = id;
      }
      else if ("task2".equals(activityId)) {
        task2ActivityInstanceId = id;
      }
      else if ("task3".equals(activityId)) {
        task3ActivityInstanceId = id;
      }
      else {
        fail();
      }
    }

    LOG.debug("test thread starts thread one");
    CancelActivityInstance threadOne = new CancelActivityInstance(processInstanceId, task1ActivityInstanceId);
    threadOne.startAndWaitUntilControlIsReturned();

    LOG.debug("test thread thread two");
    CancelActivityInstance threadTwo = new CancelActivityInstance(processInstanceId, task2ActivityInstanceId);
    threadTwo.startAndWaitUntilControlIsReturned();

    LOG.debug("test thread continues to start thread three");
    CancelActivityInstance threadThree = new CancelActivityInstance(processInstanceId, task3ActivityInstanceId);
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
