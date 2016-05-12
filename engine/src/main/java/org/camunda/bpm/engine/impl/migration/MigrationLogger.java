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
package org.camunda.bpm.engine.impl.migration;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.migration.instance.MigratingActivityInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingProcessElementInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingScopeInstance;
import org.camunda.bpm.engine.impl.migration.validation.instance.MigratingProcessInstanceValidationReportImpl;
import org.camunda.bpm.engine.impl.migration.validation.instruction.MigrationPlanValidationReportImpl;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.migration.MigratingProcessInstanceValidationException;
import org.camunda.bpm.engine.migration.MigrationPlanValidationException;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationLogger extends ProcessEngineLogger {

  public MigrationPlanValidationException failingMigrationPlanValidation(MigrationPlanValidationReportImpl validationReport) {
    StringBuilder sb = new StringBuilder();
    validationReport.writeTo(sb);
    return new MigrationPlanValidationException(exceptionMessage(
      "001",
      "{}",
      sb.toString()),
      validationReport);
  }

  public ProcessEngineException processDefinitionOfInstanceDoesNotMatchMigrationPlan(ExecutionEntity processInstance, String processDefinitionId) {
    return new ProcessEngineException(exceptionMessage(
      "002",
      "Process instance '{}' cannot be migrated. Its process definition '{}' does not match the source process definition of the migration plan '{}'",
      processInstance.getId(), processInstance.getProcessDefinitionId(), processDefinitionId
    ));
  }

  public ProcessEngineException processInstanceDoesNotExist(String processInstanceId) {
    return new ProcessEngineException(exceptionMessage(
      "003",
      "Process instance '{}' cannot be migrated. The process instance does not exist",
      processInstanceId
    ));
  }

  public MigratingProcessInstanceValidationException failingMigratingProcessInstanceValidation(MigratingProcessInstanceValidationReportImpl validationReport) {
    StringBuilder sb = new StringBuilder();
    validationReport.writeTo(sb);
    return new MigratingProcessInstanceValidationException(exceptionMessage(
      "004",
      "{}",
      sb.toString()),
      validationReport);
  }

  public ProcessEngineException cannotBecomeSubordinateInNonScope(MigratingActivityInstance activityInstance) {
    return new ProcessEngineException(exceptionMessage(
      "005",
      "{}",
      "Cannot attach a subordinate to activity instance '{}'. Activity '{}' is not a scope",
      activityInstance.getActivityInstance().getId(),
      activityInstance.getActivityInstance().getActivityId()));
  }

  public ProcessEngineException cannotDestroySubordinateInNonScope(MigratingActivityInstance activityInstance) {
    return new ProcessEngineException(exceptionMessage(
        "006",
        "{}",
        "Cannot destroy a subordinate of activity instance '{}'. Activity '{}' is not a scope",
        activityInstance.getActivityInstance().getId(),
        activityInstance.getActivityInstance().getActivityId()));
  }

  public ProcessEngineException cannotAttachToTransitionInstance(MigratingInstance attachingInstance) {
    return new ProcessEngineException(exceptionMessage(
        "007",
        "{}",
        "Cannot attach instance '{}' to a transition instance",
        attachingInstance));
  }

  public BadUserRequestException processDefinitionDoesNotExist(String processDefinitionId, String type) {
    return new BadUserRequestException(exceptionMessage(
      "008",
      "{} process definition with id '{}' does not exist",
      type, processDefinitionId
    ));
  }

  public ProcessEngineException cannotMigrateBetweenTenants(String sourceTenantId, String targetTenantId) {
    return new ProcessEngineException(exceptionMessage(
        "09",
        "Cannot migrate process instances between processes of different tenants ('{}' != '{}')",
        sourceTenantId,
        targetTenantId));
  }

  public ProcessEngineException cannotMigrateInstanceBetweenTenants(String processInstanceId, String sourceTenantId, String targetTenantId) {

    String detailMessage = null;
    if (sourceTenantId != null) {
      detailMessage = exceptionMessage(
          "010",
          "Cannot migrate process instance '{}' to a process definition of a different tenant ('{}' != '{}')",
          processInstanceId,
          sourceTenantId,
          targetTenantId);
    }
    else {
      detailMessage = exceptionMessage(
          "010",
          "Cannot migrate process instance '{}' without tenant to a process definition with a tenant ('{}')",
          processInstanceId,
          targetTenantId);
    }

    return new ProcessEngineException(detailMessage);
  }

  public ProcessEngineException cannotHandleChild(MigratingScopeInstance scopeInstance, MigratingProcessElementInstance childCandidate) {
    return new ProcessEngineException(
        exceptionMessage(
            "011",
            "Scope instance of type {} cannot have child of type {}",
            scopeInstance.getClass().getSimpleName(),
            childCandidate.getClass().getSimpleName())
        );
  }

}
