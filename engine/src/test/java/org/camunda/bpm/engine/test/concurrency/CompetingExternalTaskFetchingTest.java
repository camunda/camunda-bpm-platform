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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.CrdbTransactionRetryException;
import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.FetchExternalTasksCmd;
import org.camunda.bpm.engine.impl.externaltask.TopicFetchInstruction;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 *
 */
public class CompetingExternalTaskFetchingTest {

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule();
  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected RuntimeService runtimeService;

  @Before
  public void initializeServices() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    runtimeService = engineRule.getRuntimeService();
  }

  public class ExternalTaskFetcherThread extends ControllableThread {

    protected String workerId;
    protected int results;
    protected String topic;

    protected List<LockedExternalTask> fetchedTasks = Collections.emptyList();
    protected OptimisticLockingException exception;

    public ExternalTaskFetcherThread(String workerId, int results, String topic) {
      this.workerId = workerId;
      this.results = results;
      this.topic = topic;
    }

    public void run() {
      Map<String, TopicFetchInstruction> instructions = new HashMap<String, TopicFetchInstruction>();

      TopicFetchInstruction instruction = new TopicFetchInstruction(topic, 10000L);
      instructions.put(topic, instruction);

      ControlledCommand<List<LockedExternalTask>> cmd = new ControlledCommand<>(
          (ControllableThread) Thread.currentThread(), 
          new FetchExternalTasksCmd(workerId, results, instructions));
      
      try {
        fetchedTasks = processEngineConfiguration.getCommandExecutorTxRequired().execute(cmd);
      } catch (OptimisticLockingException e) {
        exception = e;
      }
    }
  }

  @Deployment
  @Test
  public void testCompetingExternalTaskFetching() {
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    ExternalTaskFetcherThread thread1 = new ExternalTaskFetcherThread("thread1", 5, "externalTaskTopic");
    ExternalTaskFetcherThread thread2 = new ExternalTaskFetcherThread("thread2", 5, "externalTaskTopic");

    // both threads fetch the same task and wait before flushing the lock
    thread1.startAndWaitUntilControlIsReturned();
    thread2.startAndWaitUntilControlIsReturned();

    // thread1 succeeds
    thread1.proceedAndWaitTillDone();
    assertNull(thread1.exception);
    assertEquals(1, thread1.fetchedTasks.size());

    // thread2 does not succeed in locking the job
    thread2.proceedAndWaitTillDone();
    assertEquals(0, thread2.fetchedTasks.size());
    // but does not fail with an OptimisticLockingException
    if (testRule.isOptimisticLockingExceptionSuppressible()) {
      assertNull(thread2.exception);
    } else {
      // on CockroachDb, the `commandRetries` property is 0 by default. So any retryable commands,
      // like the `FetchExternalTasksCmd` will not be retried, but report
      // a `CrdbTransactionRetryException` to the caller.
      assertTrue(thread2.exception instanceof CrdbTransactionRetryException);
    }
  }
}
