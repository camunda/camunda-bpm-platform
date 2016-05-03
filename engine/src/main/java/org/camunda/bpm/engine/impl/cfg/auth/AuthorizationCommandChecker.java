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

import static org.camunda.bpm.engine.authorization.Permissions.CREATE;
import static org.camunda.bpm.engine.authorization.Permissions.CREATE_INSTANCE;
import static org.camunda.bpm.engine.authorization.Permissions.DELETE;
import static org.camunda.bpm.engine.authorization.Permissions.DELETE_INSTANCE;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.READ_INSTANCE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE_INSTANCE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE_TASK;
import static org.camunda.bpm.engine.authorization.Resources.DECISION_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.DEPLOYMENT;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_INSTANCE;
import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.impl.batch.history.HistoricBatchEntity;
import static org.camunda.bpm.engine.authorization.Resources.TASK;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.PermissionCheck;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinition;

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
  public void checkUpdateProcessDefinitionByKey(String processDefinitionKey) {
    getAuthorizationManager().checkAuthorization(UPDATE, PROCESS_DEFINITION, processDefinitionKey);
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

  @Override
  public void checkReadTask(TaskEntity task) {
    getAuthorizationManager().checkReadTask(task);
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
  }

  @Override
  public void checkDeleteHistoricBatch(HistoricBatchEntity batch) {
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

  protected AuthorizationManager getAuthorizationManager() {
    return Context.getCommandContext().getAuthorizationManager();
  }

  protected ProcessDefinitionEntity findLatestProcessDefinitionById(String processDefinitionId) {
    return Context.getCommandContext().getProcessDefinitionManager().findLatestProcessDefinitionById(processDefinitionId);
  }

  protected ExecutionEntity findExecutionById(String processInstanceId) {
    return Context.getCommandContext().getExecutionManager().findExecutionById(processInstanceId);
  }

}
