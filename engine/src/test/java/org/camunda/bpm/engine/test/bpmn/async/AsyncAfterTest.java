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
package org.camunda.bpm.engine.test.bpmn.async;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.camunda.bpm.engine.ParseException;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperation;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.bpmn.event.error.ThrowBpmnErrorDelegate;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Daniel Meyer
 * @author Stefan Hentschel
 *
 */
public class AsyncAfterTest extends PluggableProcessEngineTest {

  @Test
  public void testTransitionIdRequired() {

    // if an outgoing sequence flow has no id, we cannot use it in asyncAfter
    try {
      repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/async/AsyncAfterTest.testTransitionIdRequired.bpmn20.xml")
        .deploy();
      fail("Exception expected");
    } catch (ParseException e) {
      testRule.assertTextPresent("Sequence flow with sourceRef='service' must have an id, activity with id 'service' uses 'asyncAfter'.", e.getMessage());
      assertThat(e.getResorceReports().get(0).getErrors().get(0).getElementIds()).containsExactly("service");
    }

  }

  @Deployment
  @Test
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
    testRule.assertProcessEnded(pi.getId());
  }

  @Deployment
  @Test
  public void testAsyncAfterMultiInstanceUserTask() {
    // start process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process");

    List<Task> list = taskService.createTaskQuery().list();
    // multiinstance says three in the bpmn
    assertThat(list).hasSize(3);

    for (Task task : list) {
      taskService.complete(task.getId());
    }

    testRule.waitForJobExecutorToProcessAllJobs(TimeUnit.MILLISECONDS.convert(5L, TimeUnit.SECONDS));

    testRule.assertProcessEnded(pi.getId());
  }

  @Deployment
  @Test
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
    testRule.assertProcessEnded(pi.getId());
  }

  @Deployment
  @Test
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
  @Test
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
  @Test
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
    testRule.assertProcessEnded(pi.getId());

  }

  @Deployment
  @Test
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
  @Test
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
    testRule.assertProcessEnded(pi.getId());
  }

  @Deployment
  @Test
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
    testRule.assertProcessEnded(pi.getId());
  }

  @Deployment
  @Test
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
    testRule.assertProcessEnded(pi.getId());
  }

  @Deployment
  @Test
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
    testRule.executeAvailableJobs();

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
    testRule.assertProcessEnded(pi.getId());
  }

  @Deployment
  @Test
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
    testRule.assertProcessEnded(pi.getId());
  }

  @Deployment
  @Test
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
    testRule.assertProcessEnded(pi.getId());
  }

  @Deployment
  @Test
  public void testAsyncAfterInclusiveGateway() {
    // start process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testInclusiveGateway");

    // listeners should be fired
    assertListenerStartInvoked(pi);
    assertListenerEndInvoked(pi);

    // the process should wait *after* the gateway
    assertEquals(2, managementService.createJobQuery().active().count());

    testRule.executeAvailableJobs();

    // if the waiting job is executed there should be 2 user tasks
    TaskQuery taskQuery = taskService.createTaskQuery();
    assertEquals(2, taskQuery.active().count());

    // finish tasks
    List<Task> tasks = taskQuery.active().list();
    for(Task task : tasks) {
      taskService.complete(task.getId());
    }

    testRule.assertProcessEnded(pi.getProcessInstanceId());

  }

  @Deployment
  @Test
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
  @Test
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

    testRule.executeAvailableJobs();

    // if the waiting job is executed there should be 2 user tasks
    TaskQuery taskQuery = taskService.createTaskQuery();
    assertEquals(1, taskQuery.active().count());

    // finish tasks
    List<Task> tasks = taskQuery.active().list();
    for(Task task : tasks) {
      taskService.complete(task.getId());
    }

    testRule.assertProcessEnded(pi.getProcessInstanceId());
  }

  @Deployment
  @Test
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
  @Test
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

  @Deployment
  @Test
  public void testAsyncAfterOnParallelGatewayFork() {
    String configuration = PvmAtomicOperation.TRANSITION_NOTIFY_LISTENER_TAKE.getCanonicalName();
    String config1 = configuration + "$afterForkFlow1";
    String config2 = configuration + "$afterForkFlow2";

    runtimeService.startProcessInstanceByKey("process");

    // there are two jobs
    List<Job> jobs = managementService.createJobQuery().list();
    assertEquals(2, jobs.size());
    Job jobToExecute = fetchFirstJobByHandlerConfiguration(jobs, config1);
    assertNotNull(jobToExecute);
    managementService.executeJob(jobToExecute.getId());

    Task task1 = taskService.createTaskQuery().taskDefinitionKey("theTask1").singleResult();
    assertNotNull(task1);

    // there is one left
    jobs = managementService.createJobQuery().list();
    assertEquals(1, jobs.size());
    jobToExecute = fetchFirstJobByHandlerConfiguration(jobs, config2);
    managementService.executeJob(jobToExecute.getId());

    Task task2 = taskService.createTaskQuery().taskDefinitionKey("theTask2").singleResult();
    assertNotNull(task2);

    assertEquals(2, taskService.createTaskQuery().count());
  }

  @Deployment
  @Test
  public void testAsyncAfterParallelMultiInstanceWithServiceTask() {
    // start process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    // listeners and behavior should be invoked by now
    assertListenerStartInvoked(pi);
    assertBehaviorInvoked(pi, 5);
    assertListenerEndInvoked(pi);

    // the process should wait *after* execute all service tasks
    testRule.executeAvailableJobs(1);

    testRule.assertProcessEnded(pi.getId());
  }

  @Deployment
  @Test
  public void testAsyncAfterServiceWrappedInParallelMultiInstance(){
    // start process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    // listeners and behavior should be invoked by now
    assertListenerStartInvoked(pi);
    assertBehaviorInvoked(pi, 5);
    assertListenerEndInvoked(pi);

    // the process should wait *after* execute each service task wrapped in the multi-instance body
    assertEquals(5L, managementService.createJobQuery().count());
    // execute all jobs - one for each service task
    testRule.executeAvailableJobs(5);

    testRule.assertProcessEnded(pi.getId());
  }

  @Deployment
  @Test
  public void testAsyncAfterServiceWrappedInSequentialMultiInstance(){
    // start process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    // listeners and behavior should be invoked by now
    assertListenerStartInvoked(pi);
    assertBehaviorInvoked(pi, 1);
    assertListenerEndInvoked(pi);

    // the process should wait *after* execute each service task step-by-step
    assertEquals(1L, managementService.createJobQuery().count());
    // execute all jobs - one for each service task wrapped in the multi-instance body
    testRule.executeAvailableJobs(5);

    // behavior should be invoked for each service task
    assertBehaviorInvoked(pi, 5);

    // the process should wait on user task after execute all service tasks
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    taskService.complete(task.getId());

    testRule.assertProcessEnded(pi.getId());
  }

  @Deployment
  @Ignore
  @Test
  public void testAsyncAfterOnParallelGatewayJoin() {
    String configuration = PvmAtomicOperation.ACTIVITY_END.getCanonicalName();

    runtimeService.startProcessInstanceByKey("process");

    // there are three jobs
    List<Job> jobs = managementService.createJobQuery().list();
    assertEquals(3, jobs.size());
    Job jobToExecute = fetchFirstJobByHandlerConfiguration(jobs, configuration);
    assertNotNull(jobToExecute);
    managementService.executeJob(jobToExecute.getId());

    // there are two jobs left
    jobs = managementService.createJobQuery().list();
    assertEquals(2, jobs.size());
    jobToExecute = fetchFirstJobByHandlerConfiguration(jobs, configuration);
    managementService.executeJob(jobToExecute.getId());

    // there is one job left
    jobToExecute = managementService.createJobQuery().singleResult();
    assertNotNull(jobToExecute);
    managementService.executeJob(jobToExecute.getId());

    // the process should stay in the user task
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
  }

  @Deployment
  @Test
  public void testAsyncAfterBoundaryEvent() {
    // given process instance
    runtimeService.startProcessInstanceByKey("Process");

    // assume
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    // when we trigger the event
    runtimeService.correlateMessage("foo");

    // then
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);

    task = taskService.createTaskQuery().singleResult();
    assertNull(task);
  }

  @Deployment
  @Test
  public void testAsyncBeforeBoundaryEvent() {
    // given process instance
    runtimeService.startProcessInstanceByKey("Process");

    // assume
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    // when we trigger the event
    runtimeService.correlateMessage("foo");

    // then
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);

    task = taskService.createTaskQuery().singleResult();
    assertNull(task);
  }

  @Test
  public void testAsyncAfterErrorEvent() {
    // given
    BpmnModelInstance instance = Bpmn.createExecutableProcess("process")
      .startEvent()
      .serviceTask("servTask")
        .camundaClass(ThrowBpmnErrorDelegate.class)
      .boundaryEvent()
        .camundaAsyncAfter(true)
        .camundaFailedJobRetryTimeCycle("R10/PT10S")
        .errorEventDefinition()
        .errorEventDefinitionDone()
      .serviceTask()
        .camundaClass("foo")
      .endEvent()
      .moveToActivity("servTask")
      .endEvent().done();
   testRule.deploy(instance);

    runtimeService.startProcessInstanceByKey("process");

    Job job = managementService.createJobQuery().singleResult();

   // when job fails
    try {
      managementService.executeJob(job.getId());
    } catch (Exception e) {
      // ignore
    }

    // then
    job = managementService.createJobQuery().singleResult();
    Assert.assertEquals(9, job.getRetries());
  }

  protected Job fetchFirstJobByHandlerConfiguration(List<Job> jobs, String configuration) {
    for (Job job : jobs) {
      JobEntity jobEntity = (JobEntity) job;
      String jobConfig = jobEntity.getJobHandlerConfigurationRaw();
      if (configuration.equals(jobConfig)) {
        return job;
      }
    }

    return null;
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

  private void assertBehaviorInvoked(ProcessInstance pi, int times) {
    Long behaviorInvoked = (Long) runtimeService.getVariable(pi.getId(), "behaviorInvoked");
    assertNotNull("behavior was not invoked", behaviorInvoked);
    assertEquals(times , behaviorInvoked.intValue());

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
