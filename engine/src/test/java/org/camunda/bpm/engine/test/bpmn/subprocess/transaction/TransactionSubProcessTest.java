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

package org.camunda.bpm.engine.test.bpmn.subprocess.transaction;

import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.describeActivityInstanceTree;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.ActivityInstanceAssert;
import org.camunda.bpm.engine.variable.Variables;


/**
 * @author Daniel Meyer
 */
public class TransactionSubProcessTest extends PluggableProcessEngineTestCase {


  @Deployment(resources={"org/camunda/bpm/engine/test/bpmn/subprocess/transaction/TransactionSubProcessTest.testSimpleCase.bpmn20.xml"})
  public void testSimpleCaseTxSuccessful() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("transactionProcess");

    // after the process is started, we have compensate event subscriptions:
    assertEquals(5, runtimeService.createEventSubscriptionQuery().eventType("compensate").activityId("undoBookHotel").count());
    assertEquals(1, runtimeService.createEventSubscriptionQuery().eventType("compensate").activityId("undoBookFlight").count());

    // the task is present:
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    // making the tx succeed:
    taskService.setVariable(task.getId(), "confirmed", true);
    taskService.complete(task.getId());

    // now the process instance execution is sitting in the 'afterSuccess' task
    // -> has left the transaction using the "normal" sequence flow
    List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstance.getId());
    assertTrue(activeActivityIds.contains("afterSuccess"));

    // there is a compensate event subscription for the transaction under the process instance
    EventSubscriptionEntity eventSubscriptionEntity = (EventSubscriptionEntity) runtimeService.createEventSubscriptionQuery()
        .eventType("compensate").activityId("tx").executionId(processInstance.getId()).singleResult();

    // there is an event-scope execution associated with the event-subscription:
    assertNotNull(eventSubscriptionEntity.getConfiguration());
    Execution eventScopeExecution = runtimeService.createExecutionQuery().executionId(eventSubscriptionEntity.getConfiguration()).singleResult();
    assertNotNull(eventScopeExecution);

    // there is a compensate event subscription for the miBody of 'bookHotel' activity
    EventSubscriptionEntity miBodyEventSubscriptionEntity = (EventSubscriptionEntity) runtimeService.createEventSubscriptionQuery()
        .eventType("compensate").activityId("bookHotel" + BpmnParse.MULTI_INSTANCE_BODY_ID_SUFFIX).executionId(eventScopeExecution.getId()).singleResult();
    assertNotNull(miBodyEventSubscriptionEntity);
    String miBodyEventScopeExecutionId = miBodyEventSubscriptionEntity.getConfiguration();

    // we still have compensate event subscriptions for the compensation handlers, only now they are part of the event scope
    assertEquals(5, runtimeService.createEventSubscriptionQuery().eventType("compensate").activityId("undoBookHotel").executionId(miBodyEventScopeExecutionId).count());
    assertEquals(1, runtimeService.createEventSubscriptionQuery().eventType("compensate").activityId("undoBookFlight").executionId(eventScopeExecution.getId()).count());
    assertEquals(1, runtimeService.createEventSubscriptionQuery().eventType("compensate").activityId("undoChargeCard").executionId(eventScopeExecution.getId()).count());

    // assert that the compensation handlers have not been invoked:
    assertNull(runtimeService.getVariable(processInstance.getId(), "undoBookHotel"));
    assertNull(runtimeService.getVariable(processInstance.getId(), "undoBookFlight"));
    assertNull(runtimeService.getVariable(processInstance.getId(), "undoChargeCard"));

    // end the process instance
    runtimeService.signal(processInstance.getId());
    assertProcessEnded(processInstance.getId());
    assertEquals(0, runtimeService.createExecutionQuery().count());

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/bpmn/subprocess/transaction/TransactionSubProcessTest.testSimpleCase.bpmn20.xml"})
  public void testActivityInstanceTreeAfterSuccessfulCompletion() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("transactionProcess");

    // the tx task is present
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    // making the tx succeed
    taskService.setVariable(task.getId(), "confirmed", true);
    taskService.complete(task.getId());

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    ActivityInstanceAssert.assertThat(tree)
      .hasStructure(
        ActivityInstanceAssert.describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .activity("afterSuccess")
        .done());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/bpmn/subprocess/transaction/TransactionSubProcessTest.testWaitstateCompensationHandler.bpmn20.xml"})
  public void testWaitstateCompensationHandler() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("transactionProcess");

    // after the process is started, we have compensate event subscriptions:
    assertEquals(5, runtimeService.createEventSubscriptionQuery().eventType("compensate").activityId("undoBookHotel").count());
    assertEquals(1, runtimeService.createEventSubscriptionQuery().eventType("compensate").activityId("undoBookFlight").count());

    // the task is present:
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    // making the tx fail:
    taskService.setVariable(task.getId(), "confirmed", false);
    taskService.complete(task.getId());

    // now there are two user task instances (the compensation handlers):

    List<Task> undoBookHotel = taskService.createTaskQuery().taskDefinitionKey("undoBookHotel").list();
    List<Task> undoBookFlight = taskService.createTaskQuery().taskDefinitionKey("undoBookFlight").list();

    assertEquals(5,undoBookHotel.size());
    assertEquals(1,undoBookFlight.size());

    ActivityInstance rootActivityInstance = runtimeService.getActivityInstance(processInstance.getId());
    List<ActivityInstance> undoBookHotelInstances = getInstancesForActivityId(rootActivityInstance, "undoBookHotel");
    List<ActivityInstance> undoBookFlightInstances = getInstancesForActivityId(rootActivityInstance, "undoBookFlight");
    assertEquals(5, undoBookHotelInstances.size());
    assertEquals(1, undoBookFlightInstances.size());

    assertThat(
        describeActivityInstanceTree(processInstance.getId())
          .beginScope("tx")
            .activity("failure")
            .activity("undoBookHotel")
            .activity("undoBookHotel")
            .activity("undoBookHotel")
            .activity("undoBookHotel")
            .activity("undoBookHotel")
            .activity("undoBookFlight")
          .done()
          );

    for (Task t : undoBookHotel) {
      taskService.complete(t.getId());
    }
    taskService.complete(undoBookFlight.get(0).getId());

    // now the process instance execution is sitting in the 'afterCancellation' task
    // -> has left the transaction using the cancel boundary event
    List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstance.getId());
    assertTrue(activeActivityIds.contains("afterCancellation"));

    // we have no more compensate event subscriptions
    assertEquals(0, runtimeService.createEventSubscriptionQuery().eventType("compensate").count());

    // end the process instance
    runtimeService.signal(processInstance.getId());
    assertProcessEnded(processInstance.getId());
    assertEquals(0, runtimeService.createExecutionQuery().count());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/bpmn/subprocess/transaction/TransactionSubProcessTest.testSimpleCase.bpmn20.xml"})
  public void testSimpleCaseTxCancelled() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("transactionProcess");

    // after the process is started, we have compensate event subscriptions:
    assertEquals(5, runtimeService.createEventSubscriptionQuery().eventType("compensate").activityId("undoBookHotel").count());
    assertEquals(1,runtimeService.createEventSubscriptionQuery().eventType("compensate").activityId("undoBookFlight").count());

    // the task is present:
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    // making the tx fail:
    taskService.setVariable(task.getId(), "confirmed", false);
    taskService.complete(task.getId());

    // we have no more compensate event subscriptions
    assertEquals(0,runtimeService.createEventSubscriptionQuery().eventType("compensate").count());

    // assert that the compensation handlers have been invoked:
    assertEquals(5, runtimeService.getVariable(processInstance.getId(), "undoBookHotel"));
    assertEquals(1, runtimeService.getVariable(processInstance.getId(), "undoBookFlight"));
    assertEquals(1, runtimeService.getVariable(processInstance.getId(), "undoChargeCard"));

    // signal compensation handler completion
    List<Execution> compensationHandlerExecutions = collectExecutionsFor("undoBookHotel", "undoBookFlight", "undoChargeCard");
    for (Execution execution : compensationHandlerExecutions) {
      runtimeService.signal(execution.getId());
    }

    // now the process instance execution is sitting in the 'afterCancellation' task
    // -> has left the transaction using the cancel boundary event
    List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstance.getId());
    assertTrue(activeActivityIds.contains("afterCancellation"));

    // if we have history, we check that the invocation of the compensation handlers is recorded in history.
    if(!processEngineConfiguration.getHistory().equals(ProcessEngineConfiguration.HISTORY_NONE)) {
      assertEquals(1, historyService.createHistoricActivityInstanceQuery()
              .activityId("undoBookFlight")
              .count());

      assertEquals(5, historyService.createHistoricActivityInstanceQuery()
              .activityId("undoBookHotel")
              .count());

      assertEquals(1, historyService.createHistoricActivityInstanceQuery()
              .activityId("undoChargeCard")
              .count());
    }

    // end the process instance
    runtimeService.signal(processInstance.getId());
    assertProcessEnded(processInstance.getId());
    assertEquals(0, runtimeService.createExecutionQuery().count());
  }


  @Deployment
  public void testCancelEndConcurrent() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("transactionProcess");

    // after the process is started, we have compensate event subscriptions:
    assertEquals(5,runtimeService.createEventSubscriptionQuery().eventType("compensate").activityId("undoBookHotel").count());
    assertEquals(1,runtimeService.createEventSubscriptionQuery().eventType("compensate").activityId("undoBookFlight").count());

    // the task is present:
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    // making the tx fail:
    taskService.setVariable(task.getId(), "confirmed", false);
    taskService.complete(task.getId());

    // we have no more compensate event subscriptions
    assertEquals(0,runtimeService.createEventSubscriptionQuery().eventType("compensate").count());

    // assert that the compensation handlers have been invoked:
    assertEquals(5, runtimeService.getVariable(processInstance.getId(), "undoBookHotel"));
    assertEquals(1, runtimeService.getVariable(processInstance.getId(), "undoBookFlight"));

    // signal compensation handler completion
    List<Execution> compensationHandlerExecutions = collectExecutionsFor("undoBookHotel", "undoBookFlight");
    for (Execution execution : compensationHandlerExecutions) {
      runtimeService.signal(execution.getId());
    }

    // now the process instance execution is sitting in the 'afterCancellation' task
    // -> has left the transaction using the cancel boundary event
    List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstance.getId());
    assertTrue(activeActivityIds.contains("afterCancellation"));

    // if we have history, we check that the invocation of the compensation handlers is recorded in history.
    if(!processEngineConfiguration.getHistory().equals(ProcessEngineConfiguration.HISTORY_NONE)) {
      assertEquals(1, historyService.createHistoricActivityInstanceQuery()
              .activityId("undoBookFlight")
              .count());

      assertEquals(5, historyService.createHistoricActivityInstanceQuery()
              .activityId("undoBookHotel")
              .count());
    }

    // end the process instance
    runtimeService.signal(processInstance.getId());
    assertProcessEnded(processInstance.getId());
    assertEquals(0, runtimeService.createExecutionQuery().count());
  }

  @Deployment
  public void testNestedCancelInner() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("transactionProcess");

    // after the process is started, we have compensate event subscriptions:
    assertEquals(0,runtimeService.createEventSubscriptionQuery().eventType("compensate").activityId("undoBookFlight").count());
    assertEquals(5,runtimeService.createEventSubscriptionQuery().eventType("compensate").activityId("innerTxundoBookHotel").count());
    assertEquals(1,runtimeService.createEventSubscriptionQuery().eventType("compensate").activityId("innerTxundoBookFlight").count());

    // the tasks are present:
    Task taskInner = taskService.createTaskQuery().taskDefinitionKey("innerTxaskCustomer").singleResult();
    Task taskOuter = taskService.createTaskQuery().taskDefinitionKey("bookFlight").singleResult();
    assertNotNull(taskInner);
    assertNotNull(taskOuter);

    // making the tx fail:
    taskService.setVariable(taskInner.getId(), "confirmed", false);
    taskService.complete(taskInner.getId());

    // we have no more compensate event subscriptions for the inner tx
    assertEquals(0,runtimeService.createEventSubscriptionQuery().eventType("compensate").activityId("innerTxundoBookHotel").count());
    assertEquals(0,runtimeService.createEventSubscriptionQuery().eventType("compensate").activityId("innerTxundoBookFlight").count());

    // we do not have a subscription or the outer tx yet
    assertEquals(0,runtimeService.createEventSubscriptionQuery().eventType("compensate").activityId("undoBookFlight").count());

    // assert that the compensation handlers have been invoked:
    assertEquals(5, runtimeService.getVariable(processInstance.getId(), "innerTxundoBookHotel"));
    assertEquals(1, runtimeService.getVariable(processInstance.getId(), "innerTxundoBookFlight"));

    // signal compensation handler completion
    List<Execution> compensationHandlerExecutions = collectExecutionsFor("innerTxundoBookFlight", "innerTxundoBookHotel");
    for (Execution execution : compensationHandlerExecutions) {
      runtimeService.signal(execution.getId());
    }

    // now the process instance execution is sitting in the 'afterInnerCancellation' task
    // -> has left the transaction using the cancel boundary event
    List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstance.getId());
    assertTrue(activeActivityIds.contains("afterInnerCancellation"));

    // if we have history, we check that the invocation of the compensation handlers is recorded in history.
    if(!processEngineConfiguration.getHistory().equals(ProcessEngineConfiguration.HISTORY_NONE)) {
      assertEquals(5, historyService.createHistoricActivityInstanceQuery()
              .activityId("innerTxundoBookHotel")
              .count());

      assertEquals(1, historyService.createHistoricActivityInstanceQuery()
              .activityId("innerTxundoBookFlight")
              .count());
    }

    // complete the task in the outer tx
    taskService.complete(taskOuter.getId());

    // end the process instance (signal the execution still sitting in afterInnerCancellation)
    runtimeService.signal(runtimeService.createExecutionQuery().activityId("afterInnerCancellation").singleResult().getId());

    assertProcessEnded(processInstance.getId());
    assertEquals(0, runtimeService.createExecutionQuery().count());
  }

  @Deployment
  public void testNestedCancelOuter() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("transactionProcess");

    // after the process is started, we have compensate event subscriptions:
    assertEquals(0,runtimeService.createEventSubscriptionQuery().eventType("compensate").activityId("undoBookFlight").count());
    assertEquals(5,runtimeService.createEventSubscriptionQuery().eventType("compensate").activityId("innerTxundoBookHotel").count());
    assertEquals(1,runtimeService.createEventSubscriptionQuery().eventType("compensate").activityId("innerTxundoBookFlight").count());

    // the tasks are present:
    Task taskInner = taskService.createTaskQuery().taskDefinitionKey("innerTxaskCustomer").singleResult();
    Task taskOuter = taskService.createTaskQuery().taskDefinitionKey("bookFlight").singleResult();
    assertNotNull(taskInner);
    assertNotNull(taskOuter);

    // making the outer tx fail (invokes cancel end event)
    taskService.complete(taskOuter.getId());

    // now the process instance is sitting in 'afterOuterCancellation'
    List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstance.getId());
    assertTrue(activeActivityIds.contains("afterOuterCancellation"));

    // we have no more compensate event subscriptions
    assertEquals(0,runtimeService.createEventSubscriptionQuery().eventType("compensate").activityId("innerTxundoBookHotel").count());
    assertEquals(0,runtimeService.createEventSubscriptionQuery().eventType("compensate").activityId("innerTxundoBookFlight").count());
    assertEquals(0,runtimeService.createEventSubscriptionQuery().eventType("compensate").activityId("undoBookFlight").count());

    // the compensation handlers of the inner tx have not been invoked
    assertNull(runtimeService.getVariable(processInstance.getId(), "innerTxundoBookHotel"));
    assertNull(runtimeService.getVariable(processInstance.getId(), "innerTxundoBookFlight"));

    // the compensation handler in the outer tx has been invoked
    assertEquals(1, runtimeService.getVariable(processInstance.getId(), "undoBookFlight"));

    // end the process instance (signal the execution still sitting in afterOuterCancellation)
    runtimeService.signal(runtimeService.createExecutionQuery().activityId("afterOuterCancellation").singleResult().getId());

    assertProcessEnded(processInstance.getId());
    assertEquals(0, runtimeService.createExecutionQuery().count());

  }

  /*
   * The cancel end event cancels all instances, compensation is performed for all instances
   *
   * see spec page 470:
   * "If the cancelActivity attribute is set, the Activity the Event is attached to is then
   * cancelled (in case of a multi-instance, all its instances are cancelled);"
   */
  @Deployment
  public void testMultiInstanceTx() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("transactionProcess");

    // there are now 5 instances of the transaction:

    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery()
      .eventType("compensate")
      .list();

    // there are 10 compensation event subscriptions
    assertEquals(10, eventSubscriptions.size());

    Task task = taskService.createTaskQuery().listPage(0, 1).get(0);

    // canceling one instance triggers compensation for all other instances:
    taskService.setVariable(task.getId(), "confirmed", false);
    taskService.complete(task.getId());

    assertEquals(0, runtimeService.createEventSubscriptionQuery().count());

    assertEquals(1, runtimeService.getVariable(processInstance.getId(), "undoBookHotel"));
    assertEquals(1, runtimeService.getVariable(processInstance.getId(), "undoBookFlight"));

    runtimeService.signal(runtimeService.createExecutionQuery().activityId("afterCancellation").singleResult().getId());

    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/bpmn/subprocess/transaction/TransactionSubProcessTest.testMultiInstanceTx.bpmn20.xml"})
  public void testMultiInstanceTxSuccessful() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("transactionProcess");

    // there are now 5 instances of the transaction:

    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery()
      .eventType("compensate")
      .list();

    // there are 10 compensation event subscriptions
    assertEquals(10, eventSubscriptions.size());

    // first complete the inner user-tasks
    List<Task> tasks = taskService.createTaskQuery().list();
    for (Task task : tasks) {
      taskService.setVariable(task.getId(), "confirmed", true);
      taskService.complete(task.getId());
    }

    // now complete the inner receive tasks
    List<Execution> executions = runtimeService.createExecutionQuery().activityId("receive").list();
    for (Execution execution : executions) {
      runtimeService.signal(execution.getId());
    }

    runtimeService.signal(runtimeService.createExecutionQuery().activityId("afterSuccess").singleResult().getId());

    assertEquals(0, runtimeService.createEventSubscriptionQuery().count());
    assertProcessEnded(processInstance.getId());

  }

  @Deployment
  public void testCompensateSubprocess() {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("txProcess");

    Task innerTask = taskService.createTaskQuery().singleResult();
    taskService.complete(innerTask.getId());

    // when the transaction is cancelled
    runtimeService.setVariable(instance.getId(), "cancelTx", true);
    runtimeService.setVariable(instance.getId(), "compensate", false);
    Task beforeCancelTask = taskService.createTaskQuery().singleResult();
    taskService.complete(beforeCancelTask.getId());

    // then compensation is triggered
    Task compensationTask = taskService.createTaskQuery().singleResult();
    assertNotNull(compensationTask);
    assertEquals("undoInnerTask", compensationTask.getTaskDefinitionKey());
    taskService.complete(compensationTask.getId());

    // and the process instance ends successfully
    Task afterBoundaryTask = taskService.createTaskQuery().singleResult();
    assertEquals("afterCancel", afterBoundaryTask.getTaskDefinitionKey());
    taskService.complete(afterBoundaryTask.getId());
    assertProcessEnded(instance.getId());
  }

  @Deployment
  public void testCompensateTransactionWithEventSubprocess() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("txProcess");
    Task beforeCancelTask = taskService.createTaskQuery().singleResult();

    // when the transaction is cancelled and handled by an event subprocess
    taskService.complete(beforeCancelTask.getId());

    // then completing compensation works
    Task compensationHandler = taskService.createTaskQuery().singleResult();
    assertNotNull(compensationHandler);
    assertEquals("blackBoxCompensationHandler", compensationHandler.getTaskDefinitionKey());

    taskService.complete(compensationHandler.getId());

    assertProcessEnded(processInstance.getId());

  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/subprocess/transaction/TransactionSubProcessTest.testCompensateTransactionWithEventSubprocess.bpmn20.xml")
  public void testCompensateTransactionWithEventSubprocessActivityInstanceTree() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("txProcess");
    Task beforeCancelTask = taskService.createTaskQuery().singleResult();

    // when the transaction is cancelled and handled by an event subprocess
    taskService.complete(beforeCancelTask.getId());

    // then the activity instance tree is correct
    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .beginScope("tx")
            .activity("cancelEnd")
            .beginScope("innerSubProcess")
              .activity("blackBoxCompensationHandler")
              .beginScope("eventSubProcess")
                .activity("eventSubProcessThrowCompensation")
       .done());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/subprocess/transaction/TransactionSubProcessTest.testCompensateSubprocess.bpmn20.xml")
  public void testCompensateSubprocessNotTriggered() {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("txProcess");

    Task innerTask = taskService.createTaskQuery().singleResult();
    taskService.complete(innerTask.getId());

    // when the transaction is not cancelled
    runtimeService.setVariable(instance.getId(), "cancelTx", false);
    runtimeService.setVariable(instance.getId(), "compensate", false);
    Task beforeEndTask = taskService.createTaskQuery().singleResult();
    taskService.complete(beforeEndTask.getId());

    // then
    Task afterTxTask = taskService.createTaskQuery().singleResult();
    assertEquals("afterTx", afterTxTask.getTaskDefinitionKey());

    // and the process has ended
    taskService.complete(afterTxTask.getId());
    assertProcessEnded(instance.getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/subprocess/transaction/TransactionSubProcessTest.testCompensateSubprocess.bpmn20.xml")
  public void testCompensateSubprocessAfterTxCompletion() {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("txProcess");

    Task innerTask = taskService.createTaskQuery().singleResult();
    taskService.complete(innerTask.getId());

    // when the transaction is not cancelled
    runtimeService.setVariable(instance.getId(), "cancelTx", false);
    runtimeService.setVariable(instance.getId(), "compensate", true);
    Task beforeTxEndTask = taskService.createTaskQuery().singleResult();
    taskService.complete(beforeTxEndTask.getId());

    // but when compensation is thrown after the tx has completed successfully
    Task afterTxTask = taskService.createTaskQuery().singleResult();
    taskService.complete(afterTxTask.getId());

    // then compensation for the subprocess is triggered
    Task compensationTask = taskService.createTaskQuery().singleResult();
    assertNotNull(compensationTask);
    assertEquals("undoInnerTask", compensationTask.getTaskDefinitionKey());
    taskService.complete(compensationTask.getId());

    // and the process has ended
    assertProcessEnded(instance.getId());
  }

  @Deployment
  public void FAILURE_testMultipleCompensationOfCancellationOfMultipleTx() {
    // when
    List<String> devices = new ArrayList<String>();
	  devices.add("device1");
    devices.add("device2");
    devices.add("fail");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey( //
	      "order", //
	      Variables.putValue("devices", devices));

    // then the compensation should be triggered three times
    int expected = 3;
    int actual = historyService
      .createHistoricActivityInstanceQuery()
      .activityId("ServiceTask_CompensateConfiguration")
      .list()
      .size();
    assertEquals(expected, actual);
  }

  public void testMultipleCancelBoundaryFails() {
    try {
      repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/subprocess/transaction/TransactionSubProcessTest.testMultipleCancelBoundaryFails.bpmn20.xml")
        .deploy();
      fail("exception expected");
    } catch (Exception e) {
      if(!e.getMessage().contains("multiple boundary events with cancelEventDefinition not supported on same transaction")) {
        fail("different exception expected");
      }
    }
  }

  public void testCancelBoundaryNoTransactionFails() {
    try {
      repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/subprocess/transaction/TransactionSubProcessTest.testCancelBoundaryNoTransactionFails.bpmn20.xml")
        .deploy();
      fail("exception expected");
    } catch (Exception e) {
      if(!e.getMessage().contains("boundary event with cancelEventDefinition only supported on transaction subprocesses")) {
        fail("different exception expected");
      }
    }
  }

  public void testCancelEndNoTransactionFails() {
    try {
      repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/subprocess/transaction/TransactionSubProcessTest.testCancelEndNoTransactionFails.bpmn20.xml")
        .deploy();
      fail("exception expected");
    } catch (Exception e) {
      if(!e.getMessage().contains("end event with cancelEventDefinition only supported inside transaction subprocess")) {
        fail("different exception expected");
      }
    }
  }

  @Deployment
  public void testParseWithDI() {

    // this test simply makes sure we can parse a transaction subprocess with DI information
    // the actual transaction behavior is tested by other testcases

    //// failing case

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("TransactionSubProcessTest");

    Task task = taskService.createTaskQuery().singleResult();
    taskService.setVariable(task.getId(), "confirmed", false);

    taskService.complete(task.getId());

    assertProcessEnded(processInstance.getId());


    ////// success case

    processInstance = runtimeService.startProcessInstanceByKey("TransactionSubProcessTest");

    task = taskService.createTaskQuery().singleResult();
    taskService.setVariable(task.getId(), "confirmed", true);

    taskService.complete(task.getId());

    assertProcessEnded(processInstance.getId());
  }

  protected List<Execution> collectExecutionsFor(String... activityIds) {
    List<Execution> executions = new ArrayList<Execution>();

    for (String activityId : activityIds) {
      executions.addAll(runtimeService.createExecutionQuery().activityId(activityId).list());
    }

    return executions;
  }
}
