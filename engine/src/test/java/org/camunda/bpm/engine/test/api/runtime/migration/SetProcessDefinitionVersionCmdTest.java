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

package org.camunda.bpm.engine.test.api.runtime.migration;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.SetProcessDefinitionVersionCmd;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.MessageJobDeclaration;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.Variables;


/**
 * @author Falko Menge
 */
public class SetProcessDefinitionVersionCmdTest extends PluggableProcessEngineTestCase {

  private static final String TEST_PROCESS_WITH_PARALLEL_GATEWAY = "org/camunda/bpm/engine/test/bpmn/gateway/ParallelGatewayTest.testForkJoin.bpmn20.xml";
  private static final String TEST_PROCESS = "org/camunda/bpm/engine/test/api/runtime/migration/SetProcessDefinitionVersionCmdTest.testSetProcessDefinitionVersion.bpmn20.xml";
  private static final String TEST_PROCESS_ACTIVITY_MISSING = "org/camunda/bpm/engine/test/api/runtime/migration/SetProcessDefinitionVersionCmdTest.testSetProcessDefinitionVersionActivityMissing.bpmn20.xml";

  private static final String TEST_PROCESS_CALL_ACTIVITY = "org/camunda/bpm/engine/test/api/runtime/migration/SetProcessDefinitionVersionCmdTest.withCallActivity.bpmn20.xml";
  private static final String TEST_PROCESS_USER_TASK_V1 = "org/camunda/bpm/engine/test/api/runtime/migration/SetProcessDefinitionVersionCmdTest.testSetProcessDefinitionVersionWithTask.bpmn20.xml";
  private static final String TEST_PROCESS_USER_TASK_V2 = "org/camunda/bpm/engine/test/api/runtime/migration/SetProcessDefinitionVersionCmdTest.testSetProcessDefinitionVersionWithTaskV2.bpmn20.xml";

  private static final String TEST_PROCESS_SERVICE_TASK_V1 = "org/camunda/bpm/engine/test/api/runtime/migration/SetProcessDefinitionVersionCmdTest.testSetProcessDefinitionVersionWithServiceTask.bpmn20.xml";
  private static final String TEST_PROCESS_SERVICE_TASK_V2 = "org/camunda/bpm/engine/test/api/runtime/migration/SetProcessDefinitionVersionCmdTest.testSetProcessDefinitionVersionWithServiceTaskV2.bpmn20.xml";

  private static final String TEST_PROCESS_WITH_MULTIPLE_PARENTS = "org/camunda/bpm/engine/test/api/runtime/migration/SetProcessDefinitionVersionCmdTest.testSetProcessDefinitionVersionWithMultipleParents.bpmn";

  private static final String TEST_PROCESS_ONE_JOB = "org/camunda/bpm/engine/test/api/runtime/migration/SetProcessDefinitionVersionCmdTest.oneJobProcess.bpmn20.xml";
  private static final String TEST_PROCESS_TWO_JOBS = "org/camunda/bpm/engine/test/api/runtime/migration/SetProcessDefinitionVersionCmdTest.twoJobsProcess.bpmn20.xml";

  private static final String TEST_PROCESS_ATTACHED_TIMER = "org/camunda/bpm/engine/test/api/runtime/migration/SetProcessDefinitionVersionCmdTest.testAttachedTimer.bpmn20.xml";

  public void testSetProcessDefinitionVersionEmptyArguments() {
    try {
      new SetProcessDefinitionVersionCmd(null, 23);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("The process instance id is mandatory: processInstanceId is null", ae.getMessage());
    }

    try {
      new SetProcessDefinitionVersionCmd("", 23);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("The process instance id is mandatory: processInstanceId is empty", ae.getMessage());
    }

    try {
      new SetProcessDefinitionVersionCmd("42", null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("The process definition version is mandatory: processDefinitionVersion is null", ae.getMessage());
    }

    try {
      new SetProcessDefinitionVersionCmd("42", -1);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("The process definition version must be positive: processDefinitionVersion is not greater than 0", ae.getMessage());
    }
  }

  public void testSetProcessDefinitionVersionNonExistingPI() {
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    try {
      commandExecutor.execute(new SetProcessDefinitionVersionCmd("42", 23));
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("No process instance found for id = '42'.", ae.getMessage());
    }
  }

  @Deployment(resources = {TEST_PROCESS_WITH_PARALLEL_GATEWAY})
  public void testSetProcessDefinitionVersionPIIsSubExecution() {
    // start process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("forkJoin");

    Execution execution = runtimeService.createExecutionQuery()
      .activityId("receivePayment")
      .singleResult();
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    SetProcessDefinitionVersionCmd command = new SetProcessDefinitionVersionCmd(execution.getId(), 1);
    try {
      commandExecutor.execute(command);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("A process instance id is required, but the provided id '"+execution.getId()+"' points to a child execution of process instance '"+pi.getId()+"'. Please invoke the "+command.getClass().getSimpleName()+" with a root execution id.", ae.getMessage());
    }
  }

  @Deployment(resources = {TEST_PROCESS})
  public void testSetProcessDefinitionVersionNonExistingPD() {
    // start process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("receiveTask");

    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    try {
      commandExecutor.execute(new SetProcessDefinitionVersionCmd(pi.getId(), 23));
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("no processes deployed with key = 'receiveTask', version = '23'", ae.getMessage());
    }
  }

  @Deployment(resources = {TEST_PROCESS})
  public void testSetProcessDefinitionVersionActivityMissing() {
    // start process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("receiveTask");

    // check that receive task has been reached
    Execution execution = runtimeService.createExecutionQuery()
      .activityId("waitState1")
      .singleResult();
    assertNotNull(execution);

    // deploy new version of the process definition
    org.camunda.bpm.engine.repository.Deployment deployment = repositoryService
      .createDeployment()
      .addClasspathResource(TEST_PROCESS_ACTIVITY_MISSING)
      .deploy();
    assertEquals(2, repositoryService.createProcessDefinitionQuery().count());

    // migrate process instance to new process definition version
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    SetProcessDefinitionVersionCmd setProcessDefinitionVersionCmd = new SetProcessDefinitionVersionCmd(pi.getId(), 2);
    try {
      commandExecutor.execute(setProcessDefinitionVersionCmd);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("The new process definition (key = 'receiveTask') does not contain the current activity (id = 'waitState1') of the process instance (id = '", ae.getMessage());
    }

    // undeploy "manually" deployed process definition
    repositoryService.deleteDeployment(deployment.getId(), true);
  }

  @Deployment
  public void testSetProcessDefinitionVersion() {
    // start process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("receiveTask");

    // check that receive task has been reached
    Execution execution = runtimeService.createExecutionQuery()
      .processInstanceId(pi.getId())
      .activityId("waitState1")
      .singleResult();
    assertNotNull(execution);

    // deploy new version of the process definition
    org.camunda.bpm.engine.repository.Deployment deployment = repositoryService
      .createDeployment()
      .addClasspathResource(TEST_PROCESS)
      .deploy();
    assertEquals(2, repositoryService.createProcessDefinitionQuery().count());

    // migrate process instance to new process definition version
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutor.execute(new SetProcessDefinitionVersionCmd(pi.getId(), 2));

    // signal process instance
    runtimeService.signal(execution.getId());

    // check that the instance now uses the new process definition version
    ProcessDefinition newProcessDefinition = repositoryService
      .createProcessDefinitionQuery()
      .processDefinitionVersion(2)
      .singleResult();
    pi = runtimeService
      .createProcessInstanceQuery()
      .processInstanceId(pi.getId())
      .singleResult();
    assertEquals(newProcessDefinition.getId(), pi.getProcessDefinitionId());

    // check history
    if (processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      HistoricProcessInstance historicPI = historyService
        .createHistoricProcessInstanceQuery()
        .processInstanceId(pi.getId())
        .singleResult();

//      assertEquals(newProcessDefinition.getId(), historicPI.getProcessDefinitionId());
    }

    // undeploy "manually" deployed process definition
    repositoryService.deleteDeployment(deployment.getId(), true);
  }

  @Deployment(resources = {TEST_PROCESS_WITH_PARALLEL_GATEWAY})
  public void testSetProcessDefinitionVersionSubExecutions() {
    // start process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("forkJoin");

    // check that the user tasks have been reached
    assertEquals(2, taskService.createTaskQuery().count());

    // deploy new version of the process definition
    org.camunda.bpm.engine.repository.Deployment deployment = repositoryService
      .createDeployment()
      .addClasspathResource(TEST_PROCESS_WITH_PARALLEL_GATEWAY)
      .deploy();
    assertEquals(2, repositoryService.createProcessDefinitionQuery().count());

    // migrate process instance to new process definition version
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutor.execute(new SetProcessDefinitionVersionCmd(pi.getId(), 2));

    // check that all executions of the instance now use the new process definition version
    ProcessDefinition newProcessDefinition = repositoryService
      .createProcessDefinitionQuery()
      .processDefinitionVersion(2)
      .singleResult();
    List<Execution> executions = runtimeService
      .createExecutionQuery()
      .processInstanceId(pi.getId())
      .list();
    for (Execution execution : executions) {
      assertEquals(newProcessDefinition.getId(), ((ExecutionEntity) execution).getProcessDefinitionId());
    }

    // undeploy "manually" deployed process definition
    repositoryService.deleteDeployment(deployment.getId(), true);
  }

  @Deployment(resources = {TEST_PROCESS_CALL_ACTIVITY})
  public void testSetProcessDefinitionVersionWithCallActivity() {
    // start process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("parentProcess");

    // check that receive task has been reached
    Execution execution = runtimeService.createExecutionQuery()
      .activityId("waitState1")
      .processDefinitionKey("childProcess")
      .singleResult();
    assertNotNull(execution);

    // deploy new version of the process definition
    org.camunda.bpm.engine.repository.Deployment deployment = repositoryService
      .createDeployment()
      .addClasspathResource(TEST_PROCESS_CALL_ACTIVITY)
      .deploy();
    assertEquals(2, repositoryService.createProcessDefinitionQuery().processDefinitionKey("parentProcess").count());

    // migrate process instance to new process definition version
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutor.execute(new SetProcessDefinitionVersionCmd(pi.getId(), 2));

    // signal process instance
    runtimeService.signal(execution.getId());

    // should be finished now
    assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).count());

    // undeploy "manually" deployed process definition
    repositoryService.deleteDeployment(deployment.getId(), true);
  }

  @Deployment(resources = {TEST_PROCESS_USER_TASK_V1})
  public void testSetProcessDefinitionVersionWithWithTask() {
    try {
    // start process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("userTask");

    // check that user task has been reached
    assertEquals(1, taskService.createTaskQuery().processInstanceId(pi.getId()).count());

    // deploy new version of the process definition
    org.camunda.bpm.engine.repository.Deployment deployment = repositoryService
      .createDeployment()
      .addClasspathResource(TEST_PROCESS_USER_TASK_V2)
      .deploy();
    assertEquals(2, repositoryService.createProcessDefinitionQuery().processDefinitionKey("userTask").count());

    ProcessDefinition newProcessDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("userTask").processDefinitionVersion(2).singleResult();

    // migrate process instance to new process definition version
    processEngineConfiguration.getCommandExecutorTxRequired().execute(new SetProcessDefinitionVersionCmd(pi.getId(), 2));

    // check UserTask
    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals(newProcessDefinition.getId(), task.getProcessDefinitionId());
    assertEquals("testFormKey", formService.getTaskFormData(task.getId()).getFormKey());

    // continue
    taskService.complete(task.getId());

    assertProcessEnded(pi.getId());

    // undeploy "manually" deployed process definition
    repositoryService.deleteDeployment(deployment.getId(), true);
    }
    catch (Exception ex) {
     ex.printStackTrace();
    }
  }

  @Deployment(resources = TEST_PROCESS_SERVICE_TASK_V1)
  public void testSetProcessDefinitionVersionWithFollowUpTask() {
    String processDefinitionId = repositoryService.createProcessDefinitionQuery().singleResult().getId();

    String secondDeploymentId =
        repositoryService.createDeployment().addClasspathResource(TEST_PROCESS_SERVICE_TASK_V2).deploy().getId();

    runtimeService.startProcessInstanceById(processDefinitionId);

    // execute job that triggers the migrating service task
    Job migrationJob = managementService.createJobQuery().singleResult();
    assertNotNull(migrationJob);

    managementService.executeJob(migrationJob.getId());

    Task followUpTask = taskService.createTaskQuery().singleResult();

    assertNotNull("Should have migrated to the new version and immediately executed the correct follow-up activity",
        followUpTask);

    repositoryService.deleteDeployment(secondDeploymentId, true);
  }

  @Deployment(resources = {TEST_PROCESS_WITH_MULTIPLE_PARENTS})
  public void testSetProcessDefinitionVersionWithMultipleParents(){
    // start process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("multipleJoins");

    // check that the user tasks have been reached
    assertEquals(2, taskService.createTaskQuery().count());

    //finish task1
    Task task = taskService.createTaskQuery().taskDefinitionKey("task1").singleResult();
    taskService.complete(task.getId());

    //we have reached task4
    task = taskService.createTaskQuery().taskDefinitionKey("task4").singleResult();
    assertNotNull(task);

    //The timer job has been created
    Job job = managementService.createJobQuery().executionId(task.getExecutionId()).singleResult();
    assertNotNull(job);

    // check there are 2 user tasks task4 and task2
    assertEquals(2, taskService.createTaskQuery().count());

    // deploy new version of the process definition
    org.camunda.bpm.engine.repository.Deployment deployment = repositoryService
      .createDeployment()
      .addClasspathResource(TEST_PROCESS_WITH_MULTIPLE_PARENTS)
      .deploy();
    assertEquals(2, repositoryService.createProcessDefinitionQuery().count());

    // migrate process instance to new process definition version
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutor.execute(new SetProcessDefinitionVersionCmd(pi.getId(), 2));

    // check that all executions of the instance now use the new process definition version
    ProcessDefinition newProcessDefinition = repositoryService
      .createProcessDefinitionQuery()
      .processDefinitionVersion(2)
      .singleResult();
    List<Execution> executions = runtimeService
      .createExecutionQuery()
      .processInstanceId(pi.getId())
      .list();
    for (Execution execution : executions) {
    	assertEquals(newProcessDefinition.getId(), ((ExecutionEntity) execution).getProcessDefinitionId());
    }

    // undeploy "manually" deployed process definition
  	repositoryService.deleteDeployment(deployment.getId(), true);
  }

  @Deployment(resources = TEST_PROCESS_ONE_JOB)
  public void testSetProcessDefinitionVersionMigrateJob() {
    // given a process instance
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneJobProcess");

    // with a job
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);

    // and a second deployment of the process
    org.camunda.bpm.engine.repository.Deployment deployment = repositoryService
      .createDeployment()
      .addClasspathResource(TEST_PROCESS_ONE_JOB)
      .deploy();

    ProcessDefinition newDefinition =
        repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).singleResult();
    assertNotNull(newDefinition);

    // when the process instance is migrated
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutor.execute(new SetProcessDefinitionVersionCmd(instance.getId(), 2));

    // then the the job should also be migrated
    Job migratedJob = managementService.createJobQuery().singleResult();
    assertNotNull(migratedJob);
    assertEquals(job.getId(), migratedJob.getId());
    assertEquals(newDefinition.getId(), migratedJob.getProcessDefinitionId());
    assertEquals(deployment.getId(), migratedJob.getDeploymentId());

    JobDefinition newJobDefinition = managementService
        .createJobDefinitionQuery().processDefinitionId(newDefinition.getId()).singleResult();
    assertNotNull(newJobDefinition);
    assertEquals(newJobDefinition.getId(), migratedJob.getJobDefinitionId());

    repositoryService.deleteDeployment(deployment.getId(), true);
  }

  @Deployment(resources = TEST_PROCESS_TWO_JOBS)
  public void testMigrateJobWithMultipleDefinitionsOnActivity() {
    // given a process instance
    ProcessInstance asyncAfterInstance = runtimeService.startProcessInstanceByKey("twoJobsProcess");

    // with an async after job
    String jobId = managementService.createJobQuery().singleResult().getId();
    managementService.executeJob(jobId);
    Job asyncAfterJob = managementService.createJobQuery().singleResult();

    // and a process instance with an before after job
    ProcessInstance asyncBeforeInstance = runtimeService.startProcessInstanceByKey("twoJobsProcess");
    Job asyncBeforeJob = managementService.createJobQuery()
        .processInstanceId(asyncBeforeInstance.getId()).singleResult();

    // and a second deployment of the process
    org.camunda.bpm.engine.repository.Deployment deployment = repositoryService
      .createDeployment()
      .addClasspathResource(TEST_PROCESS_TWO_JOBS)
      .deploy();

    ProcessDefinition newDefinition =
        repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).singleResult();
    assertNotNull(newDefinition);

    JobDefinition asnycBeforeJobDefinition =
        managementService.createJobDefinitionQuery()
          .jobConfiguration(MessageJobDeclaration.ASYNC_BEFORE)
          .processDefinitionId(newDefinition.getId())
          .singleResult();
    JobDefinition asnycAfterJobDefinition =
        managementService.createJobDefinitionQuery()
          .jobConfiguration(MessageJobDeclaration.ASYNC_AFTER)
          .processDefinitionId(newDefinition.getId())
          .singleResult();

    assertNotNull(asnycBeforeJobDefinition);
    assertNotNull(asnycAfterJobDefinition);

    // when the process instances are migrated
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutor.execute(new SetProcessDefinitionVersionCmd(asyncBeforeInstance.getId(), 2));
    commandExecutor.execute(new SetProcessDefinitionVersionCmd(asyncAfterInstance.getId(), 2));

    // then the the job's definition reference should also be migrated
    Job migratedAsyncBeforeJob = managementService.createJobQuery()
        .processInstanceId(asyncBeforeInstance.getId()).singleResult();
    assertEquals(asyncBeforeJob.getId(), migratedAsyncBeforeJob.getId());
    assertNotNull(migratedAsyncBeforeJob);
    assertEquals(asnycBeforeJobDefinition.getId(), migratedAsyncBeforeJob.getJobDefinitionId());

    Job migratedAsyncAfterJob = managementService.createJobQuery()
        .processInstanceId(asyncAfterInstance.getId()).singleResult();
    assertEquals(asyncAfterJob.getId(), migratedAsyncAfterJob.getId());
    assertNotNull(migratedAsyncAfterJob);
    assertEquals(asnycAfterJobDefinition.getId(), migratedAsyncAfterJob.getJobDefinitionId());

    repositoryService.deleteDeployment(deployment.getId(), true);
  }

  @Deployment(resources = TEST_PROCESS_ONE_JOB)
  public void testSetProcessDefinitionVersionMigrateIncident() {
    // given a process instance
    ProcessInstance instance =
        runtimeService.startProcessInstanceByKey("oneJobProcess", Variables.createVariables().putValue("shouldFail", true));

    // with a failed job
    executeAvailableJobs();

    // and an incident
    Incident incident = runtimeService.createIncidentQuery().singleResult();
    assertNotNull(incident);

    // and a second deployment of the process
    org.camunda.bpm.engine.repository.Deployment deployment = repositoryService
      .createDeployment()
      .addClasspathResource(TEST_PROCESS_ONE_JOB)
      .deploy();

    ProcessDefinition newDefinition =
        repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).singleResult();
    assertNotNull(newDefinition);

    // when the process instance is migrated
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutor.execute(new SetProcessDefinitionVersionCmd(instance.getId(), 2));

    // then the the incident should also be migrated
    Incident migratedIncident = runtimeService.createIncidentQuery().singleResult();
    assertNotNull(migratedIncident);
    assertEquals(newDefinition.getId(), migratedIncident.getProcessDefinitionId());
    assertEquals(instance.getId(), migratedIncident.getProcessInstanceId());
    assertEquals(instance.getId(), migratedIncident.getExecutionId());

    repositoryService.deleteDeployment(deployment.getId(), true);
  }

  @Deployment(resources = TEST_PROCESS_ATTACHED_TIMER)
  public void testSetProcessDefinitionVersionAttachedTimer() {
    // given a process instance
    ProcessInstance instance =
        runtimeService.startProcessInstanceByKey("attachedTimer");

    // and a second deployment of the process
    org.camunda.bpm.engine.repository.Deployment deployment = repositoryService
      .createDeployment()
      .addClasspathResource(TEST_PROCESS_ATTACHED_TIMER)
      .deploy();

    ProcessDefinition newDefinition =
        repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).singleResult();
    assertNotNull(newDefinition);

    // when the process instance is migrated
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutor.execute(new SetProcessDefinitionVersionCmd(instance.getId(), 2));

    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);
    assertEquals(newDefinition.getId(), job.getProcessDefinitionId());

    repositoryService.deleteDeployment(deployment.getId(), true);
  }

  public void testHistoryOfSetProcessDefinitionVersionCmd() {
    // given
    String resource = "org/camunda/bpm/engine/test/api/runtime/migration/SetProcessDefinitionVersionCmdTest.bpmn";

    // Deployments
    org.camunda.bpm.engine.repository.Deployment firstDeployment = repositoryService
        .createDeployment()
        .addClasspathResource(resource)
        .deploy();

    org.camunda.bpm.engine.repository.Deployment secondDeployment = repositoryService
        .createDeployment()
        .addClasspathResource(resource)
        .deploy();

    // Process definitions
    ProcessDefinition processDefinitionV1 = repositoryService
        .createProcessDefinitionQuery()
        .deploymentId(firstDeployment.getId())
        .singleResult();

    ProcessDefinition processDefinitionV2 = repositoryService
        .createProcessDefinitionQuery()
        .deploymentId(secondDeployment.getId())
        .singleResult();

    // start process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinitionV1.getId());

    // when
    setProcessDefinitionVersion(processInstance.getId(), 2);

    // then
    ProcessInstance processInstanceAfterMigration = runtimeService
        .createProcessInstanceQuery()
        .processInstanceId(processInstance.getId())
        .singleResult();
    assertEquals(processDefinitionV2.getId(), processInstanceAfterMigration.getProcessDefinitionId());

    if(processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      HistoricProcessInstance historicProcessInstance = historyService
          .createHistoricProcessInstanceQuery()
          .processInstanceId(processInstance.getId())
          .singleResult();
      assertEquals(processDefinitionV2.getId(), historicProcessInstance.getProcessDefinitionId());
    }

    // Clean up the test
    repositoryService.deleteDeployment(firstDeployment.getId(), true);
    repositoryService.deleteDeployment(secondDeployment.getId(), true);
  }

  public void testOpLogSetProcessDefinitionVersionCmd() {
    // given
    try {
      identityService.setAuthenticatedUserId("demo");
      String resource = "org/camunda/bpm/engine/test/api/runtime/migration/SetProcessDefinitionVersionCmdTest.bpmn";

      // Deployments
      org.camunda.bpm.engine.repository.Deployment firstDeployment = repositoryService
          .createDeployment()
          .addClasspathResource(resource)
          .deploy();

      org.camunda.bpm.engine.repository.Deployment secondDeployment = repositoryService
          .createDeployment()
          .addClasspathResource(resource)
          .deploy();

      // Process definitions
      ProcessDefinition processDefinitionV1 = repositoryService
          .createProcessDefinitionQuery()
          .deploymentId(firstDeployment.getId())
          .singleResult();

      ProcessDefinition processDefinitionV2 = repositoryService
          .createProcessDefinitionQuery()
          .deploymentId(secondDeployment.getId())
          .singleResult();

      // start process instance
      ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinitionV1.getId());

      // when
      setProcessDefinitionVersion(processInstance.getId(), 2);

      // then
      ProcessInstance processInstanceAfterMigration = runtimeService
          .createProcessInstanceQuery()
          .processInstanceId(processInstance.getId())
          .singleResult();
      assertEquals(processDefinitionV2.getId(), processInstanceAfterMigration.getProcessDefinitionId());

      if (processEngineConfiguration.getHistoryLevel().equals(HistoryLevel.HISTORY_LEVEL_FULL)) {
        List<UserOperationLogEntry> userOperations = historyService
            .createUserOperationLogQuery()
            .processInstanceId(processInstance.getId())
            .list();

        assertEquals(1, userOperations.size());

        UserOperationLogEntry userOperationLogEntry = userOperations.get(0);
        assertEquals(UserOperationLogEntry.OPERATION_TYPE_MODIFY_PROCESS_INSTANCE, userOperationLogEntry.getOperationType());
        assertEquals("processDefinitionVersion", userOperationLogEntry.getProperty());
        assertEquals("1", userOperationLogEntry.getOrgValue());
        assertEquals("2", userOperationLogEntry.getNewValue());
      }

      // Clean up the test
      repositoryService.deleteDeployment(firstDeployment.getId(), true);
      repositoryService.deleteDeployment(secondDeployment.getId(), true);
    } finally {
      identityService.clearAuthentication();
    }
  }

  protected void setProcessDefinitionVersion(String processInstanceId, int newProcessDefinitionVersion) {
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequiresNew();
    commandExecutor.execute(new SetProcessDefinitionVersionCmd(processInstanceId, newProcessDefinitionVersion));
  }
}
