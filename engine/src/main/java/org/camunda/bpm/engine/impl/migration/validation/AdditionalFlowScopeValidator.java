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
package org.camunda.bpm.engine.impl.migration.validation;

import org.camunda.bpm.engine.impl.migration.instance.MigratingActivityInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingProcessInstance;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;

/**
 * Validates that an activity instance is migrated below its migrated parent activity instance.
 *
 * @author Thorben Lindhauer
 */
public class AdditionalFlowScopeValidator implements MigrationInstructionInstanceValidator {

  @Override
  public void validate(MigratingProcessInstance migratingProcessInstance, MigratingActivityInstance migratingActivityInstance,
      MigrationInstructionInstanceValidationReportImpl validationReport) {
    MigratingActivityInstance ancestorInstance = getClosestPreservedAncestorInstance(migratingActivityInstance);
    ScopeImpl targetScope = migratingActivityInstance.getTargetScope();

    // note: this also detects and rejects horizontal migration. In that case, the parent activity instance
    // is migrated to a scope that is not present in this activity instance's target scope parent hierarchy
    if (ancestorInstance != null && targetScope != null && targetScope != targetScope.getProcessDefinition()) {
      ScopeImpl parentInstanceTargetScope = ancestorInstance.getTargetScope();
      if (parentInstanceTargetScope != null && !parentInstanceTargetScope.isAncestorFlowScopeOf(targetScope)) {
        validationReport.addValidationFailure(migratingActivityInstance, "Closest migrating ancestor activity instance is migrated "
            + "to activity '" + parentInstanceTargetScope.getId() + "' which is not an ancestor of target activity '"
            + targetScope.getId() + "'");
      }
    }
  }

  protected MigratingActivityInstance getClosestPreservedAncestorInstance(MigratingActivityInstance activityInstance) {
    MigratingActivityInstance parent = activityInstance.getParent();

    while (parent != null && parent.getTargetScope() == null) {
      parent = parent.getParent();
    }

    return parent;
  }
}
