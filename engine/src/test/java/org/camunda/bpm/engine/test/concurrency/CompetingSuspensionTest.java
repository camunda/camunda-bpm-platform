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
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.SignalCmd;
import org.camunda.bpm.engine.impl.cmd.SuspendProcessDefinitionCmd;
import org.camunda.bpm.engine.impl.repository.UpdateProcessDefinitionSuspensionStateBuilderImpl;
import org.camunda.bpm.engine.repository.ProcessDefinition;
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
 * @author Thorben Lindhauer
 */
public class CompetingSuspensionTest {

  protected static Logger LOG = ProcessEngineLogger.TEST_LOGGER.getLogger();

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;

  protected static ControllableThread activeThread;


  @Before
  public void initializeServices() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    repositoryService = engineRule.getRepositoryService();
    runtimeService = engineRule.getRuntimeService();
  }

  class SuspendProcessDefinitionThread extends ControllableThread {

    private String processDefinitionId;
    OptimisticLockingException exception;

    public SuspendProcessDefinitionThread(String processDefinitionId) {
      this.processDefinitionId = processDefinitionId;
    }

    @Override
    public synchronized void startAndWaitUntilControlIsReturned() {
      activeThread = this;
      super.startAndWaitUntilControlIsReturned();
    }

    @Override
    public void run() {
      try {
        processEngineConfiguration
          .getCommandExecutorTxRequired()
          .execute(new ControlledCommand<Void>(activeThread, createSuspendCommand()));

      }
      catch (OptimisticLockingException e) {
        this.exception = e;
      }
      LOG.debug(getName()+" ends");
    }

    protected SuspendProcessDefinitionCmd createSuspendCommand() {
      UpdateProcessDefinitionSuspensionStateBuilderImpl builder = new UpdateProcessDefinitionSuspensionStateBuilderImpl()
          .byProcessDefinitionId(processDefinitionId)
          .includeProcessInstances(true);

      return new SuspendProcessDefinitionCmd(builder);
    }
  }

  class SignalThread extends ControllableThread {

    private String executionId;
    OptimisticLockingException exception;

    public SignalThread(String executionId) {
      this.executionId = executionId;
    }

    @Override
    public synchronized void startAndWaitUntilControlIsReturned() {
      activeThread = this;
      super.startAndWaitUntilControlIsReturned();
    }

    @Override
    public void run() {
      try {
        processEngineConfiguration
          .getCommandExecutorTxRequired()
          .execute(new ControlledCommand(activeThread, new SignalCmd(executionId, null, null, null)));

      } catch (OptimisticLockingException e) {
        this.exception = e;
      }
      LOG.debug(getName()+" ends");
    }
  }

  /**
   * Ensures that suspending a process definition and its process instances will also increase the revision of the executions
   * such that concurrent updates fail with an OptimisticLockingException.
   */
  @Deployment
  @Test
  public void testCompetingSuspension() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("CompetingSuspensionProcess").singleResult();

    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
    Execution execution = runtimeService
        .createExecutionQuery()
        .processInstanceId(processInstance.getId())
        .activityId("wait1")
        .singleResult();

    SuspendProcessDefinitionThread suspensionThread = new SuspendProcessDefinitionThread(processDefinition.getId());
    suspensionThread.startAndWaitUntilControlIsReturned();

    SignalThread signalExecutionThread = new SignalThread(execution.getId());
    signalExecutionThread.startAndWaitUntilControlIsReturned();

    suspensionThread.proceedAndWaitTillDone();
    assertNull(suspensionThread.exception);

    signalExecutionThread.proceedAndWaitTillDone();
    assertNotNull(signalExecutionThread.exception);
  }
}
