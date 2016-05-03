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

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.impl.batch.history.HistoricBatchEntity;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.cmd.CommandLogger;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TenantManager;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Execution;

/**
 * {@link CommandChecker} to ensure that commands are only executed for
 * entities which belongs to one of the authenticated tenants.
 */
public class TenantCommandChecker implements CommandChecker {

  protected final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  @Override
  public void checkEvaluateDecision(DecisionDefinition decisionDefinition) {
    if (!getTenantManager().isAuthenticatedTenant(decisionDefinition.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("evaluate the decision", decisionDefinition);
    }
  }

  @Override
  public void checkCreateProcessInstance(ProcessDefinition processDefinition) {
    if (!getTenantManager().isAuthenticatedTenant(processDefinition.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("create an instance of the process definition", processDefinition);
    }
  }

  @Override
  public void checkReadProcessDefinition(ProcessDefinition processDefinition) {
    if (!getTenantManager().isAuthenticatedTenant(processDefinition.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("get the process definition", processDefinition);
    }
  }

  @Override
  public void checkCreateCaseInstance(CaseDefinition caseDefinition) {
    if (!getTenantManager().isAuthenticatedTenant(caseDefinition.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("create an instance of the case definition", caseDefinition);
    }
  }

  @Override
  public void checkUpdateProcessDefinitionById(String processDefinitionId) {
    if (getTenantManager().isTenantCheckEnabled()) {
      ProcessDefinitionEntity processDefinition = findLatestProcessDefinitionById(processDefinitionId);
      if (processDefinition != null && !getTenantManager().isAuthenticatedTenant(processDefinition.getTenantId())) {
        throw LOG.exceptionCommandWithUnauthorizedTenant("update the process definition suspension state", processDefinition);
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
        throw LOG.exceptionCommandWithUnauthorizedTenant("update the suspension state of an instance of the process definition", processDefinition);
      }
    }
  }

  @Override
  public void checkUpdateProcessInstance(Execution execution) {
    if (getTenantManager().isTenantCheckEnabled()) {
      if (execution != null && !getTenantManager().isAuthenticatedTenant(execution.getTenantId())) {
        throw LOG.exceptionCommandWithUnauthorizedTenant("update the process instance", execution);
      }
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
        throw LOG.exceptionCommandWithUnauthorizedTenant("update the process instance", execution);
      }
    }
  }


  @Override
  public void checkCreateMigrationPlan(ProcessDefinition sourceProcessDefinition, ProcessDefinition targetProcessDefinition) {
    String sourceTenant = sourceProcessDefinition.getTenantId();
    String targetTenant = targetProcessDefinition.getTenantId();

    if (sourceTenant != null && targetTenant != null && !sourceTenant.equals(targetTenant)) {
      throw ProcessEngineLogger.MIGRATION_LOGGER
        .cannotMigrateBetweenTenants(sourceTenant, targetTenant);
    }
  }

  public void checkReadProcessInstance(String processInstanceId) {
    if (getTenantManager().isTenantCheckEnabled()) {
      ExecutionEntity execution = findExecutionById(processInstanceId);
      if (execution != null && !getTenantManager().isAuthenticatedTenant(execution.getTenantId())) {
        throw LOG.exceptionCommandWithUnauthorizedTenant("read the process instance", execution);
      }
    }
  }

  @Override
  public void checkReadProcessInstance(ExecutionEntity execution) {
    if (getTenantManager().isTenantCheckEnabled()) {
      if (execution != null && !getTenantManager().isAuthenticatedTenant(execution.getTenantId())) {
        throw LOG.exceptionCommandWithUnauthorizedTenant("read the process instance", execution);
      }
    }
  }

  public void checkDeleteProcessInstance(ExecutionEntity execution) {
    if (getTenantManager().isTenantCheckEnabled()) {
      if (execution != null && !getTenantManager().isAuthenticatedTenant(execution.getTenantId())) {
        throw LOG.exceptionCommandWithUnauthorizedTenant("delete the process instance", execution);
      }
    }
  }

  @Override
  public void checkMigrateProcessInstance(ExecutionEntity processInstance, ProcessDefinition targetProcessDefinition) {
    String sourceTenant = processInstance.getTenantId();
    String targetTenant = targetProcessDefinition.getTenantId();

    if (targetTenant != null && (sourceTenant == null || !sourceTenant.equals(targetTenant))) {
      throw ProcessEngineLogger.MIGRATION_LOGGER
        .cannotMigrateInstanceBetweenTenants(processInstance.getId(), sourceTenant, targetTenant);
    }
  }

  public void checkReadTask(TaskEntity task) {
    if (getTenantManager().isTenantCheckEnabled()) {
      if (task != null && !getTenantManager().isAuthenticatedTenant(task.getTenantId())) {
        throw LOG.exceptionCommandWithUnauthorizedTenant("read the task", task);
      }
    }
  }

  @Override
  public void checkUpdateTask(TaskEntity task) {
    if (getTenantManager().isTenantCheckEnabled()) {
      if (task != null && !getTenantManager().isAuthenticatedTenant(task.getTenantId())) {
        throw LOG.exceptionCommandWithUnauthorizedTenant("update the task", task);
      }
    }
  }

  @Override
  public void checkDeleteBatch(BatchEntity batch) {
    if (getTenantManager().isTenantCheckEnabled()) {
      if (batch != null && !getTenantManager().isAuthenticatedTenant(batch.getTenantId())) {
        throw LOG.exceptionCommandWithUnauthorizedTenant("delete batch", batch);
      }
    }
  }

  @Override
  public void checkDeleteHistoricBatch(HistoricBatchEntity batch) {
    if (getTenantManager().isTenantCheckEnabled()) {
      if (batch != null && !getTenantManager().isAuthenticatedTenant(batch.getTenantId())) {
        throw LOG.exceptionCommandWithUnauthorizedTenant("delete historic batch", batch);
      }
    }
  }

  protected TenantManager getTenantManager() {
    return Context.getCommandContext().getTenantManager();
  }

  protected ProcessDefinitionEntity findLatestProcessDefinitionById(String processDefinitionId) {
    return Context.getCommandContext().getProcessDefinitionManager().findLatestProcessDefinitionById(processDefinitionId);
  }

  protected ProcessDefinitionEntity findLatestProcessDefinitionByKey(String processDefinitionKey) {
    return Context.getCommandContext().getProcessDefinitionManager().findLatestProcessDefinitionByKey(processDefinitionKey);
  }

  protected ExecutionEntity findExecutionById(String processInstanceId) {
    return Context.getCommandContext().getExecutionManager().findExecutionById(processInstanceId);
  }


}
