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
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE_INSTANCE;
import static org.camunda.bpm.engine.authorization.Resources.DECISION_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_INSTANCE;
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
import org.camunda.bpm.engine.runtime.Execution;

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
    getAuthorizationManager().checkReadProcessInstance(processInstanceId);
  }
  public void checkDeleteProcessInstance(ExecutionEntity execution) {
    getAuthorizationManager().checkDeleteProcessInstance(execution);
  }

  @Override
  public void checkUpdateProcessInstanceById(String processInstanceId) {
    ExecutionEntity execution = findExecutionById(processInstanceId);
    if (execution != null) {
      getAuthorizationManager().checkUpdateProcessInstance(execution);
    }
  }

  @Override
  public void checkUpdateProcessInstance(Execution execution) {
    getAuthorizationManager().checkUpdateProcessInstance((ExecutionEntity) execution);
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
    getAuthorizationManager().checkReadProcessInstance(execution);
  }

  @Override
  public void checkReadTask(TaskEntity task) {
    getAuthorizationManager().checkReadTask(task);
  }

  @Override
  public void checkUpdateTask(TaskEntity task) {
    getAuthorizationManager().checkUpdateTask(task);
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
