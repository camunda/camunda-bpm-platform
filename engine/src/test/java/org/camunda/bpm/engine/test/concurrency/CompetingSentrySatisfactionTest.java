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
import org.camunda.bpm.engine.impl.cmmn.cmd.CompleteCaseExecutionCmd;
import org.camunda.bpm.engine.impl.cmmn.cmd.ManualStartCaseExecutionCmd;
import org.camunda.bpm.engine.impl.cmmn.cmd.StateTransitionCaseExecutionCmd;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Roman Smirnov
 *
 */
public class CompetingSentrySatisfactionTest extends PluggableProcessEngineTestCase {

  private static Logger log = Logger.getLogger(CompetingSentrySatisfactionTest.class.getName());

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
      log.fine(getName()+" ends");
    }
  }

  public class CompletionSingleThread extends SingleThread {

    public CompletionSingleThread(String caseExecutionId) {
      super(caseExecutionId, new CompleteCaseExecutionCmd(caseExecutionId, null, null, null, null));
    }

  }

  public class ManualStartSingleThread extends SingleThread {

    public ManualStartSingleThread(String caseExecutionId) {
      super(caseExecutionId, new ManualStartCaseExecutionCmd(caseExecutionId, null, null, null, null));
    }

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/concurrency/CompetingSentrySatisfactionTest.testEntryCriteriaWithAndSentry.cmmn"})
  public void testEntryCriteriaWithAndSentry() {
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

    log.fine("test thread starts thread one");
    SingleThread threadOne = new ManualStartSingleThread(firstHumanTaskId);
    threadOne.startAndWaitUntilControlIsReturned();

    log.fine("test thread continues to start thread two");
    SingleThread threadTwo = new CompletionSingleThread(secondHumanTaskId);
    threadTwo.startAndWaitUntilControlIsReturned();

    log.fine("test thread notifies thread 1");
    threadOne.proceedAndWaitTillDone();
    assertNull(threadOne.exception);

    log.fine("test thread notifies thread 2");
    threadTwo.proceedAndWaitTillDone();
    assertNotNull(threadTwo.exception);

    String message = threadTwo.exception.getMessage();
    assertTextPresent("CaseSentryPartEntity", message);
    assertTextPresent("was updated by another transaction concurrently", message);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/concurrency/CompetingSentrySatisfactionTest.testExitCriteriaWithAndSentry.cmmn"})
  public void testExitCriteriaWithAndSentry() {
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

    log.fine("test thread starts thread one");
    SingleThread threadOne = new ManualStartSingleThread(firstHumanTaskId);
    threadOne.startAndWaitUntilControlIsReturned();

    log.fine("test thread continues to start thread two");
    SingleThread threadTwo = new CompletionSingleThread(secondHumanTaskId);
    threadTwo.startAndWaitUntilControlIsReturned();

    log.fine("test thread notifies thread 1");
    threadOne.proceedAndWaitTillDone();
    assertNull(threadOne.exception);

    log.fine("test thread notifies thread 2");
    threadTwo.proceedAndWaitTillDone();
    assertNotNull(threadTwo.exception);

    String message = threadTwo.exception.getMessage();
    assertTextPresent("CaseSentryPartEntity", message);
    assertTextPresent("was updated by another transaction concurrently", message);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/concurrency/CompetingSentrySatisfactionTest.testEntryCriteriaWithOrSentry.cmmn"})
  public void testEntryCriteriaWithOrSentry() {
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

    log.fine("test thread starts thread one");
    SingleThread threadOne = new ManualStartSingleThread(firstHumanTaskId);
    threadOne.startAndWaitUntilControlIsReturned();

    log.fine("test thread continues to start thread two");
    SingleThread threadTwo = new CompletionSingleThread(secondHumanTaskId);
    threadTwo.startAndWaitUntilControlIsReturned();

    log.fine("test thread notifies thread 1");
    threadOne.proceedAndWaitTillDone();
    assertNull(threadOne.exception);

    log.fine("test thread notifies thread 2");
    threadTwo.proceedAndWaitTillDone();
    assertNotNull(threadTwo.exception);

    String message = threadTwo.exception.getMessage();
    assertTextPresent("CaseExecutionEntity", message);
    assertTextPresent("was updated by another transaction concurrently", message);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/concurrency/CompetingSentrySatisfactionTest.testExitCriteriaWithOrSentry.cmmn"})
  public void testExitCriteriaWithOrSentry() {
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

    log.fine("test thread starts thread one");
    SingleThread threadOne = new ManualStartSingleThread(firstHumanTaskId);
    threadOne.startAndWaitUntilControlIsReturned();

    log.fine("test thread continues to start thread two");
    SingleThread threadTwo = new CompletionSingleThread(secondHumanTaskId);
    threadTwo.startAndWaitUntilControlIsReturned();

    log.fine("test thread notifies thread 1");
    threadOne.proceedAndWaitTillDone();
    assertNull(threadOne.exception);

    log.fine("test thread notifies thread 2");
    threadTwo.proceedAndWaitTillDone();
    assertNotNull(threadTwo.exception);

    String message = threadTwo.exception.getMessage();
    assertTextPresent("CaseExecutionEntity", message);
    assertTextPresent("was updated by another transaction concurrently", message);
  }

}
