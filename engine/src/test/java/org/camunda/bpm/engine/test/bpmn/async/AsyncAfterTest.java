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
package org.camunda.bpm.engine.test.bpmn.async;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Daniel Meyer
 * @author Stefan Hentschel
 *
 */
public class AsyncAfterTest extends PluggableProcessEngineTestCase {

  public void testTransitionIdRequired() {

    // if an outgoing sequence flow has no id, we cannot use it in asyncAfter
    try {
      repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/async/AsyncAfterTest.testTransitionIdRequired.bpmn20.xml")
        .deploy();
      fail("Exception expected");
    } catch ( ProcessEngineException e) {
      assertTextPresent("Sequence flow with sourceRef='service' must have an id, activity with id 'service' uses 'asyncAfter'.", e.getMessage());
    }

  }

  @Deployment
  public void testAsyncAfterServiceTask() {
    // start process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    // listeners should be fired by now
    assertListenerStartInvoked(pi);
    assertListenerEndInvoked(pi);

    // the process should wait *after* the catch event
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);

    // if the waiting job is executed, the process instance should end
    managementService.executeJob(job.getId());
    assertProcessEnded(pi.getId());
  }

  @Deployment
  public void testAsyncAfterAndBeforeServiceTask() {
    // start process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    // the service task is not yet invoked
    assertNotListenerStartInvoked(pi);
    assertNotBehaviorInvoked(pi);
    assertNotListenerEndInvoked(pi);

    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);

    // if the job is executed
    managementService.executeJob(job.getId());

    // the manual task is invoked
    assertListenerStartInvoked(pi);
    assertListenerEndInvoked(pi);

    // and now the process is waiting *after* the manual task
    job = managementService.createJobQuery().singleResult();
    assertNotNull(job);

    // after executing the waiting job, the process instance will end
    managementService.executeJob(job.getId());
    assertProcessEnded(pi.getId());
  }

  @Deployment
  public void testAsyncAfterServiceTaskMultipleTransitions() {

    // start process instance
    Map<String, Object> varMap = new HashMap<String, Object>();
    varMap.put("flowToTake", "flow2");
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess", varMap);

    // the service task is completely invoked
    assertListenerStartInvoked(pi);
    assertBehaviorInvoked(pi);
    assertListenerEndInvoked(pi);

    // and the execution is waiting *after* the service task
    Job continuationJob = managementService.createJobQuery().singleResult();
    assertNotNull(continuationJob);

    // if we execute the job, the process instance continues along the selected path
    managementService.executeJob(continuationJob.getId());

    assertNotNull(runtimeService.createExecutionQuery().activityId("taskAfterFlow2").singleResult());
    assertNull(runtimeService.createExecutionQuery().activityId("taskAfterFlow3").singleResult());

    // end the process
    runtimeService.signal(pi.getId());

    //////////////////////////////////////////////////////////////

    // start process instance
    varMap = new HashMap<String, Object>();
    varMap.put("flowToTake", "flow3");
    pi = runtimeService.startProcessInstanceByKey("testProcess", varMap);

    // the service task is completely invoked
    assertListenerStartInvoked(pi);
    assertBehaviorInvoked(pi);
    assertListenerEndInvoked(pi);

    // and the execution is waiting *after* the service task
    continuationJob = managementService.createJobQuery().singleResult();
    assertNotNull(continuationJob);

    // if we execute the job, the process instance continues along the selected path
    managementService.executeJob(continuationJob.getId());

    assertNull(runtimeService.createExecutionQuery().activityId("taskAfterFlow2").singleResult());
    assertNotNull(runtimeService.createExecutionQuery().activityId("taskAfterFlow3").singleResult());

  }

  @Deployment
  public void testAsyncAfterServiceTaskMultipleTransitionsConcurrent() {

    // start process instance
    Map<String, Object> varMap = new HashMap<String, Object>();
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess", varMap);

    // the service task is completely invoked
    assertListenerStartInvoked(pi);
    assertBehaviorInvoked(pi);
    assertListenerEndInvoked(pi);

    // there are two async jobs
    List<Job> jobs = managementService.createJobQuery().list();
    assertEquals(2, jobs.size());
    managementService.executeJob(jobs.get(0).getId());
    managementService.executeJob(jobs.get(1).getId());

    // both subsequent tasks are activated
    assertNotNull(runtimeService.createExecutionQuery().activityId("taskAfterFlow2").singleResult());
    assertNotNull(runtimeService.createExecutionQuery().activityId("taskAfterFlow3").singleResult());

  }

  @Deployment
  public void testAsyncAfterWithoutTransition() {

    // start process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    // the service task is completely invoked
    assertListenerStartInvoked(pi);
    assertBehaviorInvoked(pi);
    assertListenerEndInvoked(pi);

    // and the execution is waiting *after* the service task
    Job continuationJob = managementService.createJobQuery().singleResult();
    assertNotNull(continuationJob);

    // but the process end listeners have not been invoked yet
    assertNull(runtimeService.getVariable(pi.getId(), "process-listenerEndInvoked"));

    // if we execute the job, the process instance ends.
    managementService.executeJob(continuationJob.getId());
    assertProcessEnded(pi.getId());

  }

  @Deployment
  public void testAsyncAfterInNestedWithoutTransition() {

    // start process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    // the service task is completely invoked
    assertListenerStartInvoked(pi);
    assertBehaviorInvoked(pi);
    assertListenerEndInvoked(pi);

    // and the execution is waiting *after* the service task
    Job continuationJob = managementService.createJobQuery().singleResult();
    assertNotNull(continuationJob);

    // but the subprocess end listeners have not been invoked yet
    assertNull(runtimeService.getVariable(pi.getId(), "subprocess-listenerEndInvoked"));

    // if we execute the job, the listeners are invoked;
    managementService.executeJob(continuationJob.getId());
    assertTrue((Boolean)runtimeService.getVariable(pi.getId(), "subprocess-listenerEndInvoked"));

  }

  @Deployment
  public void testAsyncAfterManualTask() {
    // start process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testManualTask");

    // listeners should be fired by now
    assertListenerStartInvoked(pi);
    assertListenerEndInvoked(pi);

    // the process should wait *after* the catch event
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);

    // if the waiting job is executed, the process instance should end
    managementService.executeJob(job.getId());
    assertProcessEnded(pi.getId());
  }

  @Deployment
  public void testAsyncAfterAndBeforeManualTask() {
    // start process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testManualTask");

    // the service task is not yet invoked
    assertNotListenerStartInvoked(pi);
    assertNotListenerEndInvoked(pi);

    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);

    // if the job is executed
    managementService.executeJob(job.getId());

    // the manual task is invoked
    assertListenerStartInvoked(pi);
    assertListenerEndInvoked(pi);

    // and now the process is waiting *after* the manual task
    job = managementService.createJobQuery().singleResult();
    assertNotNull(job);

    // after executing the waiting job, the process instance will end
    managementService.executeJob(job.getId());
    assertProcessEnded(pi.getId());
  }

  @Deployment
  public void testAsyncAfterIntermediateCatchEvent() {
    // start process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testIntermediateCatchEvent");

    // the intermediate catch event is waiting for its message
    runtimeService.correlateMessage("testMessage1");

    // listeners should be fired by now
    assertListenerStartInvoked(pi);
    assertListenerEndInvoked(pi);

    // the process should wait *after* the catch event
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);

    // if the waiting job is executed, the process instance should end
    managementService.executeJob(job.getId());
    assertProcessEnded(pi.getId());
  }

  @Deployment
  public void testAsyncAfterAndBeforeIntermediateCatchEvent() {

    // start process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testIntermediateCatchEvent");

    // check that no listener is invoked by now
    assertNotListenerStartInvoked(pi);
    assertNotListenerEndInvoked(pi);

    // the process is waiting before the message event
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);

    // execute job to get to the message event
    executeAvailableJobs();

    // now we need to trigger the message to proceed
    runtimeService.correlateMessage("testMessage1");

    // now the listener should be invoked
    assertListenerStartInvoked(pi);
    assertListenerEndInvoked(pi);

    // and now the process is waiting *after* the intermediate catch event
    job = managementService.createJobQuery().singleResult();
    assertNotNull(job);

    // after executing the waiting job, the process instance will end
    managementService.executeJob(job.getId());
    assertProcessEnded(pi.getId());
  }

  @Deployment
  public void testAsyncAfterIntermediateThrowEvent() {
    // start process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testIntermediateThrowEvent");

    // listeners should be fired by now
    assertListenerStartInvoked(pi);
    assertListenerEndInvoked(pi);

    // the process should wait *after* the throw event
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);

    // if the waiting job is executed, the process instance should end
    managementService.executeJob(job.getId());
    assertProcessEnded(pi.getId());
  }

  @Deployment
  public void testAsyncAfterAndBeforeIntermediateThrowEvent() {
    // start process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testIntermediateThrowEvent");

    // the throw event is not yet invoked
    assertNotListenerStartInvoked(pi);
    assertNotListenerEndInvoked(pi);

    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);

    // if the job is executed
    managementService.executeJob(job.getId());

    // the listeners are invoked
    assertListenerStartInvoked(pi);
    assertListenerEndInvoked(pi);

    // and now the process is waiting *after* the throw event
    job = managementService.createJobQuery().singleResult();
    assertNotNull(job);

    // after executing the waiting job, the process instance will end
    managementService.executeJob(job.getId());
    assertProcessEnded(pi.getId());
  }

  @Deployment
  public void testAsyncAfterInclusiveGateway() {
    // start process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testInclusiveGateway");

    // listeners should be fired
    assertListenerStartInvoked(pi);
    assertListenerEndInvoked(pi);

    // the process should wait *after* the gateway
    assertEquals(2, managementService.createJobQuery().active().count());

    executeAvailableJobs();

    // if the waiting job is executed there should be 2 user tasks
    TaskQuery taskQuery = taskService.createTaskQuery();
    assertEquals(2, taskQuery.active().count());

    // finish tasks
    List<Task> tasks = taskQuery.active().list();
    for(Task task : tasks) {
      taskService.complete(task.getId());
    }

    assertProcessEnded(pi.getProcessInstanceId());

  }

  @Deployment
  public void testAsyncAfterAndBeforeInclusiveGateway() {
    // start process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testInclusiveGateway");

    // no listeners are fired:
    assertNotListenerStartInvoked(pi);
    assertNotListenerEndInvoked(pi);

    // we should wait *before* the gateway:
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);

    // after executing the gateway:
    managementService.executeJob(job.getId());

    // the listeners are fired:
    assertListenerStartInvoked(pi);
    assertListenerEndInvoked(pi);

    // and we will wait *after* the gateway:
    List<Job> jobs = managementService.createJobQuery().active().list();
    assertEquals(2, jobs.size());
  }

  @Deployment
  public void testAsyncAfterExclusiveGateway() {
    // start process instance with variables
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("flow", false);

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testExclusiveGateway", variables);

    // listeners should be fired
    assertListenerStartInvoked(pi);
    assertListenerEndInvoked(pi);

    // the process should wait *after* the gateway
    assertEquals(1, managementService.createJobQuery().active().count());

    executeAvailableJobs();

    // if the waiting job is executed there should be 2 user tasks
    TaskQuery taskQuery = taskService.createTaskQuery();
    assertEquals(1, taskQuery.active().count());

    // finish tasks
    List<Task> tasks = taskQuery.active().list();
    for(Task task : tasks) {
      taskService.complete(task.getId());
    }

    assertProcessEnded(pi.getProcessInstanceId());
  }

  @Deployment
  public void testAsyncAfterAndBeforeExclusiveGateway() {
    // start process instance with variables
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("flow", false);

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testExclusiveGateway", variables);

    // no listeners are fired:
    assertNotListenerStartInvoked(pi);
    assertNotListenerEndInvoked(pi);

    // we should wait *before* the gateway:
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);

    // after executing the gateway:
    managementService.executeJob(job.getId());

    // the listeners are fired:
    assertListenerStartInvoked(pi);
    assertListenerEndInvoked(pi);

    // and we will wait *after* the gateway:
    assertEquals(1, managementService.createJobQuery().active().count());
  }
  /**
   * Test for CAM-2518: Fixes an issue that creates an infinite loop when using
   * asyncAfter together with an execution listener on sequence flow event "take".
   * So the only required assertion here is that the process executes successfully.
   */
  @Deployment
  public void testAsyncAfterWithExecutionListener() {
    // given an async after job and an execution listener on that task
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);

    assertNotListenerTakeInvoked(processInstance);

    // when the job is executed
    managementService.executeJob(job.getId());

    // then the process should advance and not recreate the job
    job = managementService.createJobQuery().singleResult();
    assertNull(job);

    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    assertListenerTakeInvoked(processInstance);
  }


  protected void assertListenerStartInvoked(Execution e) {
    assertTrue((Boolean) runtimeService.getVariable(e.getId(), "listenerStartInvoked"));
  }

  protected void assertListenerTakeInvoked(Execution e) {
    assertTrue((Boolean) runtimeService.getVariable(e.getId(), "listenerTakeInvoked"));
  }

  protected void assertListenerEndInvoked(Execution e) {
    assertTrue((Boolean) runtimeService.getVariable(e.getId(), "listenerEndInvoked"));
  }

  protected void assertBehaviorInvoked(Execution e) {
    assertTrue((Boolean) runtimeService.getVariable(e.getId(), "behaviorInvoked"));
  }

  protected void assertNotListenerStartInvoked(Execution e) {
    assertNull(runtimeService.getVariable(e.getId(), "listenerStartInvoked"));
  }

  protected void assertNotListenerTakeInvoked(Execution e) {
    assertNull(runtimeService.getVariable(e.getId(), "listenerTakeInvoked"));
  }

  protected void assertNotListenerEndInvoked(Execution e) {
    assertNull(runtimeService.getVariable(e.getId(), "listenerEndInvoked"));
  }

  protected void assertNotBehaviorInvoked(Execution e) {
    assertNull(runtimeService.getVariable(e.getId(), "behaviorInvoked"));
  }

}
