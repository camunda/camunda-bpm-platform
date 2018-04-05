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

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.impl.MessageCorrelationBuilderImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.CompleteTaskCmd;
import org.camunda.bpm.engine.impl.cmd.MessageEventReceivedCmd;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.DatabaseHelper;

/**
 * @author Thorben Lindhauer
 *
 */
public class CompetingMessageCorrelationTest extends ConcurrencyTestCase {

  @Override
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

    super.tearDown();
  }

  @Override
  protected void runTest() throws Throwable {
    String databaseType = DatabaseHelper.getDatabaseType(processEngineConfiguration);

    if (DbSqlSessionFactory.H2.equals(databaseType) && getName().equals("testConcurrentExclusiveCorrelation")) {
      // skip test method - if database is H2
    } else {
      // invoke the test method
      super.runTest();
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/concurrency/CompetingMessageCorrelationTest.catchMessageProcess.bpmn20.xml")
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

  @Deployment(resources = "org/camunda/bpm/engine/test/concurrency/CompetingMessageCorrelationTest.catchMessageProcess.bpmn20.xml")
  public void testConcurrentExclusiveCorrelation() throws InterruptedException {
    InvocationLogListener.reset();

    // given a process instance
    runtimeService.startProcessInstanceByKey("testProcess");

    // and two threads correlating in parallel
    ThreadControl thread1 = executeControllableCommand(new ControllableMessageCorrelationCommand("Message", true));
    thread1.reportInterrupts();
    ThreadControl thread2 = executeControllableCommand(new ControllableMessageCorrelationCommand("Message", true));
    thread2.reportInterrupts();

    // both threads open a transaction and wait before correlating the message
    thread1.waitForSync();
    thread2.waitForSync();

    // thread one correlates and acquires the exclusive lock
    thread1.makeContinue();
    thread1.waitForSync();

    // the service task was executed once
    assertEquals(1, InvocationLogListener.getInvocations());

    // thread two attempts to acquire the exclusive lock but can't since thread 1 hasn't released it yet
    thread2.makeContinue();
    Thread.sleep(2000);

    // let the first thread ends its transaction
    thread1.makeContinue();
    assertNull(thread1.getException());

    // thread 2 can't continue because the event subscription it tried to lock was deleted
    thread2.waitForSync();
    assertTrue(thread2.getException() != null);
    assertTrue(thread2.getException() instanceof ProcessEngineException);
    assertTextPresent("does not have a subscription to a message event with name 'Message'",
        thread2.getException().getMessage());

    // the first thread ended successfully without an exception
    thread1.join();
    assertNull(thread1.getException());

    // the follow-up task was reached
    Task afterMessageTask = taskService.createTaskQuery().singleResult();
    assertEquals(afterMessageTask.getTaskDefinitionKey(), "afterMessageUserTask");

    // the service task was not executed a second time
    assertEquals(1, InvocationLogListener.getInvocations());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/concurrency/CompetingMessageCorrelationTest.catchMessageProcess.bpmn20.xml")
  public void testConcurrentExclusiveCorrelationToDifferentExecutions() throws InterruptedException {
    InvocationLogListener.reset();

    // given a process instance
    ProcessInstance instance1 = runtimeService.startProcessInstanceByKey("testProcess");
    ProcessInstance instance2 = runtimeService.startProcessInstanceByKey("testProcess");

    // and two threads correlating in parallel to each of the two instances
    ThreadControl thread1 = executeControllableCommand(new ControllableMessageCorrelationCommand("Message", instance1.getId(), true));
    thread1.reportInterrupts();
    ThreadControl thread2 = executeControllableCommand(new ControllableMessageCorrelationCommand("Message", instance2.getId(), true));
    thread2.reportInterrupts();

    // both threads open a transaction and wait before correlating the message
    thread1.waitForSync();
    thread2.waitForSync();

    // thread one correlates and acquires the exclusive lock on the event subscription of instance1
    thread1.makeContinue();
    thread1.waitForSync();

    // the service task was executed once
    assertEquals(1, InvocationLogListener.getInvocations());

    // thread two correlates and acquires the exclusive lock on the event subscription of instance2
    // depending on the database and locking used, this may block thread2
    thread2.makeContinue();

    // thread 1 completes successfully
    thread1.waitUntilDone();
    assertNull(thread1.getException());

    // thread2 should be able to continue at least after thread1 has finished and released its lock
    thread2.waitForSync();

    // the service task was executed the second time
    assertEquals(2, InvocationLogListener.getInvocations());

    // thread 2 completes successfully
    thread2.waitUntilDone();
    assertNull(thread2.getException());

    // the follow-up task was reached in both instances
    assertEquals(2, taskService.createTaskQuery().taskDefinitionKey("afterMessageUserTask").count());
  }

  /**
   * Fails at least on mssql; mssql appears to lock more than the actual event subscription row
   */
  @Deployment(resources = "org/camunda/bpm/engine/test/concurrency/CompetingMessageCorrelationTest.catchMessageProcess.bpmn20.xml")
  public void FAILING_testConcurrentExclusiveCorrelationToDifferentExecutionsCase2() throws InterruptedException {
    InvocationLogListener.reset();

    // given a process instance
    ProcessInstance instance1 = runtimeService.startProcessInstanceByKey("testProcess");
    ProcessInstance instance2 = runtimeService.startProcessInstanceByKey("testProcess");

    // and two threads correlating in parallel to each of the two instances
    ThreadControl thread1 = executeControllableCommand(new ControllableMessageCorrelationCommand("Message", instance1.getId(), true));
    thread1.reportInterrupts();
    ThreadControl thread2 = executeControllableCommand(new ControllableMessageCorrelationCommand("Message", instance2.getId(), true));
    thread2.reportInterrupts();

    // both threads open a transaction and wait before correlating the message
    thread1.waitForSync();
    thread2.waitForSync();

    // thread one correlates and acquires the exclusive lock on the event subscription of instance1
    thread1.makeContinue();
    thread1.waitForSync();

    // the service task was executed once
    assertEquals(1, InvocationLogListener.getInvocations());

    // thread two correlates and acquires the exclusive lock on the event subscription of instance2
    thread2.makeContinue();
    // FIXME: this does not return on sql server due to locking
    thread2.waitForSync();

    // the service task was executed the second time
    assertEquals(2, InvocationLogListener.getInvocations());

    // thread 2 completes successfully, even though it acquired its lock after thread 1
    thread2.waitUntilDone();
    assertNull(thread2.getException());

    // thread 1 completes successfully
    thread1.waitUntilDone();
    assertNull(thread1.getException());

    // the follow-up task was reached in both instances
    assertEquals(2, taskService.createTaskQuery().taskDefinitionKey("afterMessageUserTask").count());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/concurrency/CompetingMessageCorrelationTest.catchMessageProcess.bpmn20.xml")
  public void testConcurrentMixedCorrelation() throws InterruptedException {
    InvocationLogListener.reset();

    // given a process instance
    runtimeService.startProcessInstanceByKey("testProcess");

    // and two threads correlating in parallel (one exclusive, one non-exclusive)
    ThreadControl thread1 = executeControllableCommand(new ControllableMessageCorrelationCommand("Message", true));
    thread1.reportInterrupts();
    ThreadControl thread2 = executeControllableCommand(new ControllableMessageCorrelationCommand("Message", false));
    thread2.reportInterrupts();

    // both threads open a transaction and wait before correlating the message
    thread1.waitForSync();
    thread2.waitForSync();

    // thread one correlates and acquires the exclusive lock
    thread1.makeContinue();
    thread1.waitForSync();

    // thread two correlates since it does not need a pessimistic lock
    thread2.makeContinue();
    thread2.waitForSync();

    // the service task was executed twice
    assertEquals(2, InvocationLogListener.getInvocations());

    // the first thread ends its transaction and releases the lock; the event subscription is now gone
    thread1.waitUntilDone();
    assertNull(thread1.getException());

    Task afterMessageTask = taskService.createTaskQuery().singleResult();
    assertEquals(afterMessageTask.getTaskDefinitionKey(), "afterMessageUserTask");

    // thread two attempts to end its transaction and fails with optimistic locking
    thread2.makeContinue();
    thread2.waitForSync();

    assertTrue(thread2.getException() != null);
    assertTrue(thread2.getException() instanceof OptimisticLockingException);
  }

  /**
   * <p>
   *   At least on MySQL, this test case fails with deadlock exceptions.
   *   The reason is the combination of our flush with the locking of the event
   *   subscription documented in the ticket CAM-3636.
   * </p>
   * @throws InterruptedException
   */
  @Deployment(resources = "org/camunda/bpm/engine/test/concurrency/CompetingMessageCorrelationTest.catchMessageProcess.bpmn20.xml")
  public void FAILING_testConcurrentMixedCorrelationCase2() throws InterruptedException {
    InvocationLogListener.reset();

    // given a process instance
    runtimeService.startProcessInstanceByKey("testProcess");

    // and two threads correlating in parallel (one exclusive, one non-exclusive)
    ThreadControl thread1 = executeControllableCommand(new ControllableMessageCorrelationCommand("Message", false));
    thread1.reportInterrupts();
    ThreadControl thread2 = executeControllableCommand(new ControllableMessageCorrelationCommand("Message", true));
    thread2.reportInterrupts();

    // both threads open a transaction and wait before correlating the message
    thread1.waitForSync();
    thread2.waitForSync();

    // thread one correlates and acquires no lock
    thread1.makeContinue();
    thread1.waitForSync();

    // thread two acquires a lock and succeeds because thread one hasn't acquired one
    thread2.makeContinue();
    thread2.waitForSync();

    // the service task was executed twice
    assertEquals(2, InvocationLogListener.getInvocations());

    // thread one ends its transaction and blocks on flush when it attempts to delete the event subscription
    thread1.makeContinue();
    Thread.sleep(5000);
    assertNull(thread1.getException());

    assertEquals(0, taskService.createTaskQuery().count());

    // thread 2 flushes successfully and releases the lock
    thread2.waitUntilDone();
    assertNull(thread2.getException());

    Task afterMessageTask = taskService.createTaskQuery().singleResult();
    assertNotNull(afterMessageTask);
    assertEquals(afterMessageTask.getTaskDefinitionKey(), "afterMessageUserTask");

    // thread 1 flush fails with optimistic locking
    thread1.join();
    assertTrue(thread1.getException() != null);
    assertTrue(thread1.getException() instanceof OptimisticLockingException);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/concurrency/CompetingMessageCorrelationTest.eventSubprocess.bpmn")
  public void testEventSubprocess() {
    InvocationLogListener.reset();

    // given a process instance
    runtimeService.startProcessInstanceByKey("testProcess");

    // and two threads correlating in parallel
    ThreadControl thread1 = executeControllableCommand(new ControllableMessageCorrelationCommand("incoming", false));
    thread1.reportInterrupts();
    ThreadControl thread2 = executeControllableCommand(new ControllableMessageCorrelationCommand("incoming", false));
    thread2.reportInterrupts();

    // both threads open a transaction and wait before correlating the message
    thread1.waitForSync();
    thread2.waitForSync();

    // both threads correlate
    thread1.makeContinue();
    thread2.makeContinue();

    thread1.waitForSync();
    thread2.waitForSync();

    // the first thread ends its transaction
    thread1.waitUntilDone();
    assertNull(thread1.getException());

    // the second thread ends its transaction and fails with optimistic locking exception
    thread2.waitUntilDone();
    assertTrue(thread2.getException() != null);
    assertTrue(thread2.getException() instanceof OptimisticLockingException);
  }

  @Deployment
  public void testConcurrentMessageCorrelationAndTreeCompaction() {
    runtimeService.startProcessInstanceByKey("process");

    // trigger non-interrupting boundary event and wait before flush
    ThreadControl correlateThread = executeControllableCommand(
        new ControllableMessageCorrelationCommand("Message", false));
    correlateThread.reportInterrupts();

    // stop correlation right before the flush
    correlateThread.waitForSync();
    correlateThread.makeContinueAndWaitForSync();

    // trigger tree compaction
    List<Task> tasks = taskService.createTaskQuery().list();

    for (Task task : tasks) {
      taskService.complete(task.getId());
    }

    // flush correlation
    correlateThread.waitUntilDone();

    // the correlation should not have succeeded
    Throwable exception = correlateThread.getException();
    assertNotNull(exception);
    assertTrue(exception instanceof OptimisticLockingException);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/concurrency/CompetingMessageCorrelationTest.testConcurrentMessageCorrelationAndTreeCompaction.bpmn20.xml")
  public void testConcurrentTreeCompactionAndMessageCorrelation() {
    runtimeService.startProcessInstanceByKey("process");
    List<Task> tasks = taskService.createTaskQuery().list();

    // trigger tree compaction and wait before flush
    ThreadControl taskCompletionThread = executeControllableCommand(new ControllableCompleteTaskCommand(tasks));
    taskCompletionThread.reportInterrupts();

    // stop task completion right before flush
    taskCompletionThread.waitForSync();

    // perform message correlation to non-interrupting boundary event
    // (i.e. adds another concurrent execution to the scope execution)
    runtimeService.correlateMessage("Message");

    // flush task completion and tree compaction
    taskCompletionThread.waitUntilDone();

    // then it should not have succeeded
    Throwable exception = taskCompletionThread.getException();
    assertNotNull(exception);
    assertTrue(exception instanceof OptimisticLockingException);
  }

  @Deployment
  public void testConcurrentMessageCorrelationTwiceAndTreeCompaction() {
    runtimeService.startProcessInstanceByKey("process");

    // trigger non-interrupting boundary event 1 that ends in a none end event immediately
    runtimeService.correlateMessage("Message2");

    // trigger non-interrupting boundary event 2 and wait before flush
    ThreadControl correlateThread = executeControllableCommand(
        new ControllableMessageCorrelationCommand("Message1", false));
    correlateThread.reportInterrupts();

    // stop correlation right before the flush
    correlateThread.waitForSync();
    correlateThread.makeContinueAndWaitForSync();

    // trigger tree compaction
    List<Task> tasks = taskService.createTaskQuery().list();

    for (Task task : tasks) {
      taskService.complete(task.getId());
    }

    // flush correlation
    correlateThread.waitUntilDone();

    // the correlation should not have succeeded
    Throwable exception = correlateThread.getException();
    assertNotNull(exception);
    assertTrue(exception instanceof OptimisticLockingException);
  }

  @Deployment
  public void testConcurrentEndExecutionListener() {
    InvocationLogListener.reset();

    // given a process instance
    runtimeService.startProcessInstanceByKey("testProcess");

    List<Execution> tasks = runtimeService.createExecutionQuery().messageEventSubscriptionName("Message").list();
    // two tasks waiting for the message
    assertEquals(2, tasks.size());

    // start first thread and wait in the second execution end listener
    ThreadControl thread1 = executeControllableCommand(new ControllableMessageEventReceivedCommand(tasks.get(0).getId(), "Message", true));
    thread1.reportInterrupts();
    thread1.waitForSync();

    // the counting execution listener was executed on task 1
    assertEquals(1, InvocationLogListener.getInvocations());

    // start second thread and complete the task
    ThreadControl thread2 = executeControllableCommand(new ControllableMessageEventReceivedCommand(tasks.get(1).getId(), "Message", false));
    thread2.waitForSync();
    thread2.waitUntilDone();

    // the counting execution listener was executed on task 1 and 2
    assertEquals(2, InvocationLogListener.getInvocations());

    // continue with thread 1
    thread1.makeContinueAndWaitForSync();

    // the counting execution listener was not executed again
    assertEquals(2, InvocationLogListener.getInvocations());

    // try to complete thread 1
    thread1.waitUntilDone();

    // thread 1 was rolled back with an optimistic locking exception
    Throwable exception = thread1.getException();
    assertNotNull(exception);
    assertTrue(exception instanceof OptimisticLockingException);

    // the execution listener was not executed again
    assertEquals(2, InvocationLogListener.getInvocations());
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

    public ControllableMessageCorrelationCommand(String messageName, boolean exclusive) {
      this.messageName = messageName;
      this.exclusive = exclusive;
    }

    public ControllableMessageCorrelationCommand(String messageName, String processInstanceId, boolean exclusive) {
      this(messageName, exclusive);
      this.processInstanceId = processInstanceId;
    }

    @Override
    public Void execute(CommandContext commandContext) {

      monitor.sync();  // thread will block here until makeContinue() is called form main thread

      MessageCorrelationBuilderImpl correlationBuilder = new MessageCorrelationBuilderImpl(commandContext, messageName);
      if (processInstanceId != null) {
        correlationBuilder.processInstanceId(processInstanceId);
      }

      if (exclusive) {
        correlationBuilder.correlateExclusively();
      }
      else {
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
