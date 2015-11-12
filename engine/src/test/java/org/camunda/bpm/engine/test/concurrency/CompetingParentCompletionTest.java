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
import org.camunda.bpm.engine.impl.cmmn.cmd.CompleteCaseExecutionCmd;
import org.camunda.bpm.engine.impl.cmmn.cmd.DisableCaseExecutionCmd;
import org.camunda.bpm.engine.impl.cmmn.cmd.StateTransitionCaseExecutionCmd;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.test.Deployment;
import org.slf4j.Logger;

/**
 * @author Roman Smirnov
 *
 */
public class CompetingParentCompletionTest extends PluggableProcessEngineTestCase {

private static Logger LOG = ProcessEngineLogger.TEST_LOGGER.getLogger();

  Thread testThread = Thread.currentThread();
  static ControllableThread activeThread;

  public abstract class SingleThread extends ControllableThread {

    String caseExecutionId;
    OptimisticLockingException exception;
    protected StateTransitionCaseExecutionCmd cmd;

    public SingleThread(String caseExecutionId, StateTransitionCaseExecutionCmd cmd) {
      this.caseExecutionId = caseExecutionId;
      this.cmd = cmd;
    }

    public synchronized void startAndWaitUntilControlIsReturned() {
      activeThread = this;
      super.startAndWaitUntilControlIsReturned();
    }

    public void run() {
      try {
        processEngineConfiguration
          .getCommandExecutorTxRequired()
          .execute(new ControlledCommand(activeThread, cmd));

      } catch (OptimisticLockingException e) {
        this.exception = e;
      }
      LOG.debug(getName()+" ends");
    }
  }

  public class CompletionSingleThread extends SingleThread {

    public CompletionSingleThread(String caseExecutionId) {
      super(caseExecutionId, new CompleteCaseExecutionCmd(caseExecutionId, null, null, null, null));
    }

  }

  public class DisableSingleThread extends SingleThread {

    public DisableSingleThread(String caseExecutionId) {
      super(caseExecutionId, new DisableCaseExecutionCmd(caseExecutionId, null, null, null, null));
    }

  }

  public class TerminateSingleThread extends SingleThread {

    public TerminateSingleThread(String caseExecutionId) {
      super(caseExecutionId, new StateTransitionCaseExecutionCmd(caseExecutionId, null, null, null, null) {

        private static final long serialVersionUID = 1L;

        @Override
        protected void performStateTransition(CommandContext commandContext, CaseExecutionEntity caseExecution) {
          caseExecution.terminate();
        }

      });
    }

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/concurrency/CompetingParentCompletionTest.testComplete.cmmn"})
  public void testComplete() {
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    String firstHumanTaskId = caseService
        .createCaseExecutionQuery()
        .caseInstanceId(caseInstanceId)
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    String secondHumanTaskId = caseService
        .createCaseExecutionQuery()
        .caseInstanceId(caseInstanceId)
        .activityId("PI_HumanTask_2")
        .singleResult()
        .getId();

    LOG.debug("test thread starts thread one");
    SingleThread threadOne = new CompletionSingleThread(firstHumanTaskId);
    threadOne.startAndWaitUntilControlIsReturned();

    LOG.debug("test thread continues to start thread two");
    SingleThread threadTwo = new CompletionSingleThread(secondHumanTaskId);
    threadTwo.startAndWaitUntilControlIsReturned();

    LOG.debug("test thread notifies thread 1");
    threadOne.proceedAndWaitTillDone();
    assertNull(threadOne.exception);

    LOG.debug("test thread notifies thread 2");
    threadTwo.proceedAndWaitTillDone();
    assertNotNull(threadTwo.exception);
    assertTextPresent("was updated by another transaction concurrently", threadTwo.exception.getMessage());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/concurrency/CompetingParentCompletionTest.testDisable.cmmn"})
  public void testDisable() {
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    String firstHumanTaskId = caseService
        .createCaseExecutionQuery()
        .caseInstanceId(caseInstanceId)
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    String secondHumanTaskId = caseService
        .createCaseExecutionQuery()
        .caseInstanceId(caseInstanceId)
        .activityId("PI_HumanTask_2")
        .singleResult()
        .getId();

    LOG.debug("test thread starts thread one");
    SingleThread threadOne = new DisableSingleThread(firstHumanTaskId);
    threadOne.startAndWaitUntilControlIsReturned();

    LOG.debug("test thread continues to start thread two");
    SingleThread threadTwo = new DisableSingleThread(secondHumanTaskId);
    threadTwo.startAndWaitUntilControlIsReturned();

    LOG.debug("test thread notifies thread 1");
    threadOne.proceedAndWaitTillDone();
    assertNull(threadOne.exception);

    LOG.debug("test thread notifies thread 2");
    threadTwo.proceedAndWaitTillDone();
    assertNotNull(threadTwo.exception);
    assertTextPresent("was updated by another transaction concurrently", threadTwo.exception.getMessage());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/concurrency/CompetingParentCompletionTest.testTerminate.cmmn"})
  public void testTerminate() {
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    String firstHumanTaskId = caseService
        .createCaseExecutionQuery()
        .caseInstanceId(caseInstanceId)
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    String secondHumanTaskId = caseService
        .createCaseExecutionQuery()
        .caseInstanceId(caseInstanceId)
        .activityId("PI_HumanTask_2")
        .singleResult()
        .getId();

    LOG.debug("test thread starts thread one");
    SingleThread threadOne = new TerminateSingleThread(firstHumanTaskId);
    threadOne.startAndWaitUntilControlIsReturned();

    LOG.debug("test thread continues to start thread two");
    SingleThread threadTwo = new TerminateSingleThread(secondHumanTaskId);
    threadTwo.startAndWaitUntilControlIsReturned();

    LOG.debug("test thread notifies thread 1");
    threadOne.proceedAndWaitTillDone();
    assertNull(threadOne.exception);

    LOG.debug("test thread notifies thread 2");
    threadTwo.proceedAndWaitTillDone();
    assertNotNull(threadTwo.exception);
    assertTextPresent("was updated by another transaction concurrently", threadTwo.exception.getMessage());

  }

}
