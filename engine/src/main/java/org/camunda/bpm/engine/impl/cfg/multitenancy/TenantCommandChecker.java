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
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.impl.batch.history.HistoricBatchEntity;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.cmd.CommandLogger;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionRequirementsDefinitionEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricExternalTaskLogEntity;
import org.camunda.bpm.engine.impl.persistence.entity.*;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.CaseExecution;

/**
 * {@link CommandChecker} to ensure that commands are only executed for
 * entities which belongs to one of the authenticated tenants.
 */
public class TenantCommandChecker implements CommandChecker {

  protected final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  @Override
  public void checkEvaluateDecision(DecisionDefinition decisionDefinition) {
    if (!getTenantManager().isAuthenticatedTenant(decisionDefinition.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("evaluate the decision '"+ decisionDefinition.getId() + "'");
    }
  }

  @Override
  public void checkCreateProcessInstance(ProcessDefinition processDefinition) {
    if (!getTenantManager().isAuthenticatedTenant(processDefinition.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("create an instance of the process definition '"+ processDefinition.getId() + "'");
    }
  }

  @Override
  public void checkReadProcessDefinition(ProcessDefinition processDefinition) {
    if (!getTenantManager().isAuthenticatedTenant(processDefinition.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("get the process definition '"+ processDefinition.getId() + "'");
    }
  }

  @Override
  public void checkCreateCaseInstance(CaseDefinition caseDefinition) {
    if (!getTenantManager().isAuthenticatedTenant(caseDefinition.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("create an instance of the case definition '"+ caseDefinition.getId() + "'");
    }
  }

  @Override
  public void checkUpdateProcessDefinitionById(String processDefinitionId) {
    if (getTenantManager().isTenantCheckEnabled()) {
      ProcessDefinitionEntity processDefinition = findLatestProcessDefinitionById(processDefinitionId);
      if (processDefinition != null && !getTenantManager().isAuthenticatedTenant(processDefinition.getTenantId())) {
        throw LOG.exceptionCommandWithUnauthorizedTenant("update the process definition '"+ processDefinitionId + "'");
      }
    }
  }

  @Override
  public void checkUpdateProcessDefinitionByKey(String processDefinitionKey) {
  }

  @Override
  public void checkDeleteProcessDefinitionById(String processDefinitionId) {
    if (getTenantManager().isTenantCheckEnabled()) {
      ProcessDefinitionEntity processDefinition = findLatestProcessDefinitionById(processDefinitionId);
      if (processDefinition != null && !getTenantManager().isAuthenticatedTenant(processDefinition.getTenantId())) {
        throw LOG.exceptionCommandWithUnauthorizedTenant("delete the process definition '"+ processDefinitionId + "'");
      }
    }
  }

  @Override
  public void checkDeleteProcessDefinitionByKey(String processDefinitionKey) {
  }



  @Override
  public void checkUpdateProcessInstanceByProcessDefinitionId(String processDefinitionId) {
    if (getTenantManager().isTenantCheckEnabled()) {
      ProcessDefinitionEntity processDefinition = findLatestProcessDefinitionById(processDefinitionId);
      if (processDefinition != null && !getTenantManager().isAuthenticatedTenant(processDefinition.getTenantId())) {
        throw LOG.exceptionCommandWithUnauthorizedTenant("update the process definition '"+ processDefinitionId + "'");
      }
    }
  }

  @Override
  public void checkUpdateProcessInstance(ExecutionEntity execution) {
    if (execution != null && !getTenantManager().isAuthenticatedTenant(execution.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("update the process instance '"+ execution.getId() + "'");
    }
  }

  @Override
  public void checkUpdateJob(JobEntity job) {
    if (job != null && !getTenantManager().isAuthenticatedTenant(job.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("update the job '"+ job.getId() + "'");
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
        throw LOG.exceptionCommandWithUnauthorizedTenant("update the process instance '"+ processInstanceId + "'");
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
        throw LOG.exceptionCommandWithUnauthorizedTenant("read the process instance '"+ processInstanceId + "'");
      }
    }
  }

  public void checkReadJob(JobEntity job) {
    if (job != null && !getTenantManager().isAuthenticatedTenant(job.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("read the job '"+ job.getId() + "'");
    }
  }

  @Override
  public void checkReadProcessInstance(ExecutionEntity execution) {
    if (execution != null && !getTenantManager().isAuthenticatedTenant(execution.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("read the process instance '"+ execution.getId() + "'");
    }
  }

  public void checkDeleteProcessInstance(ExecutionEntity execution) {
    if (execution != null && !getTenantManager().isAuthenticatedTenant(execution.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("delete the process instance '"+ execution.getId() + "'");
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
      throw LOG.exceptionCommandWithUnauthorizedTenant("read the task '"+ task.getId() + "'");
    }
  }

  @Override
  public void checkUpdateTask(TaskEntity task) {
    if (task != null && !getTenantManager().isAuthenticatedTenant(task.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("update the task '"+ task.getId() + "'");
    }
  }

  @Override
  public void checkDeleteBatch(BatchEntity batch) {
    if (batch != null && !getTenantManager().isAuthenticatedTenant(batch.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("delete batch '"+ batch.getId() + "'");
    }
  }

  @Override
  public void checkDeleteHistoricBatch(HistoricBatchEntity batch) {
    if (batch != null && !getTenantManager().isAuthenticatedTenant(batch.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("delete historic batch '"+ batch.getId() + "'");
    }
  }

  @Override
  public void checkSuspendBatch(BatchEntity batch) {
    if (batch != null && !getTenantManager().isAuthenticatedTenant(batch.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("suspend batch '"+ batch.getId() + "'");
    }
  }

  @Override
  public void checkActivateBatch(BatchEntity batch) {
    if (batch != null && !getTenantManager().isAuthenticatedTenant(batch.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("activate batch '"+ batch.getId() + "'");
    }
  }

  @Override
  public void checkReadHistoricBatch() {
  }

  @Override
  public void checkCreateDeployment() {
  }

  @Override
  public void checkReadDeployment(String deploymentId) {
    if (getTenantManager().isTenantCheckEnabled()) {
      DeploymentEntity deployment = findDeploymentById(deploymentId);
      if (deployment != null && !getTenantManager().isAuthenticatedTenant(deployment.getTenantId())) {
        throw LOG.exceptionCommandWithUnauthorizedTenant("get the deployment '"+ deploymentId + "'");
      }
    }
  }

  @Override
  public void checkDeleteDeployment(String deploymentId) {
    if (getTenantManager().isTenantCheckEnabled()) {
      DeploymentEntity deployment = findDeploymentById(deploymentId);
      if (deployment != null && !getTenantManager().isAuthenticatedTenant(deployment.getTenantId())) {
        throw LOG.exceptionCommandWithUnauthorizedTenant("delete the deployment '"+ deploymentId + "'");
      }
    }
  }

  @Override
  public void checkDeleteTask(TaskEntity task) {
    if (task != null && !getTenantManager().isAuthenticatedTenant(task.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("delete the task '"+ task.getId() + "'");
    }
  }

  public void checkTaskAssign(TaskEntity task) {
    if (task != null && !getTenantManager().isAuthenticatedTenant(task.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("assign the task '"+ task.getId() + "'");
    }
  }

  public void checkCreateTask(TaskEntity task) {
    if (task != null && !getTenantManager().isAuthenticatedTenant(task.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("create the task '"+ task.getId() + "'");
    }
  }

  @Override
  public void checkCreateTask() {
  }

  public void checkTaskWork(TaskEntity task) {
    if (task != null && !getTenantManager().isAuthenticatedTenant(task.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("work on task '"+ task.getId() +"'");
    }
  }

  public void checkReadDecisionDefinition(DecisionDefinitionEntity decisionDefinition) {
    if (decisionDefinition != null && !getTenantManager().isAuthenticatedTenant(decisionDefinition.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("get the decision definition '"+ decisionDefinition.getId() + "'");
    }
  }

  @Override
  public void checkUpdateDecisionDefinitionById(String decisionDefinitionId) {
    if (getTenantManager().isTenantCheckEnabled()) {
      DecisionDefinitionEntity decisionDefinition = findLatestDecisionDefinitionById(decisionDefinitionId);
      if (decisionDefinition != null && !getTenantManager().isAuthenticatedTenant(decisionDefinition.getTenantId())) {
        throw LOG.exceptionCommandWithUnauthorizedTenant("update the decision definition '"+ decisionDefinitionId + "'");
      }
    }
  }

  public void checkReadDecisionRequirementsDefinition(DecisionRequirementsDefinitionEntity decisionRequirementsDefinition) {
    if (decisionRequirementsDefinition != null && !getTenantManager().isAuthenticatedTenant(decisionRequirementsDefinition.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("get the decision requirements definition '"+ decisionRequirementsDefinition.getId() + "'");
    }
  }

  public void checkReadCaseDefinition(CaseDefinition caseDefinition) {
    if (caseDefinition != null && !getTenantManager().isAuthenticatedTenant(caseDefinition.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("get the case definition '"+ caseDefinition.getId() + "'");
    }
  }

  public void checkUpdateCaseDefinition(CaseDefinition caseDefinition) {
    if (caseDefinition != null && !getTenantManager().isAuthenticatedTenant(caseDefinition.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("update the case definition '" + caseDefinition.getId() + "'");
    }
  }

  @Override
  public void checkDeleteHistoricTaskInstance(HistoricTaskInstanceEntity task) {
    if (task != null && !getTenantManager().isAuthenticatedTenant(task.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("delete the historic task instance '"+ task.getId() + "'");
    }
  }

  @Override
  public void checkDeleteHistoricProcessInstance(HistoricProcessInstance instance) {
    if (instance != null && !getTenantManager().isAuthenticatedTenant(instance.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("delete the historic process instance '"+ instance.getId() + "'");
    }
  }

  @Override
  public void checkDeleteHistoricCaseInstance(HistoricCaseInstance instance) {
    if (instance != null && !getTenantManager().isAuthenticatedTenant(instance.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("delete the historic case instance '"+ instance.getId() + "'");
    }
  }

  @Override
  public void checkDeleteHistoricDecisionInstance(String decisionDefinitionKey) {
    // No tenant check here because it is called in the SQL query:
    // HistoricDecisionInstance.selectHistoricDecisionInstancesByDecisionDefinitionId
    // It is necessary to make the check there because of performance issues. If the check
    // is done here then the we get all history decision instances (also from possibly
    // other tenants) and then filter them. If there are a lot instances this can cause
    // latency. Therefore does the SQL query only return the
    // historic decision instances which belong to the authenticated tenant.
  }

  @Override
  public void checkDeleteHistoricDecisionInstance(HistoricDecisionInstance decisionInstance) {
    if (decisionInstance != null && !getTenantManager().isAuthenticatedTenant(decisionInstance.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant(
          "delete the historic decision instance '" + decisionInstance.getId() + "'"
      );
    }
  }

  @Override
  public void checkReadHistoricJobLog(HistoricJobLogEventEntity historicJobLog) {
    if (historicJobLog != null && !getTenantManager().isAuthenticatedTenant(historicJobLog.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("get the historic job log '"+ historicJobLog.getId() + "'");
    }
  }

  @Override
  public void checkReadHistoryAnyProcessDefinition() {
    // No tenant check here because it is called in the SQL query:
    // Report.selectHistoricProcessInstanceDurationReport
    // It is necessary to make the check there because the query may be return only the
    // historic process instances which belong to the authenticated tenant.
  }

  @Override
  public void checkReadHistoryProcessDefinition(String processDefinitionId) {
    // No tenant check here because it is called in the SQL query:
    // Report.selectHistoricProcessInstanceDurationReport
    // It is necessary to make the check there because the query may be return only the
    // historic process instances which belong to the authenticated tenant.
  }

  @Override
  public void checkReadHistoryAnyTaskInstance() {
    // No tenant check here because it is called in the SQL query:
    // Report.selectHistoricProcessInstanceDurationReport
    // It is necessary to make the check there because the query may be return only the
    // historic process instances which belong to the authenticated tenant.
  }

  @Override
  public void checkUpdateCaseInstance(CaseExecution caseExecution) {
    if (caseExecution != null && !getTenantManager().isAuthenticatedTenant(caseExecution.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("update the case execution '" + caseExecution.getId() + "'");
    }
  }

  @Override
  public void checkReadCaseInstance(CaseExecution caseExecution) {
    if (caseExecution != null && !getTenantManager().isAuthenticatedTenant(caseExecution.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("get the case execution '" + caseExecution.getId() + "'");
    }
  }

  // helper //////////////////////////////////////////////////

  protected TenantManager getTenantManager() {
    return Context.getCommandContext().getTenantManager();
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

  protected DeploymentEntity findDeploymentById(String deploymentId) {
    return Context.getCommandContext().getDeploymentManager().findDeploymentById(deploymentId);
  }

  @Override
  public void checkDeleteUserOperationLog(UserOperationLogEntry entry) {
     // tenant check is not available for user operation log
  }

  @Override
  public void checkReadHistoricExternalTaskLog(HistoricExternalTaskLogEntity historicExternalTaskLog) {
    if (historicExternalTaskLog != null && !getTenantManager().isAuthenticatedTenant(historicExternalTaskLog.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("get the historic external task log '"+ historicExternalTaskLog.getId() + "'");
    }
  }
}
