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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmmn.cmd.CompleteCaseExecutionCmd;
import org.camunda.bpm.engine.impl.cmmn.cmd.ManualStartCaseExecutionCmd;
import org.camunda.bpm.engine.impl.cmmn.cmd.StateTransitionCaseExecutionCmd;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.slf4j.Logger;

/**
 * @author Roman Smirnov
 *
 */
public class CompetingSentrySatisfactionTest {

  private static Logger LOG = ProcessEngineLogger.TEST_LOGGER.getLogger();

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected CaseService caseService;

  protected static ControllableThread activeThread;

  @Before
  public void initializeServices() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    caseService = engineRule.getCaseService();
  }

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

  public class ManualStartSingleThread extends SingleThread {

    public ManualStartSingleThread(String caseExecutionId) {
      super(caseExecutionId, new ManualStartCaseExecutionCmd(caseExecutionId, null, null, null, null));
    }

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/concurrency/CompetingSentrySatisfactionTest.testEntryCriteriaWithAndSentry.cmmn"})
  @Test
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

    LOG.debug("test thread starts thread one");
    SingleThread threadOne = new ManualStartSingleThread(firstHumanTaskId);
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

    String message = threadTwo.exception.getMessage();
    testRule.assertTextPresent("CaseSentryPartEntity", message);
    testRule.assertTextPresent("was updated by another transaction concurrently", message);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/concurrency/CompetingSentrySatisfactionTest.testExitCriteriaWithAndSentry.cmmn"})
  @Test
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

    LOG.debug("test thread starts thread one");
    SingleThread threadOne = new ManualStartSingleThread(firstHumanTaskId);
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

    String message = threadTwo.exception.getMessage();
    testRule.assertTextPresent("CaseSentryPartEntity", message);
    testRule.assertTextPresent("was updated by another transaction concurrently", message);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/concurrency/CompetingSentrySatisfactionTest.testEntryCriteriaWithOrSentry.cmmn"})
  @Test
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

    LOG.debug("test thread starts thread one");
    SingleThread threadOne = new ManualStartSingleThread(firstHumanTaskId);
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

    String message = threadTwo.exception.getMessage();
    testRule.assertTextPresent("CaseExecutionEntity", message);
    testRule.assertTextPresent("was updated by another transaction concurrently", message);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/concurrency/CompetingSentrySatisfactionTest.testExitCriteriaWithOrSentry.cmmn",
      "org/camunda/bpm/engine/test/concurrency/CompetingSentrySatisfactionTest.oneTaskProcess.bpmn20.xml"})
  @Test
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

    CaseExecution thirdTask = caseService
      .createCaseExecutionQuery()
      .caseInstanceId(caseInstanceId)
      .activityId("ProcessTask_3")
      .singleResult();
    caseService.manuallyStartCaseExecution(thirdTask.getId());
    
    LOG.debug("test thread starts thread one");
    SingleThread threadOne = new ManualStartSingleThread(firstHumanTaskId);
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

    String message = threadTwo.exception.getMessage();
    testRule.assertTextPresent("CaseExecutionEntity", message);
    testRule.assertTextPresent("was updated by another transaction concurrently", message);
  }

}
