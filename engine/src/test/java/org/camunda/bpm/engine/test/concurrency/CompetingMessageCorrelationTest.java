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

import java.util.concurrent.atomic.AtomicInteger;

import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.MessageCorrelationBuilderImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Thorben Lindhauer
 *
 */
public class CompetingMessageCorrelationTest extends ConcurrencyTestCase {

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

  public static class InvocationLogListener implements JavaDelegate {

    protected static AtomicInteger invocations = new AtomicInteger(0);

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

  protected static class ControllableMessageCorrelationCommand extends ControllableCommand<Void> {

    protected String messageName;
    protected boolean exclusive;

    public ControllableMessageCorrelationCommand(String messageName, boolean exclusive) {
      this.messageName = messageName;
      this.exclusive = exclusive;
    }

    public Void execute(CommandContext commandContext) {

      monitor.sync();  // thread will block here until makeContinue() is called form main thread

      if (exclusive) {
        new MessageCorrelationBuilderImpl(commandContext, messageName)
          .correlateExclusively();
      }
      else {
        new MessageCorrelationBuilderImpl(commandContext, messageName)
          .correlate();
      }

      monitor.sync();  // thread will block here until waitUntilDone() is called form main thread

      return null;
    }

  }
}
