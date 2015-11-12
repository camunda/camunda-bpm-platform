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
import org.camunda.bpm.engine.impl.cmd.SignalCmd;
import org.camunda.bpm.engine.impl.cmd.SuspendProcessDefinitionCmd;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.slf4j.Logger;

/**
 * @author Thorben Lindhauer
 */
public class CompetingSuspensionTest extends PluggableProcessEngineTestCase {

private static Logger LOG = ProcessEngineLogger.TEST_LOGGER.getLogger();

  static ControllableThread activeThread;

  class SuspendProcessDefinitionThread extends ControllableThread {

    private String processDefinitionId;
    OptimisticLockingException exception;

    public SuspendProcessDefinitionThread(String processDefinitionId) {
      this.processDefinitionId = processDefinitionId;
    }

    public synchronized void startAndWaitUntilControlIsReturned() {
      activeThread = this;
      super.startAndWaitUntilControlIsReturned();
    }

    public void run() {
      try {
        processEngineConfiguration
          .getCommandExecutorTxRequired()
          .execute(new ControlledCommand(activeThread, new SuspendProcessDefinitionCmd(processDefinitionId, null, true, null)));

      }
      catch (OptimisticLockingException e) {
        this.exception = e;
      }
      LOG.debug(getName()+" ends");
    }
  }

  class SignalThread extends ControllableThread {

    private String executionId;
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
