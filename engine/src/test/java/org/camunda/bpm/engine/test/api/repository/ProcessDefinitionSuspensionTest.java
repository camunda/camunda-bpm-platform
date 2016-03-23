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
package org.camunda.bpm.engine.test.api.repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.SuspendedEntityInteractionException;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.TimerActivateProcessDefinitionHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerSuspendProcessDefinitionHandler;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
//import org.camunda.bpm.engine.impl.test.TestHelper;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.management.JobDefinitionQuery;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Daniel Meyer
 * @author Joram Barrez
 */
public class ProcessDefinitionSuspensionTest extends PluggableProcessEngineTestCase {

  @Override
  public void tearDown() throws Exception {
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        commandContext.getHistoricJobLogManager().deleteHistoricJobLogsByHandlerType(TimerActivateProcessDefinitionHandler.TYPE);
        commandContext.getHistoricJobLogManager().deleteHistoricJobLogsByHandlerType(TimerSuspendProcessDefinitionHandler.TYPE);
        return null;
      }
    });
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/repository/processOne.bpmn20.xml"})
  public void testProcessDefinitionActiveByDefault() {

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    assertFalse(processDefinition.isSuspended());

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/repository/processOne.bpmn20.xml"})
  public void testSuspendActivateProcessDefinitionById() {

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertFalse(processDefinition.isSuspended());

    // suspend
    repositoryService.suspendProcessDefinitionById(processDefinition.getId());
    processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertTrue(processDefinition.isSuspended());

    // activate
    repositoryService.activateProcessDefinitionById(processDefinition.getId());
    processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertFalse(processDefinition.isSuspended());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/repository/processOne.bpmn20.xml"})
  public void testSuspendActivateProcessDefinitionByKey() {

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertFalse(processDefinition.isSuspended());

    //suspend
    repositoryService.suspendProcessDefinitionByKey(processDefinition.getKey());
    processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertTrue(processDefinition.isSuspended());

    //activate
    repositoryService.activateProcessDefinitionByKey(processDefinition.getKey());
    processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertFalse(processDefinition.isSuspended());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/repository/processOne.bpmn20.xml"})
  public void testActivateAlreadyActiveProcessDefinition() {

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertFalse(processDefinition.isSuspended());

    try {
      repositoryService.activateProcessDefinitionById(processDefinition.getId());
      processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
      assertFalse(processDefinition.isSuspended());
    } catch (Exception e) {
      fail("Should be successful");
    }

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/repository/processOne.bpmn20.xml"})
  public void testSuspendAlreadySuspendedProcessDefinition() {

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertFalse(processDefinition.isSuspended());

    repositoryService.suspendProcessDefinitionById(processDefinition.getId());

    try {
      repositoryService.suspendProcessDefinitionById(processDefinition.getId());
      processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
      assertTrue(processDefinition.isSuspended());
    } catch (Exception e) {
      fail("Should be successful");
    }

  }

  @Deployment(resources={
          "org/camunda/bpm/engine/test/api/repository/processOne.bpmn20.xml",
          "org/camunda/bpm/engine/test/api/repository/processTwo.bpmn20.xml"
          })
  public void testQueryForActiveDefinitions() {

    // default = all definitions
    List<ProcessDefinition> processDefinitionList = repositoryService.createProcessDefinitionQuery()
      .list();
    assertEquals(2, processDefinitionList.size());

    assertEquals(2, repositoryService.createProcessDefinitionQuery().active().count());

    ProcessDefinition processDefinition = processDefinitionList.get(0);
    repositoryService.suspendProcessDefinitionById(processDefinition.getId());

    assertEquals(2, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().active().count());
  }

  @Deployment(resources={
          "org/camunda/bpm/engine/test/api/repository/processOne.bpmn20.xml",
          "org/camunda/bpm/engine/test/api/repository/processTwo.bpmn20.xml"
          })
  public void testQueryForSuspendedDefinitions() {

    // default = all definitions
    List<ProcessDefinition> processDefinitionList = repositoryService.createProcessDefinitionQuery()
      .list();
    assertEquals(2, processDefinitionList.size());

    assertEquals(2, repositoryService.createProcessDefinitionQuery().active().count());

    ProcessDefinition processDefinition = processDefinitionList.get(0);
    repositoryService.suspendProcessDefinitionById(processDefinition.getId());

    assertEquals(2, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().suspended().count());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/repository/processOne.bpmn20.xml"})
  public void testStartProcessInstanceForSuspendedProcessDefinition() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    repositoryService.suspendProcessDefinitionById(processDefinition.getId());

    // By id
    try {
      runtimeService.startProcessInstanceById(processDefinition.getId());
      fail("Exception is expected but not thrown");
    } catch(SuspendedEntityInteractionException e) {
      assertTextPresentIgnoreCase("is suspended", e.getMessage());
    }

    // By Key
    try {
      runtimeService.startProcessInstanceByKey(processDefinition.getKey());
      fail("Exception is expected but not thrown");
    } catch(SuspendedEntityInteractionException e) {
      assertTextPresentIgnoreCase("is suspended", e.getMessage());
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testContinueProcessAfterProcessDefinitionSuspend() {

    // Start Process Instance
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceByKey(processDefinition.getKey());

    // Verify one task is created
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    // Suspend process definition
    repositoryService.suspendProcessDefinitionById(processDefinition.getId());

    // Process should be able to continue
    taskService.complete(task.getId());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testSuspendProcessInstancesDuringProcessDefinitionSuspend() {

    int nrOfProcessInstances = 9;

    // Fire up a few processes for the deployed process definition
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    for (int i=0; i<nrOfProcessInstances; i++) {
      runtimeService.startProcessInstanceByKey(processDefinition.getKey());
    }
    assertEquals(nrOfProcessInstances, runtimeService.createProcessInstanceQuery().count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().suspended().count());
    assertEquals(nrOfProcessInstances, runtimeService.createProcessInstanceQuery().active().count());

    // Suspend process definitions and include process instances
    repositoryService.suspendProcessDefinitionById(processDefinition.getId(), true, null);

    // Verify all process instances are also suspended
    for (ProcessInstance processInstance : runtimeService.createProcessInstanceQuery().list()) {
      assertTrue(processInstance.isSuspended());
    }

    // Verify all process instances can't be continued
    for (Task task : taskService.createTaskQuery().list()) {
      try {
        assertTrue(task.isSuspended());
        taskService.complete(task.getId());
        fail("A suspended task shouldn't be able to be continued");
      } catch(SuspendedEntityInteractionException e) {
        assertTextPresentIgnoreCase("is suspended", e.getMessage());
      }
    }
    assertEquals(nrOfProcessInstances, runtimeService.createProcessInstanceQuery().count());
    assertEquals(nrOfProcessInstances, runtimeService.createProcessInstanceQuery().suspended().count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().active().count());

    // Activate the process definition again
    repositoryService.activateProcessDefinitionById(processDefinition.getId(), true, null);

    // Verify that all process instances can be completed
    for (Task task : taskService.createTaskQuery().list()) {
      taskService.complete(task.getId());
    }
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().suspended().count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().active().count());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testSubmitStartFormAfterProcessDefinitionSuspend() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    repositoryService.suspendProcessDefinitionById(processDefinition.getId());

    try {
      formService.submitStartFormData(processDefinition.getId(), new HashMap<String, String>());
      fail();
    } catch (ProcessEngineException e) {
      assertTextPresentIgnoreCase("is suspended", e.getMessage());
    }

    try {
      formService.submitStartFormData(processDefinition.getId(), "someKey", new HashMap<String, String>());
      fail();
    } catch (ProcessEngineException e) {
      assertTextPresentIgnoreCase("is suspended", e.getMessage());
    }

  }

  @Deployment
  public void testJobIsExecutedOnProcessDefinitionSuspend() {

    Date now = new Date();
    ClockUtil.setCurrentTime(now);

    // Suspending the process definition should not stop the execution of jobs
    // Added this test because in previous implementations, this was the case.
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceById(processDefinition.getId());
    repositoryService.suspendProcessDefinitionById(processDefinition.getId());
    assertEquals(1, managementService.createJobQuery().count());

    // The jobs should simply be executed
    Job job = managementService.createJobQuery().singleResult();
    managementService.executeJob(job.getId());
    assertEquals(0, managementService.createJobQuery().count());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testDelayedSuspendProcessDefinition() {

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    Date startTime = new Date();
    ClockUtil.setCurrentTime(startTime);

    // Suspend process definition in one week from now
    long oneWeekFromStartTime = startTime.getTime() + (7 * 24 * 60 * 60 * 1000);
    repositoryService.suspendProcessDefinitionById(processDefinition.getId(), false, new Date(oneWeekFromStartTime));

    // Verify we can just start process instances
    runtimeService.startProcessInstanceById(processDefinition.getId());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().suspended().count());

    // execute job
    Job job = managementService.createJobQuery().singleResult();
    managementService.executeJob(job.getId());

    // Try to start process instance. It should fail now.
    try {
      runtimeService.startProcessInstanceById(processDefinition.getId());
      fail();
    } catch (SuspendedEntityInteractionException e) {
      assertTextPresentIgnoreCase("suspended", e.getMessage());
    }
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().suspended().count());

    // Activate again
    repositoryService.activateProcessDefinitionById(processDefinition.getId());
    runtimeService.startProcessInstanceById(processDefinition.getId());
    assertEquals(2, runtimeService.createProcessInstanceQuery().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().suspended().count());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testDelayedSuspendProcessDefinitionIncludingProcessInstances() {

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    Date startTime = new Date();
    ClockUtil.setCurrentTime(startTime);

    // Start some process instances
    int nrOfProcessInstances = 30;
    for (int i=0; i<nrOfProcessInstances; i++) {
      runtimeService.startProcessInstanceById(processDefinition.getId());
    }

    assertEquals(nrOfProcessInstances, runtimeService.createProcessInstanceQuery().count());
    assertEquals(nrOfProcessInstances, runtimeService.createProcessInstanceQuery().active().count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().suspended().count());
    assertEquals(0, taskService.createTaskQuery().suspended().count());
    assertEquals(nrOfProcessInstances, taskService.createTaskQuery().active().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().suspended().count());

    // Suspend process definition in one week from now
    long oneWeekFromStartTime = startTime.getTime() + (7 * 24 * 60 * 60 * 1000);
    repositoryService.suspendProcessDefinitionById(processDefinition.getId(), true, new Date(oneWeekFromStartTime));

    // Verify we can start process instances
    runtimeService.startProcessInstanceById(processDefinition.getId());
    nrOfProcessInstances = nrOfProcessInstances + 1;
    assertEquals(nrOfProcessInstances, runtimeService.createProcessInstanceQuery().count());

    // execute job
    Job job = managementService.createJobQuery().singleResult();
    managementService.executeJob(job.getId());

    // Try to start process instance. It should fail now.
    try {
      runtimeService.startProcessInstanceById(processDefinition.getId());
      fail();
    } catch (SuspendedEntityInteractionException e) {
      assertTextPresentIgnoreCase("suspended", e.getMessage());
    }
    assertEquals(nrOfProcessInstances, runtimeService.createProcessInstanceQuery().count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().active().count());
    assertEquals(nrOfProcessInstances, runtimeService.createProcessInstanceQuery().suspended().count());
    assertEquals(nrOfProcessInstances, taskService.createTaskQuery().suspended().count());
    assertEquals(0, taskService.createTaskQuery().active().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().suspended().count());

    // Activate again
    repositoryService.activateProcessDefinitionById(processDefinition.getId(), true, null);
    assertEquals(nrOfProcessInstances, runtimeService.createProcessInstanceQuery().count());
    assertEquals(nrOfProcessInstances, runtimeService.createProcessInstanceQuery().active().count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().suspended().count());
    assertEquals(0, taskService.createTaskQuery().suspended().count());
    assertEquals(nrOfProcessInstances, taskService.createTaskQuery().active().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().suspended().count());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testDelayedActivateProcessDefinition() {

    Date startTime = new Date();
    ClockUtil.setCurrentTime(startTime);

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    repositoryService.suspendProcessDefinitionById(processDefinition.getId());

    // Try to start process instance. It should fail now.
    try {
      runtimeService.startProcessInstanceById(processDefinition.getId());
      fail();
    } catch (SuspendedEntityInteractionException e) {
      assertTextPresentIgnoreCase("suspended", e.getMessage());
    }
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().suspended().count());

    // Activate in a day from now
    long oneDayFromStart = startTime.getTime() + (24 * 60 * 60 * 1000);
    repositoryService.activateProcessDefinitionById(processDefinition.getId(), false, new Date(oneDayFromStart));

    // execute job
    Job job = managementService.createJobQuery().singleResult();
    managementService.executeJob(job.getId());

    // Starting a process instance should now succeed
    runtimeService.startProcessInstanceById(processDefinition.getId());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().suspended().count());
  }

  public void testSuspendMultipleProcessDefinitionsByKey () {

    // Deploy three processes
    int nrOfProcessDefinitions = 3;
    for (int i=0; i<nrOfProcessDefinitions; i++) {
      repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml").deploy();
    }
    assertEquals(nrOfProcessDefinitions, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(nrOfProcessDefinitions, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().suspended().count());

    // Suspend all process definitions with same key
    repositoryService.suspendProcessDefinitionByKey("oneTaskProcess");
    assertEquals(nrOfProcessDefinitions, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(nrOfProcessDefinitions, repositoryService.createProcessDefinitionQuery().suspended().count());

    // Activate again
    repositoryService.activateProcessDefinitionByKey("oneTaskProcess");
    assertEquals(nrOfProcessDefinitions, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(nrOfProcessDefinitions, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().suspended().count());

    // Start process instance
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // And suspend again, cascading to process instances
    repositoryService.suspendProcessDefinitionByKey("oneTaskProcess", true, null);
    assertEquals(nrOfProcessDefinitions, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(nrOfProcessDefinitions, repositoryService.createProcessDefinitionQuery().suspended().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().suspended().count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().active().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    // Clean DB
    for (org.camunda.bpm.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

  public void testDelayedSuspendMultipleProcessDefinitionsByKey () {

    Date startTime = new Date();
    ClockUtil.setCurrentTime(startTime);
    final long hourInMs = 60 * 60 * 1000;

    // Deploy five versions of the same process
    int nrOfProcessDefinitions = 5;
    for (int i=0; i<nrOfProcessDefinitions; i++) {
      repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml").deploy();
    }
    assertEquals(nrOfProcessDefinitions, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(nrOfProcessDefinitions, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().suspended().count());

    // Start process instance
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // Suspend all process definitions with same key in 2 hours from now
    repositoryService.suspendProcessDefinitionByKey("oneTaskProcess", true, new Date(startTime.getTime() + (2 * hourInMs)));
    assertEquals(nrOfProcessDefinitions, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(nrOfProcessDefinitions, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().suspended().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().active().count());

    // execute job
    Job job = managementService.createJobQuery().singleResult();
    managementService.executeJob(job.getId());

    assertEquals(nrOfProcessDefinitions, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(nrOfProcessDefinitions, repositoryService.createProcessDefinitionQuery().suspended().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().suspended().count());

    // Activate again in 5 hourse from now
    repositoryService.activateProcessDefinitionByKey("oneTaskProcess", true, new Date(startTime.getTime() + (5 * hourInMs)));
    assertEquals(nrOfProcessDefinitions, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(nrOfProcessDefinitions, repositoryService.createProcessDefinitionQuery().suspended().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().suspended().count());

    // execute job
    job = managementService.createJobQuery().singleResult();
    managementService.executeJob(job.getId());

    assertEquals(nrOfProcessDefinitions, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(nrOfProcessDefinitions, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().suspended().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().active().count());

    // Clean DB
    for (org.camunda.bpm.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/repository/ProcessDefinitionSuspensionTest.testWithOneAsyncServiceTask.bpmn"})
  public void testSuspendById_shouldSuspendJobDefinitionAndRetainJob() {
    // given

    // a process definition with a asynchronous continuation, so that there
    // exists a job definition
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // a running process instance with a failed service task
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceById(processDefinition.getId(), params);

    // when
    // the process definition will be suspended
    repositoryService.suspendProcessDefinitionById(processDefinition.getId());

    // then
    // the job definition should be suspended...
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.active().count());
    assertEquals(1, jobDefinitionQuery.suspended().count());

    JobDefinition suspendedJobDefinition = jobDefinitionQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), suspendedJobDefinition.getId());
    assertTrue(suspendedJobDefinition.isSuspended());

    // ...and the corresponding job should be still active
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.suspended().count());
    assertEquals(1, jobQuery.active().count());

    Job job = jobQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), job.getJobDefinitionId());
    assertFalse(job.isSuspended());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/repository/ProcessDefinitionSuspensionTest.testWithOneAsyncServiceTask.bpmn"})
  public void testSuspendByKey_shouldSuspendJobDefinitionAndRetainJob() {
    // given

    // a process definition with a asynchronous continuation, so that there
    // exists a job definition
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // a running process instance with a failed service task
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceById(processDefinition.getId(), params);

    // when
    // the process definition will be suspended
    repositoryService.suspendProcessDefinitionByKey(processDefinition.getKey());

    // then
    // the job definition should be suspended...
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.active().count());
    assertEquals(1, jobDefinitionQuery.suspended().count());

    JobDefinition suspendedJobDefinition = jobDefinitionQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), suspendedJobDefinition.getId());
    assertTrue(suspendedJobDefinition.isSuspended());

    // ...and the corresponding job should be still active
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.suspended().count());
    assertEquals(1, jobQuery.active().count());

    Job job = jobQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), job.getJobDefinitionId());
    assertFalse(job.isSuspended());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/repository/ProcessDefinitionSuspensionTest.testWithOneAsyncServiceTask.bpmn"})
  public void testSuspendByIdAndIncludeInstancesFlag_shouldSuspendAlsoJobDefinitionAndRetainJob() {
    // a process definition with a asynchronous continuation, so that there
    // exists a job definition
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // a running process instance with a failed service task
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceById(processDefinition.getId(), params);

    // when
    // the process definition will be suspended
    repositoryService.suspendProcessDefinitionById(processDefinition.getId(), false, null);

    // then
    // the job definition should be suspended...
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.active().count());
    assertEquals(1, jobDefinitionQuery.suspended().count());

    JobDefinition suspendedJobDefinition = jobDefinitionQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), suspendedJobDefinition.getId());
    assertTrue(suspendedJobDefinition.isSuspended());

    // ...and the corresponding job should be still active
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.suspended().count());
    assertEquals(1, jobQuery.active().count());

    Job job = jobQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), job.getJobDefinitionId());
    assertFalse(job.isSuspended());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/repository/ProcessDefinitionSuspensionTest.testWithOneAsyncServiceTask.bpmn"})
  public void testSuspendByKeyAndIncludeInstancesFlag_shouldSuspendAlsoJobDefinitionAndRetainJob() {
    // a process definition with a asynchronous continuation, so that there
    // exists a job definition
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // a running process instance with a failed service task
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceById(processDefinition.getId(), params);

    // when
    // the process definition will be suspended
    repositoryService.suspendProcessDefinitionByKey(processDefinition.getKey(), false, null);

    // then
    // the job definition should be suspended...
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.active().count());
    assertEquals(1, jobDefinitionQuery.suspended().count());

    JobDefinition suspendedJobDefinition = jobDefinitionQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), suspendedJobDefinition.getId());
    assertTrue(suspendedJobDefinition.isSuspended());

    // ...and the corresponding job should be still active
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.suspended().count());
    assertEquals(1, jobQuery.active().count());

    Job job = jobQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), job.getJobDefinitionId());
    assertFalse(job.isSuspended());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/repository/ProcessDefinitionSuspensionTest.testWithOneAsyncServiceTask.bpmn"})
  public void testSuspendByIdAndIncludeInstancesFlag_shouldSuspendJobDefinitionAndJob() {
    // a process definition with a asynchronous continuation, so that there
    // exists a job definition
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // a running process instance with a failed service task
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceById(processDefinition.getId(), params);

    // when
    // the process definition will be suspended
    repositoryService.suspendProcessDefinitionById(processDefinition.getId(), true, null);

    // then
    // the job definition should be suspended...
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.active().count());
    assertEquals(1, jobDefinitionQuery.suspended().count());

    JobDefinition suspendedJobDefinition = jobDefinitionQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), suspendedJobDefinition.getId());
    assertTrue(suspendedJobDefinition.isSuspended());

    // ...and the corresponding job should be suspended too
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.active().count());
    assertEquals(1, jobQuery.suspended().count());

    Job job = jobQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), job.getJobDefinitionId());
    assertTrue(job.isSuspended());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/repository/ProcessDefinitionSuspensionTest.testWithOneAsyncServiceTask.bpmn"})
  public void testSuspendByKeyAndIncludeInstancesFlag_shouldSuspendJobDefinitionAndJob() {
    // a process definition with a asynchronous continuation, so that there
    // exists a job definition
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // a running process instance with a failed service task
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceById(processDefinition.getId(), params);

    // when
    // the process definition will be suspended
    repositoryService.suspendProcessDefinitionByKey(processDefinition.getKey(), true, null);

    // then
    // the job definition should be suspended...
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.active().count());
    assertEquals(1, jobDefinitionQuery.suspended().count());

    JobDefinition suspendedJobDefinition = jobDefinitionQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), suspendedJobDefinition.getId());
    assertTrue(suspendedJobDefinition.isSuspended());

    // ...and the corresponding job should be suspended too
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.active().count());
    assertEquals(1, jobQuery.suspended().count());

    Job job = jobQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), job.getJobDefinitionId());
    assertTrue(job.isSuspended());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/repository/ProcessDefinitionSuspensionTest.testWithOneAsyncServiceTask.bpmn"})
  public void testDelayedSuspendByIdAndIncludeInstancesFlag_shouldSuspendJobDefinitionAndRetainJob() {
    Date startTime = new Date();
    ClockUtil.setCurrentTime(startTime);
    final long hourInMs = 60 * 60 * 1000;

    // a process definition with a asynchronous continuation, so that there
    // exists a job definition
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // a running process instance with a failed service task
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceById(processDefinition.getId(), params);

    // when
    // the process definition will be suspended in 2 hours
    repositoryService.suspendProcessDefinitionById(processDefinition.getId(), false, new Date(startTime.getTime() + (2 * hourInMs)));

    // then
    // there exists a job to suspend process definition
    Job timerToSuspendProcessDefinition = managementService.createJobQuery().timers().singleResult();
    assertNotNull(timerToSuspendProcessDefinition);

    // the job definition should still active
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.suspended().count());
    assertEquals(1, jobDefinitionQuery.active().count());

    JobDefinition activeJobDefinition = jobDefinitionQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), activeJobDefinition.getId());
    assertFalse(activeJobDefinition.isSuspended());

    // the job is still active
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.suspended().count());
    assertEquals(2, jobQuery.active().count()); // there exists two jobs, a failing job and a timer job

    // when
    // execute job
    managementService.executeJob(timerToSuspendProcessDefinition.getId());

    // then
    // the job definition should be suspended
    assertEquals(0, jobDefinitionQuery.active().count());
    assertEquals(1, jobDefinitionQuery.suspended().count());

    JobDefinition suspendedJobDefinition = jobDefinitionQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), suspendedJobDefinition.getId());
    assertTrue(suspendedJobDefinition.isSuspended());

    // the job is still active
    assertEquals(0, jobQuery.suspended().count());
    assertEquals(1, jobQuery.active().count());

    Job job = jobQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), job.getJobDefinitionId());
    assertFalse(job.isSuspended());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/repository/ProcessDefinitionSuspensionTest.testWithOneAsyncServiceTask.bpmn"})
  public void testDelayedSuspendByKeyAndIncludeInstancesFlag_shouldSuspendJobDefinitionAndRetainJob() {
    Date startTime = new Date();
    ClockUtil.setCurrentTime(startTime);
    final long hourInMs = 60 * 60 * 1000;

    // a process definition with a asynchronous continuation, so that there
    // exists a job definition
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // a running process instance with a failed service task
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceById(processDefinition.getId(), params);

    // when
    // the process definition will be suspended in 2 hours
    repositoryService.suspendProcessDefinitionByKey(processDefinition.getKey(), false,
            new Date(startTime.getTime() + (2 * hourInMs)));

    // then
    // there exists a job to suspend process definition
    Job timerToSuspendProcessDefinition = managementService.createJobQuery().timers().singleResult();
    assertNotNull(timerToSuspendProcessDefinition);

    // the job definition should still active
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.suspended().count());
    assertEquals(1, jobDefinitionQuery.active().count());

    JobDefinition activeJobDefinition = jobDefinitionQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), activeJobDefinition.getId());
    assertFalse(activeJobDefinition.isSuspended());

    // the job is still active
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.suspended().count());
    assertEquals(2, jobQuery.active().count()); // there exists two jobs, a failing job and a timer job

    // when
    // execute job
    managementService.executeJob(timerToSuspendProcessDefinition.getId());

    // then
    // the job definition should be suspended
    assertEquals(0, jobDefinitionQuery.active().count());
    assertEquals(1, jobDefinitionQuery.suspended().count());

    JobDefinition suspendedJobDefinition = jobDefinitionQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), suspendedJobDefinition.getId());
    assertTrue(suspendedJobDefinition.isSuspended());

    // the job is still active
    assertEquals(0, jobQuery.suspended().count());
    assertEquals(1, jobQuery.active().count());

    Job job = jobQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), job.getJobDefinitionId());
    assertFalse(job.isSuspended());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/repository/ProcessDefinitionSuspensionTest.testWithOneAsyncServiceTask.bpmn"})
  public void testDelayedSuspendByIdAndIncludeInstancesFlag_shouldSuspendJobDefinitionAndJob() {
    Date startTime = new Date();
    ClockUtil.setCurrentTime(startTime);
    final long hourInMs = 60 * 60 * 1000;

    // a process definition with a asynchronous continuation, so that there
    // exists a job definition
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // a running process instance with a failed service task
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceById(processDefinition.getId(), params);

    // when
    // the process definition will be suspended in 2 hours
    repositoryService.suspendProcessDefinitionById(processDefinition.getId(), true,
            new Date(startTime.getTime() + (2 * hourInMs)));

    // then
    // there exists a job to suspend process definition
    Job timerToSuspendProcessDefinition = managementService.createJobQuery().timers().singleResult();
    assertNotNull(timerToSuspendProcessDefinition);

    // the job definition should still active
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.suspended().count());
    assertEquals(1, jobDefinitionQuery.active().count());

    JobDefinition activeJobDefinition = jobDefinitionQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), activeJobDefinition.getId());
    assertFalse(activeJobDefinition.isSuspended());

    // the job is still active
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.suspended().count());
    assertEquals(2, jobQuery.active().count()); // there exists two jobs, a failing job and a timer job

    // when
    // execute job
    managementService.executeJob(timerToSuspendProcessDefinition.getId());

    // then
    // the job definition should be suspended
    assertEquals(0, jobDefinitionQuery.active().count());
    assertEquals(1, jobDefinitionQuery.suspended().count());

    JobDefinition suspendedJobDefinition = jobDefinitionQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), suspendedJobDefinition.getId());
    assertTrue(suspendedJobDefinition.isSuspended());

    // the job is still active
    assertEquals(0, jobQuery.active().count());
    assertEquals(1, jobQuery.suspended().count());

    Job job = jobQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), job.getJobDefinitionId());
    assertTrue(job.isSuspended());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/repository/ProcessDefinitionSuspensionTest.testWithOneAsyncServiceTask.bpmn"})
  public void testDelayedSuspendByKeyAndIncludeInstancesFlag_shouldSuspendJobDefinitionAndJob() {
    Date startTime = new Date();
    ClockUtil.setCurrentTime(startTime);
    final long hourInMs = 60 * 60 * 1000;

    // a process definition with a asynchronous continuation, so that there
    // exists a job definition
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // a running process instance with a failed service task
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceById(processDefinition.getId(), params);

    // when
    // the process definition will be suspended in 2 hours
    repositoryService.suspendProcessDefinitionByKey(processDefinition.getKey(), true,
            new Date(startTime.getTime() + (2 * hourInMs)));

    // then
    // there exists a job to suspend process definition
    Job timerToSuspendProcessDefinition = managementService.createJobQuery().timers().singleResult();
    assertNotNull(timerToSuspendProcessDefinition);

    // the job definition should still active
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.suspended().count());
    assertEquals(1, jobDefinitionQuery.active().count());

    JobDefinition activeJobDefinition = jobDefinitionQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), activeJobDefinition.getId());
    assertFalse(activeJobDefinition.isSuspended());

    // the job is still active
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.suspended().count());
    assertEquals(2, jobQuery.active().count()); // there exists two jobs, a failing job and a timer job

    // when
    // execute job
    managementService.executeJob(timerToSuspendProcessDefinition.getId());

    // then
    // the job definition should be suspended
    assertEquals(0, jobDefinitionQuery.active().count());
    assertEquals(1, jobDefinitionQuery.suspended().count());

    JobDefinition suspendedJobDefinition = jobDefinitionQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), suspendedJobDefinition.getId());
    assertTrue(suspendedJobDefinition.isSuspended());

    // the job is still active
    assertEquals(0, jobQuery.active().count());
    assertEquals(1, jobQuery.suspended().count());

    Job job = jobQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), job.getJobDefinitionId());
    assertTrue(job.isSuspended());
  }

  public void testMultipleSuspendByKey_shouldSuspendJobDefinitionAndRetainJob() {
    String key = "oneFailingServiceTaskProcess";

    // Deploy five versions of the same process, so that there exists
    // five job definitions
    int nrOfProcessDefinitions = 5;
    for (int i=0; i<nrOfProcessDefinitions; i++) {
      repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/ProcessDefinitionSuspensionTest.testWithOneAsyncServiceTask.bpmn").deploy();

      // a running process instance with a failed service task
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("fail", Boolean.TRUE);
      runtimeService.startProcessInstanceByKey(key, params);
    }

    // when
    // the process definition will be suspended
    repositoryService.suspendProcessDefinitionByKey(key);

    // then
    // the job definitions should be suspended...
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.active().count());
    assertEquals(5, jobDefinitionQuery.suspended().count());

    // ...and the corresponding jobs should be still active
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.suspended().count());
    assertEquals(5, jobQuery.active().count());

    // Clean DB
    for (org.camunda.bpm.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

  public void testMultipleSuspendByKeyAndIncludeInstances_shouldSuspendJobDefinitionAndRetainJob() {
    String key = "oneFailingServiceTaskProcess";

    // Deploy five versions of the same process, so that there exists
    // five job definitions
    int nrOfProcessDefinitions = 5;
    for (int i=0; i<nrOfProcessDefinitions; i++) {
      repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/ProcessDefinitionSuspensionTest.testWithOneAsyncServiceTask.bpmn").deploy();

      // a running process instance with a failed service task
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("fail", Boolean.TRUE);
      runtimeService.startProcessInstanceByKey(key, params);
    }

    // when
    // the process definition will be suspended
    repositoryService.suspendProcessDefinitionByKey(key, false, null);

    // then
    // the job definitions should be suspended...
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.active().count());
    assertEquals(5, jobDefinitionQuery.suspended().count());

    // ...and the corresponding jobs should be still active
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.suspended().count());
    assertEquals(5, jobQuery.active().count());

    // Clean DB
    for (org.camunda.bpm.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

  public void testMultipleSuspendByKeyAndIncludeInstances_shouldSuspendJobDefinitionAndJob() {
    String key = "oneFailingServiceTaskProcess";

    // Deploy five versions of the same process, so that there exists
    // five job definitions
    int nrOfProcessDefinitions = 5;
    for (int i=0; i<nrOfProcessDefinitions; i++) {
      repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/ProcessDefinitionSuspensionTest.testWithOneAsyncServiceTask.bpmn").deploy();

      // a running process instance with a failed service task
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("fail", Boolean.TRUE);
      runtimeService.startProcessInstanceByKey(key, params);
    }

    // when
    // the process definition will be suspended
    repositoryService.suspendProcessDefinitionByKey(key, true, null);

    // then
    // the job definitions should be suspended...
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.active().count());
    assertEquals(5, jobDefinitionQuery.suspended().count());

    // ...and the corresponding jobs should be suspended too
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.active().count());
    assertEquals(5, jobQuery.suspended().count());

    // Clean DB
    for (org.camunda.bpm.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

  public void testDelayedMultipleSuspendByKeyAndIncludeInstances_shouldSuspendJobDefinitionAndRetainJob() {
    Date startTime = new Date();
    ClockUtil.setCurrentTime(startTime);
    final long hourInMs = 60 * 60 * 1000;

    String key = "oneFailingServiceTaskProcess";

    // Deploy five versions of the same process, so that there exists
    // five job definitions
    int nrOfProcessDefinitions = 5;
    for (int i=0; i<nrOfProcessDefinitions; i++) {
      repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/ProcessDefinitionSuspensionTest.testWithOneAsyncServiceTask.bpmn").deploy();

      // a running process instance with a failed service task
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("fail", Boolean.TRUE);
      runtimeService.startProcessInstanceByKey(key, params);
    }

    // when
    // the process definition will be suspended
    repositoryService.suspendProcessDefinitionByKey(key, false, new Date(startTime.getTime() + (2 * hourInMs)));

    // then
    // there exists a timer job to suspend the process definition delayed
    Job timerToSuspendProcessDefinition = managementService.createJobQuery().timers().singleResult();
    assertNotNull(timerToSuspendProcessDefinition);

    // the job definitions should be still active
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.suspended().count());
    assertEquals(5, jobDefinitionQuery.active().count());

    // ...and the corresponding jobs should be still active
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.suspended().count());
    assertEquals(6, jobQuery.active().count());

    // when
    // execute job
    managementService.executeJob(timerToSuspendProcessDefinition.getId());

    // then
    // the job definitions should be suspended...
    assertEquals(0, jobDefinitionQuery.active().count());
    assertEquals(5, jobDefinitionQuery.suspended().count());

    // ...and the corresponding jobs should be still active
    assertEquals(0, jobQuery.suspended().count());
    assertEquals(5, jobQuery.active().count());

    // Clean DB
    for (org.camunda.bpm.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

  public void testDelayedMultipleSuspendByKeyAndIncludeInstances_shouldSuspendJobDefinitionAndJob() {
    Date startTime = new Date();
    ClockUtil.setCurrentTime(startTime);
    final long hourInMs = 60 * 60 * 1000;

    String key = "oneFailingServiceTaskProcess";

    // Deploy five versions of the same process, so that there exists
    // five job definitions
    int nrOfProcessDefinitions = 5;
    for (int i=0; i<nrOfProcessDefinitions; i++) {
      repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/ProcessDefinitionSuspensionTest.testWithOneAsyncServiceTask.bpmn").deploy();

      // a running process instance with a failed service task
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("fail", Boolean.TRUE);
      runtimeService.startProcessInstanceByKey(key, params);
    }

    // when
    // the process definition will be suspended
    repositoryService.suspendProcessDefinitionByKey(key, true, new Date(startTime.getTime() + (2 * hourInMs)));

    // then
    // there exists a timer job to suspend the process definition delayed
    Job timerToSuspendProcessDefinition = managementService.createJobQuery().timers().singleResult();
    assertNotNull(timerToSuspendProcessDefinition);

    // the job definitions should be still active
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.suspended().count());
    assertEquals(5, jobDefinitionQuery.active().count());

    // ...and the corresponding jobs should be still active
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.suspended().count());
    assertEquals(6, jobQuery.active().count());

    // when
    // execute job
    managementService.executeJob(timerToSuspendProcessDefinition.getId());

    // then
    // the job definitions should be suspended...
    assertEquals(0, jobDefinitionQuery.active().count());
    assertEquals(5, jobDefinitionQuery.suspended().count());

    // ...and the corresponding jobs should be suspended too
    assertEquals(0, jobQuery.active().count());
    assertEquals(5, jobQuery.suspended().count());

    // Clean DB
    for (org.camunda.bpm.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/repository/ProcessDefinitionSuspensionTest.testWithOneAsyncServiceTask.bpmn"})
  public void testActivationById_shouldActivateJobDefinitionAndRetainJob() {
    // a process definition with a asynchronous continuation, so that there
    // exists a job definition
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // a running process instance with a failed service task
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceById(processDefinition.getId(), params);

    // the process definition, job definition, process instance and job will be suspended
    repositoryService.suspendProcessDefinitionById(processDefinition.getId(), true, null);

    assertEquals(0, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().suspended().count());

    assertEquals(0, managementService.createJobDefinitionQuery().active().count());
    assertEquals(1, managementService.createJobDefinitionQuery().suspended().count());

    // when
    // the process definition will be activated
    repositoryService.activateProcessDefinitionById(processDefinition.getId());

    // then
    // the job definition should be active...
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.suspended().count());
    assertEquals(1, jobDefinitionQuery.active().count());

    JobDefinition activeJobDefinition = jobDefinitionQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), activeJobDefinition.getId());
    assertFalse(activeJobDefinition.isSuspended());

    // ...and the corresponding job should be still suspended
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.active().count());
    assertEquals(1, jobQuery.suspended().count());

    Job job = jobQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), job.getJobDefinitionId());
    assertTrue(job.isSuspended());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/repository/ProcessDefinitionSuspensionTest.testWithOneAsyncServiceTask.bpmn"})
  public void testActivationByKey_shouldActivateJobDefinitionAndRetainJob() {
    // a process definition with a asynchronous continuation, so that there
    // exists a job definition
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // a running process instance with a failed service task
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceById(processDefinition.getId(), params);

    // the process definition, job definition, process instance and job will be suspended
    repositoryService.suspendProcessDefinitionById(processDefinition.getId(), true, null);

    assertEquals(0, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().suspended().count());

    assertEquals(0, managementService.createJobDefinitionQuery().active().count());
    assertEquals(1, managementService.createJobDefinitionQuery().suspended().count());

    // when
    // the process definition will be activated
    repositoryService.activateProcessDefinitionByKey(processDefinition.getKey());

    // then
    // the job definition should be activated...
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.suspended().count());
    assertEquals(1, jobDefinitionQuery.active().count());

    JobDefinition activatedJobDefinition = jobDefinitionQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), activatedJobDefinition.getId());
    assertFalse(activatedJobDefinition.isSuspended());

    // ...and the corresponding job should be still suspended
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.active().count());
    assertEquals(1, jobQuery.suspended().count());

    Job job = jobQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), job.getJobDefinitionId());
    assertTrue(job.isSuspended());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/repository/ProcessDefinitionSuspensionTest.testWithOneAsyncServiceTask.bpmn"})
  public void testActivationByIdAndIncludeInstancesFlag_shouldActivateAlsoJobDefinitionAndRetainJob() {
    // a process definition with a asynchronous continuation, so that there
    // exists a job definition
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // a running process instance with a failed service task
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceById(processDefinition.getId(), params);

    // the process definition, job definition, process instance and job will be suspended
    repositoryService.suspendProcessDefinitionById(processDefinition.getId(), true, null);

    assertEquals(0, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().suspended().count());

    assertEquals(0, managementService.createJobDefinitionQuery().active().count());
    assertEquals(1, managementService.createJobDefinitionQuery().suspended().count());

    // when
    // the process definition will be activated
    repositoryService.activateProcessDefinitionById(processDefinition.getId(), false, null);

    // then
    // the job definition should be suspended...
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.suspended().count());
    assertEquals(1, jobDefinitionQuery.active().count());

    JobDefinition activatedJobDefinition = jobDefinitionQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), activatedJobDefinition.getId());
    assertFalse(activatedJobDefinition.isSuspended());

    // ...and the corresponding job should be still suspended
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.active().count());
    assertEquals(1, jobQuery.suspended().count());

    Job job = jobQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), job.getJobDefinitionId());
    assertTrue(job.isSuspended());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/repository/ProcessDefinitionSuspensionTest.testWithOneAsyncServiceTask.bpmn"})
  public void testActivationByKeyAndIncludeInstancesFlag_shouldActivateAlsoJobDefinitionAndRetainJob() {
    // a process definition with a asynchronous continuation, so that there
    // exists a job definition
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // a running process instance with a failed service task
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceById(processDefinition.getId(), params);

    // the process definition, job definition, process instance and job will be suspended
    repositoryService.suspendProcessDefinitionById(processDefinition.getId(), true, null);

    assertEquals(0, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().suspended().count());

    assertEquals(0, managementService.createJobDefinitionQuery().active().count());
    assertEquals(1, managementService.createJobDefinitionQuery().suspended().count());

    // when
    // the process definition will be activated
    repositoryService.activateProcessDefinitionByKey(processDefinition.getKey(), false, null);

    // then
    // the job definition should be activated...
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.suspended().count());
    assertEquals(1, jobDefinitionQuery.active().count());

    JobDefinition activatedJobDefinition = jobDefinitionQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), activatedJobDefinition.getId());
    assertFalse(activatedJobDefinition.isSuspended());

    // ...and the corresponding job should be still suspended
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.active().count());
    assertEquals(1, jobQuery.suspended().count());

    Job job = jobQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), job.getJobDefinitionId());
    assertTrue(job.isSuspended());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/repository/ProcessDefinitionSuspensionTest.testWithOneAsyncServiceTask.bpmn"})
  public void testActivationByIdAndIncludeInstancesFlag_shouldActivateJobDefinitionAndJob() {
    // a process definition with a asynchronous continuation, so that there
    // exists a job definition
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // a running process instance with a failed service task
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceById(processDefinition.getId(), params);

    // the process definition, job definition, process instance and job will be suspended
    repositoryService.suspendProcessDefinitionById(processDefinition.getId(), true, null);

    assertEquals(0, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().suspended().count());

    assertEquals(0, managementService.createJobDefinitionQuery().active().count());
    assertEquals(1, managementService.createJobDefinitionQuery().suspended().count());

    // when
    // the process definition will be activated
    repositoryService.activateProcessDefinitionById(processDefinition.getId(), true, null);

    // then
    // the job definition should be activated...
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.suspended().count());
    assertEquals(1, jobDefinitionQuery.active().count());

    JobDefinition activatedJobDefinition = jobDefinitionQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), activatedJobDefinition.getId());
    assertFalse(activatedJobDefinition.isSuspended());

    // ...and the corresponding job should be activated too
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.suspended().count());
    assertEquals(1, jobQuery.active().count());

    Job job = jobQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), job.getJobDefinitionId());
    assertFalse(job.isSuspended());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/repository/ProcessDefinitionSuspensionTest.testWithOneAsyncServiceTask.bpmn"})
  public void testActivationByKeyAndIncludeInstancesFlag_shouldActivateJobDefinitionAndJob() {
    // a process definition with a asynchronous continuation, so that there
    // exists a job definition
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // a running process instance with a failed service task
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceById(processDefinition.getId(), params);

    // the process definition, job definition, process instance and job will be suspended
    repositoryService.suspendProcessDefinitionById(processDefinition.getId(), true, null);

    assertEquals(0, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().suspended().count());

    assertEquals(0, managementService.createJobDefinitionQuery().active().count());
    assertEquals(1, managementService.createJobDefinitionQuery().suspended().count());

    // when
    // the process definition will be activated
    repositoryService.activateProcessDefinitionByKey(processDefinition.getKey(), true, null);

    // then
    // the job definition should be activated...
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.suspended().count());
    assertEquals(1, jobDefinitionQuery.active().count());

    JobDefinition suspendedJobDefinition = jobDefinitionQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), suspendedJobDefinition.getId());
    assertFalse(suspendedJobDefinition.isSuspended());

    // ...and the corresponding job should be activated too
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.suspended().count());
    assertEquals(1, jobQuery.active().count());

    Job job = jobQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), job.getJobDefinitionId());
    assertFalse(job.isSuspended());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/repository/ProcessDefinitionSuspensionTest.testWithOneAsyncServiceTask.bpmn"})
  public void testDelayedActivationByIdAndIncludeInstancesFlag_shouldActivateJobDefinitionAndRetainJob() {
    Date startTime = new Date();
    ClockUtil.setCurrentTime(startTime);
    final long hourInMs = 60 * 60 * 1000;

    // a process definition with a asynchronous continuation, so that there
    // exists a job definition
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // a running process instance with a failed service task
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceById(processDefinition.getId(), params);

    // the process definition, job definition, process instance and job will be suspended
    repositoryService.suspendProcessDefinitionById(processDefinition.getId(), true, null);

    assertEquals(0, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().suspended().count());

    assertEquals(0, managementService.createJobDefinitionQuery().active().count());
    assertEquals(1, managementService.createJobDefinitionQuery().suspended().count());

    // when
    // the process definition will be activated in 2 hours
    repositoryService.activateProcessDefinitionById(processDefinition.getId(), false, new Date(startTime.getTime() + (2 * hourInMs)));

    // then
    // there exists a job to activate process definition
    Job timerToActivateProcessDefinition = managementService.createJobQuery().timers().singleResult();
    assertNotNull(timerToActivateProcessDefinition);

    // the job definition should still suspended
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.active().count());
    assertEquals(1, jobDefinitionQuery.suspended().count());

    JobDefinition suspendedJobDefinition = jobDefinitionQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), suspendedJobDefinition.getId());
    assertTrue(suspendedJobDefinition.isSuspended());

    // the job is still suspended
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(1, jobQuery.suspended().count());
    assertEquals(1, jobQuery.active().count()); // the timer job is active

    // when
    // execute job
    managementService.executeJob(timerToActivateProcessDefinition.getId());

    // then
    // the job definition should be active
    assertEquals(0, jobDefinitionQuery.suspended().count());
    assertEquals(1, jobDefinitionQuery.active().count());

    JobDefinition activeJobDefinition = jobDefinitionQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), activeJobDefinition.getId());
    assertFalse(activeJobDefinition.isSuspended());

    // the job is still suspended
    assertEquals(0, jobQuery.active().count());
    assertEquals(1, jobQuery.suspended().count());

    Job job = jobQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), job.getJobDefinitionId());
    assertTrue(job.isSuspended());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/repository/ProcessDefinitionSuspensionTest.testWithOneAsyncServiceTask.bpmn"})
  public void testDelayedActivationByKeyAndIncludeInstancesFlag_shouldActivateJobDefinitionAndRetainJob() {
    Date startTime = new Date();
    ClockUtil.setCurrentTime(startTime);
    final long hourInMs = 60 * 60 * 1000;

    // a process definition with a asynchronous continuation, so that there
    // exists a job definition
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // a running process instance with a failed service task
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceById(processDefinition.getId(), params);

    // the process definition, job definition, process instance and job will be suspended
    repositoryService.suspendProcessDefinitionById(processDefinition.getId(), true, null);

    assertEquals(0, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().suspended().count());

    assertEquals(0, managementService.createJobDefinitionQuery().active().count());
    assertEquals(1, managementService.createJobDefinitionQuery().suspended().count());

    // when
    // the process definition will be activated in 2 hours
    repositoryService.activateProcessDefinitionByKey(processDefinition.getKey(), false,
            new Date(startTime.getTime() + (2 * hourInMs)));

    // then
    // there exists a job to activate process definition
    Job timerToActivateProcessDefinition = managementService.createJobQuery().timers().singleResult();
    assertNotNull(timerToActivateProcessDefinition);

    // the job definition should still suspended
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.active().count());
    assertEquals(1, jobDefinitionQuery.suspended().count());

    JobDefinition suspendedJobDefinition = jobDefinitionQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), suspendedJobDefinition.getId());
    assertTrue(suspendedJobDefinition.isSuspended());

    // the job is still suspended
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(1, jobQuery.suspended().count());
    assertEquals(1, jobQuery.active().count()); // the timer job is active

    // when
    // execute job
    managementService.executeJob(timerToActivateProcessDefinition.getId());

    // then
    // the job definition should be suspended
    assertEquals(0, jobDefinitionQuery.suspended().count());
    assertEquals(1, jobDefinitionQuery.active().count());

    JobDefinition activatedJobDefinition = jobDefinitionQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), activatedJobDefinition.getId());
    assertFalse(activatedJobDefinition.isSuspended());

    // the job is still suspended
    assertEquals(0, jobQuery.active().count());
    assertEquals(1, jobQuery.suspended().count());

    Job job = jobQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), job.getJobDefinitionId());
    assertTrue(job.isSuspended());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/repository/ProcessDefinitionSuspensionTest.testWithOneAsyncServiceTask.bpmn"})
  public void testDelayedActivationByIdAndIncludeInstancesFlag_shouldActivateJobDefinitionAndJob() {
    Date startTime = new Date();
    ClockUtil.setCurrentTime(startTime);
    final long hourInMs = 60 * 60 * 1000;

    // a process definition with a asynchronous continuation, so that there
    // exists a job definition
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // a running process instance with a failed service task
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceById(processDefinition.getId(), params);

    // the process definition, job definition, process instance and job will be suspended
    repositoryService.suspendProcessDefinitionById(processDefinition.getId(), true, null);

    assertEquals(0, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().suspended().count());

    assertEquals(0, managementService.createJobDefinitionQuery().active().count());
    assertEquals(1, managementService.createJobDefinitionQuery().suspended().count());

    // when
    // the process definition will be activated in 2 hours
    repositoryService.activateProcessDefinitionById(processDefinition.getId(), true,
            new Date(startTime.getTime() + (2 * hourInMs)));

    // then
    // there exists a job to activate process definition
    Job timerToActivateProcessDefinition = managementService.createJobQuery().timers().singleResult();
    assertNotNull(timerToActivateProcessDefinition);

    // the job definition should still suspended
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.active().count());
    assertEquals(1, jobDefinitionQuery.suspended().count());

    JobDefinition suspendedJobDefinition = jobDefinitionQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), suspendedJobDefinition.getId());
    assertTrue(suspendedJobDefinition.isSuspended());

    // the job is still suspended
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(1, jobQuery.suspended().count());
    assertEquals(1, jobQuery.active().count()); // the timer job is active

    // when
    // execute job
    managementService.executeJob(timerToActivateProcessDefinition.getId());

    // then
    // the job definition should be activated
    assertEquals(0, jobDefinitionQuery.suspended().count());
    assertEquals(1, jobDefinitionQuery.active().count());

    JobDefinition activatedJobDefinition = jobDefinitionQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), activatedJobDefinition.getId());
    assertFalse(activatedJobDefinition.isSuspended());

    // the job is activated
    assertEquals(0, jobQuery.suspended().count());
    assertEquals(1, jobQuery.active().count());

    Job job = jobQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), job.getJobDefinitionId());
    assertFalse(job.isSuspended());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/repository/ProcessDefinitionSuspensionTest.testWithOneAsyncServiceTask.bpmn"})
  public void testDelayedActivationByKeyAndIncludeInstancesFlag_shouldActivateJobDefinitionAndJob() {
    Date startTime = new Date();
    ClockUtil.setCurrentTime(startTime);
    final long hourInMs = 60 * 60 * 1000;

    // a process definition with a asynchronous continuation, so that there
    // exists a job definition
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // a running process instance with a failed service task
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceById(processDefinition.getId(), params);

    // the process definition, job definition, process instance and job will be suspended
    repositoryService.suspendProcessDefinitionById(processDefinition.getId(), true, null);

    assertEquals(0, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().suspended().count());

    assertEquals(0, managementService.createJobDefinitionQuery().active().count());
    assertEquals(1, managementService.createJobDefinitionQuery().suspended().count());

    // when
    // the process definition will be activated in 2 hours
    repositoryService.activateProcessDefinitionByKey(processDefinition.getKey(), true,
            new Date(startTime.getTime() + (2 * hourInMs)));

    // then
    // there exists a job to activate process definition
    Job timerToActivateProcessDefinition = managementService.createJobQuery().timers().singleResult();
    assertNotNull(timerToActivateProcessDefinition);

    // the job definition should still suspended
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.active().count());
    assertEquals(1, jobDefinitionQuery.suspended().count());

    JobDefinition suspendedJobDefinition = jobDefinitionQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), suspendedJobDefinition.getId());
    assertTrue(suspendedJobDefinition.isSuspended());

    // the job is still suspended
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(1, jobQuery.suspended().count());
    assertEquals(1, jobQuery.active().count()); // the timer job is active

    // when
    // execute job
    managementService.executeJob(timerToActivateProcessDefinition.getId());

    // then
    // the job definition should be activated
    assertEquals(0, jobDefinitionQuery.suspended().count());
    assertEquals(1, jobDefinitionQuery.active().count());

    JobDefinition activatedJobDefinition = jobDefinitionQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), activatedJobDefinition.getId());
    assertFalse(activatedJobDefinition.isSuspended());

    // the job is activated too
    assertEquals(0, jobQuery.suspended().count());
    assertEquals(1, jobQuery.active().count());

    Job job = jobQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), job.getJobDefinitionId());
    assertFalse(job.isSuspended());
  }

  public void testMultipleActivationByKey_shouldActivateJobDefinitionAndRetainJob() {
    String key = "oneFailingServiceTaskProcess";

    // Deploy five versions of the same process, so that there exists
    // five job definitions
    int nrOfProcessDefinitions = 5;
    for (int i=0; i<nrOfProcessDefinitions; i++) {
      repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/ProcessDefinitionSuspensionTest.testWithOneAsyncServiceTask.bpmn").deploy();

      // a running process instance with a failed service task
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("fail", Boolean.TRUE);
      runtimeService.startProcessInstanceByKey(key, params);
    }

    // the process definition, job definition, process instance and job will be suspended
    repositoryService.suspendProcessDefinitionByKey(key, true, null);

    assertEquals(0, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(5, repositoryService.createProcessDefinitionQuery().suspended().count());

    assertEquals(0, managementService.createJobDefinitionQuery().active().count());
    assertEquals(5, managementService.createJobDefinitionQuery().suspended().count());

    // when
    // the process definition will be activated
    repositoryService.activateProcessDefinitionByKey(key);

    // then
    // the job definitions should be activated...
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.suspended().count());
    assertEquals(5, jobDefinitionQuery.active().count());

    // ...and the corresponding jobs should be still suspended
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.active().count());
    assertEquals(5, jobQuery.suspended().count());

    // Clean DB
    for (org.camunda.bpm.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

  public void testMultipleActivationByKeyAndIncludeInstances_shouldActivateJobDefinitionAndRetainJob() {
    String key = "oneFailingServiceTaskProcess";

    // Deploy five versions of the same process, so that there exists
    // five job definitions
    int nrOfProcessDefinitions = 5;
    for (int i=0; i<nrOfProcessDefinitions; i++) {
      repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/ProcessDefinitionSuspensionTest.testWithOneAsyncServiceTask.bpmn").deploy();

      // a running process instance with a failed service task
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("fail", Boolean.TRUE);
      runtimeService.startProcessInstanceByKey(key, params);
    }

    // the process definition, job definition, process instance and job will be suspended
    repositoryService.suspendProcessDefinitionByKey(key, true, null);

    assertEquals(0, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(5, repositoryService.createProcessDefinitionQuery().suspended().count());

    assertEquals(0, managementService.createJobDefinitionQuery().active().count());
    assertEquals(5, managementService.createJobDefinitionQuery().suspended().count());

    // when
    // the process definition will be activated
    repositoryService.activateProcessDefinitionByKey(key, false, null);

    // then
    // the job definitions should be activated...
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(5, jobDefinitionQuery.active().count());
    assertEquals(0, jobDefinitionQuery.suspended().count());

    // ...and the corresponding jobs should still suspended
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.active().count());
    assertEquals(5, jobQuery.suspended().count());

    // Clean DB
    for (org.camunda.bpm.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

  public void testMultipleActivationByKeyAndIncludeInstances_shouldActivateJobDefinitionAndJob() {

    String key = "oneFailingServiceTaskProcess";

    // Deploy five versions of the same process, so that there exists
    // five job definitions
    int nrOfProcessDefinitions = 5;
    for (int i=0; i<nrOfProcessDefinitions; i++) {
      repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/ProcessDefinitionSuspensionTest.testWithOneAsyncServiceTask.bpmn").deploy();

      // a running process instance with a failed service task
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("fail", Boolean.TRUE);
      runtimeService.startProcessInstanceByKey(key, params);
    }

    // the process definition, job definition, process instance and job will be suspended
    repositoryService.suspendProcessDefinitionByKey(key, true, null);

    assertEquals(0, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(5, repositoryService.createProcessDefinitionQuery().suspended().count());

    assertEquals(0, managementService.createJobDefinitionQuery().active().count());
    assertEquals(5, managementService.createJobDefinitionQuery().suspended().count());

    // when
    // the process definition will be activated
    repositoryService.activateProcessDefinitionByKey(key, true, null);

    // then
    // the job definitions should be activated...
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(5, jobDefinitionQuery.active().count());
    assertEquals(0, jobDefinitionQuery.suspended().count());

    // ...and the corresponding jobs should be activated too
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(5, jobQuery.active().count());
    assertEquals(0, jobQuery.suspended().count());

    // Clean DB
    for (org.camunda.bpm.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

  public void testDelayedMultipleActivationByKeyAndIncludeInstances_shouldActivateJobDefinitionAndRetainJob() {
    Date startTime = new Date();
    ClockUtil.setCurrentTime(startTime);
    final long hourInMs = 60 * 60 * 1000;

    String key = "oneFailingServiceTaskProcess";

    // Deploy five versions of the same process, so that there exists
    // five job definitions
    int nrOfProcessDefinitions = 5;
    for (int i=0; i<nrOfProcessDefinitions; i++) {
      repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/ProcessDefinitionSuspensionTest.testWithOneAsyncServiceTask.bpmn").deploy();

      // a running process instance with a failed service task
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("fail", Boolean.TRUE);
      runtimeService.startProcessInstanceByKey(key, params);
    }

    // the process definition, job definition, process instance and job will be suspended
    repositoryService.suspendProcessDefinitionByKey(key, true, null);

    assertEquals(0, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(5, repositoryService.createProcessDefinitionQuery().suspended().count());

    assertEquals(0, managementService.createJobDefinitionQuery().active().count());
    assertEquals(5, managementService.createJobDefinitionQuery().suspended().count());

    // when
    // the process definition will be activated
    repositoryService.activateProcessDefinitionByKey(key, false, new Date(startTime.getTime() + (2 * hourInMs)));

    // then
    // there exists a timer job to activate the process definition delayed
    Job timerToActivateProcessDefinition = managementService.createJobQuery().timers().singleResult();
    assertNotNull(timerToActivateProcessDefinition);

    // the job definitions should be still suspended
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(5, jobDefinitionQuery.suspended().count());
    assertEquals(0, jobDefinitionQuery.active().count());

    // ...and the corresponding jobs should be still suspended
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(5, jobQuery.suspended().count());
    assertEquals(1, jobQuery.active().count());

    // when
    // execute job
    managementService.executeJob(timerToActivateProcessDefinition.getId());

    // then
    // the job definitions should be activated...
    assertEquals(5, jobDefinitionQuery.active().count());
    assertEquals(0, jobDefinitionQuery.suspended().count());

    // ...and the corresponding jobs should be still suspended
    assertEquals(5, jobQuery.suspended().count());
    assertEquals(0, jobQuery.active().count());

    // Clean DB
    for (org.camunda.bpm.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

  public void testDelayedMultipleActivationByKeyAndIncludeInstances_shouldActivateJobDefinitionAndJob() {
    Date startTime = new Date();
    ClockUtil.setCurrentTime(startTime);
    final long hourInMs = 60 * 60 * 1000;

    String key = "oneFailingServiceTaskProcess";

    // Deploy five versions of the same process, so that there exists
    // five job definitions
    int nrOfProcessDefinitions = 5;
    for (int i=0; i<nrOfProcessDefinitions; i++) {
      repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/ProcessDefinitionSuspensionTest.testWithOneAsyncServiceTask.bpmn").deploy();

      // a running process instance with a failed service task
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("fail", Boolean.TRUE);
      runtimeService.startProcessInstanceByKey(key, params);
    }

    // the process definition, job definition, process instance and job will be suspended
    repositoryService.suspendProcessDefinitionByKey(key, true, null);

    assertEquals(0, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(5, repositoryService.createProcessDefinitionQuery().suspended().count());

    assertEquals(0, managementService.createJobDefinitionQuery().active().count());
    assertEquals(5, managementService.createJobDefinitionQuery().suspended().count());

    // when
    // the process definition will be activated
    repositoryService.activateProcessDefinitionByKey(key, true, new Date(startTime.getTime() + (2 * hourInMs)));

    // then
    // there exists a timer job to activate the process definition delayed
    Job timerToActivateProcessDefinition = managementService.createJobQuery().timers().singleResult();
    assertNotNull(timerToActivateProcessDefinition);

    // the job definitions should be still suspended
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(5, jobDefinitionQuery.suspended().count());
    assertEquals(0, jobDefinitionQuery.active().count());

    // ...and the corresponding jobs should be still suspended
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(5, jobQuery.suspended().count());
    assertEquals(1, jobQuery.active().count());

    // when
    // execute job
    managementService.executeJob(timerToActivateProcessDefinition.getId());

    // then
    // the job definitions should be activated...
    assertEquals(5, jobDefinitionQuery.active().count());
    assertEquals(0, jobDefinitionQuery.suspended().count());

    // ...and the corresponding jobs should be activated too
    assertEquals(5, jobQuery.active().count());
    assertEquals(0, jobQuery.suspended().count());

    // Clean DB
    for (org.camunda.bpm.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }


  @Deployment(resources = {"org/camunda/bpm/engine/test/api/repository/ProcessDefinitionSuspensionTest.testSuspendStartTimerOnProcessDefinitionSuspension.bpmn20.xml"})
  public void testSuspendStartTimerOnProcessDefinitionSuspensionByKey() {
    Job startTimer = managementService.createJobQuery().timers().singleResult();

    assertFalse(startTimer.isSuspended());

    // when
    repositoryService.suspendProcessDefinitionByKey("process");

    // then

    // refresh job
    startTimer = managementService.createJobQuery().timers().singleResult();
    assertTrue(startTimer.isSuspended());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/repository/ProcessDefinitionSuspensionTest.testSuspendStartTimerOnProcessDefinitionSuspension.bpmn20.xml"})
  public void testSuspendStartTimerOnProcessDefinitionSuspensionById() {
    ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().singleResult();

    Job startTimer = managementService.createJobQuery().timers().singleResult();

    assertFalse(startTimer.isSuspended());

    // when
    repositoryService.suspendProcessDefinitionById(pd.getId());

    // then

    // refresh job
    startTimer = managementService.createJobQuery().timers().singleResult();
    assertTrue(startTimer.isSuspended());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/repository/ProcessDefinitionSuspensionTest.testSuspendStartTimerOnProcessDefinitionSuspension.bpmn20.xml"})
  public void testActivateStartTimerOnProcessDefinitionSuspensionByKey() {
    repositoryService.suspendProcessDefinitionByKey("process");

    Job startTimer = managementService.createJobQuery().timers().singleResult();
    assertTrue(startTimer.isSuspended());

    // when
    repositoryService.activateProcessDefinitionByKey("process");
    // then

    // refresh job
    startTimer = managementService.createJobQuery().timers().singleResult();
    assertFalse(startTimer.isSuspended());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/repository/ProcessDefinitionSuspensionTest.testSuspendStartTimerOnProcessDefinitionSuspension.bpmn20.xml"})
  public void testActivateStartTimerOnProcessDefinitionSuspensionById() {
    ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().singleResult();
    repositoryService.suspendProcessDefinitionById(pd.getId());

    Job startTimer = managementService.createJobQuery().timers().singleResult();

    assertTrue(startTimer.isSuspended());

    // when
    repositoryService.activateProcessDefinitionById(pd.getId());

    // then

    // refresh job
    startTimer = managementService.createJobQuery().timers().singleResult();
    assertFalse(startTimer.isSuspended());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testStartBeforeActivityForSuspendProcessDefinition() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    //start process instance
    runtimeService.startProcessInstanceById(processDefinition.getId());
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();

    // Suspend process definition
    repositoryService.suspendProcessDefinitionById(processDefinition.getId(), true, null);

    // try to start before activity for suspended processDefinition
    try {
      runtimeService.createProcessInstanceModification(processInstance.getId()).startBeforeActivity("theTask").execute();
      fail("Exception is expected but not thrown");
    } catch(SuspendedEntityInteractionException e) {
      assertTextPresentIgnoreCase("is suspended", e.getMessage());
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testStartAfterActivityForSuspendProcessDefinition() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    //start process instance
    runtimeService.startProcessInstanceById(processDefinition.getId());
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();

    // Suspend process definition
    repositoryService.suspendProcessDefinitionById(processDefinition.getId(), true, null);

    // try to start after activity for suspended processDefinition
    try {
      runtimeService.createProcessInstanceModification(processInstance.getId()).startAfterActivity("theTask").execute();
      fail("Exception is expected but not thrown");
    } catch(SuspendedEntityInteractionException e) {
      assertTextPresentIgnoreCase("is suspended", e.getMessage());
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/repository/processOne.bpmn20.xml"})
  public void testSuspendAndActivateProcessDefinitionByIdUsingBuilder() {

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertFalse(processDefinition.isSuspended());

    // suspend
    repositoryService
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionId(processDefinition.getId())
      .suspend();

    processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertTrue(processDefinition.isSuspended());

    // activate
    repositoryService
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionId(processDefinition.getId())
      .activate();

    processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertFalse(processDefinition.isSuspended());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/repository/processOne.bpmn20.xml"})
  public void testSuspendAndActivateProcessDefinitionByKeyUsingBuilder() {

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertFalse(processDefinition.isSuspended());

    // suspend
    repositoryService
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(processDefinition.getKey())
      .suspend();

    processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertTrue(processDefinition.isSuspended());

    // activate
    repositoryService
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(processDefinition.getKey())
      .activate();

    processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertFalse(processDefinition.isSuspended());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testDelayedSuspendProcessDefinitionUsingBuilder() {

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // suspend process definition in one week from now
    long oneWeekFromStartTime = new Date().getTime() + (7 * 24 * 60 * 60 * 1000);

    repositoryService
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionId(processDefinition.getId())
      .executionDate(new Date(oneWeekFromStartTime))
      .suspend();

    processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertFalse(processDefinition.isSuspended());

    // execute the suspension job
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);
    managementService.executeJob(job.getId());

    processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertTrue(processDefinition.isSuspended());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testDelayedActivateProcessDefinitionUsingBuilder() {

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // suspend
    repositoryService
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(processDefinition.getKey())
      .suspend();

    // activate process definition in one week from now
    long oneWeekFromStartTime = new Date().getTime() + (7 * 24 * 60 * 60 * 1000);

    repositoryService
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionId(processDefinition.getId())
      .executionDate(new Date(oneWeekFromStartTime))
      .activate();

    processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertTrue(processDefinition.isSuspended());

    // execute the activation job
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);
    managementService.executeJob(job.getId());

    processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertFalse(processDefinition.isSuspended());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testSuspendAndActivateProcessDefinitionIncludeInstancesUsingBuilder() {

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());

    assertFalse(processDefinition.isSuspended());
    assertFalse(processInstance.isSuspended());

    // suspend
    repositoryService
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionId(processDefinition.getId())
      .includeProcessInstances(true)
      .suspend();

    processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertTrue(processDefinition.isSuspended());

    processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertTrue(processInstance.isSuspended());

    // activate
    repositoryService
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionId(processDefinition.getId())
      .includeProcessInstances(true)
      .activate();

    processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertFalse(processDefinition.isSuspended());

    processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertFalse(processInstance.isSuspended());
  }

}
