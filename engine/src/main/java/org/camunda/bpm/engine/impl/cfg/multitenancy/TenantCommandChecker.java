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

package org.camunda.bpm.engine.impl.cfg.multitenancy;

import org.camunda.bpm.engine.history.HistoricCaseInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.impl.batch.history.HistoricBatchEntity;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.cmd.CommandLogger;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricJobLogEventEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricTaskInstanceEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TenantManager;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinition;

/**
 * {@link CommandChecker} to ensure that commands are only executed for
 * entities which belongs to one of the authenticated tenants.
 */
public class TenantCommandChecker implements CommandChecker {

  protected final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  @Override
  public void checkEvaluateDecision(DecisionDefinition decisionDefinition) {
    if (!getTenantManager().isAuthenticatedTenant(decisionDefinition.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("evaluate the decision");
    }
  }

  @Override
  public void checkCreateProcessInstance(ProcessDefinition processDefinition) {
    if (!getTenantManager().isAuthenticatedTenant(processDefinition.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("create an instance of the process definition");
    }
  }

  @Override
  public void checkReadProcessDefinition(ProcessDefinition processDefinition) {
    if (!getTenantManager().isAuthenticatedTenant(processDefinition.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("get the process definition");
    }
  }

  @Override
  public void checkCreateCaseInstance(CaseDefinition caseDefinition) {
    if (!getTenantManager().isAuthenticatedTenant(caseDefinition.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("create an instance of the case definition");
    }
  }

  @Override
  public void checkUpdateProcessDefinitionById(String processDefinitionId) {
    if (getTenantManager().isTenantCheckEnabled()) {
      ProcessDefinitionEntity processDefinition = findLatestProcessDefinitionById(processDefinitionId);
      if (processDefinition != null && !getTenantManager().isAuthenticatedTenant(processDefinition.getTenantId())) {
        throw LOG.exceptionCommandWithUnauthorizedTenant("update the process definition suspension state");
      }
    }
  }

  @Override
  public void checkUpdateProcessDefinitionByKey(String processDefinitionKey) {
  }

  @Override
  public void checkUpdateProcessInstanceByProcessDefinitionId(String processDefinitionId) {
    if (getTenantManager().isTenantCheckEnabled()) {
      ProcessDefinitionEntity processDefinition = findLatestProcessDefinitionById(processDefinitionId);
      if (processDefinition != null && !getTenantManager().isAuthenticatedTenant(processDefinition.getTenantId())) {
        throw LOG.exceptionCommandWithUnauthorizedTenant("update the suspension state of an instance of the process definition");
      }
    }
  }

  @Override
  public void checkUpdateProcessInstance(ExecutionEntity execution) {
    if (execution != null && !getTenantManager().isAuthenticatedTenant(execution.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("update the process instance");
    }
  }

  @Override
  public void checkUpdateProcessInstanceByProcessDefinitionKey(String processDefinitionKey) {
  }

  @Override
  public void checkUpdateProcessInstanceById(String processInstanceId) {
    if (getTenantManager().isTenantCheckEnabled()) {
      ExecutionEntity execution = findExecutionById(processInstanceId);
      if (execution != null && !getTenantManager().isAuthenticatedTenant(execution.getTenantId())) {
        throw LOG.exceptionCommandWithUnauthorizedTenant("update the process instance");
      }
    }
  }


  @Override
  public void checkCreateMigrationPlan(ProcessDefinition sourceProcessDefinition, ProcessDefinition targetProcessDefinition) {
    String sourceTenant = sourceProcessDefinition.getTenantId();
    String targetTenant = targetProcessDefinition.getTenantId();

    if (!getTenantManager().isAuthenticatedTenant(sourceTenant)) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("get process definition '" + sourceProcessDefinition.getId() + "'");
    }
    if (!getTenantManager().isAuthenticatedTenant(targetTenant)) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("get process definition '" + targetProcessDefinition.getId() + "'");
    }

    if (sourceTenant != null && targetTenant != null && !sourceTenant.equals(targetTenant)) {
      throw ProcessEngineLogger.MIGRATION_LOGGER
        .cannotMigrateBetweenTenants(sourceTenant, targetTenant);
    }
  }

  public void checkReadProcessInstance(String processInstanceId) {
    if (getTenantManager().isTenantCheckEnabled()) {
      ExecutionEntity execution = findExecutionById(processInstanceId);
      if (execution != null && !getTenantManager().isAuthenticatedTenant(execution.getTenantId())) {
        throw LOG.exceptionCommandWithUnauthorizedTenant("read the process instance");
      }
    }
  }

  @Override
  public void checkReadProcessInstance(ExecutionEntity execution) {
    if (execution != null && !getTenantManager().isAuthenticatedTenant(execution.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("read the process instance");
    }
  }

  public void checkDeleteProcessInstance(ExecutionEntity execution) {
    if (execution != null && !getTenantManager().isAuthenticatedTenant(execution.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("delete the process instance");
    }
  }

  @Override
  public void checkMigrateProcessInstance(ExecutionEntity processInstance, ProcessDefinition targetProcessDefinition) {
    String sourceTenant = processInstance.getTenantId();
    String targetTenant = targetProcessDefinition.getTenantId();

    if (getTenantManager().isTenantCheckEnabled()) {
      if (processInstance != null && !getTenantManager().isAuthenticatedTenant(processInstance.getTenantId())) {
        throw LOG.exceptionCommandWithUnauthorizedTenant("migrate process instance '" + processInstance.getId() + "'");
      }
    }

    if (targetTenant != null && (sourceTenant == null || !sourceTenant.equals(targetTenant))) {
      throw ProcessEngineLogger.MIGRATION_LOGGER
        .cannotMigrateInstanceBetweenTenants(processInstance.getId(), sourceTenant, targetTenant);
    }
  }

  public void checkReadTask(TaskEntity task) {
    if (task != null && !getTenantManager().isAuthenticatedTenant(task.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("read the task");
    }
  }

  @Override
  public void checkUpdateTask(TaskEntity task) {
    if (task != null && !getTenantManager().isAuthenticatedTenant(task.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("update the task");
    }
  }

  @Override
  public void checkDeleteBatch(BatchEntity batch) {
    if (batch != null && !getTenantManager().isAuthenticatedTenant(batch.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("delete batch");
    }
  }

  @Override
  public void checkDeleteHistoricBatch(HistoricBatchEntity batch) {
    if (batch != null && !getTenantManager().isAuthenticatedTenant(batch.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("delete historic batch");
    }
  }

  public void checkSuspendBatch(BatchEntity batch) {
    if (batch != null && !getTenantManager().isAuthenticatedTenant(batch.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("suspend batch");
    }
  }

  public void checkActivateBatch(BatchEntity batch) {
    if (batch != null && !getTenantManager().isAuthenticatedTenant(batch.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("activate batch");
    }
  }

  @Override
  public void checkCreateDeployment() {
  }

  @Override
  public void checkReadDeployment(String deploymentId) {
    if (getTenantManager().isTenantCheckEnabled()) {
      DeploymentEntity deployment = findDeploymentById(deploymentId);
      if (deployment != null && !getTenantManager().isAuthenticatedTenant(deployment.getTenantId())) {
        throw LOG.exceptionCommandWithUnauthorizedTenant("get the deployment");
      }
    }
  }

  @Override
  public void checkDeleteDeployment(String deploymentId) {
    if (getTenantManager().isTenantCheckEnabled()) {
      DeploymentEntity deployment = findDeploymentById(deploymentId);
      if (deployment != null && !getTenantManager().isAuthenticatedTenant(deployment.getTenantId())) {
        throw LOG.exceptionCommandWithUnauthorizedTenant("delete the deployment");
      }
    }
  }

  @Override
  public void checkDeleteTask(TaskEntity task) {
    if (task != null && !getTenantManager().isAuthenticatedTenant(task.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("delete the task");
    }
  }

  public void checkTaskAssign(TaskEntity task) {
    if (task != null && !getTenantManager().isAuthenticatedTenant(task.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("assign the task");
    }
  }

  public void checkCreateTask(TaskEntity task) {
    if (task != null && !getTenantManager().isAuthenticatedTenant(task.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("create the task");
    }
  }

  @Override
  public void checkCreateTask() {
  }

  public void checkTaskWork(TaskEntity task) {
    if (task != null && !getTenantManager().isAuthenticatedTenant(task.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("work on task");
    }
  }

  public void checkReadDecisionDefinition(DecisionDefinitionEntity decisionDefinition) {
    if (decisionDefinition != null && !getTenantManager().isAuthenticatedTenant(decisionDefinition.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("get the decision definition");
    }
  }

  public void checkReadCaseDefinition(CaseDefinition caseDefinition) {
    if (caseDefinition != null && !getTenantManager().isAuthenticatedTenant(caseDefinition.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("get the case definition");
    }
  }

  @Override
  public void checkDeleteHistoricTaskInstance(HistoricTaskInstanceEntity task) {
    if (task != null && !getTenantManager().isAuthenticatedTenant(task.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("delete the historic task instance");
    }
  }

  @Override
  public void checkDeleteHistoricProcessInstance(HistoricProcessInstance instance) {
    if (instance != null && !getTenantManager().isAuthenticatedTenant(instance.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("delete the historic process instance");
    }
  }

  @Override
  public void checkDeleteHistoricCaseInstance(HistoricCaseInstance instance) {
    if (instance != null && !getTenantManager().isAuthenticatedTenant(instance.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("delete the historic case instance");
    }
  }

  @Override
  public void checkDeleteHistoricDecisionInstance(String decisionDefinitionKey) {
    // No tenant check here because it is called in the SQL query:
    // HistoricDecisionInstance.selectHistoricDecisionInstancesByDecisionDefinitionId
    // It is necessary to make the check there because the query may be return only the
    // historic decision instances which belong to the authenticated tenant.
  }

  @Override
  public void checkReadHistoricJobLog(HistoricJobLogEventEntity historicJobLog) {
    if (historicJobLog != null && !getTenantManager().isAuthenticatedTenant(historicJobLog.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("get the historic job log");
    }
  }

  // helper //////////////////////////////////////////////////

  protected TenantManager getTenantManager() {
    return Context.getCommandContext().getTenantManager();
  }

  protected ProcessDefinitionEntity findLatestProcessDefinitionById(String processDefinitionId) {
    return Context.getCommandContext().getProcessDefinitionManager().findLatestProcessDefinitionById(processDefinitionId);
  }

  protected ExecutionEntity findExecutionById(String processInstanceId) {
    return Context.getCommandContext().getExecutionManager().findExecutionById(processInstanceId);
  }

  protected DeploymentEntity findDeploymentById(String deploymentId) {
    return Context.getCommandContext().getDeploymentManager().findDeploymentById(deploymentId);
  }

}
