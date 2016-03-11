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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
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
import org.camunda.bpm.engine.test.bpmn.executionlistener.RecorderExecutionListener;
import org.camunda.bpm.engine.test.bpmn.executionlistener.RecorderExecutionListener.RecordedEvent;
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
  public void testAsyncSequentialMultiInstanceWithServiceTask() {
    NUM_INVOCATIONS = 0;
    // start process
    runtimeService.startProcessInstanceByKey("asyncService");

    // the service was not invoked:
    assertEquals(0, NUM_INVOCATIONS);

    // now there should be one job for the multi-instance body to execute:
    executeAvailableJobs(1);

    // the service was invoked
    assertEquals(5, NUM_INVOCATIONS);
    // and the job is done
    assertEquals(0, managementService.createJobQuery().count());
  }


  @Deployment
  public void testAsyncParallelMultiInstanceWithServiceTask() {
    NUM_INVOCATIONS = 0;
    // start process
    runtimeService.startProcessInstanceByKey("asyncService");

    // the service was not invoked:
    assertEquals(0, NUM_INVOCATIONS);

    // now there should be one job for the multi-instance body to execute:
    executeAvailableJobs(1);

    // the service was invoked
    assertEquals(5, NUM_INVOCATIONS);
    // and the job is done
    assertEquals(0, managementService.createJobQuery().count());
  }

  @Deployment
  public void testAsyncServiceWrappedInSequentialMultiInstance() {
    NUM_INVOCATIONS = 0;
    // start process
    runtimeService.startProcessInstanceByKey("asyncService");

    // the service was not invoked:
    assertEquals(0, NUM_INVOCATIONS);

    // now there should be one job for the first service task wrapped in the multi-instance body:
    assertEquals(1, managementService.createJobQuery().count());
    // execute all jobs - one for each service task:
    executeAvailableJobs(5);

    // the service was invoked
    assertEquals(5, NUM_INVOCATIONS);
    // and the job is done
    assertEquals(0, managementService.createJobQuery().count());
  }

  @Deployment
  public void testAsyncServiceWrappedInParallelMultiInstance() {
    NUM_INVOCATIONS = 0;
    // start process
    runtimeService.startProcessInstanceByKey("asyncService");

    // the service was not invoked:
    assertEquals(0, NUM_INVOCATIONS);

    // now there should be one job for each service task wrapped in the multi-instance body:
    assertEquals(5, managementService.createJobQuery().count());
    // execute all jobs:
    executeAvailableJobs(5);

    // the service was invoked
    assertEquals(5, NUM_INVOCATIONS);
    // and the job is done
    assertEquals(0, managementService.createJobQuery().count());
  }

  @Deployment
  public void testAsyncBeforeAndAfterOfServiceWrappedInParallelMultiInstance() {
    NUM_INVOCATIONS = 0;
    // start process
    runtimeService.startProcessInstanceByKey("asyncService");

    // the service was not invoked:
    assertEquals(0, NUM_INVOCATIONS);

    // now there should be one job for each service task wrapped in the multi-instance body:
    assertEquals(5, managementService.createJobQuery().count());
    // execute all jobs - one for asyncBefore and another for asyncAfter:
    executeAvailableJobs(5+5);

    // the service was invoked
    assertEquals(5, NUM_INVOCATIONS);
    // and the job is done
    assertEquals(0, managementService.createJobQuery().count());
  }

  @Deployment
  public void testAsyncBeforeSequentialMultiInstanceWithAsyncAfterServiceWrappedInMultiInstance() {
    NUM_INVOCATIONS = 0;
    // start process
    runtimeService.startProcessInstanceByKey("asyncService");

    // the service was not invoked:
    assertEquals(0, NUM_INVOCATIONS);

    // now there should be one job for the multi-instance body:
    assertEquals(1, managementService.createJobQuery().count());
    // execute all jobs - one for multi-instance body and one for each service task wrapped in the multi-instance body:
    executeAvailableJobs(1+5);

    // the service was invoked
    assertEquals(5, NUM_INVOCATIONS);
    // and the job is done
    assertEquals(0, managementService.createJobQuery().count());
  }

  protected void assertTransitionInstances(String processInstanceId, String activityId, int numInstances) {
    ActivityInstance tree = runtimeService.getActivityInstance(processInstanceId);

    assertEquals(numInstances, tree.getTransitionInstances(activityId).length);
  }

  @Deployment
  public void testAsyncBeforeAndAfterParallelMultiInstanceWithAsyncBeforeAndAfterServiceWrappedInMultiInstance() {
    NUM_INVOCATIONS = 0;
    // start process
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("asyncService");

    // the service was not invoked:
    assertEquals(0, NUM_INVOCATIONS);

    // now there should be one job for the multi-instance body:
    assertEquals(1, managementService.createJobQuery().count());
    assertTransitionInstances(processInstance.getId(), "service" + BpmnParse.MULTI_INSTANCE_BODY_ID_SUFFIX, 1);

    // when the mi body before job is executed
    Job job = managementService.createJobQuery().singleResult();
    managementService.executeJob(job.getId());

    // then there are five inner async before jobs
    List<Job> innerBeforeJobs = managementService.createJobQuery().list();
    assertEquals(5, innerBeforeJobs.size());
    assertTransitionInstances(processInstance.getId(), "service", 5);
    assertEquals(0, NUM_INVOCATIONS);

    // when executing all inner jobs
    for (Job innerBeforeJob : innerBeforeJobs) {
      managementService.executeJob(innerBeforeJob.getId());
    }
    assertEquals(5, NUM_INVOCATIONS);

    // then there are five async after jobs
    List<Job> innerAfterJobs = managementService.createJobQuery().list();
    assertEquals(5, innerAfterJobs.size());
    assertTransitionInstances(processInstance.getId(), "service", 5);

    // when executing all inner jobs
    for (Job innerAfterJob : innerAfterJobs) {
      managementService.executeJob(innerAfterJob.getId());
    }

    // then there is one mi body after job
    job = managementService.createJobQuery().singleResult();
    assertNotNull(job);
    assertTransitionInstances(processInstance.getId(), "service" + BpmnParse.MULTI_INSTANCE_BODY_ID_SUFFIX, 1);

    // when executing this job, the process ends
    managementService.executeJob(job.getId());
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/async/AsyncTaskTest.testAsyncServiceWrappedInParallelMultiInstance.bpmn20.xml")
  public void testAsyncServiceWrappedInParallelMultiInstanceActivityInstance() {
    // given a process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("asyncService");

    // when there are five jobs for the inner activity
    assertEquals(5, managementService.createJobQuery().count());

    // then they are represented in the activity instance tree by transition instances
    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    assertThat(tree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .beginScope("service#multiInstanceBody")
            .transition("service")
            .transition("service")
            .transition("service")
            .transition("service")
            .transition("service")
        .done());
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
  public void testDeleteShouldNotInvokeListeners() {
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
   * CAM-3707
   */
  @Deployment
  public void testDeleteInScopeShouldNotInvokeListeners() {
    RecorderExecutionListener.clear();

    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("asyncListenerSubProcess",
        Variables.createVariables().putValue("listener", new RecorderExecutionListener()));
    assertEquals(1, managementService.createJobQuery().count());

    // when deleting the process instance
    runtimeService.deleteProcessInstance(instance.getId(), "");

    // then the async task end listener has not been executed but the listeners of the sub
    // process and the process

    List<RecordedEvent> recordedEvents = RecorderExecutionListener.getRecordedEvents();
    assertEquals(2, recordedEvents.size());
    assertEquals("subProcess", recordedEvents.get(0).getActivityId());
    assertNull(recordedEvents.get(1).getActivityId()); // process instance end event has no activity id

    RecorderExecutionListener.clear();
  }

  /**
   * CAM-3708
   */
  @Deployment
  public void testDeleteShouldNotInvokeOutputMapping() {
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

  /**
   * CAM-3708
   */
  @Deployment
  public void testDeleteInScopeShouldNotInvokeOutputMapping() {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("asyncOutputMappingSubProcess");
    assertEquals(1, managementService.createJobQuery().count());

    // when
    runtimeService.deleteProcessInstance(instance.getId(), "");

    // then
    if (processEngineConfiguration.getHistoryLevel().getId() >= HistoryLevel.HISTORY_LEVEL_AUDIT.getId()) {
      // the output mapping of the task has not been executed because the
      // activity was not active yet
      assertEquals(0, historyService.createHistoricVariableInstanceQuery().variableName("taskOutputMappingExecuted").count());

      // but the containing sub process output mapping was executed
      assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableName("subProcessOutputMappingExecuted").count());
    }
  }

  public void testDeployAndRemoveAsyncActivity() {
    Set<String> deployments = new HashSet<String>();

    try {
      // given a deployment that contains a process called "process" with an async task "task"
      org.camunda.bpm.engine.repository.Deployment deployment1 = repositoryService
          .createDeployment()
          .addClasspathResource("org/camunda/bpm/engine/test/bpmn/async/AsyncTaskTest.testDeployAndRemoveAsyncActivity.v1.bpmn20.xml")
          .deploy();
      deployments.add(deployment1.getId());

      // when redeploying the process where that task is not contained anymore
      org.camunda.bpm.engine.repository.Deployment deployment2 = repositoryService
          .createDeployment()
          .addClasspathResource("org/camunda/bpm/engine/test/bpmn/async/AsyncTaskTest.testDeployAndRemoveAsyncActivity.v2.bpmn20.xml")
          .deploy();
      deployments.add(deployment2.getId());

      // and clearing the deployment cache (note that the equivalent of this in a real-world
      // scenario would be making the deployment with a different engine
      processEngineConfiguration.getDeploymentCache().discardProcessDefinitionCache();

      // then it should be possible to load the latest process definition
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
      assertNotNull(processInstance);

    } finally {
      for (String deploymentId : deployments) {
        repositoryService.deleteDeployment(deploymentId, true);
      }
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/bpmn/async/processWithGatewayAndTwoEndEvents.bpmn20.xml"})
  public void testGatewayWithTwoEndEventsLastJobReAssignedToParentExe() {
    String processKey = repositoryService.createProcessDefinitionQuery().singleResult().getKey();
    String processInstanceId = runtimeService.startProcessInstanceByKey(processKey).getId();

    List<Job> jobList = managementService.createJobQuery().processInstanceId(processInstanceId).list();

    // There should be two jobs
    assertNotNull(jobList);
    assertEquals(2, jobList.size());

    managementService.executeJob(jobList.get(0).getId());

    // There should be only one job left
    jobList = managementService.createJobQuery().list();
    assertEquals(1, jobList.size());

    // There should only be 1 execution left - the root execution
    assertEquals(1, runtimeService.createExecutionQuery().list().size());

    // root execution should be attached to the last job
    assertEquals(processInstanceId, jobList.get(0).getExecutionId());

    managementService.executeJob(jobList.get(0).getId());

    // There should be no more jobs
    jobList = managementService.createJobQuery().list();
    assertEquals(0, jobList.size());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/bpmn/async/processGatewayAndTwoEndEventsPlusTimer.bpmn20.xml"})
  public void testGatewayWithTwoEndEventsLastTimerReAssignedToParentExe() {
    String processKey = repositoryService.createProcessDefinitionQuery().singleResult().getKey();
    String processInstanceId = runtimeService.startProcessInstanceByKey(processKey).getId();

    List<Job> jobList = managementService.createJobQuery().processInstanceId(processInstanceId).list();

    // There should be two jobs
    assertNotNull(jobList);
    assertEquals(2, jobList.size());

    // execute timer first
    String timerId = managementService.createJobQuery().timers().singleResult().getId();
    managementService.executeJob(timerId);

    // There should be only one job left
    jobList = managementService.createJobQuery().list();
    assertEquals(1, jobList.size());

    // There should only be 1 execution left - the root execution
    assertEquals(1, runtimeService.createExecutionQuery().list().size());

    // root execution should be attached to the last job
    assertEquals(processInstanceId, jobList.get(0).getExecutionId());

    // execute service task
    managementService.executeJob(jobList.get(0).getId());

    // There should be no more jobs
    jobList = managementService.createJobQuery().list();
    assertEquals(0, jobList.size());
  }

  @Deployment
  public void FAILING_testLongProcessDefinitionKey() {
    String key = "myrealrealrealrealrealrealrealrealrealrealreallongprocessdefinitionkeyawesome";
    String processInstanceId = runtimeService.startProcessInstanceByKey(key).getId();

    Job job = managementService.createJobQuery().processInstanceId(processInstanceId).singleResult();

    assertEquals(key, job.getProcessDefinitionKey());
  }
}
