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

import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.SignalCmd;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.slf4j.Logger;


/**
 * @author Tom Baeyens
 */
public class CompetingJoinTest {

  private static Logger LOG = ProcessEngineLogger.TEST_LOGGER.getLogger();

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected RuntimeService runtimeService;

  protected static ControllableThread activeThread;

  @Before
  public void initializeServices() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    runtimeService = engineRule.getRuntimeService();
  }

  public class SignalThread extends ControllableThread {
    String executionId;
    OptimisticLockingException exception;
    public SignalThread(String executionId) {
      this.executionId = executionId;
    }
    public synchronized void startAndWaitUntilControlIsReturned() {
      activeThread = this;
      super.startAndWaitUntilControlIsReturned();
    }
    public void run() {
      try {
        processEngineConfiguration
          .getCommandExecutorTxRequired()
          .execute(new ControlledCommand(activeThread, new SignalCmd(executionId, null, null,null)));

      } catch (OptimisticLockingException e) {
        this.exception = e;
      }
      LOG.debug(getName()+" ends");
    }
  }

  @Deployment
  @Test
  public void testCompetingJoins() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("CompetingJoinsProcess");
    Execution execution1 = runtimeService
      .createExecutionQuery()
      .processInstanceId(processInstance.getId())
      .activityId("wait1")
      .singleResult();

    Execution execution2 = runtimeService
      .createExecutionQuery()
      .processInstanceId(processInstance.getId())
      .activityId("wait2")
      .singleResult();

    LOG.debug("test thread starts thread one");
    SignalThread threadOne = new SignalThread(execution1.getId());
    threadOne.startAndWaitUntilControlIsReturned();

    LOG.debug("test thread continues to start thread two");
    SignalThread threadTwo = new SignalThread(execution2.getId());
    threadTwo.startAndWaitUntilControlIsReturned();

    LOG.debug("test thread notifies thread 1");
    threadOne.proceedAndWaitTillDone();
    assertNull(threadOne.exception);

    LOG.debug("test thread notifies thread 2");
    threadTwo.proceedAndWaitTillDone();
    assertNotNull(threadTwo.exception);
    testRule.assertTextPresent("was updated by another transaction concurrently", threadTwo.exception.getMessage());
  }

}
