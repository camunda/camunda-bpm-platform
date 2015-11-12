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
import org.camunda.bpm.engine.impl.RuntimeServiceImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.interceptor.CommandInterceptor;
import org.camunda.bpm.engine.impl.interceptor.RetryInterceptor;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.slf4j.Logger;


/**
 * @author Tom Baeyens
 */
public class CompetingSignalsTest extends PluggableProcessEngineTestCase {

private static Logger LOG = ProcessEngineLogger.TEST_LOGGER.getLogger();

  Thread testThread = Thread.currentThread();
  static ControllableThread activeThread;

  public class SignalThread extends ControllableThread {

    String executionId;
    OptimisticLockingException exception;

    public SignalThread(String executionId) {
      this.executionId = executionId;
    }

    @Override
    public synchronized void startAndWaitUntilControlIsReturned() {
      activeThread = this;
      super.startAndWaitUntilControlIsReturned();
    }

    public void run() {
      try {
        runtimeService.signal(executionId);
      } catch (OptimisticLockingException e) {
        this.exception = e;
      }
      LOG.debug(getName()+" ends");
    }
  }

  public static class ControlledConcurrencyBehavior implements ActivityBehavior {
    public void execute(ActivityExecution execution) throws Exception {
      activeThread.returnControlToTestThreadAndWait();
    }
  }

  @Deployment
  public void testCompetingSignals() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("CompetingSignalsProcess");
    String processInstanceId = processInstance.getId();

    LOG.debug("test thread starts thread one");
    SignalThread threadOne = new SignalThread(processInstanceId);
    threadOne.startAndWaitUntilControlIsReturned();

    LOG.debug("test thread continues to start thread two");
    SignalThread threadTwo = new SignalThread(processInstanceId);
    threadTwo.startAndWaitUntilControlIsReturned();

    LOG.debug("test thread notifies thread 1");
    threadOne.proceedAndWaitTillDone();
    assertNull(threadOne.exception);

    LOG.debug("test thread notifies thread 2");
    threadTwo.proceedAndWaitTillDone();
    assertNotNull(threadTwo.exception);
    assertTextPresent("was updated by another transaction concurrently", threadTwo.exception.getMessage());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/concurrency/CompetingSignalsTest.testCompetingSignals.bpmn20.xml"})
  public void testCompetingSignalsWithRetry() throws Exception {
    RuntimeServiceImpl runtimeServiceImpl = (RuntimeServiceImpl)runtimeService;
    CommandExecutor before = runtimeServiceImpl.getCommandExecutor();
    try {
      CommandInterceptor retryInterceptor = new RetryInterceptor();
      retryInterceptor.setNext(before);
      runtimeServiceImpl.setCommandExecutor(retryInterceptor);

      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("CompetingSignalsProcess");
      String processInstanceId = processInstance.getId();

      LOG.debug("test thread starts thread one");
      SignalThread threadOne = new SignalThread(processInstanceId);
      threadOne.startAndWaitUntilControlIsReturned();

      LOG.debug("test thread continues to start thread two");
      SignalThread threadTwo = new SignalThread(processInstanceId);
      threadTwo.startAndWaitUntilControlIsReturned();

      LOG.debug("test thread notifies thread 1");
      threadOne.proceedAndWaitTillDone();
      assertNull(threadOne.exception);

      LOG.debug("test thread notifies thread 2");
      threadTwo.proceedAndWaitTillDone();
      assertNull(threadTwo.exception);
    } finally {
      // reset the command executor
      runtimeServiceImpl.setCommandExecutor(before);
    }

  }
}
