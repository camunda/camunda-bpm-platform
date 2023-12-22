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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.impl.MessageCorrelationBuilderImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.CompleteTaskCmd;
import org.camunda.bpm.engine.impl.cmd.MessageEventReceivedCmd;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Test;

/**
 * @author Thorben Lindhauer
 *
 */
public class CompetingMessageCorrelationTest extends ConcurrencyTestCase {

  @After
  public void tearDown() throws Exception {
    ((ProcessEngineConfigurationImpl)processEngine.getProcessEngineConfiguration()).getCommandExecutorTxRequiresNew().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {

        List<HistoricJobLog> jobLogs = processEngine.getHistoryService().createHistoricJobLogQuery().list();
        for (HistoricJobLog jobLog : jobLogs) {
          commandContext.getHistoricJobLogManager().deleteHistoricJobLogById(jobLog.getId());
        }

        return null;
      }
    });

    assertEquals(0, processEngine.getHistoryService().createHistoricJobLogQuery().list().size());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/concurrency/CompetingMessageCorrelationTest.catchMessageProcess.bpmn20.xml")
  @Test
  public void testConcurrentCorrelationFailsWithOptimisticLockingException() {
    InvocationLogListener.reset();

    // given a process instance
    runtimeService.startProcessInstanceByKey("testProcess");

    // and two threads correlating in parallel
    ThreadControl thread1 = executeControllableCommand(new ControllableMessageCorrelationCommand("Message", false));
    thread1.reportInterrupts();
    ThreadControl thread2 = executeControllableCommand(new ControllableMessageCorrelationCommand("Message", false));
    thread2.reportInterrupts();

    // both threads open a transaction and wait before correlating the message
    thread1.waitForSync();
    thread2.waitForSync();

    // both threads correlate
    thread1.makeContinue();
    thread2.makeContinue();

    thread1.waitForSync();
    thread2.waitForSync();

    // the service task was executed twice
    assertEquals(2, InvocationLogListener.getInvocations());

    // the first thread ends its transcation
    thread1.waitUntilDone();
    assertNull(thread1.getException());

    Task afterMessageTask = taskService.createTaskQuery().singleResult();
    assertEquals(afterMessageTask.getTaskDefinitionKey(), "afterMessageUserTask");

    // the second thread ends its transaction and fails with optimistic locking exception
    thread2.waitUntilDone();
    assertTrue(thread2.getException() != null);
    assertTrue(thread2.getException() instanceof OptimisticLockingException);
  }

  @Test
  public void testCorrelationWithPayload() throws InterruptedException {
    // given
    BpmnModelInstance targetModel = createModelWithBoundaryEvent(false, false);
    testRule.deploy(targetModel);
    runtimeService.startProcessInstanceByKey("Process_1");
    Map<String, Object> messagePayload = new HashMap<>();
    String outpuValue = "outputValue";
    String variableName = "testVar";
    messagePayload.put(variableName, outpuValue);
    Map<String, Object> messagePayload1 = new HashMap<>();
    String outpuValue1 = "outputValue1";
    messagePayload1.put(variableName, outpuValue1);

    // and two threads correlating in parallel with different variable inputs
    ThreadControl thread1 = executeControllableCommand(new ControllableMessageCorrelationCommand("Message", false, messagePayload));
    thread1.reportInterrupts();
    ThreadControl thread2 = executeControllableCommand(new ControllableMessageCorrelationCommand("Message", false, messagePayload1));
    thread2.reportInterrupts();

    // both threads open a transaction and wait before correlating the message
    // so they are setting the variables and there's no guarantee in which order
    thread1.waitForSync();
    thread2.waitForSync();

    // both threads correlate
    thread1.makeContinue();
    thread2.makeContinue();

    thread1.waitForSync();
    thread2.waitForSync();

    // the first thread ends its transaction successfully
    thread1.waitUntilDone();
    assertNull(thread1.getException());

    Task afterMessageTask = taskService.createTaskQuery().taskDefinitionKey("afterMessage").singleResult();
    assertThat(afterMessageTask).isNotNull();

    // the second thread ends its transaction and fails with optimistic locking exception
    thread2.waitUntilDone();

    // then always OLE for the second thread
    // and the stored variable is always in "afterMessage"
    assertTrue(thread2.getException() != null);
    assertTrue(thread2.getException() instanceof OptimisticLockingException);
    Execution execution = runtimeService.createExecutionQuery().activityId("afterMessage").singleResult();
    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName(variableName).singleResult();
    assertThat(variable.getValue()).isEqualTo(outpuValue);
    assertThat(variable.getExecutionId()).isEqualTo(execution.getId());
  }

  protected BpmnModelInstance createModelWithBoundaryEvent(boolean isInterrupting, boolean isAsync) {
    return Bpmn.createExecutableProcess("Process_1")
        .startEvent()
        .userTask("UserTask_1")
          .boundaryEvent("Message_1")
          .camundaAsyncBefore(isAsync)
          .cancelActivity(isInterrupting)
          .message("Message")
            .exclusiveGateway("Gateway_1")
            .condition("Condition_1", "${testVar == 'outputValue'}")
              .userTask("afterMessage")
              .endEvent("happyEnd")
            .moveToLastGateway()
            .condition("Condition_2", "${testVar != 'outputValue'}")
              .userTask("wrongOutcome")
              .endEvent("unhappyEnd")
        .done();
  }

  public static class InvocationLogListener implements JavaDelegate {

    protected static AtomicInteger invocations = new AtomicInteger(0);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
      invocations.incrementAndGet();
    }

    public static void reset() {
      invocations.set(0);
    }

    public static int getInvocations() {
      return invocations.get();
    }
  }

  public static class WaitingListener implements ExecutionListener {

    protected static ThreadControl monitor;

    public void notify(DelegateExecution execution) throws Exception {
      if (WaitingListener.monitor != null) {
        ThreadControl localMonitor = WaitingListener.monitor;
        WaitingListener.monitor = null;
        localMonitor.sync();
      }
    }

    public static void setMonitor(ThreadControl monitor) {
      WaitingListener.monitor = monitor;
    }
  }

  protected static class ControllableMessageCorrelationCommand extends ControllableCommand<Void> {

    protected String messageName;
    protected boolean exclusive;
    protected String processInstanceId;
    protected Map<String, Object> payload;

    public ControllableMessageCorrelationCommand(String messageName, boolean exclusive) {
      this.messageName = messageName;
      this.exclusive = exclusive;
    }

    public ControllableMessageCorrelationCommand(String messageName, boolean exclusive, Map<String, Object> messagePayload) {
      this.messageName = messageName;
      this.exclusive = exclusive;
      this.payload = messagePayload;
    }

    public ControllableMessageCorrelationCommand(String messageName, String processInstanceId, boolean exclusive) {
      this(messageName, exclusive);
      this.processInstanceId = processInstanceId;
    }

    @Override
    public Void execute(CommandContext commandContext) {

      monitor.sync();  // thread will block here until makeContinue() is called from main thread

      MessageCorrelationBuilderImpl correlationBuilder = new MessageCorrelationBuilderImpl(commandContext, messageName);
      if (processInstanceId != null) {
        correlationBuilder.processInstanceId(processInstanceId);
      }

      if (exclusive) {
        correlationBuilder.correlateExclusively();
      }
      else {
        correlationBuilder.setVariablesToTriggeredScope(payload);
        correlationBuilder.correlate();
      }

      monitor.sync();  // thread will block here until waitUntilDone() is called form main thread

      return null;
    }

  }

  protected static class ControllableMessageEventReceivedCommand extends ControllableCommand<Void> {

    protected final String executionId;
    protected final String messageName;
    protected final boolean shouldWaitInListener;

    public ControllableMessageEventReceivedCommand(String executionId, String messageName, boolean shouldWaitInListener) {
      this.executionId = executionId;
      this.messageName = messageName;
      this.shouldWaitInListener = shouldWaitInListener;
    }

    public Void execute(CommandContext commandContext) {

      if (shouldWaitInListener) {
        WaitingListener.setMonitor(monitor);
      }

      MessageEventReceivedCmd receivedCmd = new MessageEventReceivedCmd(messageName, executionId, null);

      receivedCmd.execute(commandContext);

      monitor.sync();

      return null;
    }
  }

  public static class ControllableCompleteTaskCommand extends ControllableCommand<Void> {

    protected List<Task> tasks;

    public ControllableCompleteTaskCommand(List<Task> tasks) {
      this.tasks = tasks;
    }

    public Void execute(CommandContext commandContext) {

      for (Task task : tasks) {
        CompleteTaskCmd completeTaskCmd = new CompleteTaskCmd(task.getId(), null);
        completeTaskCmd.execute(commandContext);
      }

      monitor.sync();

      return null;
    }

  }

}
