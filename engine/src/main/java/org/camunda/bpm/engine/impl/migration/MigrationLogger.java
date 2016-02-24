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

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.migration.validation.MigrationInstructionInstanceValidationReportImpl;
import org.camunda.bpm.engine.impl.migration.validation.MigrationPlanValidationReportImpl;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.migration.MigrationInstructionInstanceValidationException;
import org.camunda.bpm.engine.migration.MigrationPlanValidationException;
import org.camunda.bpm.engine.runtime.ActivityInstance;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationLogger extends ProcessEngineLogger {

  public ProcessEngineException unmappedActivityInstances(String processInstanceId, Set<ActivityInstance> unmappedInstances) {
    return new ProcessEngineException(exceptionMessage(
        "001",
        "Process instance '{}' cannot be migrated. There are no migration instructions that apply to the following activity instances: {}",
        processInstanceId, formatActivityInstances(unmappedInstances)));
  }

  public ProcessEngineException processDefinitionOfInstanceDoesNotMatchMigrationPlan(ExecutionEntity processInstance, String processDefinitionId) {
    return new ProcessEngineException(exceptionMessage(
      "002",
      "Process instance '{}' cannot be migrated. Its process definition '{}' does not match the source process definition of the migration plan '{}'",
      processInstance.getId(), processInstance.getProcessDefinitionId(), processDefinitionId
    ));
  }

  public MigrationInstructionInstanceValidationException failingInstructionInstanceValidation(MigrationInstructionInstanceValidationReportImpl validationReport) {
    StringBuilder sb = new StringBuilder();
    validationReport.writeTo(sb);
    return new MigrationInstructionInstanceValidationException(exceptionMessage("003", "Cannot migrate process instance '{}': {}",
      validationReport.getMigratingProcessInstance().getProcessInstanceId(),
      sb.toString()), validationReport);
  }

  public MigrationPlanValidationException failingMigrationPlanValidation(MigrationPlanValidationReportImpl validationReport) {
    StringBuilder sb = new StringBuilder();
    validationReport.writeTo(sb);
    return new MigrationPlanValidationException(exceptionMessage("004", "Cannot migrate process definition '{}' to '{}': {}",
      validationReport.getMigrationPlan().getSourceProcessDefinitionId(),
      validationReport.getMigrationPlan().getTargetProcessDefinitionId(),
      sb.toString()), validationReport);
  }

  public ProcessEngineException processInstanceDoesNotExist(String processInstanceId) {
    return new ProcessEngineException(exceptionMessage(
      "005",
      "Process instance '{}' cannot be migrated. The process instance does not exist",
      processInstanceId
    ));
  }

  protected String formatActivityInstances(Collection<ActivityInstance> activityInstances) {
    StringBuilder sb = new StringBuilder();

    Iterator<ActivityInstance> iterator = activityInstances.iterator();
    while (iterator.hasNext()) {
      sb.append(iterator.next().getId());
      if (iterator.hasNext()) {
        sb.append(", ");
      }
    }

    return sb.toString();
  }
}
