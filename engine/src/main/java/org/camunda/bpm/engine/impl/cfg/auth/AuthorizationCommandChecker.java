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
import org.camunda.bpm.engine.impl.db.PermissionCheck;
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
  public void checkUpdateProcessInstanceByProcessDefinitionKey(String processDefinitionKey) {
    // necessary permissions:
    // - UPDATE on ANY PROCESS_INSTANCE

    PermissionCheck firstCheck = new PermissionCheck();
    firstCheck.setPermission(UPDATE);
    firstCheck.setResource(PROCESS_INSTANCE);

    // ... OR ...

    // - UPDATE_INSTANCE on PROCESS_DEFINITION

    PermissionCheck secondCheck = new PermissionCheck();
    secondCheck.setPermission(UPDATE_INSTANCE);
    secondCheck.setResource(PROCESS_DEFINITION);
    secondCheck.setResourceId(processDefinitionKey);
    secondCheck.setAuthorizationNotFoundReturnValue(0l);

    getAuthorizationManager().checkAuthorization(firstCheck, secondCheck);
  }

  public void checkReadProcessInstance(String processInstanceId) {
    ExecutionEntity execution = findExecutionById(processInstanceId);
    if (execution != null) {
      checkReadProcessInstance(execution);
    }
  }

  public void checkDeleteProcessInstance(ExecutionEntity execution) {
    ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) execution.getProcessDefinition();

    // necessary permissions:
    // - DELETE on PROCESS_INSTANCE

    PermissionCheck firstCheck = new PermissionCheck();
    firstCheck.setPermission(DELETE);
    firstCheck.setResource(PROCESS_INSTANCE);
    firstCheck.setResourceId(execution.getProcessInstanceId());

    // ... OR ...

    // - DELETE_INSTANCE on PROCESS_DEFINITION

    PermissionCheck secondCheck = new PermissionCheck();
    secondCheck.setPermission(DELETE_INSTANCE);
    secondCheck.setResource(PROCESS_DEFINITION);
    secondCheck.setResourceId(processDefinition.getKey());
    secondCheck.setAuthorizationNotFoundReturnValue(0l);

    getAuthorizationManager().checkAuthorization(firstCheck, secondCheck);
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
    ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) execution.getProcessDefinition();

    // necessary permissions:
    // - UPDATE on PROCESS_INSTANCE

    PermissionCheck firstCheck = new PermissionCheck();
    firstCheck.setPermission(UPDATE);
    firstCheck.setResource(PROCESS_INSTANCE);
    firstCheck.setResourceId(execution.getProcessInstanceId());

    // ... OR ...

    // - UPDATE_INSTANCE on PROCESS_DEFINITION

    PermissionCheck secondCheck = new PermissionCheck();
    secondCheck.setPermission(UPDATE_INSTANCE);
    secondCheck.setResource(PROCESS_DEFINITION);
    secondCheck.setResourceId(processDefinition.getKey());
    secondCheck.setAuthorizationNotFoundReturnValue(0l);

    getAuthorizationManager().checkAuthorization(firstCheck, secondCheck);
  }

  public void checkUpdateJob(JobEntity job) {
    if (job.getProcessDefinitionKey() == null) {
      // "standalone" job: nothing to do!
      return;
    }

    // necessary permissions:
    // - READ on PROCESS_INSTANCE

    PermissionCheck firstCheck = getAuthorizationManager().newPermissionCheck();
    firstCheck.setPermission(UPDATE);
    firstCheck.setResource(PROCESS_INSTANCE);
    firstCheck.setResourceId(job.getProcessInstanceId());

    // ... OR ...

    // - UPDATE_INSTANCE on PROCESS_DEFINITION
    PermissionCheck secondCheck = getAuthorizationManager().newPermissionCheck();
    secondCheck.setPermission(UPDATE_INSTANCE);
    secondCheck.setResource(PROCESS_DEFINITION);
    secondCheck.setResourceId(job.getProcessDefinitionKey());
    secondCheck.setAuthorizationNotFoundReturnValue(0l);

    getAuthorizationManager().checkAuthorization(firstCheck, secondCheck);
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
    ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) execution.getProcessDefinition();

    // necessary permissions:
    // - READ on PROCESS_INSTANCE

    PermissionCheck firstCheck = new PermissionCheck();
    firstCheck.setPermission(READ);
    firstCheck.setResource(PROCESS_INSTANCE);
    firstCheck.setResourceId(execution.getProcessInstanceId());

    // ... OR ...

    // - READ_INSTANCE on PROCESS_DEFINITION
    PermissionCheck secondCheck = new PermissionCheck();
    secondCheck.setPermission(READ_INSTANCE);
    secondCheck.setResource(PROCESS_DEFINITION);
    secondCheck.setResourceId(processDefinition.getKey());
    secondCheck.setAuthorizationNotFoundReturnValue(0l);

    getAuthorizationManager().checkAuthorization(firstCheck, secondCheck);
  }

  public void checkReadJob(JobEntity job) {
    if (job.getProcessDefinitionKey() == null) {
      // "standalone" job: nothing to do!
      return;
    }

    // necessary permissions:
    // - READ on PROCESS_INSTANCE

    PermissionCheck firstCheck = getAuthorizationManager().newPermissionCheck();
    firstCheck.setPermission(READ);
    firstCheck.setResource(PROCESS_INSTANCE);
    firstCheck.setResourceId(job.getProcessInstanceId());

    // ... OR ...

    // - READ_INSTANCE on PROCESS_DEFINITION
    PermissionCheck secondCheck = getAuthorizationManager().newPermissionCheck();
    secondCheck.setPermission(READ_INSTANCE);
    secondCheck.setResource(PROCESS_DEFINITION);
    secondCheck.setResourceId(job.getProcessDefinitionKey());
    secondCheck.setAuthorizationNotFoundReturnValue(0l);

    getAuthorizationManager().checkAuthorization(firstCheck, secondCheck);
  }

  @Override
  public void checkReadTask(TaskEntity task) {
    String taskId = task.getId();

    String executionId = task.getExecutionId();
    if (executionId != null) {

      // if task exists in context of a process instance
      // then check the following permissions:
      // - READ on TASK
      // - READ_TASK on PROCESS_DEFINITION

      ExecutionEntity execution = task.getExecution();
      ProcessDefinitionEntity processDefinition = execution.getProcessDefinition();


      PermissionCheck readPermissionCheck = getAuthorizationManager().newPermissionCheck();
      readPermissionCheck.setPermission(READ);
      readPermissionCheck.setResource(TASK);
      readPermissionCheck.setResourceId(taskId);

      PermissionCheck readTaskPermissionCheck = getAuthorizationManager().newPermissionCheck();
      readTaskPermissionCheck.setPermission(READ_TASK);
      readTaskPermissionCheck.setResource(PROCESS_DEFINITION);
      readTaskPermissionCheck.setResourceId(processDefinition.getKey());
      readTaskPermissionCheck.setAuthorizationNotFoundReturnValue(0l);

      getAuthorizationManager().checkAuthorization(readPermissionCheck, readTaskPermissionCheck);

    } else {

      // if task does not exist in context of process
      // instance, then it is either a (a) standalone task
      // or (b) it exists in context of a case instance.

      // (a) standalone task: check following permission
      // - READ on TASK
      // (b) task in context of a case instance, in this
      // case it is not necessary to check any permission,
      // because such tasks can always be read

      String caseExecutionId = task.getCaseExecutionId();
      if (caseExecutionId == null) {
        getAuthorizationManager().checkAuthorization(READ, TASK, taskId);
      }

    }
  }

  public void checkUpdateTask(TaskEntity task) {
    String taskId = task.getId();

    String executionId = task.getExecutionId();
    if (executionId != null) {

      // if task exists in context of a process instance
      // then check the following permissions:
      // - UPDATE on TASK
      // - UPDATE_TASK on PROCESS_DEFINITION

      ExecutionEntity execution = task.getExecution();
      ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) execution.getProcessDefinition();

      PermissionCheck updatePermissionCheck = new PermissionCheck();
      updatePermissionCheck.setPermission(UPDATE);
      updatePermissionCheck.setResource(TASK);
      updatePermissionCheck.setResourceId(taskId);

      PermissionCheck updateTaskPermissionCheck = new PermissionCheck();
      updateTaskPermissionCheck.setPermission(UPDATE_TASK);
      updateTaskPermissionCheck.setResource(PROCESS_DEFINITION);
      updateTaskPermissionCheck.setResourceId(processDefinition.getKey());
      updateTaskPermissionCheck.setAuthorizationNotFoundReturnValue(0l);

      getAuthorizationManager().checkAuthorization(updatePermissionCheck, updateTaskPermissionCheck);

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
        getAuthorizationManager().checkAuthorization(UPDATE, TASK, taskId);
      }

    }
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

  public void checkReadHistoryAnyTaskInstance() {
    getAuthorizationManager().checkAuthorization(READ_HISTORY, TASK, ANY);
  }

  @Override
  public void checkUpdateCaseInstance(CaseExecution caseExecution) {
  }

  @Override
  public void checkReadCaseInstance(CaseExecution caseExecution) {
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

  @Override
  public void checkDeleteUserOperationLog(UserOperationLogEntry entry) {
    if (entry != null) {
      String processDefinitionKey = entry.getProcessDefinitionKey();
      if (processDefinitionKey != null) {
        getAuthorizationManager().checkAuthorization(DELETE_HISTORY, PROCESS_DEFINITION, processDefinitionKey);
      }
    }
  }

  @Override
  public void checkReadHistoricExternalTaskLog(HistoricExternalTaskLogEntity historicExternalTaskLog) {
    if (historicExternalTaskLog.getProcessDefinitionKey() != null) {
      getAuthorizationManager().checkAuthorization(READ_HISTORY, PROCESS_DEFINITION, historicExternalTaskLog.getProcessDefinitionKey());
    }
  }
}
