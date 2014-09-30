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
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Daniel Meyer
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

    // the service task is completely invoked
    assertListenerStartInvoked(pi);
    assertBehaviorInvoked(pi);
    assertListenerEndInvoked(pi);

    // and the execution is waiting *after* the service task
    Job continuationJob = managementService.createJobQuery().singleResult();
    assertNotNull(continuationJob);

    // if we execute the job, the process instance ends.
    managementService.executeJob(continuationJob.getId());
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

    // and the execution is waiting *before* the service task
    Job continuationJob = managementService.createJobQuery().singleResult();
    assertNotNull(continuationJob);

    // if we execute the job
    managementService.executeJob(continuationJob.getId());

    // the service task is invoked
    assertListenerStartInvoked(pi);
    assertBehaviorInvoked(pi);
    assertListenerEndInvoked(pi);

    // and now the execution is waiting *after* the service task
    continuationJob = managementService.createJobQuery().singleResult();
    assertNotNull(continuationJob);

    // if we execute the job, the process instance ends.
    managementService.executeJob(continuationJob.getId());
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
