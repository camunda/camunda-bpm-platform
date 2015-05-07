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

import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.describeActivityInstanceTree;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.examples.bpmn.executionlistener.RecorderExecutionListener;
import org.camunda.bpm.engine.variable.Variables;

/**
 *
 * @author Daniel Meyer
 * @author Stefan Hentschel
 */
public class AsyncTaskTest extends PluggableProcessEngineTestCase {

  public static boolean INVOCATION;
  public static int NUM_INVOCATIONS = 0;

  @Deployment
  public void testAsyncServiceNoListeners() {
    INVOCATION = false;
    // start process
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("asyncService");

    // now we have one transition instance below the process instance:
    ActivityInstance activityInstance = runtimeService.getActivityInstance(processInstance.getId());
    assertEquals(1, activityInstance.getChildTransitionInstances().length);
    assertEquals(0, activityInstance.getChildActivityInstances().length);

    assertNotNull(activityInstance.getChildTransitionInstances()[0]);

    // now there should be one job in the database:
    assertEquals(1, managementService.createJobQuery().count());
    // the service was not invoked:
    assertFalse(INVOCATION);

    executeAvailableJobs();

    // the service was invoked
    assertTrue(INVOCATION);
    // and the job is done
    assertEquals(0, managementService.createJobQuery().count());
  }

  @Deployment
  public void testAsyncServiceListeners() {
    String pid = runtimeService.startProcessInstanceByKey("asyncService").getProcessInstanceId();
    assertEquals(1, managementService.createJobQuery().count());
    // the listener was not yet invoked:
    assertNull(runtimeService.getVariable(pid, "listener"));

    executeAvailableJobs();

    assertEquals(0, managementService.createJobQuery().count());
  }

  @Deployment
  public void testAsyncServiceConcurrent() {
    INVOCATION = false;
    // start process
    runtimeService.startProcessInstanceByKey("asyncService");
    // now there should be one job in the database:
    assertEquals(1, managementService.createJobQuery().count());
    // the service was not invoked:
    assertFalse(INVOCATION);

    executeAvailableJobs();

    // the service was invoked
    assertTrue(INVOCATION);
    // and the job is done
    assertEquals(0, managementService.createJobQuery().count());
  }

  @Deployment
  public void testAsyncServiceSequentialMultiInstance() {
    NUM_INVOCATIONS = 0;
    // start process
    runtimeService.startProcessInstanceByKey("asyncService");
    // now there should be one job for the multi-instance body:
    assertEquals(1, managementService.createJobQuery().count());
    // the service was not invoked:
    assertEquals(0, NUM_INVOCATIONS);

    executeAvailableJobs();

    // the service was invoked
    assertEquals(5, NUM_INVOCATIONS);
    // and the job is done
    assertEquals(0, managementService.createJobQuery().count());
  }


  @Deployment
  public void testAsyncServiceParallelMultiInstance() {
    NUM_INVOCATIONS = 0;
    // start process
    runtimeService.startProcessInstanceByKey("asyncService");
    // now there should be one job for the multi-instance body:
    assertEquals(1, managementService.createJobQuery().count());
    // the service was not invoked:
    assertEquals(0, NUM_INVOCATIONS);

    executeAvailableJobs();

    // the service was invoked
    assertEquals(5, NUM_INVOCATIONS);
    // and the job is done
    assertEquals(0, managementService.createJobQuery().count());
  }

  @Deployment
  public void testFailingAsyncServiceTimer() {
    // start process
    runtimeService.startProcessInstanceByKey("asyncService");
    // now there should be one job in the database, and it is a message
    assertEquals(1, managementService.createJobQuery().count());
    Job job = managementService.createJobQuery().singleResult();
    if(!(job instanceof MessageEntity)) {
      fail("the job must be a message");
    }

    executeAvailableJobs();

    // the service failed: the execution is still sitting in the service task:
    Execution execution = runtimeService.createExecutionQuery().singleResult();
    assertNotNull(execution);
    assertEquals("service", runtimeService.getActiveActivityIds(execution.getId()).get(0));

    // there is still a single job because the timer was created in the same transaction as the
    // service was executed (which rolled back)
    assertEquals(1, managementService.createJobQuery().count());

    runtimeService.deleteProcessInstance(execution.getId(), "dead");
  }

  // TODO: Think about this:
  @Deployment
  public void FAILING_testFailingAsyncServiceTimer() {
    // start process
    runtimeService.startProcessInstanceByKey("asyncService");
    // now there are two jobs the message and a timer:
    assertEquals(2, managementService.createJobQuery().count());

    // let 'max-retires' on the message be reached
    executeAvailableJobs();

    // the service failed: the execution is still sitting in the service task:
    Execution execution = runtimeService.createExecutionQuery().singleResult();
    assertNotNull(execution);
    assertEquals("service", runtimeService.getActiveActivityIds(execution.getId()).get(0));

    // there are two jobs, the message and the timer (the message will not be retried anymore, max retires is reached.)
    assertEquals(2, managementService.createJobQuery().count());

    // now the timer triggers:
    ClockUtil.setCurrentTime(new Date(System.currentTimeMillis()+10000));
    executeAvailableJobs();

    // and we are done:
    assertNull(runtimeService.createExecutionQuery().singleResult());
    // and there are no more jobs left:
    assertEquals(0, managementService.createJobQuery().count());

  }

  @Deployment
  public void testAsyncServiceSubProcessTimer() {
    INVOCATION = false;
    // start process
    runtimeService.startProcessInstanceByKey("asyncService");
    // now there should be two jobs in the database:
    assertEquals(2, managementService.createJobQuery().count());
    // the service was not invoked:
    assertFalse(INVOCATION);

    Job job = managementService.createJobQuery().messages().singleResult();
    managementService.executeJob(job.getId());

    // the service was invoked
    assertTrue(INVOCATION);
    // both the timer and the message are cancelled
    assertEquals(0, managementService.createJobQuery().count());

  }

  @Deployment
  public void testAsyncServiceSubProcess() {
    // start process
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("asyncService");

    assertEquals(1, managementService.createJobQuery().count());

    ActivityInstance activityInstance = runtimeService.getActivityInstance(processInstance.getId());

    assertThat(activityInstance).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .transition("subProcess")
      .done());

    executeAvailableJobs();

    // both the timer and the message are cancelled
    assertEquals(0, managementService.createJobQuery().count());

  }

  @Deployment
  public void testAsyncTask() {
    // start process
    runtimeService.startProcessInstanceByKey("asyncTask");
    // now there should be one job in the database:
    assertEquals(1, managementService.createJobQuery().count());

    executeAvailableJobs();

    // the job is done
    assertEquals(0, managementService.createJobQuery().count());
  }

  @Deployment
  public void testAsyncScript() {
    // start process
    runtimeService.startProcessInstanceByKey("asyncScript").getProcessInstanceId();
    // now there should be one job in the database:
    assertEquals(1, managementService.createJobQuery().count());
    // the script was not invoked:
    String eid = runtimeService.createExecutionQuery().singleResult().getId();
    assertNull(runtimeService.getVariable(eid, "invoked"));

    executeAvailableJobs();

    // and the job is done
    assertEquals(0, managementService.createJobQuery().count());

    // the script was invoked
    assertEquals("true", runtimeService.getVariable(eid, "invoked"));

    runtimeService.signal(eid);
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/bpmn/async/AsyncTaskTest.testAsyncCallActivity.bpmn20.xml",
          "org/camunda/bpm/engine/test/bpmn/async/AsyncTaskTest.testAsyncServiceNoListeners.bpmn20.xml"})
  public void testAsyncCallActivity() {
    // start process
    runtimeService.startProcessInstanceByKey("asyncCallactivity");
    // now there should be one job in the database:
    assertEquals(1, managementService.createJobQuery().count());

    executeAvailableJobs();

    assertEquals(0, managementService.createJobQuery().count());

  }

  @Deployment
  public void testAsyncUserTask() {
    // start process
    String pid = runtimeService.startProcessInstanceByKey("asyncUserTask").getProcessInstanceId();
    // now there should be one job in the database:
    assertEquals(1, managementService.createJobQuery().count());
    // the listener was not yet invoked:
    assertNull(runtimeService.getVariable(pid, "listener"));
    // there is no usertask
    assertNull(taskService.createTaskQuery().singleResult());

    executeAvailableJobs();
    // the listener was now invoked:
    assertNotNull(runtimeService.getVariable(pid, "listener"));

    // there is a usertask
    assertNotNull(taskService.createTaskQuery().singleResult());
    // and no more job
    assertEquals(0, managementService.createJobQuery().count());

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);

  }

  @Deployment
  public void testAsyncManualTask() {
    // start PI
    String pid = runtimeService.startProcessInstanceByKey("asyncManualTask").getProcessInstanceId();

    // now there should be one job in the database:
    assertEquals(1, managementService.createJobQuery().count());
    // the listener was not yet invoked:
    assertNull(runtimeService.getVariable(pid, "listener"));
    // there is no manual Task
    assertNull(taskService.createTaskQuery().singleResult());

    executeAvailableJobs();

    // the listener was invoked now:
    assertNotNull(runtimeService.getVariable(pid, "listener"));
    // there isn't a job anymore:
    assertEquals(0, managementService.createJobQuery().count());
    // now there is a userTask
    assertNotNull(taskService.createTaskQuery().singleResult());

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);
  }

  @Deployment
  public void testAsyncIntermediateCatchEvent() {
    // start PI
    String pid = runtimeService.startProcessInstanceByKey("asyncIntermediateCatchEvent").getProcessInstanceId();

    // now there is 1 job in the database:
    assertEquals(1, managementService.createJobQuery().count());
    // the listener was not invoked now:
    assertNull(runtimeService.getVariable(pid, "listener"));
    // there is no intermediate catch event:
    assertNull(taskService.createTaskQuery().singleResult());

    executeAvailableJobs();
    runtimeService.correlateMessage("testMessage1");

    // the listener was now invoked:
    assertNotNull(runtimeService.getVariable(pid, "listener"));
    // there isn't a job anymore
    assertEquals(0, managementService.createJobQuery().count());
    // now there is a userTask
    assertNotNull(taskService.createTaskQuery().singleResult());

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);

  }

  @Deployment
  public void testAsyncIntermediateThrowEvent() {
    // start PI
    String pid = runtimeService.startProcessInstanceByKey("asyncIntermediateThrowEvent").getProcessInstanceId();

    // now there is 1 job in the database:
    assertEquals(1, managementService.createJobQuery().count());
    // the listener was not invoked now:
    assertNull(runtimeService.getVariable(pid, "listener"));
    // there is no intermediate throw event:
    assertNull(taskService.createTaskQuery().singleResult());

    executeAvailableJobs();

    // the listener was now invoked:
    assertNotNull(runtimeService.getVariable(pid, "listener"));
    // there isn't a job anymore
    assertEquals(0, managementService.createJobQuery().count());
    // now there is a userTask
    assertNotNull(taskService.createTaskQuery().singleResult());

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);
  }

  @Deployment
  public void testAsyncExclusiveGateway() {
    // The test needs variables to work properly
    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put("flow", false);

    // start PI
    String pid = runtimeService.startProcessInstanceByKey("asyncExclusiveGateway", variables).getProcessInstanceId();

    // now there is 1 job in the database:
    assertEquals(1, managementService.createJobQuery().count());
    // the listener was not invoked now:
    assertNull(runtimeService.getVariable(pid, "listener"));
    // there is no gateway:
    assertNull(taskService.createTaskQuery().singleResult());

    executeAvailableJobs();

    // the listener was now invoked:
    assertNotNull(runtimeService.getVariable(pid, "listener"));
    // there isn't a job anymore
    assertEquals(0, managementService.createJobQuery().count());
    // now there is a userTask
    assertNotNull(taskService.createTaskQuery().singleResult());

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);
  }

  @Deployment
  public void testAsyncInclusiveGateway() {
    // start PI
    String pid = runtimeService.startProcessInstanceByKey("asyncInclusiveGateway").getProcessInstanceId();

    // now there is 1 job in the database:
    assertEquals(1, managementService.createJobQuery().count());
    // the listener was not invoked now:
    assertNull(runtimeService.getVariable(pid, "listener"));
    // there is no gateway:
    assertNull(taskService.createTaskQuery().singleResult());

    executeAvailableJobs();

    // the listener was now invoked:
    assertNotNull(runtimeService.getVariable(pid, "listener"));
    // there isn't a job anymore
    assertEquals(0, managementService.createJobQuery().count());
    // now there are 2 user tasks
    List<Task> list = taskService.createTaskQuery().list();
    assertEquals(2, list.size());

    // complete these tasks and finish the process instance
    for(Task task: list) {
      taskService.complete(task.getId());
    }
  }

  @Deployment
  public void testAsyncEventGateway() {
    // start PI
    String pid = runtimeService.startProcessInstanceByKey("asyncEventGateway").getProcessInstanceId();

    // now there is a job in the database
    assertEquals(1, managementService.createJobQuery().count());
    // the listener was not invoked now:
    assertNull(runtimeService.getVariable(pid, "listener"));
    // there is no task:
    assertNull(taskService.createTaskQuery().singleResult());

    executeAvailableJobs();

    // the listener was now invoked:
    assertNotNull(runtimeService.getVariable(pid, "listener"));
    // there isn't a job anymore
    assertEquals(0, managementService.createJobQuery().count());

    // correlate Message
    runtimeService.correlateMessage("testMessageDef1");

    // now there is a userTask
    assertNotNull(taskService.createTaskQuery().singleResult());

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);
  }

  /**
   * CAM-3707
   */
  @Deployment
  public void FAILING_testDeleteShouldNotInvokeListeners() {
    RecorderExecutionListener.clear();

    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("asyncListener",
        Variables.createVariables().putValue("listener", new RecorderExecutionListener()));
    assertEquals(1, managementService.createJobQuery().count());

    // when deleting the process instance
    runtimeService.deleteProcessInstance(instance.getId(), "");

    // then no listeners for the async activity should have been invoked because
    // it was not active yet
    assertEquals(0, RecorderExecutionListener.getRecordedEvents().size());

    RecorderExecutionListener.clear();
  }

  /**
   * CAM-3708
   */
  @Deployment
  public void FAILING_testDeleteShouldNotInvokeOutputMapping() {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("asyncOutputMapping");
    assertEquals(1, managementService.createJobQuery().count());

    // when
    runtimeService.deleteProcessInstance(instance.getId(), "");

    // then the output mapping has not been executed because the
    // activity was not active yet
    if (processEngineConfiguration.getHistoryLevel().getId() >= HistoryLevel.HISTORY_LEVEL_AUDIT.getId()) {
      assertEquals(0, historyService.createHistoricVariableInstanceQuery().count());
    }

  }

}
