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
package org.camunda.bpm.engine.impl.cfg.auth;

import org.camunda.bpm.engine.history.HistoricCaseInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.impl.batch.history.HistoricBatchEntity;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.CompositePermissionCheck;
import org.camunda.bpm.engine.impl.db.PermissionCheckBuilder;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionRequirementsDefinitionEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricExternalTaskLogEntity;
import org.camunda.bpm.engine.impl.persistence.entity.*;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.CaseExecution;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.*;
import static org.camunda.bpm.engine.authorization.Resources.*;

import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions;
import org.camunda.bpm.engine.authorization.ProcessInstancePermissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.authorization.SystemPermissions;
import org.camunda.bpm.engine.authorization.TaskPermissions;
import org.camunda.bpm.engine.authorization.UserOperationLogCategoryPermissions;

/**
 * {@link CommandChecker} that uses the {@link AuthorizationManager} to perform
 * authorization checks.
 */
public class AuthorizationCommandChecker implements CommandChecker {

  @Override
  public void checkEvaluateDecision(DecisionDefinition decisionDefinition) {
    getAuthorizationManager().checkAuthorization(CREATE_INSTANCE, DECISION_DEFINITION, decisionDefinition.getKey());
  }

  @Override
  public void checkCreateProcessInstance(ProcessDefinition processDefinition) {
    // necessary permissions:
    // - CREATE on PROCESS_INSTANCE
    // AND
    // - CREATE_INSTANCE on PROCESS_DEFINITION
    getAuthorizationManager().checkAuthorization(CREATE, PROCESS_INSTANCE);
    getAuthorizationManager().checkAuthorization(CREATE_INSTANCE, PROCESS_DEFINITION, processDefinition.getKey());
  }

  @Override
  public void checkReadProcessDefinition(ProcessDefinition processDefinition) {
    getAuthorizationManager().checkAuthorization(READ, PROCESS_DEFINITION, processDefinition.getKey());
  }

  @Override
  public void checkCreateCaseInstance(CaseDefinition caseDefinition) {
    // no authorization check for CMMN
  }

  @Override
  public void checkUpdateProcessDefinitionById(String processDefinitionId) {
    if (getAuthorizationManager().isAuthorizationEnabled()) {
      ProcessDefinitionEntity processDefinition = findLatestProcessDefinitionById(processDefinitionId);
      if (processDefinition != null) {
        checkUpdateProcessDefinitionByKey(processDefinition.getKey());
      }
    }
  }

  @Override
  public void checkUpdateProcessDefinitionSuspensionStateById(String processDefinitionId) {
    if (getAuthorizationManager().isAuthorizationEnabled()) {
      ProcessDefinitionEntity processDefinition = findLatestProcessDefinitionById(processDefinitionId);
      if (processDefinition != null) {
        checkUpdateProcessDefinitionSuspensionStateByKey(processDefinition.getKey());
      }
    }
  }

  @Override
  public void checkUpdateDecisionDefinitionById(String decisionDefinitionId) {
    if (getAuthorizationManager().isAuthorizationEnabled()) {
      DecisionDefinitionEntity decisionDefinition = findLatestDecisionDefinitionById(decisionDefinitionId);
      if (decisionDefinition != null) {
        checkUpdateDecisionDefinition(decisionDefinition);
      }
    }
  }

  @Override
  public void checkUpdateProcessDefinitionByKey(String processDefinitionKey) {
    getAuthorizationManager().checkAuthorization(UPDATE, PROCESS_DEFINITION, processDefinitionKey);
  }

  @Override
  public void checkUpdateProcessDefinitionSuspensionStateByKey(String processDefinitionKey) {
    CompositePermissionCheck suspensionStatePermission = new PermissionCheckBuilder()
        .disjunctive()
          .atomicCheckForResourceId(PROCESS_DEFINITION, processDefinitionKey, ProcessDefinitionPermissions.SUSPEND)
          .atomicCheckForResourceId(PROCESS_DEFINITION, processDefinitionKey, UPDATE)
        .build();

    getAuthorizationManager().checkAuthorization(suspensionStatePermission);
  }

  @Override
  public void checkDeleteProcessDefinitionById(String processDefinitionId) {
    if (getAuthorizationManager().isAuthorizationEnabled()) {
      ProcessDefinitionEntity processDefinition = findLatestProcessDefinitionById(processDefinitionId);
      if (processDefinition != null) {
        checkDeleteProcessDefinitionByKey(processDefinition.getKey());
      }
    }
  }

  @Override
  public void checkDeleteProcessDefinitionByKey(String processDefinitionKey) {
    getAuthorizationManager().checkAuthorization(DELETE, PROCESS_DEFINITION, processDefinitionKey);
  }

  @Override
  public void checkUpdateProcessInstanceByProcessDefinitionId(String processDefinitionId) {
    if (getAuthorizationManager().isAuthorizationEnabled()) {
      ProcessDefinitionEntity processDefinition = findLatestProcessDefinitionById(processDefinitionId);
      if (processDefinition != null) {
        checkUpdateProcessInstanceByProcessDefinitionKey(processDefinition.getKey());
      }
    }
  }

  @Override
  public void checkUpdateRetriesProcessInstanceByProcessDefinitionId(String processDefinitionId) {
    if (getAuthorizationManager().isAuthorizationEnabled()) {
      ProcessDefinitionEntity processDefinition = findLatestProcessDefinitionById(processDefinitionId);
      if (processDefinition != null) {

        CompositePermissionCheck retryJobPermission = new PermissionCheckBuilder()
            .disjunctive()
              .atomicCheckForResourceId(PROCESS_INSTANCE, ANY, ProcessInstancePermissions.RETRY_JOB)
              .atomicCheckForResourceId(PROCESS_DEFINITION, processDefinition.getKey(), ProcessDefinitionPermissions.RETRY_JOB)
              .atomicCheckForResourceId(PROCESS_INSTANCE, ANY, UPDATE)
              .atomicCheckForResourceId(PROCESS_DEFINITION, processDefinition.getKey(), UPDATE_INSTANCE)
            .build();

        getAuthorizationManager().checkAuthorization(retryJobPermission);
      }
    }
  }

  @Override
  public void checkUpdateProcessInstanceSuspensionStateByProcessDefinitionId(String processDefinitionId) {
    if (getAuthorizationManager().isAuthorizationEnabled()) {
      ProcessDefinitionEntity processDefinition = findLatestProcessDefinitionById(processDefinitionId);
      if (processDefinition != null) {
        checkUpdateProcessInstanceSuspensionStateByProcessDefinitionKey(processDefinition.getKey());
      }
    }
  }

  @Override
  public void checkUpdateProcessInstanceByProcessDefinitionKey(String processDefinitionKey) {
    CompositePermissionCheck suspensionStatePermission = new PermissionCheckBuilder()
        .disjunctive()
          .atomicCheckForResourceId(PROCESS_INSTANCE, null, UPDATE)
          .atomicCheckForResourceId(PROCESS_DEFINITION, processDefinitionKey, UPDATE_INSTANCE)
        .build();

    getAuthorizationManager().checkAuthorization(suspensionStatePermission);
  }

  @Override
  public void checkUpdateProcessInstanceSuspensionStateByProcessDefinitionKey(String processDefinitionKey) {
    CompositePermissionCheck suspensionStatePermission = new PermissionCheckBuilder()
        .disjunctive()
          .atomicCheckForResourceId(PROCESS_INSTANCE, null, ProcessInstancePermissions.SUSPEND)
          .atomicCheckForResourceId(PROCESS_DEFINITION, processDefinitionKey, ProcessDefinitionPermissions.SUSPEND_INSTANCE)
          .atomicCheckForResourceId(PROCESS_INSTANCE, null, UPDATE)
          .atomicCheckForResourceId(PROCESS_DEFINITION, processDefinitionKey, UPDATE_INSTANCE)
        .build();

    getAuthorizationManager().checkAuthorization(suspensionStatePermission);
  }

  public void checkReadProcessInstance(String processInstanceId) {
    ExecutionEntity execution = findExecutionById(processInstanceId);
    if (execution != null) {
      checkReadProcessInstance(execution);
    }
  }

  public void checkDeleteProcessInstance(ExecutionEntity execution) {
    ProcessDefinitionEntity processDefinition = execution.getProcessDefinition();

    // necessary permissions:
    // - DELETE on PROCESS_INSTANCE
    // ... OR ...
    // - DELETE_INSTANCE on PROCESS_DEFINITION

    CompositePermissionCheck deleteInstancePermission = new PermissionCheckBuilder()
        .disjunctive()
          .atomicCheckForResourceId(PROCESS_INSTANCE, execution.getProcessInstanceId(), DELETE)
          .atomicCheckForResourceId(PROCESS_DEFINITION, processDefinition.getKey(), DELETE_INSTANCE)
        .build();

    getAuthorizationManager().checkAuthorization(deleteInstancePermission);
  }

  @Override
  public void checkUpdateProcessInstanceById(String processInstanceId) {
    ExecutionEntity execution = findExecutionById(processInstanceId);
    if (execution != null) {
      checkUpdateProcessInstance(execution);
    }
  }

  @Override
  public void checkUpdateProcessInstance(ExecutionEntity execution) {
    ProcessDefinitionEntity processDefinition = execution.getProcessDefinition();
    CompositePermissionCheck suspensionStatePermission = new PermissionCheckBuilder()
        .disjunctive()
          .atomicCheckForResourceId(PROCESS_INSTANCE, execution.getProcessInstanceId(), UPDATE)
          .atomicCheckForResourceId(PROCESS_DEFINITION, processDefinition.getKey(), UPDATE_INSTANCE)
        .build();

    getAuthorizationManager().checkAuthorization(suspensionStatePermission);
  }

  @Override
  public void checkUpdateProcessInstanceVariables(ExecutionEntity execution) {
    ProcessDefinitionEntity processDefinition = execution.getProcessDefinition();
    CompositePermissionCheck suspensionStatePermission = new PermissionCheckBuilder()
        .disjunctive()
          .atomicCheckForResourceId(PROCESS_INSTANCE, execution.getProcessInstanceId(), ProcessInstancePermissions.UPDATE_VARIABLE)
          .atomicCheckForResourceId(PROCESS_DEFINITION, processDefinition.getKey(), ProcessDefinitionPermissions.UPDATE_INSTANCE_VARIABLE)
          .atomicCheckForResourceId(PROCESS_INSTANCE, execution.getProcessInstanceId(), UPDATE)
          .atomicCheckForResourceId(PROCESS_DEFINITION, processDefinition.getKey(), UPDATE_INSTANCE)
        .build();

    getAuthorizationManager().checkAuthorization(suspensionStatePermission);
  }

  @Override
  public void checkUpdateProcessInstanceSuspensionStateById(String processInstanceId) {
    ExecutionEntity execution = findExecutionById(processInstanceId);
    if (execution != null) {
      checkUpdateProcessInstanceSuspensionState(execution);
    }
  }

  public void checkUpdateProcessInstanceSuspensionState(ExecutionEntity execution) {
    ProcessDefinitionEntity processDefinition = execution.getProcessDefinition();
    CompositePermissionCheck suspensionStatePermission = new PermissionCheckBuilder()
        .disjunctive()
          .atomicCheckForResourceId(PROCESS_INSTANCE, execution.getProcessInstanceId(), ProcessInstancePermissions.SUSPEND)
          .atomicCheckForResourceId(PROCESS_DEFINITION, processDefinition.getKey(), ProcessDefinitionPermissions.SUSPEND_INSTANCE)
          .atomicCheckForResourceId(PROCESS_INSTANCE, execution.getProcessInstanceId(), UPDATE)
          .atomicCheckForResourceId(PROCESS_DEFINITION, processDefinition.getKey(), UPDATE_INSTANCE)
        .build();

    getAuthorizationManager().checkAuthorization(suspensionStatePermission);
  }

  public void checkUpdateJob(JobEntity job) {
    if (job.getProcessDefinitionKey() == null) {
      // "standalone" job: nothing to do!
      return;
    }

    CompositePermissionCheck retryJobPermission = new PermissionCheckBuilder()
        .disjunctive()
          .atomicCheckForResourceId(PROCESS_INSTANCE, job.getProcessInstanceId(), UPDATE)
          .atomicCheckForResourceId(PROCESS_DEFINITION, job.getProcessDefinitionKey(), UPDATE_INSTANCE)
        .build();

    getAuthorizationManager().checkAuthorization(retryJobPermission);
  }

  @Override
  public void checkUpdateRetriesJob(JobEntity job) {
    if (job.getProcessDefinitionKey() == null) {
      // "standalone" job: nothing to do!
      return;
    }

    CompositePermissionCheck retryJobPermission = new PermissionCheckBuilder()
        .disjunctive()
          .atomicCheckForResourceId(PROCESS_INSTANCE, job.getProcessInstanceId(), ProcessInstancePermissions.RETRY_JOB)
          .atomicCheckForResourceId(PROCESS_DEFINITION, job.getProcessDefinitionKey(), ProcessDefinitionPermissions.RETRY_JOB)
          .atomicCheckForResourceId(PROCESS_INSTANCE, job.getProcessInstanceId(), UPDATE)
          .atomicCheckForResourceId(PROCESS_DEFINITION, job.getProcessDefinitionKey(), UPDATE_INSTANCE)
        .build();

    getAuthorizationManager().checkAuthorization(retryJobPermission);
  }

  @Override
  public void checkCreateMigrationPlan(ProcessDefinition sourceProcessDefinition, ProcessDefinition targetProcessDefinition) {
    checkReadProcessDefinition(sourceProcessDefinition);
    checkReadProcessDefinition(targetProcessDefinition);
  }

  @Override
  public void checkMigrateProcessInstance(ExecutionEntity processInstance, ProcessDefinition targetProcessDefinition) {
  }

  public void checkReadProcessInstance(ExecutionEntity execution) {
    ProcessDefinitionEntity processDefinition = execution.getProcessDefinition();

    // necessary permissions:
    // - READ on PROCESS_INSTANCE
    // ... OR ...
    // - READ_INSTANCE on PROCESS_DEFINITION
    CompositePermissionCheck readProcessInstancePermission = new PermissionCheckBuilder()
        .disjunctive()
          .atomicCheckForResourceId(PROCESS_INSTANCE, execution.getProcessInstanceId(), READ)
          .atomicCheckForResourceId(PROCESS_DEFINITION, processDefinition.getKey(), READ_INSTANCE)
        .build();

    getAuthorizationManager().checkAuthorization(readProcessInstancePermission);
  }

  @Override
  public void checkReadProcessInstanceVariable(ExecutionEntity execution) {
    if (getAuthorizationManager().isEnsureSpecificVariablePermission()) {
      ProcessDefinitionEntity processDefinition = execution.getProcessDefinition();

      // necessary permissions:
      // - READ_INSTANCE_VARIABLE on PROCESS_DEFINITION
      CompositePermissionCheck readProcessInstancePermission = new PermissionCheckBuilder()
          .disjunctive()
          .atomicCheckForResourceId(PROCESS_DEFINITION, processDefinition.getKey(), ProcessDefinitionPermissions.READ_INSTANCE_VARIABLE)
          .build();

      getAuthorizationManager().checkAuthorization(readProcessInstancePermission);
    } else {
      checkReadProcessInstance(execution);
    }
  }

  public void checkReadJob(JobEntity job) {
    if (job.getProcessDefinitionKey() == null) {
      // "standalone" job: nothing to do!
      return;
    }

    // necessary permissions:
    // - READ on PROCESS_INSTANCE
    // ... OR ...
    // - READ_INSTANCE on PROCESS_DEFINITION
    CompositePermissionCheck readJobPermission = new PermissionCheckBuilder()
        .disjunctive()
        .atomicCheckForResourceId(PROCESS_INSTANCE, job.getProcessInstanceId(), READ)
        .atomicCheckForResourceId(PROCESS_DEFINITION, job.getProcessDefinitionKey(), READ_INSTANCE)
        .build();

    getAuthorizationManager().checkAuthorization(readJobPermission);
  }

  @Override
  public void checkReadTask(TaskEntity task) {
    checkTaskPermission(task, READ_TASK, READ);
  }

  @Override
  public void checkReadTaskVariable(TaskEntity task) {
    Permission readProcessInstanceTaskPermission;
    Permission readStandaloneTaskPermission;
    if (getAuthorizationManager().isEnsureSpecificVariablePermission()) {
      readProcessInstanceTaskPermission = ProcessDefinitionPermissions.READ_TASK_VARIABLE;
      readStandaloneTaskPermission = TaskPermissions.READ_VARIABLE;
    } else {
      readProcessInstanceTaskPermission = READ_TASK;
      readStandaloneTaskPermission = READ;
    }
    checkTaskPermission(task, readProcessInstanceTaskPermission, readStandaloneTaskPermission);
  }

  protected void checkTaskPermission(TaskEntity task, Permission processDefinitionPermission, Permission taskPermission) {
    String taskId = task.getId();
    String executionId = task.getExecutionId();

    if (executionId != null) {

      // if task exists in context of a process instance
      // then check the following permissions:
      // - 'taskPermission' on TASK
      // - 'processDefinitionPermission' on PROCESS_DEFINITION

      ExecutionEntity execution = task.getExecution();
      ProcessDefinitionEntity processDefinition = execution.getProcessDefinition();

      CompositePermissionCheck readTaskPermission = new PermissionCheckBuilder()
          .disjunctive()
          .atomicCheckForResourceId(TASK, taskId, taskPermission)
          .atomicCheckForResourceId(PROCESS_DEFINITION, processDefinition.getKey(), processDefinitionPermission)
        .build();

      getAuthorizationManager().checkAuthorization(readTaskPermission);

    } else {

      // if task does not exist in context of process
      // instance, then it is either a (a) standalone task
      // or (b) it exists in context of a case instance.

      // (a) standalone task: check following permission
      // - 'taskPermission' on TASK
      // (b) task in context of a case instance, in this
      // case it is not necessary to check any permission,
      // because such tasks can always be read

      String caseExecutionId = task.getCaseExecutionId();
      if (caseExecutionId == null) {
        getAuthorizationManager().checkAuthorization(taskPermission, TASK, taskId);
      }

    }
  }

  public void checkUpdateTaskVariable(TaskEntity task) {
    String taskId = task.getId();

    String executionId = task.getExecutionId();
    if (executionId != null) {

      // if task exists in context of a process instance
      // then check the following permissions:
      // - UPDATE_VARIABLE on TASK
      // - UPDATE_TASK_VARIABLE on PROCESS_DEFINITION
      // - UPDATE on TASK
      // - UPDATE_TASK on PROCESS_DEFINITION

      ExecutionEntity execution = task.getExecution();
      ProcessDefinitionEntity processDefinition = execution.getProcessDefinition();

      CompositePermissionCheck updateTaskPermissionCheck = new PermissionCheckBuilder()
          .disjunctive()
            .atomicCheckForResourceId(TASK, taskId, TaskPermissions.UPDATE_VARIABLE)
            .atomicCheckForResourceId(PROCESS_DEFINITION, processDefinition.getKey(), ProcessDefinitionPermissions.UPDATE_TASK_VARIABLE)
            .atomicCheckForResourceId(TASK, taskId, UPDATE)
            .atomicCheckForResourceId(PROCESS_DEFINITION, processDefinition.getKey(), UPDATE_TASK)
          .build();

      getAuthorizationManager().checkAuthorization(updateTaskPermissionCheck);

    } else {

      // if task does not exist in context of process
      // instance, then it is either a (a) standalone task
      // or (b) it exists in context of a case instance.

      // (a) standalone task: check following permission
      // - READ on TASK
      // (b) task in context of a case instance, in this
      // case it is not necessary to check any permission,
      // because such tasks can always be updated

      String caseExecutionId = task.getCaseExecutionId();
      if (caseExecutionId == null) {
        // standalone task
        CompositePermissionCheck updateTaskPermissionCheck = new PermissionCheckBuilder()
            .disjunctive()
              .atomicCheckForResourceId(TASK, taskId, TaskPermissions.UPDATE_VARIABLE)
              .atomicCheckForResourceId(TASK, taskId, UPDATE)
            .build();

        getAuthorizationManager().checkAuthorization(updateTaskPermissionCheck);
      }

    }
  }

  @Override
  public void checkCreateBatch(Permission permission) {
    CompositePermissionCheck createBatchPermission = new PermissionCheckBuilder()
      .disjunctive()
        .atomicCheckForResourceId(BATCH, null, permission)
        .atomicCheckForResourceId(BATCH, null, CREATE)
      .build();

    getAuthorizationManager().checkAuthorization(createBatchPermission);
  }

  @Override
  public void checkDeleteBatch(BatchEntity batch) {
    getAuthorizationManager().checkAuthorization(DELETE, BATCH, batch.getId());
  }

  @Override
  public void checkDeleteHistoricBatch(HistoricBatchEntity batch) {
    getAuthorizationManager().checkAuthorization(DELETE_HISTORY, BATCH, batch.getId());
  }

  public void checkSuspendBatch(BatchEntity batch) {
    getAuthorizationManager().checkAuthorization(UPDATE, BATCH, batch.getId());
  }

  public void checkActivateBatch(BatchEntity batch) {
    getAuthorizationManager().checkAuthorization(UPDATE, BATCH, batch.getId());
  }

  public void checkReadHistoricBatch() {
    getAuthorizationManager().checkAuthorization(READ_HISTORY, BATCH);
  }

  /* DEPLOYMENT */

  // create permission ////////////////////////////////////////////////

  public void checkCreateDeployment() {
    getAuthorizationManager().checkAuthorization(CREATE, DEPLOYMENT);
  }

  // read permission //////////////////////////////////////////////////

  public void checkReadDeployment(String deploymentId) {
    getAuthorizationManager().checkAuthorization(READ, DEPLOYMENT, deploymentId);
  }

  // delete permission //////////////////////////////////////////////////

  public void checkDeleteDeployment(String deploymentId) {
    getAuthorizationManager().checkAuthorization(DELETE, DEPLOYMENT, deploymentId);
  }

  public void checkReadDecisionDefinition(DecisionDefinitionEntity decisionDefinition) {
    getAuthorizationManager().checkAuthorization(READ, DECISION_DEFINITION, decisionDefinition.getKey());
  }

  public void checkUpdateDecisionDefinition(DecisionDefinitionEntity decisionDefinition) {
    getAuthorizationManager().checkAuthorization(UPDATE, DECISION_DEFINITION, decisionDefinition.getKey());
  }

  public void checkReadDecisionRequirementsDefinition(DecisionRequirementsDefinitionEntity decisionRequirementsDefinition) {
    getAuthorizationManager().checkAuthorization(READ, DECISION_REQUIREMENTS_DEFINITION, decisionRequirementsDefinition.getKey());
  }

  @Override
  public void checkReadCaseDefinition(CaseDefinition caseDefinition) {
  }

  @Override
  public void checkUpdateCaseDefinition(CaseDefinition caseDefinition) {
  }

  // delete permission ////////////////////////////////////////

  public void checkDeleteHistoricTaskInstance(HistoricTaskInstanceEntity task) {
    // deleting unexisting historic task instance should be silently ignored
    // see javaDoc HistoryService.deleteHistoricTaskInstance
    if (task != null) {
      if (task.getProcessDefinitionKey() != null) {
        getAuthorizationManager().checkAuthorization(DELETE_HISTORY, PROCESS_DEFINITION, task.getProcessDefinitionKey());
      }
    }
  }

  // delete permission /////////////////////////////////////////////////

  public void checkDeleteHistoricProcessInstance(HistoricProcessInstance instance) {
    getAuthorizationManager().checkAuthorization(DELETE_HISTORY, PROCESS_DEFINITION, instance.getProcessDefinitionKey());
  }

  public void checkDeleteHistoricCaseInstance(HistoricCaseInstance instance) {
  }

  public void checkDeleteHistoricDecisionInstance(String decisionDefinitionKey) {
    getAuthorizationManager().checkAuthorization(DELETE_HISTORY, DECISION_DEFINITION, decisionDefinitionKey);
  }

  public void checkDeleteHistoricDecisionInstance(HistoricDecisionInstance decisionInstance) {
    getAuthorizationManager().checkAuthorization(
        DELETE_HISTORY, DECISION_DEFINITION, decisionInstance.getDecisionDefinitionKey()
    );
  }

  public void checkReadHistoricJobLog(HistoricJobLogEventEntity historicJobLog) {
    if (historicJobLog.getProcessDefinitionKey() != null) {
      getAuthorizationManager().checkAuthorization(READ_HISTORY, PROCESS_DEFINITION, historicJobLog.getProcessDefinitionKey());
    }
  }

  public void checkReadHistoryAnyProcessDefinition() {
    getAuthorizationManager().checkAuthorization(READ_HISTORY, PROCESS_DEFINITION, ANY);
  }

  public void checkReadHistoryProcessDefinition(String processDefinitionKey) {
    getAuthorizationManager().checkAuthorization(READ_HISTORY, PROCESS_DEFINITION, processDefinitionKey);
  }

  @Override
  public void checkUpdateCaseInstance(CaseExecution caseExecution) {
  }

  @Override
  public void checkReadCaseInstance(CaseExecution caseExecution) {
  }

  public void checkTaskAssign(TaskEntity task) {

    String taskId = task.getId();

    String executionId = task.getExecutionId();
    if (executionId != null) {

      // Permissions to task actions is based on the order in which PermissioncheckBuilder is built
      CompositePermissionCheck taskWorkPermission = new PermissionCheckBuilder()
        .disjunctive()
          .atomicCheckForResourceId(TASK, taskId, TASK_ASSIGN)
          .atomicCheckForResourceId(PROCESS_DEFINITION, task.getProcessDefinition().getKey(), TASK_ASSIGN)
          .atomicCheckForResourceId(TASK, taskId, UPDATE)
          .atomicCheckForResourceId(PROCESS_DEFINITION, task.getProcessDefinition().getKey(), UPDATE_TASK)
        .build();

      getAuthorizationManager().checkAuthorization(taskWorkPermission);

    }
    else {

      // if task does not exist in context of process
      // instance, then it is either a (a) standalone task
      // or (b) it exists in context of a case instance.

      // (a) standalone task: check following permission
      // - TASK_ASSIGN or UPDATE
      // (b) task in context of a case instance, in this
      // case it is not necessary to check any permission,
      // because such tasks can always be updated

      String caseExecutionId = task.getCaseExecutionId();
      if (caseExecutionId == null) {
        // standalone task
        CompositePermissionCheck taskWorkPermission = new PermissionCheckBuilder()
            .disjunctive()
            .atomicCheckForResourceId(TASK, taskId, TASK_ASSIGN)
            .atomicCheckForResourceId(TASK, taskId, UPDATE)
          .build();

        getAuthorizationManager().checkAuthorization(taskWorkPermission);
      }
    }
  }

  // create permission /////////////////////////////////////////////

  public void checkCreateTask(TaskEntity entity) {
    getAuthorizationManager().checkAuthorization(CREATE, TASK);
  }

  public void checkCreateTask() {
    getAuthorizationManager().checkAuthorization(CREATE, TASK);
  }

  @Override
  public void checkTaskWork(TaskEntity task) {

    String taskId = task.getId();

    String executionId = task.getExecutionId();
    if (executionId != null) {

      // Permissions to task actions is based on the order in which PermissioncheckBuilder is built
      CompositePermissionCheck taskWorkPermission = new PermissionCheckBuilder()
          .disjunctive()
          .atomicCheckForResourceId(TASK, taskId, TASK_WORK)
          .atomicCheckForResourceId(PROCESS_DEFINITION, task.getProcessDefinition().getKey(), TASK_WORK)
          .atomicCheckForResourceId(TASK, taskId, UPDATE)
          .atomicCheckForResourceId(PROCESS_DEFINITION, task.getProcessDefinition().getKey(), UPDATE_TASK)
        .build();

      getAuthorizationManager().checkAuthorization(taskWorkPermission);

    }
    else {

      // if task does not exist in context of process
      // instance, then it is either a (a) standalone task
      // or (b) it exists in context of a case instance.

      // (a) standalone task: check following permission
      // - TASK_WORK or UPDATE
      // (b) task in context of a case instance, in this
      // case it is not necessary to check any permission,
      // because such tasks can always be updated

      String caseExecutionId = task.getCaseExecutionId();
      if (caseExecutionId == null) {
        // standalone task
        CompositePermissionCheck taskWorkPermission = new PermissionCheckBuilder()
            .disjunctive()
            .atomicCheckForResourceId(TASK, taskId, TASK_WORK)
            .atomicCheckForResourceId(TASK, taskId, UPDATE)
          .build();

          getAuthorizationManager().checkAuthorization(taskWorkPermission);
      }
    }
  }

  public void checkDeleteTask(TaskEntity task) {
    String taskId = task.getId();

    // Note: Calling TaskService#deleteTask() to
    // delete a task which exists in context of
    // a process instance or case instance cannot
    // be deleted. In such a case TaskService#deleteTask()
    // throws an exception before invoking the
    // authorization check.

    String executionId = task.getExecutionId();
    String caseExecutionId = task.getCaseExecutionId();

    if (executionId == null && caseExecutionId == null) {
      getAuthorizationManager().checkAuthorization(DELETE, TASK, taskId);
    }
  }

  public void checkUserOperationLog(UserOperationLogEntry entry,
                                    ProcessDefinitionPermissions processDefinitionPermission,
                                    UserOperationLogCategoryPermissions operationLogCategoryPermission) {
    /*
     * (1) if entry has a category and a process definition key:
     *   => entry in context of process definition
     *   => check either
     *        UPDATE_/DELETE_HISTORY on PROCESS_DEFINITION with processDefinitionKey OR
     *        UPDATE/DELETE OPERATION_LOG_CATEGORY with category
     *
     * (2) if entry has a category but no process definition key:
     *   => standalone entry (task, job, batch, ...), admin entry (user, tenant, ...) or CMMN related
     *   => check UPDATE/DELETE on OPERATION_LOG_CATEGORY with category
     *
     * (3) if entry has no category but a process definition key:
     *   => pre-7.11.0 entry in context of process definition
     *   => check UPDATE_/DELETE_HISTORY on PROCESS_DEFINITION with processDefinitionKey
     *
     * (4) if entry has no category and no process definition key:
     *   => pre-7.11.0 standalone entry (task, job, batch, ...) or CMMN related
     *   => no authorization check like before 7.11.0
     */
    if (entry != null) {
      String category = entry.getCategory();
      String processDefinitionKey = entry.getProcessDefinitionKey();
      if (category != null || processDefinitionKey != null) {
        CompositePermissionCheck permissionCheck = null;
        if (category == null) {
          // case (3)
          permissionCheck = new PermissionCheckBuilder()
              .atomicCheckForResourceId(PROCESS_DEFINITION, processDefinitionKey, processDefinitionPermission)
              .build();
        } else if (processDefinitionKey == null) {
          // case (2)
          permissionCheck = new PermissionCheckBuilder()
              .atomicCheckForResourceId(Resources.OPERATION_LOG_CATEGORY, category, operationLogCategoryPermission)
              .build();
        } else {
          // case (1)
          permissionCheck = new PermissionCheckBuilder()
              .disjunctive()
                .atomicCheckForResourceId(PROCESS_DEFINITION, processDefinitionKey, processDefinitionPermission)
                .atomicCheckForResourceId(Resources.OPERATION_LOG_CATEGORY, category, operationLogCategoryPermission)
              .build();
        }
        getAuthorizationManager().checkAuthorization(permissionCheck);
      }
      // case (4)
    }
  }

  @Override
  public void checkDeleteUserOperationLog(UserOperationLogEntry entry) {
    checkUserOperationLog(entry, ProcessDefinitionPermissions.DELETE_HISTORY, UserOperationLogCategoryPermissions.DELETE);
  }

  @Override
  public void checkUpdateUserOperationLog(UserOperationLogEntry entry) {
    checkUserOperationLog(entry, ProcessDefinitionPermissions.UPDATE_HISTORY, UserOperationLogCategoryPermissions.UPDATE);
  }

  @Override
  public void checkReadHistoricExternalTaskLog(HistoricExternalTaskLogEntity historicExternalTaskLog) {
    if (historicExternalTaskLog.getProcessDefinitionKey() != null) {
      getAuthorizationManager().checkAuthorization(READ_HISTORY, PROCESS_DEFINITION, historicExternalTaskLog.getProcessDefinitionKey());
    }
  }

  @Override
  public void checkDeleteHistoricVariableInstance(HistoricVariableInstanceEntity variable) {
    if (variable != null && variable.getProcessDefinitionKey() != null) {
      getAuthorizationManager().checkAuthorization(DELETE_HISTORY, PROCESS_DEFINITION, variable.getProcessDefinitionKey());
    }
    // XXX if CAM-6570 is implemented, there should be a check for variables of standalone tasks here as well
  }

  @Override
  public void checkDeleteHistoricVariableInstancesByProcessInstance(HistoricProcessInstanceEntity instance) {
    checkDeleteHistoricProcessInstance(instance);
  }

  @Override
  public void checkReadTelemetryData() {
    getAuthorizationManager().checkAuthorization(SystemPermissions.READ, Resources.SYSTEM);
  }

  @Override
  public void checkConfigureTelemetry() {
    getAuthorizationManager().checkAuthorization(SystemPermissions.SET, Resources.SYSTEM);
  }

  @Override
  public void checkReadTelemetryCollectionStatusData() {
    getAuthorizationManager().checkAuthorization(SystemPermissions.READ, Resources.SYSTEM);
  }

  @Override
  public void checkReadHistoryLevel() {
    getAuthorizationManager().checkAuthorization(SystemPermissions.READ, Resources.SYSTEM);
  }

  @Override
  public void checkReadTableCount() {
    getAuthorizationManager().checkAuthorization(SystemPermissions.READ, Resources.SYSTEM);
  }

  @Override
  public void checkReadTableName() {
    getAuthorizationManager().checkAuthorization(SystemPermissions.READ, Resources.SYSTEM);
  }

  @Override
  public void checkReadTableMetaData() {
    getAuthorizationManager().checkAuthorization(SystemPermissions.READ, Resources.SYSTEM);
  }

  @Override
  public void checkReadProperties() {
    getAuthorizationManager().checkAuthorization(SystemPermissions.READ, Resources.SYSTEM);
  }

  @Override
  public void checkSetProperty() {
    getAuthorizationManager().checkAuthorization(SystemPermissions.SET, Resources.SYSTEM);
  }

  @Override
  public void checkDeleteProperty() {
    getAuthorizationManager().checkAuthorization(SystemPermissions.DELETE, Resources.SYSTEM);
  }

  @Override
  public void checkDeleteLicenseKey() {
    getAuthorizationManager().checkAuthorization(SystemPermissions.DELETE, Resources.SYSTEM);
  }

  @Override
  public void checkSetLicenseKey() {
    getAuthorizationManager().checkAuthorization(SystemPermissions.SET, Resources.SYSTEM);
  }

  @Override
  public void checkReadLicenseKey() {
    getAuthorizationManager().checkAuthorization(SystemPermissions.READ, Resources.SYSTEM);
  }

  @Override
  public void checkRegisterProcessApplication() {
    getAuthorizationManager().checkAuthorization(SystemPermissions.SET, Resources.SYSTEM);
  }

  @Override
  public void checkUnregisterProcessApplication() {
    getAuthorizationManager().checkAuthorization(SystemPermissions.SET, Resources.SYSTEM);
  }

  @Override
  public void checkReadRegisteredDeployments() {
    getAuthorizationManager().checkAuthorization(SystemPermissions.READ, Resources.SYSTEM);
  }

  @Override
  public void checkReadProcessApplicationForDeployment() {
    getAuthorizationManager().checkAuthorization(SystemPermissions.READ, Resources.SYSTEM);
  }

  @Override
  public void checkRegisterDeployment() {
    getAuthorizationManager().checkAuthorization(SystemPermissions.SET, Resources.SYSTEM);
  }

  @Override
  public void checkUnregisterDeployment() {
    getAuthorizationManager().checkAuthorization(SystemPermissions.SET, Resources.SYSTEM);
  }

  @Override
  public void checkDeleteMetrics() {
    getAuthorizationManager().checkAuthorization(SystemPermissions.DELETE, Resources.SYSTEM);
  }

  @Override
  public void checkDeleteTaskMetrics() {
    getAuthorizationManager().checkAuthorization(SystemPermissions.DELETE, Resources.SYSTEM);
  }

  @Override
  public void checkReadSchemaLog() {
    getAuthorizationManager().checkAuthorization(SystemPermissions.READ, Resources.SYSTEM);
  }

  // helper ////////////////////////////////////////

  protected AuthorizationManager getAuthorizationManager() {
    return Context.getCommandContext().getAuthorizationManager();
  }

  protected ProcessDefinitionEntity findLatestProcessDefinitionById(String processDefinitionId) {
    return Context.getCommandContext().getProcessDefinitionManager().findLatestProcessDefinitionById(processDefinitionId);
  }

  protected DecisionDefinitionEntity findLatestDecisionDefinitionById(String decisionDefinitionId) {
    return Context.getCommandContext().getDecisionDefinitionManager().findDecisionDefinitionById(decisionDefinitionId);
  }

  protected ExecutionEntity findExecutionById(String processInstanceId) {
    return Context.getCommandContext().getExecutionManager().findExecutionById(processInstanceId);
  }

}
